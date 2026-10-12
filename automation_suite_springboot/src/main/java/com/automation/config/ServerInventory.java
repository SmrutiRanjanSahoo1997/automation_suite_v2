package com.automation.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Single source of truth Java constant class defining all managed target servers
 * copied directly from V1 (com.sunrise.automation_suite.config.ServerInventory).
 */
public final class ServerInventory {

    public enum ServerType {
        INSPIRE_AUTOMATION,
        INSPIRE_SCALER,
        IPS_POOL,
        INTERACTIVE
    }

    public enum Environment {
        PROD,
        T01,
        T02,
        T03,
        T04,
        DEV
    }

    public enum ServiceEnum {
        IPS,
        IPS1,
        IPS2,
        IPS_II,
        INSPIRE_PRODUCTION_SERVER,
        IA,
        INSPIRE_AUTOMATION,
        ICM,
        INSPIRE_CONTENT_MANAGER,
        INSPIRE_LICENCE_MANAGER,
        INSPIRE_SCALER,
        SCALER,
        INTERACTIVE,
        INSPIRE_INTERACTIVE
    }

    public static class ServerConfig {
        public final String host;
        public final ServerType serverType;
        public final Environment env;
        public final List<ServerType> workerTypes;
        public final List<ServiceEnum> expectedServices;
        public final String displayName;

        public ServerConfig(String host, ServerType serverType, Environment env,
                            List<ServerType> workerTypes, List<ServiceEnum> expectedServices,
                            String displayName) {
            this.host = host;
            this.serverType = serverType;
            this.env = env;
            this.workerTypes = workerTypes;
            this.expectedServices = expectedServices;
            this.displayName = displayName;
        }

        // Getters for Jackson JSON serialization
        public String getHost() { return host; }
        public ServerType getServerType() { return serverType; }
        public ServerType getCategory() { return serverType; }
        public Environment getEnv() { return env; }
        public Environment getEnvironment() { return env; }
        public List<ServerType> getWorkerTypes() { return workerTypes; }
        public List<ServiceEnum> getExpectedServices() { return expectedServices; }
        public String getDisplayName() { return displayName; }
        public int getPort() { return 9010; }
    }

    public static final List<ServerConfig> SERVERS;

