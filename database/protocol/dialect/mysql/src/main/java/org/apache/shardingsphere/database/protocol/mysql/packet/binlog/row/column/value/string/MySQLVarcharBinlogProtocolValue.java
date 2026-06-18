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

package org.apache.shardingsphere.database.protocol.mysql.packet.binlog.row.column.value.string;

import org.apache.shardingsphere.database.protocol.mysql.packet.binlog.row.column.MySQLBinlogColumnDef;
import org.apache.shardingsphere.database.protocol.mysql.packet.binlog.row.column.value.MySQLBinlogProtocolValue;
import org.apache.shardingsphere.database.protocol.mysql.payload.MySQLPacketPayload;

import java.io.Serializable;

/**
 * VARCHAR / VAR_STRING type value of MySQL binlog protocol.
 */
public final class MySQLVarcharBinlogProtocolValue implements MySQLBinlogProtocolValue {
    
    private static final int VARCHAR_LENGTH_META_POINT = 256;
    
    @Override
    public Serializable read(final MySQLBinlogColumnDef columnDef, final MySQLPacketPayload payload) {
        byte[] bytes = payload.readStringFixByBytes(VARCHAR_LENGTH_META_POINT > columnDef.getColumnMeta() ? payload.getByteBuf().readUnsignedByte() : payload.getByteBuf().readUnsignedShortLE());
        return new MySQLBinaryString(bytes);
    }
}
