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
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;

public final class WeightReadQueryLoadBalanceAlgorithmTest {
    
    @Test
    public void assertGetSingleReadDataSource() {
        ReadQueryLoadBalanceAlgorithm loadBalanceAlgorithm = TypedSPILoader.getService(ReadQueryLoadBalanceAlgorithm.class, "WEIGHT", PropertiesBuilder.build(new Property("test_read_ds_1", "5")));
        assertThat(loadBalanceAlgorithm.getDataSource("ds", "test_write_ds", Collections.singletonList("test_read_ds_1"), new TransactionConnectionContext()), is("test_read_ds_1"));
    }
    
    @Test
    public void assertGetMultipleReadDataSources() {
        ReadQueryLoadBalanceAlgorithm loadBalanceAlgorithm = TypedSPILoader.getService(ReadQueryLoadBalanceAlgorithm.class,
                "WEIGHT", PropertiesBuilder.build(new Property("test_read_ds_1", "5"), new Property("test_read_ds_2", "5")));
        String writeDataSourceName = "test_write_ds";
        String readDataSourceName1 = "test_read_ds_1";
        String readDataSourceName2 = "test_read_ds_2";
        List<String> readDataSourceNames = Arrays.asList(readDataSourceName1, readDataSourceName2);
        assertWeightReadQueryLoadBalance(loadBalanceAlgorithm, writeDataSourceName, readDataSourceNames);
    }
    
    private void assertWeightReadQueryLoadBalance(final ReadQueryLoadBalanceAlgorithm loadBalanceAlgorithm, final String writeDataSourceName, final List<String> readDataSourceNames) {
        assertThat(loadBalanceAlgorithm.getDataSource("ds", writeDataSourceName, readDataSourceNames, new TransactionConnectionContext()), notNullValue());
        assertThat(loadBalanceAlgorithm.getDataSource("ds", writeDataSourceName, readDataSourceNames, new TransactionConnectionContext()), notNullValue());
        assertThat(loadBalanceAlgorithm.getDataSource("ds", writeDataSourceName, readDataSourceNames, new TransactionConnectionContext()), notNullValue());
    }
    
    @Test
    public void assertGetReadDataSourceDefaultStrategy() {
        ReadQueryLoadBalanceAlgorithm loadBalanceAlgorithm = TypedSPILoader.getService(ReadQueryLoadBalanceAlgorithm.class,
                "WEIGHT", PropertiesBuilder.build(new Property("test_read_ds_1", "5"), new Property("test_read_ds_2", "5")));
        String writeDataSourceName = "test_write_ds";
        String readDataSourceName1 = "test_read_ds_1";
        String readDataSourceName2 = "test_read_ds_2";
        List<String> readDataSourceNames = Arrays.asList(readDataSourceName1, readDataSourceName2);
        TransactionConnectionContext context = new TransactionConnectionContext();
        assertWeightReadQueryLoadBalance(loadBalanceAlgorithm, writeDataSourceName, readDataSourceNames);
        context.setInTransaction(true);
        assertThat(loadBalanceAlgorithm.getDataSource("ds", writeDataSourceName, readDataSourceNames, context), is(writeDataSourceName));
    }
    
    @Test
    public void assertGetDataSourceWithFixedPrimaryStrategy() {
        ReadQueryLoadBalanceAlgorithm loadBalanceAlgorithm = TypedSPILoader.getService(ReadQueryLoadBalanceAlgorithm.class, "WEIGHT",
                PropertiesBuilder.build(new Property("test_read_ds_1", "5"), new Property("test_read_ds_2", "5"),
                        new Property(TransactionReadQueryStrategyAware.TRANSACTION_READ_QUERY_STRATEGY, TransactionReadQueryStrategy.FIXED_PRIMARY.name())));
        String writeDataSourceName = "test_write_ds";
        String readDataSourceName1 = "test_read_ds_1";
        String readDataSourceName2 = "test_read_ds_2";
        List<String> readDataSourceNames = Arrays.asList(readDataSourceName1, readDataSourceName2);
        TransactionConnectionContext context = new TransactionConnectionContext();
        assertWeightReadQueryLoadBalance(loadBalanceAlgorithm, writeDataSourceName, readDataSourceNames);
        context.setInTransaction(true);
        assertTrue(writeDataSourceName.contains(loadBalanceAlgorithm.getDataSource("ds", writeDataSourceName, readDataSourceNames, context)));
        assertTrue(writeDataSourceName.contains(loadBalanceAlgorithm.getDataSource("ds", writeDataSourceName, readDataSourceNames, context)));
    }
    
