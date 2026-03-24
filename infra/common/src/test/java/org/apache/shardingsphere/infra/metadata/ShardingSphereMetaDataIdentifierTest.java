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

package org.apache.shardingsphere.infra.metadata;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.config.props.MetadataIdentifierCaseSensitivity;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabaseFactory;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Properties;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ShardingSphereMetaDataIdentifierTest {
    
    private final DatabaseType postgreSQLDatabaseType = TypedSPILoader.getService(DatabaseType.class, "PostgreSQL");
    
    private final DatabaseType oracleDatabaseType = TypedSPILoader.getService(DatabaseType.class, "Oracle");
    
    @Test
    void assertContainsDatabaseByString() {
        assertTrue(createMetaData(postgreSQLDatabaseType, createDatabase("foo_db", postgreSQLDatabaseType)).containsDatabase("FOO_DB"));
    }
    
    @Test
    void assertContainsDatabaseWithOracleRule() {
        assertTrue(createMetaData(oracleDatabaseType, createDatabase("FOO_DB", oracleDatabaseType)).containsDatabase("foo_db"));
    }
    
    @Test
    void assertGetDatabaseByString() {
        ShardingSphereDatabase database = createDatabase("foo_db", postgreSQLDatabaseType);
        assertThat(createMetaData(postgreSQLDatabaseType, database).getDatabase("FOO_DB"), is(database));
    }
    
    @Test
    void assertGetDatabaseWithOracleRule() {
        ShardingSphereDatabase database = createDatabase("FOO_DB", oracleDatabaseType);
        assertThat(createMetaData(oracleDatabaseType, database).getDatabase("foo_db"), is(database));
    }
    
    @Test
    void assertContainsLowerCaseLogicalDatabaseWithOracleProtocol() {
        assertTrue(createMetaData(oracleDatabaseType, createDatabase("foo_db", oracleDatabaseType)).containsDatabase("FOO_DB"));
    }
    
    @Test
    void assertGetLowerCaseLogicalDatabaseWithOracleProtocol() {
        ShardingSphereDatabase database = createDatabase("foo_db", oracleDatabaseType);
        assertThat(createMetaData(oracleDatabaseType, database).getDatabase("FOO_DB"), is(database));
    }
    
    @Test
    void assertAddDatabase() {
        ShardingSphereMetaData metaData = createMetaData(postgreSQLDatabaseType);
        metaData.addDatabase("foo_db", postgreSQLDatabaseType, new ConfigurationProperties(new Properties()));
        assertTrue(metaData.containsDatabase("FOO_DB"));
    }
    
    @Test
    void assertPutDatabase() {
        ShardingSphereMetaData metaData = createMetaData(postgreSQLDatabaseType);
        ShardingSphereDatabase database = createDatabase("foo_db", postgreSQLDatabaseType);
        metaData.putDatabase(database);
        assertThat(metaData.getDatabase("FOO_DB"), is(database));
    }
    
    @Test
    void assertDropDatabase() {
        ShardingSphereMetaData metaData = createMetaData(postgreSQLDatabaseType, createDatabase("foo_db", postgreSQLDatabaseType));
        metaData.dropDatabase("FOO_DB");
        assertFalse(metaData.containsDatabase("foo_db"));
    }
    
    @Test
    void assertContainsDatabaseWithSensitiveProps() {
        Properties props = new Properties();
        props.setProperty(ConfigurationPropertyKey.METADATA_IDENTIFIER_CASE_SENSITIVITY.getKey(), MetadataIdentifierCaseSensitivity.SENSITIVE.name());
        ShardingSphereMetaData metaData =
                createMetaData(postgreSQLDatabaseType, new ConfigurationProperties(props), createDatabase("foo_db", postgreSQLDatabaseType));
        assertTrue(metaData.containsDatabase("FOO_DB"));
    }
    
    @Test
    void assertLegacyConstructorUsesInsensitiveLookup() {
        ShardingSphereMetaData metaData = new ShardingSphereMetaData(Collections.singleton(createDatabase("foo_db", postgreSQLDatabaseType)),
                new ResourceMetaData(Collections.emptyMap()), new RuleMetaData(Collections.emptyList()), new ConfigurationProperties(new Properties()));
        assertTrue(metaData.containsDatabase("FOO_DB"));
    }
    
    private ShardingSphereMetaData createMetaData(final DatabaseType databaseType, final ShardingSphereDatabase database) {
        return createMetaData(databaseType, new ConfigurationProperties(new Properties()), database);
    }
    
    private ShardingSphereMetaData createMetaData(final DatabaseType databaseType, final ConfigurationProperties props, final ShardingSphereDatabase database) {
        return new ShardingSphereMetaData(Collections.singleton(database),
                new ResourceMetaData(Collections.emptyMap()), new RuleMetaData(Collections.emptyList()), props, databaseType);
    }
    
    private ShardingSphereMetaData createMetaData(final DatabaseType databaseType) {
        return new ShardingSphereMetaData(Collections.emptyList(),
                new ResourceMetaData(Collections.emptyMap()), new RuleMetaData(Collections.emptyList()), new ConfigurationProperties(new Properties()), databaseType);
    }
    
    private ShardingSphereDatabase createDatabase(final String databaseName, final DatabaseType databaseType) {
        return ShardingSphereDatabaseFactory.create(databaseName, databaseType, new ConfigurationProperties(new Properties()));
    }
}
