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

package org.apache.shardingsphere.driver.jdbc.unsupported;

import org.apache.shardingsphere.driver.jdbc.core.resultset.ShardingSphereResultSet;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionContext;
import org.apache.shardingsphere.infra.merge.result.MergedResult;
import org.junit.Before;
import org.junit.Test;

import java.io.StringReader;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.NClob;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Collections;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

public final class UnsupportedUpdateOperationResultSetTest {
    
    private ShardingSphereResultSet shardingSphereResultSet;
    
    @Before
    public void init() throws SQLException {
        shardingSphereResultSet = new ShardingSphereResultSet(
                Collections.singletonList(mock(ResultSet.class, RETURNS_DEEP_STUBS)), mock(MergedResult.class), mock(Statement.class), mock(ExecutionContext.class));
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateNullForColumnIndex() throws SQLException {
        shardingSphereResultSet.updateNull(1);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateNullForColumnLabel() throws SQLException {
        shardingSphereResultSet.updateNull("label");
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateBooleanForColumnIndex() throws SQLException {
        shardingSphereResultSet.updateBoolean(1, false);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateBooleanForColumnLabel() throws SQLException {
        shardingSphereResultSet.updateBoolean("label", false);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateByteForColumnIndex() throws SQLException {
        shardingSphereResultSet.updateByte(1, (byte) 1);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateByteForColumnLabel() throws SQLException {
        shardingSphereResultSet.updateByte("label", (byte) 1);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateShortForColumnIndex() throws SQLException {
        shardingSphereResultSet.updateShort(1, (short) 1);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateShortForColumnLabel() throws SQLException {
        shardingSphereResultSet.updateShort("label", (short) 1);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateIntForColumnIndex() throws SQLException {
        shardingSphereResultSet.updateInt(1, 1);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateIntForColumnLabel() throws SQLException {
        shardingSphereResultSet.updateInt("label", 1);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateLongForColumnIndex() throws SQLException {
        shardingSphereResultSet.updateLong(1, 1L);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateLongForColumnLabel() throws SQLException {
        shardingSphereResultSet.updateLong("label", 1L);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateFloatForColumnIndex() throws SQLException {
        shardingSphereResultSet.updateFloat(1, 1.0F);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateFloatForColumnLabel() throws SQLException {
        shardingSphereResultSet.updateFloat("label", 1.0F);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateDoubleForColumnIndex() throws SQLException {
        shardingSphereResultSet.updateDouble(1, 1.0D);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateDoubleForColumnLabel() throws SQLException {
        shardingSphereResultSet.updateDouble("label", 1.0D);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateBigDecimalForColumnIndex() throws SQLException {
        shardingSphereResultSet.updateBigDecimal(1, new BigDecimal("1"));
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateBigDecimalForColumnLabel() throws SQLException {
        shardingSphereResultSet.updateBigDecimal("label", new BigDecimal("1"));
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateStringForColumnIndex() throws SQLException {
        shardingSphereResultSet.updateString(1, "1");
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateStringForColumnLabel() throws SQLException {
        shardingSphereResultSet.updateString("label", "1");
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateNStringForColumnIndex() throws SQLException {
        shardingSphereResultSet.updateNString(1, "");
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateNStringForColumnLabel() throws SQLException {
        shardingSphereResultSet.updateNString("label", "");
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateBytesForColumnIndex() throws SQLException {
        shardingSphereResultSet.updateBytes(1, new byte[]{});
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateBytesForColumnLabel() throws SQLException {
        shardingSphereResultSet.updateBytes("label", new byte[]{});
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateDateForColumnIndex() throws SQLException {
        shardingSphereResultSet.updateDate(1, new Date(0L));
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateDateForColumnLabel() throws SQLException {
        shardingSphereResultSet.updateDate("label", new Date(0L));
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateTimeForColumnIndex() throws SQLException {
        shardingSphereResultSet.updateTime(1, new Time(0L));
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateTimeForColumnLabel() throws SQLException {
        shardingSphereResultSet.updateTime("label", new Time(0L));
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateTimestampForColumnIndex() throws SQLException {
        shardingSphereResultSet.updateTimestamp(1, new Timestamp(0L));
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateTimestampForColumnLabel() throws SQLException {
        shardingSphereResultSet.updateTimestamp("label", new Timestamp(0L));
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateAsciiStreamForColumnIndex() throws SQLException {
        shardingSphereResultSet.updateAsciiStream(1, System.in);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateAsciiStreamForColumnLabel() throws SQLException {
        shardingSphereResultSet.updateAsciiStream("label", System.in);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateAsciiStreamForColumnIndexWithIntegerLength() throws SQLException {
        shardingSphereResultSet.updateAsciiStream(1, System.in, 1);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateAsciiStreamForColumnLabelWithIntegerLength() throws SQLException {
        shardingSphereResultSet.updateAsciiStream("label", System.in, 1);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateAsciiStreamForColumnIndexWithLongLength() throws SQLException {
        shardingSphereResultSet.updateAsciiStream(1, System.in, 1L);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateAsciiStreamForColumnLabelWithLongLength() throws SQLException {
        shardingSphereResultSet.updateAsciiStream("label", System.in, 1L);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateBinaryStreamForColumnIndex() throws SQLException {
        shardingSphereResultSet.updateBinaryStream(1, System.in);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateBinaryStreamForColumnLabel() throws SQLException {
        shardingSphereResultSet.updateBinaryStream("label", System.in);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateBinaryStreamForColumnIndexWithIntegerLength() throws SQLException {
        shardingSphereResultSet.updateBinaryStream(1, System.in, 1);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateBinaryStreamForColumnLabelWithIntegerLength() throws SQLException {
        shardingSphereResultSet.updateBinaryStream("label", System.in, 1);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateBinaryStreamForColumnIndexWithLongLength() throws SQLException {
        shardingSphereResultSet.updateBinaryStream(1, System.in, 1L);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateBinaryStreamForColumnLabelWithLongLength() throws SQLException {
        shardingSphereResultSet.updateBinaryStream("label", System.in, 1L);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateCharacterStreamForColumnIndex() throws SQLException {
        shardingSphereResultSet.updateCharacterStream(1, new StringReader(""));
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateCharacterStreamForColumnLabel() throws SQLException {
        shardingSphereResultSet.updateCharacterStream("label", new StringReader(""));
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateCharacterStreamForColumnIndexWithIntegerLength() throws SQLException {
        shardingSphereResultSet.updateCharacterStream(1, new StringReader(""), 1);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateCharacterStreamForColumnLabelWithIntegerLength() throws SQLException {
        shardingSphereResultSet.updateCharacterStream("label", new StringReader(""), 1);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateCharacterStreamForColumnIndexWithLongLength() throws SQLException {
        shardingSphereResultSet.updateCharacterStream(1, new StringReader(""), 1L);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateCharacterStreamForColumnLabelWithLongLength() throws SQLException {
        shardingSphereResultSet.updateCharacterStream("label", new StringReader(""), 1L);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateNCharacterStreamForColumnIndex() throws SQLException {
        shardingSphereResultSet.updateNCharacterStream(1, new StringReader(""));
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateNCharacterStreamForColumnLabel() throws SQLException {
        shardingSphereResultSet.updateNCharacterStream("label", new StringReader(""));
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateNCharacterStreamForColumnIndexWithLength() throws SQLException {
        shardingSphereResultSet.updateNCharacterStream(1, new StringReader(""), 1);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateNCharacterStreamForColumnLabelWithLength() throws SQLException {
        shardingSphereResultSet.updateNCharacterStream("label", new StringReader(""), 1);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateObjectForColumnIndex() throws SQLException {
        shardingSphereResultSet.updateObject(1, new Object());
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateObjectForColumnLabel() throws SQLException {
        shardingSphereResultSet.updateObject("label", new Object());
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateObjectForColumnIndexWithScaleOrLength() throws SQLException {
        shardingSphereResultSet.updateObject(1, new Object(), 1);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateObjectForColumnLabelWithScaleOrLength() throws SQLException {
        shardingSphereResultSet.updateObject("label", new Object(), 1);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateRefForColumnIndex() throws SQLException {
        shardingSphereResultSet.updateRef(1, null);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateRefForColumnLabel() throws SQLException {
        shardingSphereResultSet.updateRef("label", null);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateBlobForColumnIndex() throws SQLException {
        shardingSphereResultSet.updateBlob(1, (Blob) null);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateBlobForColumnLabel() throws SQLException {
        shardingSphereResultSet.updateBlob("label", (Blob) null);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateBlobForColumnIndexWithInputStream() throws SQLException {
        shardingSphereResultSet.updateBlob(1, System.in);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateBlobForColumnLabelWithInputStream() throws SQLException {
        shardingSphereResultSet.updateBlob("label", System.in);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateBlobForColumnIndexWithInputStreamAndLength() throws SQLException {
        shardingSphereResultSet.updateBlob(1, System.in, 100);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateBlobForColumnLabelWithInputStreamAndLength() throws SQLException {
        shardingSphereResultSet.updateBlob("label", System.in, 100);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateClobForColumnIndex() throws SQLException {
        shardingSphereResultSet.updateClob(1, (Clob) null);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateClobForColumnLabel() throws SQLException {
        shardingSphereResultSet.updateClob("label", (Clob) null);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateClobForColumnIndexWithInputStream() throws SQLException {
        shardingSphereResultSet.updateClob(1, new StringReader(""));
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateClobForColumnLabelWithInputStream() throws SQLException {
        shardingSphereResultSet.updateClob("label", new StringReader(""));
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateClobForColumnIndexWithInputStreamAndLength() throws SQLException {
        shardingSphereResultSet.updateClob(1, new StringReader(""), 100);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateClobForColumnLabelWithInputStreamAndLength() throws SQLException {
        shardingSphereResultSet.updateClob("label", new StringReader(""), 100);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateNClobForColumnIndex() throws SQLException {
        shardingSphereResultSet.updateNClob(1, (NClob) null);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateNClobForColumnLabel() throws SQLException {
        shardingSphereResultSet.updateNClob("label", (NClob) null);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateNClobForColumnIndexWithInputStream() throws SQLException {
        shardingSphereResultSet.updateNClob(1, new StringReader(""));
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateNClobForColumnLabelWithInputStream() throws SQLException {
        shardingSphereResultSet.updateNClob("label", new StringReader(""));
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateNClobForColumnIndexWithInputStreamAndLength() throws SQLException {
        shardingSphereResultSet.updateNClob(1, new StringReader(""), 100);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateNClobForColumnLabelWithInputStreamAndLength() throws SQLException {
        shardingSphereResultSet.updateNClob("label", new StringReader(""), 100);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateArrayForColumnIndex() throws SQLException {
        shardingSphereResultSet.updateArray(1, null);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateArrayForColumnLabel() throws SQLException {
        shardingSphereResultSet.updateArray("label", null);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateRowIdForColumnIndex() throws SQLException {
        shardingSphereResultSet.updateRowId(1, null);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateRowIdForColumnLabel() throws SQLException {
        shardingSphereResultSet.updateRowId("label", null);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateSQLXMLForColumnIndex() throws SQLException {
        shardingSphereResultSet.updateSQLXML(1, null);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateSQXMLForColumnLabel() throws SQLException {
        shardingSphereResultSet.updateSQLXML("label", null);
    }
}
