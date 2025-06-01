param (
    [string]$ddc
)

# Generate realistic mock data
try {
    # Simulate business hours impact (8AM-6PM)
    $isBusinessHours = (Get-Date).Hour -ge 8 -and (Get-Date).Hour -lt 18
    $loadMultiplier = if ($isBusinessHours) { 1.5 } else { 0.7 }
    
    # Mock Get-BrokerSite data
    $siteInfo = [PSCustomObject]@{
        licenseEdition = "Premium"
        licenseServerName = "lic-server-01.example.com"
        licenseServerPort = 27000
        licensingGraceHoursLeft = (Get-Random -Minimum 24 -Maximum 720) # 1-30 days
        licensingGracePeriodActive = $false
        localHostCacheEnabled = $true
        peakConcurrentLicenseUsers = [math]::Round((Get-Random -Minimum 100 -Maximum 300) * $loadMultiplier)
        peakConcurrentLicensedDevices = [math]::Round((Get-Random -Minimum 120 -Maximum 350) * $loadMultiplier)
    }

    # Mock datastore status with occasional degraded states
    $dataStoreStatus = if ((Get-Random -Minimum 1 -Maximum 100) -gt 90) { 
        "Degraded" 
    } else { 
        "OK" 
    }
    
    $logStatus = if ((Get-Date).Minute -lt 5) { "Degraded" } else { "OK" }

    $result = [PSCustomObject]@{
        licenseEdition             = $siteInfo.licenseEdition
        licenseServerName          = $siteInfo.licenseServerName
        licenseServerPort          = $siteInfo.licenseServerPort
        licensingGraceHoursLeft    = $siteInfo.licensingGraceHoursLeft
        licensingGracePeriodActive = $siteInfo.licensingGraceHoursLeft -lt 24
        localHostCacheEnabled     = $siteInfo.localHostCacheEnabled
        peakConcurrentLicenseUsers = $siteInfo.peakConcurrentLicenseUsers
        peakConcurrentLicensedDevices = $siteInfo.peakConcurrentLicensedDevices
        dataStoreSite             = $dataStoreStatus
        dataStoreMonitor         = $dataStoreStatus
        dataStoreLog             = $logStatus
    }
} catch {
    $result = [PSCustomObject]@{
        licenseEdition             = $null
        licenseServerName          = $null
        licenseServerPort          = $null
        licensingGraceHoursLeft    = 0
        licensingGracePeriodActive = $false
        localHostCacheEnabled      = $false
        peakConcurrentLicenseUsers = 0
        peakConcurrentLicensedDevices = 0
        dataStoreSite = $null
        dataStoreMonitor = $null
        dataStoreLog = $null
    }
}

$result | ConvertTo-Json -Depth 3