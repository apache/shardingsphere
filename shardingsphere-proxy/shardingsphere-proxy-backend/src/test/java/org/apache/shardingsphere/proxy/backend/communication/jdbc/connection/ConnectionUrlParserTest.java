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

package org.apache.shardingsphere.proxy.backend.communication.jdbc.connection;

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.infra.config.exception.ShardingSphereConfigurationException;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@Slf4j
public final class ConnectionUrlParserTest {
    
    private static final String MYSQL_CONNECTION_WITHOUT_PROPS = "jdbc:mysql://127.0.0.1:3306/demo_ds";
    
    private static final String MYSQL_CONNECTION_WITH_PROPS = "jdbc:mysql://127.0.0.1:3306/demo_ds?serverTimezone=UTC&useSSL=false";
    
    private static final String MYSQL_CONNECTION_WITH_REPLICATION = "jdbc:mysql:replication://master_ip:3306,slave_1_ip:3306,slave_2_ip:3306/demo_ds?useUnicode=true";
    
    private static final String POSTGRESQL_CONNECTION_WITH_PROPS = "jdbc:postgresql://127.0.0.1:5432/demo_ds?prepareThreshold=1&preferQueryMode=extendedForPrepared";
    
    private static final String MICROSOFT_SQLSERVER_CONNECTION_WITHOUT_PROPS = "jdbc:microsoft:sqlserver://127.0.0.1:3306/demo_ds";
    
    private static final String MOCK_CONNECTION_WITHOUT_PROPS = "mock:jdbc://127.0.0.1:3306/demo_ds";
    
    private static final String INCORRECT_MYSQL_CONNECTION = "jdbc:mysql://127.0.0.1:3306//demo_ds";
    
    @Test
    public void assertParseMySQLWithoutProps() {
        ConnectionUrlParser connectionUrlParser = new ConnectionUrlParser(MYSQL_CONNECTION_WITHOUT_PROPS);
        assertThat(connectionUrlParser.getScheme(), is("jdbc:mysql:"));
        assertThat(connectionUrlParser.getAuthority(), is("127.0.0.1:3306"));
        assertThat(connectionUrlParser.getPath(), is("demo_ds"));
        assertNull(connectionUrlParser.getQuery());
        assertTrue(connectionUrlParser.getQueryMap().isEmpty());
    }
    
    @Test
    public void assertParseMySQLWithProps() {
        ConnectionUrlParser connectionUrlParser = new ConnectionUrlParser(MYSQL_CONNECTION_WITH_PROPS);
        assertThat(connectionUrlParser.getScheme(), is("jdbc:mysql:"));
        assertThat(connectionUrlParser.getAuthority(), is("127.0.0.1:3306"));
        assertThat(connectionUrlParser.getPath(), is("demo_ds"));
        assertThat(connectionUrlParser.getQuery(), is("serverTimezone=UTC&useSSL=false"));
        assertThat(connectionUrlParser.getQueryMap().size(), is(2));
        assertThat(connectionUrlParser.getQueryMap().get("serverTimezone"), is("UTC"));
        assertThat(connectionUrlParser.getQueryMap().get("useSSL"), is("false"));
    }
    
    @Test
    public void assertParseMySQLWithReplication() {
        ConnectionUrlParser connectionUrlParser = new ConnectionUrlParser(MYSQL_CONNECTION_WITH_REPLICATION);
        assertThat(connectionUrlParser.getScheme(), is("jdbc:mysql:replication:"));
        assertThat(connectionUrlParser.getAuthority(), is("master_ip:3306,slave_1_ip:3306,slave_2_ip:3306"));
        assertThat(connectionUrlParser.getPath(), is("demo_ds"));
        assertThat(connectionUrlParser.getQuery(), is("useUnicode=true"));
        assertThat(connectionUrlParser.getQueryMap().size(), is(1));
        assertThat(connectionUrlParser.getQueryMap().get("useUnicode"), is("true"));
    }
    
    @Test
    public void assertParsePostgreSQLWithProps() {
        ConnectionUrlParser connectionUrlParser = new ConnectionUrlParser(POSTGRESQL_CONNECTION_WITH_PROPS);
        assertThat(connectionUrlParser.getScheme(), is("jdbc:postgresql:"));
        assertThat(connectionUrlParser.getAuthority(), is("127.0.0.1:5432"));
        assertThat(connectionUrlParser.getPath(), is("demo_ds"));
        assertThat(connectionUrlParser.getQuery(), is("prepareThreshold=1&preferQueryMode=extendedForPrepared"));
        assertThat(connectionUrlParser.getQueryMap().size(), is(2));
        assertThat(connectionUrlParser.getQueryMap().get("prepareThreshold"), is("1"));
        assertThat(connectionUrlParser.getQueryMap().get("preferQueryMode"), is("extendedForPrepared"));
    }
    
    @Test
    public void assertParseMicrosoftSQLServerWithoutProps() {
        ConnectionUrlParser connectionUrlParser = new ConnectionUrlParser(MICROSOFT_SQLSERVER_CONNECTION_WITHOUT_PROPS);
        assertThat(connectionUrlParser.getScheme(), is("jdbc:microsoft:sqlserver:"));
        assertThat(connectionUrlParser.getAuthority(), is("127.0.0.1:3306"));
        assertThat(connectionUrlParser.getPath(), is("demo_ds"));
        assertNull(connectionUrlParser.getQuery());
        assertTrue(connectionUrlParser.getQueryMap().isEmpty());
    }
    
    @Test
    public void assertParseMockSQLWithoutProps() {
        ConnectionUrlParser connectionUrlParser = new ConnectionUrlParser(MOCK_CONNECTION_WITHOUT_PROPS);
        assertThat(connectionUrlParser.getScheme(), is("mock:jdbc:"));
        assertThat(connectionUrlParser.getAuthority(), is("127.0.0.1:3306"));
        assertThat(connectionUrlParser.getPath(), is("demo_ds"));
        assertNull(connectionUrlParser.getQuery());
        assertTrue(connectionUrlParser.getQueryMap().isEmpty());
    }
    
    @Test(expected = ShardingSphereConfigurationException.class)
    public void assertParseIncorrectURL() {
        new ConnectionUrlParser(INCORRECT_MYSQL_CONNECTION);
    }
}
