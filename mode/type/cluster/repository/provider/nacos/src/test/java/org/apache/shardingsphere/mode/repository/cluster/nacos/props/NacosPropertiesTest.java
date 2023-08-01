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

package org.apache.shardingsphere.mode.repository.cluster.nacos.props;

import org.apache.shardingsphere.test.util.PropertiesBuilder;
import org.apache.shardingsphere.test.util.PropertiesBuilder.Property;
import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class NacosPropertiesTest {
    
    @Test
    void assertGetValue() {
        NacosProperties actual = new NacosProperties(createProperties());
        assertThat(actual.getValue(NacosPropertyKey.CLUSTER_IP), is("127.0.0.1"));
        assertThat(actual.getValue(NacosPropertyKey.RETRY_INTERVAL_MILLISECONDS), is(1000L));
        assertThat(actual.getValue(NacosPropertyKey.MAX_RETRIES), is(5));
        assertThat(actual.getValue(NacosPropertyKey.TIME_TO_LIVE_SECONDS), is(60));
        assertThat(actual.getValue(NacosPropertyKey.USERNAME), is("nacos"));
        assertThat(actual.getValue(NacosPropertyKey.PASSWORD), is("nacos"));
    }
    
    private Properties createProperties() {
        return PropertiesBuilder.build(
                new Property(NacosPropertyKey.CLUSTER_IP.getKey(), "127.0.0.1"),
                new Property(NacosPropertyKey.RETRY_INTERVAL_MILLISECONDS.getKey(), "1000"),
                new Property(NacosPropertyKey.MAX_RETRIES.getKey(), "5"),
                new Property(NacosPropertyKey.TIME_TO_LIVE_SECONDS.getKey(), "60"),
                new Property(NacosPropertyKey.USERNAME.getKey(), "nacos"),
                new Property(NacosPropertyKey.PASSWORD.getKey(), "nacos"));
    }
    
    @Test
    void assertGetDefaultValue() {
        NacosProperties actual = new NacosProperties(new Properties());
        assertThat(actual.getValue(NacosPropertyKey.CLUSTER_IP), is(""));
        assertThat(actual.getValue(NacosPropertyKey.RETRY_INTERVAL_MILLISECONDS), is(500L));
        assertThat(actual.getValue(NacosPropertyKey.MAX_RETRIES), is(3));
        assertThat(actual.getValue(NacosPropertyKey.TIME_TO_LIVE_SECONDS), is(30));
        assertThat(actual.getValue(NacosPropertyKey.USERNAME), is(""));
        assertThat(actual.getValue(NacosPropertyKey.PASSWORD), is(""));
    }
}
