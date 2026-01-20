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

package org.apache.shardingsphere.database.connector.oracle.statement;

import org.apache.shardingsphere.database.connector.core.statement.DialectPreparedStatementParameterReplayer;
import org.apache.shardingsphere.database.connector.core.statement.PreparedStatementParameter;
import java.io.InputStream;
import java.io.Reader;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Oracle prepared statement parameter replayer.
 * Handles Oracle-specific BLOB/InputStream parameter issues with ojdbc.
 */
public final class OraclePreparedStatementParameterReplayer implements DialectPreparedStatementParameterReplayer {
    
    @Override
    public void replay(final PreparedStatement preparedStatement, final PreparedStatementParameter parameter) throws SQLException {
        int index = parameter.getIndex();
        Object value = parameter.getValue();
        long length = parameter.getLength();

        switch (parameter.getSetterMethodType()) {
            case SET_NULL:
                preparedStatement.setObject(index, null);
                break;
            case SET_BLOB:
                preparedStatement.setBlob(index, (Blob) value);
                break;
            case SET_CLOB:
                preparedStatement.setClob(index, (Clob) value);
                break;
            case SET_CLOB_READER:
                replayClob(preparedStatement, index, (Reader) value, length);
                break;
            case SET_BLOB_INPUT_STREAM:
                replayBlobInputStream(preparedStatement, index, (InputStream) value, length);
                break;
            case SET_BINARY_STREAM:
                replayBinaryStream(preparedStatement, index, (InputStream) value, length);
                break;
            case SET_ASCII_STREAM:
                replayAsciiStream(preparedStatement, index, (InputStream) value, length);
                break;
            case SET_CHARACTER_STREAM:
                replayCharacterStream(preparedStatement, index, (Reader) value, length);
                break;
            case SET_BYTES:
                preparedStatement.setBytes(index, (byte[]) value);
                break;
            default:
                preparedStatement.setObject(index, value);
        }
    }
    
    private void replayBlobInputStream(final PreparedStatement preparedStatement, final int index, final InputStream value, final long length) throws SQLException {
        if (length > 0) {
            preparedStatement.setBlob(index, value, length);
        } else {
            preparedStatement.setBlob(index, value);
        }
    }
    
    private void replayBinaryStream(final PreparedStatement preparedStatement, final int index, final InputStream value, final long length) throws SQLException {
        if (length > 0) {
            preparedStatement.setBinaryStream(index, value, length);
        } else {
            preparedStatement.setBinaryStream(index, value);
        }
    }
    
    private void replayClob(final PreparedStatement preparedStatement, final int index, final Reader value, final long length) throws SQLException {
        if (length > 0) {
            preparedStatement.setClob(index, value, length);
        } else {
            preparedStatement.setClob(index, value);
        }
    }
    
    private void replayAsciiStream(final PreparedStatement preparedStatement, final int index, final InputStream value, final long length) throws SQLException {
        if (length > 0) {
            preparedStatement.setAsciiStream(index, value, length);
        } else {
            preparedStatement.setAsciiStream(index, value);
        }
    }
    
    private void replayCharacterStream(final PreparedStatement preparedStatement, final int index, final Reader value, final long length) throws SQLException {
        if (length > 0) {
            preparedStatement.setCharacterStream(index, value, length);
        } else {
            preparedStatement.setCharacterStream(index, value);
        }
    }
    
    @Override
    public String getDatabaseType() {
        return "Oracle";
    }
}
