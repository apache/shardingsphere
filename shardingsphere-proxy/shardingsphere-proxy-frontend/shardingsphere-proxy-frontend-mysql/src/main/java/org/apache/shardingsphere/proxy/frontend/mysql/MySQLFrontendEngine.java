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

package org.apache.shardingsphere.proxy.frontend.mysql;

import lombok.Getter;
import org.apache.shardingsphere.db.protocol.codec.DatabasePacketCodecEngine;
import org.apache.shardingsphere.db.protocol.mysql.codec.MySQLPacketCodecEngine;
import org.apache.shardingsphere.db.protocol.mysql.packet.MySQLPacket;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.frontend.authentication.AuthenticationEngine;
import org.apache.shardingsphere.proxy.frontend.command.CommandExecuteEngine;
import org.apache.shardingsphere.proxy.frontend.context.FrontendContext;
import org.apache.shardingsphere.proxy.frontend.mysql.authentication.MySQLAuthenticationEngine;
import org.apache.shardingsphere.proxy.frontend.mysql.command.MySQLCommandExecuteEngine;
import org.apache.shardingsphere.proxy.frontend.spi.DatabaseProtocolFrontendEngine;

/**
 * Frontend engine for MySQL.
 */
@Getter
public final class MySQLFrontendEngine implements DatabaseProtocolFrontendEngine {
    
    private final FrontendContext frontendContext = new FrontendContext(false, true);
    
    private final AuthenticationEngine authenticationEngine = new MySQLAuthenticationEngine();
    
    private final CommandExecuteEngine commandExecuteEngine = new MySQLCommandExecuteEngine();
    
    private final DatabasePacketCodecEngine<MySQLPacket> codecEngine = new MySQLPacketCodecEngine();
    
    @Override
    public void release(final BackendConnection backendConnection) {
    }
    
    @Override
    public String getDatabaseType() {
        return "MySQL";
    }
}
