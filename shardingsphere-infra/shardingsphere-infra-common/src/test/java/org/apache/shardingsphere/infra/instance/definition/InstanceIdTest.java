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

package org.apache.shardingsphere.infra.instance.definition;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

public final class InstanceIdTest {
    
    @Test
    public void assertInitInstanceIdWithPort() {
        InstanceId actual = new InstanceId(3307);
        assertThat(actual.getUniqueSign(), is(3307));
    }
    
    @Test
    public void assertInitInstanceIdWithEmptyParameter() {
        InstanceId actual = new InstanceId();
        assertThat(actual.getId().split("@").length, is(2));
    }
    
    @Test
    public void assertInitInstanceIdWithIpAndUniqueSign() {
        InstanceId actual = new InstanceId("127.0.0.1", 3307);
        assertThat(actual.getId(), is("127.0.0.1@3307"));
    }
    
    @Test
    public void assertInitInstanceIdWithExistId() {
        InstanceId actual = new InstanceId("127.0.0.1@3307");
        assertThat(actual.getIp(), is("127.0.0.1"));
        assertThat(actual.getUniqueSign(), is(3307));
    }
    
    @Test
    public void assertInitMultipleInstanceId() {
        assertFalse(new InstanceId().getId().equals(new InstanceId().getId()));
    }
}
