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

import lombok.AccessLevel;
import lombok.Getter;
import org.apache.shardingsphere.db.protocol.opengauss.codec.OpenGaussPacketCodecEngine;
import org.apache.shardingsphere.dialect.exception.transaction.InTransactionException;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.frontend.opengauss.authentication.OpenGaussAuthenticationEngine;
import org.apache.shardingsphere.proxy.frontend.opengauss.command.OpenGaussCommandExecuteEngine;
import org.apache.shardingsphere.proxy.frontend.postgresql.PostgreSQLFrontendEngine;
import org.apache.shardingsphere.proxy.frontend.spi.DatabaseProtocolFrontendEngine;

/**
 * Frontend engine for openGauss.
 */
@Getter
public final class OpenGaussFrontendEngine implements DatabaseProtocolFrontendEngine {
    
    @Getter(AccessLevel.NONE)
    private final PostgreSQLFrontendEngine postgreSQLFrontendEngine = new PostgreSQLFrontendEngine();
    
    private final OpenGaussAuthenticationEngine authenticationEngine = new OpenGaussAuthenticationEngine();
    
    private final OpenGaussCommandExecuteEngine commandExecuteEngine = new OpenGaussCommandExecuteEngine();
    
    private final OpenGaussPacketCodecEngine codecEngine = new OpenGaussPacketCodecEngine();
    
    @Override
    public void release(final ConnectionSession connectionSession) {
        postgreSQLFrontendEngine.release(connectionSession);
    }
    
    @Override
    public void handleException(final ConnectionSession connectionSession, final Exception exception) {
        if (connectionSession.getTransactionStatus().isInTransaction() && !connectionSession.getTransactionStatus().isExceptionOccur() && !(exception instanceof InTransactionException)) {
            connectionSession.getTransactionStatus().setExceptionOccur(true);
        }
    }
    
    @Override
    public String getDatabaseType() {
        return "openGauss";
    }
}
