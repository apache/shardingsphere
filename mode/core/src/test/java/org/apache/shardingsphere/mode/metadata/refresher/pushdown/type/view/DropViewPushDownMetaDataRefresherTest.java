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

package org.apache.shardingsphere.mode.metadata.refresher.pushdown.type.view;

import org.apache.shardingsphere.database.connector.core.metadata.database.enums.QuoteCharacter;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereView;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mode.metadata.refresher.pushdown.PushDownMetaDataManagerPersistServiceFixture;
import org.apache.shardingsphere.mode.metadata.refresher.pushdown.PushDownMetaDataRefresher;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.view.DropViewStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Properties;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;

class DropViewPushDownMetaDataRefresherTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    private final DropViewPushDownMetaDataRefresher refresher = (DropViewPushDownMetaDataRefresher) TypedSPILoader.getService(PushDownMetaDataRefresher.class, DropViewStatement.class);
    
    @Test
    void assertRefreshUsesActualViewNames() {
        PushDownMetaDataManagerPersistServiceFixture persistService = new PushDownMetaDataManagerPersistServiceFixture();
        DropViewStatement sqlStatement = new DropViewStatement(databaseType, Arrays.asList(
                new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("foo_view"))),
                new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("bar_view")))), false);
        refresher.refresh(persistService, createDatabase(), "logic_ds", "foo_schema", databaseType, sqlStatement, new ConfigurationProperties(new Properties()));
        assertThat(persistService.getDroppedTableSchemaName(), is("Foo_Schema"));
        assertThat(persistService.getDroppedTableNames(), contains("Foo_View", "Bar_View"));
        assertThat(persistService.getDroppedViewSchemaName(), is("Foo_Schema"));
        assertThat(persistService.getDroppedViewNames(), contains("Foo_View", "Bar_View"));
    }
    
    @Test
    void assertRefreshUsesActualQuotedViewNamesWithSensitiveProps() {
        PushDownMetaDataManagerPersistServiceFixture persistService = new PushDownMetaDataManagerPersistServiceFixture();
        DropViewStatement sqlStatement = new DropViewStatement(databaseType, Arrays.asList(
                new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("Foo_View", QuoteCharacter.QUOTE))),
                new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("Bar_View", QuoteCharacter.QUOTE)))), false);
        Properties props = new Properties();
        props.setProperty("metadata-identifier-case-sensitivity", "SENSITIVE");
        refresher.refresh(persistService, createDatabase(), "logic_ds", "Foo_Schema", databaseType, sqlStatement, new ConfigurationProperties(props));
        assertThat(persistService.getDroppedTableSchemaName(), is("Foo_Schema"));
        assertThat(persistService.getDroppedTableNames(), contains("Foo_View", "Bar_View"));
        assertThat(persistService.getDroppedViewSchemaName(), is("Foo_Schema"));
        assertThat(persistService.getDroppedViewNames(), contains("Foo_View", "Bar_View"));
    }
    
    private ShardingSphereDatabase createDatabase() {
        ShardingSphereSchema schema = new ShardingSphereSchema("Foo_Schema", databaseType, Collections.emptyList(),
                Arrays.asList(new ShardingSphereView("Foo_View", "SELECT 1"), new ShardingSphereView("Bar_View", "SELECT 1")));
        return new ShardingSphereDatabase("foo_db", databaseType, new ResourceMetaData(Collections.emptyMap()), new RuleMetaData(Collections.emptyList()), Collections.singletonList(schema),
                new ConfigurationProperties(new Properties()));
    }
}
