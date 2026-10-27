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

package org.apache.shardingsphere.database.connector.firebird.metadata.data.loader;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FirebirdSystemTableColumnSizeLoaderTest {
    
    @Mock
    private Connection connection;
    
    @Mock
    private PreparedStatement preparedStatement;
    
    @Mock
    private ResultSet resultSet;
    
    @Test
    void assertLoad() throws SQLException {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getString("TABLE_NAME")).thenReturn("RDB$RELATIONS", "RDB$DATABASE");
        when(resultSet.getString("COLUMN_NAME")).thenReturn("RDB$RELATION_NAME", "rdb$character_set_name");
        when(resultSet.getInt("CHAR_LEN")).thenReturn(63, 0);
        when(resultSet.getInt("FIELD_LENGTH")).thenReturn(31);
        Map<String, Map<String, Integer>> actual = new FirebirdSystemTableColumnSizeLoader(connection).load();
        assertThat(actual.size(), is(2));
        assertThat(actual.get("RDB$RELATIONS"), is(Collections.singletonMap("RDB$RELATION_NAME", 63)));
        assertThat(actual.get("RDB$DATABASE"), is(Collections.singletonMap("RDB$CHARACTER_SET_NAME", 31)));
        verify(resultSet).close();
        verify(preparedStatement).close();
        verify(connection, never()).close();
    }
}
