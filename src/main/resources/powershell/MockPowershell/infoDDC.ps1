param (
    [string]$ddc
)

# Simular obtener otros DDCs relacionados a partir del DDC recibido
# Por ejemplo, si te pasan "MainDDC", generamos 3 DDCs: MainDDC, MainDDC-1, MainDDC-2
$relatedDDCs = @($ddc, "$ddc-1", "$ddc-2")

$resultList = @()

foreach ($currentDDC in $relatedDDCs) {
    $controllerData = @{
        dnsName = $currentDDC
        state = if ((Get-Random) -gt 0.1) { "Active" } else { "Inactive" }
        desktopsRegistered = Get-Random -Minimum 10 -Maximum 100
    }

    if ($currentDDC -eq "error") {
        $result = [PSCustomObject]@{
            dnsName = $currentDDC
            state = "Error"
            desktopsRegistered = 0
        }
    }
    else {
        $result = [PSCustomObject]$controllerData
    }

    $resultList += $result
}

# Siempre devuelve una lista JSON
$resultList | ConvertTo-Json -Depth 5
