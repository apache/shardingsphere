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

package org.apache.shardingsphere.spring.namespace;

import org.apache.shardingsphere.spring.namespace.fixture.ShardingSphereFixtureAlgorithm;
import org.apache.shardingsphere.spring.namespace.fixture.FooShardingSphereFixtureAlgorithm;
import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

import javax.annotation.Resource;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@ContextConfiguration(locations = "classpath:META-INF/spring/application-context.xml")
public final class ShardingSphereAlgorithmSpringNamespaceTest extends AbstractJUnit4SpringContextTests {
    
    @Resource
    private ShardingSphereFixtureAlgorithm algorithmWithProps;
    
    @Resource
    private ShardingSphereFixtureAlgorithm algorithmWithoutProps;
    
    @Test
    public void assertAlgorithmWithProps() {
        assertThat(algorithmWithProps.getType(), is("FIXTURE"));
        assertThat(algorithmWithProps.getProps().getProperty("fixture.value"), is("foo"));
        assertThat(((FooShardingSphereFixtureAlgorithm) algorithmWithProps).getValue(), is("foo"));
    }
    
    @Test
    public void assertAlgorithmWithoutProps() {
        assertThat(algorithmWithoutProps.getType(), is("FIXTURE"));
        assertTrue(algorithmWithoutProps.getProps().isEmpty());
        assertNull(((FooShardingSphereFixtureAlgorithm) algorithmWithoutProps).getValue());
    }
}
