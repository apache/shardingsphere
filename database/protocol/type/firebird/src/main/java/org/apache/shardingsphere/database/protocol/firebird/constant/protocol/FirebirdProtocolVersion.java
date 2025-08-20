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

package org.apache.shardingsphere.database.protocol.firebird.constant.protocol;

import com.google.common.base.Preconditions;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * Firebird protocol version.
 */
@RequiredArgsConstructor
@Getter
public enum FirebirdProtocolVersion {
    
    /*
     * Protocol 10 includes support for warnings and removes the requirement for encoding and decoding status codes
     */
    PROTOCOL_VERSION10(10),
    
    /*
     * Protocol 11 has support for user authentication related operations (op_update_account_info, op_authenticate_user and op_trusted_auth). When specific operation is not supported, we say "sorry".
     */
    PROTOCOL_VERSION11(FirebirdProtocolVersion.FB_PROTOCOL_FLAG | 11),
    
    /*
     * Protocol 12 has support for asynchronous call op_cancel. Currently implemented asynchronously only for TCP/IP.
     */
    PROTOCOL_VERSION12(FirebirdProtocolVersion.FB_PROTOCOL_FLAG | 12),
    
    /*
     * Protocol 13 has support for authentication plugins (op_cont_auth). It also transfers SQL messages in the packed (null aware) format.
     */
    PROTOCOL_VERSION13(FirebirdProtocolVersion.FB_PROTOCOL_FLAG | 13),
    
    /* Protocol 14: - fixes a bug in database crypt key callback */
    PROTOCOL_VERSION14(FirebirdProtocolVersion.FB_PROTOCOL_FLAG | 14),
    
    /* Protocol 15: - supports crypt key callback at connect phase */
    PROTOCOL_VERSION15(FirebirdProtocolVersion.FB_PROTOCOL_FLAG | 15),
    
    /* Protocol 16: - supports statement timeouts */
    PROTOCOL_VERSION16(FirebirdProtocolVersion.FB_PROTOCOL_FLAG | 16),
    
    /*
     * Protocol 17: - supports op_batch_sync, op_info_batch
     */
    PROTOCOL_VERSION17(FirebirdProtocolVersion.FB_PROTOCOL_FLAG | 17),
    
    /*
     * Protocol 18: - supports op_fetch_scroll
     */
    PROTOCOL_VERSION18(FirebirdProtocolVersion.FB_PROTOCOL_FLAG | 18),
    
    /*
     * Protocol 19: - supports passing flags to IStatement::prepare
     */
    PROTOCOL_VERSION19(FirebirdProtocolVersion.FB_PROTOCOL_FLAG | 19);
    
    private static final Map<Integer, FirebirdProtocolVersion> FIREBIRD_PROTOCOL_VERSION_CACHE = new HashMap<>();
    
    /*
     * Since protocol 11 we must be separated from Borland Interbase. Therefore, always set highmost bit in protocol version to 1. For unsigned protocol version this does not break version's compare.
     */
    private static final int FB_PROTOCOL_FLAG = 0x8000;
    
    private static final int FB_PROTOCOL_MASK = ~FB_PROTOCOL_FLAG & 0xFFFF;
    
    private final int code;
    
    static {
        for (FirebirdProtocolVersion each : values()) {
            FIREBIRD_PROTOCOL_VERSION_CACHE.put(each.code, each);
        }
    }
    
    /**
     * Value of.
     *
     * @param code protocol version code
     * @return Firebird arch type
     */
    public static FirebirdProtocolVersion valueOf(final int code) {
        FirebirdProtocolVersion result = FIREBIRD_PROTOCOL_VERSION_CACHE.get(code);
        Preconditions.checkNotNull(result, "Cannot find '%s' in arch type", code);
        return result;
    }
}
