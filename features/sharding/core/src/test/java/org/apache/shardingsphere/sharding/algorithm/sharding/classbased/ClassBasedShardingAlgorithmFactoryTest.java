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

import org.apache.shardingsphere.sharding.api.sharding.standard.StandardShardingAlgorithm;
import org.apache.shardingsphere.sharding.exception.algorithm.ShardingAlgorithmClassImplementationException;
import org.apache.shardingsphere.sharding.fixture.ClassBasedHintShardingAlgorithmFixture;
import org.apache.shardingsphere.sharding.fixture.ClassBasedStandardShardingAlgorithmFixture;
import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ClassBasedShardingAlgorithmFactoryTest {
    
    @Test
    void assertNewInstanceWithUnAssignableFrom() {
        assertThrows(ShardingAlgorithmClassImplementationException.class,
                () -> ClassBasedShardingAlgorithmFactory.newInstance(ClassBasedHintShardingAlgorithmFixture.class.getName(), StandardShardingAlgorithm.class, new Properties()));
    }
    
    @Test
    void assertNewInstance() {
        assertThat(ClassBasedShardingAlgorithmFactory.newInstance(ClassBasedStandardShardingAlgorithmFixture.class.getName(), StandardShardingAlgorithm.class, new Properties()),
                isA(ClassBasedStandardShardingAlgorithmFixture.class));
    }
}
