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
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class InstanceMetaDataFactoryTest {
    
    @Test
    void assertCreateJDBCInstanceMetaDataWithInstanceId() {
        InstanceMetaData actual = InstanceMetaDataFactory.create("foo_id", InstanceType.JDBC, "", "foo_version");
        assertThat(actual.getId(), is("foo_id"));
        assertNotNull(actual.getIp());
        assertThat(actual.getAttributes(), is(""));
        assertThat(actual.getVersion(), is("foo_version"));
        assertThat(actual.getType(), is(InstanceType.JDBC));
    }
    
    @Test
    void assertCreateProxyInstanceMetaDataWithInstanceId() {
        ProxyInstanceMetaData actual = (ProxyInstanceMetaData) InstanceMetaDataFactory.create("foo_id", InstanceType.PROXY, "127.0.0.1@3307", "foo_version");
        assertThat(actual.getId(), is("foo_id"));
        assertThat(actual.getIp(), is("127.0.0.1"));
        assertThat(actual.getPort(), is(3307));
        assertThat(actual.getAttributes(), is("127.0.0.1@3307"));
        assertThat(actual.getVersion(), is("foo_version"));
        assertThat(actual.getType(), is(InstanceType.PROXY));
    }
}
