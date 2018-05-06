/*
 * Copyright 1999-2015 dangdang.com.
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

package io.shardingjdbc.dbtest.env;

import com.google.common.base.Joiner;
import io.shardingjdbc.core.constant.DatabaseType;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.lang3.StringUtils;
import org.h2.tools.RunScript;

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
 * Database environment manager.
 * 
 * @author zhangliang 
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DatabaseEnvironmentManager {
    
    /**
     * Create database.
     *
     * @param shardingRuleType sharding rule type
     * @throws JAXBException JAXB exception
     * @throws IOException IO exception
     */
    public static void createDatabase(final String shardingRuleType) throws JAXBException, IOException {
        DatabaseEnvironmentSchema databaseInitialization = unmarshal(EnvironmentPath.getDatabaseEnvironmentResourceFile(shardingRuleType));
        for (DatabaseType each : IntegrateTestEnvironment.getInstance().getDatabaseTypes()) {
            try (
                    BasicDataSource dataSource = (BasicDataSource) DataSourceUtil.createDataSource(each, null);
                    Connection connection = dataSource.getConnection();
                    StringReader stringReader = new StringReader(Joiner.on(";\n").skipNulls().join(generateCreateDatabaseSQLs(each, databaseInitialization.getDatabases())))) {
                RunScript.execute(connection, stringReader);
            } catch (final SQLException ex) {
                // TODO schema maybe exist for oracle only
            }
        }
    }
    
    /**
     * Drop database.
     *
     * @param shardingRuleType sharding rule type
     * @throws JAXBException JAXB exception
     * @throws IOException IO exception
     */
    public static void dropDatabase(final String shardingRuleType) throws JAXBException, IOException {
        DatabaseEnvironmentSchema databaseInitialization = unmarshal(EnvironmentPath.getDatabaseEnvironmentResourceFile(shardingRuleType));
        for (DatabaseType each : IntegrateTestEnvironment.getInstance().getDatabaseTypes()) {
            try (
                    BasicDataSource dataSource = (BasicDataSource) DataSourceUtil.createDataSource(each, null);
                    Connection connection = dataSource.getConnection();
                    StringReader stringReader = new StringReader(Joiner.on(";\n").skipNulls().join(generateDropDatabaseSQLs(each, databaseInitialization.getDatabases())))) {
                RunScript.execute(connection, stringReader);
            } catch (final SQLException ex) {
                // TODO schema maybe not exist for oracle only
            }
        }
    }
    
    private static DatabaseEnvironmentSchema unmarshal(final String databaseInitializationFilePath) throws IOException, JAXBException {
        try (FileReader reader = new FileReader(databaseInitializationFilePath)) {
            return (DatabaseEnvironmentSchema) JAXBContext.newInstance(DatabaseEnvironmentSchema.class).createUnmarshaller().unmarshal(reader);
        }
    }
    
    private static Collection<String> generateCreateDatabaseSQLs(final DatabaseType databaseType, final List<String> databases) {
        if (DatabaseType.H2 == databaseType) {
            return Collections.emptyList();
        }
        String sql = DatabaseType.Oracle == databaseType ? "CREATE SCHEMA %s" : "CREATE DATABASE IF NOT EXISTS %s";
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
     */
    public static void createTable(final String shardingRuleType) throws JAXBException, IOException {
        for (DatabaseType each : IntegrateTestEnvironment.getInstance().getDatabaseTypes()) {
            DatabaseEnvironmentSchema databaseEnvironmentSchema = unmarshal(EnvironmentPath.getDatabaseEnvironmentResourceFile(shardingRuleType));
            List<String> databases = databaseEnvironmentSchema.getDatabases();
            for (String database : databases) {
                try (BasicDataSource dataSource = (BasicDataSource) DataSourceUtil.createDataSource(each, database);
                     Connection connection = dataSource.getConnection();
                     StringReader stringReader = new StringReader(StringUtils.join(databaseEnvironmentSchema.getTableCreateSQLs(), ";\n"))) {
                    RunScript.execute(connection, stringReader);
                } catch (final SQLException ex) {
                    // TODO schema maybe not exist for oracle only
                }
            }
        }
    }
    
    /**
     * Execute SQL.
     * 
     * @param shardingRuleType sharding rule type
     * @param databaseType database type
     * @param sql SQL to be executed
     * @throws JAXBException JAXB exception
     * @throws IOException IO exception
     */
    public static void executeSQL(final String shardingRuleType, final DatabaseType databaseType, final String sql) throws JAXBException, IOException {
        try {
            DatabaseEnvironmentSchema databaseEnvironmentSchema = unmarshal(EnvironmentPath.getDatabaseEnvironmentResourceFile(shardingRuleType));
            List<String> databases = databaseEnvironmentSchema.getDatabases();
            for (String database : databases) {
                try (BasicDataSource dataSource = (BasicDataSource) DataSourceUtil.createDataSource(databaseType, database);
                     Connection connection = dataSource.getConnection();
                     StringReader stringReader = new StringReader(sql)) {
                    RunScript.execute(connection, stringReader);
                }
            }
        } catch (final SQLException ex) {
            // TODO: Table may not exist on deletion
        }
    }
}
