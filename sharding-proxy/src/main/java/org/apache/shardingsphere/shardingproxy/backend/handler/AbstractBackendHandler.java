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

package org.apache.shardingsphere.shardingproxy.backend.handler;

import org.apache.shardingsphere.shardingproxy.backend.ResultPacket;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.command.CommandResponsePackets;

import java.sql.SQLException;

/**
 * Abstract backend handler.
 *
 * @author zhangliang
 */
public abstract class AbstractBackendHandler implements BackendHandler {
    
    /**
     * default execute implement for adapter.
     *
     * @return command response packets
     */
    @Override
    public CommandResponsePackets execute() {
        try {
            return execute0();
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            return new CommandResponsePackets(ex);
        }
    }
    
    protected abstract CommandResponsePackets execute0() throws Exception;
    
    @Override
    public boolean next() throws SQLException {
        return false;
    }
    
    @Override
    public ResultPacket getResultValue() throws SQLException {
        return null;
    }
}
