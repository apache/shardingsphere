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

package org.apache.shardingsphere.database.connector.core.jdbcurl.parser;

import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class ConnectionPropertiesTest {
    
    @Test
    void assertChangeCatalog() {
        Properties queryProperties = new Properties();
        ConnectionProperties actual = new ConnectionProperties("127.0.0.1", 3306, "foo_catalog", "foo_schema", queryProperties).changeCatalog("bar_catalog");
        assertThat(actual.getHostname(), is("127.0.0.1"));
        assertThat(actual.getPort(), is(3306));
        assertThat(actual.getCatalog(), is("bar_catalog"));
        assertThat(actual.getSchema(), is("foo_schema"));
        assertThat(actual.getQueryProperties(), is(queryProperties));
    }
    
    @Test
    void assertChangeCatalogWithNullValue() {
        Properties queryProperties = new Properties();
        ConnectionProperties actual = new ConnectionProperties("127.0.0.1", 3306, "foo_catalog", "foo_schema", queryProperties).changeCatalog(null);
        assertThat(actual.getHostname(), is("127.0.0.1"));
        assertThat(actual.getPort(), is(3306));
        assertThat(actual.getCatalog(), is("foo_catalog"));
        assertThat(actual.getSchema(), is("foo_schema"));
        assertThat(actual.getQueryProperties(), is(queryProperties));
    }
}
