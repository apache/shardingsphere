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

import org.apache.shardingsphere.infra.algorithm.ShardingSphereAlgorithmFactory;
import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.context.transaction.TransactionConnectionContext;
import org.apache.shardingsphere.readwritesplitting.spi.ReadQueryLoadBalanceAlgorithm;
import org.apache.shardingsphere.test.util.PropertiesBuilder;
import org.apache.shardingsphere.test.util.PropertiesBuilder.Property;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.internal.configuration.plugins.Plugins;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;

public final class FixedReplicaWeightReadQueryLoadBalanceAlgorithmTest {
    
    @Before
    @After
    public void reset() throws NoSuchFieldException, IllegalAccessException {
        ((Map<?, ?>) Plugins.getMemberAccessor().get(
                FixedReplicaWeightReadQueryLoadBalanceAlgorithm.class.getDeclaredField("WEIGHT_MAP"), FixedReplicaWeightReadQueryLoadBalanceAlgorithm.class)).clear();
    }
    
    @Test
    public void assertGetSingleReadDataSourceInTransaction() {
        ReadQueryLoadBalanceAlgorithm loadBalanceAlgorithm = ShardingSphereAlgorithmFactory.createAlgorithm(
                new AlgorithmConfiguration("FIXED_REPLICA_WEIGHT", PropertiesBuilder.build(new Property("test_read_ds_1", "5"))), ReadQueryLoadBalanceAlgorithm.class);
        TransactionConnectionContext context = new TransactionConnectionContext();
        context.setInTransaction(true);
        String routeDataSource = loadBalanceAlgorithm.getDataSource("ds", "test_write_ds", Collections.singletonList("test_read_ds_1"), context);
        assertThat(routeDataSource, is("test_read_ds_1"));
        assertThat(loadBalanceAlgorithm.getDataSource("ds", "test_write_ds", Collections.singletonList("test_read_ds_1"), context), is(routeDataSource));
    }
    
    @Test
    public void assertGetMultipleReadDataSourcesWithoutTransaction() {
        ReadQueryLoadBalanceAlgorithm loadBalanceAlgorithm = ShardingSphereAlgorithmFactory.createAlgorithm(new AlgorithmConfiguration(
                "FIXED_REPLICA_WEIGHT", PropertiesBuilder.build(new Property("test_read_ds_1", "5"), new Property("test_read_ds_2", "5"))), ReadQueryLoadBalanceAlgorithm.class);
        String writeDataSourceName = "test_write_ds";
        String readDataSourceName1 = "test_read_ds_1";
        String readDataSourceName2 = "test_read_ds_2";
        List<String> readDataSourceNames = Arrays.asList(readDataSourceName1, readDataSourceName2);
        List<String> noTransactionReadDataSourceNames = new LinkedList<>();
        for (int i = 0; i < 5; i++) {
            String routeDataSource = loadBalanceAlgorithm.getDataSource("ds", writeDataSourceName, readDataSourceNames, new TransactionConnectionContext());
            noTransactionReadDataSourceNames.add(routeDataSource);
        }
        assertTrue(noTransactionReadDataSourceNames.size() > 1);
    }
    
    @Test
    public void assertGetMultipleReadDataSourcesInTransaction() {
        ReadQueryLoadBalanceAlgorithm loadBalanceAlgorithm = ShardingSphereAlgorithmFactory.createAlgorithm(new AlgorithmConfiguration(
                "FIXED_REPLICA_WEIGHT", PropertiesBuilder.build(new Property("test_read_ds_1", "5"), new Property("test_read_ds_2", "5"))), ReadQueryLoadBalanceAlgorithm.class);
        String writeDataSourceName = "test_write_ds";
        String readDataSourceName1 = "test_read_ds_1";
        String readDataSourceName2 = "test_read_ds_2";
        List<String> readDataSourceNames = Arrays.asList(readDataSourceName1, readDataSourceName2);
        TransactionConnectionContext transactionConnectionContext = new TransactionConnectionContext();
        transactionConnectionContext.setInTransaction(true);
        String routeDataSource = loadBalanceAlgorithm.getDataSource("ds", "test_write_ds", readDataSourceNames, transactionConnectionContext);
        assertThat(loadBalanceAlgorithm.getDataSource("ds", writeDataSourceName, readDataSourceNames, transactionConnectionContext), is(routeDataSource));
        assertThat(loadBalanceAlgorithm.getDataSource("ds", writeDataSourceName, readDataSourceNames, transactionConnectionContext), is(routeDataSource));
        assertThat(loadBalanceAlgorithm.getDataSource("ds", writeDataSourceName, readDataSourceNames, transactionConnectionContext), is(routeDataSource));
    }
    
    @Test
    public void assertGetDataSourceWhenReadDataSourceChanged() {
        ReadQueryLoadBalanceAlgorithm algorithm = ShardingSphereAlgorithmFactory.createAlgorithm(new AlgorithmConfiguration(
                "FIXED_REPLICA_WEIGHT", PropertiesBuilder.build(new Property("test_read_ds_1", "1"), new Property("test_read_ds_2", "2"))), ReadQueryLoadBalanceAlgorithm.class);
        algorithm.getDataSource("ds", "test_write_ds", Arrays.asList("test_read_ds_1", "test_read_ds_1"), new TransactionConnectionContext());
        assertThat(algorithm.getDataSource("ds", "test_write_ds", Collections.singletonList("test_read_ds_1"), new TransactionConnectionContext()), is("test_read_ds_1"));
    }
}
