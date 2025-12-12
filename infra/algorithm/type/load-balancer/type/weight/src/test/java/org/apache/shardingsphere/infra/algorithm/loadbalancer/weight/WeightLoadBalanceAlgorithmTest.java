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

package org.apache.shardingsphere.infra.algorithm.loadbalancer.weight;

import org.apache.shardingsphere.infra.algorithm.core.exception.AlgorithmInitializationException;
import org.apache.shardingsphere.infra.algorithm.loadbalancer.spi.LoadBalanceAlgorithm;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder.Property;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class WeightLoadBalanceAlgorithmTest {
    
    @Test
    void assertInitFailed() {
        assertThrows(AlgorithmInitializationException.class, () -> TypedSPILoader.getService(LoadBalanceAlgorithm.class, "WEIGHT", PropertiesBuilder.build(new Property("test_read_ds_1", "a"))));
    }
    
    @Test
    void assertCheck() {
        LoadBalanceAlgorithm loadBalanceAlgorithm = TypedSPILoader.getService(LoadBalanceAlgorithm.class, "WEIGHT", PropertiesBuilder.build(new Property("test_read_ds_1", "5")));
        assertThrows(AlgorithmInitializationException.class, () -> loadBalanceAlgorithm.check("foo_db", Collections.singletonList("test_read_ds_0")));
    }
    
    @Test
    void assertGetSingleAvailableTarget() {
        LoadBalanceAlgorithm loadBalanceAlgorithm = TypedSPILoader.getService(LoadBalanceAlgorithm.class, "WEIGHT", PropertiesBuilder.build(new Property("test_read_ds_1", "5")));
        assertThat(loadBalanceAlgorithm.getTargetName("ds", Collections.singletonList("test_read_ds_1")), is("test_read_ds_1"));
    }
    
    @Test
    void assertGetMultipleAvailableTargets() {
        LoadBalanceAlgorithm loadBalanceAlgorithm = TypedSPILoader.getService(LoadBalanceAlgorithm.class,
                "WEIGHT", PropertiesBuilder.build(new Property("test_read_ds_1", "5"), new Property("test_read_ds_2", "5")));
        String availableTargetName1 = "test_read_ds_1";
        String availableTargetName2 = "test_read_ds_2";
        List<String> availableTargetNames = Arrays.asList(availableTargetName1, availableTargetName2);
        assertWeightLoadBalance(loadBalanceAlgorithm, availableTargetNames);
    }
    
    private void assertWeightLoadBalance(final LoadBalanceAlgorithm loadBalanceAlgorithm, final List<String> availableTargetNames) {
        assertThat(loadBalanceAlgorithm.getTargetName("ds", availableTargetNames), notNullValue());
        assertThat(loadBalanceAlgorithm.getTargetName("ds", availableTargetNames), notNullValue());
        assertThat(loadBalanceAlgorithm.getTargetName("ds", availableTargetNames), notNullValue());
    }
    
    @Test
    void assertGetAvailableTargetNameWhenTargetChanged() {
        LoadBalanceAlgorithm loadBalanceAlgorithm = TypedSPILoader.getService(LoadBalanceAlgorithm.class,
                "WEIGHT", PropertiesBuilder.build(new Property("test_read_ds_1", "5"), new Property("test_read_ds_2", "5")));
        loadBalanceAlgorithm.getTargetName("ds", Arrays.asList("test_read_ds_1", "test_read_ds_1"));
        assertThat(loadBalanceAlgorithm.getTargetName("ds", Collections.singletonList("test_read_ds_1")), is("test_read_ds_1"));
    }
}
