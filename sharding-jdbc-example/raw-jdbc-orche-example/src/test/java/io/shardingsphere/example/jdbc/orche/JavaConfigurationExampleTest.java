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

package io.shardingsphere.example.jdbc.orche;

import io.shardingsphere.example.jdbc.orche.factory.OrchestrationDataSourceFactory;
import io.shardingsphere.example.repository.api.senario.CommonServiceScenario;
import io.shardingsphere.example.repository.api.trace.ResultAssertUtils;
import io.shardingsphere.example.repository.jdbc.service.RawPojoService;
import io.shardingsphere.example.type.RegistryCenterType;
import io.shardingsphere.example.type.ShardingType;
import org.apache.shardingsphere.shardingjdbc.orchestration.internal.datasource.AbstractOrchestrationDataSource;
import org.junit.After;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import javax.sql.DataSource;
import java.lang.reflect.Method;
import java.sql.SQLException;

@FixMethodOrder(MethodSorters.JVM)
public class JavaConfigurationExampleTest {
    
    private DataSource dataSource;
    
    @After
    public void tearDown() throws Exception {
        closeDataSource(dataSource);
    }
    
    private void closeDataSource(final DataSource dataSource) throws Exception {
        Method method = dataSource.getClass().getMethod("close");
        method.setAccessible(true);
        method.invoke(dataSource);
    }

    @Test
    public void assertShardingDatabaseWithLocalZookeeper() throws SQLException {
        dataSource = OrchestrationDataSourceFactory.newInstance(ShardingType.SHARDING_DATABASES, RegistryCenterType.ZOOKEEPER, false);
        CommonServiceScenario scenario = new CommonServiceScenario(new RawPojoService(dataSource));
        scenario.process();
        ResultAssertUtils.assertShardingDatabaseResult(scenario.getCommonService(), false);
    }
    
    @Test
    public void assertShardingDatabaseWithCloudZookeeper() throws SQLException {
        dataSource = OrchestrationDataSourceFactory.newInstance(ShardingType.SHARDING_DATABASES, RegistryCenterType.ZOOKEEPER, true);
        CommonServiceScenario scenario = new CommonServiceScenario(new RawPojoService(dataSource));
        scenario.process();
        ResultAssertUtils.assertShardingDatabaseResult(scenario.getCommonService(), false);
    }

    @Test
    public void assertShardingTablesWithLocalZookeeper() throws SQLException {
        dataSource = OrchestrationDataSourceFactory.newInstance(ShardingType.SHARDING_TABLES, RegistryCenterType.ZOOKEEPER, false);
        CommonServiceScenario scenario = new CommonServiceScenario(new RawPojoService(dataSource));
        scenario.process();
        ResultAssertUtils.assertShardingTableResult(scenario.getCommonService(), false);
    }
    
    @Test
    public void assertShardingTablesWithCloudZookeeper() throws SQLException {
        dataSource = OrchestrationDataSourceFactory.newInstance(ShardingType.SHARDING_TABLES, RegistryCenterType.ZOOKEEPER, true);
        CommonServiceScenario scenario = new CommonServiceScenario(new RawPojoService(dataSource));
        scenario.process();
        ResultAssertUtils.assertShardingTableResult(scenario.getCommonService(), false);
    }

    @Test
    public void assertShardingDatabaseAndTablesWithLocalZookeeper() throws SQLException {
        dataSource = OrchestrationDataSourceFactory.newInstance(ShardingType.SHARDING_DATABASES_AND_TABLES, RegistryCenterType.ZOOKEEPER, false);
        CommonServiceScenario scenario = new CommonServiceScenario(new RawPojoService(dataSource));
        scenario.process();
        ResultAssertUtils.assertShardingDatabaseAndTableResult(scenario.getCommonService(), false);
    }
    
    @Test
    public void assertShardingDatabaseAndTablesWithCloudZookeeper() throws SQLException {
        dataSource = OrchestrationDataSourceFactory.newInstance(ShardingType.SHARDING_DATABASES_AND_TABLES, RegistryCenterType.ZOOKEEPER, true);
        CommonServiceScenario scenario = new CommonServiceScenario(new RawPojoService(dataSource));
        scenario.process();
        ResultAssertUtils.assertShardingDatabaseAndTableResult(scenario.getCommonService(), false);
    }

    @Test
    public void assertMasterSlaveWithLocalZookeeper() throws SQLException {
        dataSource = OrchestrationDataSourceFactory.newInstance(ShardingType.MASTER_SLAVE, RegistryCenterType.ZOOKEEPER, false);
        CommonServiceScenario scenario = new CommonServiceScenario(new RawPojoService(dataSource));
        scenario.process();
        ResultAssertUtils.assertMasterSlaveResult(scenario.getCommonService());
    }
    
    @Test
    public void assertMasterSlaveWithCloudZookeeper() throws SQLException {
        dataSource = OrchestrationDataSourceFactory.newInstance(ShardingType.MASTER_SLAVE, RegistryCenterType.ZOOKEEPER, true);
        CommonServiceScenario scenario = new CommonServiceScenario(new RawPojoService(dataSource));
        scenario.process();
        ResultAssertUtils.assertMasterSlaveResult(scenario.getCommonService());
    }

    @Test
    public void assertShardingMasterSlaveWithLocalZookeeper() throws SQLException {
        dataSource = OrchestrationDataSourceFactory.newInstance(ShardingType.SHARDING_MASTER_SLAVE, RegistryCenterType.ZOOKEEPER, false);
        CommonServiceScenario scenario = new CommonServiceScenario(new RawPojoService(dataSource));
        scenario.process();
        ResultAssertUtils.assertMasterSlaveResult(scenario.getCommonService());
    }
    
    @Test
    public void assertShardingMasterSlaveWithCloudZookeeper() throws SQLException {
        dataSource = OrchestrationDataSourceFactory.newInstance(ShardingType.SHARDING_MASTER_SLAVE, RegistryCenterType.ZOOKEEPER, true);
        CommonServiceScenario scenario = new CommonServiceScenario(new RawPojoService(dataSource));
        scenario.process();
        ResultAssertUtils.assertMasterSlaveResult(scenario.getCommonService());
    }
}
