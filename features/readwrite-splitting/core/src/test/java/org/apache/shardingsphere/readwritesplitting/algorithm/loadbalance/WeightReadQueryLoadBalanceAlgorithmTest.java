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

import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.readwritesplitting.spi.ReadQueryLoadBalanceAlgorithm;
import org.apache.shardingsphere.test.util.PropertiesBuilder;
import org.apache.shardingsphere.test.util.PropertiesBuilder.Property;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

class WeightReadQueryLoadBalanceAlgorithmTest {
    
    @Test
    void assertGetSingleReadDataSource() {
        ReadQueryLoadBalanceAlgorithm loadBalanceAlgorithm = TypedSPILoader.getService(ReadQueryLoadBalanceAlgorithm.class, "WEIGHT", PropertiesBuilder.build(new Property("test_read_ds_1", "5")));
        assertThat(loadBalanceAlgorithm.getDataSource("ds", "test_write_ds", Collections.singletonList("test_read_ds_1")), is("test_read_ds_1"));
    }
    
    @Test
    void assertGetMultipleReadDataSources() {
        ReadQueryLoadBalanceAlgorithm loadBalanceAlgorithm = TypedSPILoader.getService(ReadQueryLoadBalanceAlgorithm.class,
                "WEIGHT", PropertiesBuilder.build(new Property("test_read_ds_1", "5"), new Property("test_read_ds_2", "5")));
        String writeDataSourceName = "test_write_ds";
        String readDataSourceName1 = "test_read_ds_1";
        String readDataSourceName2 = "test_read_ds_2";
        List<String> readDataSourceNames = Arrays.asList(readDataSourceName1, readDataSourceName2);
        assertWeightReadQueryLoadBalance(loadBalanceAlgorithm, writeDataSourceName, readDataSourceNames);
    }
    
    private void assertWeightReadQueryLoadBalance(final ReadQueryLoadBalanceAlgorithm loadBalanceAlgorithm, final String writeDataSourceName, final List<String> readDataSourceNames) {
        assertThat(loadBalanceAlgorithm.getDataSource("ds", writeDataSourceName, readDataSourceNames), notNullValue());
        assertThat(loadBalanceAlgorithm.getDataSource("ds", writeDataSourceName, readDataSourceNames), notNullValue());
        assertThat(loadBalanceAlgorithm.getDataSource("ds", writeDataSourceName, readDataSourceNames), notNullValue());
    }
    
    @Test
    void assertGetDataSourceWhenReadDataSourceChanged() {
        ReadQueryLoadBalanceAlgorithm loadBalanceAlgorithm = TypedSPILoader.getService(ReadQueryLoadBalanceAlgorithm.class,
                "WEIGHT", PropertiesBuilder.build(new Property("test_read_ds_1", "5"), new Property("test_read_ds_2", "5")));
        loadBalanceAlgorithm.getDataSource("ds", "test_write_ds", Arrays.asList("test_read_ds_1", "test_read_ds_1"));
        assertThat(loadBalanceAlgorithm.getDataSource("ds", "test_write_ds", Collections.singletonList("test_read_ds_1")), is("test_read_ds_1"));
    }
}
