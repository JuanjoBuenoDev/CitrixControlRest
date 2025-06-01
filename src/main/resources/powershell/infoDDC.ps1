param (
    [string]$ddc
)

# Cargar el Snap-In si no está presente
if (-not (Get-PSSnapin -Name Citrix.Broker.Admin.V2 -ErrorAction SilentlyContinue)) {
    Add-PSSnapin Citrix.Broker.Admin.V2 -ErrorAction Stop
}

# Inicializar resultados
$result = @()

try {
    $controllerInfo = Get-BrokerController -AdminAddress $ddc -ErrorAction Stop | Select-Object `
        @{Name = "dnsName"; Expression = { $_.DNSName }},
        @{Name = "state"; Expression = { $_.State }},
        @{Name = "desktopsRegistered"; Expression = { $_.DesktopsRegistered }}

    $result += $controllerInfo
} catch {
    $result += [PSCustomObject]@{
        dnsName = $ddc
        state = "Error"
        desktopsRegistered = 0
    }
}

$result | ConvertTo-Json -Depth 3