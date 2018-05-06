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

package io.shardingjdbc.dbtest.init;

import com.google.common.base.Joiner;
import io.shardingjdbc.core.constant.DatabaseType;
import io.shardingjdbc.dbtest.IntegrateTestEnvironment;
import io.shardingjdbc.dbtest.common.DatabaseEnvironment;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.lang3.StringUtils;
import org.h2.tools.RunScript;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertNotNull;

public final class DatabaseEnvironmentManager {
    
    private static final String DATABASE_INITIALIZATION_RESOURCES_PATH = "integrate/dbtest/%s/schema.xml";
    
    @Deprecated
    public static final Set<String> SHARDING_RULE_TYPE = new HashSet<>();
    
    
    /**
     * Create database.
     *
     * @param shardingRuleType sharding rule type
     * @throws JAXBException JAXB exception
     * @throws IOException IO exception
     */
    public static void createDatabase(final String shardingRuleType) throws JAXBException, IOException {
        DatabaseEnvironmentSchema databaseInitialization = getDatabaseInitialization(shardingRuleType);
        for (DatabaseType each : IntegrateTestEnvironment.getInstance().getDatabaseTypes()) {
            try (
                    BasicDataSource dataSource = (BasicDataSource) new DatabaseEnvironment(each).createDataSource(null);
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
        DatabaseEnvironmentSchema databaseInitialization = getDatabaseInitialization(shardingRuleType);
        for (DatabaseType each : IntegrateTestEnvironment.getInstance().getDatabaseTypes()) {
            try (
                    BasicDataSource dataSource = (BasicDataSource) new DatabaseEnvironment(each).createDataSource(null);
                    Connection connection = dataSource.getConnection();
                    StringReader stringReader = new StringReader(Joiner.on(";\n").skipNulls().join(generateDropDatabaseSQLs(each, databaseInitialization.getDatabases())))) {
                RunScript.execute(connection, stringReader);
            } catch (final SQLException ex) {
                // TODO schema maybe not exist for oracle only
            }
        }
    }
    
    private static DatabaseEnvironmentSchema getDatabaseInitialization(final String shardingRuleType) throws IOException, JAXBException {
        URL databaseInitializationResources = DatabaseEnvironmentManager.class.getClassLoader().getResource(String.format(DATABASE_INITIALIZATION_RESOURCES_PATH, shardingRuleType));
        assertNotNull(databaseInitializationResources);
        return unmarshal(databaseInitializationResources.getFile());
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
     * @throws SQLException SQL exception
     * @throws IOException IO exception
     */
    public static void createTable(final String shardingRuleType) throws JAXBException, SQLException, IOException {
        for (DatabaseType each : IntegrateTestEnvironment.getInstance().getDatabaseTypes()) {
            DatabaseEnvironmentSchema databaseEnvironmentSchema = getDatabaseInitialization(shardingRuleType);
            List<String> databases = databaseEnvironmentSchema.getDatabases();
            for (String database : databases) {
                try (BasicDataSource dataSource = (BasicDataSource) new DatabaseEnvironment(each).createDataSource(database);
                     Connection connection = dataSource.getConnection();
                     StringReader stringReader = new StringReader(StringUtils.join(databaseEnvironmentSchema.getTableCreateSQLs(), ";\n"))) {
                    RunScript.execute(connection, stringReader);
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
            DatabaseEnvironmentSchema databaseEnvironmentSchema = getDatabaseInitialization(shardingRuleType);
            List<String> databases = databaseEnvironmentSchema.getDatabases();
            for (String database : databases) {
                try (BasicDataSource dataSource = (BasicDataSource) new DatabaseEnvironment(databaseType).createDataSource(database);
                     Connection connection = dataSource.getConnection();
                     StringReader stringReader = new StringReader(sql)) {
                    RunScript.execute(connection, stringReader);
                }
            }
        } catch (final SQLException ex) {
            // The table may not exist at the time of deletion（删除时可能表不存在）
        }
    }
    
    /**
     * Get the database type enumeration.
     *
     * @param typeStrs String database type
     * @return database enumeration
     */
    public static List<DatabaseType> getDatabaseTypes(final String typeStrs) {
        if (StringUtils.isBlank(typeStrs)) {
            return Arrays.asList(DatabaseType.values());
        }
        String[] types = StringUtils.split(typeStrs, ",");
        List<DatabaseType> result = new ArrayList<>();
        for (String eachType : types) {
            DatabaseType[] databaseTypeSrcs = DatabaseType.values();
            for (DatabaseType each : databaseTypeSrcs) {
                if (eachType.equalsIgnoreCase(each.name())) {
                    result.add(each);
                }
            }
        }
        return result;
    }
}
