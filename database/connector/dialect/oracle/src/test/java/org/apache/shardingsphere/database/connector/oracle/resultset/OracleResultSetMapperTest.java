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

package org.apache.shardingsphere.database.connector.oracle.resultset;

import org.apache.shardingsphere.database.connector.core.resultset.DialectResultSetMapper;
import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class OracleResultSetMapperTest {
    
    private static final int ORACLE_TIMESTAMP_WITH_TIME_ZONE = -101;
    
    private static final int ORACLE_TIMESTAMP_WITH_LOCAL_TIME_ZONE = -102;
    
    private final DialectResultSetMapper dialectResultSetMapper = DatabaseTypedSPILoader.getService(DialectResultSetMapper.class, TypedSPILoader.getService(DatabaseType.class, "Oracle"));
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ResultSet resultSet;
    
    @Test
    void assertGetSmallintValue() throws SQLException {
        when(resultSet.getInt(1)).thenReturn(0);
        assertThat(dialectResultSetMapper.getSmallintValue(resultSet, 1), is(0));
    }
    
    @Test
    void assertGetDateValue() throws SQLException {
        when(resultSet.getDate(1)).thenReturn(new Date(0L));
        assertThat(dialectResultSetMapper.getDateValue(resultSet, 1), is(new Date(0L)));
    }
    
    @Test
    void assertGetTimestampValueWithOracleTimestampWithTimeZone() throws SQLException {
        Timestamp expected = new Timestamp(System.currentTimeMillis());
        when(resultSet.getTimestamp(1)).thenReturn(expected);
        assertThat(dialectResultSetMapper.getTimestampValue(resultSet, 1, ORACLE_TIMESTAMP_WITH_TIME_ZONE), is(expected));
    }
    
    @Test
    void assertGetTimestampValueWithOracleTimestampWithLocalTimeZone() throws SQLException {
        Timestamp expected = new Timestamp(System.currentTimeMillis());
        when(resultSet.getTimestamp(1)).thenReturn(expected);
        assertThat(dialectResultSetMapper.getTimestampValue(resultSet, 1, ORACLE_TIMESTAMP_WITH_LOCAL_TIME_ZONE), is(expected));
    }
    
    @Test
    void assertGetTimestampValueWithStandardTimestamp() throws SQLException {
        Object expected = new Object();
        when(resultSet.getObject(1)).thenReturn(expected);
        assertThat(dialectResultSetMapper.getTimestampValue(resultSet, 1, java.sql.Types.TIMESTAMP), is(expected));
    }
}
