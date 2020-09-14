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

package org.apache.shardingsphere.infra.database.metadata.dialect;

import org.apache.shardingsphere.infra.database.metadata.UnrecognizedDatabaseURLException;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class OracleDataSourceMetaDataTest {
    
    @Test
    public void assertNewConstructorWithPort() {
        OracleDataSourceMetaData actual = new OracleDataSourceMetaData("jdbc:oracle:thin:@//127.0.0.1:9999/ds_0", "test");
        assertThat(actual.getHostName(), is("127.0.0.1"));
        assertThat(actual.getPort(), is(9999));
        assertThat(actual.getCatalog(), is("ds_0"));
        assertThat(actual.getSchema(), is("test"));
    }
    
    @Test
    public void assertNewConstructorWithDefaultPort() {
        OracleDataSourceMetaData actual = new OracleDataSourceMetaData("jdbc:oracle:oci:@127.0.0.1/ds_0", "test");
        assertThat(actual.getHostName(), is("127.0.0.1"));
        assertThat(actual.getPort(), is(1521));
        assertThat(actual.getSchema(), is("test"));
    }
    
    @Test
    public void assertNewConstructorWithConnectDescriptorUrl() {
        OracleDataSourceMetaData actual = new OracleDataSourceMetaData("jdbc:oracle:thin:@(DESCRIPTION =(ADDRESS = (PROTOCOL = TCP)(HOST = 172.16.0.12)(PORT = 1521))(ADDRESS = (PROTOCOL = TCP)"
                + "(HOST = 172.16.0.22)(PORT = 1521))(LOAD_BALANCE = yes)(FAILOVER = ON)(CONNECT_DATA =(SERVER = DEDICATED)"
                + "(SERVICE_NAME = rac)(FAILOVER_MODE=(TYPE = SELECT)(METHOD = BASIC)(RETIRES = 20)(DELAY = 15))))", "test");
        assertThat(actual.getHostName(), is("172.16.0.12"));
        assertThat(actual.getPort(), is(1521));
        assertThat(actual.getCatalog(), is("rac"));
        assertThat(actual.getSchema(), is("test"));
    }
    
    @Test(expected = UnrecognizedDatabaseURLException.class)
    public void assertNewConstructorFailure() {
        new OracleDataSourceMetaData("jdbc:oracle:xxxxxxxx", "test");
    }
}
