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
import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.test.integration.env.EnvironmentPath;
import org.apache.shardingsphere.test.integration.env.IntegrateTestEnvironment;
import org.apache.shardingsphere.test.integration.env.datasource.builder.ActualDataSourceBuilder;
import org.h2.tools.RunScript;

import javax.sql.DataSource;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;

/**
 * Schema environment manager.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SchemaEnvironmentManager {
    
    /**
     * Get data source names.
     * 
     * @param scenario scenario
     * @return data source names
     * @throws IOException IO exception
     * @throws JAXBException JAXB exception
     */
    public static Collection<String> getDataSourceNames(final String scenario) throws IOException, JAXBException {
        return unmarshal(EnvironmentPath.getSchemaFile(scenario)).getDatabases();
    }
    
    private static SchemaEnvironment unmarshal(final String schemaEnvironmentConfigFile) throws IOException, JAXBException {
        try (FileReader reader = new FileReader(schemaEnvironmentConfigFile)) {
            return (SchemaEnvironment) JAXBContext.newInstance(SchemaEnvironment.class).createUnmarshaller().unmarshal(reader);
        }
    }
    
    /**
     * Create databases.
     */
    public static void createDatabases() {
        if (IntegrateTestEnvironment.getInstance().isEnvironmentPrepared()) {
            return;
        }
        for (String each : IntegrateTestEnvironment.getInstance().getScenarios()) {
            createDatabases(each);
        }
    }
    
    private static void createDatabases(final String scenario) {
        for (DatabaseType each : IntegrateTestEnvironment.getInstance().getDatabaseEnvironments().keySet()) {
            // TODO use multiple threads to improve performance
            File file = new File(EnvironmentPath.getInitSQLFile(each, scenario));
            DataSource dataSource = ActualDataSourceBuilder.build(null, scenario, each);
            executeSQLScript(dataSource, file);
        }
    }
    
    /**
     * Create tables.
     *
     * @throws JAXBException JAXB exception
     * @throws IOException IO exception
     */
    public static void createTables() throws JAXBException, IOException {
        if (IntegrateTestEnvironment.getInstance().isEnvironmentPrepared()) {
            return;
        }
        for (String each : IntegrateTestEnvironment.getInstance().getScenarios()) {
            createTables(each);
        }
    }
    
    private static void createTables(final String scenario) throws JAXBException, IOException {
        SchemaEnvironment schemaEnvironment = unmarshal(EnvironmentPath.getSchemaFile(scenario));
        for (DatabaseType each : IntegrateTestEnvironment.getInstance().getDatabaseEnvironments().keySet()) {
            createTables(schemaEnvironment, scenario, each);
        }
    }
    
    private static void createTables(final SchemaEnvironment schemaEnvironment, final String scenario, final DatabaseType databaseType) {
        for (String each : schemaEnvironment.getDatabases()) {
            // TODO use multiple threads to improve performance
            DataSource dataSource = ActualDataSourceBuilder.build(each, scenario, databaseType);
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
        if (IntegrateTestEnvironment.getInstance().isEnvironmentPrepared()) {
            return;
        }
        for (String each : IntegrateTestEnvironment.getInstance().getScenarios()) {
            dropTables(each);
        }
    }
    
    private static void dropTables(final String scenario) throws JAXBException, IOException {
        SchemaEnvironment schemaEnvironment = unmarshal(EnvironmentPath.getSchemaFile(scenario));
        for (DatabaseType each : IntegrateTestEnvironment.getInstance().getDatabaseEnvironments().keySet()) {
            dropTables(schemaEnvironment, scenario, each);
        }
    }
    
    private static void dropTables(final SchemaEnvironment schemaEnvironment, final String scenario, final DatabaseType databaseType) {
        for (String each : schemaEnvironment.getDatabases()) {
            // TODO use multiple threads to improve performance
            DataSource dataSource = ActualDataSourceBuilder.build(each, scenario, databaseType);
            executeSQLScript(dataSource, schemaEnvironment.getTableDropSQLs());
        }
    }
    
    @SneakyThrows(IOException.class)
    private static void executeSQLScript(final DataSource dataSource, final File file) {
        try (Connection connection = dataSource.getConnection();
             FileReader reader = new FileReader(file)) {
            RunScript.execute(connection, reader);
        } catch (final SQLException ignored) {
            // TODO print err message if not drop not existed database/table
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
