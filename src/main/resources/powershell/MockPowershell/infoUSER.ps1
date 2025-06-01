param (
    [string]$ddc
)

# Listas de nombres y apellidos comunes para generar usuarios con formato nombre.apellido@company.com
$firstNames = @("john", "jane", "robert", "sarah", "michael", "emily", "david", "linda", "james", "patricia", "charles", "barbara", "thomas", "jennifer", "daniel", "mary", "mark", "nancy", "steven", "laura")
$lastNames = @("doe", "smith", "johnson", "williams", "brown", "jones", "miller", "davis", "garcia", "rodriguez", "wilson", "martinez", "anderson", "taylor", "thomas", "hernandez", "moore", "martin", "jackson", "lee")

# Generar lista de 50 usuarios combinando aleatoriamente nombres y apellidos (sin repetir)
$allUsers = @()
while ($allUsers.Count -lt 50) {
    $first = $firstNames | Get-Random
    $last = $lastNames | Get-Random
    $user = "$first.$last@company.com"
    if (-not $allUsers.Contains($user)) {
        $allUsers += $user
    }
}

$mockMachines = @("VDA-SALES-01", "VDA-FIN-02", "VDA-DEV-03", "VDA-HR-04")
$mockDesktopGroups = @("Sales-VDIs", "Finance-VDIs", "Dev-VDIs", "HR-VDIs")
$mockApplications = @("Word", "Excel", "Chrome", "Outlook", "SAP", "CustomApp")
$mockFailureReasons = @("None", "LicenseLimit", "NoAvailableWorkers", "Maintenance", $null)

# Elegir aleatoriamente entre 30 y 50 usuarios para esta consulta
$userCount = Get-Random -Minimum 30 -Maximum 51
$selectedUsers = $allUsers | Get-Random -Count $userCount

$result = @()

try {
    foreach ($user in $selectedUsers) {
        try {
            $sessionCount = Get-Random -Minimum 1 -Maximum 3
            $sessions = @()
            1..$sessionCount | ForEach-Object {
                $sessions += [PSCustomObject]@{
                    UserName = $user
                    MachineName = $mockMachines | Get-Random
                    DesktopGroupName = $mockDesktopGroups | Get-Random
                    ApplicationsInUse = $mockApplications | Get-Random -Count (Get-Random -Minimum 1 -Maximum 4)
                    LastConnectionFailureReason = $mockFailureReasons | Get-Random
                    LastFailureEndTime = if ((Get-Random -Maximum 10) -lt 3) { 
                        (Get-Date).AddHours(-(Get-Random -Minimum 1 -Maximum 24)).ToString("o") 
                    } else { $null }
                }
            }

            $machines = $sessions | Select-Object -ExpandProperty MachineName -Unique
            $desktopGroups = $sessions | Select-Object -ExpandProperty DesktopGroupName -Unique
            $appsInUse = $sessions | ForEach-Object { $_.ApplicationsInUse } | Where-Object { $_ } | Select-Object -Unique

            $userInfo = [PSCustomObject]@{
                username                    = $user
                lastConnectionFailureReason = $sessions[0].LastConnectionFailureReason
                lastFailureEndTime          = $sessions[0].LastFailureEndTime
                lastMachineUsed             = $sessions[0].MachineName
                aplicacionesEnUso           = @($appsInUse)
                maquinas                    = @($machines)
                desktopGroups               = @($desktopGroups)
            }

            $result += $userInfo
        } catch {
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
} catch {
    Write-Warning "Error en DDC $ddc : $_"
    @() | ConvertTo-Json -Depth 5
    exit
}

$result | ConvertTo-Json -Depth 5
