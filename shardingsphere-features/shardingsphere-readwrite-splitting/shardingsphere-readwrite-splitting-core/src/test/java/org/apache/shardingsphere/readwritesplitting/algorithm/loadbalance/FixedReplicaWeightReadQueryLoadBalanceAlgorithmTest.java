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

import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.readwritesplitting.factory.ReadQueryLoadBalanceAlgorithmFactory;
import org.apache.shardingsphere.transaction.TransactionHolder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertTrue;

public final class FixedReplicaWeightReadQueryLoadBalanceAlgorithmTest {
    
    @SuppressWarnings("rawtypes")
    @Before
    @After
    public void reset() throws NoSuchFieldException, IllegalAccessException {
        Field accuracyThresholdField = FixedReplicaWeightReadQueryLoadBalanceAlgorithm.class.getDeclaredField("WEIGHT_MAP");
        accuracyThresholdField.setAccessible(true);
        ((Map) accuracyThresholdField.get(FixedReplicaWeightReadQueryLoadBalanceAlgorithm.class)).clear();
    }
    
    @Test
    public void assertGetSingleReadDataSourceInTransaction() {
        FixedReplicaWeightReadQueryLoadBalanceAlgorithm loadBalanceAlgorithm = createReadQueryLoadBalanceAlgorithm(createSingleDataSourceProperties());
        TransactionHolder.isTransaction();
        String routeDataSource = loadBalanceAlgorithm.getDataSource("ds", "test_write_ds", Collections.singletonList("test_read_ds_1"));
        assertThat(routeDataSource, is("test_read_ds_1"));
        assertThat(loadBalanceAlgorithm.getDataSource("ds", "test_write_ds", Collections.singletonList("test_read_ds_1")), is(routeDataSource));
        TransactionHolder.clear();
    }
    
    private Properties createSingleDataSourceProperties() {
        Properties result = new Properties();
        result.setProperty("test_read_ds_1", "5");
        return result;
    }
    
    @Test
    public void assertGetMultipleReadDataSourcesWithoutTransaction() {
        FixedReplicaWeightReadQueryLoadBalanceAlgorithm loadBalanceAlgorithm = createReadQueryLoadBalanceAlgorithm(createMultipleDataSourcesProperties());
        String writeDataSourceName = "test_write_ds";
        String readDataSourceName1 = "test_read_ds_1";
        String readDataSourceName2 = "test_read_ds_2";
        List<String> readDataSourceNames = Arrays.asList(readDataSourceName1, readDataSourceName2);
        List<String> noTransactionReadDataSourceNames = new LinkedList<>();
        for (int i = 0; i < 5; i++) {
            String routeDataSource = loadBalanceAlgorithm.getDataSource("ds", writeDataSourceName, readDataSourceNames);
            noTransactionReadDataSourceNames.add(routeDataSource);
        }
        assertTrue(noTransactionReadDataSourceNames.size() > 1);
    }
    
    @Test
    public void assertGetMultipleReadDataSourcesInTransaction() {
        FixedReplicaWeightReadQueryLoadBalanceAlgorithm loadBalanceAlgorithm = createReadQueryLoadBalanceAlgorithm(createMultipleDataSourcesProperties());
        String writeDataSourceName = "test_write_ds";
        String readDataSourceName1 = "test_read_ds_1";
        String readDataSourceName2 = "test_read_ds_2";
        List<String> readDataSourceNames = Arrays.asList(readDataSourceName1, readDataSourceName2);
        TransactionHolder.setInTransaction();
        String routeDataSource = loadBalanceAlgorithm.getDataSource("ds", "test_write_ds", readDataSourceNames);
        assertThat(loadBalanceAlgorithm.getDataSource("ds", writeDataSourceName, readDataSourceNames), is(routeDataSource));
        assertThat(loadBalanceAlgorithm.getDataSource("ds", writeDataSourceName, readDataSourceNames), is(routeDataSource));
        assertThat(loadBalanceAlgorithm.getDataSource("ds", writeDataSourceName, readDataSourceNames), is(routeDataSource));
        TransactionHolder.clear();
    }
    
    private Properties createMultipleDataSourcesProperties() {
        Properties result = new Properties();
        result.setProperty("test_read_ds_1", "5");
        result.setProperty("test_read_ds_2", "5");
        return result;
    }
    
    private FixedReplicaWeightReadQueryLoadBalanceAlgorithm createReadQueryLoadBalanceAlgorithm(final Properties props) {
        return (FixedReplicaWeightReadQueryLoadBalanceAlgorithm) ReadQueryLoadBalanceAlgorithmFactory.newInstance(new ShardingSphereAlgorithmConfiguration("FIXED_REPLICA_WEIGHT", props));
    }
}
