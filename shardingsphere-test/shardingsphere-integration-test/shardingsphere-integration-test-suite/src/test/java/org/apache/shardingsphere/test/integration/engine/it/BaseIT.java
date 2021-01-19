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
import org.apache.shardingsphere.test.integration.env.IntegrationTestEnvironment;
import org.apache.shardingsphere.test.integration.env.datasource.builder.ActualDataSourceBuilder;
import org.apache.shardingsphere.test.integration.env.datasource.builder.ProxyDataSourceBuilder;
import org.apache.shardingsphere.test.integration.env.database.DatabaseEnvironmentManager;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import javax.sql.DataSource;
import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;
import java.util.TimeZone;

@RunWith(Parameterized.class)
@Getter(AccessLevel.PROTECTED)
public abstract class BaseIT {
    
    public static final String NOT_VERIFY_FLAG = "NOT_VERIFY";
    
    private final String adapter;
    
    private final String scenario;
    
    private final DatabaseType databaseType;
    
    private final Map<String, DataSource> actualDataSources;
    
    private DataSource targetDataSource;
    
    static {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }
    
    BaseIT(final String adapter, final String scenario, final DatabaseType databaseType) throws IOException, JAXBException, SQLException {
        this.adapter = adapter;
        this.scenario = scenario;
        this.databaseType = databaseType;
        actualDataSources = ActualDataSourceBuilder.createActualDataSources(scenario, databaseType);
        targetDataSource = createTargetDataSource();
    }
    
    private DataSource createTargetDataSource() throws SQLException, IOException {
        if ("proxy".equalsIgnoreCase(adapter)) {
            return ProxyDataSourceBuilder.build(scenario, databaseType, IntegrationTestEnvironment.getInstance().getProxyEnvironments().get(scenario));
        }
        return YamlShardingSphereDataSourceFactory.createDataSource(actualDataSources, new File(EnvironmentPath.getRulesConfigurationFile(scenario)));
    }
    
    protected final void resetTargetDataSource() throws IOException, SQLException {
        targetDataSource = createTargetDataSource();
    }
    
    @BeforeClass
    public static void executeInitSQLs() throws IOException, JAXBException, SQLException {
        if (!IntegrationTestEnvironment.getInstance().isEnvironmentPrepared()) {
            DatabaseEnvironmentManager.executeInitSQLs();
        }
    }
    
    @After
    public final void tearDown() {
        if (targetDataSource instanceof ShardingSphereDataSource) {
            ((ShardingSphereDataSource) targetDataSource).getMetaDataContexts().getExecutorEngine().close();
        }
    }
}
