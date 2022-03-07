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

package org.apache.shardingsphere.proxy.backend.response.header.query.impl;

import org.apache.shardingsphere.infra.database.type.dialect.OpenGaussDatabaseType;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResultMetaData;
import org.junit.Test;

import java.sql.SQLException;
import java.sql.Types;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class OpenGaussQueryHeaderBuilderTest {
    
    @Test
    public void assertDatabaseType() {
        assertThat(new OpenGaussQueryHeaderBuilder().getDatabaseType(), is(new OpenGaussDatabaseType().getName()));
    }
    
    @Test
    public void assertBuildOpenGaussQueryHeader() throws SQLException {
        final int columnIndex = 1;
        QueryResultMetaData queryResultMetaData = mock(QueryResultMetaData.class);
        when(queryResultMetaData.getColumnLabel(columnIndex)).thenReturn("label");
        when(queryResultMetaData.getColumnType(columnIndex)).thenReturn(Types.INTEGER);
        when(queryResultMetaData.getColumnTypeName(columnIndex)).thenReturn("int");
        when(queryResultMetaData.getColumnLength(columnIndex)).thenReturn(11);
        QueryHeader expected = new PostgreSQLQueryHeaderBuilder().doBuild(queryResultMetaData, null, null, queryResultMetaData.getColumnLabel(columnIndex), columnIndex, null);
        QueryHeader actual = new OpenGaussQueryHeaderBuilder().doBuild(queryResultMetaData, null, null, queryResultMetaData.getColumnLabel(columnIndex), columnIndex, null);
        assertThat(actual.getColumnLabel(), is(expected.getColumnLabel()));
        assertThat(actual.getColumnType(), is(expected.getColumnType()));
        assertThat(actual.getColumnTypeName(), is(expected.getColumnTypeName()));
        assertThat(actual.getColumnLength(), is(expected.getColumnLength()));
    }
}
