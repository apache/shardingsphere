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

package org.apache.shardingsphere.sharding.algorithm.sharding.classbased;

import com.google.common.collect.Range;
import org.apache.shardingsphere.infra.algorithm.ShardingSphereAlgorithmFactory;
import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.datanode.DataNodeInfo;
import org.apache.shardingsphere.sharding.api.sharding.complex.ComplexKeysShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.hint.HintShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.PreciseShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.RangeShardingValue;
import org.apache.shardingsphere.sharding.exception.algorithm.sharding.ShardingAlgorithmClassImplementationException;
import org.apache.shardingsphere.sharding.exception.algorithm.sharding.ShardingAlgorithmInitializationException;
import org.apache.shardingsphere.sharding.fixture.ClassBasedComplexKeysShardingAlgorithmFixture;
import org.apache.shardingsphere.sharding.fixture.ClassBasedHintShardingAlgorithmFixture;
import org.apache.shardingsphere.sharding.fixture.ClassBasedStandardShardingAlgorithmFixture;
import org.apache.shardingsphere.sharding.spi.ShardingAlgorithm;
import org.apache.shardingsphere.test.util.PropertiesBuilder;
import org.apache.shardingsphere.test.util.PropertiesBuilder.Property;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public final class ClassBasedShardingAlgorithmTest {
    
    @Test(expected = ShardingAlgorithmInitializationException.class)
    public void assertInitWithNullStrategy() {
        Properties props = PropertiesBuilder.build();
        ShardingSphereAlgorithmFactory.createAlgorithm(new AlgorithmConfiguration("CLASS_BASED", props), ShardingAlgorithm.class);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertInitWithWrongStrategy() {
        Properties props = PropertiesBuilder.build(new Property("strategy", "wrong"));
        ShardingSphereAlgorithmFactory.createAlgorithm(new AlgorithmConfiguration("CLASS_BASED", props), ShardingAlgorithm.class);
    }
    
    @Test(expected = ShardingAlgorithmInitializationException.class)
    public void assertInitWithNullClass() {
        Properties props = PropertiesBuilder.build(new Property("strategy", "standard"));
        ShardingSphereAlgorithmFactory.createAlgorithm(new AlgorithmConfiguration("CLASS_BASED", props), ShardingAlgorithm.class);
    }
    
    @Test(expected = ClassNotFoundException.class)
    public void assertInitWithUndefinedClass() {
        Properties props = PropertiesBuilder.build(new Property("strategy", "standard"), new Property("algorithmClassName", "org.apache.shardingsphere.sharding.UndefinedClass"));
        ShardingSphereAlgorithmFactory.createAlgorithm(new AlgorithmConfiguration("CLASS_BASED", props), ShardingAlgorithm.class);
    }
    
    @Test(expected = ShardingAlgorithmClassImplementationException.class)
    public void assertInitWithMismatchStrategy() {
        Properties props = PropertiesBuilder.build(new Property("strategy", "standard"), new Property("algorithmClassName", ClassBasedComplexKeysShardingAlgorithmFixture.class.getName()));
        ShardingSphereAlgorithmFactory.createAlgorithm(new AlgorithmConfiguration("CLASS_BASED", props), ShardingAlgorithm.class);
    }
    
    @Test
    public void assertPreciseDoSharding() {
        Properties props = PropertiesBuilder.build(new Property("strategy", "standard"), new Property("algorithmClassName", ClassBasedStandardShardingAlgorithmFixture.class.getName()));
        ClassBasedShardingAlgorithm algorithm = ShardingSphereAlgorithmFactory.createAlgorithm(new AlgorithmConfiguration("CLASS_BASED", props), ShardingAlgorithm.class);
        Collection<String> availableTargetNames = Arrays.asList("t_order_0", "t_order_1", "t_order_2", "t_order_3");
        assertThat(algorithm.doSharding(availableTargetNames, new PreciseShardingValue<>("t_order", "order_id", new DataNodeInfo("t_order_", 1, '0'), 0)), is("t_order_0"));
    }
    
    @Test
    public void assertRangeDoSharding() {
        Properties props = PropertiesBuilder.build(new Property("strategy", "standard"), new Property("algorithmClassName", ClassBasedStandardShardingAlgorithmFixture.class.getName()));
        ClassBasedShardingAlgorithm algorithm = ShardingSphereAlgorithmFactory.createAlgorithm(new AlgorithmConfiguration("CLASS_BASED", props), ShardingAlgorithm.class);
        Collection<String> availableTargetNames = Arrays.asList("t_order_0", "t_order_1", "t_order_2", "t_order_3");
        Collection<String> actual = algorithm.doSharding(availableTargetNames, new RangeShardingValue<>("t_order", "order_id", new DataNodeInfo("t_order_", 1, '0'), Range.closed(2, 15)));
        assertThat(actual.size(), is(4));
    }
    
    @Test
    public void assertComplexKeysDoSharding() {
        Properties props = PropertiesBuilder.build(new Property("strategy", "complex"), new Property("algorithmClassName", ClassBasedComplexKeysShardingAlgorithmFixture.class.getName()));
        ClassBasedShardingAlgorithm algorithm = ShardingSphereAlgorithmFactory.createAlgorithm(new AlgorithmConfiguration("CLASS_BASED", props), ShardingAlgorithm.class);
        Collection<String> availableTargetNames = Arrays.asList("t_order_0", "t_order_1", "t_order_2", "t_order_3");
        Collection<String> actual = algorithm.doSharding(availableTargetNames, new ComplexKeysShardingValue<>("t_order", null, null));
        assertThat(actual.size(), is(4));
    }
    
    @Test
    public void assertHintDoSharding() {
        Properties props = PropertiesBuilder.build(new Property("strategy", "hint"), new Property("algorithmClassName", ClassBasedHintShardingAlgorithmFixture.class.getName()));
        ClassBasedShardingAlgorithm algorithm = ShardingSphereAlgorithmFactory.createAlgorithm(new AlgorithmConfiguration("CLASS_BASED", props), ShardingAlgorithm.class);
        Collection<String> availableTargetNames = Arrays.asList("t_order_0", "t_order_1", "t_order_2", "t_order_3");
        Collection<String> actual = algorithm.doSharding(availableTargetNames, new HintShardingValue<>("t_order", "order_id", null));
        assertThat(actual.size(), is(4));
    }
}
