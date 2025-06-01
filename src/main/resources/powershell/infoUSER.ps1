param (
    [string]$ddc
)

# Cargar Snap-in Citrix si no está cargado
if (-not (Get-PSSnapin -Name Citrix.Broker.Admin.V2 -ErrorAction SilentlyContinue)) {
    Add-PSSnapin Citrix.Broker.Admin.V2 -ErrorAction Stop
}

# Inicializar el conjunto de usuarios activos
$usernames = @()

try {
    $sessions = Get-BrokerSession -AdminAddress $ddc -SessionState "Active" -ErrorAction Stop
    $usernames = $sessions | Select-Object -ExpandProperty UserName -Unique
} catch {
    Write-Warning "Error al obtener sesiones del DDC $ddc : $_"
    # Retornar array vacío si hay error
    @() | ConvertTo-Json -Depth 5
    exit
}

# Inicializar resultados
$result = @()

foreach ($user in $usernames) {
    try {
        $userSessions = Get-BrokerSession -AdminAddress $ddc -UserName $user -ErrorAction Stop
        
        # Obtener máquinas y grupos de escritorio únicos
        $machines = $userSessions | Select-Object -ExpandProperty MachineName -Unique
        $desktopGroups = $userSessions | Select-Object -ExpandProperty DesktopGroupName -Unique
        
        # Obtener aplicaciones en uso (sin duplicados)
        $appsInUse = $userSessions | ForEach-Object { $_.ApplicationsInUse } | Where-Object { $_ } | Select-Object -Unique
        
        $userInfo = [PSCustomObject]@{
            username                    = $user
            lastConnectionFailureReason = $userSessions[0].LastConnectionFailureReason  # Tomamos el primero
            lastFailureEndTime          = $userSessions[0].LastFailureEndTime
            lastMachineUsed             = $userSessions[0].MachineName  # Última máquina usada
            aplicacionesEnUso           = @($appsInUse)
            maquinas                    = @($machines)
            desktopGroups               = @($desktopGroups)
        }
        
        $result += $userInfo
    } catch {
        # Si hay error (usuario no encontrado u otro), agregar objeto vacío
        $result += [PSCustomObject]@{
            username                    = $user
            lastConnectionFailureReason = $null
            lastFailureEndTime          = $null
            lastMachineUsed             = $null
            aplicacionesEnUso           = @()
            maquinas                    = @()
            desktopGroups               = @()
        }
    }
}

$result | ConvertTo-Json -Depth 5
