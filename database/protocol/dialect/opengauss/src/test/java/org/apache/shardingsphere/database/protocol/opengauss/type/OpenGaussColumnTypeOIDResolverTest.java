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

package org.apache.shardingsphere.database.protocol.opengauss.type;

import org.apache.shardingsphere.database.protocol.postgresql.type.ColumnTypeOIDResolver;
import org.junit.jupiter.api.Test;
import org.opengauss.core.BaseConnection;
import org.opengauss.core.Oid;
import org.opengauss.core.TypeInfo;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OpenGaussColumnTypeOIDResolverTest {
    
    private final ColumnTypeOIDResolver resolver = new OpenGaussColumnTypeOIDResolver();
    
    @Test
    void assertFindTypeOIDFromNonOpenGaussConnection() throws SQLException {
        Connection connection = mock(Connection.class);
        assertFalse(resolver.findTypeOID(connection, "record_type").isPresent());
    }
    
    @Test
    void assertFindUnspecifiedTypeOID() throws SQLException {
        BaseConnection connection = mockConnection("unknown_type", Oid.UNSPECIFIED);
        assertFalse(resolver.findTypeOID(connection, "unknown_type").isPresent());
    }
    
    @Test
    void assertFindTypeOID() throws SQLException {
        BaseConnection connection = mockConnection("record_type", 2249);
        assertThat(resolver.findTypeOID(connection, "record_type"), is(Optional.of(2249)));
    }
    
    private BaseConnection mockConnection(final String columnTypeName, final int typeOID) throws SQLException {
        TypeInfo typeInfo = mock(TypeInfo.class);
        when(typeInfo.getPGType(columnTypeName)).thenReturn(typeOID);
        BaseConnection result = mock(BaseConnection.class);
        when(result.getTypeInfo()).thenReturn(typeInfo);
        when(result.isWrapperFor(BaseConnection.class)).thenReturn(true);
        when(result.unwrap(BaseConnection.class)).thenReturn(result);
        return result;
    }
}
