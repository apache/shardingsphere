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
import org.apache.shardingsphere.infra.distsql.exception.DistSQLException;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;

/**
 * Updatable RAL backend handler .
 */
public abstract class UpdatableRALBackendHandler<E extends RALStatement, R extends UpdatableRALBackendHandler> extends RALBackendHandler<E, R> {
    
    @Override
    protected final ResponseHeader handle(final ContextManager contextManager, final E sqlStatement) throws DistSQLException {
        update(contextManager, sqlStatement);
        return new UpdateResponseHeader(sqlStatement);
    }
    
    protected abstract void update(ContextManager contextManager, E sqlStatement) throws DistSQLException;
}
