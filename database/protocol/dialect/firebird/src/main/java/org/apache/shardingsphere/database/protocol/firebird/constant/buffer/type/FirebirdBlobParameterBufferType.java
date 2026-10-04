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

package org.apache.shardingsphere.database.protocol.firebird.constant.buffer.type;

import com.google.common.base.Preconditions;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.database.protocol.firebird.constant.FirebirdValueFormat;
import org.apache.shardingsphere.database.protocol.firebird.constant.buffer.FirebirdParameterBuffer;
import org.apache.shardingsphere.database.protocol.firebird.constant.buffer.FirebirdParameterBufferType;

import java.util.HashMap;
import java.util.Map;

/**
 * Firebird blob parameter buffer type.
 */
@RequiredArgsConstructor
@Getter
public enum FirebirdBlobParameterBufferType implements FirebirdParameterBufferType {
    
    SOURCE_TYPE(1),
    TARGET_TYPE(2),
    TYPE(3),
    SOURCE_INTERP(4),
    TARGET_INTERP(5),
    FILTER_PARAMETER(6),
    STORAGE(7);
    
    private static final Map<Integer, FirebirdBlobParameterBufferType> FIREBIRD_BPB_TYPE_CACHE = new HashMap<>(values().length, 1F);
    
    private final int code;
    
    private final FirebirdValueFormat format;
    
    static {
        for (FirebirdBlobParameterBufferType each : values()) {
            FIREBIRD_BPB_TYPE_CACHE.put(each.code, each);
        }
    }
    
    FirebirdBlobParameterBufferType(final int code) {
        this(code, FirebirdValueFormat.SIZED_INT);
    }
    
    /**
     * Value of.
     *
     * @param code bpb type code
     * @return Firebird bpb type
     */
    public static FirebirdBlobParameterBufferType valueOf(final int code) {
        FirebirdBlobParameterBufferType result = FIREBIRD_BPB_TYPE_CACHE.get(code);
        Preconditions.checkNotNull(result, "Cannot find code '%d' in bpb type", code);
        return result;
    }
    
    /**
     * Decides whether to use a traditional type for integers.
     *
     * @param version version of parameter buffer
     * @return is traditional type
     */
    public static boolean isTraditionalType(final int version) {
        return 1 == version;
    }
    
    /**
     * Creates parameter buffer of this type.
     *
     * @return Firebird blob parameter buffer
     */
    public static FirebirdParameterBuffer createBuffer() {
        return new FirebirdParameterBuffer(FirebirdBlobParameterBufferType::valueOf, FirebirdBlobParameterBufferType::isTraditionalType);
    }
}
