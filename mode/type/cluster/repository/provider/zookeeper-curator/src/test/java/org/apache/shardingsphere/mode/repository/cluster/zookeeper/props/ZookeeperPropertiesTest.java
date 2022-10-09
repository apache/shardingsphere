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

package org.apache.shardingsphere.mode.repository.cluster.zookeeper.props;

import org.junit.Test;

import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public final class ZookeeperPropertiesTest {
    
    @Test
    public void assertGetValue() {
        ZookeeperProperties actual = new ZookeeperProperties(createProperties());
        assertThat(actual.getValue(ZookeeperPropertyKey.RETRY_INTERVAL_MILLISECONDS), is(3000));
        assertThat(actual.getValue(ZookeeperPropertyKey.MAX_RETRIES), is(2));
        assertThat(actual.getValue(ZookeeperPropertyKey.TIME_TO_LIVE_SECONDS), is(600));
        assertThat(actual.getValue(ZookeeperPropertyKey.OPERATION_TIMEOUT_MILLISECONDS), is(6000));
        assertThat(actual.getValue(ZookeeperPropertyKey.DIGEST), is("any"));
    }
    
    private Properties createProperties() {
        Properties result = new Properties();
        result.setProperty(ZookeeperPropertyKey.RETRY_INTERVAL_MILLISECONDS.getKey(), "3000");
        result.setProperty(ZookeeperPropertyKey.MAX_RETRIES.getKey(), "2");
        result.setProperty(ZookeeperPropertyKey.TIME_TO_LIVE_SECONDS.getKey(), "600");
        result.setProperty(ZookeeperPropertyKey.OPERATION_TIMEOUT_MILLISECONDS.getKey(), "6000");
        result.setProperty(ZookeeperPropertyKey.DIGEST.getKey(), "any");
        return result;
    }
    
    @Test
    public void assertGetDefaultValue() {
        ZookeeperProperties actual = new ZookeeperProperties(new Properties());
        assertThat(actual.getValue(ZookeeperPropertyKey.RETRY_INTERVAL_MILLISECONDS), is(500));
        assertThat(actual.getValue(ZookeeperPropertyKey.MAX_RETRIES), is(3));
        assertThat(actual.getValue(ZookeeperPropertyKey.TIME_TO_LIVE_SECONDS), is(60));
        assertThat(actual.getValue(ZookeeperPropertyKey.OPERATION_TIMEOUT_MILLISECONDS), is(500));
        assertThat(actual.getValue(ZookeeperPropertyKey.DIGEST), is(""));
    }
}
