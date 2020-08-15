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

import org.apache.shardingsphere.driver.common.base.AbstractShardingSphereDataSourceForShardingTest;
import org.apache.shardingsphere.driver.jdbc.core.connection.ShardingSphereConnection;
import org.apache.shardingsphere.driver.jdbc.util.JDBCTestSQL;
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
import java.util.Collection;
import java.util.LinkedList;

public final class UnsupportedUpdateOperationResultSetTest extends AbstractShardingSphereDataSourceForShardingTest {
    
    private final Collection<ShardingSphereConnection> shardingSphereConnections = new LinkedList<>();
    
    private final Collection<Statement> statements = new LinkedList<>();
    
    private final Collection<ResultSet> resultSets = new LinkedList<>();
    
    @Before
    public void init() throws SQLException {
        ShardingSphereConnection connection = getShardingSphereDataSource().getConnection();
        shardingSphereConnections.add(connection);
        Statement statement = connection.createStatement();
        statements.add(statement);
        ResultSet resultSet = statement.executeQuery(JDBCTestSQL.SELECT_ORDER_BY_USER_ID_SQL);
        resultSets.add(resultSet);
    }
    
    @After
    public void close() throws SQLException {
        for (ResultSet each : resultSets) {
            each.close();
        }
        for (Statement each : statements) {
            each.close();
        }
        for (ShardingSphereConnection each : shardingSphereConnections) {
            each.close();
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateNullForColumnIndex() throws SQLException {
        for (ResultSet each : resultSets) {
            each.updateNull(1);
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateNullForColumnLabel() throws SQLException {
        for (ResultSet each : resultSets) {
            each.updateNull("label");
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateBooleanForColumnIndex() throws SQLException {
        for (ResultSet each : resultSets) {
            each.updateBoolean(1, false);
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateBooleanForColumnLabel() throws SQLException {
        for (ResultSet each : resultSets) {
            each.updateBoolean("label", false);
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateByteForColumnIndex() throws SQLException {
        for (ResultSet each : resultSets) {
            each.updateByte(1, (byte) 1);
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateByteForColumnLabel() throws SQLException {
        for (ResultSet each : resultSets) {
            each.updateByte("label", (byte) 1);
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateShortForColumnIndex() throws SQLException {
        for (ResultSet each : resultSets) {
            each.updateShort(1, (short) 1);
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateShortForColumnLabel() throws SQLException {
        for (ResultSet each : resultSets) {
            each.updateShort("label", (short) 1);
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateIntForColumnIndex() throws SQLException {
        for (ResultSet each : resultSets) {
            each.updateInt(1, 1);
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateIntForColumnLabel() throws SQLException {
        for (ResultSet each : resultSets) {
            each.updateInt("label", 1);
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateLongForColumnIndex() throws SQLException {
        for (ResultSet each : resultSets) {
            each.updateLong(1, 1L);
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateLongForColumnLabel() throws SQLException {
        for (ResultSet each : resultSets) {
            each.updateLong("label", 1L);
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateFloatForColumnIndex() throws SQLException {
        for (ResultSet each : resultSets) {
            each.updateFloat(1, 1.0F);
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateFloatForColumnLabel() throws SQLException {
        for (ResultSet each : resultSets) {
            each.updateFloat("label", 1.0F);
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateDoubleForColumnIndex() throws SQLException {
        for (ResultSet each : resultSets) {
            each.updateDouble(1, 1.0D);
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateDoubleForColumnLabel() throws SQLException {
        for (ResultSet each : resultSets) {
            each.updateDouble("label", 1.0D);
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateBigDecimalForColumnIndex() throws SQLException {
        for (ResultSet each : resultSets) {
            each.updateBigDecimal(1, new BigDecimal("1"));
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateBigDecimalForColumnLabel() throws SQLException {
        for (ResultSet each : resultSets) {
            each.updateBigDecimal("label", new BigDecimal("1"));
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateStringForColumnIndex() throws SQLException {
        for (ResultSet each : resultSets) {
            each.updateString(1, "1");
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateStringForColumnLabel() throws SQLException {
        for (ResultSet each : resultSets) {
            each.updateString("label", "1");
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateNStringForColumnIndex() throws SQLException {
        for (ResultSet each : resultSets) {
            each.updateNString(1, "");
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateNStringForColumnLabel() throws SQLException {
        for (ResultSet each : resultSets) {
            each.updateNString("label", "");
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateBytesForColumnIndex() throws SQLException {
        for (ResultSet each : resultSets) {
            each.updateBytes(1, new byte[]{});
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateBytesForColumnLabel() throws SQLException {
        for (ResultSet each : resultSets) {
            each.updateBytes("label", new byte[]{});
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateDateForColumnIndex() throws SQLException {
        for (ResultSet each : resultSets) {
            each.updateDate(1, new Date(0L));
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateDateForColumnLabel() throws SQLException {
        for (ResultSet each : resultSets) {
            each.updateDate("label", new Date(0L));
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateTimeForColumnIndex() throws SQLException {
        for (ResultSet each : resultSets) {
            each.updateTime(1, new Time(0L));
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateTimeForColumnLabel() throws SQLException {
        for (ResultSet each : resultSets) {
            each.updateTime("label", new Time(0L));
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateTimestampForColumnIndex() throws SQLException {
        for (ResultSet each : resultSets) {
            each.updateTimestamp(1, new Timestamp(0L));
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateTimestampForColumnLabel() throws SQLException {
        for (ResultSet each : resultSets) {
            each.updateTimestamp("label", new Timestamp(0L));
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateAsciiStreamForColumnIndex() throws SQLException {
        for (ResultSet each : resultSets) {
            each.updateAsciiStream(1, System.in);
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateAsciiStreamForColumnLabel() throws SQLException {
        for (ResultSet each : resultSets) {
            each.updateAsciiStream("label", System.in);
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateAsciiStreamForColumnIndexWithIntegerLength() throws SQLException {
        for (ResultSet each : resultSets) {
            each.updateAsciiStream(1, System.in, 1);
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateAsciiStreamForColumnLabelWithIntegerLength() throws SQLException {
        for (ResultSet each : resultSets) {
            each.updateAsciiStream("label", System.in, 1);
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateAsciiStreamForColumnIndexWithLongLength() throws SQLException {
        for (ResultSet each : resultSets) {
            each.updateAsciiStream(1, System.in, 1L);
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateAsciiStreamForColumnLabelWithLongLength() throws SQLException {
        for (ResultSet each : resultSets) {
            each.updateAsciiStream("label", System.in, 1L);
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateBinaryStreamForColumnIndex() throws SQLException {
        for (ResultSet each : resultSets) {
            each.updateBinaryStream(1, System.in);
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateBinaryStreamForColumnLabel() throws SQLException {
        for (ResultSet each : resultSets) {
            each.updateBinaryStream("label", System.in);
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateBinaryStreamForColumnIndexWithIntegerLength() throws SQLException {
        for (ResultSet each : resultSets) {
            each.updateBinaryStream(1, System.in, 1);
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateBinaryStreamForColumnLabelWithIntegerLength() throws SQLException {
        for (ResultSet each : resultSets) {
            each.updateBinaryStream("label", System.in, 1);
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateBinaryStreamForColumnIndexWithLongLength() throws SQLException {
        for (ResultSet each : resultSets) {
            each.updateBinaryStream(1, System.in, 1L);
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateBinaryStreamForColumnLabelWithLongLength() throws SQLException {
        for (ResultSet each : resultSets) {
            each.updateBinaryStream("label", System.in, 1L);
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateCharacterStreamForColumnIndex() throws SQLException {
        for (ResultSet each : resultSets) {
            each.updateCharacterStream(1, new StringReader(""));
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateCharacterStreamForColumnLabel() throws SQLException {
        for (ResultSet each : resultSets) {
            each.updateCharacterStream("label", new StringReader(""));
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateCharacterStreamForColumnIndexWithIntegerLength() throws SQLException {
        for (ResultSet each : resultSets) {
            each.updateCharacterStream(1, new StringReader(""), 1);
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateCharacterStreamForColumnLabelWithIntegerLength() throws SQLException {
        for (ResultSet each : resultSets) {
            each.updateCharacterStream("label", new StringReader(""), 1);
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateCharacterStreamForColumnIndexWithLongLength() throws SQLException {
        for (ResultSet each : resultSets) {
            each.updateCharacterStream(1, new StringReader(""), 1L);
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateCharacterStreamForColumnLabelWithLongLength() throws SQLException {
        for (ResultSet each : resultSets) {
            each.updateCharacterStream("label", new StringReader(""), 1L);
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateNCharacterStreamForColumnIndex() throws SQLException {
        for (ResultSet each : resultSets) {
            each.updateNCharacterStream(1, new StringReader(""));
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateNCharacterStreamForColumnLabel() throws SQLException {
        for (ResultSet each : resultSets) {
            each.updateNCharacterStream("label", new StringReader(""));
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateNCharacterStreamForColumnIndexWithLength() throws SQLException {
        for (ResultSet each : resultSets) {
            each.updateNCharacterStream(1, new StringReader(""), 1);
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateNCharacterStreamForColumnLabelWithLength() throws SQLException {
        for (ResultSet each : resultSets) {
            each.updateNCharacterStream("label", new StringReader(""), 1);
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateObjectForColumnIndex() throws SQLException {
        for (ResultSet each : resultSets) {
            each.updateObject(1, new Object());
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateObjectForColumnLabel() throws SQLException {
        for (ResultSet each : resultSets) {
            each.updateObject("label", new Object());
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateObjectForColumnIndexWithScaleOrLength() throws SQLException {
        for (ResultSet each : resultSets) {
            each.updateObject(1, new Object(), 1);
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateObjectForColumnLabelWithScaleOrLength() throws SQLException {
        for (ResultSet each : resultSets) {
            each.updateObject("label", new Object(), 1);
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateRefForColumnIndex() throws SQLException {
        for (ResultSet each : resultSets) {
            each.updateRef(1, null);
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateRefForColumnLabel() throws SQLException {
        for (ResultSet each : resultSets) {
            each.updateRef("label", null);
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateBlobForColumnIndex() throws SQLException {
        for (ResultSet each : resultSets) {
            each.updateBlob(1, (Blob) null);
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateBlobForColumnLabel() throws SQLException {
        for (ResultSet each : resultSets) {
            each.updateBlob("label", (Blob) null);
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateBlobForColumnIndexWithInputStream() throws SQLException {
        for (ResultSet each : resultSets) {
            each.updateBlob(1, System.in);
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateBlobForColumnLabelWithInputStream() throws SQLException {
        for (ResultSet each : resultSets) {
            each.updateBlob("label", System.in);
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateBlobForColumnIndexWithInputStreamAndLength() throws SQLException {
        for (ResultSet each : resultSets) {
            each.updateBlob(1, System.in, 100);
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateBlobForColumnLabelWithInputStreamAndLength() throws SQLException {
        for (ResultSet each : resultSets) {
            each.updateBlob("label", System.in, 100);
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateClobForColumnIndex() throws SQLException {
        for (ResultSet each : resultSets) {
            each.updateClob(1, (Clob) null);
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateClobForColumnLabel() throws SQLException {
        for (ResultSet each : resultSets) {
            each.updateClob("label", (Clob) null);
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateClobForColumnIndexWithInputStream() throws SQLException {
        for (ResultSet each : resultSets) {
            each.updateClob(1, new StringReader(""));
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateClobForColumnLabelWithInputStream() throws SQLException {
        for (ResultSet each : resultSets) {
            each.updateClob("label", new StringReader(""));
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateClobForColumnIndexWithInputStreamAndLength() throws SQLException {
        for (ResultSet each : resultSets) {
            each.updateClob(1, new StringReader(""), 100);
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateClobForColumnLabelWithInputStreamAndLength() throws SQLException {
        for (ResultSet each : resultSets) {
            each.updateClob("label", new StringReader(""), 100);
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateNClobForColumnIndex() throws SQLException {
        for (ResultSet each : resultSets) {
            each.updateNClob(1, (NClob) null);
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateNClobForColumnLabel() throws SQLException {
        for (ResultSet each : resultSets) {
            each.updateNClob("label", (NClob) null);
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateNClobForColumnIndexWithInputStream() throws SQLException {
        for (ResultSet each : resultSets) {
            each.updateNClob(1, new StringReader(""));
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateNClobForColumnLabelWithInputStream() throws SQLException {
        for (ResultSet each : resultSets) {
            each.updateNClob("label", new StringReader(""));
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateNClobForColumnIndexWithInputStreamAndLength() throws SQLException {
        for (ResultSet each : resultSets) {
            each.updateNClob(1, new StringReader(""), 100);
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateNClobForColumnLabelWithInputStreamAndLength() throws SQLException {
        for (ResultSet each : resultSets) {
            each.updateNClob("label", new StringReader(""), 100);
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateArrayForColumnIndex() throws SQLException {
        for (ResultSet each : resultSets) {
            each.updateArray(1, null);
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateArrayForColumnLabel() throws SQLException {
        for (ResultSet each : resultSets) {
            each.updateArray("label", null);
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateRowIdForColumnIndex() throws SQLException {
        for (ResultSet each : resultSets) {
            each.updateRowId(1, null);
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateRowIdForColumnLabel() throws SQLException {
        for (ResultSet each : resultSets) {
            each.updateRowId("label", null);
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateSQLXMLForColumnIndex() throws SQLException {
        for (ResultSet each : resultSets) {
            each.updateSQLXML(1, null);
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateSQXMLForColumnLabel() throws SQLException {
        for (ResultSet each : resultSets) {
            each.updateSQLXML("label", null);
        }
    }
}
