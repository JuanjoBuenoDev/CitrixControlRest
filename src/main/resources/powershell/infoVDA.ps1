param (
    [string]$ddc
)

# Cargar Snap-in si no está cargado
if (-not (Get-PSSnapin -Name Citrix.Broker.Admin.V2 -ErrorAction SilentlyContinue)) {
    Add-PSSnapin Citrix.Broker.Admin.V2 -ErrorAction Stop
}

# Inicializar resultados
$result = @()

    try {
        $vdaList = Get-BrokerMachine -AdminAddress $ddc -ErrorAction Stop

        foreach ($vda in $vdaList) {
            $tags = @()
            if ($vda.Tags) {
                $tags = $vda.Tags
            }

            $apps = @()
            try {
                $apps = Get-BrokerApplication -AdminAddress $ddc -MachineName $vda.MachineName | Select-Object -ExpandProperty Name
            } catch {
                $apps = @()
            }

            $result += [PSCustomObject]@{
                machineName          = $vda.MachineName
                catalogName          = $vda.CatalogName
                registrationState    = $vda.RegistrationState
                powerState           = $vda.PowerState
                inMaintenanceMode    = $vda.InMaintenanceMode
                loadIndex            = $vda.LoadIndex
                agentVersion         = $vda.AgentVersion
                desktopGroupName     = $vda.DesktopGroupName
                osType               = $vda.OsType
                deliveryType         = $vda.DeliveryType
                ipAddress            = $vda.IpAddress
                isPhysical           = $vda.IsPhysical
                lastRegistrationTime = $vda.LastRegistrationTime
                persistUserChanges   = $vda.PersistUserChanges
                sessionsEstablished  = $vda.SessionsEstablished
                applications         = $apps
            }
        }
    } catch {
        # En caso de error con un DDC, devolver objeto con datos por defecto
        $result += [PSCustomObject]@{
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
            persistUserChanges   = $false
            sessionsEstablished  = 0
            applications         = @()
        }
    }


# Salida en JSON
$result | ConvertTo-Json -Depth 4
