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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;

@SuppressWarnings("resource")
class AbstractUnsupportedOperationSQLFederationResultSetTest {
    
    @Test
    void assertUpdateNullWithColumnIndex() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class,
                () -> mock(AbstractUnsupportedOperationSQLFederationResultSet.class, CALLS_REAL_METHODS).updateNull(1));
        assertThat(ex.getMessage(), is("updateNull"));
    }
    
    @Test
    void assertUpdateNullWithColumnLabel() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class,
                () -> mock(AbstractUnsupportedOperationSQLFederationResultSet.class, CALLS_REAL_METHODS).updateNull("c"));
        assertThat(ex.getMessage(), is("updateNull"));
    }
    
    @Test
    void assertUpdateBooleanWithColumnIndex() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class,
                () -> mock(AbstractUnsupportedOperationSQLFederationResultSet.class, CALLS_REAL_METHODS).updateBoolean(1, true));
        assertThat(ex.getMessage(), is("updateBoolean"));
    }
    
    @Test
    void assertUpdateBooleanWithColumnLabel() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class,
                () -> mock(AbstractUnsupportedOperationSQLFederationResultSet.class, CALLS_REAL_METHODS).updateBoolean("c", true));
        assertThat(ex.getMessage(), is("updateBoolean"));
    }
    
    @Test
    void assertUpdateByteWithColumnIndex() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class,
                () -> mock(AbstractUnsupportedOperationSQLFederationResultSet.class, CALLS_REAL_METHODS).updateByte(1, (byte) 1));
        assertThat(ex.getMessage(), is("updateByte"));
    }
    
    @Test
    void assertUpdateByteWithColumnLabel() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class,
                () -> mock(AbstractUnsupportedOperationSQLFederationResultSet.class, CALLS_REAL_METHODS).updateByte("c", (byte) 1));
        assertThat(ex.getMessage(), is("updateByte"));
    }
    
    @Test
    void assertUpdateShortWithColumnIndex() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class,
                () -> mock(AbstractUnsupportedOperationSQLFederationResultSet.class, CALLS_REAL_METHODS).updateShort(1, (short) 1));
        assertThat(ex.getMessage(), is("updateShort"));
    }
    
    @Test
    void assertUpdateShortWithColumnLabel() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class,
                () -> mock(AbstractUnsupportedOperationSQLFederationResultSet.class, CALLS_REAL_METHODS).updateShort("c", (short) 1));
        assertThat(ex.getMessage(), is("updateShort"));
    }
    
    @Test
    void assertUpdateIntWithColumnIndex() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class,
                () -> mock(AbstractUnsupportedOperationSQLFederationResultSet.class, CALLS_REAL_METHODS).updateInt(1, 1));
        assertThat(ex.getMessage(), is("updateInt"));
    }
    
    @Test
    void assertUpdateIntWithColumnLabel() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class,
                () -> mock(AbstractUnsupportedOperationSQLFederationResultSet.class, CALLS_REAL_METHODS).updateInt("c", 1));
        assertThat(ex.getMessage(), is("updateInt"));
    }
    
    @Test
    void assertUpdateLongWithColumnIndex() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class,
                () -> mock(AbstractUnsupportedOperationSQLFederationResultSet.class, CALLS_REAL_METHODS).updateLong(1, 1L));
        assertThat(ex.getMessage(), is("updateLong"));
    }
    
    @Test
    void assertUpdateLongWithColumnLabel() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class,
                () -> mock(AbstractUnsupportedOperationSQLFederationResultSet.class, CALLS_REAL_METHODS).updateLong("c", 1L));
        assertThat(ex.getMessage(), is("updateLong"));
    }
    
    @Test
    void assertUpdateFloatWithColumnIndex() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class,
                () -> mock(AbstractUnsupportedOperationSQLFederationResultSet.class, CALLS_REAL_METHODS).updateFloat(1, 1F));
        assertThat(ex.getMessage(), is("updateFloat"));
    }
    
    @Test
    void assertUpdateFloatWithColumnLabel() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class,
                () -> mock(AbstractUnsupportedOperationSQLFederationResultSet.class, CALLS_REAL_METHODS).updateFloat("c", 1F));
        assertThat(ex.getMessage(), is("updateFloat"));
    }
    
    @Test
    void assertUpdateDoubleWithColumnIndex() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class,
                () -> mock(AbstractUnsupportedOperationSQLFederationResultSet.class, CALLS_REAL_METHODS).updateDouble(1, 1D));
        assertThat(ex.getMessage(), is("updateDouble"));
    }
    
    @Test
    void assertUpdateDoubleWithColumnLabel() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class,
                () -> mock(AbstractUnsupportedOperationSQLFederationResultSet.class, CALLS_REAL_METHODS).updateDouble("c", 1D));
        assertThat(ex.getMessage(), is("updateDouble"));
    }
    
    @Test
    void assertUpdateBigDecimalWithColumnIndex() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class,
                () -> mock(AbstractUnsupportedOperationSQLFederationResultSet.class, CALLS_REAL_METHODS).updateBigDecimal(1, BigDecimal.ONE));
        assertThat(ex.getMessage(), is("updateBigDecimal"));
    }
    
    @Test
    void assertUpdateBigDecimalWithColumnLabel() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class,
                () -> mock(AbstractUnsupportedOperationSQLFederationResultSet.class, CALLS_REAL_METHODS).updateBigDecimal("c", BigDecimal.ONE));
        assertThat(ex.getMessage(), is("updateBigDecimal"));
    }
    
    @Test
    void assertUpdateStringWithColumnIndex() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class,
                () -> mock(AbstractUnsupportedOperationSQLFederationResultSet.class, CALLS_REAL_METHODS).updateString(1, "v"));
        assertThat(ex.getMessage(), is("updateString"));
    }
    
    @Test
    void assertUpdateStringWithColumnLabel() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class,
                () -> mock(AbstractUnsupportedOperationSQLFederationResultSet.class, CALLS_REAL_METHODS).updateString("c", "v"));
        assertThat(ex.getMessage(), is("updateString"));
    }
    
    @Test
    void assertUpdateNStringWithColumnIndex() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class,
                () -> mock(AbstractUnsupportedOperationSQLFederationResultSet.class, CALLS_REAL_METHODS).updateNString(1, "v"));
        assertThat(ex.getMessage(), is("updateNString"));
    }
    
    @Test
    void assertUpdateNStringWithColumnLabel() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class,
                () -> mock(AbstractUnsupportedOperationSQLFederationResultSet.class, CALLS_REAL_METHODS).updateNString("c", "v"));
        assertThat(ex.getMessage(), is("updateNString"));
    }
    
    @Test
    void assertUpdateBytesWithColumnIndex() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class,
                () -> mock(AbstractUnsupportedOperationSQLFederationResultSet.class, CALLS_REAL_METHODS).updateBytes(1, new byte[]{}));
        assertThat(ex.getMessage(), is("updateBytes"));
    }
    
    @Test
    void assertUpdateBytesWithColumnLabel() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class,
                () -> mock(AbstractUnsupportedOperationSQLFederationResultSet.class, CALLS_REAL_METHODS).updateBytes("c", new byte[]{}));
        assertThat(ex.getMessage(), is("updateBytes"));
    }
    
    @Test
    void assertUpdateDateWithColumnIndex() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class,
                () -> mock(AbstractUnsupportedOperationSQLFederationResultSet.class, CALLS_REAL_METHODS).updateDate(1, mock(Date.class)));
        assertThat(ex.getMessage(), is("updateDate"));
    }
    
    @Test
    void assertUpdateDateWithColumnLabel() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class,
                () -> mock(AbstractUnsupportedOperationSQLFederationResultSet.class, CALLS_REAL_METHODS).updateDate("c", mock(Date.class)));
        assertThat(ex.getMessage(), is("updateDate"));
    }
    
    @Test
    void assertUpdateTimeWithColumnIndex() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class,
                () -> mock(AbstractUnsupportedOperationSQLFederationResultSet.class, CALLS_REAL_METHODS).updateTime(1, mock(Time.class)));
        assertThat(ex.getMessage(), is("updateTime"));
    }
    
    @Test
    void assertUpdateTimeWithColumnLabel() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class,
                () -> mock(AbstractUnsupportedOperationSQLFederationResultSet.class, CALLS_REAL_METHODS).updateTime("c", mock(Time.class)));
        assertThat(ex.getMessage(), is("updateTime"));
    }
    
    @Test
    void assertUpdateTimestampWithColumnIndex() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class,
                () -> mock(AbstractUnsupportedOperationSQLFederationResultSet.class, CALLS_REAL_METHODS).updateTimestamp(1, mock(Timestamp.class)));
        assertThat(ex.getMessage(), is("updateTimestamp"));
    }
    
    @Test
    void assertUpdateTimestampWithColumnLabel() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class,
                () -> mock(AbstractUnsupportedOperationSQLFederationResultSet.class, CALLS_REAL_METHODS).updateTimestamp("c", mock(Timestamp.class)));
        assertThat(ex.getMessage(), is("updateTimestamp"));
    }
    
    @Test
    void assertUpdateAsciiStreamWithColumnIndex() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class,
                () -> mock(AbstractUnsupportedOperationSQLFederationResultSet.class, CALLS_REAL_METHODS).updateAsciiStream(1, mock(InputStream.class)));
        assertThat(ex.getMessage(), is("updateAsciiStream"));
    }
    
    @Test
    void assertUpdateAsciiStreamWithColumnLabel() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class,
                () -> mock(AbstractUnsupportedOperationSQLFederationResultSet.class, CALLS_REAL_METHODS).updateAsciiStream("c", mock(InputStream.class)));
        assertThat(ex.getMessage(), is("updateAsciiStream"));
    }
    
    @Test
    void assertUpdateAsciiStreamWithLengthByIndex() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class,
                () -> mock(AbstractUnsupportedOperationSQLFederationResultSet.class, CALLS_REAL_METHODS).updateAsciiStream(1, mock(InputStream.class), 1));
        assertThat(ex.getMessage(), is("updateAsciiStream"));
    }
    
    @Test
    void assertUpdateAsciiStreamWithLengthByLabel() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class,
                () -> mock(AbstractUnsupportedOperationSQLFederationResultSet.class, CALLS_REAL_METHODS).updateAsciiStream("c", mock(InputStream.class), 1));
        assertThat(ex.getMessage(), is("updateAsciiStream"));
    }
    
    @Test
    void assertUpdateAsciiStreamWithLongLengthByIndex() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class,
                () -> mock(AbstractUnsupportedOperationSQLFederationResultSet.class, CALLS_REAL_METHODS).updateAsciiStream(1, mock(InputStream.class), 1L));
        assertThat(ex.getMessage(), is("updateAsciiStream"));
    }
    
    @Test
    void assertUpdateAsciiStreamWithLongLengthByLabel() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class,
                () -> mock(AbstractUnsupportedOperationSQLFederationResultSet.class, CALLS_REAL_METHODS).updateAsciiStream("c", mock(InputStream.class), 1L));
        assertThat(ex.getMessage(), is("updateAsciiStream"));
    }
    
    @Test
    void assertUpdateBinaryStreamWithColumnIndex() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class,
                () -> mock(AbstractUnsupportedOperationSQLFederationResultSet.class, CALLS_REAL_METHODS).updateBinaryStream(1, mock(InputStream.class)));
        assertThat(ex.getMessage(), is("updateBinaryStream"));
    }
    
    @Test
    void assertUpdateBinaryStreamWithColumnLabel() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class,
                () -> mock(AbstractUnsupportedOperationSQLFederationResultSet.class, CALLS_REAL_METHODS).updateBinaryStream("c", mock(InputStream.class)));
        assertThat(ex.getMessage(), is("updateBinaryStream"));
    }
    
    @Test
    void assertUpdateBinaryStreamWithLengthByIndex() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class,
                () -> mock(AbstractUnsupportedOperationSQLFederationResultSet.class, CALLS_REAL_METHODS).updateBinaryStream(1, mock(InputStream.class), 1));
        assertThat(ex.getMessage(), is("updateBinaryStream"));
    }
    
    @Test
    void assertUpdateBinaryStreamWithLengthByLabel() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class,
                () -> mock(AbstractUnsupportedOperationSQLFederationResultSet.class, CALLS_REAL_METHODS).updateBinaryStream("c", mock(InputStream.class), 1));
        assertThat(ex.getMessage(), is("updateBinaryStream"));
    }
    
    @Test
    void assertUpdateBinaryStreamWithLongLengthByIndex() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class,
                () -> mock(AbstractUnsupportedOperationSQLFederationResultSet.class, CALLS_REAL_METHODS).updateBinaryStream(1, mock(InputStream.class), 1L));
        assertThat(ex.getMessage(), is("updateBinaryStream"));
    }
    
    @Test
    void assertUpdateBinaryStreamWithLongLengthByLabel() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class,
                () -> mock(AbstractUnsupportedOperationSQLFederationResultSet.class, CALLS_REAL_METHODS).updateBinaryStream("c", mock(InputStream.class), 1L));
        assertThat(ex.getMessage(), is("updateBinaryStream"));
    }
    
    @Test
    void assertUpdateCharacterStreamWithColumnIndex() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class,
                () -> mock(AbstractUnsupportedOperationSQLFederationResultSet.class, CALLS_REAL_METHODS).updateCharacterStream(1, mock(Reader.class)));
        assertThat(ex.getMessage(), is("updateCharacterStream"));
    }
    
    @Test
    void assertUpdateCharacterStreamWithColumnLabel() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class,
                () -> mock(AbstractUnsupportedOperationSQLFederationResultSet.class, CALLS_REAL_METHODS).updateCharacterStream("c", mock(Reader.class)));
        assertThat(ex.getMessage(), is("updateCharacterStream"));
    }
    
    @Test
    void assertUpdateCharacterStreamWithLengthByIndex() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class,
                () -> mock(AbstractUnsupportedOperationSQLFederationResultSet.class, CALLS_REAL_METHODS).updateCharacterStream(1, mock(Reader.class), 1));
        assertThat(ex.getMessage(), is("updateCharacterStream"));
    }
    
    @Test
    void assertUpdateCharacterStreamWithLengthByLabel() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class,
                () -> mock(AbstractUnsupportedOperationSQLFederationResultSet.class, CALLS_REAL_METHODS).updateCharacterStream("c", mock(Reader.class), 1));
        assertThat(ex.getMessage(), is("updateCharacterStream"));
    }
    
    @Test
    void assertUpdateCharacterStreamWithLongLengthByIndex() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class,
                () -> mock(AbstractUnsupportedOperationSQLFederationResultSet.class, CALLS_REAL_METHODS).updateCharacterStream(1, mock(Reader.class), 1L));
        assertThat(ex.getMessage(), is("updateCharacterStream"));
    }
    
    @Test
    void assertUpdateCharacterStreamWithLongLengthByLabel() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class,
                () -> mock(AbstractUnsupportedOperationSQLFederationResultSet.class, CALLS_REAL_METHODS).updateCharacterStream("c", mock(Reader.class), 1L));
        assertThat(ex.getMessage(), is("updateCharacterStream"));
    }
    
    @Test
    void assertUpdateNCharacterStreamWithColumnIndex() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class,
                () -> mock(AbstractUnsupportedOperationSQLFederationResultSet.class, CALLS_REAL_METHODS).updateNCharacterStream(1, mock(Reader.class)));
        assertThat(ex.getMessage(), is("updateNCharacterStream"));
    }
    
    @Test
    void assertUpdateNCharacterStreamWithColumnLabel() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class,
                () -> mock(AbstractUnsupportedOperationSQLFederationResultSet.class, CALLS_REAL_METHODS).updateNCharacterStream("c", mock(Reader.class)));
        assertThat(ex.getMessage(), is("updateNCharacterStream"));
    }
    
    @Test
    void assertUpdateNCharacterStreamWithLongLengthByIndex() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class,
                () -> mock(AbstractUnsupportedOperationSQLFederationResultSet.class, CALLS_REAL_METHODS).updateNCharacterStream(1, mock(Reader.class), 1L));
        assertThat(ex.getMessage(), is("updateNCharacterStream"));
    }
    
    @Test
    void assertUpdateNCharacterStreamWithLongLengthByLabel() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class,
                () -> mock(AbstractUnsupportedOperationSQLFederationResultSet.class, CALLS_REAL_METHODS).updateNCharacterStream("c", mock(Reader.class), 1L));
        assertThat(ex.getMessage(), is("updateNCharacterStream"));
    }
    
    @Test
    void assertUpdateObjectWithColumnIndex() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class,
                () -> mock(AbstractUnsupportedOperationSQLFederationResultSet.class, CALLS_REAL_METHODS).updateObject(1, new Object()));
        assertThat(ex.getMessage(), is("updateObject"));
    }
    
    @Test
    void assertUpdateObjectWithColumnLabel() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class,
                () -> mock(AbstractUnsupportedOperationSQLFederationResultSet.class, CALLS_REAL_METHODS).updateObject("c", new Object()));
        assertThat(ex.getMessage(), is("updateObject"));
    }
    
    @Test
    void assertUpdateObjectWithScaleByIndex() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class,
                () -> mock(AbstractUnsupportedOperationSQLFederationResultSet.class, CALLS_REAL_METHODS).updateObject(1, new Object(), 1));
        assertThat(ex.getMessage(), is("updateObject"));
    }
    
    @Test
    void assertUpdateObjectWithScaleByLabel() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class,
                () -> mock(AbstractUnsupportedOperationSQLFederationResultSet.class, CALLS_REAL_METHODS).updateObject("c", new Object(), 1));
        assertThat(ex.getMessage(), is("updateObject"));
    }
    
    @Test
    void assertUpdateRefWithColumnIndex() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class,
                () -> mock(AbstractUnsupportedOperationSQLFederationResultSet.class, CALLS_REAL_METHODS).updateRef(1, mock(Ref.class)));
        assertThat(ex.getMessage(), is("updateRef"));
    }
    
    @Test
    void assertUpdateRefWithColumnLabel() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class,
                () -> mock(AbstractUnsupportedOperationSQLFederationResultSet.class, CALLS_REAL_METHODS).updateRef("c", mock(Ref.class)));
        assertThat(ex.getMessage(), is("updateRef"));
    }
    
    @Test
    void assertUpdateBlobWithColumnIndex() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class,
                () -> mock(AbstractUnsupportedOperationSQLFederationResultSet.class, CALLS_REAL_METHODS).updateBlob(1, mock(Blob.class)));
        assertThat(ex.getMessage(), is("updateBlob"));
    }
    
    @Test
    void assertUpdateBlobWithColumnLabel() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class,
                () -> mock(AbstractUnsupportedOperationSQLFederationResultSet.class, CALLS_REAL_METHODS).updateBlob("c", mock(Blob.class)));
        assertThat(ex.getMessage(), is("updateBlob"));
    }
    
    @Test
    void assertUpdateBlobWithStreamByIndex() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class,
                () -> mock(AbstractUnsupportedOperationSQLFederationResultSet.class, CALLS_REAL_METHODS).updateBlob(1, mock(InputStream.class)));
        assertThat(ex.getMessage(), is("updateBlob"));
    }
    
    @Test
    void assertUpdateBlobWithStreamByLabel() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class,
                () -> mock(AbstractUnsupportedOperationSQLFederationResultSet.class, CALLS_REAL_METHODS).updateBlob("c", mock(InputStream.class)));
        assertThat(ex.getMessage(), is("updateBlob"));
    }
    
    @Test
    void assertUpdateBlobWithLongLengthByIndex() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class,
                () -> mock(AbstractUnsupportedOperationSQLFederationResultSet.class, CALLS_REAL_METHODS).updateBlob(1, mock(InputStream.class), 1L));
        assertThat(ex.getMessage(), is("updateBlob"));
    }
    
    @Test
    void assertUpdateBlobWithLongLengthByLabel() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class,
                () -> mock(AbstractUnsupportedOperationSQLFederationResultSet.class, CALLS_REAL_METHODS).updateBlob("c", mock(InputStream.class), 1L));
        assertThat(ex.getMessage(), is("updateBlob"));
    }
    
    @Test
    void assertUpdateClobWithColumnIndex() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class,
                () -> mock(AbstractUnsupportedOperationSQLFederationResultSet.class, CALLS_REAL_METHODS).updateClob(1, mock(Clob.class)));
        assertThat(ex.getMessage(), is("updateClob"));
    }
    
    @Test
    void assertUpdateClobWithColumnLabel() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class,
                () -> mock(AbstractUnsupportedOperationSQLFederationResultSet.class, CALLS_REAL_METHODS).updateClob("c", mock(Clob.class)));
        assertThat(ex.getMessage(), is("updateClob"));
    }
    
    @Test
    void assertUpdateClobWithReaderByIndex() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class,
                () -> mock(AbstractUnsupportedOperationSQLFederationResultSet.class, CALLS_REAL_METHODS).updateClob(1, mock(Reader.class)));
        assertThat(ex.getMessage(), is("updateClob"));
    }
    
    @Test
    void assertUpdateClobWithReaderByLabel() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class,
                () -> mock(AbstractUnsupportedOperationSQLFederationResultSet.class, CALLS_REAL_METHODS).updateClob("c", mock(Reader.class)));
        assertThat(ex.getMessage(), is("updateClob"));
    }
    
    @Test
    void assertUpdateClobWithLongLengthByIndex() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class,
                () -> mock(AbstractUnsupportedOperationSQLFederationResultSet.class, CALLS_REAL_METHODS).updateClob(1, mock(Reader.class), 1L));
        assertThat(ex.getMessage(), is("updateClob"));
    }
    
    @Test
    void assertUpdateClobWithLongLengthByLabel() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class,
                () -> mock(AbstractUnsupportedOperationSQLFederationResultSet.class, CALLS_REAL_METHODS).updateClob("c", mock(Reader.class), 1L));
        assertThat(ex.getMessage(), is("updateClob"));
    }
    
    @Test
    void assertUpdateNClobWithColumnIndex() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class,
                () -> mock(AbstractUnsupportedOperationSQLFederationResultSet.class, CALLS_REAL_METHODS).updateNClob(1, mock(NClob.class)));
        assertThat(ex.getMessage(), is("updateNClob"));
    }
    
    @Test
    void assertUpdateNClobWithColumnLabel() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class,
                () -> mock(AbstractUnsupportedOperationSQLFederationResultSet.class, CALLS_REAL_METHODS).updateNClob("c", mock(NClob.class)));
        assertThat(ex.getMessage(), is("updateNClob"));
    }
    
    @Test
    void assertUpdateNClobWithReaderByIndex() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class,
                () -> mock(AbstractUnsupportedOperationSQLFederationResultSet.class, CALLS_REAL_METHODS).updateNClob(1, mock(Reader.class)));
        assertThat(ex.getMessage(), is("updateNClob"));
    }
    
    @Test
    void assertUpdateNClobWithReaderByLabel() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class,
                () -> mock(AbstractUnsupportedOperationSQLFederationResultSet.class, CALLS_REAL_METHODS).updateNClob("c", mock(Reader.class)));
        assertThat(ex.getMessage(), is("updateNClob"));
    }
    
    @Test
    void assertUpdateNClobWithLongLengthByIndex() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class,
                () -> mock(AbstractUnsupportedOperationSQLFederationResultSet.class, CALLS_REAL_METHODS).updateNClob(1, mock(Reader.class), 1L));
        assertThat(ex.getMessage(), is("updateNClob"));
    }
    
    @Test
    void assertUpdateNClobWithLongLengthByLabel() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class,
                () -> mock(AbstractUnsupportedOperationSQLFederationResultSet.class, CALLS_REAL_METHODS).updateNClob("c", mock(Reader.class), 1L));
        assertThat(ex.getMessage(), is("updateNClob"));
    }
    
    @Test
    void assertUpdateArrayWithColumnIndex() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class,
                () -> mock(AbstractUnsupportedOperationSQLFederationResultSet.class, CALLS_REAL_METHODS).updateArray(1, mock(Array.class)));
        assertThat(ex.getMessage(), is("updateArray"));
    }
    
    @Test
    void assertUpdateArrayWithColumnLabel() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class,
                () -> mock(AbstractUnsupportedOperationSQLFederationResultSet.class, CALLS_REAL_METHODS).updateArray("c", mock(Array.class)));
        assertThat(ex.getMessage(), is("updateArray"));
    }
    
    @Test
    void assertUpdateRowIdWithColumnIndex() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class,
                () -> mock(AbstractUnsupportedOperationSQLFederationResultSet.class, CALLS_REAL_METHODS).updateRowId(1, mock(RowId.class)));
        assertThat(ex.getMessage(), is("updateRowId"));
    }
    
    @Test
    void assertUpdateRowIdWithColumnLabel() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class,
                () -> mock(AbstractUnsupportedOperationSQLFederationResultSet.class, CALLS_REAL_METHODS).updateRowId("c", mock(RowId.class)));
        assertThat(ex.getMessage(), is("updateRowId"));
    }
    
    @Test
    void assertUpdateSQLXMLWithColumnIndex() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class,
                () -> mock(AbstractUnsupportedOperationSQLFederationResultSet.class, CALLS_REAL_METHODS).updateSQLXML(1, mock(SQLXML.class)));
        assertThat(ex.getMessage(), is("updateSQLXML"));
    }
    
    @Test
    void assertUpdateSQLXMLWithColumnLabel() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class,
                () -> mock(AbstractUnsupportedOperationSQLFederationResultSet.class, CALLS_REAL_METHODS).updateSQLXML("c", mock(SQLXML.class)));
        assertThat(ex.getMessage(), is("updateSQLXML"));
    }
    
    @Test
    void assertPrevious() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class,
                () -> mock(AbstractUnsupportedOperationSQLFederationResultSet.class, CALLS_REAL_METHODS).previous());
        assertThat(ex.getMessage(), is("previous"));
    }
    
    @Test
    void assertIsBeforeFirst() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class,
                () -> mock(AbstractUnsupportedOperationSQLFederationResultSet.class, CALLS_REAL_METHODS).isBeforeFirst());
        assertThat(ex.getMessage(), is("isBeforeFirst"));
    }
    
    @Test
    void assertIsAfterLast() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class,
                () -> mock(AbstractUnsupportedOperationSQLFederationResultSet.class, CALLS_REAL_METHODS).isAfterLast());
        assertThat(ex.getMessage(), is("isAfterLast"));
    }
    
    @Test
    void assertIsFirst() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class,
                () -> mock(AbstractUnsupportedOperationSQLFederationResultSet.class, CALLS_REAL_METHODS).isFirst());
        assertThat(ex.getMessage(), is("isFirst"));
    }
    
    @Test
    void assertIsLast() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class,
                () -> mock(AbstractUnsupportedOperationSQLFederationResultSet.class, CALLS_REAL_METHODS).isLast());
        assertThat(ex.getMessage(), is("isLast"));
    }
    
    @Test
    void assertBeforeFirst() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class,
                () -> mock(AbstractUnsupportedOperationSQLFederationResultSet.class, CALLS_REAL_METHODS).beforeFirst());
        assertThat(ex.getMessage(), is("beforeFirst"));
    }
    
    @Test
    void assertAfterLast() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class,
                () -> mock(AbstractUnsupportedOperationSQLFederationResultSet.class, CALLS_REAL_METHODS).afterLast());
        assertThat(ex.getMessage(), is("afterLast"));
    }
    
    @Test
    void assertFirst() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class,
                () -> mock(AbstractUnsupportedOperationSQLFederationResultSet.class, CALLS_REAL_METHODS).first());
        assertThat(ex.getMessage(), is("first"));
    }
    
    @Test
    void assertLast() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class,
                () -> mock(AbstractUnsupportedOperationSQLFederationResultSet.class, CALLS_REAL_METHODS).last());
        assertThat(ex.getMessage(), is("last"));
    }
    
    @Test
    void assertAbsolute() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class,
                () -> mock(AbstractUnsupportedOperationSQLFederationResultSet.class, CALLS_REAL_METHODS).absolute(1));
        assertThat(ex.getMessage(), is("absolute"));
    }
    
    @Test
    void assertRelative() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class,
                () -> mock(AbstractUnsupportedOperationSQLFederationResultSet.class, CALLS_REAL_METHODS).relative(1));
        assertThat(ex.getMessage(), is("relative"));
    }
    
    @Test
    void assertGetRow() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class,
                () -> mock(AbstractUnsupportedOperationSQLFederationResultSet.class, CALLS_REAL_METHODS).getRow());
        assertThat(ex.getMessage(), is("getRow"));
    }
    
    @Test
    void assertInsertRow() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class,
                () -> mock(AbstractUnsupportedOperationSQLFederationResultSet.class, CALLS_REAL_METHODS).insertRow());
        assertThat(ex.getMessage(), is("insertRow"));
    }
    
    @Test
    void assertUpdateRow() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class,
                () -> mock(AbstractUnsupportedOperationSQLFederationResultSet.class, CALLS_REAL_METHODS).updateRow());
        assertThat(ex.getMessage(), is("updateRow"));
    }
    
    @Test
    void assertDeleteRow() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class,
                () -> mock(AbstractUnsupportedOperationSQLFederationResultSet.class, CALLS_REAL_METHODS).deleteRow());
        assertThat(ex.getMessage(), is("deleteRow"));
    }
    
    @Test
    void assertRefreshRow() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class,
                () -> mock(AbstractUnsupportedOperationSQLFederationResultSet.class, CALLS_REAL_METHODS).refreshRow());
        assertThat(ex.getMessage(), is("refreshRow"));
    }
    
    @Test
    void assertCancelRowUpdates() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class,
                () -> mock(AbstractUnsupportedOperationSQLFederationResultSet.class, CALLS_REAL_METHODS).cancelRowUpdates());
        assertThat(ex.getMessage(), is("cancelRowUpdates"));
    }
    
    @Test
    void assertMoveToInsertRow() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class,
                () -> mock(AbstractUnsupportedOperationSQLFederationResultSet.class, CALLS_REAL_METHODS).moveToInsertRow());
        assertThat(ex.getMessage(), is("moveToInsertRow"));
    }
    
    @Test
    void assertMoveToCurrentRow() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class,
                () -> mock(AbstractUnsupportedOperationSQLFederationResultSet.class, CALLS_REAL_METHODS).moveToCurrentRow());
        assertThat(ex.getMessage(), is("moveToCurrentRow"));
    }
    
    @Test
    void assertRowInserted() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class,
                () -> mock(AbstractUnsupportedOperationSQLFederationResultSet.class, CALLS_REAL_METHODS).rowInserted());
        assertThat(ex.getMessage(), is("rowInserted"));
    }
    
    @Test
    void assertRowUpdated() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class,
                () -> mock(AbstractUnsupportedOperationSQLFederationResultSet.class, CALLS_REAL_METHODS).rowUpdated());
        assertThat(ex.getMessage(), is("rowUpdated"));
    }
    
    @Test
    void assertRowDeleted() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class,
                () -> mock(AbstractUnsupportedOperationSQLFederationResultSet.class, CALLS_REAL_METHODS).rowDeleted());
        assertThat(ex.getMessage(), is("rowDeleted"));
    }
    
    @Test
    void assertGetCursorName() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class,
                () -> mock(AbstractUnsupportedOperationSQLFederationResultSet.class, CALLS_REAL_METHODS).getCursorName());
        assertThat(ex.getMessage(), is("getCursorName"));
    }
    
    @Test
    void assertGetHoldability() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class,
                () -> mock(AbstractUnsupportedOperationSQLFederationResultSet.class, CALLS_REAL_METHODS).getHoldability());
        assertThat(ex.getMessage(), is("getHoldability"));
    }
    
    @Test
    void assertGetNClobWithColumnIndex() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class,
                () -> mock(AbstractUnsupportedOperationSQLFederationResultSet.class, CALLS_REAL_METHODS).getNClob(1));
        assertThat(ex.getMessage(), is("getNClob"));
    }
    
    @Test
    void assertGetNClobWithColumnLabel() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class,
                () -> mock(AbstractUnsupportedOperationSQLFederationResultSet.class, CALLS_REAL_METHODS).getNClob("c"));
        assertThat(ex.getMessage(), is("getNClob"));
    }
    
    @Test
    void assertGetNCharacterStreamWithColumnIndex() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class,
                () -> mock(AbstractUnsupportedOperationSQLFederationResultSet.class, CALLS_REAL_METHODS).getNCharacterStream(1));
        assertThat(ex.getMessage(), is("getNCharacterStream"));
    }
    
    @Test
    void assertGetNCharacterStreamWithColumnLabel() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class,
                () -> mock(AbstractUnsupportedOperationSQLFederationResultSet.class, CALLS_REAL_METHODS).getNCharacterStream("c"));
        assertThat(ex.getMessage(), is("getNCharacterStream"));
    }
    
    @Test
    void assertGetRefWithColumnIndex() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class,
                () -> mock(AbstractUnsupportedOperationSQLFederationResultSet.class, CALLS_REAL_METHODS).getRef(1));
        assertThat(ex.getMessage(), is("getRef"));
    }
    
    @Test
    void assertGetRefWithColumnLabel() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class,
                () -> mock(AbstractUnsupportedOperationSQLFederationResultSet.class, CALLS_REAL_METHODS).getRef("c"));
        assertThat(ex.getMessage(), is("getRef"));
    }
    
    @Test
    void assertGetRowIdWithColumnIndex() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class,
                () -> mock(AbstractUnsupportedOperationSQLFederationResultSet.class, CALLS_REAL_METHODS).getRowId(1));
        assertThat(ex.getMessage(), is("getRowId"));
    }
    
    @Test
    void assertGetRowIdWithColumnLabel() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class,
                () -> mock(AbstractUnsupportedOperationSQLFederationResultSet.class, CALLS_REAL_METHODS).getRowId("c"));
        assertThat(ex.getMessage(), is("getRowId"));
    }
    
    @Test
    void assertGetObjectWithTypeByColumnIndex() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class,
                () -> mock(AbstractUnsupportedOperationSQLFederationResultSet.class, CALLS_REAL_METHODS).getObject(1, Object.class));
        assertThat(ex.getMessage(), is("getObject with type"));
    }
    
    @Test
    void assertGetObjectWithTypeByColumnLabel() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class,
                () -> mock(AbstractUnsupportedOperationSQLFederationResultSet.class, CALLS_REAL_METHODS).getObject("c", Object.class));
        assertThat(ex.getMessage(), is("getObject with type"));
    }
    
    @Test
    void assertGetObjectWithMapByColumnLabel() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class,
                () -> mock(AbstractUnsupportedOperationSQLFederationResultSet.class, CALLS_REAL_METHODS).getObject("c", Collections.emptyMap()));
        assertThat(ex.getMessage(), is("getObject with map"));
    }
    
    @Test
    void assertGetObjectWithMapByColumnIndex() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class,
                () -> mock(AbstractUnsupportedOperationSQLFederationResultSet.class, CALLS_REAL_METHODS).getObject(1, Collections.emptyMap()));
        assertThat(ex.getMessage(), is("getObject with map"));
    }
}
