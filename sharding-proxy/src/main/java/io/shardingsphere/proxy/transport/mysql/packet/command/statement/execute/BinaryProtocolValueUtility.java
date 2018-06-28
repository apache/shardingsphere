/*
 * Copyright 2016-2018 shardingsphere.io.
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

package io.shardingsphere.proxy.transport.mysql.packet.command.statement.execute;

import io.shardingsphere.proxy.transport.mysql.constant.ColumnType;
import io.shardingsphere.proxy.transport.mysql.packet.MySQLPacketPayload;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.util.Date;

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
     * @return object value
     */
    public Object readBinaryProtocolValue(final ColumnType columnType, final MySQLPacketPayload mysqlPacketPayload) {
        switch (columnType) {
            case MYSQL_TYPE_STRING:
            case MYSQL_TYPE_VARCHAR:
            case MYSQL_TYPE_VAR_STRING:
            case MYSQL_TYPE_ENUM:
            case MYSQL_TYPE_SET:
            case MYSQL_TYPE_LONG_BLOB:
            case MYSQL_TYPE_MEDIUM_BLOB:
            case MYSQL_TYPE_BLOB:
            case MYSQL_TYPE_TINY_BLOB:
            case MYSQL_TYPE_GEOMETRY:
            case MYSQL_TYPE_BIT:
            case MYSQL_TYPE_DECIMAL:
            case MYSQL_TYPE_NEWDECIMAL:
                return mysqlPacketPayload.readStringLenenc();
            case MYSQL_TYPE_LONGLONG:
                return mysqlPacketPayload.readInt8();
            case MYSQL_TYPE_LONG:
            case MYSQL_TYPE_INT24:
                return mysqlPacketPayload.readInt4();
            case MYSQL_TYPE_SHORT:
            case MYSQL_TYPE_YEAR:
                return mysqlPacketPayload.readInt2();
            case MYSQL_TYPE_TINY:
                return mysqlPacketPayload.readInt1();
            case MYSQL_TYPE_DOUBLE:
                return mysqlPacketPayload.readDouble();
            case MYSQL_TYPE_FLOAT:
                return mysqlPacketPayload.readFloat();
            case MYSQL_TYPE_DATE:
            case MYSQL_TYPE_DATETIME:
            case MYSQL_TYPE_TIMESTAMP:
                return mysqlPacketPayload.readDate();
            case MYSQL_TYPE_TIME:
                return mysqlPacketPayload.readTime();
            default:
                throw new IllegalArgumentException(String.format("Cannot find MYSQL type '%s' in column type when read binary protocol value", columnType));
        }
    }
    
    /**
     * Write binary protocol value.
     *
     * @param columnType column type
     * @param mysqlPacketPayload mysql packet pay load
     * @param objectData object data
     */
    public void writeBinaryProtocolValue(final ColumnType columnType, final Object objectData, final MySQLPacketPayload mysqlPacketPayload) {
        switch (columnType) {
            case MYSQL_TYPE_STRING:
            case MYSQL_TYPE_VARCHAR:
            case MYSQL_TYPE_VAR_STRING:
            case MYSQL_TYPE_ENUM:
            case MYSQL_TYPE_SET:
            case MYSQL_TYPE_LONG_BLOB:
            case MYSQL_TYPE_MEDIUM_BLOB:
            case MYSQL_TYPE_BLOB:
            case MYSQL_TYPE_TINY_BLOB:
            case MYSQL_TYPE_GEOMETRY:
            case MYSQL_TYPE_BIT:
            case MYSQL_TYPE_DECIMAL:
            case MYSQL_TYPE_NEWDECIMAL:
                mysqlPacketPayload.writeStringLenenc(objectData.toString());
                break;
            case MYSQL_TYPE_LONGLONG:
                mysqlPacketPayload.writeInt8((Long) objectData);
                break;
            case MYSQL_TYPE_LONG:
            case MYSQL_TYPE_INT24:
                mysqlPacketPayload.writeInt4((Integer) objectData);
                break;
            case MYSQL_TYPE_SHORT:
            case MYSQL_TYPE_YEAR:
                mysqlPacketPayload.writeInt2((Integer) objectData);
                break;
            case MYSQL_TYPE_TINY:
                mysqlPacketPayload.writeInt1((Integer) objectData);
                break;
            case MYSQL_TYPE_DOUBLE:
                mysqlPacketPayload.writeDouble(Double.parseDouble(objectData.toString()));
                break;
            case MYSQL_TYPE_FLOAT:
                mysqlPacketPayload.writeFloat(Float.parseFloat(objectData.toString()));
                break;
            case MYSQL_TYPE_DATE:
            case MYSQL_TYPE_DATETIME:
            case MYSQL_TYPE_TIMESTAMP:
                mysqlPacketPayload.writeDate((Timestamp) objectData);
                break;
            case MYSQL_TYPE_TIME:
                mysqlPacketPayload.writeTime((Date) objectData);
                break;
            default:
                throw new IllegalArgumentException(String.format("Cannot find MYSQL type '%s' in column type when write binary protocol value", columnType));
        }
    }
}
