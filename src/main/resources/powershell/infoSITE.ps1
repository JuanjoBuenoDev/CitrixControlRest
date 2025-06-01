param (
    [string]$ddc
)

# Cargar Snap-in si no está cargado
if (-not (Get-PSSnapin -Name Citrix.Broker.Admin.V2 -ErrorAction SilentlyContinue)) {
    Add-PSSnapin Citrix.Broker.Admin.V2 -ErrorAction Stop
}

try {
    $siteInfo = Get-BrokerSite -ErrorAction Stop | Select-Object `
        @{Name="licenseEdition"; Expression = { $_.LicenseEdition }}, `
        @{Name="licenseServerName"; Expression = { $_.LicenseServerName }}, `
        @{Name="licenseServerPort"; Expression = { $_.LicenseServerPort }}, `
        @{Name="licensingGraceHoursLeft"; Expression = { $_.LicensingGraceHoursLeft }}, `
        @{Name="licensingGracePeriodActive"; Expression = { $_.LicensingGracePeriodActive }}, `
        @{Name="localHostCacheEnabled"; Expression = { $_.LocalHostCacheEnabled }}, `
        @{Name="peakConcurrentLicenseUsers"; Expression = { $_.PeakConcurrentLicenseUsers }}, `
        @{Name="peakConcurrentLicensedDevices"; Expression = { $_.PeakConcurrentLicensedDevices }}
    
    # Get monitor and log datastore status
    $monitorSiteInfo = Get-MonitorDataStore -AdminAddress $ddc | Where-Object {$_.DataStore -eq 'site'} | Select-Object -ExpandProperty Status
    $monitorMonitorInfo = Get-MonitorDataStore -AdminAddress $ddc | Where-Object {$_.DataStore -eq 'monitor'} | Select-Object -ExpandProperty Status
    $logInfo = Get-LogDataStore -AdminAddress $ddc | Where-Object {$_.DataStore -eq 'site'} | Select-Object -ExpandProperty Status

    $result = [PSCustomObject]@{
        licenseEdition             = $siteInfo.licenseEdition
        licenseServerName          = $siteInfo.licenseServerName
        licenseServerPort          = $siteInfo.licenseServerPort
        licensingGraceHoursLeft    = $siteInfo.licensingGraceHoursLeft
        licensingGracePeriodActive = $siteInfo.licensingGracePeriodActive
        localHostCacheEnabled     = $siteInfo.localHostCacheEnabled
        peakConcurrentLicenseUsers = $siteInfo.peakConcurrentLicenseUsers
        peakConcurrentLicensedDevices = $siteInfo.peakConcurrentLicensedDevices
        dataStoreSite             = $monitorSiteInfo
        dataStoreMonitor         = $monitorMonitorInfo
        dataStoreLog             = $logInfo
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
