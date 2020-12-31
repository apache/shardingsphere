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

package org.apache.shardingsphere.test.integration.engine.it;

import lombok.AccessLevel;
import lombok.Getter;
import org.apache.shardingsphere.driver.api.yaml.YamlShardingSphereDataSourceFactory;
import org.apache.shardingsphere.driver.jdbc.core.datasource.ShardingSphereDataSource;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.test.integration.env.EnvironmentPath;
import org.apache.shardingsphere.test.integration.env.IntegrateTestEnvironment;
import org.apache.shardingsphere.test.integration.env.datasource.builder.ActualDataSourceBuilder;
import org.apache.shardingsphere.test.integration.env.datasource.builder.ProxyDataSourceBuilder;
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
import java.util.Map;
import java.util.TimeZone;

@RunWith(Parameterized.class)
@Getter(AccessLevel.PROTECTED)
public abstract class BaseIT {
    
    public static final String NOT_VERIFY_FLAG = "NOT_VERIFY";
    
    private final String ruleType;
    
    private final DatabaseType databaseType;
    
    private final Map<String, DataSource> actualDataSources;
    
    private DataSource targetDataSource;
    
    static {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        if (IntegrateTestEnvironment.getInstance().isProxyEnvironment()) {
            waitForProxyReady();
        }
    }
    
    BaseIT(final String ruleType, final DatabaseType databaseType) throws IOException, JAXBException, SQLException {
        this.ruleType = ruleType;
        this.databaseType = databaseType;
        actualDataSources = ActualDataSourceBuilder.createActualDataSources(ruleType, databaseType);
        targetDataSource = createTargetDataSource();
    }
    
    private DataSource createTargetDataSource() throws SQLException, IOException {
        return IntegrateTestEnvironment.getInstance().isProxyEnvironment() ? ProxyDataSourceBuilder.build(String.format("proxy_%s", ruleType), databaseType) 
                : YamlShardingSphereDataSourceFactory.createDataSource(actualDataSources, new File(EnvironmentPath.getRulesConfigurationFile(ruleType)));
    }
    
    protected final void resetTargetDataSource() throws IOException, SQLException {
        targetDataSource = createTargetDataSource();
    }
    
    @After
    public final void tearDown() {
        if (targetDataSource instanceof ShardingSphereDataSource) {
            ((ShardingSphereDataSource) targetDataSource).getMetaDataContexts().getExecutorEngine().close();
        }
    }
    
    private static void waitForProxyReady() {
        int retryCount = 0;
        while (!isProxyReady() && retryCount < 30) {
            try {
                Thread.sleep(1000L);
            } catch (final InterruptedException ignore) {
            }
            retryCount++;
        }
    }
    
    private static boolean isProxyReady() {
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://127.0.0.1:33070/proxy_db/?serverTimezone=UTC&useSSL=false&useLocalSessionState=true");
             Statement statement = connection.createStatement()) {
            statement.execute("SELECT 1");
        } catch (final SQLException ignore) {
            return false;
        }
        return true;
    }
}
