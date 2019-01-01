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

package io.shardingsphere.shardingproxy.frontend.mysql;

import io.shardingsphere.shardingproxy.transport.mysql.packet.command.CommandResponsePackets;
import lombok.NoArgsConstructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The sequencer is used for reorder response.
 *
 * @author wuxu
 */
@NoArgsConstructor
public final class CommandResponseSequencer {
    
    private final List<String> commandPacketIds = new ArrayList<>();
    
    private final Map<String, Map<CommandExecutor, CommandResponsePackets>> delays = new HashMap<>();
    
    /**
     * Add command packet id to the list which is used to reorder response.
     *
     * @param commandPacketId command packet id
     */
    protected void addCommandPacketId(final String commandPacketId) {
        commandPacketIds.add(commandPacketId);
    }
    
    /**
     * Reorder and write response by list of command packet id.
     *
     * @param commandExecutor command executor
     * @param responsePackets command response packets
     */
    protected void reorderAndWriteResponse(final CommandExecutor commandExecutor, final CommandResponsePackets responsePackets) {
        String commandPacketId = commandExecutor.getCommandPacketId();
        if (commandPacketIds.indexOf(commandPacketId) == 0) {
            commandExecutor.doWriteResult(responsePackets);
            commandPacketIds.remove(commandPacketId);
            delays.remove(commandPacketId);
            if (delays.isEmpty() || commandPacketIds.isEmpty()) {
                commandExecutor.flush();
                return;
            }
            String firstCommandPacketId = commandPacketIds.get(0);
            if (delays.containsKey(firstCommandPacketId)) {
                Map<CommandExecutor, CommandResponsePackets> commandResponsePacketsMap = delays.get(firstCommandPacketId);
                reorderAndWriteResponse(commandResponsePacketsMap.keySet().iterator().next(), commandResponsePacketsMap.values().iterator().next());
            }
        } else {
            Map<CommandExecutor, CommandResponsePackets> commandResponsePacketsMap = new HashMap<>();
            commandResponsePacketsMap.put(commandExecutor, responsePackets);
            delays.put(commandPacketId, commandResponsePacketsMap);
        }
    }
}
