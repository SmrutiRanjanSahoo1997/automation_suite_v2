package com.automation.mssql.executor;

public class MssqlScriptGenerator {

    public static String buildExecuteQueryScript(String env, String sql) {
        String server = "prod".equalsIgnoreCase(env) ? "PAG0035L" : "TAG0026L.swi.srse.net";
        String escapedSql = sql.replace("'", "''");

        return String.format(
            "& {\n" +
            "  $ErrorActionPreference = 'Stop';\n" +
            "  $envName = '%s';\n" +
            "  $serverName = '%s';\n" +
            "  $sqlText = '%s';\n" +
            "  $connStr = \"Server=$serverName;Integrated Security=True;Pooling=True;Min Pool Size=0;Max Pool Size=100;Connect Timeout=30;\";\n" +
            "  [Console]::Out.WriteLine((@{type='connecting';env=$envName;server=$serverName} | ConvertTo-Json -Compress)); [Console]::Out.Flush();\n" +
            "  $conn = $null;\n" +
            "  try {\n" +
            "    $conn = New-Object System.Data.SqlClient.SqlConnection($connStr);\n" +
            "    $conn.Open();\n" +
            "    [Console]::Out.WriteLine((@{type='connected';env=$envName;server=$serverName} | ConvertTo-Json -Compress)); [Console]::Out.Flush();\n" +
            "    [Console]::Out.WriteLine((@{type='querySent';env=$envName;sql=$sqlText} | ConvertTo-Json -Compress)); [Console]::Out.Flush();\n" +
            "    $cmd = $conn.CreateCommand();\n" +
            "    $cmd.CommandText = $sqlText;\n" +
            "    $cmd.CommandTimeout = 0;\n" +
            "    $sw = [System.Diagnostics.Stopwatch]::StartNew();\n" +
            "    $reader = $cmd.ExecuteReader([System.Data.CommandBehavior]::SequentialAccess);\n" +
            "    $batch = @();\n" +
            "    $total = 0;\n" +
            "    while ($reader.Read()) {\n" +
            "      $row = [ordered]@{};\n" +
            "      for ($i = 0; $i -lt $reader.FieldCount; $i++) {\n" +
            "        $colName = $reader.GetName($i);\n" +
            "        if ($reader.IsDBNull($i)) { $row[$colName] = $null } else { $row[$colName] = $reader.GetValue($i).ToString() }\n" +
            "      }\n" +
            "      $batch += $row;\n" +
            "      $total++;\n" +
            "      if ($batch.Count -ge 100) {\n" +
            "        $out = @{type='output';env=$envName;data=$batch;rowCount=$total;end=$false;error=$null} | ConvertTo-Json -Compress -Depth 5;\n" +
            "        [Console]::Out.WriteLine($out); [Console]::Out.Flush();\n" +
            "        $batch = @();\n" +
            "      }\n" +
            "    }\n" +
            "    $sw.Stop();\n" +
            "    $finalOut = @{type='output';env=$envName;data=$batch;rowCount=$total;end=$true;durationMs=$sw.ElapsedMilliseconds;error=$null} | ConvertTo-Json -Compress -Depth 5;\n" +
            "    [Console]::Out.WriteLine($finalOut); [Console]::Out.Flush();\n" +
            "    $reader.Close();\n" +
            "  } catch {\n" +
            "    $errOut = @{type='output';env=$envName;data=@();rowCount=0;end=$true;success=$false;error=$_.Exception.Message} | ConvertTo-Json -Compress;\n" +
            "    [Console]::Out.WriteLine($errOut); [Console]::Out.Flush();\n" +
            "  } finally {\n" +
            "    if ($conn) { try { $conn.Close(); $conn.Dispose(); } catch {} };\n" +
            "    [Console]::Out.WriteLine((@{type='closed';env=$envName} | ConvertTo-Json -Compress)); [Console]::Out.Flush();\n" +
            "  }\n" +
            "}\n",
            env, server, escapedSql
        );
    }

    public static String buildGetDatabasesScript(String env) {
        String server = "prod".equalsIgnoreCase(env) ? "PAG0035L" : "TAG0026L.swi.srse.net";
        String sql = "SELECT name FROM sys.databases WHERE state_desc = 'ONLINE' AND name NOT IN ('master','tempdb','model','msdb') ORDER BY name;";

        return String.format(
            "& {\n" +
            "  $ErrorActionPreference = 'Stop';\n" +
            "  $envName = '%s';\n" +
            "  $serverName = '%s';\n" +
            "  $connStr = \"Server=$serverName;Database=master;Integrated Security=True;Pooling=True;Connect Timeout=15;\";\n" +
            "  try {\n" +
            "    $conn = New-Object System.Data.SqlClient.SqlConnection($connStr); $conn.Open();\n" +
            "    $cmd = $conn.CreateCommand(); $cmd.CommandText = \"%s\";\n" +
            "    $reader = $cmd.ExecuteReader(); $dbs = @();\n" +
            "    while ($reader.Read()) { $dbs += $reader.GetString(0) };\n" +
            "    $reader.Close(); $conn.Close(); $conn.Dispose();\n" +
            "    $res = @{type='databases';env=$envName;databases=$dbs;success=$true} | ConvertTo-Json -Compress;\n" +
            "    [Console]::Out.WriteLine($res); [Console]::Out.Flush();\n" +
            "  } catch {\n" +
            "    $res = @{type='databases';env=$envName;databases=@();success=$false;error=$_.Exception.Message} | ConvertTo-Json -Compress;\n" +
            "    [Console]::Out.WriteLine($res); [Console]::Out.Flush();\n" +
            "  }\n" +
            "}\n",
            env, server, sql
        );
    }

    public static String buildGetTablesScript(String env, String database) {
        String server = "prod".equalsIgnoreCase(env) ? "PAG0035L" : "TAG0026L.swi.srse.net";
        String safeDb = (database == null || database.isBlank()) ? "master" : database.replace("'", "''");
        String sql = "SELECT TABLE_SCHEMA, TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_TYPE = 'BASE TABLE' ORDER BY TABLE_SCHEMA, TABLE_NAME;";

        return String.format(
            "& {\n" +
            "  $ErrorActionPreference = 'Stop';\n" +
            "  $envName = '%s';\n" +
            "  $serverName = '%s';\n" +
            "  $dbName = '%s';\n" +
            "  $connStr = \"Server=$serverName;Database=$dbName;Integrated Security=True;Pooling=True;Connect Timeout=15;\";\n" +
            "  try {\n" +
            "    $conn = New-Object System.Data.SqlClient.SqlConnection($connStr); $conn.Open();\n" +
            "    $cmd = $conn.CreateCommand(); $cmd.CommandText = \"%s\";\n" +
            "    $reader = $cmd.ExecuteReader(); $tables = @();\n" +
            "    while ($reader.Read()) { $tables += @{schema=$reader.GetString(0); name=$reader.GetString(1)} };\n" +
            "    $reader.Close(); $conn.Close(); $conn.Dispose();\n" +
            "    $res = @{type='tables';env=$envName;database=$dbName;tables=$tables;success=$true} | ConvertTo-Json -Compress -Depth 5;\n" +
            "    [Console]::Out.WriteLine($res); [Console]::Out.Flush();\n" +
            "  } catch {\n" +
            "    $res = @{type='tables';env=$envName;database=$dbName;tables=@();success=$false;error=$_.Exception.Message} | ConvertTo-Json -Compress;\n" +
            "    [Console]::Out.WriteLine($res); [Console]::Out.Flush();\n" +
            "  }\n" +
            "}\n",
            env, server, safeDb, sql
        );
    }
}
