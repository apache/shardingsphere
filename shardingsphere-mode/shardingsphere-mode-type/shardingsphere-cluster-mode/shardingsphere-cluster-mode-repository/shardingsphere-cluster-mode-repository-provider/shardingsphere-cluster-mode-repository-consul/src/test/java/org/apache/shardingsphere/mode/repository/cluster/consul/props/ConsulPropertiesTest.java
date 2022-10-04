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

package org.apache.shardingsphere.mode.repository.cluster.consul.props;

import org.junit.Test;

import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class ConsulPropertiesTest {
    
    @Test
    public void assertGetValue() {
        assertThat(new ConsulProperties(createProperties()).getValue(ConsulPropertyKey.BLOCK_QUERY_TIME_IN_SECONDS), is(60L));
    }
    
    private Properties createProperties() {
        Properties result = new Properties();
        result.setProperty(ConsulPropertyKey.TIME_TO_LIVE_IN_SECONDS.getKey(), "50");
        return result;
    }
    
    @Test
    public void assertGetDefaultValue() {
        assertThat(new ConsulProperties(new Properties()).getValue(ConsulPropertyKey.TIME_TO_LIVE_IN_SECONDS), is(30L));
    }
}
