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

package org.apache.shardingsphere.distsql.handler.executor.ral.plugin;

import org.apache.shardingsphere.database.connector.core.spi.DatabaseSupportedTypedSPI;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.spi.annotation.SPIDescription;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPI;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(ShardingSphereServiceLoader.class)
public final class PluginMetaDataQueryResultRowTest {
    
    @Test
    void assertToLocalDataQueryResultRowWithTypedSPI() {
        TypedSPI plugin = mock(TypedSPI.class);
        when(plugin.getType()).thenReturn("regular_type");
        when(plugin.getTypeAliases()).thenReturn(Arrays.asList("foo_alias", "bar_alias"));
        LocalDataQueryResultRow actual = new PluginMetaDataQueryResultRow(plugin).toLocalDataQueryResultRow();
        assertThat(actual.getCell(1), is("regular_type"));
        assertThat(actual.getCell(2), is("foo_alias,bar_alias"));
        assertThat(actual.getCell(3), is(""));
        assertThrows(IllegalArgumentException.class, () -> actual.getCell(4));
    }
    
    @Test
    void assertToLocalDataQueryResultRowWithSupportedDatabaseTypes() {
        LocalDataQueryResultRow actual = new PluginMetaDataQueryResultRow(new DescribedDatabaseSupportedTypedSPI()).toLocalDataQueryResultRow();
        assertThat(actual.getCell(1), is("database_aware_type"));
        assertThat(actual.getCell(2), is("foo_alias,bar_alias"));
        assertThat(actual.getCell(3), is("foo_db,bar_db"));
        assertThat(actual.getCell(4), is("database aware description"));
        assertThrows(IllegalArgumentException.class, () -> actual.getCell(5));
    }
    
    @Test
    void assertToLocalDataQueryResultRowWithLoadedSupportedDatabaseTypes() {
        DatabaseSupportedTypedSPI plugin = mock(DatabaseSupportedTypedSPI.class);
        when(plugin.getType()).thenReturn("loaded_type");
        when(plugin.getTypeAliases()).thenReturn(Collections.singleton("loaded_alias"));
        when(plugin.getSupportedDatabaseTypes()).thenReturn(Collections.emptyList());
        Collection<DatabaseType> databaseTypes = Arrays.asList(createDatabaseType("foo_db"), createDatabaseType("bar_db"));
        when(ShardingSphereServiceLoader.getServiceInstances(DatabaseType.class)).thenReturn(databaseTypes);
        LocalDataQueryResultRow actual = new PluginMetaDataQueryResultRow(plugin).toLocalDataQueryResultRow();
        assertThat(actual.getCell(1), is("loaded_type"));
        assertThat(actual.getCell(2), is("loaded_alias"));
        assertThat(actual.getCell(3), is("foo_db,bar_db"));
        assertThat(actual.getCell(4), is(""));
        assertThrows(IllegalArgumentException.class, () -> actual.getCell(5));
    }
    
    private DatabaseType createDatabaseType(final String type) {
        DatabaseType result = mock(DatabaseType.class);
        when(result.getType()).thenReturn(type);
        return result;
    }
    
    @SPIDescription("database aware description")
    private class DescribedDatabaseSupportedTypedSPI implements DatabaseSupportedTypedSPI {
        
        @Override
        public String getType() {
            return "database_aware_type";
        }
        
        @Override
        public Collection<Object> getTypeAliases() {
            return Arrays.asList("foo_alias", "bar_alias");
        }
        
        @Override
        public Collection<DatabaseType> getSupportedDatabaseTypes() {
            return Arrays.asList(createDatabaseType("foo_db"), createDatabaseType("bar_db"));
        }
    }
}
