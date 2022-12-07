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

package org.apache.shardingsphere.proxy.backend.handler.distsql.ral.updatable;

import com.google.common.base.Strings;
import org.apache.shardingsphere.dialect.exception.syntax.database.NoDatabaseSelectedException;
import org.apache.shardingsphere.dialect.exception.syntax.database.UnknownDatabaseException;
import org.apache.shardingsphere.distsql.parser.statement.ral.updatable.RefreshDatabaseMetaDataStatement;
import org.apache.shardingsphere.infra.util.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.handler.distsql.ral.UpdatableRALBackendHandler;

import java.util.Optional;

/**
 * Refresh database meta data handler.
 */
public final class RefreshDatabaseMetaDataHandler extends UpdatableRALBackendHandler<RefreshDatabaseMetaDataStatement> {
    
    @Override
    protected void update(final ContextManager contextManager) {
        contextManager.reloadDatabaseMetaData(getDatabaseName());
    }
    
    private String getDatabaseName() {
        Optional<String> databaseName = getSqlStatement().getDatabaseName();
        String result = databaseName.orElseGet(() -> getConnectionSession().getDatabaseName());
        ShardingSpherePreconditions.checkState(!Strings.isNullOrEmpty(result), NoDatabaseSelectedException::new);
        ShardingSpherePreconditions.checkState(ProxyContext.getInstance().databaseExists(result), () -> new UnknownDatabaseException(result));
        return result;
    }
}
