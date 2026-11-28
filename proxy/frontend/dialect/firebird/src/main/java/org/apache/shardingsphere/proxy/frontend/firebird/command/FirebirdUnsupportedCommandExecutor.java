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

package org.apache.shardingsphere.proxy.frontend.firebird.command;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.UnsupportedCommandException;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.FirebirdCommandPacketType;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.FirebirdUnsupportedCommandPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.commonresponses.generic.FirebirdErrorPacketFactory;
import org.apache.shardingsphere.database.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.database.protocol.packet.command.CommandPacket;
import org.apache.shardingsphere.proxy.frontend.command.executor.CommandExecutor;

import java.util.Collection;
import java.util.Collections;

/**
 * Unsupported command executor for Firebird.
 */
@RequiredArgsConstructor
public final class FirebirdUnsupportedCommandExecutor implements CommandExecutor {
    
    private final CommandPacket commandPacket;
    
    private final FirebirdCommandPacketType commandPacketType;
    
    @Override
    public Collection<DatabasePacket> execute() {
        Exception cause = commandPacket instanceof FirebirdUnsupportedCommandPacket
                ? UnsupportedCommandException.forCommandPacket(commandPacketType.name())
                : UnsupportedCommandException.forCommandExecutor(commandPacketType.name());
        return Collections.singleton(FirebirdErrorPacketFactory.newInstance(cause));
    }
}
