/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.proxy.backend.common.jdbc.execute.response;

import io.shardingsphere.proxy.backend.common.jdbc.execute.response.unit.ExecuteResponseUnit;
import io.shardingsphere.proxy.transport.mysql.packet.command.reponse.CommandResponsePackets;
import lombok.Getter;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Execute update response.
 * 
 * @author zhangliang
 */
@Getter
public final class ExecuteUpdateResponse implements ExecuteResponse {
    
    private final List<CommandResponsePackets> packetsList = new LinkedList<>();
    
    private final CommandResponsePackets firstPackets;
    
    public ExecuteUpdateResponse(final CommandResponsePackets packets) {
        packetsList.add(packets);
        firstPackets = packetsList.iterator().next();
    }
    
    public ExecuteUpdateResponse(final Collection<ExecuteResponseUnit> responseUnits) {
        for (ExecuteResponseUnit each : responseUnits) {
            packetsList.add(each.getCommandResponsePackets());
        }
        firstPackets = packetsList.iterator().next();
    }
}
