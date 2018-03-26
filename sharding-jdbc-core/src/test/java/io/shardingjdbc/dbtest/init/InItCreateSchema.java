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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

import io.shardingjdbc.core.yaml.sharding.YamlShardingConfiguration;
import io.shardingjdbc.dbtest.common.DatabaseEnvironment;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.dbcp2.BasicDataSource;
import org.h2.tools.RunScript;

import io.shardingjdbc.core.constant.DatabaseType;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import javax.sql.DataSource;

public class InItCreateSchema {
    
    private static Set<DatabaseType> DATABASE_SCHEMAS;
    
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
    public static synchronized void dropTable() {
        for (DatabaseType db : DATABASE_SCHEMAS) {
            dropSchema(db);
        }
    }
    
    /**
     * Create a database.
     */
    public static void createDatabase() {
        Connection conn = null;
        try {
            for (DatabaseType each : DATABASE_SCHEMAS) {
                
                conn = initialConnection(null, each);
                String packing = "default";
                if (DatabaseType.Oracle == each) {
                    packing = "oracle";
                }
                RunScript.execute(conn, new InputStreamReader(InItCreateSchema.class.getClassLoader()
                        .getResourceAsStream("integrate/schema/" + packing + "/manual_schema_create.sql")));
                
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
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
        try {
            for (DatabaseType each : DATABASE_SCHEMAS) {
                
                conn = initialConnection(null, each);
                String packing = "default";
                if (DatabaseType.Oracle == each) {
                    packing = "oracle";
                }
                RunScript.execute(conn, new InputStreamReader(InItCreateSchema.class.getClassLoader()
                        .getResourceAsStream("integrate/schema/" + packing + "/manual_schema_drop.sql")));
                
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
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
        createJdbcSchema(dbType);
        createMasterSlaveOnlySchema(dbType);
        createShardingSchema(dbType);
    }
    
    private static void dropSchema(final DatabaseType dbType) {
        dropJdbcSchema(dbType);
        dropMasterSlaveOnlySchema(dbType);
        dropShardingSchema(dbType);
    }
    
    private static void createShardingSchema(final DatabaseType dbType) {
        try {
            Connection conn;
            for (int i = 0; i < 10; i++) {
                for (String database : Arrays.asList("db", "dbtbl", "nullable", "master", "slave")) {
                    conn = initialConnection(database + "_" + i, dbType);
                    RunScript.execute(conn, new InputStreamReader(InItCreateSchema.class.getClassLoader()
                            .getResourceAsStream("integrate/schema/table/create/" + database + ".sql")));
                    conn.close();
                }
            }
            conn = initialConnection("tbl", dbType);
            RunScript.execute(conn, new InputStreamReader(
                    InItCreateSchema.class.getClassLoader().getResourceAsStream("integrate/schema/table/create/tbl.sql")));
            conn.close();
        } catch (final SQLException ex) {
            ex.printStackTrace();
        }
    }
    
    private static void createMasterSlaveOnlySchema(final DatabaseType dbType) {
        try {
            Connection conn;
            for (String database : Arrays.asList("master_only", "slave_only")) {
                conn = initialConnection(database, dbType);
                RunScript.execute(conn, new InputStreamReader(InItCreateSchema.class.getClassLoader()
                        .getResourceAsStream("integrate/schema/table/create/" + database + ".sql")));
                conn.close();
            }
        } catch (final SQLException ex) {
            ex.printStackTrace();
        }
    }
    
    private static void createJdbcSchema(final DatabaseType dbType) {
        try {
            Connection conn;
            for (int i = 0; i < 2; i++) {
                conn = initialConnection("jdbc_" + i, dbType);
                RunScript.execute(conn, new InputStreamReader(InItCreateSchema.class.getClassLoader()
                        .getResourceAsStream("integrate/schema/table/create/jdbc.sql")));
                conn.close();
            }
        } catch (final SQLException ex) {
            ex.printStackTrace();
        }
    }
    
    private static void dropShardingSchema(final DatabaseType dbType) {
        try {
            Connection conn;
            for (int i = 0; i < 10; i++) {
                for (String database : Arrays.asList("db", "dbtbl", "nullable", "master", "slave")) {
                    conn = initialConnection(database + "_" + i, dbType);
                    RunScript.execute(conn, new InputStreamReader(InItCreateSchema.class.getClassLoader()
                            .getResourceAsStream("integrate/schema/table/drop/" + database + ".sql")));
                    conn.close();
                }
            }
            conn = initialConnection("tbl", dbType);
            RunScript.execute(conn, new InputStreamReader(
                    InItCreateSchema.class.getClassLoader().getResourceAsStream("integrate/schema/table/create/tbl.sql")));
            conn.close();
        } catch (final SQLException ex) {
            ex.printStackTrace();
        }
    }
    
    private static void dropMasterSlaveOnlySchema(final DatabaseType dbType) {
        try {
            Connection conn;
            for (String database : Arrays.asList("master_only", "slave_only")) {
                conn = initialConnection(database, dbType);
                RunScript.execute(conn, new InputStreamReader(InItCreateSchema.class.getClassLoader()
                        .getResourceAsStream("integrate/schema/table/drop/" + database + ".sql")));
                conn.close();
            }
        } catch (final SQLException ex) {
            ex.printStackTrace();
        }
    }
    
    private static void dropJdbcSchema(final DatabaseType dbType) {
        try {
            Connection conn;
            for (int i = 0; i < 2; i++) {
                conn = initialConnection("jdbc_" + i, dbType);
                RunScript.execute(conn, new InputStreamReader(InItCreateSchema.class.getClassLoader()
                        .getResourceAsStream("integrate/schema/table/drop/jdbc.sql")));
                conn.close();
            }
        } catch (final SQLException ex) {
            ex.printStackTrace();
        }
    }
    
    private static BasicDataSource buildDataSource(final String dbName, final DatabaseType type) {
        
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
     * @param paths paths
     * @return
     */
    public static Set<DatabaseType> getDatabaseSchema(final List<String> paths) throws IOException {
        Set<DatabaseType> dbset = new HashSet<>();
        DatabaseEnvironment databaseEnvironment = new DatabaseEnvironment(DatabaseType.H2);
        for (String each : paths) {
            YamlShardingConfiguration shardingConfiguration =  unmarshal(new File(each));
            Map<String, DataSource> dataSourceMap = shardingConfiguration.getDataSources();
            for (Map.Entry<String, DataSource> eachDataSourceEntry : dataSourceMap.entrySet()) {
                BasicDataSource dataSource = (BasicDataSource)eachDataSourceEntry.getValue();
                DatabaseType databaseType = databaseEnvironment.getDatabaseTypeByJdbcDriver( dataSource.getDriverClassName());
                dbset.add(databaseType);
            }
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
    
}
