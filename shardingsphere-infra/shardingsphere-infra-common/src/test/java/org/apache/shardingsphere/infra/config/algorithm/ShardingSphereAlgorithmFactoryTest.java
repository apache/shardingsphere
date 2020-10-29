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

package org.apache.shardingsphere.infra.config.algorithm;

import org.apache.shardingsphere.infra.config.fixture.WithInitAlgorithm;
import org.apache.shardingsphere.infra.config.fixture.WithoutInitAlgorithm;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Properties;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class ShardingSphereAlgorithmFactoryTest {
    
    @BeforeClass
    public static void setUp() {
        ShardingSphereServiceLoader.register(ShardingSphereAlgorithm.class);
    }
    
    @Test
    public void assertCreateAlgorithmWithoutInit() {
        Properties props = new Properties();
        props.setProperty("key", "value");
        ShardingSphereAlgorithm actual = ShardingSphereAlgorithmFactory.createAlgorithm(new ShardingSphereAlgorithmConfiguration("WITHOUT_INIT", props), ShardingSphereAlgorithm.class);
        assertThat(actual, instanceOf(WithoutInitAlgorithm.class));
        assertThat(actual.getProps().getProperty("key"), is("value"));
    }
    
    @Test
    public void assertCreateAlgorithmWithInit() {
        Properties props = new Properties();
        props.setProperty("key", "value");
        ShardingSphereAlgorithm actual = ShardingSphereAlgorithmFactory.createAlgorithm(new ShardingSphereAlgorithmConfiguration("WITH_INIT", props), ShardingSphereAlgorithm.class);
        assertThat(actual, instanceOf(WithInitAlgorithm.class));
        assertThat(actual.getProps().getProperty("key"), is("value"));
        assertThat(((WithInitAlgorithm) actual).getTestValue(), is("value"));
    }
}
