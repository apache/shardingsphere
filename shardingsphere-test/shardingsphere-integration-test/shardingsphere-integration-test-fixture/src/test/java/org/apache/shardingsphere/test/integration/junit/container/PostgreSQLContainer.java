package org.apache.shardingsphere.test.integration.junit.container;

import org.apache.shardingsphere.infra.database.type.dialect.PostgreSQLDatabaseType;

public class PostgreSQLContainer extends StorageContainer {
    
    public PostgreSQLContainer() {
        super("postgres:12.6", new PostgreSQLDatabaseType());
    }
    
    @Override
    protected void configure() {
        addEnv("POSTGRES_USER", "postgres");
        addEnv("POSTGRES_PASSWORD", "postgres");
        withInitSQLMapping("/env/" + getDescription().getScenario() + "/init-sql/postgresql");
        super.configure();
    }
    
    @Override
    protected String getUrl(final String dataSourceName) {
        return String.format("jdbc:postgresql://%s:%s/", getHost(), getPort());
    }
    
    @Override
    protected int getPort() {
        return getMappedPort(5432);
    }
    
    @Override
    protected String getUsername() {
        return "postgres";
    }
    
    @Override
    protected String getPassword() {
        return "postgres";
    }
    
}
