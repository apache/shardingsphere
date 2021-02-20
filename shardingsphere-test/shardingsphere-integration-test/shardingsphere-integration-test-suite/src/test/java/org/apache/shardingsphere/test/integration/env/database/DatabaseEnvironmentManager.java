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

package org.apache.shardingsphere.test.integration.env.database;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.spi.typed.TypedSPIRegistry;
import org.apache.shardingsphere.test.integration.env.EnvironmentPath;
import org.apache.shardingsphere.test.integration.env.IntegrationTestEnvironment;
import org.apache.shardingsphere.test.integration.env.database.initialization.DatabaseSQLInitialization;
import org.h2.tools.RunScript;

import javax.sql.DataSource;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Properties;

/**
 * Database environment manager.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DatabaseEnvironmentManager {
    
    static {
        ShardingSphereServiceLoader.register(DatabaseSQLInitialization.class);
    }
    
    /**
     * Get database names.
     * 
     * @param scenario scenario
     * @return database names
     * @throws IOException IO exception
     * @throws JAXBException JAXB exception
     */
    public static Collection<String> getDatabaseNames(final String scenario) throws IOException, JAXBException {
        return unmarshal(EnvironmentPath.getDatabasesFile(scenario)).getDatabases();
    }
    
    private static DatabaseNameEnvironment unmarshal(final String databasesFile) throws IOException, JAXBException {
        try (FileReader reader = new FileReader(databasesFile)) {
            return (DatabaseNameEnvironment) JAXBContext.newInstance(DatabaseNameEnvironment.class).createUnmarshaller().unmarshal(reader);
        }
    }
    
    /**
     * Execute init SQLs.
     * 
     * @throws IOException IO exception
     * @throws JAXBException JAXB exception
     * @throws SQLException SQL exception
     */
    public static void executeInitSQLs() throws IOException, JAXBException, SQLException {
        for (String each : IntegrationTestEnvironment.getInstance().getScenarios()) {
            executeInitSQLs(each);
        }
    }
    
    private static void executeInitSQLs(final String scenario) throws IOException, JAXBException, SQLException {
        for (DatabaseType each : IntegrationTestEnvironment.getInstance().getDataSourceEnvironments().keySet()) {
            DatabaseSQLInitialization databaseSQLInitialization = TypedSPIRegistry.getRegisteredService(DatabaseSQLInitialization.class, each.getName(), new Properties());
            databaseSQLInitialization.executeInitSQLs(scenario, each);
        }
    }
    
    /**
     * Execute SQL script.
     *
     * @param dataSource data source
     * @param file script file
     *
     * @throws SQLException SQL exception
     * @throws IOException IO exception
     */
    public static void executeSQLScript(final DataSource dataSource, final File file) throws SQLException, IOException {
        try (Connection connection = dataSource.getConnection();
             FileReader reader = new FileReader(file)) {
            // TODO If you don't use H2 in the future, you need to implement this method.
            RunScript.execute(connection, reader);
        }
    }
}
