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
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.table.DropTableStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Properties;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;

class DropTablePushDownMetaDataRefresherTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    private final DropTablePushDownMetaDataRefresher refresher = (DropTablePushDownMetaDataRefresher) TypedSPILoader.getService(PushDownMetaDataRefresher.class, DropTableStatement.class);
    
    @Test
    void assertRefreshUsesActualTableNames() {
        PushDownMetaDataManagerPersistServiceFixture persistService = new PushDownMetaDataManagerPersistServiceFixture();
        DropTableStatement sqlStatement = new DropTableStatement(databaseType, Arrays.asList(
                new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("foo_tbl"))),
                new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("bar_tbl")))), false, false);
        refresher.refresh(persistService, createDatabase(), "logic_ds", "foo_schema", databaseType, sqlStatement, new ConfigurationProperties(new Properties()));
        assertThat(persistService.getDroppedTableSchemaName(), is("Foo_Schema"));
        assertThat(persistService.getDroppedTableNames(), contains("Foo_Tbl", "Bar_Tbl"));
    }
    
    @Test
    void assertRefreshUsesActualQuotedTableNamesWithSensitiveProps() {
        PushDownMetaDataManagerPersistServiceFixture persistService = new PushDownMetaDataManagerPersistServiceFixture();
        DropTableStatement sqlStatement = new DropTableStatement(databaseType, Arrays.asList(
                new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("Foo_Tbl", QuoteCharacter.QUOTE))),
                new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("Bar_Tbl", QuoteCharacter.QUOTE)))), false, false);
        Properties props = new Properties();
        props.setProperty("metadata-identifier-case-sensitivity", "SENSITIVE");
        refresher.refresh(persistService, createDatabase(), "logic_ds", "Foo_Schema", databaseType, sqlStatement, new ConfigurationProperties(props));
        assertThat(persistService.getDroppedTableSchemaName(), is("Foo_Schema"));
        assertThat(persistService.getDroppedTableNames(), contains("Foo_Tbl", "Bar_Tbl"));
    }
    
    private ShardingSphereDatabase createDatabase() {
        ShardingSphereSchema schema = new ShardingSphereSchema("Foo_Schema", databaseType,
                Arrays.asList(new ShardingSphereTable("Foo_Tbl", Collections.emptyList(), Collections.emptyList(), Collections.emptyList()),
                        new ShardingSphereTable("Bar_Tbl", Collections.emptyList(), Collections.emptyList(), Collections.emptyList())),
                Collections.emptyList());
        return new ShardingSphereDatabase("foo_db", databaseType, new ResourceMetaData(Collections.emptyMap()), new RuleMetaData(Collections.emptyList()), Collections.singletonList(schema));
    }
}
