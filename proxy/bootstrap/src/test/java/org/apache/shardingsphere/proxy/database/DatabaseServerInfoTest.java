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

package org.apache.shardingsphere.proxy.database;

import org.apache.shardingsphere.proxy.exception.DatabaseServerLoadingServerException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.sql.DataSource;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DatabaseServerInfoTest {
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private DataSource dataSource;
    
    @Test
    void assertNewInstanceFailure() throws SQLException {
        when(dataSource.getConnection()).thenThrow(SQLException.class);
        assertThrows(DatabaseServerLoadingServerException.class, () -> new DatabaseServerInfo(dataSource));
    }
    
    @Test
    void assertToString() throws SQLException {
        DatabaseMetaData databaseMetaData = mock(DatabaseMetaData.class);
        when(databaseMetaData.getDatabaseProductName()).thenReturn("fixtureDB");
        when(databaseMetaData.getDatabaseProductVersion()).thenReturn("1.0.0");
        when(dataSource.getConnection().getMetaData()).thenReturn(databaseMetaData);
        assertThat(new DatabaseServerInfo(dataSource).toString(), is("Database name is `fixtureDB`, version is `1.0.0`"));
    }
}
