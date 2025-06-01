param (
    [string]$ddc  # Solo un DDC
)

# Cargar Snap-in Citrix si no está cargado
if (-not (Get-PSSnapin -Name Citrix.Broker.Admin.V2 -ErrorAction SilentlyContinue)) {
    Add-PSSnapin Citrix.Broker.Admin.V2 -ErrorAction Stop
}

$result = @()

try {
    # Obtener grupos de escritorio del DDC
    $deliveryGroups = Get-BrokerDesktopGroup -AdminAddress $ddc -ErrorAction Stop
    
    foreach ($dg in $deliveryGroups) {
        try {
            # Obtener solo los nombres de las máquinas VDA del grupo
            $vdaNames = @(Get-BrokerMachine -AdminAddress $ddc -DesktopGroupName $dg.Name -ErrorAction Stop | 
                        Select-Object -ExpandProperty MachineName)
            
            # Obtener información adicional para cálculos
            $vdaDetails = @(Get-BrokerMachine -AdminAddress $ddc -DesktopGroupName $dg.Name -ErrorAction Stop | 
                        Select-Object LoadIndex, SessionCount)

            # Calcular promedio de LoadIndex y total de sesiones
            $avgLoadIndex = if ($vdaDetails.Count -gt 0) { 
                [math]::Round(($vdaDetails | Measure-Object -Property LoadIndex -Average).Average, 2) 
            } else { 0 }
            
            $totalSessions = ($vdaDetails | Measure-Object -Property SessionCount -Sum).Sum

            # Obtener información de reinicio
            $schedule = Get-BrokerRebootSchedule -AdminAddress $ddc -DesktopGroupUid $dg.Uid -ErrorAction SilentlyContinue
            
            $result += [PSCustomObject]@{
                # Información básica del grupo
                uid               = $dg.Uid
                name              = $dg.Name
                state             = $dg.State
                sessionCount      = $totalSessions
                vdas              = $vdaNames  # Solo nombres de máquinas
                averageLoadIndex  = $avgLoadIndex
                isMaintenanceMode = $dg.IsInMaintenanceMode
                
                # Información de reinicio
                rebootEnabled       = if ($schedule) { $schedule.Enabled } else { $false }
                rebootFrequency     = if ($schedule) { $schedule.Frequency } else { $null }
                rebootDaysOfWeek    = if ($schedule -and $schedule.WeekDays) { $schedule.WeekDays -join ',' } else { $null }
                rebootStartTime     = if ($schedule) { $schedule.StartTime } else { $null }
                rebootDuration      = if ($schedule) { $schedule.Duration } else { $null }
            }
        } catch {
            # Error procesando un grupo específico
            $result += [PSCustomObject]@{
                uid               = $dg.Uid
                name              = "[Error] $($dg.Name)"
                state             = "Error"
                sessionCount      = 0
                vdas              = @()  # Array vacío
                averageLoadIndex  = 0
                isMaintenanceMode = $false
                rebootEnabled       = $false
                rebootFrequency     = $null
                rebootDaysOfWeek    = $null
                rebootStartTime     = $null
                rebootDuration      = $null
            }
        }
    }
} catch {
    # Error al conectar con el DDC
    $result += [PSCustomObject]@{
        uid               = $null
        name              = "Error en DDC: $ddc"
        state             = "Error"
        sessionCount      = 0
        vdas              = @()  # Array vacío
        averageLoadIndex  = 0
        isMaintenanceMode = $false
        rebootEnabled       = $false
        rebootFrequency     = $null
        rebootDaysOfWeek    = $null
        rebootStartTime     = $null
        rebootDuration      = $null
    }
}

$result | ConvertTo-Json -Depth 5