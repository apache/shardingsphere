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

package org.apache.shardingsphere.infra.database.metadata.url;

import org.apache.shardingsphere.infra.database.metadata.UnrecognizedDatabaseURLException;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class StandardJdbcUrlParserTest {
    
    @Test
    public void assertParseSimpleJdbcUrl() {
        JdbcUrl actual = new StandardJdbcUrlParser().parse("mock:jdbc://127.0.0.1/");
        assertThat(actual.getHostname(), is("127.0.0.1"));
        assertThat(actual.getPort(), is(-1));
        assertThat(actual.getDatabase(), is(""));
        assertTrue(actual.getQueryProperties().isEmpty());
    }
    
    @Test
    public void assertParseMySQLJdbcUrl() {
        JdbcUrl actual = new StandardJdbcUrlParser().parse("jdbc:mysql://127.0.0.1:3306/demo_ds?serverTimezone=UTC&useSSL=false&sessionVariables=group_concat_max_len=204800,SQL_SAFE_UPDATES=0");
        assertThat(actual.getHostname(), is("127.0.0.1"));
        assertThat(actual.getPort(), is(3306));
        assertThat(actual.getDatabase(), is("demo_ds"));
        assertThat(actual.getQueryProperties().size(), is(3));
        assertThat(actual.getQueryProperties().get("serverTimezone"), is("UTC"));
        assertThat(actual.getQueryProperties().get("useSSL"), is(Boolean.FALSE.toString()));
        assertThat(actual.getQueryProperties().get("sessionVariables"), is("group_concat_max_len=204800,SQL_SAFE_UPDATES=0"));
    }
    
    @Test
    public void assertParseMySQLJdbcUrlWithReplication() {
        JdbcUrl actual = new StandardJdbcUrlParser().parse("jdbc:mysql:replication://master-ip:3306,slave-1-ip:3306,slave-2-ip:3306/demo_ds?useUnicode=true");
        assertThat(actual.getHostname(), is("master-ip"));
        assertThat(actual.getPort(), is(3306));
        assertThat(actual.getDatabase(), is("demo_ds"));
        assertThat(actual.getQueryProperties().size(), is(1));
        assertThat(actual.getQueryProperties().get("useUnicode"), is(Boolean.TRUE.toString()));
    }
    
    @Test
    public void assertParsePostgreSQLJdbcUrl() {
        JdbcUrl actual = new StandardJdbcUrlParser().parse("jdbc:postgresql://127.0.0.1:5432/demo_ds?prepareThreshold=1&preferQueryMode=extendedForPrepared");
        assertThat(actual.getHostname(), is("127.0.0.1"));
        assertThat(actual.getPort(), is(5432));
        assertThat(actual.getDatabase(), is("demo_ds"));
        assertThat(actual.getQueryProperties().size(), is(2));
        assertThat(actual.getQueryProperties().get("prepareThreshold"), is("1"));
        assertThat(actual.getQueryProperties().get("preferQueryMode"), is("extendedForPrepared"));
    }
    
    @Test
    public void assertParseMicrosoftSQLServerJdbcUrl() {
        JdbcUrl actual = new StandardJdbcUrlParser().parse("jdbc:microsoft:sqlserver://127.0.0.1:3306/demo_ds");
        assertThat(actual.getHostname(), is("127.0.0.1"));
        assertThat(actual.getPort(), is(3306));
        assertThat(actual.getDatabase(), is("demo_ds"));
        assertTrue(actual.getQueryProperties().isEmpty());
    }
    
    @Test(expected = UnrecognizedDatabaseURLException.class)
    public void assertParseIncorrectURL() {
        new StandardJdbcUrlParser().parse("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MySQL");
    }
}
