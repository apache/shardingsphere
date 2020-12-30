/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.test.integration.env.schema;

import com.google.common.base.Joiner;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.test.integration.env.EnvironmentPath;
import org.apache.shardingsphere.test.integration.env.IntegrateTestEnvironment;
import org.apache.shardingsphere.test.integration.env.datasource.builder.JdbcDataSourceBuilder;
import org.h2.tools.RunScript;

import javax.sql.DataSource;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

/**
 * Schema environment manager.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SchemaEnvironmentManager {
    
    /**
     * Get data source names.
     * 
     * @param ruleType rule type
     * @return data source names
     * @throws IOException IO exception
     * @throws JAXBException JAXB exception
     */
    public static Collection<String> getDataSourceNames(final String ruleType) throws IOException, JAXBException {
        return unmarshal(EnvironmentPath.getSchemaFile(ruleType)).getDatabases();
    }
    
    private static SchemaEnvironment unmarshal(final String schemaEnvironmentConfigFile) throws IOException, JAXBException {
        try (FileReader reader = new FileReader(schemaEnvironmentConfigFile)) {
            return (SchemaEnvironment) JAXBContext.newInstance(SchemaEnvironment.class).createUnmarshaller().unmarshal(reader);
        }
    }
    
    /**
     * Create databases.
     *
     * @throws IOException IO exception
     * @throws JAXBException JAXB exception
     */
    public static void createDatabases() throws IOException, JAXBException {
        for (String each : IntegrateTestEnvironment.getInstance().getRuleTypes()) {
            dropDatabases(each);
            createDatabases(each);
        }
    }
    
    private static void createDatabases(final String ruleType) throws IOException, JAXBException {
        SchemaEnvironment schemaEnvironment = unmarshal(EnvironmentPath.getSchemaFile(ruleType));
        for (DatabaseType each : IntegrateTestEnvironment.getInstance().getDatabaseEnvironments().keySet()) {
            DataSource dataSource = JdbcDataSourceBuilder.build(null, each);
            executeSQLScript(dataSource, generateCreateDatabaseSQLs(each, schemaEnvironment.getDatabases()));
        }
    }
    
    private static Collection<String> generateCreateDatabaseSQLs(final DatabaseType databaseType, final Collection<String> databaseNames) {
        switch (databaseType.getName()) {
            case "H2":
                return Collections.emptyList();
            case "Oracle":
                return databaseNames.stream().map(each -> String.format("CREATE SCHEMA %s", each)).collect(Collectors.toList());
            default:
                return databaseNames.stream().map(each -> String.format("CREATE DATABASE %s", each)).collect(Collectors.toList());
        }
    }
    
    /**
     * Drop databases.
     *
     * @throws IOException IO exception
     * @throws JAXBException JAXB exception
     */
    public static void dropDatabases() throws IOException, JAXBException {
        for (String each : IntegrateTestEnvironment.getInstance().getRuleTypes()) {
            dropDatabases(each);
        }
    }
    
    private static void dropDatabases(final String ruleType) throws IOException, JAXBException {
        SchemaEnvironment schemaEnvironment = unmarshal(EnvironmentPath.getSchemaFile(ruleType));
        for (DatabaseType each : IntegrateTestEnvironment.getInstance().getDatabaseEnvironments().keySet()) {
            DataSource dataSource = JdbcDataSourceBuilder.build(null, each);
            executeSQLScript(dataSource, generatePrepareDropDatabaseSQLs(each, schemaEnvironment.getDatabases()));
            executeSQLScript(dataSource, generateDropDatabaseSQLs(each, schemaEnvironment.getDatabases()));
        }
    }
    
    private static Collection<String> generatePrepareDropDatabaseSQLs(final DatabaseType databaseType, final Collection<String> databaseNames) {
        if ("PostgreSQL".equals(databaseType.getName())) {
            String sql = "SELECT pg_terminate_backend (pg_stat_activity.pid) FROM pg_stat_activity WHERE pg_stat_activity.datname = '%s'";
            return databaseNames.stream().map(each -> String.format(sql, each)).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
    
    private static Collection<String> generateDropDatabaseSQLs(final DatabaseType databaseType, final Collection<String> databaseNames) {
        switch (databaseType.getName()) {
            case "H2":
                return Collections.emptyList();
            case "Oracle":
                return databaseNames.stream().map(each -> String.format("DROP SCHEMA %s", each)).collect(Collectors.toList());
            default:
                return databaseNames.stream().map(each -> String.format("DROP DATABASE IF EXISTS %s", each)).collect(Collectors.toList());
        }
    }
    
    /**
     * Create tables.
     *
     * @throws JAXBException JAXB exception
     * @throws IOException IO exception
     */
    public static void createTables() throws JAXBException, IOException {
        for (String each : IntegrateTestEnvironment.getInstance().getRuleTypes()) {
            createTables(each);
        }
    }
    
    private static void createTables(final String ruleType) throws JAXBException, IOException {
        SchemaEnvironment schemaEnvironment = unmarshal(EnvironmentPath.getSchemaFile(ruleType));
        for (DatabaseType each : IntegrateTestEnvironment.getInstance().getDatabaseEnvironments().keySet()) {
            createTables(schemaEnvironment, each);
        }
    }
    
    private static void createTables(final SchemaEnvironment schemaEnvironment, final DatabaseType databaseType) {
        for (String each : schemaEnvironment.getDatabases()) {
            DataSource dataSource = JdbcDataSourceBuilder.build(each, databaseType);
            executeSQLScript(dataSource, schemaEnvironment.getTableCreateSQLs());
        }
    }
    
    /**
     * Drop tables.
     *
     * @throws JAXBException JAXB exception
     * @throws IOException IO exception
     */
    public static void dropTables() throws JAXBException, IOException {
        for (String each : IntegrateTestEnvironment.getInstance().getRuleTypes()) {
            dropTables(each);
        }
    }
    
    private static void dropTables(final String ruleType) throws JAXBException, IOException {
        SchemaEnvironment schemaEnvironment = unmarshal(EnvironmentPath.getSchemaFile(ruleType));
        for (DatabaseType each : IntegrateTestEnvironment.getInstance().getDatabaseEnvironments().keySet()) {
            dropTables(schemaEnvironment, each);
        }
    }
    
    private static void dropTables(final SchemaEnvironment schemaEnvironment, final DatabaseType databaseType) {
        for (String each : schemaEnvironment.getDatabases()) {
            DataSource dataSource = JdbcDataSourceBuilder.build(each, databaseType);
            executeSQLScript(dataSource, schemaEnvironment.getTableDropSQLs());
        }
    }
    
    private static void executeSQLScript(final DataSource dataSource, final Collection<String> sqls) {
        if (sqls.isEmpty()) {
            return;
        }
        try (Connection connection = dataSource.getConnection();
             StringReader sqlScript = new StringReader(Joiner.on(";\n").skipNulls().join(sqls))) {
            RunScript.execute(connection, sqlScript);
        } catch (final SQLException ignored) {
            // TODO print err message if not drop not existed database/table
        }
    }
}
