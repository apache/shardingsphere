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

import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;

class DatabaseTypeRegistryTest {
    
    @Test
    void assertGetAllBranchDatabaseTypes() {
        Collection<DatabaseType> actual = new DatabaseTypeRegistry(TypedSPILoader.getService(DatabaseType.class, "TRUNK")).getAllBranchDatabaseTypes();
        assertThat(actual, is(Collections.singletonList(TypedSPILoader.getService(DatabaseType.class, "BRANCH"))));
    }
    
    @Test
    void assertGetDefaultSchemaNameWhenDatabaseTypeContainsDefaultSchema() {
        assertThat(new DatabaseTypeRegistry(TypedSPILoader.getService(DatabaseType.class, "TRUNK")).getDefaultSchemaName("FOO"), is("test"));
    }
    
    @Test
    void assertGetDefaultSchemaNameWhenDatabaseTypeNotContainsDefaultSchema() {
        assertThat(new DatabaseTypeRegistry(TypedSPILoader.getService(DatabaseType.class, "BRANCH")).getDefaultSchemaName("FOO"), is("foo"));
    }
    
    @Test
    void assertGetDefaultSchemaNameWhenDatabaseTypeNotContainsDefaultSchemaAndNullDatabaseName() {
        assertNull(new DatabaseTypeRegistry(TypedSPILoader.getService(DatabaseType.class, "BRANCH")).getDefaultSchemaName(null));
    }
}
