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

import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.inventory.query.Range;
import org.apache.shardingsphere.data.pipeline.core.ingest.position.type.pk.UniqueKeyIngestPosition;
import org.junit.jupiter.api.Test;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BinaryPositionHandlerTest {
    
    private final BinaryPositionHandler handler = new BinaryPositionHandler();
    
    @Test
    void assertReadColumnValue() throws Exception {
        byte[] expectedValue = new byte[]{0x01, 0x02, 0x03};
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.getBytes(1)).thenReturn(expectedValue);
        byte[] actualValue = handler.readColumnValue(resultSet, 1);
        assertThat(actualValue, is(expectedValue));
    }
    
    @Test
    void assertReadNullColumnValue() throws Exception {
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.getBytes(1)).thenReturn(null);
        byte[] actualValue = handler.readColumnValue(resultSet, 1);
        assertThat(actualValue, is((byte[]) null));
    }
    
    @Test
    void assertSetPreparedStatementValue() throws Exception {
        byte[] value = new byte[]{0x01, 0x02, 0x03};
        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        handler.setPreparedStatementValue(preparedStatement, 1, value);
        verify(preparedStatement).setBytes(1, value);
    }
    
    @Test
    void assertSetNullPreparedStatementValue() throws Exception {
        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        handler.setPreparedStatementValue(preparedStatement, 1, null);
        verify(preparedStatement).setBytes(1, null);
    }
    
    @Test
    void assertCreateIngestPosition() {
        byte[] lowerBound = new byte[]{0x01, 0x02};
        byte[] upperBound = new byte[]{0x03, 0x04};
        UniqueKeyIngestPosition<byte[]> position = handler.createIngestPosition(Range.closed(lowerBound, upperBound));
        assertThat(position.getType(), is('b'));
        assertThat(position.getLowerBound(), is(lowerBound));
        assertThat(position.getUpperBound(), is(upperBound));
    }
    
    @Test
    void assertCreateIngestPositionWithNullBounds() {
        UniqueKeyIngestPosition<byte[]> position = handler.createIngestPosition(Range.closed(null, null));
        assertThat(position.getType(), is('b'));
        assertThat(position.getLowerBound(), is((byte[]) null));
        assertThat(position.getUpperBound(), is((byte[]) null));
    }
}
