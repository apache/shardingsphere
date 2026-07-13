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

package org.apache.shardingsphere.test.e2e.operation.pipeline.framework.container.compose.natived.dialect;

import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.test.e2e.env.container.storage.option.NativeStorageContainerOption;
import org.apache.shardingsphere.test.e2e.env.container.storage.option.StorageContainerConnectOption;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class OraclePipelineNativeContainerDropTableOptionTest {
    
    @Test
    void assertGetJdbcUrl() {
        DatabaseType databaseType = mock(DatabaseType.class);
        StorageContainerConnectOption connectOption = mock(StorageContainerConnectOption.class);
        NativeStorageContainerOption nativeOption = mock(NativeStorageContainerOption.class);
        when(nativeOption.getAccessURL(connectOption, "localhost", 1521, "pipeline_e2e_0")).thenReturn("jdbc:oracle:thin:@localhost:1521:XE");
        try (
                MockedStatic<TypedSPILoader> typedSPILoader = mockStatic(TypedSPILoader.class);
                MockedStatic<DatabaseTypedSPILoader> databaseTypedSPILoader = mockStatic(DatabaseTypedSPILoader.class)) {
            typedSPILoader.when(() -> TypedSPILoader.getService(DatabaseType.class, "Oracle")).thenReturn(databaseType);
            databaseTypedSPILoader.when(() -> DatabaseTypedSPILoader.findService(NativeStorageContainerOption.class, databaseType)).thenReturn(Optional.of(nativeOption));
            assertThat(new OraclePipelineNativeContainerDropTableOption().getJdbcUrl(connectOption, 1521, "pipeline_e2e_0"), is("jdbc:oracle:thin:@localhost:1521:XE"));
        }
    }
    
    @Test
    void assertGetJdbcUrlWithoutNativeOption() {
        DatabaseType databaseType = mock(DatabaseType.class);
        StorageContainerConnectOption connectOption = mock(StorageContainerConnectOption.class);
        when(connectOption.getURL("localhost", 1521, "")).thenReturn("jdbc:oracle:thin:@localhost:1521:XE");
        try (
                MockedStatic<TypedSPILoader> typedSPILoader = mockStatic(TypedSPILoader.class);
                MockedStatic<DatabaseTypedSPILoader> databaseTypedSPILoader = mockStatic(DatabaseTypedSPILoader.class)) {
            typedSPILoader.when(() -> TypedSPILoader.getService(DatabaseType.class, "Oracle")).thenReturn(databaseType);
            databaseTypedSPILoader.when(() -> DatabaseTypedSPILoader.findService(NativeStorageContainerOption.class, databaseType)).thenReturn(Optional.empty());
            assertThat(new OraclePipelineNativeContainerDropTableOption().getJdbcUrl(connectOption, 1521, "pipeline_e2e_0"), is("jdbc:oracle:thin:@localhost:1521:XE"));
        }
    }
}
