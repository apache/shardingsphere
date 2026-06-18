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
import org.apache.shardingsphere.mode.metadata.refresher.pushdown.PushDownMetaDataManagerPersistServiceFixture;
import org.apache.shardingsphere.mode.metadata.refresher.pushdown.PushDownMetaDataRefresher;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.index.IndexNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.index.IndexSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.OwnerSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.index.AlterIndexStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Properties;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AlterIndexPushDownMetaDataRefresherTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    private final AlterIndexPushDownMetaDataRefresher refresher =
            (AlterIndexPushDownMetaDataRefresher) TypedSPILoader.getService(PushDownMetaDataRefresher.class, AlterIndexStatement.class);
    
    @Test
    void assertRefreshReturnWhenRenameMissing() {
        AlterIndexStatement sqlStatement = new AlterIndexStatement(databaseType, new IndexSegment(0, 0, new IndexNameSegment(0, 0, new IdentifierValue("idx_old"))), null, null);
        PushDownMetaDataManagerPersistServiceFixture persistService = new PushDownMetaDataManagerPersistServiceFixture();
        refresher.refresh(persistService, createDatabase(Collections.emptyList()), "logic_ds", "Foo_Schema", databaseType, sqlStatement, new ConfigurationProperties(new Properties()));
        assertTrue(persistService.getAlteredTables().isEmpty());
    }
    
    @Test
    void assertRefreshReturnWhenIndexMissing() {
        AlterIndexStatement sqlStatement = new AlterIndexStatement(databaseType, null, new IndexSegment(0, 0, new IndexNameSegment(0, 0, new IdentifierValue("idx_new"))), null);
        PushDownMetaDataManagerPersistServiceFixture persistService = new PushDownMetaDataManagerPersistServiceFixture();
        refresher.refresh(persistService, createDatabase(Collections.emptyList()), "logic_ds", "Foo_Schema", databaseType, sqlStatement, new ConfigurationProperties(new Properties()));
        assertTrue(persistService.getAlteredTables().isEmpty());
    }
    
    @Test
    void assertRefreshRenameIndex() {
        ShardingSphereTable table = new ShardingSphereTable(
                "Foo_Tbl", Collections.emptyList(), Collections.singleton(new ShardingSphereIndex("Idx_Old", Collections.emptyList(), false)), Collections.emptyList());
        ShardingSphereSchema schema = new ShardingSphereSchema("Bar_Schema", databaseType, Collections.singleton(table), Collections.emptyList());
        ShardingSphereDatabase database = createDatabase(Collections.singleton(schema));
        PushDownMetaDataManagerPersistServiceFixture persistService = new PushDownMetaDataManagerPersistServiceFixture();
        refresher.refresh(persistService, database, "logic_ds", "foo_schema", databaseType, createAlterStatement(), new ConfigurationProperties(new Properties()));
        ShardingSphereTable actualTable = persistService.getAlteredTables().iterator().next();
        assertThat(persistService.getAlteredTableSchemaName(), is("Bar_Schema"));
        assertFalse(actualTable.containsIndex("Idx_Old"));
        assertTrue(actualTable.containsIndex("idx_new"));
    }
    
    private AlterIndexStatement createAlterStatement() {
        IndexSegment index = new IndexSegment(0, 0, new IndexNameSegment(0, 0, new IdentifierValue("idx_old")));
        index.setOwner(new OwnerSegment(0, 0, new IdentifierValue("BAR_SCHEMA")));
        return new AlterIndexStatement(databaseType, index, new IndexSegment(0, 0, new IndexNameSegment(0, 0, new IdentifierValue("idx_new"))), null);
    }
    
    private ShardingSphereDatabase createDatabase(final java.util.Collection<ShardingSphereSchema> schemas) {
        return new ShardingSphereDatabase("foo_db", databaseType, new ResourceMetaData(Collections.emptyMap()), new RuleMetaData(Collections.emptyList()), schemas,
                new ConfigurationProperties(new Properties()));
    }
}
