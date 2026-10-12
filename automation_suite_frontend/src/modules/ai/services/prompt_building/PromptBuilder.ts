import featureSelectionTemplate from './prompt_feature_selection.txt?raw';
import hostSelectionTemplate from './terminal/prompt_target_host_selection.txt?raw';
import commandCreationTemplate from './terminal/prompt_command_creation.txt?raw';
import runbookUpdateTemplate from './prompt_runbook_update.txt?raw';
import databaseQueryTemplate from './database/prompt_database_query_generation.txt?raw';
import databaseResultSynthesisTemplate from './database/prompt_database_result_synthesis.txt?raw';

export class PromptBuilder {
  public static readonly DEFAULT_AVAILABLE_FEATURES = [
    {
      id: 'terminal',
      name: 'Terminal',
      description: 'Executes PowerShell or CMD commands, file searches, process inspection, system services, and OS diagnostics on target servers',
    },
    {
      id: 'database',
      name: 'Database (MSSQL)',
      description: 'Queries Microsoft SQL Server databases, tables, records, schemas, executes T-SQL queries (SELECT, counts, sums, joins) across Prod (PAG0035L) and Test (TAG0026L) environments',
    },
  ];

  public static buildFeatureSelectionPrompt(
    query: string,
    accumulatedContext?: string | null,
    availableFeatures: Array<{ id: string; name: string; description: string }> = PromptBuilder.DEFAULT_AVAILABLE_FEATURES
  ): string {
    return featureSelectionTemplate
      .replace('{USER_QUERY}', query)
      .replace('{AVAILABLE_FEATURES}', JSON.stringify(availableFeatures, null, 2))
      .replace('{ACCUMULATED_CONTEXT_DATA}', accumulatedContext || 'None');
  }

  public static buildDashboardSelectionPrompt(
    query: string,
    availableDashboards: Array<{ id: string; name: string; description?: string }> = PromptBuilder.DEFAULT_AVAILABLE_FEATURES
  ): string {
    return this.buildFeatureSelectionPrompt(query, null, availableDashboards as any);
  }

  public static buildHostSelectionPrompt(
    query: string,
    servers: any[],
    accumulatedContext?: string | null
  ): string {
    const hostInventory = servers.map((s) => ({
      host: s.host,
      displayName: s.displayName,
      ip: s.ip,
      category: s.category || s.serverType,
      env: s.env || s.environment,
      os: s.os,
    }));

    return hostSelectionTemplate
      .replace('{USER_QUERY}', query)
      .replace('{HOST_INVENTORY}', JSON.stringify(hostInventory, null, 2))
      .replace('{ACCUMULATED_CONTEXT_DATA}', accumulatedContext || 'None');
  }

  public static buildDiagnosticsRemediationPrompt(
    query: string,
    host: string,
    accumulatedContext?: string | null
  ): string {
    return commandCreationTemplate
      .replace('{USER_QUERY}', query)
      .replace(/{TARGET_HOST}/g, host)
      .replace('{ACCUMULATED_CONTEXT_DATA}', accumulatedContext || 'None');
  }

  public static buildDatabaseQueryPrompt(
    query: string,
    accumulatedContext?: string | null
  ): string {
    return databaseQueryTemplate
      .replace('{USER_QUERY}', query)
      .replace('{ACCUMULATED_CONTEXT_DATA}', accumulatedContext || 'None');
  }

  public static buildDatabaseResultSynthesisPrompt(
    query: string,
    environment: string,
    sql: string,
    resultsJson: string,
    accumulatedContext?: string | null
  ): string {
    return databaseResultSynthesisTemplate
      .replace('{USER_QUERY}', query)
      .replace('{ENVIRONMENT}', environment.toUpperCase())
      .replace('{EXECUTED_SQL}', sql)
      .replace('{QUERY_RESULTS}', resultsJson)
      .replace('{ACCUMULATED_CONTEXT_DATA}', accumulatedContext || 'None');
  }

  public static buildRunbookUpdatePrompt(
    query: string,
    finalOutcome: string,
    accumulatedContext?: string | null
  ): string {
    return runbookUpdateTemplate
      .replace('{USER_QUERY}', query)
      .replace('{FINAL_OUTCOME}', finalOutcome || 'None')
      .replace('{ACCUMULATED_CONTEXT_DATA}', accumulatedContext || 'None');
  }
}
