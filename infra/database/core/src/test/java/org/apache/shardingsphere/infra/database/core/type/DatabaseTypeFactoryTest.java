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

package org.apache.shardingsphere.infra.database.core.type;

import org.apache.shardingsphere.infra.util.spi.type.typed.TypedSPILoader;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DatabaseTypeFactoryTest {
    
    @Test
    void assertGetDatabaseTypeWithTrunkURL() {
        assertThat(DatabaseTypeFactory.get("jdbc:trunk://localhost:3306/test").getType(), is("TRUNK"));
    }
    
    @Test
    void assertGetDatabaseTypeWithBranchURL() {
        assertThat(DatabaseTypeFactory.get("jdbc:trunk:branch://localhost:3306/test").getType(), is("BRANCH"));
    }
    
    @Test
    void assertGetDatabaseTypeWithUnrecognizedURL() {
        assertThrows(UnsupportedStorageTypeException.class, () -> DatabaseTypeFactory.get("jdbc:not-existed:test"));
    }
    
    @Test
    void assertGetAllBranchDatabaseTypes() {
        Collection<DatabaseType> actual = DatabaseTypeFactory.getAllBranchDatabaseTypes(TypedSPILoader.getService(DatabaseType.class, "TRUNK"));
        assertThat(actual, is(Collections.singletonList(TypedSPILoader.getService(DatabaseType.class, "BRANCH"))));
    }
    
    @Test
    void assertGetDefaultSchemaNameWhenDatabaseTypeContainsDefaultSchema() {
        DatabaseType schemaNoSupportDatabaseType = TypedSPILoader.getService(DatabaseType.class, "TRUNK");
        assertThat(DatabaseTypeFactory.getDefaultSchemaName(schemaNoSupportDatabaseType, "FOO"), is("test"));
    }
    
    @Test
    void assertGetDefaultSchemaNameWhenDatabaseTypeNotContainsDefaultSchema() {
        DatabaseType schemaNoSupportDatabaseType = TypedSPILoader.getService(DatabaseType.class, "BRANCH");
        assertThat(DatabaseTypeFactory.getDefaultSchemaName(schemaNoSupportDatabaseType, "FOO"), is("foo"));
    }
    
    @Test
    void assertGetDefaultSchemaNameWhenDatabaseTypeNotContainsDefaultSchemaAndNullDatabaseName() {
        DatabaseType schemaNoSupportDatabaseType = TypedSPILoader.getService(DatabaseType.class, "BRANCH");
        assertNull(DatabaseTypeFactory.getDefaultSchemaName(schemaNoSupportDatabaseType, null));
    }
}
