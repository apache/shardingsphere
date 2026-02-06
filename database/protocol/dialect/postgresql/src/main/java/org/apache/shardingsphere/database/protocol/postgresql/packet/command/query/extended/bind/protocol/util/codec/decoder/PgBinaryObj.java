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

package org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.extended.bind.protocol.util.codec.decoder;

import org.postgresql.util.PGBinaryObject;
import org.postgresql.util.PGobject;

import java.sql.SQLException;
import java.util.Arrays;

/**
 * Cusmtom PgBinaryObj.
 */
public class PgBinaryObj extends PGobject implements PGBinaryObject {
    
    private byte[] bytes;
    
    public PgBinaryObj(final byte[] value) {
        bytes = value;
    }
    
    @Override
    public void setByteValue(final byte[] value, final int offset) throws SQLException {
        if (value == null) {
            bytes = new byte[0];
        } else {
            if (offset < 0 || offset > value.length) {
                throw new SQLException("Invalid offset: " + offset);
            }
            bytes = Arrays.copyOfRange(value, offset, value.length);
        }
    }
    
    @Override
    public int lengthInBytes() {
        return bytes.length;
    }
    
    @Override
    public void toBytes(final byte[] target, final int offset) {
        if (target == null) {
            throw new IllegalArgumentException("Target array cannot be null");
        }
        if (offset < 0 || offset + bytes.length > target.length) {
            throw new IllegalArgumentException("Target array too small or invalid offset");
        }
        System.arraycopy(bytes, 0, target, offset, bytes.length);
    }
}
