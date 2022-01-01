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

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class ConnectionURLParserTest {
    
    @Test
    public void assertParseMySQLWithoutProps() {
        ConnectionURLParser connectionUrlParser = new ConnectionURLParser("jdbc:mysql://127.0.0.1:3306/demo_ds");
        assertTrue(connectionUrlParser.getProperties().isEmpty());
    }
    
    @Test
    public void assertParseMySQLWithProps() {
        ConnectionURLParser connectionUrlParser = new ConnectionURLParser("jdbc:mysql://127.0.0.1:3306/demo_ds?serverTimezone=UTC&useSSL=false");
        assertThat(connectionUrlParser.getProperties().size(), is(2));
        assertThat(connectionUrlParser.getProperties().get("serverTimezone"), is("UTC"));
        assertThat(connectionUrlParser.getProperties().get("useSSL"), is("false"));
    }
    
    @Test
    public void assertParseMySQLWithReplication() {
        ConnectionURLParser connectionUrlParser = new ConnectionURLParser("jdbc:mysql:replication://master_ip:3306,slave_1_ip:3306,slave_2_ip:3306/demo_ds?useUnicode=true");
        assertThat(connectionUrlParser.getProperties().size(), is(1));
        assertThat(connectionUrlParser.getProperties().get("useUnicode"), is("true"));
    }
    
    @Test
    public void assertParsePostgreSQLWithProps() {
        ConnectionURLParser connectionUrlParser = new ConnectionURLParser("jdbc:postgresql://127.0.0.1:5432/demo_ds?prepareThreshold=1&preferQueryMode=extendedForPrepared");
        assertThat(connectionUrlParser.getProperties().size(), is(2));
        assertThat(connectionUrlParser.getProperties().get("prepareThreshold"), is("1"));
        assertThat(connectionUrlParser.getProperties().get("preferQueryMode"), is("extendedForPrepared"));
    }
    
    @Test
    public void assertParseMicrosoftSQLServerWithoutProps() {
        assertTrue(new ConnectionURLParser("jdbc:microsoft:sqlserver://127.0.0.1:3306/demo_ds").getProperties().isEmpty());
    }
    
    @Test
    public void assertParseMockSQLWithoutProps() {
        assertTrue(new ConnectionURLParser("mock:jdbc://127.0.0.1:3306/demo_ds").getProperties().isEmpty());
    }
    
    @Test
    public void assertParseIncorrectURL() {
        assertTrue(new ConnectionURLParser("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MySQL").getProperties().isEmpty());
    }
}