    @Test
    public void assertGetDataSourceWithFixedReplicaStrategy() {
        ReadQueryLoadBalanceAlgorithm loadBalanceAlgorithm = TypedSPILoader.getService(ReadQueryLoadBalanceAlgorithm.class, "WEIGHT",
                PropertiesBuilder.build(new Property("test_read_ds_1", "5"), new Property("test_read_ds_2", "5"),
                        new Property(TransactionReadQueryStrategyAware.TRANSACTION_READ_QUERY_STRATEGY, TransactionReadQueryStrategy.FIXED_REPLICA.name())));
        String writeDataSourceName = "test_write_ds";
        String readDataSourceName1 = "test_read_ds_1";
        String readDataSourceName2 = "test_read_ds_2";
        List<String> readDataSourceNames = Arrays.asList(readDataSourceName1, readDataSourceName2);
        TransactionConnectionContext context = new TransactionConnectionContext();
        assertWeightReadQueryLoadBalance(loadBalanceAlgorithm, writeDataSourceName, readDataSourceNames);
        context.setInTransaction(true);
        String routeDataSource = loadBalanceAlgorithm.getDataSource("ds", writeDataSourceName, readDataSourceNames, context);
        assertTrue(readDataSourceNames.contains(loadBalanceAlgorithm.getDataSource("ds", writeDataSourceName, readDataSourceNames, context)));
        assertThat(loadBalanceAlgorithm.getDataSource("ds", writeDataSourceName, readDataSourceNames, context), is(routeDataSource));
        assertThat(loadBalanceAlgorithm.getDataSource("ds", writeDataSourceName, readDataSourceNames, context), is(routeDataSource));
    }
    
    @Test
    public void assertGetDataSourceWithDynamicReplicaStrategy() {
        ReadQueryLoadBalanceAlgorithm loadBalanceAlgorithm = TypedSPILoader.getService(ReadQueryLoadBalanceAlgorithm.class, "WEIGHT",
                PropertiesBuilder.build(new Property("test_read_ds_1", "5"), new Property("test_read_ds_2", "5"),
                        new Property(TransactionReadQueryStrategyAware.TRANSACTION_READ_QUERY_STRATEGY, TransactionReadQueryStrategy.DYNAMIC_REPLICA.name())));
        String writeDataSourceName = "test_write_ds";
        String readDataSourceName1 = "test_read_ds_1";
        String readDataSourceName2 = "test_read_ds_2";
        List<String> readDataSourceNames = Arrays.asList(readDataSourceName1, readDataSourceName2);
        TransactionConnectionContext context = new TransactionConnectionContext();
        assertWeightReadQueryLoadBalance(loadBalanceAlgorithm, writeDataSourceName, readDataSourceNames);
        context.setInTransaction(true);
        assertWeightReadQueryLoadBalance(loadBalanceAlgorithm, writeDataSourceName, readDataSourceNames);
    }
    
    @Test
    public void assertGetDataSourceWhenReadDataSourceChanged() {
        ReadQueryLoadBalanceAlgorithm loadBalanceAlgorithm = TypedSPILoader.getService(ReadQueryLoadBalanceAlgorithm.class,
                "WEIGHT", PropertiesBuilder.build(new Property("test_read_ds_1", "5"), new Property("test_read_ds_2", "5")));
        loadBalanceAlgorithm.getDataSource("ds", "test_write_ds", Arrays.asList("test_read_ds_1", "test_read_ds_1"), new TransactionConnectionContext());
        assertThat(loadBalanceAlgorithm.getDataSource("ds", "test_write_ds", Collections.singletonList("test_read_ds_1"), new TransactionConnectionContext()), is("test_read_ds_1"));
    }
}
