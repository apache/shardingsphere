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
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;

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
    void assertIsNeedRefreshWhenRefresherAbsent() {
        when(sqlStatementContext.getSqlStatement()).thenReturn(new SQLStatement(databaseType));
        when(TypedSPILoader.findService(FederationMetaDataRefresher.class, SQLStatement.class)).thenReturn(Optional.empty());
        when(TypedSPILoader.findService(FederationMetaDataRefresher.class, Object.class)).thenReturn(Optional.empty());
        assertFalse(new FederationMetaDataRefreshEngine(sqlStatementContext).isNeedRefresh());
    }
    
    @SuppressWarnings("unchecked")
    @Test
    void assertIsNeedRefreshWhenRefresherFoundFromSuperClass() {
        SQLStatement sqlStatement = mock(SQLStatement.class);
        when(sqlStatementContext.getSqlStatement()).thenReturn(sqlStatement);
        when(TypedSPILoader.findService(FederationMetaDataRefresher.class, sqlStatement.getClass())).thenReturn(Optional.empty());
        FederationMetaDataRefresher<SQLStatement> refresher = mock(FederationMetaDataRefresher.class);
        when(TypedSPILoader.findService(FederationMetaDataRefresher.class, SQLStatement.class)).thenReturn(Optional.of(refresher));
        assertTrue(new FederationMetaDataRefreshEngine(sqlStatementContext).isNeedRefresh());
    }
    
    @SuppressWarnings("unchecked")
    @Test
    void assertRefresh() {
        SQLStatement sqlStatement = new SQLStatement(databaseType);
        when(sqlStatementContext.getSqlStatement()).thenReturn(sqlStatement);
        FederationMetaDataRefresher<SQLStatement> refresher = mock(FederationMetaDataRefresher.class);
        when(TypedSPILoader.findService(FederationMetaDataRefresher.class, SQLStatement.class)).thenReturn(Optional.of(refresher));
        when(SchemaRefreshUtils.getSchemaName(database, sqlStatementContext)).thenReturn("foo_schema");
        new FederationMetaDataRefreshEngine(sqlStatementContext).refresh(metaDataManagerPersistService, database);
        verify(refresher).refresh(metaDataManagerPersistService, databaseType, database, "foo_schema", sqlStatement);
    }
}
