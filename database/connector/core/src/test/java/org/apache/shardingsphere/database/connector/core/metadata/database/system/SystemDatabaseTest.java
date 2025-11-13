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

package org.apache.shardingsphere.database.connector.core.metadata.database.system;

import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(DatabaseTypedSPILoader.class)
class SystemDatabaseTest {
    
    @Test
    void assertGetSystemDatabases() {
        DatabaseType databaseType = mock(DatabaseType.class);
        DialectSystemDatabase dialectSystemDatabase = mock(DialectSystemDatabase.class);
        Collection<String> expected = Arrays.asList("sys_db", "meta_db");
        when(dialectSystemDatabase.getSystemDatabases()).thenReturn(expected);
        when(DatabaseTypedSPILoader.findService(DialectSystemDatabase.class, databaseType)).thenReturn(Optional.of(dialectSystemDatabase));
        SystemDatabase systemDatabase = new SystemDatabase(databaseType);
        assertThat(systemDatabase.getSystemDatabases(), is(expected));
    }
    
    @Test
    void assertGetSystemSchemasWithDatabaseName() {
        DatabaseType databaseType = mock(DatabaseType.class);
        DialectSystemDatabase dialectSystemDatabase = mock(DialectSystemDatabase.class);
        Collection<String> expected = Collections.singletonList("information_schema");
        when(dialectSystemDatabase.getSystemSchemas("postgres")).thenReturn(expected);
        when(DatabaseTypedSPILoader.findService(DialectSystemDatabase.class, databaseType)).thenReturn(Optional.of(dialectSystemDatabase));
        SystemDatabase systemDatabase = new SystemDatabase(databaseType);
        assertThat(systemDatabase.getSystemSchemas("postgres"), is(expected));
    }
    
    @Test
    void assertGetSystemSchemasWhenSpiAbsent() {
        DatabaseType databaseType = mock(DatabaseType.class);
        when(DatabaseTypedSPILoader.findService(DialectSystemDatabase.class, databaseType)).thenReturn(Optional.empty());
        SystemDatabase systemDatabase = new SystemDatabase(databaseType);
        assertTrue(systemDatabase.getSystemSchemas().isEmpty());
    }
}
