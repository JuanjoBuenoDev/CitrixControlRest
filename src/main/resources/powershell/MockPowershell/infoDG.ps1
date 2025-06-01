param (
    [string]$ddc
)

# Generate mock delivery groups
$mockGroups = @(
    [PSCustomObject]@{
        Uid = [guid]::NewGuid()
        Name = "Sales-VDIs"
        State = "Active"
        IsInMaintenanceMode = $false
    },
    [PSCustomObject]@{
        Uid = [guid]::NewGuid()
        Name = "Finance-VDIs"
        State = "Active"
        IsInMaintenanceMode = $true
    },
    [PSCustomObject]@{
        Uid = [guid]::NewGuid()
        Name = "Dev-VDIs"
        State = "Active"
        IsInMaintenanceMode = $false
    }
)

# Generate mock reboot schedules (some groups will have them)
$mockSchedules = @{
    ($mockGroups[0].Uid) = [PSCustomObject]@{
        Enabled = $true
        Frequency = "Weekly"
        WeekDays = @("Tuesday", "Thursday")
        StartTime = "03:00:00"
        Duration = "02:00:00"
        WarningDuration = "00:15:00"
        NaturalReboot = $false
    }
}

$result = @()

try {
    foreach ($dg in $mockGroups) {
        try {
            # Generate dynamic VDA data
            $vdaCount = switch ($dg.Name) {
                "Sales-VDIs" { Get-Random -Minimum 10 -Maximum 20 }
                "Finance-VDIs" { Get-Random -Minimum 5 -Maximum 10 }
                default { Get-Random -Minimum 3 -Maximum 8 }
            }
            
            $vdaNames = @()
            $vdaDetails = @()
            1..$vdaCount | ForEach-Object {
                $vdaNames += "VDA-$($dg.Name.Split('-')[0])-$_"
                $vdaDetails += [PSCustomObject]@{
                    LoadIndex = Get-Random -Minimum 10 -Maximum 90
                    SessionCount = Get-Random -Minimum 0 -Maximum 5
                }
            }
            
            # Calculate averages
            $avgLoadIndex = if ($vdaDetails.Count -gt 0) { 
                [math]::Round(($vdaDetails | Measure-Object -Property LoadIndex -Average).Average, 2) 
            } else { 0 }
            
            $totalSessions = ($vdaDetails | Measure-Object -Property SessionCount -Sum).Sum
            
            # Get reboot schedule if exists
            $schedule = $mockSchedules[$dg.Uid]
            
            $result += [PSCustomObject]@{
                uid = $dg.Uid.ToString()
                name = $dg.Name
                state = $dg.State
                sessionCount = $totalSessions
                vdas = $vdaNames
                averageLoadIndex = $avgLoadIndex
                isMaintenanceMode = $dg.IsInMaintenanceMode
                rebootEnabled = if ($schedule) { $schedule.Enabled } else { $false }
                rebootFrequency = if ($schedule) { $schedule.Frequency } else { $null }
                rebootDaysOfWeek = if ($schedule) { $schedule.WeekDays -join ',' } else { $null }
                rebootStartTime = if ($schedule) { $schedule.StartTime } else { $null }
                rebootDuration = if ($schedule) { $schedule.Duration } else { $null }
            }
        } catch {
            $result += [PSCustomObject]@{
                uid = $dg.Uid.ToString()
                name = "[Error] $($dg.Name)"
                state = "Error"
                sessionCount = 0
                vdas = @()
                averageLoadIndex = 0
                isMaintenanceMode = $false
                rebootEnabled = $false
                rebootFrequency = $null
                rebootDaysOfWeek = $null
                rebootStartTime = $null
                rebootDuration = $null
            }
        }
    }
} catch {
    $result += [PSCustomObject]@{
        uid = $null
        name = "Error en DDC: $ddc"
        state = "Error"
        sessionCount = 0
        vdas = @()
        averageLoadIndex = 0
        isMaintenanceMode = $false
        rebootEnabled = $false
        rebootFrequency = $null
        rebootDaysOfWeek = $null
        rebootStartTime = $null
        rebootDuration = $null
    }
}

$result | ConvertTo-Json -Depth 5