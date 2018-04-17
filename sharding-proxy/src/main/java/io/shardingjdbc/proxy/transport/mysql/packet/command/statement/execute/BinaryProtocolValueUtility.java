/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingjdbc.proxy.transport.mysql.packet.command.statement.execute;

import io.shardingjdbc.proxy.transport.mysql.constant.ColumnType;
import io.shardingjdbc.proxy.transport.mysql.packet.MySQLPacketPayload;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Binary protocol value.
 * @see <a href="https://dev.mysql.com/doc/internals/en/binary-protocol-value.html">Binary Protocol Value</a>
 *
 * @author zhangyonglun
 */
@NoArgsConstructor(access = AccessLevel.NONE)
public final class BinaryProtocolValueUtility {
    
    private static final BinaryProtocolValueUtility INSTANCE = new BinaryProtocolValueUtility();
    
    /**
     * Get binary protocol value utility instance.
     *
     * @return binary protocol value utility
     */
    public static BinaryProtocolValueUtility getInstance() {
        return INSTANCE;
    }
    
    /**
     * Read binary protocol value.
     *
     * @param columnType column type
     * @param mysqlPacketPayload mysql packet payload
     * @return string value
     */
    public String readBinaryProtocolValue(final ColumnType columnType, final MySQLPacketPayload mysqlPacketPayload) {
        // TODO add more types
        switch (columnType) {
            case MYSQL_TYPE_LONG:
                return String.valueOf(mysqlPacketPayload.readInt4());
            default:
                return String.valueOf(mysqlPacketPayload.readStringLenenc());
        }
    }
    
    /**
     * Write binary protocol value.
     *
     * @param columnType column type
     * @param mysqlPacketPayload mysql packet pay load
     * @param stringData string data
     */
    public void writeBinaryProtocolValue(final ColumnType columnType, final MySQLPacketPayload mysqlPacketPayload, final String stringData) {
        // TODO add more types
        switch (columnType) {
            case MYSQL_TYPE_LONGLONG:
                mysqlPacketPayload.writeInt8(Long.parseLong(stringData));
                break;
            case MYSQL_TYPE_LONG:
                mysqlPacketPayload.writeInt4(Integer.parseInt(stringData));
                break;
            default:
                mysqlPacketPayload.writeStringLenenc(stringData);
        }
    }
    
    /**
     * Get value.
     *
     * @param columnType column type
     * @param value string value
     * @return object value
     */
    public Object getValue(final ColumnType columnType, final String value) {
        // TODO add more types
        switch (columnType) {
            case MYSQL_TYPE_LONG:
                return Long.parseLong(value);
            default:
                return null;
        }
    }
}
