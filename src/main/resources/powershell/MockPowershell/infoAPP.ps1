param (
    [string]$ddc
)

# Generate mock application data
$mockApplications = @(
    [PSCustomObject]@{
        Uid = [guid]::NewGuid()
        Name = "Microsoft Word"
        ApplicationName = "WINWORD.EXE"
        BrowserName = "Word"
        PublishedName = "Word 365"
        MaxTotalInstances = 100
        MaxPerUserInstances = 5
        CommandLineExecutable = "C:\Program Files\Microsoft Office\root\Office16\WINWORD.EXE"
        CommandLineArguments = ""
        WorkingDirectory = "C:\Program Files\Microsoft Office\root\Office16"
        UserFolder = "C:\Users\%username%\Documents"
        AssociatedTags = @("Office", "Productivity")
        Enabled = $true
    },
    [PSCustomObject]@{
        Uid = [guid]::NewGuid()
        Name = "Google Chrome"
        ApplicationName = "chrome.exe"
        BrowserName = "Chrome"
        PublishedName = "Chrome Browser"
        MaxTotalInstances = 200
        MaxPerUserInstances = 10
        CommandLineExecutable = "C:\Program Files\Google\Chrome\Application\chrome.exe"
        CommandLineArguments = "--no-sandbox"
        WorkingDirectory = "C:\Program Files\Google\Chrome\Application"
        UserFolder = "C:\Users\%username%\AppData\Local\Google\Chrome"
        AssociatedTags = @("Browser", "Internet")
        Enabled = $true
    },
    [PSCustomObject]@{
        Uid = [guid]::NewGuid()
        Name = "Adobe Reader"
        ApplicationName = "AcroRd32.exe"
        BrowserName = "Adobe"
        PublishedName = "PDF Reader"
        MaxTotalInstances = 50
        MaxPerUserInstances = 3
        CommandLineExecutable = "C:\Program Files (x86)\Adobe\Acrobat Reader DC\Reader\AcroRd32.exe"
        CommandLineArguments = "/n"
        WorkingDirectory = "C:\Program Files (x86)\Adobe\Acrobat Reader DC\Reader"
        UserFolder = "C:\Users\%username%\Documents"
        AssociatedTags = @("PDF", "Viewer")
        Enabled = $true
    }
)

$mockDesktopGroups = @("SalesVDIs", "FinanceVDIs", "DevVDIs")
$mockUsers = @("user1@domain.com", "user2@domain.com", "user3@domain.com", "user4@domain.com")

$result = @()

try {
    foreach ($app in $mockApplications) {
        try {
            $sessionCount = Get-Random -Minimum 1 -Maximum 10
            $sessions = @()
            1..$sessionCount | ForEach-Object {
                $sessions += [PSCustomObject]@{
                    DesktopGroupName = $mockDesktopGroups | Get-Random
                    MachineName = "VDA-" + ($mockDesktopGroups | Get-Random).Split('-')[0] + "-" + (Get-Random -Minimum 1 -Maximum 10)
                    UserName = $mockUsers | Get-Random
                }
            }
            
            # Extraer valores únicos como arrays
            $dgs = @($sessions | Select-Object -ExpandProperty DesktopGroupName | Sort-Object -Unique)
            $vdas = @($sessions | Select-Object -ExpandProperty MachineName | Sort-Object -Unique)
            $activeUsers = @($sessions | Select-Object -ExpandProperty UserName | Sort-Object -Unique)
            
            # Directorio del ejecutable
            $directory = if ($app.CommandLineExecutable) { 
                Split-Path $app.CommandLineExecutable -ErrorAction SilentlyContinue 
            } else { 
                $null 
            }

            # Construcción del objeto para JSON
            $result += [PSCustomObject]@{
                uid                   = $app.Uid.ToString()
                name                  = $app.Name
                applicationName       = $app.ApplicationName
                browserName           = $app.BrowserName
                publishedName         = $app.PublishedName
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
                executablePath        = $app.CommandLineExecutable
            }
        } catch {
            $result += [PSCustomObject]@{
                uid                   = $app.Uid.ToString()
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
                vdas                  = @()
                activeUsers           = @()
                enabled               = $false
                executablePath        = $null
            }
        }
    }
} catch {
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

# Convertir a JSON para que sea consumido desde Java
$result | ConvertTo-Json -Depth 5