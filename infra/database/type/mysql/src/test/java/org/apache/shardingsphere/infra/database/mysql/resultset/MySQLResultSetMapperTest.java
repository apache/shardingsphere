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

package org.apache.shardingsphere.infra.database.mysql.resultset;

import org.apache.shardingsphere.infra.database.core.resultset.DialectResultSetMapper;
import org.apache.shardingsphere.infra.database.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MySQLResultSetMapperTest {
    
    private final DialectResultSetMapper dialectResultSetMapper = DatabaseTypedSPILoader.getService(DialectResultSetMapper.class, TypedSPILoader.getService(DatabaseType.class, "MySQL"));
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ResultSet resultSet;
    
    @Test
    void assertGetSmallintValue() throws SQLException {
        when(resultSet.getInt(Mockito.anyInt())).thenReturn(0);
        assertThat(dialectResultSetMapper.getSmallintValue(resultSet, Mockito.anyInt()), is(0));
    }
    
    @Test
    void assertGetDateValue() throws SQLException {
        when(resultSet.getMetaData().getColumnTypeName(0)).thenReturn("YEAR");
        Object expectedObject = new Object();
        when(resultSet.getObject(Mockito.anyInt())).thenReturn(expectedObject);
        assertThat(dialectResultSetMapper.getDateValue(resultSet, 0), is(expectedObject));
    }
    
    @Test
    void assertGetDateValueNoIsYearDataType() throws SQLException {
        when(resultSet.getMetaData().getColumnTypeName(0)).thenReturn("test");
        Date expectedDate = mock(Date.class);
        when(resultSet.getDate(Mockito.anyInt())).thenReturn(expectedDate);
        assertThat(dialectResultSetMapper.getDateValue(resultSet, 0), is(expectedDate));
    }
    
    @Test
    void assertGetDatabaseType() {
        assertThat(dialectResultSetMapper.getDatabaseType(), is("MySQL"));
    }
    
}
