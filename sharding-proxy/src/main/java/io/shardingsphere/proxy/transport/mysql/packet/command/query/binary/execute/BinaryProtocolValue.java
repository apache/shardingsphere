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

package io.shardingsphere.proxy.transport.mysql.packet.command.query.binary.execute;

import io.shardingsphere.proxy.transport.mysql.constant.ColumnType;
import io.shardingsphere.proxy.transport.mysql.packet.MySQLPacketPayload;
import lombok.RequiredArgsConstructor;

import java.sql.Timestamp;
import java.util.Date;

/**
 * Binary protocol value.
 * 
 * @see <a href="https://dev.mysql.com/doc/internals/en/binary-protocol-value.html">Binary Protocol Value</a>
 *
 * @author zhangyonglun
 */
@RequiredArgsConstructor
public final class BinaryProtocolValue {
    
    private final ColumnType columnType;
    
    private final MySQLPacketPayload payload;
    
    /**
     * Read binary protocol value.
     *
     * @return binary value result
     */
    public Object read() {
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
                return payload.readStringLenenc();
            case MYSQL_TYPE_LONGLONG:
                return payload.readInt8();
            case MYSQL_TYPE_LONG:
            case MYSQL_TYPE_INT24:
                return payload.readInt4();
            case MYSQL_TYPE_SHORT:
            case MYSQL_TYPE_YEAR:
                return payload.readInt2();
            case MYSQL_TYPE_TINY:
                return payload.readInt1();
            case MYSQL_TYPE_DOUBLE:
                return payload.readDouble();
            case MYSQL_TYPE_FLOAT:
                return payload.readFloat();
            case MYSQL_TYPE_DATE:
            case MYSQL_TYPE_DATETIME:
            case MYSQL_TYPE_TIMESTAMP:
                return payload.readDate();
            case MYSQL_TYPE_TIME:
                return payload.readTime();
            default:
                throw new IllegalArgumentException(String.format("Cannot find MySQL type '%s' in column type when read binary protocol value", columnType));
        }
    }
    
    /**
     * Write binary protocol value.
     *
     * @param binaryData binary data to be written
     */
    public void write(final Object binaryData) {
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
                payload.writeStringLenenc(binaryData.toString());
                break;
            case MYSQL_TYPE_LONGLONG:
                payload.writeInt8((Long) binaryData);
                break;
            case MYSQL_TYPE_LONG:
            case MYSQL_TYPE_INT24:
                payload.writeInt4((Integer) binaryData);
                break;
            case MYSQL_TYPE_SHORT:
            case MYSQL_TYPE_YEAR:
                payload.writeInt2((Integer) binaryData);
                break;
            case MYSQL_TYPE_TINY:
                payload.writeInt1((Integer) binaryData);
                break;
            case MYSQL_TYPE_DOUBLE:
                payload.writeDouble(Double.parseDouble(binaryData.toString()));
                break;
            case MYSQL_TYPE_FLOAT:
                payload.writeFloat(Float.parseFloat(binaryData.toString()));
                break;
            case MYSQL_TYPE_DATE:
            case MYSQL_TYPE_DATETIME:
            case MYSQL_TYPE_TIMESTAMP:
                payload.writeDate((Timestamp) binaryData);
                break;
            case MYSQL_TYPE_TIME:
                payload.writeTime((Date) binaryData);
                break;
            default:
                throw new IllegalArgumentException(String.format("Cannot find MySQL type '%s' in column type when write binary protocol value", columnType));
        }
    }
}
