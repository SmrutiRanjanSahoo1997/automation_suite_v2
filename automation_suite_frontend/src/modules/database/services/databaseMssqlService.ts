import { globalWebSocket } from '../../../network/globalWebSocket';
import { MssqlEnvironment } from '../types';

export const databaseMssqlService = {
  executeQuery(taskId: string, userId: string, environment: MssqlEnvironment, sql: string) {
    globalWebSocket.send({
      eventType: 'MSSQL_EXECUTE_QUERY',
      taskId,
      userId,
      environment,
      sql,
    });
  },

  getDatabases(taskId: string, userId: string, environment: MssqlEnvironment) {
    globalWebSocket.send({
      eventType: 'MSSQL_GET_DATABASES',
      taskId,
      userId,
      environment,
    });
  },

  getTables(taskId: string, userId: string, environment: MssqlEnvironment, database: string) {
    globalWebSocket.send({
      eventType: 'MSSQL_GET_TABLES',
      taskId,
      userId,
      environment,
      database,
    });
  },

  getGeneralQuery(taskId: string, userId: string, environment: MssqlEnvironment, database: string, table: string) {
    globalWebSocket.send({
      eventType: 'MSSQL_GET_GENERAL_QUERY',
      taskId,
      userId,
      environment,
      database,
      table,
    });
  },

  getStatus() {
    globalWebSocket.send({
      eventType: 'MSSQL_GET_STATUS',
    });
  },

  subscribe(callback: (frame: any) => void): () => void {
    return globalWebSocket.subscribe(callback);
  },
};
