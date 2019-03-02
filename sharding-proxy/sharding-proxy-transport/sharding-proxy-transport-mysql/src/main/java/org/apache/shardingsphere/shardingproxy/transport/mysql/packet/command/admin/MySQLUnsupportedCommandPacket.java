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

package org.apache.shardingsphere.shardingproxy.transport.mysql.packet.command.admin;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.shardingproxy.error.CommonErrorCode;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.MySQLPacket;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.command.MySQLCommandPacket;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.command.MySQLCommandPacketType;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.generic.MySQLErrPacket;
import org.apache.shardingsphere.shardingproxy.transport.mysql.payload.MySQLPacketPayload;

import java.util.Collection;
import java.util.Collections;

/**
 * MySQL unsupported command packet.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
public final class MySQLUnsupportedCommandPacket implements MySQLCommandPacket {
    
    private final MySQLCommandPacketType type;
    
    @Override
    public Collection<MySQLPacket> execute() {
        return Collections.<MySQLPacket>singletonList(new MySQLErrPacket(1, CommonErrorCode.UNSUPPORTED_COMMAND, type));
    }
    
    @Override
    public void write(final MySQLPacketPayload payload) {
    }
    
    @Override
    public int getSequenceId() {
        return 0;
    }
}
