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

package org.apache.shardingsphere.db.protocol.mysql.constant;

import org.apache.shardingsphere.db.protocol.CommonConstants;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class MySQLServerInfoTest {
    
    @Test
    public void assertSetServerVersion() {
        CommonConstants.PROXY_VERSION.set("5.0.0");
        MySQLServerInfo.setServerVersion("5.1.47");
        assertThat(MySQLServerInfo.getServerVersion(), is("5.1.47-ShardingSphere-Proxy 5.0.0"));
    }
    
    @Test
    public void assertSetServerVersionForNull() {
        CommonConstants.PROXY_VERSION.set("5.0.0");
        MySQLServerInfo.setServerVersion(null);
        assertThat(MySQLServerInfo.getServerVersion(), is("5.7.22-ShardingSphere-Proxy 5.0.0"));
    }
}
