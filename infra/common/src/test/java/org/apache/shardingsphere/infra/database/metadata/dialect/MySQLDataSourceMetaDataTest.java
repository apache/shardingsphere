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
import static org.junit.Assert.assertNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;

public final class MySQLDataSourceMetaDataTest {
    
    @Test
    public void assertNewConstructorWithSimpleJdbcUrl() {
        MySQLDataSourceMetaData actual = new MySQLDataSourceMetaData("jdbc:mysql://127.0.0.1/foo_ds");
        assertThat(actual.getHostname(), is("127.0.0.1"));
        assertThat(actual.getPort(), is(3306));
        assertThat(actual.getCatalog(), is("foo_ds"));
        assertNull(actual.getSchema());
        assertTrue(actual.getQueryProperties().isEmpty());
    }
    
    @Test
    public void assertNewConstructorWithComplexJdbcUrl() {
        MySQLDataSourceMetaData actual = new MySQLDataSourceMetaData("jdbc:mysql:loadbalance://127.0.0.1:9999,127.0.0.2:9999/foo_ds?serverTimezone=UTC&useSSL=false");
        assertThat(actual.getHostname(), is("127.0.0.1"));
        assertThat(actual.getPort(), is(9999));
        assertThat(actual.getCatalog(), is("foo_ds"));
        assertNull(actual.getSchema());
        assertThat(actual.getQueryProperties().size(), is(2));
        assertThat(actual.getQueryProperties().getProperty("serverTimezone"), is("UTC"));
        assertThat(actual.getQueryProperties().getProperty("useSSL"), is(Boolean.FALSE.toString()));
    }
    
    @Test(expected = UnrecognizedDatabaseURLException.class)
    public void assertNewConstructorFailure() {
        new MySQLDataSourceMetaData("jdbc:mysql:xxxxxxxx");
    }
}
