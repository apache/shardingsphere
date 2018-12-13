/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.dbtest.env.schema;

import com.google.common.base.Joiner;
import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.dbtest.env.EnvironmentPath;
import io.shardingsphere.dbtest.env.IntegrateTestEnvironment;
import io.shardingsphere.dbtest.env.datasource.DataSourceUtil;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
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
import java.util.LinkedList;
import java.util.List;

/**
 * Schema environment manager.
 * 
 * @author zhangliang 
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SchemaEnvironmentManager {
    
    /**
     * Get data source names.
     * 
     * @param shardingRuleType sharding rule type
     * @return data source names
     * @throws IOException IO exception
     * @throws JAXBException JAXB exception
     */
    public static Collection<String> getDataSourceNames(final String shardingRuleType) throws IOException, JAXBException {
        return unmarshal(EnvironmentPath.getDatabaseEnvironmentResourceFile(shardingRuleType)).getDatabases();
    } 
    
    /**
     * Create database.
     *
     * @param shardingRuleType sharding rule type
     * @throws IOException IO exception
     * @throws JAXBException JAXB exception
     * @throws SQLException SQL exception
     */
    public static void createDatabase(final String shardingRuleType) throws IOException, JAXBException, SQLException {
        SchemaEnvironment databaseInitialization = unmarshal(EnvironmentPath.getDatabaseEnvironmentResourceFile(shardingRuleType));
        for (DatabaseType each : IntegrateTestEnvironment.getInstance().getDatabaseTypes()) {
            DataSource dataSource = DataSourceUtil.createDataSource(each, null);
            try (
                    Connection connection = dataSource.getConnection();
                    StringReader stringReader = new StringReader(Joiner.on(";\n").skipNulls().join(generateCreateDatabaseSQLs(each, databaseInitialization.getDatabases())))) {
                RunScript.execute(connection, stringReader);
            }
        }
    }
    
    /**
     * Drop database.
     *
     * @param shardingRuleType sharding rule type
     * @throws IOException IO exception
     * @throws JAXBException JAXB exception
     */
    public static void dropDatabase(final String shardingRuleType) throws IOException, JAXBException {
        SchemaEnvironment databaseInitialization = unmarshal(EnvironmentPath.getDatabaseEnvironmentResourceFile(shardingRuleType));
        for (DatabaseType each : IntegrateTestEnvironment.getInstance().getDatabaseTypes()) {
            DataSource dataSource = DataSourceUtil.createDataSource(each, null);
            try (
                    Connection connection = dataSource.getConnection();
                    StringReader stringReader = new StringReader(Joiner.on(";\n").skipNulls().join(generateDropDatabaseSQLs(each, databaseInitialization.getDatabases())))) {
                RunScript.execute(connection, stringReader);
            } catch (final SQLException ex) {
                // TODO database maybe not exist
            }
        }
    }
    
    private static SchemaEnvironment unmarshal(final String databaseInitializationFilePath) throws IOException, JAXBException {
        try (FileReader reader = new FileReader(databaseInitializationFilePath)) {
            return (SchemaEnvironment) JAXBContext.newInstance(SchemaEnvironment.class).createUnmarshaller().unmarshal(reader);
        }
    }
    
    private static Collection<String> generateCreateDatabaseSQLs(final DatabaseType databaseType, final List<String> databases) {
        if (DatabaseType.H2 == databaseType) {
            return Collections.emptyList();
        }
        String sql = DatabaseType.Oracle == databaseType ? "CREATE SCHEMA %s" : "CREATE DATABASE %s";
        Collection<String> result = new LinkedList<>();
        for (String each : databases) {
            result.add(String.format(sql, each));
        }
        return result;
    }
    
    private static Collection<String> generateDropDatabaseSQLs(final DatabaseType databaseType, final List<String> databases) {
        if (DatabaseType.H2 == databaseType) {
            return Collections.emptyList();
        }
        String sql = DatabaseType.Oracle == databaseType ? "DROP SCHEMA %s" : "DROP DATABASE IF EXISTS %s";
        Collection<String> result = new LinkedList<>();
        for (String each : databases) {
            result.add(String.format(sql, each));
        }
        return result;
    }
    
    /**
     * Create table.
     *
     * @param shardingRuleType sharding rule type
     * @throws JAXBException JAXB exception
     * @throws IOException IO exception
     * @throws SQLException SQL exception
     */
    public static void createTable(final String shardingRuleType) throws JAXBException, IOException, SQLException {
        for (DatabaseType each : IntegrateTestEnvironment.getInstance().getDatabaseTypes()) {
            SchemaEnvironment databaseEnvironmentSchema = unmarshal(EnvironmentPath.getDatabaseEnvironmentResourceFile(shardingRuleType));
            createTable(databaseEnvironmentSchema, each);
        }
    }
    
    private static void createTable(final SchemaEnvironment databaseEnvironmentSchema, final DatabaseType databaseType) throws SQLException {
        for (String each : databaseEnvironmentSchema.getDatabases()) {
            DataSource dataSource = DataSourceUtil.createDataSource(databaseType, each);
            try (Connection connection = dataSource.getConnection();
                 StringReader stringReader = new StringReader(Joiner.on(";\n").join(databaseEnvironmentSchema.getTableCreateSQLs()))) {
                RunScript.execute(connection, stringReader);
            }
        }
    }
    
    /**
     * Drop table.
     *
     * @param shardingRuleType sharding rule type
     * @throws JAXBException JAXB exception
     * @throws IOException IO exception
     */
    public static void dropTable(final String shardingRuleType) throws JAXBException, IOException {
        for (DatabaseType each : IntegrateTestEnvironment.getInstance().getDatabaseTypes()) {
            SchemaEnvironment databaseEnvironmentSchema = unmarshal(EnvironmentPath.getDatabaseEnvironmentResourceFile(shardingRuleType));
            dropTable(databaseEnvironmentSchema, each);
        }
    }
    
    private static void dropTable(final SchemaEnvironment databaseEnvironmentSchema, final DatabaseType databaseType) {
        for (String each : databaseEnvironmentSchema.getDatabases()) {
            DataSource dataSource = DataSourceUtil.createDataSource(databaseType, each);
            try (Connection connection = dataSource.getConnection();
                 StringReader stringReader = new StringReader(Joiner.on(";\n").join(databaseEnvironmentSchema.getTableDropSQLs()))) {
                RunScript.execute(connection, stringReader);
            } catch (final SQLException ex) {
                // TODO table maybe not exist
            }
        }
    }
}
