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
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import io.shardingjdbc.core.common.env.DatabaseEnvironment;
import org.apache.commons.dbcp2.BasicDataSource;
import org.h2.tools.RunScript;

import io.shardingjdbc.core.constant.DatabaseType;
import io.shardingjdbc.dbtest.common.DatabaseTypeUtils;

public class InItCreateSchema {
    
    /**
     * Initialize the database table.
     */
    public static synchronized void initTable(List<String> dbs) {
        for (String db : dbs) {
            DatabaseType databaseType = DatabaseTypeUtils.getDatabaseType(db);
            createSchema(databaseType);
        }
    }
    
    /**
     * Create a database.
     */
    public static void createDatabase(List<String> dbs) {
        Connection conn = null;
        try {
            for (String each : dbs) {
                DatabaseType databaseType = DatabaseTypeUtils.getDatabaseType(each);
                
                conn = initialConnection(null, databaseType);
                String packing = "default";
                if (DatabaseType.Oracle == databaseType) {
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
    public static synchronized void dropDatabase(List<String> dbs) {
        Connection conn = null;
        try {
            for (String each : dbs) {
                DatabaseType databaseType = DatabaseTypeUtils.getDatabaseType(each);
                
                conn = initialConnection(null, databaseType);
                String packing = "default";
                if (DatabaseType.Oracle == databaseType) {
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
    
    private static void createShardingSchema(final DatabaseType dbType) {
        try {
            Connection conn;
            for (int i = 0; i < 10; i++) {
                for (String database : Arrays.asList("db", "dbtbl", "nullable", "master", "slave")) {
                    conn = initialConnection(database + "_" + i, dbType);
                    RunScript.execute(conn, new InputStreamReader(InItCreateSchema.class.getClassLoader()
                            .getResourceAsStream("integrate/schema/table/" + database + ".sql")));
                    conn.close();
                }
            }
            conn = initialConnection("tbl", dbType);
            RunScript.execute(conn, new InputStreamReader(
                    InItCreateSchema.class.getClassLoader().getResourceAsStream("integrate/schema/table/tbl.sql")));
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
                        .getResourceAsStream("integrate/schema/table/" + database + ".sql")));
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
                        .getResourceAsStream("integrate/schema/table/jdbc.sql")));
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
    
}
