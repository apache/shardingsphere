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

package org.apache.shardingsphere.mode.metadata.refresher.federation;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mode.metadata.refresher.util.SchemaRefreshUtils;
import org.apache.shardingsphere.mode.persist.service.MetaDataManagerPersistService;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.view.AlterViewStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.view.CreateViewStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.view.DropViewStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.UpdateStatement;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings({TypedSPILoader.class, SchemaRefreshUtils.class})
class FederationMetaDataRefreshEngineTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    @Mock
    private MetaDataManagerPersistService metaDataManagerPersistService;
    
    @Mock
    private ShardingSphereDatabase database;
    
    @Mock
    private SQLStatementContext sqlStatementContext;
    
    @Test
    void assertIsNeedRefreshWhenStatementNotSupported() {
        when(sqlStatementContext.getSqlStatement()).thenReturn(new UpdateStatement(databaseType));
        assertFalse(new FederationMetaDataRefreshEngine(sqlStatementContext).isNeedRefresh());
    }
    
    @Test
    void assertIsNeedRefreshWhenCreateViewStatement() {
        when(sqlStatementContext.getSqlStatement()).thenReturn(new CreateViewStatement(databaseType));
        assertTrue(new FederationMetaDataRefreshEngine(sqlStatementContext).isNeedRefresh());
    }
    
    @Test
    void assertIsNeedRefreshWhenAlterViewStatement() {
        when(sqlStatementContext.getSqlStatement()).thenReturn(new AlterViewStatement(databaseType));
        assertTrue(new FederationMetaDataRefreshEngine(sqlStatementContext).isNeedRefresh());
    }
    
    @Test
    void assertIsNeedRefreshWhenDropViewStatement() {
        when(sqlStatementContext.getSqlStatement()).thenReturn(new DropViewStatement(databaseType));
        assertTrue(new FederationMetaDataRefreshEngine(sqlStatementContext).isNeedRefresh());
    }
    
    @Test
    void assertRefreshWithCreateViewStatement() {
        CreateViewStatement sqlStatement = new CreateViewStatement(databaseType);
        when(sqlStatementContext.getSqlStatement()).thenReturn(sqlStatement);
        FederationMetaDataRefresher<CreateViewStatement> refresher = mock(FederationMetaDataRefresher.class);
        when(TypedSPILoader.findService(FederationMetaDataRefresher.class, CreateViewStatement.class)).thenReturn(Optional.of(refresher));
        when(SchemaRefreshUtils.getSchemaName(database, sqlStatementContext)).thenReturn("foo_schema");
        new FederationMetaDataRefreshEngine(sqlStatementContext).refresh(metaDataManagerPersistService, database);
        verify(refresher).refresh(metaDataManagerPersistService, databaseType, database, "foo_schema", sqlStatement);
    }
    
    @Test
    void assertRefreshWithAlterViewStatement() {
        AlterViewStatement sqlStatement = new AlterViewStatement(databaseType);
        when(sqlStatementContext.getSqlStatement()).thenReturn(sqlStatement);
        FederationMetaDataRefresher<AlterViewStatement> refresher = mock(FederationMetaDataRefresher.class);
        when(TypedSPILoader.findService(FederationMetaDataRefresher.class, AlterViewStatement.class)).thenReturn(Optional.of(refresher));
        when(SchemaRefreshUtils.getSchemaName(database, sqlStatementContext)).thenReturn("bar_schema");
        new FederationMetaDataRefreshEngine(sqlStatementContext).refresh(metaDataManagerPersistService, database);
        verify(refresher).refresh(metaDataManagerPersistService, databaseType, database, "bar_schema", sqlStatement);
    }
    
    @Test
    void assertRefreshWhenRefresherNotFound() {
        when(sqlStatementContext.getSqlStatement()).thenReturn(new UpdateStatement(databaseType));
        FederationMetaDataRefreshEngine engine = new FederationMetaDataRefreshEngine(sqlStatementContext);
        engine.refresh(metaDataManagerPersistService, database);
    }
    
    @Test
    void assertRefreshMultipleTimesWithSameStatement() {
        CreateViewStatement sqlStatement = new CreateViewStatement(databaseType);
        when(sqlStatementContext.getSqlStatement()).thenReturn(sqlStatement);
        FederationMetaDataRefresher<CreateViewStatement> refresher = mock(FederationMetaDataRefresher.class);
        when(TypedSPILoader.findService(FederationMetaDataRefresher.class, CreateViewStatement.class)).thenReturn(Optional.of(refresher));
        when(SchemaRefreshUtils.getSchemaName(database, sqlStatementContext)).thenReturn("foo_schema");
        FederationMetaDataRefreshEngine engine = new FederationMetaDataRefreshEngine(sqlStatementContext);
        engine.refresh(metaDataManagerPersistService, database);
        engine.refresh(metaDataManagerPersistService, database);
        verify(refresher, Mockito.times(2)).refresh(metaDataManagerPersistService, databaseType, database, "foo_schema", sqlStatement);
    }
}
