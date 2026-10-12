param(
    [ValidateSet('prod','test')]
    [string]$Environment = 'prod',
    [string]$Database = 'pIDT',
    [string]$Sql = "SELECT TOP (1000) * FROM [dbo].[CommunicationUnit] ORDER BY [UCCPInputDate] DESC;"
)

$ErrorActionPreference = 'Stop'

function Write-StreamEvent {
    param([Parameter(Mandatory=$true)][object]$Event)

    try {
        $json = $Event | ConvertTo-Json -Compress -Depth 20
        [System.Console]::Out.WriteLine($json)
        [System.Console]::Out.Flush()
    }
    catch {
        $fallback = [ordered]@{
            type = 'streamError'
            success = $false
            error = $_.Exception.Message
        } | ConvertTo-Json -Compress
        [System.Console]::Out.WriteLine($fallback)
        [System.Console]::Out.Flush()
    }
}

function Connect-Mssql {
    param(
        [Parameter(Mandatory=$true)][ValidateSet('prod','test')][string]$Environment,
        [Parameter(Mandatory=$true)][string]$Database
    )

    $server = if ($Environment -eq 'prod') { 'PAG0035L' } else { 'TAG0026L.swi.srse.net' }
    Write-StreamEvent ([ordered]@{type='connecting';environment=$Environment;server=$server;database=$Database;timestamp=[datetime]::UtcNow.ToString('o')})

    $connectionString = "Server=$server;Database=$Database;Integrated Security=True;Pooling=True;Min Pool Size=0;Max Pool Size=100;Connect Timeout=30;"
    $connection = New-Object System.Data.SqlClient.SqlConnection($connectionString)

    try {
        $connection.Open()
        Write-StreamEvent ([ordered]@{type='connected';environment=$Environment;server=$connection.DataSource;database=$connection.Database;connectionState=$connection.State.ToString()})
        return $connection
    }
    catch {
        Write-StreamEvent ([ordered]@{type='connectionError';environment=$Environment;server=$server;database=$Database;success=$false;error=$_.Exception.Message})
        if ($null -ne $connection) { $connection.Dispose() }
        throw
    }
}

function Invoke-MssqlQuery {
    param(
        [Parameter(Mandatory=$true)]$Connection,
        [Parameter(Mandatory=$true)][ValidateSet('prod','test')][string]$Environment,
        [Parameter(Mandatory=$true)][string]$Database,
        [Parameter(Mandatory=$true)][string]$Sql
    )

    $command = $null
    $reader = $null
    $rowCount = 0
    $success = $false
    $errorMessage = $null
    $timer = New-Object System.Diagnostics.Stopwatch
    $timer.Start()

    try {
        if ($Connection.State.ToString() -ne 'Open') { throw 'The MSSQL connection is not open.' }
        if ([string]::IsNullOrWhiteSpace($Sql)) { throw 'The SQL query is empty.' }

        Write-StreamEvent ([ordered]@{type='queryStarted';environment=$Environment;server=$Connection.DataSource;database=$Connection.Database;sql=$Sql})
        $command = $Connection.CreateCommand()
        $command.CommandText = $Sql
        $command.CommandTimeout = 0
        $reader = $command.ExecuteReader()

        $columns = @()
        for ($i=0; $i -lt $reader.FieldCount; $i++) {
            $columns += [ordered]@{ordinal=$i;name=$reader.GetName($i);dataType=$reader.GetFieldType($i).FullName}
        }
        Write-StreamEvent ([ordered]@{type='schema';environment=$Environment;server=$Connection.DataSource;database=$Connection.Database;columns=$columns})

        while ($reader.Read()) {
            $rowCount++
            $data = [ordered]@{}
            for ($i=0; $i -lt $reader.FieldCount; $i++) {
                $name = $reader.GetName($i)
                if ($reader.IsDBNull($i)) { $data[$name] = $null }
                else {
                    $value = $reader.GetValue($i)
                    if ($value -is [datetime]) { $data[$name] = $value.ToString('yyyy-MM-dd HH:mm:ss.fff') }
                    else { $data[$name] = $value }
                }
            }
            Write-StreamEvent ([ordered]@{type='row';environment=$Environment;server=$Connection.DataSource;database=$Connection.Database;rowNumber=$rowCount;data=$data})
        }
        $success = $true
    }
    catch {
        $errorMessage = $_.Exception.Message
        Write-StreamEvent ([ordered]@{type='queryError';environment=$Environment;server=$Connection.DataSource;database=$Connection.Database;success=$false;error=$errorMessage})
    }
    finally {
        if ($null -ne $reader) { try { $reader.Close(); $reader.Dispose() } catch {} }
        if ($null -ne $command) { try { $command.Dispose() } catch {} }
        $timer.Stop()

        Write-StreamEvent ([ordered]@{type='queryCompleted';environment=$Environment;server=$Connection.DataSource;database=$Connection.Database;success=$success;rowCount=$rowCount;elapsedMilliseconds=$timer.ElapsedMilliseconds;error=$errorMessage})
    }
}

function Close-Mssql {
    param(
        $Connection,
        [Parameter(Mandatory=$true)][ValidateSet('prod','test')][string]$Environment
    )

    if ($null -eq $Connection) {
        Write-StreamEvent ([ordered]@{type='closed';environment=$Environment;success=$true;state='NotCreated';error=$null})
        return
    }

    $server = $Connection.DataSource
    Write-StreamEvent ([ordered]@{type='closing';environment=$Environment;server=$server;connectionState=$Connection.State.ToString()})
    try {
        if ($Connection.State.ToString() -ne 'Closed') { $Connection.Close() }
        $Connection.Dispose()
        Write-StreamEvent ([ordered]@{type='closed';environment=$Environment;server=$server;success=$true;state='Closed';error=$null})
    }
    catch {
        Write-StreamEvent ([ordered]@{type='closeError';environment=$Environment;server=$server;success=$false;state=$Connection.State.ToString();error=$_.Exception.Message})
    }
}

$connection = $null
try {
    $connection = Connect-Mssql -Environment $Environment -Database $Database
    Invoke-MssqlQuery -Connection $connection -Environment $Environment -Database $Database -Sql $Sql
}
catch {
    Write-StreamEvent ([ordered]@{type='fatalError';environment=$Environment;database=$Database;success=$false;error=$_.Exception.Message})
}
finally {
    Close-Mssql -Connection $connection -Environment $Environment
}