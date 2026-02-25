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

package org.apache.shardingsphere.proxy.backend.firebird.response.header.query;

import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResultMetaData;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.proxy.backend.response.header.query.QueryHeader;
import org.apache.shardingsphere.proxy.backend.response.header.query.QueryHeaderBuilder;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.sql.Types;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FirebirdQueryHeaderBuilderTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "Firebird");
    
    private final QueryHeaderBuilder queryHeaderBuilder = DatabaseTypedSPILoader.getService(QueryHeaderBuilder.class, databaseType);
    
    @Test
    void assertBuild() throws SQLException {
        QueryResultMetaData queryResultMetaData = mock(QueryResultMetaData.class);
        when(queryResultMetaData.getColumnType(1)).thenReturn(Types.INTEGER);
        when(queryResultMetaData.getColumnTypeName(1)).thenReturn("int");
        when(queryResultMetaData.getColumnLength(1)).thenReturn(11);
        QueryHeader actual = queryHeaderBuilder.build(queryResultMetaData, null, null, "foo_label", 1);
        assertThat(actual.getSchema(), is(""));
        assertThat(actual.getTable(), is(""));
        assertThat(actual.getColumnLabel(), is("foo_label"));
        assertThat(actual.getColumnName(), is(""));
        assertThat(actual.getColumnType(), is(Types.INTEGER));
        assertThat(actual.getColumnTypeName(), is("int"));
        assertThat(actual.getColumnLength(), is(11));
        assertThat(actual.getDecimals(), is(0));
    }
}
