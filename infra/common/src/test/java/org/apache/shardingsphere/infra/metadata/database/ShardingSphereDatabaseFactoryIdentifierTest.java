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
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.config.props.MetadataIdentifierCaseSensitivity;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ShardingSphereDatabaseFactoryIdentifierTest {
    
    private final DatabaseType postgreSQLDatabaseType = TypedSPILoader.getService(DatabaseType.class, "PostgreSQL");
    
    @Test
    void assertCreateSystemDatabaseUsesProtocolAwareLookup() {
        ShardingSphereDatabase database = ShardingSphereDatabaseFactory.create("foo_db", postgreSQLDatabaseType, new ConfigurationProperties(new Properties()));
        assertTrue(database.containsSchema("PUBLIC"));
    }
    
    @Test
    void assertCreateSystemDatabaseUsesConfiguredSensitivity() {
        Properties props = new Properties();
        props.setProperty(ConfigurationPropertyKey.METADATA_IDENTIFIER_CASE_SENSITIVITY.getKey(), MetadataIdentifierCaseSensitivity.SENSITIVE.name());
        ShardingSphereDatabase database = ShardingSphereDatabaseFactory.create("foo_db", postgreSQLDatabaseType, new ConfigurationProperties(props));
        assertFalse(database.containsSchema("PUBLIC"));
    }
}
