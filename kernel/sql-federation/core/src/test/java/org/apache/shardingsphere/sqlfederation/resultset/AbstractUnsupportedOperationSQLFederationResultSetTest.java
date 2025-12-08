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

package org.apache.shardingsphere.sqlfederation.resultset;

import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.NClob;
import java.sql.Ref;
import java.sql.RowId;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.SQLXML;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;

class AbstractUnsupportedOperationSQLFederationResultSetTest {
    
    @Test
    void assertUpdateNullWithColumnIndexUnsupported() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class, () -> mock(OperationFixture.class, CALLS_REAL_METHODS).updateNull(1));
        assertThat(ex.getMessage(), containsString("updateNull"));
    }
    
    @Test
    void assertUpdateNullWithColumnLabelUnsupported() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class, () -> mock(OperationFixture.class, CALLS_REAL_METHODS).updateNull("c"));
        assertThat(ex.getMessage(), containsString("updateNull"));
    }
    
    @Test
    void assertUpdateBooleanWithColumnIndexUnsupported() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class, () -> mock(OperationFixture.class, CALLS_REAL_METHODS).updateBoolean(1, true));
        assertThat(ex.getMessage(), containsString("updateBoolean"));
    }
    
    @Test
    void assertUpdateBooleanWithColumnLabelUnsupported() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class, () -> mock(OperationFixture.class, CALLS_REAL_METHODS).updateBoolean("c", true));
        assertThat(ex.getMessage(), containsString("updateBoolean"));
    }
    
    @Test
    void assertUpdateByteWithColumnIndexUnsupported() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class, () -> mock(OperationFixture.class, CALLS_REAL_METHODS).updateByte(1, (byte) 1));
        assertThat(ex.getMessage(), containsString("updateByte"));
    }
    
    @Test
    void assertUpdateByteWithColumnLabelUnsupported() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class, () -> mock(OperationFixture.class, CALLS_REAL_METHODS).updateByte("c", (byte) 1));
        assertThat(ex.getMessage(), containsString("updateByte"));
    }
    
    @Test
    void assertUpdateShortWithColumnIndexUnsupported() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class, () -> mock(OperationFixture.class, CALLS_REAL_METHODS).updateShort(1, (short) 1));
        assertThat(ex.getMessage(), containsString("updateShort"));
    }
    
    @Test
    void assertUpdateShortWithColumnLabelUnsupported() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class, () -> mock(OperationFixture.class, CALLS_REAL_METHODS).updateShort("c", (short) 1));
        assertThat(ex.getMessage(), containsString("updateShort"));
    }
    
    @Test
    void assertUpdateIntWithColumnIndexUnsupported() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class, () -> mock(OperationFixture.class, CALLS_REAL_METHODS).updateInt(1, 1));
        assertThat(ex.getMessage(), containsString("updateInt"));
    }
    
    @Test
    void assertUpdateIntWithColumnLabelUnsupported() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class, () -> mock(OperationFixture.class, CALLS_REAL_METHODS).updateInt("c", 1));
        assertThat(ex.getMessage(), containsString("updateInt"));
    }
    
    @Test
    void assertUpdateLongWithColumnIndexUnsupported() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class, () -> mock(OperationFixture.class, CALLS_REAL_METHODS).updateLong(1, 1L));
        assertThat(ex.getMessage(), containsString("updateLong"));
    }
    
    @Test
    void assertUpdateLongWithColumnLabelUnsupported() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class, () -> mock(OperationFixture.class, CALLS_REAL_METHODS).updateLong("c", 1L));
        assertThat(ex.getMessage(), containsString("updateLong"));
    }
    
    @Test
    void assertUpdateFloatWithColumnIndexUnsupported() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class, () -> mock(OperationFixture.class, CALLS_REAL_METHODS).updateFloat(1, 1F));
        assertThat(ex.getMessage(), containsString("updateFloat"));
    }
    
    @Test
    void assertUpdateFloatWithColumnLabelUnsupported() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class, () -> mock(OperationFixture.class, CALLS_REAL_METHODS).updateFloat("c", 1F));
        assertThat(ex.getMessage(), containsString("updateFloat"));
    }
    
    @Test
    void assertUpdateDoubleWithColumnIndexUnsupported() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class, () -> mock(OperationFixture.class, CALLS_REAL_METHODS).updateDouble(1, 1D));
        assertThat(ex.getMessage(), containsString("updateDouble"));
    }
    
    @Test
    void assertUpdateDoubleWithColumnLabelUnsupported() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class, () -> mock(OperationFixture.class, CALLS_REAL_METHODS).updateDouble("c", 1D));
        assertThat(ex.getMessage(), containsString("updateDouble"));
    }
    
    @Test
    void assertUpdateBigDecimalWithColumnIndexUnsupported() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class, () -> mock(OperationFixture.class, CALLS_REAL_METHODS).updateBigDecimal(1, BigDecimal.ONE));
        assertThat(ex.getMessage(), containsString("updateBigDecimal"));
    }
    
    @Test
    void assertUpdateBigDecimalWithColumnLabelUnsupported() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class, () -> mock(OperationFixture.class, CALLS_REAL_METHODS).updateBigDecimal("c", BigDecimal.ONE));
        assertThat(ex.getMessage(), containsString("updateBigDecimal"));
    }
    
    @Test
    void assertUpdateStringWithColumnIndexUnsupported() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class, () -> mock(OperationFixture.class, CALLS_REAL_METHODS).updateString(1, "v"));
        assertThat(ex.getMessage(), containsString("updateString"));
    }
    
    @Test
    void assertUpdateStringWithColumnLabelUnsupported() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class, () -> mock(OperationFixture.class, CALLS_REAL_METHODS).updateString("c", "v"));
        assertThat(ex.getMessage(), containsString("updateString"));
    }
    
    @Test
    void assertUpdateNStringWithColumnIndexUnsupported() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class, () -> mock(OperationFixture.class, CALLS_REAL_METHODS).updateNString(1, "v"));
        assertThat(ex.getMessage(), containsString("updateNString"));
    }
    
    @Test
    void assertUpdateNStringWithColumnLabelUnsupported() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class, () -> mock(OperationFixture.class, CALLS_REAL_METHODS).updateNString("c", "v"));
        assertThat(ex.getMessage(), containsString("updateNString"));
    }
    
    @Test
    void assertUpdateBytesWithColumnIndexUnsupported() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class, () -> mock(OperationFixture.class, CALLS_REAL_METHODS).updateBytes(1, new byte[]{}));
        assertThat(ex.getMessage(), containsString("updateBytes"));
    }
    
    @Test
    void assertUpdateBytesWithColumnLabelUnsupported() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class, () -> mock(OperationFixture.class, CALLS_REAL_METHODS).updateBytes("c", new byte[]{}));
        assertThat(ex.getMessage(), containsString("updateBytes"));
    }
    
    @Test
    void assertUpdateDateWithColumnIndexUnsupported() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class, () -> mock(OperationFixture.class, CALLS_REAL_METHODS).updateDate(1, mock(Date.class)));
        assertThat(ex.getMessage(), containsString("updateDate"));
    }
    
    @Test
    void assertUpdateDateWithColumnLabelUnsupported() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class, () -> mock(OperationFixture.class, CALLS_REAL_METHODS).updateDate("c", mock(Date.class)));
        assertThat(ex.getMessage(), containsString("updateDate"));
    }
    
    @Test
    void assertUpdateTimeWithColumnIndexUnsupported() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class, () -> mock(OperationFixture.class, CALLS_REAL_METHODS).updateTime(1, mock(Time.class)));
        assertThat(ex.getMessage(), containsString("updateTime"));
    }
    
    @Test
    void assertUpdateTimeWithColumnLabelUnsupported() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class, () -> mock(OperationFixture.class, CALLS_REAL_METHODS).updateTime("c", mock(Time.class)));
        assertThat(ex.getMessage(), containsString("updateTime"));
    }
    
    @Test
    void assertUpdateTimestampWithColumnIndexUnsupported() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class, () -> mock(OperationFixture.class, CALLS_REAL_METHODS).updateTimestamp(1, mock(Timestamp.class)));
        assertThat(ex.getMessage(), containsString("updateTimestamp"));
    }
    
    @Test
    void assertUpdateTimestampWithColumnLabelUnsupported() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class,
                () -> mock(OperationFixture.class, CALLS_REAL_METHODS).updateTimestamp("c", mock(Timestamp.class)));
        assertThat(ex.getMessage(), containsString("updateTimestamp"));
    }
    
    @Test
    void assertUpdateAsciiStreamWithColumnIndexUnsupported() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class,
                () -> mock(OperationFixture.class, CALLS_REAL_METHODS).updateAsciiStream(1, mock(InputStream.class)));
        assertThat(ex.getMessage(), containsString("updateAsciiStream"));
    }
    
    @Test
    void assertUpdateAsciiStreamWithColumnLabelUnsupported() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class,
                () -> mock(OperationFixture.class, CALLS_REAL_METHODS).updateAsciiStream("c", mock(InputStream.class)));
        assertThat(ex.getMessage(), containsString("updateAsciiStream"));
    }
    
    @Test
    void assertUpdateAsciiStreamWithLengthByIndexUnsupported() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class,
                () -> mock(OperationFixture.class, CALLS_REAL_METHODS).updateAsciiStream(1, mock(InputStream.class), 1));
        assertThat(ex.getMessage(), containsString("updateAsciiStream"));
    }
    
    @Test
    void assertUpdateAsciiStreamWithLengthByLabelUnsupported() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class,
                () -> mock(OperationFixture.class, CALLS_REAL_METHODS).updateAsciiStream("c", mock(InputStream.class), 1));
        assertThat(ex.getMessage(), containsString("updateAsciiStream"));
    }
    
    @Test
    void assertUpdateAsciiStreamWithLongLengthByIndexUnsupported() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class,
                () -> mock(OperationFixture.class, CALLS_REAL_METHODS).updateAsciiStream(1, mock(InputStream.class), 1L));
        assertThat(ex.getMessage(), containsString("updateAsciiStream"));
    }
    
    @Test
    void assertUpdateAsciiStreamWithLongLengthByLabelUnsupported() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class,
                () -> mock(OperationFixture.class, CALLS_REAL_METHODS).updateAsciiStream("c", mock(InputStream.class), 1L));
        assertThat(ex.getMessage(), containsString("updateAsciiStream"));
    }
    
    @Test
    void assertUpdateBinaryStreamWithColumnIndexUnsupported() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class,
                () -> mock(OperationFixture.class, CALLS_REAL_METHODS).updateBinaryStream(1, mock(InputStream.class)));
        assertThat(ex.getMessage(), containsString("updateBinaryStream"));
    }
    
    @Test
    void assertUpdateBinaryStreamWithColumnLabelUnsupported() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class,
                () -> mock(OperationFixture.class, CALLS_REAL_METHODS).updateBinaryStream("c", mock(InputStream.class)));
        assertThat(ex.getMessage(), containsString("updateBinaryStream"));
    }
    
    @Test
    void assertUpdateBinaryStreamWithLengthByIndexUnsupported() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class,
                () -> mock(OperationFixture.class, CALLS_REAL_METHODS).updateBinaryStream(1, mock(InputStream.class), 1));
        assertThat(ex.getMessage(), containsString("updateBinaryStream"));
    }
    
    @Test
    void assertUpdateBinaryStreamWithLengthByLabelUnsupported() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class,
                () -> mock(OperationFixture.class, CALLS_REAL_METHODS).updateBinaryStream("c", mock(InputStream.class), 1));
        assertThat(ex.getMessage(), containsString("updateBinaryStream"));
    }
    
    @Test
    void assertUpdateBinaryStreamWithLongLengthByIndexUnsupported() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class,
                () -> mock(OperationFixture.class, CALLS_REAL_METHODS).updateBinaryStream(1, mock(InputStream.class), 1L));
        assertThat(ex.getMessage(), containsString("updateBinaryStream"));
    }
    
    @Test
    void assertUpdateBinaryStreamWithLongLengthByLabelUnsupported() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class,
                () -> mock(OperationFixture.class, CALLS_REAL_METHODS).updateBinaryStream("c", mock(InputStream.class), 1L));
        assertThat(ex.getMessage(), containsString("updateBinaryStream"));
    }
    
    @Test
    void assertUpdateCharacterStreamWithColumnIndexUnsupported() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class,
                () -> mock(OperationFixture.class, CALLS_REAL_METHODS).updateCharacterStream(1, mock(Reader.class)));
        assertThat(ex.getMessage(), containsString("updateCharacterStream"));
    }
    
    @Test
    void assertUpdateCharacterStreamWithColumnLabelUnsupported() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class,
                () -> mock(OperationFixture.class, CALLS_REAL_METHODS).updateCharacterStream("c", mock(Reader.class)));
        assertThat(ex.getMessage(), containsString("updateCharacterStream"));
    }
    
    @Test
    void assertUpdateCharacterStreamWithLengthByIndexUnsupported() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class,
                () -> mock(OperationFixture.class, CALLS_REAL_METHODS).updateCharacterStream(1, mock(Reader.class), 1));
        assertThat(ex.getMessage(), containsString("updateCharacterStream"));
    }
    
    @Test
    void assertUpdateCharacterStreamWithLengthByLabelUnsupported() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class,
                () -> mock(OperationFixture.class, CALLS_REAL_METHODS).updateCharacterStream("c", mock(Reader.class), 1));
        assertThat(ex.getMessage(), containsString("updateCharacterStream"));
    }
    
    @Test
    void assertUpdateCharacterStreamWithLongLengthByIndexUnsupported() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class,
                () -> mock(OperationFixture.class, CALLS_REAL_METHODS).updateCharacterStream(1, mock(Reader.class), 1L));
        assertThat(ex.getMessage(), containsString("updateCharacterStream"));
    }
    
    @Test
    void assertUpdateCharacterStreamWithLongLengthByLabelUnsupported() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class,
                () -> mock(OperationFixture.class, CALLS_REAL_METHODS).updateCharacterStream("c", mock(Reader.class), 1L));
        assertThat(ex.getMessage(), containsString("updateCharacterStream"));
    }
    
    @Test
    void assertUpdateNCharacterStreamWithColumnIndexUnsupported() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class, () -> mock(OperationFixture.class, CALLS_REAL_METHODS).updateNCharacterStream(1, mock(Reader.class)));
        assertThat(ex.getMessage(), containsString("updateNCharacterStream"));
    }
    
    @Test
    void assertUpdateNCharacterStreamWithColumnLabelUnsupported() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class,
                () -> mock(OperationFixture.class, CALLS_REAL_METHODS).updateNCharacterStream("c", mock(Reader.class)));
        assertThat(ex.getMessage(), containsString("updateNCharacterStream"));
    }
    
    @Test
    void assertUpdateNCharacterStreamWithLongLengthByIndexUnsupported() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class,
                () -> mock(OperationFixture.class, CALLS_REAL_METHODS).updateNCharacterStream(1, mock(Reader.class), 1L));
        assertThat(ex.getMessage(), containsString("updateNCharacterStream"));
    }
    
    @Test
    void assertUpdateNCharacterStreamWithLongLengthByLabelUnsupported() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class,
                () -> mock(OperationFixture.class, CALLS_REAL_METHODS).updateNCharacterStream("c", mock(Reader.class), 1L));
        assertThat(ex.getMessage(), containsString("updateNCharacterStream"));
    }
    
    @Test
    void assertUpdateObjectWithColumnIndexUnsupported() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class, () -> mock(OperationFixture.class, CALLS_REAL_METHODS).updateObject(1, new Object()));
        assertThat(ex.getMessage(), containsString("updateObject"));
    }
    
    @Test
    void assertUpdateObjectWithColumnLabelUnsupported() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class, () -> mock(OperationFixture.class, CALLS_REAL_METHODS).updateObject("c", new Object()));
        assertThat(ex.getMessage(), containsString("updateObject"));
    }
    
    @Test
    void assertUpdateObjectWithScaleByIndexUnsupported() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class, () -> mock(OperationFixture.class, CALLS_REAL_METHODS).updateObject(1, new Object(), 1));
        assertThat(ex.getMessage(), containsString("updateObject"));
    }
    
    @Test
    void assertUpdateObjectWithScaleByLabelUnsupported() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class, () -> mock(OperationFixture.class, CALLS_REAL_METHODS).updateObject("c", new Object(), 1));
        assertThat(ex.getMessage(), containsString("updateObject"));
    }
    
    @Test
    void assertUpdateRefWithColumnIndexUnsupported() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class, () -> mock(OperationFixture.class, CALLS_REAL_METHODS).updateRef(1, mock(Ref.class)));
        assertThat(ex.getMessage(), containsString("updateRef"));
    }
    
    @Test
    void assertUpdateRefWithColumnLabelUnsupported() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class, () -> mock(OperationFixture.class, CALLS_REAL_METHODS).updateRef("c", mock(Ref.class)));
        assertThat(ex.getMessage(), containsString("updateRef"));
    }
    
    @Test
    void assertUpdateBlobWithColumnIndexUnsupported() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class, () -> mock(OperationFixture.class, CALLS_REAL_METHODS).updateBlob(1, mock(Blob.class)));
        assertThat(ex.getMessage(), containsString("updateBlob"));
    }
    
    @Test
    void assertUpdateBlobWithColumnLabelUnsupported() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class, () -> mock(OperationFixture.class, CALLS_REAL_METHODS).updateBlob("c", mock(Blob.class)));
        assertThat(ex.getMessage(), containsString("updateBlob"));
    }
    
    @Test
    void assertUpdateBlobWithStreamByIndexUnsupported() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class, () -> mock(OperationFixture.class, CALLS_REAL_METHODS).updateBlob(1, mock(InputStream.class)));
        assertThat(ex.getMessage(), containsString("updateBlob"));
    }
    
    @Test
    void assertUpdateBlobWithStreamByLabelUnsupported() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class, () -> mock(OperationFixture.class, CALLS_REAL_METHODS).updateBlob("c", mock(InputStream.class)));
        assertThat(ex.getMessage(), containsString("updateBlob"));
    }
    
    @Test
    void assertUpdateBlobWithLongLengthByIndexUnsupported() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class, () -> mock(OperationFixture.class, CALLS_REAL_METHODS).updateBlob(1, mock(InputStream.class), 1L));
        assertThat(ex.getMessage(), containsString("updateBlob"));
    }
    
    @Test
    void assertUpdateBlobWithLongLengthByLabelUnsupported() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class, () -> mock(OperationFixture.class, CALLS_REAL_METHODS).updateBlob("c", mock(InputStream.class), 1L));
        assertThat(ex.getMessage(), containsString("updateBlob"));
    }
    
    @Test
    void assertUpdateClobWithColumnIndexUnsupported() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class, () -> mock(OperationFixture.class, CALLS_REAL_METHODS).updateClob(1, mock(Clob.class)));
        assertThat(ex.getMessage(), containsString("updateClob"));
    }
    
    @Test
    void assertUpdateClobWithColumnLabelUnsupported() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class, () -> mock(OperationFixture.class, CALLS_REAL_METHODS).updateClob("c", mock(Clob.class)));
        assertThat(ex.getMessage(), containsString("updateClob"));
    }
    
    @Test
    void assertUpdateClobWithReaderByIndexUnsupported() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class, () -> mock(OperationFixture.class, CALLS_REAL_METHODS).updateClob(1, mock(Reader.class)));
        assertThat(ex.getMessage(), containsString("updateClob"));
    }
    
    @Test
    void assertUpdateClobWithReaderByLabelUnsupported() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class, () -> mock(OperationFixture.class, CALLS_REAL_METHODS).updateClob("c", mock(Reader.class)));
        assertThat(ex.getMessage(), containsString("updateClob"));
    }
    
    @Test
    void assertUpdateClobWithLongLengthByIndexUnsupported() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class, () -> mock(OperationFixture.class, CALLS_REAL_METHODS).updateClob(1, mock(Reader.class), 1L));
        assertThat(ex.getMessage(), containsString("updateClob"));
    }
    
    @Test
    void assertUpdateClobWithLongLengthByLabelUnsupported() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class, () -> mock(OperationFixture.class, CALLS_REAL_METHODS).updateClob("c", mock(Reader.class), 1L));
        assertThat(ex.getMessage(), containsString("updateClob"));
    }
    
    @Test
    void assertUpdateNClobWithColumnIndexUnsupported() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class, () -> mock(OperationFixture.class, CALLS_REAL_METHODS).updateNClob(1, mock(NClob.class)));
        assertThat(ex.getMessage(), containsString("updateNClob"));
    }
    
    @Test
    void assertUpdateNClobWithColumnLabelUnsupported() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class, () -> mock(OperationFixture.class, CALLS_REAL_METHODS).updateNClob("c", mock(NClob.class)));
        assertThat(ex.getMessage(), containsString("updateNClob"));
    }
    
    @Test
    void assertUpdateNClobWithReaderByIndexUnsupported() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class, () -> mock(OperationFixture.class, CALLS_REAL_METHODS).updateNClob(1, mock(Reader.class)));
        assertThat(ex.getMessage(), containsString("updateNClob"));
    }
    
    @Test
    void assertUpdateNClobWithReaderByLabelUnsupported() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class, () -> mock(OperationFixture.class, CALLS_REAL_METHODS).updateNClob("c", mock(Reader.class)));
        assertThat(ex.getMessage(), containsString("updateNClob"));
    }
    
    @Test
    void assertUpdateNClobWithLongLengthByIndexUnsupported() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class, () -> mock(OperationFixture.class, CALLS_REAL_METHODS).updateNClob(1, mock(Reader.class), 1L));
        assertThat(ex.getMessage(), containsString("updateNClob"));
    }
    
    @Test
    void assertUpdateNClobWithLongLengthByLabelUnsupported() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class, () -> mock(OperationFixture.class, CALLS_REAL_METHODS).updateNClob("c", mock(Reader.class), 1L));
        assertThat(ex.getMessage(), containsString("updateNClob"));
    }
    
    @Test
    void assertUpdateArrayWithColumnIndexUnsupported() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class, () -> mock(OperationFixture.class, CALLS_REAL_METHODS).updateArray(1, mock(Array.class)));
        assertThat(ex.getMessage(), containsString("updateArray"));
    }
    
    @Test
    void assertUpdateArrayWithColumnLabelUnsupported() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class, () -> mock(OperationFixture.class, CALLS_REAL_METHODS).updateArray("c", mock(Array.class)));
        assertThat(ex.getMessage(), containsString("updateArray"));
    }
    
    @Test
    void assertUpdateRowIdWithColumnIndexUnsupported() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class, () -> mock(OperationFixture.class, CALLS_REAL_METHODS).updateRowId(1, mock(RowId.class)));
        assertThat(ex.getMessage(), containsString("updateRowId"));
    }
    
    @Test
    void assertUpdateRowIdWithColumnLabelUnsupported() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class, () -> mock(OperationFixture.class, CALLS_REAL_METHODS).updateRowId("c", mock(RowId.class)));
        assertThat(ex.getMessage(), containsString("updateRowId"));
    }
    
    @Test
    void assertUpdateSQLXMLWithColumnIndexUnsupported() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class, () -> mock(OperationFixture.class, CALLS_REAL_METHODS).updateSQLXML(1, mock(SQLXML.class)));
        assertThat(ex.getMessage(), containsString("updateSQLXML"));
    }
    
    @Test
    void assertUpdateSQLXMLWithColumnLabelUnsupported() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class, () -> mock(OperationFixture.class, CALLS_REAL_METHODS).updateSQLXML("c", mock(SQLXML.class)));
        assertThat(ex.getMessage(), containsString("updateSQLXML"));
    }
    
    @Test
    void assertPreviousUnsupported() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class, () -> mock(OperationFixture.class, CALLS_REAL_METHODS).previous());
        assertThat(ex.getMessage(), containsString("previous"));
    }
    
    @Test
    void assertIsBeforeFirstUnsupported() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class, () -> mock(OperationFixture.class, CALLS_REAL_METHODS).isBeforeFirst());
        assertThat(ex.getMessage(), containsString("isBeforeFirst"));
    }
    
    @Test
    void assertIsAfterLastUnsupported() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class, () -> mock(OperationFixture.class, CALLS_REAL_METHODS).isAfterLast());
        assertThat(ex.getMessage(), containsString("isAfterLast"));
    }
    
    @Test
    void assertIsFirstUnsupported() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class, () -> mock(OperationFixture.class, CALLS_REAL_METHODS).isFirst());
        assertThat(ex.getMessage(), containsString("isFirst"));
    }
    
    @Test
    void assertIsLastUnsupported() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class, () -> mock(OperationFixture.class, CALLS_REAL_METHODS).isLast());
        assertThat(ex.getMessage(), containsString("isLast"));
    }
    
    @Test
    void assertBeforeFirstUnsupported() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class, () -> mock(OperationFixture.class, CALLS_REAL_METHODS).beforeFirst());
        assertThat(ex.getMessage(), containsString("beforeFirst"));
    }
    
    @Test
    void assertAfterLastUnsupported() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class, () -> mock(OperationFixture.class, CALLS_REAL_METHODS).afterLast());
        assertThat(ex.getMessage(), containsString("afterLast"));
    }
    
    @Test
    void assertFirstUnsupported() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class, () -> mock(OperationFixture.class, CALLS_REAL_METHODS).first());
        assertThat(ex.getMessage(), containsString("first"));
    }
    
    @Test
    void assertLastUnsupported() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class, () -> mock(OperationFixture.class, CALLS_REAL_METHODS).last());
        assertThat(ex.getMessage(), containsString("last"));
    }
    
    @Test
    void assertAbsoluteUnsupported() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class, () -> mock(OperationFixture.class, CALLS_REAL_METHODS).absolute(1));
        assertThat(ex.getMessage(), containsString("absolute"));
    }
    
    @Test
    void assertRelativeUnsupported() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class, () -> mock(OperationFixture.class, CALLS_REAL_METHODS).relative(1));
        assertThat(ex.getMessage(), containsString("relative"));
    }
    
    @Test
    void assertGetRowUnsupported() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class, () -> mock(OperationFixture.class, CALLS_REAL_METHODS).getRow());
        assertThat(ex.getMessage(), containsString("getRow"));
    }
    
    @Test
    void assertInsertRowUnsupported() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class, () -> mock(OperationFixture.class, CALLS_REAL_METHODS).insertRow());
        assertThat(ex.getMessage(), containsString("insertRow"));
    }
    
    @Test
    void assertUpdateRowUnsupported() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class, () -> mock(OperationFixture.class, CALLS_REAL_METHODS).updateRow());
        assertThat(ex.getMessage(), containsString("updateRow"));
    }
    
    @Test
    void assertDeleteRowUnsupported() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class, () -> mock(OperationFixture.class, CALLS_REAL_METHODS).deleteRow());
        assertThat(ex.getMessage(), containsString("deleteRow"));
    }
    
    @Test
    void assertRefreshRowUnsupported() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class, () -> mock(OperationFixture.class, CALLS_REAL_METHODS).refreshRow());
        assertThat(ex.getMessage(), containsString("refreshRow"));
    }
    
    @Test
    void assertCancelRowUpdatesUnsupported() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class, () -> mock(OperationFixture.class, CALLS_REAL_METHODS).cancelRowUpdates());
        assertThat(ex.getMessage(), containsString("cancelRowUpdates"));
    }
    
    @Test
    void assertMoveToInsertRowUnsupported() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class, () -> mock(OperationFixture.class, CALLS_REAL_METHODS).moveToInsertRow());
        assertThat(ex.getMessage(), containsString("moveToInsertRow"));
    }
    
    @Test
    void assertMoveToCurrentRowUnsupported() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class, () -> mock(OperationFixture.class, CALLS_REAL_METHODS).moveToCurrentRow());
        assertThat(ex.getMessage(), containsString("moveToCurrentRow"));
    }
    
    @Test
    void assertRowInsertedUnsupported() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class, () -> mock(OperationFixture.class, CALLS_REAL_METHODS).rowInserted());
        assertThat(ex.getMessage(), containsString("rowInserted"));
    }
    
    @Test
    void assertRowUpdatedUnsupported() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class, () -> mock(OperationFixture.class, CALLS_REAL_METHODS).rowUpdated());
        assertThat(ex.getMessage(), containsString("rowUpdated"));
    }
    
    @Test
    void assertRowDeletedUnsupported() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class, () -> mock(OperationFixture.class, CALLS_REAL_METHODS).rowDeleted());
        assertThat(ex.getMessage(), containsString("rowDeleted"));
    }
    
    @Test
    void assertGetCursorNameUnsupported() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class, () -> mock(OperationFixture.class, CALLS_REAL_METHODS).getCursorName());
        assertThat(ex.getMessage(), containsString("getCursorName"));
    }
    
    @Test
    void assertGetHoldabilityUnsupported() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class, () -> mock(OperationFixture.class, CALLS_REAL_METHODS).getHoldability());
        assertThat(ex.getMessage(), containsString("getHoldability"));
    }
    
    @Test
    void assertGetNClobWithColumnIndexUnsupported() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class, () -> mock(OperationFixture.class, CALLS_REAL_METHODS).getNClob(1));
        assertThat(ex.getMessage(), containsString("getNClob"));
    }
    
    @Test
    void assertGetNClobWithColumnLabelUnsupported() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class, () -> mock(OperationFixture.class, CALLS_REAL_METHODS).getNClob("c"));
        assertThat(ex.getMessage(), containsString("getNClob"));
    }
    
    @Test
    void assertGetNCharacterStreamWithColumnIndexUnsupported() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class, () -> mock(OperationFixture.class, CALLS_REAL_METHODS).getNCharacterStream(1));
        assertThat(ex.getMessage(), containsString("getNCharacterStream"));
    }
    
    @Test
    void assertGetNCharacterStreamWithColumnLabelUnsupported() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class, () -> mock(OperationFixture.class, CALLS_REAL_METHODS).getNCharacterStream("c"));
        assertThat(ex.getMessage(), containsString("getNCharacterStream"));
    }
    
    @Test
    void assertGetRefWithColumnIndexUnsupported() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class, () -> mock(OperationFixture.class, CALLS_REAL_METHODS).getRef(1));
        assertThat(ex.getMessage(), containsString("getRef"));
    }
    
    @Test
    void assertGetRefWithColumnLabelUnsupported() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class, () -> mock(OperationFixture.class, CALLS_REAL_METHODS).getRef("c"));
        assertThat(ex.getMessage(), containsString("getRef"));
    }
    
    @Test
    void assertGetRowIdWithColumnIndexUnsupported() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class, () -> mock(OperationFixture.class, CALLS_REAL_METHODS).getRowId(1));
        assertThat(ex.getMessage(), containsString("getRowId"));
    }
    
    @Test
    void assertGetRowIdWithColumnLabelUnsupported() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class, () -> mock(OperationFixture.class, CALLS_REAL_METHODS).getRowId("c"));
        assertThat(ex.getMessage(), containsString("getRowId"));
    }
    
    @Test
    void assertGetObjectWithTypeByColumnIndexUnsupported() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class, () -> mock(OperationFixture.class, CALLS_REAL_METHODS).getObject(1, Object.class));
        assertThat(ex.getMessage(), containsString("getObject with type"));
    }
    
    @Test
    void assertGetObjectWithTypeByColumnLabelUnsupported() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class, () -> mock(OperationFixture.class, CALLS_REAL_METHODS).getObject("c", Object.class));
        assertThat(ex.getMessage(), containsString("getObject with type"));
    }
    
    @Test
    void assertGetObjectWithMapByColumnLabelUnsupported() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class, () -> mock(OperationFixture.class, CALLS_REAL_METHODS).getObject("c", Collections.emptyMap()));
        assertThat(ex.getMessage(), containsString("getObject with map"));
    }
    
    @Test
    void assertGetObjectWithMapByColumnIndexUnsupported() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class, () -> mock(OperationFixture.class, CALLS_REAL_METHODS).getObject(1, Collections.emptyMap()));
        assertThat(ex.getMessage(), containsString("getObject with map"));
    }
    
    abstract static class OperationFixture extends AbstractUnsupportedOperationSQLFederationResultSet {
    }
}
