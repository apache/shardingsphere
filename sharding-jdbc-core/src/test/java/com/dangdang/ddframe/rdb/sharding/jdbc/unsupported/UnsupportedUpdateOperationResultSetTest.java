/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.rdb.sharding.jdbc.unsupported;

import com.dangdang.ddframe.rdb.integrate.db.AbstractShardingDatabaseOnlyDBUnitTest;
import com.dangdang.ddframe.rdb.sharding.jdbc.core.connection.ShardingConnection;
import org.junit.After;
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

public final class UnsupportedUpdateOperationResultSetTest extends AbstractShardingDatabaseOnlyDBUnitTest {
    
    private ShardingConnection shardingConnection;
    
    private Statement statement;
    
    private ResultSet actual;
    
    @Before
    public void init() throws SQLException {
        shardingConnection = getShardingDataSource().getConnection();
        statement = shardingConnection.createStatement();
        actual = statement.executeQuery(getDatabaseTestSQL().getSelectUserIdByStatusSql());
    }
    
    @After
    public void close() throws SQLException {
        actual.close();
        statement.close();
        shardingConnection.close();
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateNullForColumnIndex() throws SQLException {
        actual.updateNull(1);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateNullForColumnLabel() throws SQLException {
        actual.updateNull("label");
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateBooleanForColumnIndex() throws SQLException {
        actual.updateBoolean(1, false);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateBooleanForColumnLabel() throws SQLException {
        actual.updateBoolean("label", false);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateByteForColumnIndex() throws SQLException {
        actual.updateByte(1, (byte) 1);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateByteForColumnLabel() throws SQLException {
        actual.updateByte("label", (byte) 1);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateShortForColumnIndex() throws SQLException {
        actual.updateShort(1, (short) 1);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateShortForColumnLabel() throws SQLException {
        actual.updateShort("label", (short) 1);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateIntForColumnIndex() throws SQLException {
        actual.updateInt(1, 1);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateIntForColumnLabel() throws SQLException {
        actual.updateInt("label", 1);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateLongForColumnIndex() throws SQLException {
        actual.updateLong(1, 1L);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateLongForColumnLabel() throws SQLException {
        actual.updateLong("label", 1L);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateFloatForColumnIndex() throws SQLException {
        actual.updateFloat(1, 1F);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateFloatForColumnLabel() throws SQLException {
        actual.updateFloat("label", 1F);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateDoubleForColumnIndex() throws SQLException {
        actual.updateDouble(1, 1D);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateDoubleForColumnLabel() throws SQLException {
        actual.updateDouble("label", 1D);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateBigDecimalForColumnIndex() throws SQLException {
        actual.updateBigDecimal(1, new BigDecimal("1"));
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateBigDecimalForColumnLabel() throws SQLException {
        actual.updateBigDecimal("label", new BigDecimal("1"));
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateStringForColumnIndex() throws SQLException {
        actual.updateString(1, "1");
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateStringForColumnLabel() throws SQLException {
        actual.updateString("label", "1");
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateNStringForColumnIndex() throws SQLException {
        actual.updateNString(1, "");
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateNStringForColumnLabel() throws SQLException {
        actual.updateNString("label", "");
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateBytesForColumnIndex() throws SQLException {
        actual.updateBytes(1, new byte[] {});
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateBytesForColumnLabel() throws SQLException {
        actual.updateBytes("label", new byte[] {});
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateDateForColumnIndex() throws SQLException {
        actual.updateDate(1, new Date(0L));
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateDateForColumnLabel() throws SQLException {
        actual.updateDate("label", new Date(0L));
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateTimeForColumnIndex() throws SQLException {
        actual.updateTime(1, new Time(0L));
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateTimeForColumnLabel() throws SQLException {
        actual.updateTime("label", new Time(0L));
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateTimestampForColumnIndex() throws SQLException {
        actual.updateTimestamp(1, new Timestamp(0L));
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateTimestampForColumnLabel() throws SQLException {
        actual.updateTimestamp("label", new Timestamp(0L));
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateAsciiStreamForColumnIndex() throws SQLException {
        actual.updateAsciiStream(1, System.in);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateAsciiStreamForColumnLabel() throws SQLException {
        actual.updateAsciiStream("label", System.in);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateAsciiStreamForColumnIndexWithIntegerLength() throws SQLException {
        actual.updateAsciiStream(1, System.in, 1);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateAsciiStreamForColumnLabelWithIntegerLength() throws SQLException {
        actual.updateAsciiStream("label", System.in, 1);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateAsciiStreamForColumnIndexWithLongLength() throws SQLException {
        actual.updateAsciiStream(1, System.in, 1L);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateAsciiStreamForColumnLabelWithLongLength() throws SQLException {
        actual.updateAsciiStream("label", System.in, 1L);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateBinaryStreamForColumnIndex() throws SQLException {
        actual.updateBinaryStream(1, System.in);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateBinaryStreamForColumnLabel() throws SQLException {
        actual.updateBinaryStream("label", System.in);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateBinaryStreamForColumnIndexWithIntegerLength() throws SQLException {
        actual.updateBinaryStream(1, System.in, 1);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateBinaryStreamForColumnLabelWithIntegerLength() throws SQLException {
        actual.updateBinaryStream("label", System.in, 1);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateBinaryStreamForColumnIndexWithLongLength() throws SQLException {
        actual.updateBinaryStream(1, System.in, 1L);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateBinaryStreamForColumnLabelWithLongLength() throws SQLException {
        actual.updateBinaryStream("label", System.in, 1L);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateCharacterStreamForColumnIndex() throws SQLException {
        actual.updateCharacterStream(1, new StringReader(""));
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateCharacterStreamForColumnLabel() throws SQLException {
        actual.updateCharacterStream("label", new StringReader(""));
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateCharacterStreamForColumnIndexWithIntegerLength() throws SQLException {
        actual.updateCharacterStream(1, new StringReader(""), 1);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateCharacterStreamForColumnLabelWithIntegerLength() throws SQLException {
        actual.updateCharacterStream("label", new StringReader(""), 1);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateCharacterStreamForColumnIndexWithLongLength() throws SQLException {
        actual.updateCharacterStream(1, new StringReader(""), 1L);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateCharacterStreamForColumnLabelWithLongLength() throws SQLException {
        actual.updateCharacterStream("label", new StringReader(""), 1L);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateNCharacterStreamForColumnIndex() throws SQLException {
        actual.updateNCharacterStream(1, new StringReader(""));
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateNCharacterStreamForColumnLabel() throws SQLException {
        actual.updateNCharacterStream("label", new StringReader(""));
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateNCharacterStreamForColumnIndexWithLength() throws SQLException {
        actual.updateNCharacterStream(1, new StringReader(""), 1);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateNCharacterStreamForColumnLabelWithLength() throws SQLException {
        actual.updateNCharacterStream("label", new StringReader(""), 1);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateObjectForColumnIndex() throws SQLException {
        actual.updateObject(1, new Object());
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateObjectForColumnLabel() throws SQLException {
        actual.updateObject("label", new Object());
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateObjectForColumnIndexWithScaleOrLength() throws SQLException {
        actual.updateObject(1, new Object(), 1);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateObjectForColumnLabelWithScaleOrLength() throws SQLException {
        actual.updateObject("label", new Object(), 1);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateRefForColumnIndex() throws SQLException {
        actual.updateRef(1, null);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateRefForColumnLabel() throws SQLException {
        actual.updateRef("label", null);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateBlobForColumnIndex() throws SQLException {
        actual.updateBlob(1, (Blob) null);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateBlobForColumnLabel() throws SQLException {
        actual.updateBlob("label", (Blob) null);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateBlobForColumnIndexWithInputStream() throws SQLException {
        actual.updateBlob(1, System.in);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateBlobForColumnLabelWithInputStream() throws SQLException {
        actual.updateBlob("label", System.in);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateBlobForColumnIndexWithInputStreamAndLength() throws SQLException {
        actual.updateBlob(1, System.in, 100);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateBlobForColumnLabelWithInputStreamAndLength() throws SQLException {
        actual.updateBlob("label", System.in, 100);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateClobForColumnIndex() throws SQLException {
        actual.updateClob(1, (Clob) null);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateClobForColumnLabel() throws SQLException {
        actual.updateClob("label", (Clob) null);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateClobForColumnIndexWithInputStream() throws SQLException {
        actual.updateClob(1, new StringReader(""));
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateClobForColumnLabelWithInputStream() throws SQLException {
        actual.updateClob("label", new StringReader(""));
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateClobForColumnIndexWithInputStreamAndLength() throws SQLException {
        actual.updateClob(1, new StringReader(""), 100);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateClobForColumnLabelWithInputStreamAndLength() throws SQLException {
        actual.updateClob("label", new StringReader(""), 100);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateNClobForColumnIndex() throws SQLException {
        actual.updateNClob(1, (NClob) null);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateNClobForColumnLabel() throws SQLException {
        actual.updateNClob("label", (NClob) null);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateNClobForColumnIndexWithInputStream() throws SQLException {
        actual.updateNClob(1, new StringReader(""));
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateNClobForColumnLabelWithInputStream() throws SQLException {
        actual.updateNClob("label", new StringReader(""));
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateNClobForColumnIndexWithInputStreamAndLength() throws SQLException {
        actual.updateNClob(1, new StringReader(""), 100);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateNClobForColumnLabelWithInputStreamAndLength() throws SQLException {
        actual.updateNClob("label", new StringReader(""), 100);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateArrayForColumnIndex() throws SQLException {
        actual.updateArray(1, null);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateArrayForColumnLabel() throws SQLException {
        actual.updateArray("label", null);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateRowIdForColumnIndex() throws SQLException {
        actual.updateRowId(1, null);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateRowIdForColumnLabel() throws SQLException {
        actual.updateRowId("label", null);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateSQLXMLForColumnIndex() throws SQLException {
        actual.updateSQLXML(1, null);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateSQXMLForColumnLabel() throws SQLException {
        actual.updateSQLXML("label", null);
    }
}
