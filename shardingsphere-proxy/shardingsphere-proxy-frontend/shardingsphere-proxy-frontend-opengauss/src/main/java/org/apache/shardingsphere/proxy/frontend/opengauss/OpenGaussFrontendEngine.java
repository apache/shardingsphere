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

package org.apache.shardingsphere.proxy.frontend.opengauss;

import lombok.Getter;
import org.apache.shardingsphere.db.protocol.codec.DatabasePacketCodecEngine;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.frontend.authentication.AuthenticationEngine;
import org.apache.shardingsphere.proxy.frontend.context.FrontendContext;
import org.apache.shardingsphere.proxy.frontend.opengauss.command.OpenGaussCommandExecuteEngine;
import org.apache.shardingsphere.proxy.frontend.postgresql.PostgreSQLFrontendEngine;
import org.apache.shardingsphere.proxy.frontend.spi.DatabaseProtocolFrontendEngine;

/**
 * Frontend engine for openGauss.
 */
public final class OpenGaussFrontendEngine implements DatabaseProtocolFrontendEngine {
    
    private final PostgreSQLFrontendEngine postgreSQLFrontendEngine = new PostgreSQLFrontendEngine();
    
    @Getter
    private final OpenGaussCommandExecuteEngine commandExecuteEngine = new OpenGaussCommandExecuteEngine();
    
    @Override
    public FrontendContext getFrontendContext() {
        return postgreSQLFrontendEngine.getFrontendContext();
    }
    
    @Override
    public DatabasePacketCodecEngine<?> getCodecEngine() {
        return postgreSQLFrontendEngine.getCodecEngine();
    }
    
    @Override
    public AuthenticationEngine getAuthenticationEngine() {
        return postgreSQLFrontendEngine.getAuthenticationEngine();
    }
    
    @Override
    public void release(final BackendConnection backendConnection) {
        postgreSQLFrontendEngine.release(backendConnection);
    }
    
    @Override
    public String getDatabaseType() {
        return "openGauss";
    }
}
