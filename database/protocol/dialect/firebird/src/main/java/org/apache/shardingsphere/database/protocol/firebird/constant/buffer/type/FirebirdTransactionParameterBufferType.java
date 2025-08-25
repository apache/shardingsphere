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
 * Firebird database parameter buffer type.
 */
@RequiredArgsConstructor
@Getter
public enum FirebirdTransactionParameterBufferType implements FirebirdParameterBufferType {
    
    CONSISTENCY(1),
    CONCURRENCY(2),
    SHARED(3),
    PROTECTED(4),
    EXCLUSIVE(5),
    WAIT(6),
    NOWAIT(7),
    READ(8),
    WRITE(9),
    LOCK_READ(10),
    LOCK_WRITE(11),
    VERB_TIME(12),
    COMMIT_TIME(13),
    IGNORE_LIMBO(14),
    READ_COMMITTED(15),
    AUTOCOMMIT(16),
    REC_VERSION(17),
    NO_REC_VERSION(18),
    RESTART_REQUESTS(19),
    NO_AUTO_UNDO(20),
    LOCK_TIMEOUT(21, FirebirdValueFormat.INT),
    READ_CONSISTENCY(22),
    AT_SNAPSHOT_NUMBER(23);
    
    private static final Map<Integer, FirebirdTransactionParameterBufferType> FIREBIRD_TPB_TYPE_CACHE = new HashMap<>();
    
    private final int code;
    
    private final FirebirdValueFormat format;
    
    static {
        for (FirebirdTransactionParameterBufferType each : values()) {
            FIREBIRD_TPB_TYPE_CACHE.put(each.code, each);
        }
    }
    
    FirebirdTransactionParameterBufferType(final int code) {
        this(code, FirebirdValueFormat.BOOLEAN);
    }
    
    /**
     * Value of.
     *
     * @param code arch type code
     * @return Firebird tpb type
     */
    public static FirebirdTransactionParameterBufferType valueOf(final int code) {
        FirebirdTransactionParameterBufferType result = FIREBIRD_TPB_TYPE_CACHE.get(code);
        Preconditions.checkNotNull(result, "Cannot find code '%d' in tpb type", code);
        return result;
    }
    
    /**
     * Decides whether to use a traditional type for integers.
     *
     * @param version verstion of parameter buffer
     * @return Is traditional type
     */
    public static boolean isTraditionalType(final int version) {
        return true;
    }
    
    /**
     * Creates parameter buffer of this type.
     *
     * @return Firebird transaction parameter buffer
     */
    public static FirebirdParameterBuffer createBuffer() {
        return new FirebirdParameterBuffer(FirebirdTransactionParameterBufferType::valueOf, FirebirdTransactionParameterBufferType::isTraditionalType);
    }
}
