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

package org.apache.shardingsphere.data.pipeline.core.preparer.inventory.calculator.position.exact;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class IntegerPositionHandlerTest {
    
    private final IntegerPositionHandler handler = new IntegerPositionHandler();
    
    @Test
    void assertReadColumnValue() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.getString(1)).thenReturn("9223372036854775808");
        assertThat(handler.readColumnValue(resultSet, 1), is(new BigInteger("9223372036854775808")));
    }
    
    @Test
    void assertReadNullColumnValue() throws SQLException {
        assertNull(handler.readColumnValue(mock(ResultSet.class), 1));
    }
    
    @Test
    void assertSetPreparedStatementValue() throws SQLException {
        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        handler.setPreparedStatementValue(preparedStatement, 1, new BigInteger("9223372036854775808"));
        verify(preparedStatement).setBigDecimal(1, new BigDecimal("9223372036854775808"));
    }
}
