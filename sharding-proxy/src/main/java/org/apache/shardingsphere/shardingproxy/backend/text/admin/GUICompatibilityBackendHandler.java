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

package org.apache.shardingsphere.shardingproxy.backend.text.admin;

import org.apache.shardingsphere.shardingproxy.backend.result.query.QueryData;
import org.apache.shardingsphere.shardingproxy.backend.text.TextProtocolBackendHandler;
import org.apache.shardingsphere.shardingproxy.transport.common.packet.command.CommandResponsePackets;
import org.apache.shardingsphere.shardingproxy.transport.common.packet.generic.DatabaseSuccessPacket;

/**
 * GUI compatibility backend handler.
 *
 * @author zhangyonglun
 */
public final class GUICompatibilityBackendHandler implements TextProtocolBackendHandler {
    
    @Override
    public CommandResponsePackets execute() {
        return new CommandResponsePackets(new DatabaseSuccessPacket(1, 0L, 0L));
    }
    
    @Override
    public boolean next() {
        return false;
    }
    
    @Override
    public QueryData getQueryData() {
        return null;
    }
}
