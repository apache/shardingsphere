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

import org.apache.shardingsphere.infra.util.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.readwritesplitting.spi.ReadQueryLoadBalanceAlgorithm;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertTrue;

class RandomReadQueryLoadBalanceAlgorithmTest {
    
    @Test
    void assertGetDataSourceWithDefaultStrategy() {
        ReadQueryLoadBalanceAlgorithm loadBalanceAlgorithm = TypedSPILoader.getService(ReadQueryLoadBalanceAlgorithm.class, "RANDOM", new Properties());
        String writeDataSourceName = "test_write_ds";
        String readDataSourceName1 = "test_read_ds_1";
        String readDataSourceName2 = "test_read_ds_2";
        List<String> readDataSourceNames = Arrays.asList(readDataSourceName1, readDataSourceName2);
        assertRandomReadQueryLoadBalance(readDataSourceNames, loadBalanceAlgorithm, writeDataSourceName);
        assertTrue(readDataSourceNames.contains(loadBalanceAlgorithm.getDataSource("ds", writeDataSourceName, readDataSourceNames)));
        assertTrue(readDataSourceNames.contains(loadBalanceAlgorithm.getDataSource("ds", writeDataSourceName, readDataSourceNames)));
    }
    
    private void assertRandomReadQueryLoadBalance(final List<String> readDataSourceNames, final ReadQueryLoadBalanceAlgorithm loadBalanceAlgorithm, final String writeDataSourceName) {
        assertTrue(readDataSourceNames.contains(loadBalanceAlgorithm.getDataSource("ds", writeDataSourceName, readDataSourceNames)));
        assertTrue(readDataSourceNames.contains(loadBalanceAlgorithm.getDataSource("ds", writeDataSourceName, readDataSourceNames)));
        assertTrue(readDataSourceNames.contains(loadBalanceAlgorithm.getDataSource("ds", writeDataSourceName, readDataSourceNames)));
    }
}
