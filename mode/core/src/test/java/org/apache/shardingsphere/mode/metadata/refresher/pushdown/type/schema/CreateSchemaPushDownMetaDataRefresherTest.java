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

package org.apache.shardingsphere.mode.metadata.refresher.pushdown.type.schema;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mode.metadata.refresher.pushdown.PushDownMetaDataRefresher;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.schema.CreateSchemaStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Properties;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class CreateSchemaPushDownMetaDataRefresherTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    private final CreateSchemaPushDownMetaDataRefresher refresher = (CreateSchemaPushDownMetaDataRefresher) TypedSPILoader.getService(PushDownMetaDataRefresher.class, CreateSchemaStatement.class);
    
    @Test
    void assertRefreshCreateSchemaWithSchemaName() {
        SchemaMetaDataManagerPersistServiceFixture persistService = new SchemaMetaDataManagerPersistServiceFixture();
        CreateSchemaStatement sqlStatement = new CreateSchemaStatement(databaseType);
        sqlStatement.setSchemaName(new IdentifierValue("FOO_SCHEMA"));
        refresher.refresh(persistService, createDatabase(), "logic_ds", "foo_schema", databaseType, sqlStatement, new ConfigurationProperties(new Properties()));
        assertThat(persistService.getCreatedSchemaName(), is("foo_schema"));
    }
    
    @Test
    void assertRefreshCreateSchemaWithSensitiveProps() {
        SchemaMetaDataManagerPersistServiceFixture persistService = new SchemaMetaDataManagerPersistServiceFixture();
        CreateSchemaStatement sqlStatement = new CreateSchemaStatement(databaseType);
        sqlStatement.setSchemaName(new IdentifierValue("FOO_SCHEMA"));
        Properties props = new Properties();
        props.setProperty("metadata-identifier-case-sensitivity", "SENSITIVE");
        refresher.refresh(persistService, createDatabase(), "logic_ds", "foo_schema", databaseType, sqlStatement, new ConfigurationProperties(props));
        assertThat(persistService.getCreatedSchemaName(), is("FOO_SCHEMA"));
    }
    
    @Test
    void assertRefreshNoSchemaOrUserDoesNothing() {
        SchemaMetaDataManagerPersistServiceFixture persistService = new SchemaMetaDataManagerPersistServiceFixture();
        refresher.refresh(persistService, createDatabase(), "logic_ds", "foo_schema", databaseType, new CreateSchemaStatement(databaseType), new ConfigurationProperties(new Properties()));
        assertThat(persistService.getCreatedSchemaName(), org.hamcrest.Matchers.nullValue());
    }
    
    private ShardingSphereDatabase createDatabase() {
        return new ShardingSphereDatabase("foo_db", databaseType, new ResourceMetaData(Collections.emptyMap()), new RuleMetaData(Collections.emptyList()), Collections.emptyList());
    }
}
