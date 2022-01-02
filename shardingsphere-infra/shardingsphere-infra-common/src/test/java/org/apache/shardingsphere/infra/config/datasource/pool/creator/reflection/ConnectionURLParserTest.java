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

import org.apache.shardingsphere.infra.config.datasource.JdbcUri;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class ConnectionURLParserTest {
    
    @Test
    public void assertParseMySQLWithoutQueryProperties() {
        ConnectionURLParser connectionURLParser = new ConnectionURLParser("jdbc:mysql://127.0.0.1:3306/demo_ds");
        assertTrue(connectionURLParser.getQueryProperties().isEmpty());
    }
    
    @Test
    public void assertParseMySQLWithQueryProperties() {
        ConnectionURLParser connectionURLParser = new ConnectionURLParser("jdbc:mysql://127.0.0.1:3306/demo_ds?serverTimezone=UTC&useSSL=false");
        assertThat(connectionURLParser.getQueryProperties().size(), is(2));
        assertThat(connectionURLParser.getQueryProperties().get("serverTimezone"), is("UTC"));
        assertThat(connectionURLParser.getQueryProperties().get("useSSL"), is("false"));
    }
    
    @Test
    public void assertParseMySQLWithReplication() {
        ConnectionURLParser connectionURLParser = new ConnectionURLParser("jdbc:mysql:replication://master-ip:3306,slave-1-ip:3306,slave-2-ip:3306/demo_ds?useUnicode=true");
        assertThat(connectionURLParser.getQueryProperties().size(), is(1));
        assertThat(connectionURLParser.getQueryProperties().get("useUnicode"), is("true"));
    }
    
    @Test
    public void assertParsePostgreSQLWithQueryProperties() {
        ConnectionURLParser connectionURLParser = new ConnectionURLParser("jdbc:postgresql://127.0.0.1:5432/demo_ds?prepareThreshold=1&preferQueryMode=extendedForPrepared");
        assertThat(connectionURLParser.getQueryProperties().size(), is(2));
        assertThat(connectionURLParser.getQueryProperties().get("prepareThreshold"), is("1"));
        assertThat(connectionURLParser.getQueryProperties().get("preferQueryMode"), is("extendedForPrepared"));
    }
    
    @Test
    public void assertParseMicrosoftSQLServerWithoutQueryProperties() {
        assertTrue(new JdbcUri("jdbc:microsoft:sqlserver://127.0.0.1:3306/demo_ds").getParameters().isEmpty());
    }
    
    @Test
    public void assertParseMockSQLWithoutQueryProperties() {
        assertTrue(new JdbcUri("mock:jdbc://127.0.0.1:3306/demo_ds").getParameters().isEmpty());
    }
    
    @Test
    public void assertParseIncorrectURL() {
        assertTrue(new ConnectionURLParser("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MySQL").getQueryProperties().isEmpty());
    }
}
