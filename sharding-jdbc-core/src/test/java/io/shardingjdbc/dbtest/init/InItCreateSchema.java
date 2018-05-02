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

import java.io.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import io.shardingjdbc.core.yaml.sharding.YamlShardingConfiguration;
import io.shardingjdbc.dbtest.StartTest;
import io.shardingjdbc.dbtest.asserts.AssertEngine;
import io.shardingjdbc.dbtest.common.DatabaseEnvironment;
import io.shardingjdbc.dbtest.common.FileUtil;
import io.shardingjdbc.dbtest.common.PathUtil;
import io.shardingjdbc.dbtest.config.AnalyzeDatabase;
import io.shardingjdbc.dbtest.config.AnalyzeSql;
import io.shardingjdbc.test.sql.SQLCasesLoader;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.lang3.StringUtils;
import org.h2.tools.RunScript;

import io.shardingjdbc.core.constant.DatabaseType;
import org.xml.sax.SAXException;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import javax.sql.DataSource;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

public class InItCreateSchema {
    
    private static Set<DatabaseType> DATABASE_SCHEMAS = new HashSet<>();
    
    @Getter
    public static final Set<String> DATABASES = new HashSet<>();
    
    /**
     * add database.
     *
     * @param database database name
     */
    public static void addDatabase(String database) {
        DATABASES.add(database);
    }
    
    static {
        String assertPath = StartTest.getAssertPath();
        assertPath = PathUtil.getPath(assertPath);
        List<String> paths = FileUtil.getAllFilePaths(new File(assertPath), "t", "yaml");
        try {
            Set<DatabaseType> databaseSchemas = InItCreateSchema.getDatabaseSchema();
            InItCreateSchema.setDatabaseSchemas(databaseSchemas);
        } catch (IOException e) {
            e.printStackTrace();
        }
        
    }
    
    public static Set<DatabaseType> getDatabaseSchemas() {
        return DATABASE_SCHEMAS;
    }
    
    public static void setDatabaseSchemas(final Set<DatabaseType> databaseSchemas) {
        DATABASE_SCHEMAS = databaseSchemas;
    }
    
    /**
     * Initialize the database table.
     */
    public static synchronized void createTable() {
        for (DatabaseType db : DATABASE_SCHEMAS) {
            createSchema(db);
        }
    }
    
    /**
     * Initialize the database table.
     */
    public static synchronized void createTable(final String sqlId) {
        for (DatabaseType db : DATABASE_SCHEMAS) {
            createSchema(db, sqlId);
        }
    }
    
    /**
     * Initialize the database table.
     */
    public static synchronized void dropTableAll() {
        for (DatabaseType db : DATABASE_SCHEMAS) {
            dropShardingSchemaAll(db);
        }
    }
    
    /**
     * Initialize the database table.
     */
    public static synchronized void dropTable() {
        for (DatabaseType db : DATABASE_SCHEMAS) {
            dropSchema(db);
        }
    }
    
    /**
     * Initialize the database table.
     */
    public static synchronized void dropTable(final String sqlId) {
        for (DatabaseType db : DATABASE_SCHEMAS) {
            dropSchema(db, sqlId);
        }
    }
    
