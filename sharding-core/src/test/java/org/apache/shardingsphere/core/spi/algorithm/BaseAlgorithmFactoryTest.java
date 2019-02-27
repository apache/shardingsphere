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

package org.apache.shardingsphere.core.spi.algorithm;

import org.apache.shardingsphere.core.exception.ShardingConfigurationException;
import org.apache.shardingsphere.core.spi.algorithm.fixture.BaseAlgorithmFactoryFixture;
import org.apache.shardingsphere.core.spi.algorithm.fixture.BaseAlgorithmFixture;
import org.apache.shardingsphere.core.spi.algorithm.fixture.BaseAlgorithmFixtureImpl;
import org.junit.Test;

import java.util.Properties;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class BaseAlgorithmFactoryTest {
    
    @Test
    public void assertNewAlgorithmByType() {
        Properties properties = new Properties();
        properties.setProperty("key", "value");
        BaseAlgorithmFixture actual = BaseAlgorithmFactoryFixture.getInstance().newAlgorithm("FIXTURE", properties);
        assertThat(actual, instanceOf(BaseAlgorithmFixtureImpl.class));
        assertThat(actual.getProperties().getProperty("key"), is("value"));
    }
    
    @Test
    public void assertNewAlgorithmByDefault() {
        BaseAlgorithmFixture actual = BaseAlgorithmFactoryFixture.getInstance().newAlgorithm();
        assertThat(actual, instanceOf(BaseAlgorithmFixtureImpl.class));
        assertTrue(actual.getProperties().isEmpty());
    }
    
    @Test(expected = ShardingConfigurationException.class)
    public void assertNewAlgorithmFailure() {
        BaseAlgorithmFactoryFixture.getInstance().newAlgorithm("INVALID", new Properties());
    }
}
