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

package org.apache.shardingsphere.database.connector.presto.jdbcurl;

import org.apache.shardingsphere.database.connector.core.jdbcurl.parser.ConnectionProperties;
import org.apache.shardingsphere.database.connector.core.jdbcurl.parser.ConnectionPropertiesParser;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder.Property;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class PrestoConnectionPropertiesParserTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "Presto");
    
    private final ConnectionPropertiesParser parser = TypedSPILoader.getService(ConnectionPropertiesParser.class, databaseType);
    
    @Test
    void assertParse() {
        ConnectionProperties actual = parser.parse("jdbc:presto://localhost/foo_catalog?schema=foo_schema", "unused_user", "unused_catalog");
        assertThat(actual.getHostname(), is("localhost"));
        assertThat(actual.getPort(), is(8080));
        assertThat(actual.getCatalog(), is("foo_catalog"));
        assertThat(actual.getSchema(), is("foo_schema"));
        assertThat(actual.getQueryProperties(), is(PropertiesBuilder.build(new Property("schema", "foo_schema"))));
    }
}
