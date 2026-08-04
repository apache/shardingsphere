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

package org.apache.shardingsphere.mode.metadata.refresher.pushdown;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.CommonSQLStatementContext;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierCasePolicyFactory;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnit;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.metadata.identifier.DatabaseIdentifierContext;
import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mode.metadata.refresher.util.SchemaRefreshUtils;
import org.apache.shardingsphere.mode.metadata.refresher.pushdown.type.table.DropTablePushDownMetaDataRefresher;
import org.apache.shardingsphere.mode.persist.service.MetaDataManagerPersistService;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.bound.TableSegmentBoundInfo;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.table.DropTableStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.UpdateStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;

import java.sql.SQLException;
import java.util.Collections;
import java.util.Optional;
import java.util.Properties;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings({TypedSPILoader.class, SchemaRefreshUtils.class})
class PushDownMetaDataRefreshEngineTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    @Mock
    private MetaDataManagerPersistService metaDataManagerPersistService;
    
    @Mock
    private ShardingSphereDatabase database;
    
    @Mock
    private SQLStatementContext sqlStatementContext;
    
    @Test
    void assertIsNeedRefreshWhenRefresherAbsent() {
        SQLStatement sqlStatement = UpdateStatement.builder().databaseType(databaseType).build();
        when(sqlStatementContext.getSqlStatement()).thenReturn(sqlStatement);
        when(TypedSPILoader.findService(PushDownMetaDataRefresher.class, sqlStatement.getClass())).thenReturn(Optional.empty());
        when(TypedSPILoader.findService(PushDownMetaDataRefresher.class, sqlStatement.getClass().getSuperclass())).thenReturn(Optional.empty());
        assertFalse(new PushDownMetaDataRefreshEngine(sqlStatementContext).isNeedRefresh());
    }
    
    @SuppressWarnings("unchecked")
    @Test
    void assertIsNeedRefreshWhenRefresherFound() {
        SQLStatement sqlStatement = UpdateStatement.builder().databaseType(databaseType).build();
        when(sqlStatementContext.getSqlStatement()).thenReturn(sqlStatement);
        PushDownMetaDataRefresher<SQLStatement> refresher = mock(PushDownMetaDataRefresher.class);
        when(TypedSPILoader.findService(PushDownMetaDataRefresher.class, sqlStatement.getClass())).thenReturn(Optional.of(refresher));
        assertFalse(new PushDownMetaDataRefreshEngine(sqlStatementContext).isNeedRefresh());
    }
    
    @SuppressWarnings("unchecked")
    @Test
    void assertRefreshUsesStorageUnitTypeAndLogicDataSource() throws SQLException {
        SQLStatement sqlStatement = UpdateStatement.builder().databaseType(databaseType).build();
        when(sqlStatementContext.getSqlStatement()).thenReturn(sqlStatement);
        StorageUnit storageUnit = mock(StorageUnit.class);
        DatabaseType storageType = mock(DatabaseType.class);
        when(storageUnit.getStorageType()).thenReturn(storageType);
        ResourceMetaData resourceMetaData = mock(ResourceMetaData.class);
        when(resourceMetaData.getStorageUnits()).thenReturn(Collections.singletonMap("actual_ds", storageUnit));
        when(database.getResourceMetaData()).thenReturn(resourceMetaData);
        ConfigurationProperties props = new ConfigurationProperties(new Properties());
        when(SchemaRefreshUtils.getActualSchemaName(database, sqlStatementContext)).thenReturn("foo_schema");
        PushDownMetaDataRefresher<SQLStatement> refresher = mock(PushDownMetaDataRefresher.class);
        when(TypedSPILoader.findService(PushDownMetaDataRefresher.class, sqlStatement.getClass())).thenReturn(Optional.of(refresher));
        new PushDownMetaDataRefreshEngine(sqlStatementContext).refresh(
                metaDataManagerPersistService, database, props, Collections.singleton(new RouteUnit(new RouteMapper("logic_ds", "actual_ds"), Collections.emptyList())));
        verify(refresher).refresh(metaDataManagerPersistService, database, "logic_ds", "foo_schema", storageType, sqlStatement, props);
    }
    
    @SuppressWarnings("unchecked")
    @Test
    void assertRefreshUsesStatementDatabaseTypeWhenRouteUnitsAbsent() throws SQLException {
        SQLStatement sqlStatement = UpdateStatement.builder().databaseType(databaseType).build();
        when(sqlStatementContext.getSqlStatement()).thenReturn(sqlStatement);
        ConfigurationProperties props = new ConfigurationProperties(new Properties());
        when(SchemaRefreshUtils.getActualSchemaName(database, sqlStatementContext)).thenReturn("foo_schema");
        PushDownMetaDataRefresher<SQLStatement> refresher = mock(PushDownMetaDataRefresher.class);
        when(TypedSPILoader.findService(PushDownMetaDataRefresher.class, sqlStatement.getClass())).thenReturn(Optional.of(refresher));
        new PushDownMetaDataRefreshEngine(sqlStatementContext).refresh(metaDataManagerPersistService, database, props, Collections.emptyList());
        verify(refresher).refresh(metaDataManagerPersistService, database, null, "foo_schema", databaseType, sqlStatement, props);
    }
    
    @Test
    void assertRefreshUsesActualSchemaAndTableNames() throws SQLException {
        DropTableStatement sqlStatement = createDropTableStatement();
        SQLStatementContext sqlStatementContext = new CommonSQLStatementContext(sqlStatement);
        ShardingSphereDatabase database = createDatabase();
        when(TypedSPILoader.findService(PushDownMetaDataRefresher.class, DropTableStatement.class)).thenReturn(Optional.of(new DropTablePushDownMetaDataRefresher()));
        when(SchemaRefreshUtils.getActualSchemaName(same(database), same(sqlStatementContext))).thenReturn("foo_schema");
        when(SchemaRefreshUtils.getActualSchemaName(same(database), any(IdentifierValue.class))).thenCallRealMethod();
        PushDownMetaDataManagerPersistServiceFixture persistService = new PushDownMetaDataManagerPersistServiceFixture();
        new PushDownMetaDataRefreshEngine(sqlStatementContext).refresh(persistService, database, new ConfigurationProperties(new Properties()), Collections.emptyList());
        assertThat(persistService.getDroppedTableSchemaName(), is("Foo_Schema"));
        assertThat(persistService.getDroppedTableNames(), contains("Foo_Tbl"));
    }
    
    private DropTableStatement createDropTableStatement() {
        TableNameSegment tableNameSegment = new TableNameSegment(0, 0, new IdentifierValue("foo_tbl"));
        tableNameSegment.setTableBoundInfo(new TableSegmentBoundInfo(new IdentifierValue("foo_db"), new IdentifierValue("foo_schema")));
        return new DropTableStatement(databaseType, Collections.singletonList(new SimpleTableSegment(tableNameSegment)), false, false);
    }
    
    private ShardingSphereDatabase createDatabase() {
        ShardingSphereSchema schema = new ShardingSphereSchema("Foo_Schema", databaseType,
                Collections.singletonList(new ShardingSphereTable("Foo_Tbl", Collections.emptyList(), Collections.emptyList(), Collections.emptyList())),
                Collections.emptyList());
        ShardingSphereDatabase result = mock(ShardingSphereDatabase.class);
        when(result.getIdentifierContext()).thenReturn(new DatabaseIdentifierContext(IdentifierCasePolicyFactory.newInsensitivePolicySet()));
        when(result.getAllSchemas()).thenReturn(Collections.singleton(schema));
        when(result.getSchema("Foo_Schema")).thenReturn(schema);
        return result;
    }
}
