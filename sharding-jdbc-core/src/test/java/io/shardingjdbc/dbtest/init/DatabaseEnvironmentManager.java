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
import io.shardingjdbc.dbtest.config.AnalyzeDatabase;
import io.shardingjdbc.dbtest.config.AnalyzeSql;
import io.shardingjdbc.test.sql.SQLCasesLoader;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.lang3.StringUtils;
import org.h2.tools.RunScript;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
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
    
    private static final String DATABASE_INITIALIZATION_RESOURCES_PATH = "integrate/dbtest/%s/database.xml";
    
    @Deprecated
    public static final Set<String> SHARDING_RULE_TYPE = new HashSet<>();
    
    
    /**
     * Create database.
     *
     * @param shardingRuleType sharding rule type
     * @throws JAXBException JAXB exception
     * @throws SQLException SQL exception
     * @throws IOException IO exception
     */
    public static void createDatabase(final String shardingRuleType) throws JAXBException, SQLException, IOException {
        DatabaseInitialization databaseInitialization = getDatabaseInitialization(shardingRuleType);
        for (DatabaseType each : IntegrateTestEnvironment.getInstance().getDatabaseTypes()) {
            try (
                    BasicDataSource dataSource = (BasicDataSource) new DatabaseEnvironment(each).createDataSource(null);
                    Connection conn = dataSource.getConnection();
                    StringReader stringReader = new StringReader(Joiner.on("\n").skipNulls().join(generateCreateDatabaseSQLs(each, databaseInitialization.getDatabases())))) {
                ResultSet resultSet = RunScript.execute(conn, stringReader);
                if (resultSet != null) {
                    resultSet.close();
                }
            }
        }
    }
    
    /**
     * Drop database.
     *
     * @param shardingRuleType sharding rule type
     * @throws JAXBException JAXB exception
     * @throws SQLException SQL exception
     * @throws IOException IO exception
     */
    public static void dropDatabase(final String shardingRuleType) throws JAXBException, SQLException, IOException {
        DatabaseInitialization databaseInitialization = getDatabaseInitialization(shardingRuleType);
        for (DatabaseType each : IntegrateTestEnvironment.getInstance().getDatabaseTypes()) {
            try (
                    BasicDataSource dataSource = (BasicDataSource) new DatabaseEnvironment(each).createDataSource(null);
                    Connection conn = dataSource.getConnection();
                    StringReader stringReader = new StringReader(Joiner.on("\n").skipNulls().join(generateDropDatabaseSQLs(each, databaseInitialization.getDatabases())))) {
                ResultSet resultSet = RunScript.execute(conn, stringReader);
                if (resultSet != null) {
                    resultSet.close();
                }
            }
        }
    }
    
    private static DatabaseInitialization getDatabaseInitialization(final String shardingRuleType) throws IOException, JAXBException {
        URL databaseInitializationResources = DatabaseEnvironmentManager.class.getClassLoader().getResource(String.format(DATABASE_INITIALIZATION_RESOURCES_PATH, shardingRuleType));
        assertNotNull(databaseInitializationResources);
        return unmarshal(databaseInitializationResources.getFile());
    }
    
    private static DatabaseInitialization unmarshal(final String databaseInitializationFilePath) throws IOException, JAXBException {
        try (FileReader reader = new FileReader(databaseInitializationFilePath)) {
            return (DatabaseInitialization) JAXBContext.newInstance(DatabaseInitialization.class).createUnmarshaller().unmarshal(reader);
        }
    }
    
    private static Collection<String> generateCreateDatabaseSQLs(final DatabaseType databaseType, final List<String> databases) {
        if (DatabaseType.H2 == databaseType) {
            return Collections.emptyList();
        }
        String sql = DatabaseType.Oracle == databaseType ? "CREATE SCHEMA %s;" : "CREATE DATABASE %s;";
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
        String sql = DatabaseType.Oracle == databaseType ? "DROP SCHEMA %s;" : "CREATE DROP %s;";
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
    public static void createTable(final String shardingRuleType) throws JAXBException, SQLException, IOException, XPathExpressionException, SAXException, ParserConfigurationException {
        for (DatabaseType each : IntegrateTestEnvironment.getInstance().getDatabaseTypes()) {
            List<String> databases = getDatabaseInitialization(shardingRuleType).getDatabases();
            List<String> tableSqlIds = AnalyzeSql.analyze(DatabaseEnvironmentManager.class.getClassLoader().getResource("integrate/dbtest").getPath() + "/" + shardingRuleType + "/table/create-table.xml");
            List<String> tableSqls = new LinkedList<>();
            for (String tableSqlId : tableSqlIds) {
                tableSqls.add(SQLCasesLoader.getInstance().getSchemaSQLCaseMap(tableSqlId));
            }
            for (String database : databases) {
                try (BasicDataSource dataSource = (BasicDataSource) new DatabaseEnvironment(each).createDataSource(database);
                     Connection conn = dataSource.getConnection();
                     StringReader sr = new StringReader(StringUtils.join(tableSqls, ";\n"));) {
                    ResultSet resultSet = RunScript.execute(conn, sr);
                    if (resultSet != null) {
                        resultSet.close();
                    }
                }
            }
        }
    }
    
    /**
     * create Table.
     *
     * @param dbType dbType
     * @param sqlId  sqlId
     * @param dbname dbname
     */
    public static void createTable(final DatabaseType dbType, final String sqlId, final String dbname) {
        dropOrCreateShardingSchema(dbType, sqlId, dbname);
    }
    
    /**
     * drop Table.
     *
     * @param dbType dbType
     * @param sqlId  sqlId
     * @param dbname dbname
     */
    public static void dropTable(final DatabaseType dbType, final String sqlId, final String dbname) {
        dropOrCreateShardingSchema(dbType, sqlId, dbname);
    }
    
    private static void dropOrCreateShardingSchema(final DatabaseType dbType, final String sqlId, final String dbname) {
        try {
            for (String each : SHARDING_RULE_TYPE) {
                if (dbname != null) {
                    if (!each.equals(dbname)) {
                        continue;
                    }
                }
                List<String> databases = AnalyzeDatabase.analyze(DatabaseEnvironmentManager.class.getClassLoader()
                        .getResource("integrate/dbtest").getPath() + "/" + each + "/database.xml");
                List<String> tableSqls = new ArrayList<>();
                tableSqls.add(SQLCasesLoader.getInstance().getSchemaSQLCaseMap(sqlId));
                for (String database : databases) {
                    
                    try (BasicDataSource dataSource = (BasicDataSource) new DatabaseEnvironment(dbType).createDataSource(database);
                         Connection conn = dataSource.getConnection();
                         
                         StringReader sr = new StringReader(StringUtils.join(tableSqls, ";\n"));) {
                        ResultSet resultSet = RunScript.execute(conn, sr);
                        if (resultSet != null) {
                            resultSet.close();
                        }
                    }
                }
            }
        } catch (SQLException | ParserConfigurationException | IOException | XPathExpressionException | SAXException e) {
            // The table may not exist at the time of deletion（删除时可能表不存在）
            //e.printStackTrace();
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
