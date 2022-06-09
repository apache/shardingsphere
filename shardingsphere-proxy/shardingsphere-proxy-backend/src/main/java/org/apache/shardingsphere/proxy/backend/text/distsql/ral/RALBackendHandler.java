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

package org.apache.shardingsphere.proxy.backend.text.distsql.ral;

import com.google.common.base.Preconditions;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.distsql.parser.statement.ral.RALStatement;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.text.TextProtocolBackendHandler;

import java.sql.SQLException;

/**
 * RAL backend handler.
 */
@Getter
public abstract class RALBackendHandler<E extends RALStatement> implements TextProtocolBackendHandler {
    
    private E sqlStatement;
    
    /**
     * Method to initialize handler, this method needs to be rewritten when the handler has properties other than sql statement.
     *
     * @param parameter parameters required by handler
     */
    public void init(final HandlerParameter<E> parameter) {
        sqlStatement = parameter.getStatement();
    }
    
    @Override
    public final ResponseHeader execute() throws SQLException {
        Preconditions.checkNotNull(sqlStatement, "sql statement cannot be empty.");
        ContextManager contextManager = ProxyContext.getInstance().getContextManager();
        return handle(contextManager, sqlStatement);
    }
    
    protected abstract ResponseHeader handle(ContextManager contextManager, E sqlStatement) throws SQLException;
    
    @RequiredArgsConstructor
    @Getter
    public static class HandlerParameter<E extends RALStatement> {
        
        private final E statement;
        
        private final ConnectionSession connectionSession;
    }
}
