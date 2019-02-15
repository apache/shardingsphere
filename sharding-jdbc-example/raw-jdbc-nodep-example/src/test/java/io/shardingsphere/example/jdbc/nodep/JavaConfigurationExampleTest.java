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

package io.shardingsphere.example.jdbc.nodep;

import io.shardingsphere.example.jdbc.nodep.factory.CommonServiceFactory;
import io.shardingsphere.example.repository.api.senario.CommonServiceScenario;
import io.shardingsphere.example.type.ShardingType;
import org.junit.Test;

import java.sql.SQLException;

public class JavaConfigurationExampleTest extends BaseConfigurationExample {

    @Test
    public void assertShardingDatabasePrecise() throws SQLException {
        CommonServiceScenario scenario = new CommonServiceScenario(CommonServiceFactory.newInstance(ShardingType.SHARDING_DATABASES));
        scenario.executeShardingCRUDSuccess();
        assertShardingDatabaseResult(scenario.getCommonService(), false);
    }

    @Test
    public void assertShardingDatabaseRange() throws SQLException {
        CommonServiceScenario scenario = new CommonServiceScenario(CommonServiceFactory.newInstance(ShardingType.SHARDING_DATABASES_RANGE));
        scenario.executeShardingCRUDSuccess();
        assertShardingDatabaseResult(scenario.getCommonService(), true);
    }
    
    @Test
    public void assertShardingTablesPrecise() throws SQLException {
        CommonServiceScenario scenario = new CommonServiceScenario(CommonServiceFactory.newInstance(ShardingType.SHARDING_TABLES));
        scenario.executeShardingCRUDSuccess();
        assertShardingTableResult(scenario.getCommonService(), false);
    }
    
    @Test
    public void assertShardingTablesRange() throws SQLException {
        CommonServiceScenario scenario = new CommonServiceScenario(CommonServiceFactory.newInstance(ShardingType.SHARDING_TABLES_RANGE));
        scenario.executeShardingCRUDSuccess();
        assertShardingTableResult(scenario.getCommonService(), true);
    }

    @Test
    public void assertShardingDatabaseAndTablesPrecise() throws SQLException {
        CommonServiceScenario scenario = new CommonServiceScenario(CommonServiceFactory.newInstance(ShardingType.SHARDING_DATABASES_AND_TABLES));
        scenario.executeShardingCRUDSuccess();
        assertShardingDatabaseAndTableResult(scenario.getCommonService(), false);
    }

    @Test
    public void assertShardingDatabaseAndTablesRange() throws SQLException {
        CommonServiceScenario scenario = new CommonServiceScenario(CommonServiceFactory.newInstance(ShardingType.SHARDING_DATABASES_AND_TABLES_RANGE));
        scenario.executeShardingCRUDSuccess();
        assertShardingDatabaseAndTableResult(scenario.getCommonService(), true);
    }

    @Test
    public void assertMasterSlave() throws SQLException {
        CommonServiceScenario scenario = new CommonServiceScenario(CommonServiceFactory.newInstance(ShardingType.MASTER_SLAVE));
        scenario.executeShardingCRUDSuccess();
        assertMasterSlaveResult(scenario.getCommonService());
    }

    @Test
    public void assertShardingMasterSlavePrecise() throws SQLException {
        CommonServiceScenario scenario = new CommonServiceScenario(CommonServiceFactory.newInstance(ShardingType.SHARDING_MASTER_SLAVE));
        scenario.executeShardingCRUDSuccess();
        assertMasterSlaveResult(scenario.getCommonService());
    }

    @Test
    public void assertShardingMasterSlaveRange() throws SQLException {
        CommonServiceScenario scenario = new CommonServiceScenario(CommonServiceFactory.newInstance(ShardingType.SHARDING_MASTER_SLAVE_RANGE));
        scenario.executeShardingCRUDSuccess();
        assertMasterSlaveResult(scenario.getCommonService());
    }
}
