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

import io.shardingsphere.example.jdbc.orche.factory.CommonTransactionServiceFactory;
import io.shardingsphere.example.repository.api.senario.TransactionServiceScenario;
import io.shardingsphere.example.repository.api.trace.ResultAssertUtils;
import io.shardingsphere.example.type.RegistryCenterType;
import io.shardingsphere.example.type.ShardingType;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.sql.SQLException;

@FixMethodOrder(MethodSorters.JVM)
public class JavaConfigurationTransactionExampleTest {

    @Test
    public void assertShardingDatabaseWithLocalZookeeper() throws SQLException {
        TransactionServiceScenario scenario = new TransactionServiceScenario(CommonTransactionServiceFactory.newInstance(ShardingType.SHARDING_DATABASES, RegistryCenterType.ZOOKEEPER, false));
        scenario.executeShardingCRUDFailure();
        ResultAssertUtils.assertTransactionServiceResult(scenario.getTransactionService());
    }
    
    @Test
    public void assertShardingDatabaseWithCloudZookeeper() throws SQLException {
        TransactionServiceScenario scenario = new TransactionServiceScenario(CommonTransactionServiceFactory.newInstance(ShardingType.SHARDING_DATABASES, RegistryCenterType.ZOOKEEPER, true));
        scenario.executeShardingCRUDFailure();
        ResultAssertUtils.assertTransactionServiceResult(scenario.getTransactionService());
    }

    @Test
    public void assertShardingTablesWithLocalZookeeper() throws SQLException {
        TransactionServiceScenario scenario = new TransactionServiceScenario(CommonTransactionServiceFactory.newInstance(ShardingType.SHARDING_TABLES, RegistryCenterType.ZOOKEEPER, false));
        scenario.executeShardingCRUDFailure();
        ResultAssertUtils.assertTransactionServiceResult(scenario.getTransactionService());
    }
    
    @Test
    public void assertShardingTablesWithCloudZookeeper() throws SQLException {
        TransactionServiceScenario scenario = new TransactionServiceScenario(CommonTransactionServiceFactory.newInstance(ShardingType.SHARDING_TABLES, RegistryCenterType.ZOOKEEPER, true));
        scenario.executeShardingCRUDFailure();
        ResultAssertUtils.assertTransactionServiceResult(scenario.getTransactionService());
    }

    @Test
    public void assertShardingDatabaseAndTablesWithLocalZookeeper() throws SQLException {
        TransactionServiceScenario scenario = new TransactionServiceScenario(CommonTransactionServiceFactory.newInstance(ShardingType.SHARDING_DATABASES_AND_TABLES, RegistryCenterType.ZOOKEEPER, false));
        scenario.executeShardingCRUDFailure();
        ResultAssertUtils.assertTransactionServiceResult(scenario.getTransactionService());
    }
    
    @Test
    public void assertShardingDatabaseAndTablesWithCloudZookeeper() throws SQLException {
        TransactionServiceScenario scenario = new TransactionServiceScenario(CommonTransactionServiceFactory.newInstance(ShardingType.SHARDING_DATABASES_AND_TABLES, RegistryCenterType.ZOOKEEPER, true));
        scenario.executeShardingCRUDFailure();
        ResultAssertUtils.assertTransactionServiceResult(scenario.getTransactionService());
    }

    @Test
    public void assertMasterSlaveWithLocalZookeeper() throws SQLException {
        TransactionServiceScenario scenario = new TransactionServiceScenario(CommonTransactionServiceFactory.newInstance(ShardingType.MASTER_SLAVE, RegistryCenterType.ZOOKEEPER, false));
        scenario.executeShardingCRUDFailure();
        ResultAssertUtils.assertTransactionServiceResult(scenario.getTransactionService());
    }
    
    @Test
    public void assertMasterSlaveWithCloudZookeeper() throws SQLException {
        TransactionServiceScenario scenario = new TransactionServiceScenario(CommonTransactionServiceFactory.newInstance(ShardingType.MASTER_SLAVE, RegistryCenterType.ZOOKEEPER, true));
        scenario.executeShardingCRUDFailure();
        ResultAssertUtils.assertTransactionServiceResult(scenario.getTransactionService());
    }

    @Test
    public void assertShardingMasterSlaveWithLocalZookeeper() throws SQLException {
        TransactionServiceScenario scenario = new TransactionServiceScenario(CommonTransactionServiceFactory.newInstance(ShardingType.SHARDING_MASTER_SLAVE, RegistryCenterType.ZOOKEEPER, false));
        scenario.executeShardingCRUDFailure();
        ResultAssertUtils.assertTransactionServiceResult(scenario.getTransactionService());
    }
    
    @Test
    public void assertShardingMasterSlaveWithCloudZookeeper() throws SQLException {
        TransactionServiceScenario scenario = new TransactionServiceScenario(CommonTransactionServiceFactory.newInstance(ShardingType.SHARDING_MASTER_SLAVE, RegistryCenterType.ZOOKEEPER, true));
        scenario.executeShardingCRUDFailure();
        ResultAssertUtils.assertTransactionServiceResult(scenario.getTransactionService());
    }
}
