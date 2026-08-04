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

package org.apache.shardingsphere.database.protocol.mysql.packet.command.query.binary.execute.protocol;

import com.google.common.io.ByteStreams;
import com.google.common.io.CharStreams;
import org.apache.shardingsphere.database.protocol.mysql.payload.MySQLPacketPayload;
import org.apache.shardingsphere.infra.exception.generic.UnknownSQLException;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.SQLException;

/**
 * Binary protocol value for byte lenenc for MySQL. Actually this is string lenenc, but converting to {@link String} may corrupt the raw bytes.
 */
public final class MySQLByteLenencBinaryProtocolValue implements MySQLBinaryProtocolValue {
    
    @Override
    public Object read(final MySQLPacketPayload payload, final boolean unsigned) {
        return payload.readStringLenencByBytes();
    }
    
    @Override
    public void write(final MySQLPacketPayload payload, final Object value) {
        if (value instanceof byte[]) {
            payload.writeBytesLenenc((byte[]) value);
        } else if (value instanceof Blob) {
            payload.writeBytesLenenc(readBlob((Blob) value));
        } else if (value instanceof Clob) {
            payload.writeStringLenenc(readClob((Clob) value));
        } else {
            payload.writeStringLenenc(value.toString());
        }
    }
    
    private byte[] readBlob(final Blob value) {
        try (InputStream inputStream = value.getBinaryStream()) {
            return ByteStreams.toByteArray(inputStream);
        } catch (final IOException | SQLException ex) {
            throw new UnknownSQLException(ex);
        }
    }
    
    private String readClob(final Clob value) {
        try (Reader reader = value.getCharacterStream()) {
            return CharStreams.toString(reader);
        } catch (final IOException | SQLException ex) {
            throw new UnknownSQLException(ex);
        }
    }
}
