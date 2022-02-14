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

import org.apache.shardingsphere.distsql.parser.statement.ral.RALStatement;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;

/**
 * RAL backend handler .
 */
public abstract class UpdatableBackendHandler<E extends RALStatement, R extends UpdatableBackendHandler> extends RALBackendHandler<E, R> {
    
    @Override
    protected R init(final HandlerParameter<E> parameter) {
        setSqlStatement(parameter.getStatement());
        return (R) this;
    }
    
    @Override
    protected final ResponseHeader handle(ContextManager contextManager, E sqlStatement) {
        handler(contextManager, sqlStatement);
        return new UpdateResponseHeader(sqlStatement);
    }
    
    protected abstract void handler(ContextManager contextManager, E sqlStatement);
}
    
    