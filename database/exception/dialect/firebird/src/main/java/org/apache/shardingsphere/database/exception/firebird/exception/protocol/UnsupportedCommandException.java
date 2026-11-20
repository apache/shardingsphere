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

package org.apache.shardingsphere.database.exception.firebird.exception.protocol;

import org.apache.shardingsphere.database.exception.core.exception.SQLDialectException;

/**
 * Unsupported command exception for Firebird.
 */
public final class UnsupportedCommandException extends SQLDialectException {
    
    private static final long serialVersionUID = -4649043290553949685L;
    
    private UnsupportedCommandException(final String reason) {
        super(reason);
    }
    
    /**
     * Create new instance for an unsupported command packet.
     *
     * @param command command name
     * @return created instance
     */
    public static UnsupportedCommandException forCommandPacket(final String command) {
        return new UnsupportedCommandException(String.format("Unsupported Firebird command packet: %s", command));
    }
    
    /**
     * Create new instance for an unsupported command executor.
     *
     * @param command command name
     * @return created instance
     */
    public static UnsupportedCommandException forCommandExecutor(final String command) {
        return new UnsupportedCommandException(String.format("Unsupported Firebird command executor: %s", command));
    }
    
    /**
     * Create new instance for an unsupported command packet type.
     *
     * @param value command packet type value
     * @return created instance
     */
    public static UnsupportedCommandException forCommandPacketType(final int value) {
        return new UnsupportedCommandException(String.format("Unsupported Firebird type of command packet: %d", value));
    }
}
