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

package org.apache.shardingsphere.mode.metadata.refresher.pushdown.type.index;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereIndex;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mode.metadata.refresher.pushdown.PushDownMetaDataRefresher;
import org.apache.shardingsphere.mode.persist.service.MetaDataManagerPersistService;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.index.IndexNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.index.IndexSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.index.CreateIndexStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collection;
import java.util.Collections;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class CreateIndexPushDownMetaDataRefresherTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    private final CreateIndexPushDownMetaDataRefresher refresher = (CreateIndexPushDownMetaDataRefresher) TypedSPILoader.getService(PushDownMetaDataRefresher.class, CreateIndexStatement.class);
    
    @Mock
    private MetaDataManagerPersistService metaDataManagerPersistService;
    
    @SuppressWarnings("unchecked")
    @Test
    void assertRefreshCreateIndex() {
        ShardingSphereSchema schema = new ShardingSphereSchema(
                "foo_schema", Collections.singleton(new ShardingSphereTable("foo_tbl", Collections.emptyList(), Collections.emptyList(), Collections.emptyList())), Collections.emptyList(),
                databaseType);
        ShardingSphereDatabase database = createDatabase(Collections.singleton(schema));
        refresher.refresh(metaDataManagerPersistService, database, "logic_ds", "foo_schema", databaseType, createCreateIndexStatement(), new ConfigurationProperties(new Properties()));
        ArgumentCaptor<Collection<ShardingSphereTable>> alteredTablesCaptor = ArgumentCaptor.forClass(Collection.class);
        verify(metaDataManagerPersistService).alterTables(eq(database), eq("foo_schema"), alteredTablesCaptor.capture());
        ShardingSphereTable actualTable = alteredTablesCaptor.getValue().iterator().next();
        assertTrue(actualTable.containsIndex("idx_foo"));
        ShardingSphereIndex actualIndex = actualTable.getAllIndexes().iterator().next();
        assertThat(actualIndex.getColumns().iterator().next(), is("order_id"));
    }
    
    private CreateIndexStatement createCreateIndexStatement() {
        CreateIndexStatement result = new CreateIndexStatement(databaseType);
        result.setTable(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("foo_tbl"))));
        result.setIndex(new IndexSegment(0, 0, new IndexNameSegment(0, 0, new IdentifierValue("idx_foo"))));
        result.getColumns().add(new ColumnSegment(0, 0, new IdentifierValue("order_id")));
        return result;
    }
    
    @Test
    void assertRefreshThrowsWhenIndexCountInvalid() {
        CreateIndexStatement sqlStatement = new CreateIndexStatement(databaseType);
        assertThrows(IllegalArgumentException.class, () -> refresher.refresh(metaDataManagerPersistService,
                createDatabase(Collections.emptyList()), "logic_ds", "foo_schema", databaseType, sqlStatement, new ConfigurationProperties(new Properties())));
        verifyNoInteractions(metaDataManagerPersistService);
    }
    
    private ShardingSphereDatabase createDatabase(final Collection<ShardingSphereSchema> schemas) {
        return new ShardingSphereDatabase("foo_db", databaseType, new ResourceMetaData(Collections.emptyMap()), new RuleMetaData(Collections.emptyList()), schemas);
    }
}
