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

package org.apache.shardingsphere.database.protocol.firebird.packet.command.query.statement.execute.protocol;

import com.google.common.base.Preconditions;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.database.protocol.binary.BinaryColumnType;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.FirebirdBinaryColumnType;

import java.util.HashMap;
import java.util.Map;

/**
 * Binary protocol value factory for Firebird.
 * TODO Add handle for timezones and EX
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FirebirdBinaryProtocolValueFactory {
    
    private static final Map<BinaryColumnType, FirebirdBinaryProtocolValue> BINARY_PROTOCOL_VALUES = new HashMap<>();
    
    static {
        setStringBinaryProtocolValue();
        // setByteBinaryProtocolValue();
        setInt16BinaryProtocolValue();
        setInt8BinaryProtocolValue();
        setInt4BinaryProtocolValue();
        setInt2BinaryProtocolValue();
        setInt1BinaryProtocolValue();
        setDoubleBinaryProtocolValue();
        setFloatBinaryProtocolValue();
        setDateBinaryProtocolValue();
        setTimeBinaryProtocolValue();
        setTimestampBinaryProtocolValue();
        setTimestampTZBinaryProtocolValue();
        setNullBinaryProtocolValue();
    }
    
    private static void setStringBinaryProtocolValue() {
        FirebirdStringBinaryProtocolValue binaryProtocolValue = new FirebirdStringBinaryProtocolValue();
        BINARY_PROTOCOL_VALUES.put(FirebirdBinaryColumnType.VARYING, binaryProtocolValue);
        BINARY_PROTOCOL_VALUES.put(FirebirdBinaryColumnType.TEXT, binaryProtocolValue);
        BINARY_PROTOCOL_VALUES.put(FirebirdBinaryColumnType.LEGACY_VARYING, binaryProtocolValue);
        BINARY_PROTOCOL_VALUES.put(FirebirdBinaryColumnType.LEGACY_TEXT, binaryProtocolValue);
        BINARY_PROTOCOL_VALUES.put(FirebirdBinaryColumnType.BLOB, binaryProtocolValue);
    }
    
    // TODO Uncomment when a specific handler is required; currently BLOB is handled by StringBinaryProtocolValue
    // private static void setByteBinaryProtocolValue() {
    // FirebirdByteBinaryProtocolValue binaryProtocolValue = new FirebirdByteBinaryProtocolValue();
    // }
    
    private static void setInt16BinaryProtocolValue() {
        FirebirdInt16BinaryProtocolValue binaryProtocolValue = new FirebirdInt16BinaryProtocolValue();
        BINARY_PROTOCOL_VALUES.put(FirebirdBinaryColumnType.INT128, binaryProtocolValue);
    }
    
    private static void setInt8BinaryProtocolValue() {
        FirebirdInt8BinaryProtocolValue binaryProtocolValue = new FirebirdInt8BinaryProtocolValue();
        BINARY_PROTOCOL_VALUES.put(FirebirdBinaryColumnType.INT64, binaryProtocolValue);
        BINARY_PROTOCOL_VALUES.put(FirebirdBinaryColumnType.NUMERIC, binaryProtocolValue);
        BINARY_PROTOCOL_VALUES.put(FirebirdBinaryColumnType.DECIMAL, binaryProtocolValue);
        BINARY_PROTOCOL_VALUES.put(FirebirdBinaryColumnType.DEC34, binaryProtocolValue);
    }
    
    private static void setInt4BinaryProtocolValue() {
        FirebirdInt4BinaryProtocolValue binaryProtocolValue = new FirebirdInt4BinaryProtocolValue();
        BINARY_PROTOCOL_VALUES.put(FirebirdBinaryColumnType.LONG, binaryProtocolValue);
        BINARY_PROTOCOL_VALUES.put(FirebirdBinaryColumnType.DEC16, binaryProtocolValue);
        BINARY_PROTOCOL_VALUES.put(FirebirdBinaryColumnType.QUAD, binaryProtocolValue);
    }
    
    private static void setInt2BinaryProtocolValue() {
        FirebirdInt2BinaryProtocolValue binaryProtocolValue = new FirebirdInt2BinaryProtocolValue();
        BINARY_PROTOCOL_VALUES.put(FirebirdBinaryColumnType.SHORT, binaryProtocolValue);
    }
    
    private static void setInt1BinaryProtocolValue() {
        FirebirdInt1BinaryProtocolValue binaryProtocolValue = new FirebirdInt1BinaryProtocolValue();
        BINARY_PROTOCOL_VALUES.put(FirebirdBinaryColumnType.BOOLEAN, binaryProtocolValue);
    }
    
    private static void setDoubleBinaryProtocolValue() {
        FirebirdDoubleBinaryProtocolValue binaryProtocolValue = new FirebirdDoubleBinaryProtocolValue();
        BINARY_PROTOCOL_VALUES.put(FirebirdBinaryColumnType.DOUBLE, binaryProtocolValue);
    }
    
    private static void setFloatBinaryProtocolValue() {
        FirebirdFloatBinaryProtocolValue binaryProtocolValue = new FirebirdFloatBinaryProtocolValue();
        BINARY_PROTOCOL_VALUES.put(FirebirdBinaryColumnType.FLOAT, binaryProtocolValue);
        BINARY_PROTOCOL_VALUES.put(FirebirdBinaryColumnType.D_FLOAT, binaryProtocolValue);
    }
    
    private static void setDateBinaryProtocolValue() {
        FirebirdDateBinaryProtocolValue binaryProtocolValue = new FirebirdDateBinaryProtocolValue();
        BINARY_PROTOCOL_VALUES.put(FirebirdBinaryColumnType.DATE, binaryProtocolValue);
    }
    
    private static void setTimeBinaryProtocolValue() {
        FirebirdTimeBinaryProtocolValue binaryProtocolValue = new FirebirdTimeBinaryProtocolValue();
        BINARY_PROTOCOL_VALUES.put(FirebirdBinaryColumnType.TIME, binaryProtocolValue);
    }
    
    private static void setTimestampBinaryProtocolValue() {
        FirebirdTimestampBinaryProtocolValue binaryProtocolValue = new FirebirdTimestampBinaryProtocolValue();
        BINARY_PROTOCOL_VALUES.put(FirebirdBinaryColumnType.TIMESTAMP, binaryProtocolValue);
    }
    
    private static void setTimestampTZBinaryProtocolValue() {
        FirebirdTimestampTZBinaryProtocolValue binaryProtocolValue = new FirebirdTimestampTZBinaryProtocolValue();
        BINARY_PROTOCOL_VALUES.put(FirebirdBinaryColumnType.TIMESTAMP_TZ, binaryProtocolValue);
    }
    
    private static void setNullBinaryProtocolValue() {
        FirebirdNullBinaryProtocolValue binaryProtocolValue = new FirebirdNullBinaryProtocolValue();
        BINARY_PROTOCOL_VALUES.put(FirebirdBinaryColumnType.NULL, binaryProtocolValue);
    }
    
    /**
     * Get binary protocol value.
     *
     * @param binaryColumnType binary column type
     * @return binary protocol value
     */
    public static FirebirdBinaryProtocolValue getBinaryProtocolValue(final BinaryColumnType binaryColumnType) {
        Preconditions.checkArgument(BINARY_PROTOCOL_VALUES.containsKey(binaryColumnType), "Cannot find Firebird type '%s' in column type when process binary protocol value", binaryColumnType);
        return BINARY_PROTOCOL_VALUES.get(binaryColumnType);
    }
}
