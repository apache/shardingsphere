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

package org.apache.shardingsphere.database.protocol.firebird.constant.buffer;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.database.protocol.firebird.exception.FirebirdProtocolException;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Firebird parameter buffer.
 */
@RequiredArgsConstructor
public final class FirebirdParameterBuffer {
    
    @Getter
    private int version;
    
    private final Map<FirebirdParameterBufferType, Object> parameterBuffer = new HashMap<>();
    
    private final Function<Integer, FirebirdParameterBufferType> valueOf;
    
    private final Function<Integer, Boolean> isTraditionalType;
    
    /**
     * Parse parameter buffer from the given {@link ByteBuf}.
     *
     * @param buffer byte buffer containing parameter data
     */
    public void parseBuffer(final ByteBuf buffer) {
        version = buffer.readUnsignedByte();
        while (buffer.isReadable()) {
            FirebirdParameterBufferType type = valueOf.apply((int) buffer.readUnsignedByte());
            parameterBuffer.put(type, parseValue(buffer, type));
        }
    }
    
    private Object parseValue(final ByteBuf parameterBuffer, final FirebirdParameterBufferType type) {
        boolean traditionalStyle = isTraditionalType.apply(version);
        switch (type.getFormat()) {
            case INT:
                if (traditionalStyle) {
                    parameterBuffer.skipBytes(1);
                } else {
                    parameterBuffer.skipBytes(4);
                }
                return parameterBuffer.readIntLE();
            case BOOLEAN:
                return true;
            case STRING:
                int length = traditionalStyle ? parameterBuffer.readByte() : parameterBuffer.readIntLE();
                return parameterBuffer.readSlice(length).toString(StandardCharsets.UTF_8);
            default:
                throw new FirebirdProtocolException("Unsupported format type %s", type.getFormat().name());
        }
    }
    
    /**
     * Get property value.
     *
     * @param key property key
     * @param <T> class type of return value
     * @return property value or null
     */
    @SuppressWarnings("unchecked")
    public <T> T getValue(final FirebirdParameterBufferType key) {
        return (T) parameterBuffer.get(key);
    }
}
