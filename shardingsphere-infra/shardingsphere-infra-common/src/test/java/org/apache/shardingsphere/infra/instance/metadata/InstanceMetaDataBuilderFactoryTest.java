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

package org.apache.shardingsphere.infra.instance.metadata;

import org.apache.shardingsphere.infra.instance.metadata.proxy.ProxyInstanceMetaData;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

public final class InstanceMetaDataBuilderFactoryTest {
    
    @Test
    public void assertCreateJDBCInstanceMetaDataWithoutInstanceId() {
        InstanceMetaData actual = InstanceMetaDataBuilderFactory.create("JDBC", -1);
        assertNotNull(actual.getId());
        assertNotNull(actual.getIp());
        assertThat(actual.getAttributes(), is(""));
        assertThat(actual.getType(), is(InstanceType.JDBC));
    }
    
    @Test
    public void assertCreateProxyInstanceMetaDataWithoutInstanceId() {
        ProxyInstanceMetaData actual = (ProxyInstanceMetaData) InstanceMetaDataBuilderFactory.create("Proxy", 3307);
        assertNotNull(actual.getId());
        assertNotNull(actual.getIp());
        assertThat(actual.getPort(), is(3307));
        assertThat(actual.getAttributes(), endsWith("@3307"));
        assertThat(actual.getType(), is(InstanceType.PROXY));
    }
    
    @Test
    public void assertCreateJDBCInstanceMetaDataWithInstanceId() {
        InstanceMetaData actual = InstanceMetaDataBuilderFactory.create("foo_id", InstanceType.JDBC, "");
        assertThat(actual.getId(), is("foo_id"));
        assertNotNull(actual.getIp());
        assertThat(actual.getAttributes(), is(""));
        assertThat(actual.getType(), is(InstanceType.JDBC));
    }
    
    @Test
    public void assertCreateProxyInstanceMetaDataWithInstanceId() {
        ProxyInstanceMetaData actual = (ProxyInstanceMetaData) InstanceMetaDataBuilderFactory.create("foo_id", InstanceType.PROXY, "127.0.0.1@3307");
        assertThat(actual.getId(), is("foo_id"));
        assertThat(actual.getIp(), is("127.0.0.1"));
        assertThat(actual.getPort(), is(3307));
        assertThat(actual.getAttributes(), is("127.0.0.1@3307"));
        assertThat(actual.getType(), is(InstanceType.PROXY));
    }
}
