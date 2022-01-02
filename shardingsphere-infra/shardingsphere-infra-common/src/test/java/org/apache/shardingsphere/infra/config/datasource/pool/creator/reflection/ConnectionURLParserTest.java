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

package org.apache.shardingsphere.infra.config.datasource.pool.creator.reflection;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class ConnectionURLParserTest {
    
    @Test
    public void assertParseSimpleJdbcUrl() {
        ConnectionURLParser connectionURLParser = new ConnectionURLParser("mock:jdbc://127.0.0.1/");
        assertThat(connectionURLParser.getHostname(), is("127.0.0.1"));
        assertThat(connectionURLParser.getPort(), is(3306));
        assertThat(connectionURLParser.getDatabase(), is(""));
        assertTrue(connectionURLParser.getQueryProperties().isEmpty());
    }
    
    @Test
    public void assertParseMySQLJdbcUrl() {
        ConnectionURLParser connectionURLParser = new ConnectionURLParser("jdbc:mysql://127.0.0.1:3306/demo_ds?serverTimezone=UTC&useSSL=false");
        assertThat(connectionURLParser.getHostname(), is("127.0.0.1"));
        assertThat(connectionURLParser.getPort(), is(3306));
        assertThat(connectionURLParser.getDatabase(), is("demo_ds"));
        assertThat(connectionURLParser.getQueryProperties().size(), is(2));
        assertThat(connectionURLParser.getQueryProperties().get("serverTimezone"), is("UTC"));
        assertThat(connectionURLParser.getQueryProperties().get("useSSL"), is("false"));
    }
    
    @Test
    public void assertParseMySQLJdbcUrlWithReplication() {
        ConnectionURLParser connectionURLParser = new ConnectionURLParser("jdbc:mysql:replication://master-ip:3306,slave-1-ip:3306,slave-2-ip:3306/demo_ds?useUnicode=true");
        assertNull(connectionURLParser.getHostname());
        assertThat(connectionURLParser.getPort(), is(-1));
        assertThat(connectionURLParser.getDatabase(), is("demo_ds"));
        assertThat(connectionURLParser.getQueryProperties().size(), is(1));
        assertThat(connectionURLParser.getQueryProperties().get("useUnicode"), is("true"));
    }
    
    @Test
    public void assertParsePostgreSQLJdbcUrl() {
        ConnectionURLParser connectionURLParser = new ConnectionURLParser("jdbc:postgresql://127.0.0.1:5432/demo_ds?prepareThreshold=1&preferQueryMode=extendedForPrepared");
        assertThat(connectionURLParser.getHostname(), is("127.0.0.1"));
        assertThat(connectionURLParser.getPort(), is(5432));
        assertThat(connectionURLParser.getDatabase(), is("demo_ds"));
        assertThat(connectionURLParser.getQueryProperties().size(), is(2));
        assertThat(connectionURLParser.getQueryProperties().get("prepareThreshold"), is("1"));
        assertThat(connectionURLParser.getQueryProperties().get("preferQueryMode"), is("extendedForPrepared"));
    }
    
    @Test
    public void assertParseMicrosoftSQLServerJdbcUrl() {
        ConnectionURLParser connectionURLParser = new ConnectionURLParser("jdbc:microsoft:sqlserver://127.0.0.1:3306/demo_ds");
        assertThat(connectionURLParser.getHostname(), is("127.0.0.1"));
        assertThat(connectionURLParser.getPort(), is(3306));
        assertThat(connectionURLParser.getDatabase(), is("demo_ds"));
        assertTrue(connectionURLParser.getQueryProperties().isEmpty());
    }
    
    @Test
    public void assertParseIncorrectURL() {
        ConnectionURLParser connectionURLParser = new ConnectionURLParser("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MySQL");
        assertThat(connectionURLParser.getHostname(), is(""));
        assertThat(connectionURLParser.getPort(), is(3306));
        assertThat(connectionURLParser.getDatabase(), is(""));
        assertTrue(connectionURLParser.getQueryProperties().isEmpty());
    }
    
    @Test
    public void assertAppendQueryPropertiesWithoutOriginalQueryProperties() {
        ConnectionURLParser connectionURLParser = new ConnectionURLParser("jdbc:mysql://192.168.0.1:3306/demo_ds");
        String actual = connectionURLParser.appendQueryProperties(ImmutableMap.<String, String>builder().put("rewriteBatchedStatements", "true").build());
        assertThat(actual, is("jdbc:mysql://192.168.0.1:3306/demo_ds?rewriteBatchedStatements=true"));
    }
    
    @Test
    public void assertAppendQueryPropertiesWithOriginalQueryProperties() {
        ConnectionURLParser connectionURLParser = new ConnectionURLParser("jdbc:mysql://192.168.0.1:3306/demo_ds?serverTimezone=UTC&useSSL=false");
        String actual = connectionURLParser.appendQueryProperties(ImmutableMap.<String, String>builder().put("rewriteBatchedStatements", "true").build());
        assertThat(actual, is("jdbc:mysql://192.168.0.1:3306/demo_ds?serverTimezone=UTC&useSSL=false&rewriteBatchedStatements=true"));
    }
}
