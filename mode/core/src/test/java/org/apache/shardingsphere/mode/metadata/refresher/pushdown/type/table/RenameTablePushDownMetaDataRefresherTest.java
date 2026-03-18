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

package org.apache.shardingsphere.mode.metadata.refresher.pushdown.type.table;

import org.apache.shardingsphere.database.connector.core.metadata.database.enums.QuoteCharacter;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mode.metadata.refresher.pushdown.PushDownMetaDataManagerPersistServiceFixture;
import org.apache.shardingsphere.mode.metadata.refresher.pushdown.PushDownMetaDataRefresher;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.table.RenameTableDefinitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.table.RenameTableStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Properties;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

class RenameTablePushDownMetaDataRefresherTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    private final RenameTablePushDownMetaDataRefresher refresher = (RenameTablePushDownMetaDataRefresher) TypedSPILoader.getService(PushDownMetaDataRefresher.class, RenameTableStatement.class);
    
    @Test
    void assertRefreshRenamesTablesWithActualSourceName() {
        PushDownMetaDataManagerPersistServiceFixture persistService = new PushDownMetaDataManagerPersistServiceFixture();
        RenameTableStatement sqlStatement = new RenameTableStatement(databaseType, Collections.singleton(createRenameDefinition(
                new IdentifierValue("foo_tbl"), new IdentifierValue("bar_tbl"))));
        refresher.refresh(persistService, createDatabase(), "logic_ds", "foo_schema", databaseType, sqlStatement, new ConfigurationProperties(new Properties()));
        assertThat(persistService.getAlteredTableSchemaName(), is("Foo_Schema"));
        assertThat(persistService.getAlteredTables(), hasSize(1));
        assertThat(persistService.getAlteredTables().iterator().next().getName(), is("bar_tbl"));
        assertThat(persistService.getDroppedTableSchemaName(), is("Foo_Schema"));
        assertThat(persistService.getDroppedTableNames().iterator().next(), is("Foo_Tbl"));
    }
    
    @Test
    void assertRefreshRenamesQuotedTablesWithSensitiveProps() {
        PushDownMetaDataManagerPersistServiceFixture persistService = new PushDownMetaDataManagerPersistServiceFixture();
        RenameTableStatement sqlStatement = new RenameTableStatement(databaseType, Collections.singleton(createRenameDefinition(
                new IdentifierValue("Foo_Tbl", QuoteCharacter.QUOTE), new IdentifierValue("Bar_Tbl", QuoteCharacter.QUOTE))));
        Properties props = new Properties();
        props.setProperty("metadata-identifier-case-sensitivity", "SENSITIVE");
        refresher.refresh(persistService, createDatabase(), "logic_ds", "Foo_Schema", databaseType, sqlStatement, new ConfigurationProperties(props));
        assertThat(persistService.getAlteredTableSchemaName(), is("Foo_Schema"));
        assertThat(persistService.getAlteredTables(), hasSize(1));
        assertThat(persistService.getAlteredTables().iterator().next().getName(), is("Bar_Tbl"));
        assertThat(persistService.getDroppedTableSchemaName(), is("Foo_Schema"));
        assertThat(persistService.getDroppedTableNames().iterator().next(), is("Foo_Tbl"));
    }
    
    private RenameTableDefinitionSegment createRenameDefinition(final IdentifierValue sourceTableName, final IdentifierValue targetTableName) {
        RenameTableDefinitionSegment result = new RenameTableDefinitionSegment(0, 0);
        result.setTable(new SimpleTableSegment(new TableNameSegment(0, 0, sourceTableName)));
        result.setRenameTable(new SimpleTableSegment(new TableNameSegment(0, 0, targetTableName)));
        return result;
    }
    
    private ShardingSphereDatabase createDatabase() {
        ShardingSphereTable table = new ShardingSphereTable("Foo_Tbl", Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        ShardingSphereSchema schema = new ShardingSphereSchema("Foo_Schema", databaseType, Collections.singleton(table), Collections.emptyList());
        return new ShardingSphereDatabase("foo_db", databaseType, new ResourceMetaData(Collections.emptyMap()), new RuleMetaData(Collections.emptyList()), Collections.singleton(schema));
    }
}
