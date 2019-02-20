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

import io.shardingsphere.example.jdbc.nodep.factory.DataSourceFactory;
import io.shardingsphere.example.repository.api.senario.TransactionServiceScenario;
import io.shardingsphere.example.repository.api.trace.ResultAssertUtils;
import io.shardingsphere.example.repository.jdbc.service.RawPojoTransactionService;
import io.shardingsphere.example.type.ShardingType;
import org.junit.Test;

import javax.sql.DataSource;
import java.sql.SQLException;

public class YamlConfigurationTransactionExampleTest {
    
    @Test
    public void assertShardingDatabase() throws SQLException {
        DataSource dataSource = DataSourceFactory.newInstance(ShardingType.SHARDING_DATABASES);
        TransactionServiceScenario scenario = new TransactionServiceScenario(new RawPojoTransactionService(dataSource));
        scenario.process();
        ResultAssertUtils.assertTransactionServiceResult(scenario.getTransactionService());
    }
    
    @Test
    public void assertShardingTables() throws SQLException {
        DataSource dataSource = DataSourceFactory.newInstance(ShardingType.SHARDING_TABLES);
        TransactionServiceScenario scenario = new TransactionServiceScenario(new RawPojoTransactionService(dataSource));
        scenario.process();
        ResultAssertUtils.assertTransactionServiceResult(scenario.getTransactionService());
    }
    
    @Test
    public void assertShardingDatabaseAndTables() throws SQLException {
        DataSource dataSource = DataSourceFactory.newInstance(ShardingType.SHARDING_DATABASES_AND_TABLES);
        TransactionServiceScenario scenario = new TransactionServiceScenario(new RawPojoTransactionService(dataSource));
        scenario.process();
        ResultAssertUtils.assertTransactionServiceResult(scenario.getTransactionService());
    }
    
    @Test
    public void assertMasterSlave() throws SQLException {
        DataSource dataSource = DataSourceFactory.newInstance(ShardingType.MASTER_SLAVE);
        TransactionServiceScenario scenario = new TransactionServiceScenario(new RawPojoTransactionService(dataSource));
        scenario.process();
        ResultAssertUtils.assertTransactionServiceResult(scenario.getTransactionService());
    }
    
    @Test
    public void assertShardingMasterSlave() throws SQLException {
        DataSource dataSource = DataSourceFactory.newInstance(ShardingType.SHARDING_MASTER_SLAVE);
        TransactionServiceScenario scenario = new TransactionServiceScenario(new RawPojoTransactionService(dataSource));
        scenario.process();
        ResultAssertUtils.assertTransactionServiceResult(scenario.getTransactionService());
    }
}
