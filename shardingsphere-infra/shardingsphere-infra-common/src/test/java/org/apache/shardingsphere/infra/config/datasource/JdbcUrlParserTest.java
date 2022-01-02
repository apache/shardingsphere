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

package org.apache.shardingsphere.infra.config.datasource;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class JdbcUrlParserTest {
    
    @Test
    public void assertParseSimpleJdbcUrl() {
        JdbcUrlParser jdbcUrlParser = new JdbcUrlParser("mock:jdbc://127.0.0.1/");
        assertThat(jdbcUrlParser.getHostname(), is("127.0.0.1"));
        assertThat(jdbcUrlParser.getPort(), is(3306));
        assertThat(jdbcUrlParser.getDatabase(), is(""));
        assertTrue(jdbcUrlParser.getQueryProperties().isEmpty());
    }
    
    @Test
    public void assertParseMySQLJdbcUrl() {
        JdbcUrlParser jdbcUrlParser = new JdbcUrlParser("jdbc:mysql://127.0.0.1:3306/demo_ds?serverTimezone=UTC&useSSL=false");
        assertThat(jdbcUrlParser.getHostname(), is("127.0.0.1"));
        assertThat(jdbcUrlParser.getPort(), is(3306));
        assertThat(jdbcUrlParser.getDatabase(), is("demo_ds"));
        assertThat(jdbcUrlParser.getQueryProperties().size(), is(2));
        assertThat(jdbcUrlParser.getQueryProperties().get("serverTimezone"), is("UTC"));
        assertThat(jdbcUrlParser.getQueryProperties().get("useSSL"), is("false"));
    }
    
    @Test
    public void assertParseMySQLJdbcUrlWithReplication() {
        JdbcUrlParser jdbcUrlParser = new JdbcUrlParser("jdbc:mysql:replication://master-ip:3306,slave-1-ip:3306,slave-2-ip:3306/demo_ds?useUnicode=true");
        assertNull(jdbcUrlParser.getHostname());
        assertThat(jdbcUrlParser.getPort(), is(-1));
        assertThat(jdbcUrlParser.getDatabase(), is("demo_ds"));
        assertThat(jdbcUrlParser.getQueryProperties().size(), is(1));
        assertThat(jdbcUrlParser.getQueryProperties().get("useUnicode"), is("true"));
    }
    
    @Test
    public void assertParsePostgreSQLJdbcUrl() {
        JdbcUrlParser jdbcUrlParser = new JdbcUrlParser("jdbc:postgresql://127.0.0.1:5432/demo_ds?prepareThreshold=1&preferQueryMode=extendedForPrepared");
        assertThat(jdbcUrlParser.getHostname(), is("127.0.0.1"));
        assertThat(jdbcUrlParser.getPort(), is(5432));
        assertThat(jdbcUrlParser.getDatabase(), is("demo_ds"));
        assertThat(jdbcUrlParser.getQueryProperties().size(), is(2));
        assertThat(jdbcUrlParser.getQueryProperties().get("prepareThreshold"), is("1"));
        assertThat(jdbcUrlParser.getQueryProperties().get("preferQueryMode"), is("extendedForPrepared"));
    }
    
    @Test
    public void assertParseMicrosoftSQLServerJdbcUrl() {
        JdbcUrlParser jdbcUrlParser = new JdbcUrlParser("jdbc:microsoft:sqlserver://127.0.0.1:3306/demo_ds");
        assertThat(jdbcUrlParser.getHostname(), is("127.0.0.1"));
        assertThat(jdbcUrlParser.getPort(), is(3306));
        assertThat(jdbcUrlParser.getDatabase(), is("demo_ds"));
        assertTrue(jdbcUrlParser.getQueryProperties().isEmpty());
    }
    
    @Test
    public void assertParseIncorrectURL() {
        JdbcUrlParser jdbcUrlParser = new JdbcUrlParser("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MySQL");
        assertThat(jdbcUrlParser.getHostname(), is(""));
        assertThat(jdbcUrlParser.getPort(), is(3306));
        assertThat(jdbcUrlParser.getDatabase(), is(""));
        assertTrue(jdbcUrlParser.getQueryProperties().isEmpty());
    }
    
    @Test
    public void assertAppendQueryPropertiesWithoutOriginalQueryProperties() {
        JdbcUrlParser jdbcUrlParser = new JdbcUrlParser("jdbc:mysql://192.168.0.1:3306/demo_ds");
        String actual = jdbcUrlParser.appendQueryProperties(ImmutableMap.<String, String>builder().put("rewriteBatchedStatements", "true").build());
        assertThat(actual, is("jdbc:mysql://192.168.0.1:3306/demo_ds?rewriteBatchedStatements=true"));
    }
    
    @Test
    public void assertAppendQueryPropertiesWithOriginalQueryProperties() {
        JdbcUrlParser jdbcUrlParser = new JdbcUrlParser("jdbc:mysql://192.168.0.1:3306/demo_ds?serverTimezone=UTC&useSSL=false");
        String actual = jdbcUrlParser.appendQueryProperties(ImmutableMap.<String, String>builder().put("rewriteBatchedStatements", "true").build());
        assertThat(actual, is("jdbc:mysql://192.168.0.1:3306/demo_ds?serverTimezone=UTC&useSSL=false&rewriteBatchedStatements=true"));
    }
}
