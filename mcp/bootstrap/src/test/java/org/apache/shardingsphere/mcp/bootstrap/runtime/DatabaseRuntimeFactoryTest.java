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

package org.apache.shardingsphere.mcp.bootstrap.runtime;

import org.apache.shardingsphere.infra.util.props.PropertiesBuilder;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder.Property;
import org.apache.shardingsphere.mcp.bootstrap.runtime.DatabaseRuntimeFactory.DatabaseConnectionConfiguration;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Properties;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DatabaseRuntimeFactoryTest {
    
    @Test
    void assertCreateConnectionConfigurations() {
        DatabaseRuntimeFactory databaseRuntimeFactory = new DatabaseRuntimeFactory();
        
        Map<String, DatabaseConnectionConfiguration> actual = databaseRuntimeFactory.createConnectionConfigurations(PropertiesBuilder.build(
                new Property("databaseName", "logic_db"),
                new Property("databaseType", "H2"),
                new Property("jdbcUrl", "jdbc:h2:mem:test")));
        
        assertThat(actual.size(), is(1));
        assertThat(actual.get("logic_db").getDatabaseType(), is("H2"));
        assertThat(actual.get("logic_db").getJdbcUrl(), is("jdbc:h2:mem:test"));
    }
    
    @Test
    void assertCreateConnectionConfigurationsWithLegacyDatabaseNames() {
        DatabaseRuntimeFactory databaseRuntimeFactory = new DatabaseRuntimeFactory();
        
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class, () -> databaseRuntimeFactory.createConnectionConfigurations(
                PropertiesBuilder.build(new Property("databaseNames", "logic_db"))));
        
        assertThat(actual.getMessage(),
                is("Runtime property `databaseNames` is no longer supported. Configure a single database with `databaseName`, `databaseType`, and `jdbcUrl`."));
    }
    
    @Test
    void assertCreateConnectionConfigurationsWithLegacyDatabaseEntries() {
        DatabaseRuntimeFactory databaseRuntimeFactory = new DatabaseRuntimeFactory();
        Properties props = PropertiesBuilder.build(
                new Property("databaseName", "logic_db"),
                new Property("databaseType", "H2"),
                new Property("jdbcUrl", "jdbc:h2:mem:test"),
                new Property("databases.logic_db.jdbcUrl", "jdbc:h2:mem:legacy"));
        
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class, () -> databaseRuntimeFactory.createConnectionConfigurations(props));
        
        assertThat(actual.getMessage(),
                is("Runtime properties with `databases.<name>.*` are no longer supported. Configure a single database with `databaseName`, `databaseType`, and `jdbcUrl`."));
    }
}
