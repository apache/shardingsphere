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
import org.apache.shardingsphere.infra.algorithm.core.exception.AlgorithmInitializationException;
import org.apache.shardingsphere.infra.datanode.DataNodeInfo;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder.Property;
import org.apache.shardingsphere.sharding.api.sharding.complex.ComplexKeysShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.hint.HintShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.PreciseShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.RangeShardingValue;
import org.apache.shardingsphere.sharding.exception.algorithm.ShardingAlgorithmClassImplementationException;
import org.apache.shardingsphere.sharding.fixture.ClassBasedComplexKeysShardingAlgorithmFixture;
import org.apache.shardingsphere.sharding.fixture.ClassBasedHintShardingAlgorithmFixture;
import org.apache.shardingsphere.sharding.fixture.ClassBasedStandardShardingAlgorithmFixture;
import org.apache.shardingsphere.sharding.spi.ShardingAlgorithm;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ClassBasedShardingAlgorithmTest {
    
    @Test
    void assertInitWithNullStrategy() {
        assertThrows(AlgorithmInitializationException.class, () -> TypedSPILoader.getService(ShardingAlgorithm.class, "CLASS_BASED"));
    }
    
    @Test
    void assertInitWithWrongStrategy() {
        assertThrows(AlgorithmInitializationException.class,
                () -> TypedSPILoader.getService(ShardingAlgorithm.class, "CLASS_BASED", PropertiesBuilder.build(new Property("strategy", "wrong"))));
    }
    
    @Test
    void assertInitWithNullClass() {
        assertThrows(AlgorithmInitializationException.class,
                () -> TypedSPILoader.getService(ShardingAlgorithm.class, "CLASS_BASED", PropertiesBuilder.build(new Property("strategy", "standard"))));
    }
    
    @Test
    void assertInitWithEmptyClassName() {
        assertThrows(AlgorithmInitializationException.class,
                () -> TypedSPILoader.getService(ShardingAlgorithm.class, "CLASS_BASED", PropertiesBuilder.build(new Property("strategy", "standard"), new Property("algorithmClassName", ""))));
    }
    
    @Test
    void assertInitWithUndefinedClass() {
        assertThrows(ClassNotFoundException.class,
                () -> TypedSPILoader.getService(ShardingAlgorithm.class,
                        "CLASS_BASED", PropertiesBuilder.build(new Property("strategy", "standard"), new Property("algorithmClassName", "org.apache.shardingsphere.sharding.UndefinedClass"))));
    }
    
    @Test
    void assertInitWithMismatchStrategy() {
        assertThrows(ShardingAlgorithmClassImplementationException.class,
                () -> TypedSPILoader.getService(ShardingAlgorithm.class, "CLASS_BASED",
                        PropertiesBuilder.build(new Property("strategy", "standard"), new Property("algorithmClassName", ClassBasedComplexKeysShardingAlgorithmFixture.class.getName()))));
    }
    
    @Test
    void assertPreciseDoSharding() {
        ClassBasedShardingAlgorithm algorithm = (ClassBasedShardingAlgorithm) TypedSPILoader.getService(ShardingAlgorithm.class,
                "CLASS_BASED", PropertiesBuilder.build(new Property("strategy", "standard"), new Property("algorithmClassName", ClassBasedStandardShardingAlgorithmFixture.class.getName())));
        Collection<String> availableTargetNames = Arrays.asList("t_order_0", "t_order_1", "t_order_2", "t_order_3");
        assertThat(algorithm.doSharding(availableTargetNames, new PreciseShardingValue<>("t_order", "order_id", new DataNodeInfo("t_order_", 1, '0'), 0)), is("t_order_0"));
    }
    
    @Test
    void assertRangeDoSharding() {
        ClassBasedShardingAlgorithm algorithm = (ClassBasedShardingAlgorithm) TypedSPILoader.getService(ShardingAlgorithm.class,
                "CLASS_BASED", PropertiesBuilder.build(new Property("strategy", "standard"), new Property("algorithmClassName", ClassBasedStandardShardingAlgorithmFixture.class.getName())));
        Collection<String> availableTargetNames = Arrays.asList("t_order_0", "t_order_1", "t_order_2", "t_order_3");
        Collection<String> actual = algorithm.doSharding(availableTargetNames, new RangeShardingValue<>("t_order", "order_id", new DataNodeInfo("t_order_", 1, '0'), Range.closed(2, 15)));
        assertThat(actual.size(), is(4));
    }
    
    @Test
    void assertComplexKeysDoSharding() {
        ClassBasedShardingAlgorithm algorithm = (ClassBasedShardingAlgorithm) TypedSPILoader.getService(ShardingAlgorithm.class,
                "CLASS_BASED", PropertiesBuilder.build(new Property("strategy", "complex"), new Property("algorithmClassName", ClassBasedComplexKeysShardingAlgorithmFixture.class.getName())));
        Collection<String> availableTargetNames = Arrays.asList("t_order_0", "t_order_1", "t_order_2", "t_order_3");
        Collection<String> actual = algorithm.doSharding(availableTargetNames, new ComplexKeysShardingValue<>("t_order", null, null));
        assertThat(actual.size(), is(4));
    }
    
    @Test
    void assertHintDoSharding() {
        ClassBasedShardingAlgorithm algorithm = (ClassBasedShardingAlgorithm) TypedSPILoader.getService(ShardingAlgorithm.class,
                "CLASS_BASED", PropertiesBuilder.build(new Property("strategy", "hint"), new Property("algorithmClassName", ClassBasedHintShardingAlgorithmFixture.class.getName())));
        Collection<String> availableTargetNames = Arrays.asList("t_order_0", "t_order_1", "t_order_2", "t_order_3");
        Collection<String> actual = algorithm.doSharding(availableTargetNames, new HintShardingValue<>("t_order", "order_id", null));
        assertThat(actual.size(), is(4));
    }
}
