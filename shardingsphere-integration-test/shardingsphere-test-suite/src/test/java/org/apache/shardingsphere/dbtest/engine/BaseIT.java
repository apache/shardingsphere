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

package org.apache.shardingsphere.dbtest.engine;

import com.google.common.base.Joiner;
import lombok.AccessLevel;
import lombok.Getter;
import org.apache.shardingsphere.dbtest.env.EnvironmentPath;
import org.apache.shardingsphere.dbtest.env.IntegrateTestEnvironment;
import org.apache.shardingsphere.dbtest.env.datasource.DataSourceUtil;
import org.apache.shardingsphere.dbtest.env.datasource.ProxyDataSourceUtil;
import org.apache.shardingsphere.dbtest.env.schema.SchemaEnvironmentManager;
import org.apache.shardingsphere.driver.api.yaml.YamlShardingSphereDataSourceFactory;
import org.apache.shardingsphere.driver.jdbc.core.datasource.ShardingSphereDataSource;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.junit.After;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import javax.sql.DataSource;
import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

@RunWith(Parameterized.class)
@Getter(AccessLevel.PROTECTED)
public abstract class BaseIT {
    
    public static final String NOT_VERIFY_FLAG = "NOT_VERIFY";
    
    private final String ruleType;
    
    private final DatabaseType databaseType;
    
    private final Map<String, DataSource> dataSourceMap;
    
    private final DataSource dataSource;
    
    static {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        if (IntegrateTestEnvironment.getInstance().isProxyEnvironment()) {
            waitForProxy();
        }
    }
    
    BaseIT(final String ruleType, final DatabaseType databaseType) throws IOException, JAXBException, SQLException {
        this.ruleType = ruleType;
        this.databaseType = databaseType;
        dataSourceMap = createDataSourceMap();
        dataSource = createDataSource();
    }
    
    private Map<String, DataSource> createDataSourceMap() throws IOException, JAXBException {
        Collection<String> dataSourceNames = SchemaEnvironmentManager.getDataSourceNames(ruleType);
        Map<String, DataSource> result = new HashMap<>(dataSourceNames.size(), 1);
        for (String each : dataSourceNames) {
            result.put(each, DataSourceUtil.createDataSource(databaseType, each));
        }
        return result;
    }
    
    private DataSource createDataSource() throws SQLException, IOException {
        return IntegrateTestEnvironment.getInstance().isProxyEnvironment() ? ProxyDataSourceUtil.createDataSource(databaseType, "proxy_" + ruleType)
            : YamlShardingSphereDataSourceFactory.createDataSource(dataSourceMap, new File(EnvironmentPath.getRuleResourceFile(ruleType)));
    }
    
    protected final String getExpectedDataFile(final String path, final String ruleType, final DatabaseType databaseType, final String expectedDataFile) {
        if (null == expectedDataFile) {
            return null;
        }
        String prefix = path.substring(0, path.lastIndexOf(File.separator));
        String result = Joiner.on("/").join(prefix, "dataset", ruleType, databaseType.getName().toLowerCase(), expectedDataFile);
        if (new File(result).exists()) {
            return result;
        }
        result = Joiner.on("/").join(prefix, "dataset", ruleType, expectedDataFile);
        if (new File(result).exists()) {
            return result;
        }
        return Joiner.on("/").join(prefix, "dataset", expectedDataFile);
    }
    
    protected static void createDatabasesAndTables() {
        createDatabases();
        dropTables();
        createTables();
    }
    
    protected static void createDatabases() {
        try {
            for (String each : IntegrateTestEnvironment.getInstance().getRuleTypes()) {
                SchemaEnvironmentManager.dropDatabase(each);
            }
            for (String each : IntegrateTestEnvironment.getInstance().getRuleTypes()) {
                SchemaEnvironmentManager.createDatabase(each);
            }
        } catch (final JAXBException | IOException | SQLException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    protected static void createTables() {
        try {
            for (String each : IntegrateTestEnvironment.getInstance().getRuleTypes()) {
                SchemaEnvironmentManager.createTable(each);
            }
        } catch (final JAXBException | IOException | SQLException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    protected static void dropDatabases() {
        try {
            for (String each : IntegrateTestEnvironment.getInstance().getRuleTypes()) {
                SchemaEnvironmentManager.dropDatabase(each);
            }
        } catch (final JAXBException | IOException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    protected static void dropTables() {
        try {
            for (String each : IntegrateTestEnvironment.getInstance().getRuleTypes()) {
                SchemaEnvironmentManager.dropTable(each);
            }
        } catch (final JAXBException | IOException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    @After
    public void tearDown() {
        if (dataSource instanceof ShardingSphereDataSource) {
            ((ShardingSphereDataSource) dataSource).getSchemaContexts().getDefaultSchemaContext().getRuntimeContext().getExecutorKernel().close();
        }
    }
    
    private static void waitForProxy() {
        int retryCount = 1;
        while (!isProxyAvailable() && retryCount < 30) {
            try {
                Thread.sleep(1000L);
            } catch (final InterruptedException ignore) {
            }
            retryCount++;
        }
    }
    
    private static boolean isProxyAvailable() {
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://127.0.0.1:33070/proxy_db/?serverTimezone=UTC&useSSL=false&useLocalSessionState=true");
             Statement statement = connection.createStatement()) {
            statement.execute("SELECT 1");
            
        } catch (final SQLException ignore) {
            return false;
        }
        return true;
    }
}

