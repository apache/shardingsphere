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

package org.apache.shardingsphere.orchestration.repository.nacos;

import org.junit.Test;

import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class NacosPropertiesTest {
    
    @Test
    public void assertGetValue() {
        Properties props = new Properties();
        props.setProperty(NacosPropertyKey.GROUP.getKey(), "SHARDING_SPHERE_TEST_GROUP");
        props.setProperty(NacosPropertyKey.TIMEOUT.getKey(), "6000");
        NacosProperties actual = new NacosProperties(props);
        assertThat(actual.getValue(NacosPropertyKey.GROUP), is("SHARDING_SPHERE_TEST_GROUP"));
        assertThat(actual.getValue(NacosPropertyKey.TIMEOUT), is(6000L));
    }
    
    @Test
    public void assertGetDefaultValue() {
        Properties props = new Properties();
        NacosProperties actual = new NacosProperties(props);
        assertThat(actual.getValue(NacosPropertyKey.GROUP), is("SHARDING_SPHERE_DEFAULT_GROUP"));
        assertThat(actual.getValue(NacosPropertyKey.TIMEOUT), is(3000L));
    }
}
