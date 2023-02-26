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

package org.apache.shardingsphere.readwritesplitting.algorithm.loadbalance;

import org.apache.shardingsphere.infra.context.transaction.TransactionConnectionContext;
import org.apache.shardingsphere.infra.util.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.readwritesplitting.api.transaction.TransactionReadQueryStrategyAware;
import org.apache.shardingsphere.readwritesplitting.api.transaction.TransactionReadQueryStrategy;
import org.apache.shardingsphere.readwritesplitting.spi.ReadQueryLoadBalanceAlgorithm;
import org.apache.shardingsphere.test.util.PropertiesBuilder;
import org.apache.shardingsphere.test.util.PropertiesBuilder.Property;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;

public final class RoundRobinReadQueryLoadBalanceAlgorithmTest {
    
    @Test
    public void assertGetDataSourceWithDefaultStrategy() {
        ReadQueryLoadBalanceAlgorithm loadBalanceAlgorithm = TypedSPILoader.getService(ReadQueryLoadBalanceAlgorithm.class, "ROUND_ROBIN", new Properties());
        String writeDataSourceName = "test_write_ds";
        String readDataSourceName1 = "test_read_ds_1";
        String readDataSourceName2 = "test_read_ds_2";
        List<String> readDataSourceNames = Arrays.asList(readDataSourceName1, readDataSourceName2);
        TransactionConnectionContext context = new TransactionConnectionContext();
        assertRoundRobinReadQueryLoadBalance(writeDataSourceName, readDataSourceName1, readDataSourceName2, loadBalanceAlgorithm, readDataSourceNames, context);
    }
    
    private void assertRoundRobinReadQueryLoadBalance(final String writeDataSourceName, final String readDataSourceName1, final String readDataSourceName2,
                                                      final ReadQueryLoadBalanceAlgorithm loadBalanceAlgorithm, final List<String> readDataSourceNames, final TransactionConnectionContext context) {
        assertThat(loadBalanceAlgorithm.getDataSource("ds", writeDataSourceName, readDataSourceNames, context), is(readDataSourceName1));
        assertThat(loadBalanceAlgorithm.getDataSource("ds", writeDataSourceName, readDataSourceNames, context), is(readDataSourceName2));
        assertThat(loadBalanceAlgorithm.getDataSource("ds", writeDataSourceName, readDataSourceNames, context), is(readDataSourceName1));
        assertThat(loadBalanceAlgorithm.getDataSource("ds", writeDataSourceName, readDataSourceNames, context), is(readDataSourceName2));
    }
    
    @Test
    public void assertGetDataSourceWithFixedPrimaryStrategy() {
        ReadQueryLoadBalanceAlgorithm loadBalanceAlgorithm = TypedSPILoader.getService(ReadQueryLoadBalanceAlgorithm.class, "ROUND_ROBIN",
                PropertiesBuilder.build(new Property(TransactionReadQueryStrategyAware.TRANSACTION_READ_QUERY_STRATEGY, TransactionReadQueryStrategy.FIXED_PRIMARY.name())));
        String writeDataSourceName = "test_write_ds";
        String readDataSourceName1 = "test_read_ds_1";
        String readDataSourceName2 = "test_read_ds_2";
        List<String> readDataSourceNames = Arrays.asList(readDataSourceName1, readDataSourceName2);
        TransactionConnectionContext context = new TransactionConnectionContext();
        assertRoundRobinReadQueryLoadBalance(writeDataSourceName, readDataSourceName1, readDataSourceName2, loadBalanceAlgorithm, readDataSourceNames, context);
        context.setInTransaction(true);
        assertTrue(writeDataSourceName.contains(loadBalanceAlgorithm.getDataSource("ds", writeDataSourceName, readDataSourceNames, context)));
        assertTrue(writeDataSourceName.contains(loadBalanceAlgorithm.getDataSource("ds", writeDataSourceName, readDataSourceNames, context)));
    }
    
    @Test
    public void assertGetDataSourceWithFixedReplicaStrategy() {
        ReadQueryLoadBalanceAlgorithm loadBalanceAlgorithm = TypedSPILoader.getService(ReadQueryLoadBalanceAlgorithm.class, "ROUND_ROBIN",
                PropertiesBuilder.build(new Property(TransactionReadQueryStrategyAware.TRANSACTION_READ_QUERY_STRATEGY, TransactionReadQueryStrategy.FIXED_REPLICA.name())));
        String writeDataSourceName = "test_write_ds";
        String readDataSourceName1 = "test_read_ds_1";
        String readDataSourceName2 = "test_read_ds_2";
        List<String> readDataSourceNames = Arrays.asList(readDataSourceName1, readDataSourceName2);
        TransactionConnectionContext context = new TransactionConnectionContext();
        assertRoundRobinReadQueryLoadBalance(writeDataSourceName, readDataSourceName1, readDataSourceName2, loadBalanceAlgorithm, readDataSourceNames, context);
        context.setInTransaction(true);
        String routeDataSource = loadBalanceAlgorithm.getDataSource("ds", writeDataSourceName, readDataSourceNames, context);
        assertTrue(readDataSourceNames.contains(loadBalanceAlgorithm.getDataSource("ds", writeDataSourceName, readDataSourceNames, context)));
        assertThat(loadBalanceAlgorithm.getDataSource("ds", writeDataSourceName, readDataSourceNames, context), is(routeDataSource));
        assertThat(loadBalanceAlgorithm.getDataSource("ds", writeDataSourceName, readDataSourceNames, context), is(routeDataSource));
    }
    
    @Test
    public void assertGetDataSourceWithDynamicReplicaStrategy() {
        ReadQueryLoadBalanceAlgorithm loadBalanceAlgorithm = TypedSPILoader.getService(ReadQueryLoadBalanceAlgorithm.class, "ROUND_ROBIN",
                PropertiesBuilder.build(new Property(TransactionReadQueryStrategyAware.TRANSACTION_READ_QUERY_STRATEGY, TransactionReadQueryStrategy.DYNAMIC_REPLICA.name())));
        String writeDataSourceName = "test_write_ds";
        String readDataSourceName1 = "test_read_ds_1";
        String readDataSourceName2 = "test_read_ds_2";
        List<String> readDataSourceNames = Arrays.asList(readDataSourceName1, readDataSourceName2);
        TransactionConnectionContext context = new TransactionConnectionContext();
        assertRoundRobinReadQueryLoadBalance(writeDataSourceName, readDataSourceName1, readDataSourceName2, loadBalanceAlgorithm, readDataSourceNames, context);
        context.setInTransaction(true);
        assertRoundRobinReadQueryLoadBalance(writeDataSourceName, readDataSourceName1, readDataSourceName2, loadBalanceAlgorithm, readDataSourceNames, context);
    }
}
