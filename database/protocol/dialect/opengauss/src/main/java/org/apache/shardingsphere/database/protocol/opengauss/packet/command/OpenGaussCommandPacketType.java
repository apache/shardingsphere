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

package org.apache.shardingsphere.database.protocol.opengauss.packet.command;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.database.protocol.opengauss.packet.identifier.OpenGaussIdentifierTag;
import org.apache.shardingsphere.database.protocol.packet.command.CommandPacketType;
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.PostgreSQLCommandPacketType;

/**
 * Command packet type for openGauss.
 */
@RequiredArgsConstructor
@Getter
public enum OpenGaussCommandPacketType implements CommandPacketType, OpenGaussIdentifierTag {
    
    BATCH_BIND_COMMAND('U');
    
    private final char value;
    
    /**
     * Value of integer.
     *
     * @param value integer value
     * @return command packet type enum
     */
    public static CommandPacketType valueOf(final int value) {
        return BATCH_BIND_COMMAND.value == value ? BATCH_BIND_COMMAND : PostgreSQLCommandPacketType.valueOf(value);
    }
    
    /**
     * Check if the packet type is extended protocol packet type.
     *
     * @param commandPacketType command packet type
     * @return is extended protocol packet type
     */
    public static boolean isExtendedProtocolPacketType(final CommandPacketType commandPacketType) {
        return BATCH_BIND_COMMAND == commandPacketType || PostgreSQLCommandPacketType.isExtendedProtocolPacketType(commandPacketType);
    }
}