    /**
     * Create a database.
     */
    public static void createDatabase() {
        Connection conn = null;
        ResultSet resultSet = null;
        try {
            for (String database : DATABASES) {
                String sql = getCreateTableSql(DatabaseType.H2, AnalyzeDatabase.analyze(InItCreateSchema.class.getClassLoader()
                        .getResource("integrate/dbtest").getPath() + "/" + database + "/database.xml"));
                for (DatabaseType each : DATABASE_SCHEMAS) {
                    if (each.equals(DatabaseType.H2)) {
                        continue;
                    }
                    if (DatabaseType.Oracle.equals(each)) {
                        String oracleSql = getCreateTableSql(DatabaseType.Oracle, AnalyzeDatabase.analyze(InItCreateSchema.class.getClassLoader()
                                .getResource("integrate/dbtest").getPath() + "/" + database + "/database.xml"));
                        StringReader sr = new StringReader(oracleSql);
                        conn = initialConnection(null, each);
                        
                        resultSet = RunScript.execute(conn, sr);
                        resultSet.close();
                    } else {
                        StringReader sr = new StringReader(sql);
                        conn = initialConnection(null, each);
                        
                        resultSet = RunScript.execute(conn, sr);
                    }
                }
            }
        } catch (SQLException | ParserConfigurationException | IOException | XPathExpressionException | SAXException e) {
            e.printStackTrace();
        } finally {
            
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    /**
     * drop the database table.
     */
    public static synchronized void dropDatabase() {
        Connection conn = null;
        ResultSet resultSet = null;
        StringReader sr = null;
        try {
            for (String database : DATABASES) {
                String sql = getDropTableSql(DatabaseType.H2, AnalyzeDatabase.analyze(InItCreateSchema.class.getClassLoader()
                        .getResource("integrate/dbtest").getPath() + "/" + database + "/database.xml"));
                for (DatabaseType each : DATABASE_SCHEMAS) {
                    if (each.equals(DatabaseType.H2)) {
                        continue;
                    }
                    if (DatabaseType.Oracle.equals(each)) {
                        String oracleSql = getDropTableSql(DatabaseType.Oracle, AnalyzeDatabase.analyze(InItCreateSchema.class.getClassLoader()
                                .getResource("integrate/dbtest").getPath() + "/" + database + "/database.xml"));
                        sr = new StringReader(oracleSql);
                        conn = initialConnection(null, each);
                        
                        resultSet = RunScript.execute(conn, sr);
                        
                    } else {
                        sr = new StringReader(sql);
                        conn = initialConnection(null, each);
                        
                        resultSet = RunScript.execute(conn, sr);
                    }
                }
            }
        } catch (SQLException | ParserConfigurationException | IOException | XPathExpressionException | SAXException e) {
            e.printStackTrace();
        } finally {
            if (sr != null) {
                sr.close();
            }
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    private static void createSchema(final DatabaseType dbType) {
        createShardingSchema(dbType);
    }
    
    private static void createSchema(final DatabaseType dbType, final String sqlId) {
        createShardingSchema(dbType, sqlId);
    }
    
    private static void dropSchema(final DatabaseType dbType) {
        dropShardingSchema(dbType);
    }
    
    private static void dropSchema(final DatabaseType dbType, final String sqlId) {
        dropShardingSchema(dbType, sqlId);
    }
    
    private static void createShardingSchema(final DatabaseType dbType) {
        Connection conn = null;
        ResultSet resultSet = null;
        StringReader sr = null;
        try {
            for (String each : DATABASES) {
                List<String> databases = AnalyzeDatabase.analyze(InItCreateSchema.class.getClassLoader()
                        .getResource("integrate/dbtest").getPath() + "/" + each + "/database.xml");
                for (String database : databases) {
                    conn = initialConnection(database, dbType);
                    List<String> tableSqlIds = AnalyzeSql.analyze(InItCreateSchema.class.getClassLoader()
                            .getResource("integrate/dbtest").getPath() + "/" + each + "/table/create-table.xml");
                    List<String> tableSqls = new ArrayList<>();
                    for (String tableSqlId : tableSqlIds) {
                        tableSqls.add(SQLCasesLoader.getInstance().getSchemaSQLCaseMap(tableSqlId));
                    }
                    
                    sr = new StringReader(StringUtils.join(tableSqls, ";\n"));
                    
                    resultSet = RunScript.execute(conn, sr);
                }
            }
            
        } catch (SQLException | ParserConfigurationException | IOException | XPathExpressionException | SAXException e) {
            e.printStackTrace();
        } finally {
            if (sr != null) {
                sr.close();
            }
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    private static void createShardingSchema(final DatabaseType dbType, final String sqlId) {
        Connection conn = null;
        ResultSet resultSet = null;
        StringReader sr = null;
        try {
            
            for (String each : DATABASES) {
                List<String> databases = AnalyzeDatabase.analyze(InItCreateSchema.class.getClassLoader()
                        .getResource("integrate/dbtest").getPath() + "/" + each + "/database.xml");
                for (String database : databases) {
                    conn = initialConnection(database, dbType);
                    List<String> tableSqls = new ArrayList<>();
                    tableSqls.add(SQLCasesLoader.getInstance().getSchemaSQLCaseMap(sqlId));
                    
                    sr = new StringReader(StringUtils.join(tableSqls, ";\n"));
                    
                    resultSet = RunScript.execute(conn, sr);
                }
            }
            
        } catch (SQLException | ParserConfigurationException | IOException | XPathExpressionException | SAXException e) {
            e.printStackTrace();
        } finally {
            if (sr != null) {
                sr.close();
            }
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    private static void dropShardingSchemaAll(final DatabaseType dbType) {
        Connection conn = null;
        ResultSet resultSet = null;
        StringReader sr = null;
        try {
            for (String each : DATABASES) {
                List<String> databases = AnalyzeDatabase.analyze(InItCreateSchema.class.getClassLoader()
                        .getResource("integrate/dbtest").getPath() + "/" + each + "/database.xml");
                for (String database : databases) {
                    conn = initialConnection(database, dbType);
                    List<String> tableSqlIds = AnalyzeSql.analyze(InItCreateSchema.class.getClassLoader()
                            .getResource("integrate/dbtest").getPath() + "/" + each + "/table/drop-table.xml");
                    List<String> tableSqls = new ArrayList<>();
                    for (String tableSqlId : tableSqlIds) {
                        tableSqls.add(SQLCasesLoader.getInstance().getSchemaSQLCaseMap(tableSqlId));
                    }
                    sr = new StringReader(StringUtils.join(tableSqls, ";\n"));
                    try{
                        resultSet = RunScript.execute(conn, sr);
                    }catch (SQLException e) {
                        // The table may not exist at the time of deletion（删除时可能表不存在），这个错误可以忽略
                    }
                }
            }
            
        } catch (SQLException | ParserConfigurationException | IOException | XPathExpressionException | SAXException e) {
            // The table may not exist at the time of deletion（删除时可能表不存在），这个错误可以忽略
            //e.printStackTrace();
        } finally {
            if (sr != null) {
                sr.close();
            }
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    private static void dropShardingSchema(final DatabaseType dbType) {
        Connection conn = null;
        ResultSet resultSet = null;
        StringReader sr = null;
        try {
            for (String each : DATABASES) {
                List<String> databases = AnalyzeDatabase.analyze(InItCreateSchema.class.getClassLoader()
                        .getResource("integrate/dbtest").getPath() + "/" + each + "/database.xml");
                for (String database : databases) {
                    conn = initialConnection(database, dbType);
                    List<String> tableSqlIds = AnalyzeSql.analyze(InItCreateSchema.class.getClassLoader()
                            .getResource("integrate/dbtest").getPath() + "/" + each + "/table/drop-table.xml");
                    List<String> tableSqls = new ArrayList<>();
                    for (String tableSqlId : tableSqlIds) {
                        tableSqls.add(SQLCasesLoader.getInstance().getSchemaSQLCaseMap(tableSqlId));
                    }
                    sr = new StringReader(StringUtils.join(tableSqls, ";\n"));
                    
                    resultSet = RunScript.execute(conn, sr);
                }
            }
            
        } catch (SQLException | ParserConfigurationException | IOException | XPathExpressionException | SAXException e) {
            // The table may not exist at the time of deletion（删除时可能表不存在），这个错误可以忽略
            //e.printStackTrace();
        } finally {
            if (sr != null) {
                sr.close();
            }
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    private static void dropShardingSchema(final DatabaseType dbType, final String sqlId) {
        Connection conn = null;
        ResultSet resultSet = null;
        StringReader sr = null;
        try {
            for (String each : DATABASES) {
                List<String> databases = AnalyzeDatabase.analyze(InItCreateSchema.class.getClassLoader()
                        .getResource("integrate/dbtest").getPath() + "/" + each + "/database.xml");
                for (String database : databases) {
                    conn = initialConnection(database, dbType);
                    List<String> tableSqls = new ArrayList<>();
                    tableSqls.add(SQLCasesLoader.getInstance().getSchemaSQLCaseMap(sqlId));
                    
                    sr = new StringReader(StringUtils.join(tableSqls, ";\n"));
                    
                    resultSet = RunScript.execute(conn, sr);
                }
            }
            
        } catch (SQLException | ParserConfigurationException | IOException | XPathExpressionException | SAXException e) {
            // The table may not exist at the time of deletion（删除时可能表不存在），这个错误可以忽略
            //e.printStackTrace();
        } finally {
            if (sr != null) {
                sr.close();
            }
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    public static BasicDataSource buildDataSource(final String dbName, final DatabaseType type) {
        
        DatabaseEnvironment dbEnv = new DatabaseEnvironment(type);
        BasicDataSource result = new BasicDataSource();
        result.setDriverClassName(dbEnv.getDriverClassName());
        
        result.setUrl(dbEnv.getURL(dbName));
        result.setUsername(dbEnv.getUsername());
        result.setPassword(dbEnv.getPassword());
        
        //result.setMaxActive(1);
        if (DatabaseType.Oracle == type) {
            result.setConnectionInitSqls(Collections.singleton("ALTER SESSION SET CURRENT_SCHEMA = " + dbName));
        }
        return result;
    }
    
    private static Connection initialConnection(final String dbName, final DatabaseType type) throws SQLException {
        return buildDataSource(dbName, type).getConnection();
    }
    
    /**
     *
     * @return
     */
    public static Set<DatabaseType> getDatabaseSchema() throws IOException {
        Set<DatabaseType> dbset = new HashSet<>();
        for (String each : AssertEngine.getDatabases()) {
            DatabaseType databaseType = getDatabaseType(each);
            dbset.add(databaseType);
        }
        return dbset;
    }
    
    private static YamlShardingConfiguration unmarshal(final File yamlFile) throws IOException {
        try (
                FileInputStream fileInputStream = new FileInputStream(yamlFile);
                InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, "UTF-8")
        ) {
            return new Yaml(new Constructor(YamlShardingConfiguration.class)).loadAs(inputStreamReader, YamlShardingConfiguration.class);
        }
    }
    
    private static String getCreateTableSql(final DatabaseType databaseType, final List<String> databases) {
        String basesql = "CREATE DATABASE ";
        if (DatabaseType.Oracle == databaseType) {
            basesql = "CREATE SCHEMA ";
        }
        List<String> sqls = new ArrayList<>();
        for (String database : databases) {
            sqls.add(basesql + database + ";");
        }
        return StringUtils.join(sqls, "\n");
    }
    
    private static String getDropTableSql(final DatabaseType databaseType, final List<String> databases) {
        String basesql = "DROP DATABASE ";
        if (DatabaseType.Oracle == databaseType) {
            basesql = "DROP SCHEMA ";
        }
        List<String> sqls = new ArrayList<>();
        for (String database : databases) {
            sqls.add(basesql + database + ";");
        }
        return StringUtils.join(sqls, "\n");
    }
    
    /**
     * Get the database type enumeration.
     *
     * @param type String database type
     * @return database enumeration
     */
    public static DatabaseType getDatabaseType(final String type) {
        
        DatabaseType[] databaseTypes = DatabaseType.values();
        for (DatabaseType each : databaseTypes) {
            if (type.equalsIgnoreCase(each.name())) {
                return each;
            }
        }
        return DatabaseType.H2;
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
        List<DatabaseType> databaseTypes = new ArrayList<>();
        for (String eachType : types) {
            DatabaseType[] databaseTypeSrcs = DatabaseType.values();
            for (DatabaseType each : databaseTypeSrcs) {
                if (eachType.equalsIgnoreCase(each.name())) {
                    databaseTypes.add(each);
                }
            }
        }
        return databaseTypes;
    }
}
