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

package org.apache.shardingsphere.database.connector.core.resultset;

import org.junit.jupiter.api.Test;

import java.sql.ResultSet;
import java.sql.SQLException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DialectResultSetMapperTest {
    
    private final DialectResultSetMapper dialectResultSetMapper = mock(DialectResultSetMapper.class, CALLS_REAL_METHODS);
    
    @Test
    void assertGetDefaultValue() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        Object expectedValue = new Object();
        when(resultSet.getObject(1)).thenReturn(expectedValue);
        assertThat(dialectResultSetMapper.getDefaultValue(resultSet, 1, 0), is(expectedValue));
    }
}
