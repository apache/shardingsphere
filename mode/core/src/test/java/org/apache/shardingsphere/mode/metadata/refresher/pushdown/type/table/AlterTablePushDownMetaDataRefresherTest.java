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

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mode.metadata.refresher.pushdown.PushDownMetaDataManagerPersistServiceFixture;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.table.AlterTableStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AlterTablePushDownMetaDataRefresherTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    @Test
    void assertRefreshRenameTableUsesActualDroppedName() throws SQLException {
        AtomicReference<String> loadedTableName = new AtomicReference<>();
        PushDownMetaDataManagerPersistServiceFixture persistService = new PushDownMetaDataManagerPersistServiceFixture();
        AlterTablePushDownMetaDataRefresher refresher = new AlterTablePushDownMetaDataRefresher((database, logicDataSourceName, schemaName, tableName, props) -> {
            loadedTableName.set(tableName);
            return new ShardingSphereTable("Foo_New_Tbl", Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        });
        AlterTableStatement sqlStatement = AlterTableStatement.builder().databaseType(databaseType)
                .table(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("foo_old_tbl"))))
                .renameTable(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("foo_new_tbl")))).build();
        ShardingSphereDatabase database = createDatabase();
        refresher.refresh(persistService, database, "logic_ds", "Foo_Schema", databaseType, sqlStatement, new ConfigurationProperties(new Properties()));
        assertThat(loadedTableName.get(), is("foo_new_tbl"));
        assertThat(persistService.getAlteredTableSchemaName(), is("Foo_Schema"));
        assertThat(persistService.getAlteredTables().iterator().next().getName(), is("Foo_New_Tbl"));
        assertThat(persistService.getDroppedTableNames(), contains("Foo_Old_Tbl"));
    }
    
    @Test
    void assertRefreshAlterTableWithoutRenameUsesLoadedTable() throws SQLException {
        AtomicReference<String> loadedTableName = new AtomicReference<>();
        PushDownMetaDataManagerPersistServiceFixture persistService = new PushDownMetaDataManagerPersistServiceFixture();
        AlterTablePushDownMetaDataRefresher refresher = new AlterTablePushDownMetaDataRefresher((database, logicDataSourceName, schemaName, tableName, props) -> {
            loadedTableName.set(tableName);
            return new ShardingSphereTable("Foo_Old_Tbl", Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        });
        AlterTableStatement sqlStatement = AlterTableStatement.builder().databaseType(databaseType)
                .table(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("foo_old_tbl")))).build();
        refresher.refresh(persistService, createDatabase(), "logic_ds", "Foo_Schema", databaseType, sqlStatement, new ConfigurationProperties(new Properties()));
        assertThat(loadedTableName.get(), is("foo_old_tbl"));
        assertThat(persistService.getAlteredTables().iterator().next().getName(), is("Foo_Old_Tbl"));
        assertTrue(persistService.getDroppedTableNames().isEmpty());
    }
    
    private ShardingSphereDatabase createDatabase() {
        ShardingSphereSchema schema = new ShardingSphereSchema("Foo_Schema", databaseType,
                Collections.singleton(new ShardingSphereTable("Foo_Old_Tbl", Collections.emptyList(), Collections.emptyList(), Collections.emptyList())),
                Collections.emptyList());
        return new ShardingSphereDatabase("foo_db", databaseType, new ResourceMetaData(Collections.emptyMap()),
                new RuleMetaData(Collections.emptyList()), Collections.singleton(schema));
    }
}
