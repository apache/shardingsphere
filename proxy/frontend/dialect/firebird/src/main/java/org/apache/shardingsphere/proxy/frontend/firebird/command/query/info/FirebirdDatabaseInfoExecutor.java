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

package org.apache.shardingsphere.proxy.frontend.firebird.command.query.info;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.database.protocol.firebird.constant.FirebirdConstant;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.info.FirebirdInfoPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.info.type.database.FirebirdDatabaseInfoPacketType;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.info.type.database.FirebirdDatabaseInfoReturnPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.generic.FirebirdGenericResponsePacket;
import org.apache.shardingsphere.database.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.infra.annotation.HighFrequencyInvocation;
import org.apache.shardingsphere.proxy.backend.firebird.handler.admin.executor.variable.charset.FirebirdCharacterSets;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.frontend.command.executor.CommandExecutor;

import java.util.Collection;
import java.util.Collections;

/**
 * Database info command executor for Firebird.
 */
@RequiredArgsConstructor
public final class FirebirdDatabaseInfoExecutor implements CommandExecutor {
    
    private final FirebirdInfoPacket packet;
    
    private final ConnectionSession connectionSession;
    
    @Override
    @HighFrequencyInvocation
    public Collection<DatabasePacket> execute() {
        int charsetId = packet.getInfoItems().contains(FirebirdDatabaseInfoPacketType.ATT_CHARSET)
                ? FirebirdCharacterSets.findCharacterSetId(connectionSession.getAttributeMap().attr(FirebirdConstant.ATTACHMENT_CHARSET).get())
                : 0;
        return Collections.singleton(new FirebirdGenericResponsePacket().setData(new FirebirdDatabaseInfoReturnPacket(packet.getInfoItems(), charsetId)));
    }
}
