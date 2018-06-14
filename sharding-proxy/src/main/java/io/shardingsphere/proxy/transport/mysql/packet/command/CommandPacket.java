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

package io.shardingsphere.proxy.transport.mysql.packet.command;

import io.shardingsphere.proxy.transport.common.packet.DatabaseProtocolPacket;
import io.shardingsphere.proxy.transport.mysql.packet.MySQLPacket;

/**
 * Command packet.
 *
 * @author zhangliang
 * @author wangkai
 */
public abstract class CommandPacket extends MySQLPacket {
    
    public CommandPacket(final int sequenceId) {
        super(sequenceId);
    }
    
    /**
     * Execute command.
     * 
     * @return result packets to be sent
     */
    public abstract CommandResponsePackets execute();
    
    /**
     * Has more result value.
     *
     * @return has more result value
     */
    public abstract boolean hasMoreResultValue();
    
    /**
     * Get result value.
     *
     * @return result to be sent
     */
    public abstract DatabaseProtocolPacket getResultValue();
}
