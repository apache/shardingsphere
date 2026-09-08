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

package org.apache.shardingsphere.database.connector.core.metadata.identifier;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.sql.DataSource;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(IdentifierNormalizeEngine.class)
class DefaultSchemaNameResolverTest {
    
    private final DatabaseType trunkDatabaseType = TypedSPILoader.getService(DatabaseType.class, "TRUNK");
    
    private final DatabaseType branchDatabaseType = TypedSPILoader.getService(DatabaseType.class, "BRANCH");
    
    @Test
    void assertResolveProtocolWithFixedDefaultSchema() {
        assertThat(DefaultSchemaNameResolver.resolveProtocol(trunkDatabaseType, "foo_db"), is("test"));
    }
    
    @Test
    void assertResolveProtocolWithIdentifierCasePolicy() {
        IdentifierCasePolicy policy = IdentifierCasePolicyFactory.newUpperCasePolicySet().getPolicy(IdentifierScope.SCHEMA);
        when(IdentifierNormalizeEngine.resolvePolicy(branchDatabaseType, null, IdentifierScope.SCHEMA)).thenReturn(policy);
        when(IdentifierNormalizeEngine.normalize(policy, "foo_db")).thenCallRealMethod();
        assertThat(DefaultSchemaNameResolver.resolveProtocol(branchDatabaseType, "foo_db"), is("FOO_DB"));
    }
    
    @Test
    void assertResolveProtocolWithNullDatabaseName() {
        when(IdentifierNormalizeEngine.resolvePolicy(branchDatabaseType, null, IdentifierScope.SCHEMA)).thenThrow(AssertionError.class);
        assertNull(DefaultSchemaNameResolver.resolveProtocol(branchDatabaseType, null));
    }
    
    @Test
    void assertResolveStorageWithDataSourceIdentifierCasePolicy() {
        DataSource dataSource = mock(DataSource.class);
        IdentifierCasePolicy policy = IdentifierCasePolicyFactory.newLowerCasePolicySet().getPolicy(IdentifierScope.SCHEMA);
        when(IdentifierNormalizeEngine.resolvePolicy(branchDatabaseType, dataSource, IdentifierScope.SCHEMA)).thenReturn(policy);
        when(IdentifierNormalizeEngine.normalize(policy, "FOO_DB")).thenCallRealMethod();
        assertThat(DefaultSchemaNameResolver.resolveStorage(branchDatabaseType, dataSource, "FOO_DB"), is("foo_db"));
    }
}
