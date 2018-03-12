package io.shardingjdbc.dbtest.init;

import io.shardingjdbc.core.constant.DatabaseType;
import io.shardingjdbc.core.jdbc.core.ShardingContext;
import io.shardingjdbc.core.jdbc.core.datasource.MasterSlaveDataSource;
import io.shardingjdbc.core.jdbc.core.datasource.ShardingDataSource;
import io.shardingjdbc.dbtest.common.ConfigRuntime;
import io.shardingjdbc.dbtest.common.DatabaseTypeUtils;
import io.shardingjdbc.dbtest.exception.DbTestException;
import org.apache.commons.dbcp.BasicDataSource;
import org.h2.tools.RunScript;

import javax.sql.DataSource;
import java.io.File;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

public class InItCreateSchema {

    private static Map<DatabaseType, ShardingDataSource> shardingDataSources = new HashMap<>();

    private final Map<DatabaseType, Map<String, DataSource>> databaseTypeMap = new HashMap<>();

    /**
     * 初始化数据库表
     */
    public static synchronized void initTable() {
        for (String db : ConfigRuntime.getDbs()) {
            DatabaseType databaseType = DatabaseTypeUtils.getDatabaseType(db);
            createSchema(databaseType);
        }
    }

    protected static Map<DatabaseType, ShardingDataSource> getShardingDataSources() {
        return shardingDataSources;
    }

    /**
     * 创建数据库
     */
    public static  void createDatabase() {
        Connection conn = null;
        try {
            for (String db : ConfigRuntime.getDbs()) {
                DatabaseType databaseType = DatabaseTypeUtils.getDatabaseType(db);

                conn = initialConnection(null, databaseType);
                String packing = "default";
                if(DatabaseType.Oracle == databaseType){
                    packing = "oracle";
                }
                RunScript.execute(conn, new InputStreamReader(InItCreateSchema.class.getClassLoader().getResourceAsStream("integrate/schema/"+packing+"/manual_schema_create.sql")));

            }
        } catch (SQLException e) {
            e.printStackTrace();
        }finally {
            if(conn != null){
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    /**
     * 初始化数据库表
     */
    public static synchronized void dropDatabase() {
        Connection conn = null;
        try {
            for (String db : ConfigRuntime.getDbs()) {
                DatabaseType databaseType = DatabaseTypeUtils.getDatabaseType(db);

                conn = initialConnection(null, databaseType);
                String packing = "default";
                if(DatabaseType.Oracle == databaseType){
                    packing = "oracle";
                }
                RunScript.execute(conn, new InputStreamReader(InItCreateSchema.class.getClassLoader().getResourceAsStream("integrate/schema/"+packing+"/manual_schema_drop.sql")));

            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if(conn != null){
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    public static void main(String[] args) {
        initTable();
    }

    private static DatabaseType findDatabaseType(final String databaseType) {
        for (DatabaseType each : DatabaseType.values()) {
            if (each.name().equalsIgnoreCase(databaseType)) {
                return each;
            }
        }
        throw new DbTestException("Can't find database type of:" + databaseType);
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
                    RunScript.execute(conn, new InputStreamReader(InItCreateSchema.class.getClassLoader().getResourceAsStream("integrate/schema/table/" + database + ".sql")));
                    conn.close();
                }
            }
            conn = initialConnection("tbl", dbType);
            RunScript.execute(conn, new InputStreamReader(InItCreateSchema.class.getClassLoader().getResourceAsStream("integrate/schema/table/tbl.sql")));
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
                RunScript.execute(conn, new InputStreamReader(InItCreateSchema.class.getClassLoader().getResourceAsStream("integrate/schema/table/" + database + ".sql")));
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
                RunScript.execute(conn, new InputStreamReader(InItCreateSchema.class.getClassLoader().getResourceAsStream("integrate/schema/table/jdbc.sql")));
                conn.close();
            }
        } catch (final SQLException ex) {
            ex.printStackTrace();
        }
    }


    protected static String getDatabaseName(final String dataSetFile) {
        String fileName = new File(dataSetFile).getName();
        if (-1 == fileName.lastIndexOf(".")) {
            return fileName;
        }
        return fileName.substring(0, fileName.lastIndexOf("."));
    }


    private static BasicDataSource buildDataSource(String dbName, final DatabaseType type) {

        BasicDataSource result = new BasicDataSource();
        result.setDriverClassName(ConfigRuntime.getDriverClassName(type));
        if (dbName == null) {
            dbName = ConfigRuntime.getDefualtdb(type);
        }
        result.setUrl(ConfigRuntime.getURL(type, dbName));
        result.setUsername(ConfigRuntime.getUsername(type));
        result.setPassword(ConfigRuntime.getPassword(type));
        result.setMaxActive(1);
        if (DatabaseType.Oracle == type) {
            result.setConnectionInitSqls(Collections.singleton("ALTER SESSION SET CURRENT_SCHEMA = " + dbName));
        }
        return result;
    }

   /* public final Map<DatabaseType, Map<String, DataSource>> createDataSourceMap(List<String> initDataSetFiles) {
        for (String each : initDataSetFiles) {
            String dbName = getDatabaseName(each);

            for (String db : ConfigRuntime.getDbs()) {
                DatabaseType databaseType = DatabaseTypeUtils.getDatabaseType(db);
                createDataSources(dbName, databaseType);
            }
        }

        return databaseTypeMap;
    }*/

    private static Connection initialConnection(final String dbName, final DatabaseType type) throws SQLException {
        return buildDataSource(dbName, type).getConnection();
    }



    /*protected final void importDataSet() throws Exception {
        for (DatabaseType databaseType : getDatabaseTypes()) {
            if (databaseType == getCurrentDatabaseType() || null == getCurrentDatabaseType()) {
                DatabaseEnvironment dbEnv = new DatabaseEnvironment(databaseType);
                for (String each : getInitDataSetFiles()) {
                    InputStream is = AbstractSQLTest.class.getClassLoader().getResourceAsStream(each);
                    IDataSet dataSet = new FlatXmlDataSetBuilder().build(new InputStreamReader(is));
                    IDatabaseTester databaseTester = new ShardingJdbcDatabaseTester(dbEnv.getDriverClassName(), dbEnv.getURL(getDatabaseName(each)),
                            dbEnv.getUsername(), dbEnv.getPassword(), dbEnv.getSchema(getDatabaseName(each)));
                    databaseTester.setSetUpOperation(DatabaseOperation.CLEAN_INSERT);
                    databaseTester.setDataSet(dataSet);
                    databaseTester.onSetup();
                }
            }
        }
    }*/


}
