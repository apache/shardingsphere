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

package org.apache.shardingsphere.proxy.backend.state.impl;

import org.apache.shardingsphere.distsql.statement.ral.updatable.ImportMetaDataStatement;
import org.apache.shardingsphere.infra.database.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mode.exception.ShardingSphereStateException;
import org.apache.shardingsphere.proxy.backend.state.type.UnavailableProxyState;
import org.apache.shardingsphere.proxy.backend.state.DialectProxyStateSupportedSQLProvider;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dml.DMLStatement;
import org.apache.shardingsphere.test.mock.AutoMockExtension;
import org.apache.shardingsphere.test.mock.StaticMockSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(DatabaseTypedSPILoader.class)
class UnavailableProxyStateTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    @Test
    void assertExecuteWithUnsupportedSQL() {
        when(DatabaseTypedSPILoader.findService(DialectProxyStateSupportedSQLProvider.class, databaseType)).thenReturn(Optional.empty());
        assertThrows(ShardingSphereStateException.class, () -> new UnavailableProxyState().check(mock(DMLStatement.class), databaseType));
    }
    
    @Test
    void assertExecuteWithSupportedSQL() {
        when(DatabaseTypedSPILoader.findService(DialectProxyStateSupportedSQLProvider.class, databaseType)).thenReturn(Optional.empty());
        assertDoesNotThrow(() -> new UnavailableProxyState().check(mock(ImportMetaDataStatement.class), databaseType));
    }
    
    @Test
    void assertExecuteWithDialectSupportedSQL() {
        DialectProxyStateSupportedSQLProvider supportedSQLProvider = mock(DialectProxyStateSupportedSQLProvider.class);
        when(supportedSQLProvider.getSupportedSQLStatementTypesOnUnavailableState()).thenReturn(Collections.singleton(DMLStatement.class));
        when(DatabaseTypedSPILoader.findService(DialectProxyStateSupportedSQLProvider.class, databaseType)).thenReturn(Optional.of(supportedSQLProvider));
        assertDoesNotThrow(() -> new UnavailableProxyState().check(mock(DMLStatement.class), databaseType));
    }
}
