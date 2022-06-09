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
import org.apache.shardingsphere.readwritesplitting.factory.ReplicaLoadBalanceAlgorithmFactory;
import org.apache.shardingsphere.transaction.TransactionHolder;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

public final class WeightReplicaLoadBalanceAlgorithmTest {
    
    @Test
    public void assertGetSingleReadDataSource() {
        WeightReplicaLoadBalanceAlgorithm weightReplicaLoadBalanceAlgorithm = createReplicaLoadBalanceAlgorithm(createSingleDataSourceProperties());
        assertThat(weightReplicaLoadBalanceAlgorithm.getDataSource("ds", "test_write_ds", Collections.singletonList("test_read_ds_1")), is("test_read_ds_1"));
    }
    
    private Properties createSingleDataSourceProperties() {
        Properties result = new Properties();
        result.setProperty("test_read_ds_1", "5");
        return result;
    }
    
    @Test
    public void assertGetMultipleReadDataSources() {
        WeightReplicaLoadBalanceAlgorithm weightReplicaLoadBalanceAlgorithm = createReplicaLoadBalanceAlgorithm(createMultipleDataSourcesProperties());
        String writeDataSourceName = "test_write_ds";
        String readDataSourceName1 = "test_read_ds_1";
        String readDataSourceName2 = "test_read_ds_2";
        List<String> readDataSourceNames = Arrays.asList(readDataSourceName1, readDataSourceName2);
        assertThat(weightReplicaLoadBalanceAlgorithm.getDataSource("ds", writeDataSourceName, readDataSourceNames), notNullValue());
        assertThat(weightReplicaLoadBalanceAlgorithm.getDataSource("ds", writeDataSourceName, readDataSourceNames), notNullValue());
        assertThat(weightReplicaLoadBalanceAlgorithm.getDataSource("ds", writeDataSourceName, readDataSourceNames), notNullValue());
    }
    
    private Properties createMultipleDataSourcesProperties() {
        Properties result = new Properties();
        result.setProperty("test_read_ds_1", "5");
        result.setProperty("test_read_ds_2", "5");
        return result;
    }
    
    private WeightReplicaLoadBalanceAlgorithm createReplicaLoadBalanceAlgorithm(final Properties props) {
        return (WeightReplicaLoadBalanceAlgorithm) ReplicaLoadBalanceAlgorithmFactory.newInstance(new ShardingSphereAlgorithmConfiguration("WEIGHT", props));
    }
    
    @Test
    public void assertGetReadDataSourceInTransaction() {
        WeightReplicaLoadBalanceAlgorithm weightReplicaLoadBalanceAlgorithm = createReplicaLoadBalanceAlgorithm(createMultipleDataSourcesProperties());
        String writeDataSourceName = "test_write_ds";
        String readDataSourceName1 = "test_read_ds_1";
        String readDataSourceName2 = "test_read_ds_2";
        List<String> readDataSourceNames = Arrays.asList(readDataSourceName1, readDataSourceName2);
        TransactionHolder.setInTransaction();
        assertThat(weightReplicaLoadBalanceAlgorithm.getDataSource("ds", writeDataSourceName, readDataSourceNames), is(writeDataSourceName));
        TransactionHolder.clear();
    }
}
