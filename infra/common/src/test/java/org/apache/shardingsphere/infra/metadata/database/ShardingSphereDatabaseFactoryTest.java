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

package org.apache.shardingsphere.infra.metadata.database;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.spi.exception.ServiceProviderNotFoundException;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class ShardingSphereDatabaseFactoryTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    @Test
    void assertCreateSystemDatabase() {
        ShardingSphereDatabase actual = ShardingSphereDatabaseFactory.create("system_db", databaseType, new ConfigurationProperties(new Properties()));
        assertThat(actual.getName(), is("system_db"));
        assertThat(actual.getProtocolType(), is(databaseType));
        assertTrue(actual.getRuleMetaData().getRules().isEmpty());
    }
    
    @Test
    void assertCreateDatabaseWithComputeNodeInstanceContext() {
        assertThrows(ServiceProviderNotFoundException.class, () -> ShardingSphereDatabaseFactory.create(null, mock(), mock(), new ConfigurationProperties(new Properties()), mock()));
    }
    
    @Test
    void assertCreateDatabaseWithSchemas() {
        ShardingSphereDatabase actual = ShardingSphereDatabaseFactory.create("foo_db", databaseType, mock(), mock(), Collections.emptyList());
        assertThat(actual.getName(), is("foo_db"));
        assertThat(actual.getProtocolType(), is(databaseType));
    }
}
