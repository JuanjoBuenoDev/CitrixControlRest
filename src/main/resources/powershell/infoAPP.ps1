param (
    [string]$ddc
)

# Cargar Snap-in si no está cargado
if (-not (Get-PSSnapin -Name Citrix.Broker.Admin.V2 -ErrorAction SilentlyContinue)) {
    Add-PSSnapin Citrix.Broker.Admin.V2 -ErrorAction Stop
}

$result = @()

try {
    # Obtener todas las aplicaciones (corregido el AdminAddress que estaba hardcodeado)
    $apps = Get-BrokerApplication -AdminAddress $ddc -MaxRecordCount 10000 -ErrorAction Stop

    foreach ($app in $apps) {
        try {
            # Obtener información de sesiones para esta aplicación
            $sessions = Get-BrokerSession -AdminAddress $ddc -MaxRecordCount 10000 | Where-Object { $_.ApplicationsInUse -contains $app.Name }
            
            # Procesar datos de sesiones
            $dgs = ($sessions | Select-Object -ExpandProperty DesktopGroupName | Sort-Object -Unique) -join ','
            $vdas = ($sessions | Select-Object -ExpandProperty MachineName | Sort-Object -Unique) -join ','
            $activeUsers = ($sessions | Select-Object -ExpandProperty UserName | Sort-Object -Unique) -join ','
            
            # Obtener el directorio del ejecutable (corregido para usar Split-Path como en tu versión funcional)
            $directory = if ($app.CommandLineExecutable) { Split-Path $app.CommandLineExecutable -ErrorAction SilentlyContinue } else { $null }
            
            # Obtener tags asociados (corregido para unirlos con comas)
            $tags = if ($app.AssociatedTags) { $app.AssociatedTags -join ',' } else { $null }

            # Construir objeto de resultado
            $result += [PSCustomObject]@{
                uid                   = $app.Uid
                name                  = $app.Name
                applicationName       = $app.ApplicationName
                browserName           = $app.BrowserName
                publishedName        = $app.PublishedName
                maxTotalInstances     = $app.MaxTotalInstances
                maxPerUserInstances   = $app.MaxPerUserInstances
                commandLineExecutable = $app.CommandLineExecutable
                commandLineArguments  = $app.CommandLineArguments
                directory             = $directory
                userFolder            = $app.UserFolder
                desktopGroups         = $dgs
                vdas                  = $vdas
                activeUsers           = $activeUsers
                enabled               = $app.Enabled
                executablePath        = $app.CommandLineExecutable  # Usando CommandLineExecutable para coincidir
            }
        } catch {
            # Manejar errores en el procesamiento de cada aplicación
            $result += [PSCustomObject]@{
                uid                   = $app.Uid
                name                  = $app.Name
                applicationName       = "Error procesando aplicación"
                browserName           = $null
                publishedName         = $null
                maxTotalInstances     = 0
                maxPerUserInstances   = 0
                commandLineExecutable = $null
                commandLineArguments  = $null
                directory             = $null
                userFolder            = $null
                desktopGroups         = @()
                vdas                 = @()
                activeUsers           = @()
                enabled               = $false
                executablePath        = $null
            }
        }
    }
} catch {
    # Manejar errores generales
    $result += [PSCustomObject]@{
        uid                   = $null
        name                  = "Error en DDC: $ddc"
        applicationName       = $null
        browserName           = $null
        publishedName         = $null
        maxTotalInstances     = 0
        maxPerUserInstances   = 0
        commandLineExecutable = $null
        commandLineArguments  = $null
        directory             = $null
        userFolder            = $null
        desktopGroups         = @()
        vdas                  = @()
        activeUsers           = @()
        enabled               = $false
        executablePath        = $null
    }
}

$result | ConvertTo-Json -Depth 5