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

package org.apache.shardingsphere.database.connector.core.metadata.database.datatype;

import com.cedarsoftware.util.CaseInsensitiveMap;
import lombok.SneakyThrows;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.DialectDatabaseMetaData;
import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.exception.external.sql.type.wrapper.SQLWrapperException;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.internal.configuration.plugins.Plugins;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class DataTypeRegistryTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    @AfterEach
    void cleanUp() {
        getDataTypes().clear();
    }
    
    @SuppressWarnings("unchecked")
    @SneakyThrows(ReflectiveOperationException.class)
    private Map<String, Map<String, Integer>> getDataTypes() {
        return (Map<String, Map<String, Integer>>) Plugins.getMemberAccessor().get(DataTypeRegistry.class.getDeclaredField("DATA_TYPES"), null);
    }
    
    @Test
    void assertLoad() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getString("TYPE_NAME")).thenReturn("int", "varchar");
        when(resultSet.getInt("DATA_TYPE")).thenReturn(Types.INTEGER, Types.VARCHAR);
        DataSource dataSource = mock(DataSource.class, RETURNS_DEEP_STUBS);
        when(dataSource.getConnection().getMetaData().getTypeInfo()).thenReturn(resultSet);
        DialectDatabaseMetaData dialectDatabaseMetaData = mock(DialectDatabaseMetaData.class, RETURNS_DEEP_STUBS);
        when(dialectDatabaseMetaData.getDataTypeOption().getExtraDataTypes()).thenReturn(Collections.singletonMap("EXTRA_TYPE", Types.OTHER));
        try (MockedStatic<DatabaseTypedSPILoader> databaseTypedSPILoader = mockStatic(DatabaseTypedSPILoader.class)) {
            databaseTypedSPILoader.when(() -> DatabaseTypedSPILoader.getService(DialectDatabaseMetaData.class, databaseType)).thenReturn(dialectDatabaseMetaData);
            try (MockedStatic<TypedSPILoader> typedSPILoader = mockStatic(TypedSPILoader.class)) {
                typedSPILoader.when(() -> TypedSPILoader.getService(DatabaseType.class, "FIXTURE")).thenReturn(databaseType);
                typedSPILoader.when(() -> TypedSPILoader.getService(DialectDatabaseMetaData.class, null)).thenReturn(dialectDatabaseMetaData);
                DataTypeRegistry.load(dataSource, "FIXTURE");
                assertThat(DataTypeRegistry.getDataType("FIXTURE", "int"), is(Optional.of(Types.INTEGER)));
                assertThat(DataTypeRegistry.getDataType("FIXTURE", "extra_type"), is(Optional.of(Types.OTHER)));
                DataTypeRegistry.load(mock(DataSource.class, RETURNS_DEEP_STUBS), "FIXTURE");
                DataSource brokenDataSource = mock(DataSource.class);
                when(brokenDataSource.getConnection()).thenThrow(SQLException.class);
                assertThrows(SQLWrapperException.class, () -> DataTypeRegistry.load(brokenDataSource, "broken"));
            }
        }
    }
    
    @Test
    void assertGetDataTypeWhenDatabaseMissing() {
        assertFalse(DataTypeRegistry.getDataType("unknown", "int").isPresent());
    }
    
    @Test
    void assertGetDataType() {
        Map<String, Map<String, Integer>> dataTypes = getDataTypes();
        dataTypes.put("test_db", new CaseInsensitiveMap<>(Collections.singletonMap("varchar", Types.VARCHAR)));
        assertThat(DataTypeRegistry.getDataType("test_db", "varchar"), is(Optional.of(Types.VARCHAR)));
        assertFalse(DataTypeRegistry.getDataType("test_db", "missing").isPresent());
    }
}