    static {
        List<ServerConfig> list = new ArrayList<>();

        // -----------------------------------------------------------------
        // 1. INSPIRE AUTOMATION (IA) - 8 Servers
        // -----------------------------------------------------------------
        list.add(new ServerConfig("H1191", ServerType.INSPIRE_AUTOMATION, Environment.PROD, List.of(ServerType.INSPIRE_AUTOMATION), List.of(ServiceEnum.IA, ServiceEnum.INSPIRE_AUTOMATION), "IA Prod 01"));
        list.add(new ServerConfig("H1375", ServerType.INSPIRE_AUTOMATION, Environment.PROD, List.of(ServerType.INSPIRE_AUTOMATION), List.of(ServiceEnum.IA, ServiceEnum.INSPIRE_AUTOMATION), "IA Prod 02"));
        list.add(new ServerConfig("H1190", ServerType.INSPIRE_AUTOMATION, Environment.T01, List.of(ServerType.INSPIRE_AUTOMATION), List.of(ServiceEnum.IA), "IA T01"));
        list.add(new ServerConfig("H1376", ServerType.INSPIRE_AUTOMATION, Environment.T01, List.of(ServerType.INSPIRE_AUTOMATION), List.of(ServiceEnum.IA), "IA T01 Worker"));
        list.add(new ServerConfig("H1189", ServerType.INSPIRE_AUTOMATION, Environment.T02, List.of(ServerType.INSPIRE_AUTOMATION), List.of(ServiceEnum.IA), "IA T02"));
        list.add(new ServerConfig("H1188", ServerType.INSPIRE_AUTOMATION, Environment.T03, List.of(ServerType.INSPIRE_AUTOMATION), List.of(ServiceEnum.IA), "IA T03"));
        list.add(new ServerConfig("H1749", ServerType.INSPIRE_AUTOMATION, Environment.T04, List.of(ServerType.INSPIRE_AUTOMATION), List.of(ServiceEnum.IA), "IA T04"));
        list.add(new ServerConfig("H1187", ServerType.INSPIRE_AUTOMATION, Environment.DEV, List.of(ServerType.INSPIRE_AUTOMATION), List.of(ServiceEnum.IA), "IA DEV 01"));

        // -----------------------------------------------------------------
        // 2. IPS POOL (Dedicated for IA) - 9 Servers
        // -----------------------------------------------------------------
        list.add(new ServerConfig("H1628", ServerType.IPS_POOL, Environment.PROD, List.of(ServerType.INSPIRE_AUTOMATION), List.of(ServiceEnum.IPS), "IA IPS Pool Prod 03"));
        list.add(new ServerConfig("H1622", ServerType.INTERACTIVE, Environment.PROD, List.of(ServerType.INTERACTIVE, ServerType.INSPIRE_AUTOMATION), List.of(ServiceEnum.INTERACTIVE, ServiceEnum.IPS), "Interactive Prod && IA Prod IPS Pool"));
        list.add(new ServerConfig("H1623", ServerType.IPS_POOL, Environment.PROD, List.of(ServerType.INSPIRE_AUTOMATION), List.of(ServiceEnum.IPS), "IA IPS Pool Prod 04"));
        list.add(new ServerConfig("H2154", ServerType.IPS_POOL, Environment.PROD, List.of(ServerType.INSPIRE_AUTOMATION), List.of(ServiceEnum.IPS), "IA IPS Pool Prod 05"));
        list.add(new ServerConfig("H2155", ServerType.IPS_POOL, Environment.PROD, List.of(ServerType.INSPIRE_AUTOMATION), List.of(ServiceEnum.IPS), "IA IPS Pool Prod 06"));
        list.add(new ServerConfig("H2176", ServerType.IPS_POOL, Environment.PROD, List.of(ServerType.INSPIRE_AUTOMATION), List.of(ServiceEnum.IPS), "IA IPS Pool Prod 07"));
        list.add(new ServerConfig("H2177", ServerType.IPS_POOL, Environment.PROD, List.of(ServerType.INSPIRE_AUTOMATION), List.of(ServiceEnum.IPS), "IA IPS Pool Prod 08"));
        list.add(new ServerConfig("H2178", ServerType.IPS_POOL, Environment.PROD, List.of(ServerType.INSPIRE_AUTOMATION), List.of(ServiceEnum.IPS), "IA IPS Pool Prod 09"));
        list.add(new ServerConfig("H2179", ServerType.IPS_POOL, Environment.PROD, List.of(ServerType.INSPIRE_AUTOMATION), List.of(ServiceEnum.IPS), "IA IPS Pool Prod 10"));

        // -----------------------------------------------------------------
        // 3. INSPIRE SCALER - 12 Servers
        // -----------------------------------------------------------------
        list.add(new ServerConfig("H1351", ServerType.INSPIRE_SCALER, Environment.PROD, List.of(ServerType.INSPIRE_SCALER), List.of(ServiceEnum.INSPIRE_SCALER), "Inspire Scaler Prod 01"));
        list.add(new ServerConfig("H1352", ServerType.INSPIRE_SCALER, Environment.PROD, List.of(ServerType.INSPIRE_SCALER), List.of(ServiceEnum.INSPIRE_SCALER), "Inspire Scaler Prod 02"));
        list.add(new ServerConfig("H1758", ServerType.INSPIRE_SCALER, Environment.PROD, List.of(ServerType.INSPIRE_SCALER), List.of(ServiceEnum.INSPIRE_SCALER), "Inspire Scaler Prod 03"));
        list.add(new ServerConfig("H1748", ServerType.INSPIRE_SCALER, Environment.PROD, List.of(ServerType.INSPIRE_SCALER), List.of(ServiceEnum.INSPIRE_SCALER), "Inspire Scaler Prod 04"));
        list.add(new ServerConfig("H1750", ServerType.INSPIRE_SCALER, Environment.PROD, List.of(ServerType.INSPIRE_SCALER), List.of(ServiceEnum.INSPIRE_SCALER), "Inspire Scaler Prod 05"));
        list.add(new ServerConfig("H1350", ServerType.INSPIRE_SCALER, Environment.T01, List.of(ServerType.INSPIRE_SCALER), List.of(ServiceEnum.INSPIRE_SCALER), "Inspire Scaler T01"));
        list.add(new ServerConfig("H1630", ServerType.INSPIRE_SCALER, Environment.T01, List.of(ServerType.INSPIRE_SCALER), List.of(ServiceEnum.INSPIRE_SCALER), "Inspire Scaler T01 Worker A"));
        list.add(new ServerConfig("H1629", ServerType.INSPIRE_SCALER, Environment.T01, List.of(ServerType.INSPIRE_SCALER), List.of(ServiceEnum.INSPIRE_SCALER), "Inspire Scaler T01 Worker B"));
        list.add(new ServerConfig("H1349", ServerType.INSPIRE_SCALER, Environment.T02, List.of(ServerType.INSPIRE_SCALER), List.of(ServiceEnum.INSPIRE_SCALER), "Inspire Scaler T02"));
        list.add(new ServerConfig("H1348", ServerType.INSPIRE_SCALER, Environment.T03, List.of(ServerType.INSPIRE_SCALER), List.of(ServiceEnum.INSPIRE_SCALER), "Inspire Scaler T03"));
        list.add(new ServerConfig("H1751", ServerType.INSPIRE_SCALER, Environment.T04, List.of(ServerType.INSPIRE_SCALER), List.of(ServiceEnum.INSPIRE_SCALER), "Inspire Scaler T04"));
        list.add(new ServerConfig("H1347", ServerType.INSPIRE_SCALER, Environment.DEV, List.of(ServerType.INSPIRE_SCALER), List.of(ServiceEnum.INSPIRE_SCALER), "Inspire Scaler DEV"));

        // -----------------------------------------------------------------
        // 4. INTERACTIVE - 5 Dedicated/Worker Hosts
        // -----------------------------------------------------------------
        list.add(new ServerConfig("H1626", ServerType.INTERACTIVE, Environment.T01, List.of(ServerType.INTERACTIVE), List.of(ServiceEnum.INTERACTIVE), "Interactive T01"));
        list.add(new ServerConfig("H1625", ServerType.INTERACTIVE, Environment.T02, List.of(ServerType.INTERACTIVE), List.of(ServiceEnum.INTERACTIVE), "Interactive T02"));
        list.add(new ServerConfig("H1624", ServerType.INTERACTIVE, Environment.T03, List.of(ServerType.INTERACTIVE), List.of(ServiceEnum.INTERACTIVE), "Interactive T03"));
        list.add(new ServerConfig("H1627", ServerType.INTERACTIVE, Environment.DEV, List.of(ServerType.INTERACTIVE), List.of(ServiceEnum.INTERACTIVE), "Interactive DEV"));

        SERVERS = Collections.unmodifiableList(list);
    }

    private ServerInventory() {
    }
}
