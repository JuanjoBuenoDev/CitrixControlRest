param (
    [string]$ddc
)

# Configuración inicial
$ErrorActionPreference = "Stop"
$mockCatalogs = @("Win10-Catalog", "Win11-Catalog", "Server-Catalog")
$mockDesktopGroups = @("Sales-VDIs", "Finance-VDIs", "Dev-VDIs")
$mockApplications = @("Office", "Chrome", "Adobe", "SAP", "CustomApp")

function New-MockVDA {
    param(
        [int]$Index,
        [string[]]$Catalogs,
        [string[]]$DesktopGroups,
        [string[]]$Applications
    )
    
    try {
        $catalog = $Catalogs | Get-Random -ErrorAction Stop
        $desktopGroup = $DesktopGroups | Get-Random -ErrorAction Stop
        
        # Validación adicional
        if ([string]::IsNullOrEmpty($desktopGroup)) {
            throw "Desktop group is null or empty"
        }
        
        $groupPrefix = ($desktopGroup -split '-')[0]
        $appCount = Get-Random -Minimum 0 -Maximum 4 -ErrorAction Stop
        if ($appCount -eq 0) {
            $apps = @()
        } else {
            $apps = @($Applications | Get-Random -Count $appCount -ErrorAction Stop)
        }
        
        
        # Generar IP segura
        $ipOctet3 = Get-Random -Minimum 1 -Maximum 254 -ErrorAction Stop
        $ipOctet4 = Get-Random -Minimum 1 -Maximum 254 -ErrorAction Stop
        $ip = "192.168.$ipOctet3.$ipOctet4"
        
        # Generar fecha segura
        $daysOffset = Get-Random -Minimum 0 -Maximum 30 -ErrorAction Stop
        $hoursOffset = Get-Random -Minimum 0 -Maximum 24 -ErrorAction Stop
        $regTime = (Get-Date).AddDays(-$daysOffset).AddHours(-$hoursOffset)
        
        [PSCustomObject]@{
            machineName          = "VDA-$groupPrefix-$Index"
            catalogName          = $catalog
            registrationState    = @("Registered", "Unregistered", "Initializing") | Get-Random -ErrorAction Stop
            powerState           = @("On", "Off", "Suspended") | Get-Random -ErrorAction Stop
            inMaintenanceMode    = (Get-Random -Maximum 10 -ErrorAction Stop) -lt 2
            loadIndex            = Get-Random -Minimum 10 -Maximum 90 -ErrorAction Stop
            agentVersion         = "2203." + (Get-Random -Minimum 1000 -Maximum 9999 -ErrorAction Stop)
            desktopGroupName     = $desktopGroup
            osType               = if ($catalog -like "*Server*") { "Server" } else { "Workstation" }
            deliveryType         = @("DesktopsOnly", "AppsOnly", "DesktopsAndApps") | Get-Random -ErrorAction Stop
            ipAddress            = $ip
            isPhysical           = $false
            lastRegistrationTime = $regTime.ToString("o")
            persistUserChanges   = @("Discard", "OnLocal", "OnPvd") | Get-Random -ErrorAction Stop
            sessionsEstablished  = Get-Random -Minimum 0 -Maximum 5 -ErrorAction Stop
            applications         = $apps
        }
    } catch {
        Write-Warning "Error generando VDA mock: $_"
        [PSCustomObject]@{
            machineName          = "Error-Mock-$Index"
            catalogName          = $null
            registrationState    = $null
            powerState           = $null
            inMaintenanceMode    = $false
            loadIndex            = 0
            agentVersion         = $null
            desktopGroupName     = $null
            osType               = $null
            deliveryType         = $null
            ipAddress            = $null
            isPhysical           = $false
            lastRegistrationTime = $null
            persistUserChanges   = $null
            sessionsEstablished  = 0
            applications         = @()
        }
    }
}

# Generar lista de resultados
try {
    $vdaCount = Get-Random -Minimum 5 -Maximum 15 -ErrorAction Stop
    $resultList = 1..$vdaCount | ForEach-Object {
        New-MockVDA -Index $_ -Catalogs $mockCatalogs -DesktopGroups $mockDesktopGroups -Applications $mockApplications
    }
} catch {
    Write-Error "Error general en la generación de datos mock: $_"
    $resultList = @([PSCustomObject]@{
        machineName          = "Error en DDC: $ddc"
        catalogName          = $null
        registrationState    = $null
        powerState           = $null
        inMaintenanceMode    = $false
        loadIndex            = 0
        agentVersion         = $null
        desktopGroupName     = $null
        osType               = $null
        deliveryType         = $null
        ipAddress            = $null
        isPhysical           = $false
        lastRegistrationTime = $null
        persistUserChanges   = $null
        sessionsEstablished  = 0
        applications         = @()
    })
}

# Salida
$resultList | ConvertTo-Json -Depth 5