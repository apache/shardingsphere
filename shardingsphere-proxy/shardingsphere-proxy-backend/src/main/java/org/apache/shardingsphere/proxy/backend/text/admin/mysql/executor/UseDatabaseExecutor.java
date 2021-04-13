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

package org.apache.shardingsphere.proxy.backend.text.admin.mysql.executor;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.executor.check.SQLCheckEngine;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.exception.UnknownDatabaseException;
import org.apache.shardingsphere.proxy.backend.text.admin.executor.DatabaseAdminExecutor;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dal.UseStatement;
import org.apache.shardingsphere.sql.parser.sql.common.util.SQLUtil;

import java.util.Collection;
import java.util.LinkedList;

/**
 * Use database executor.
 */
@RequiredArgsConstructor
public final class UseDatabaseExecutor implements DatabaseAdminExecutor {
    
    private final UseStatement useStatement;
    
    @Override
    public void execute(final BackendConnection backendConnection) {
        String schemaName = SQLUtil.getExactlyValue(useStatement.getSchema());
        if (!ProxyContext.getInstance().schemaExists(schemaName) && SQLCheckEngine.check(schemaName, getRules(schemaName), backendConnection.getGrantee())) {
            throw new UnknownDatabaseException(schemaName);
        }
        backendConnection.setCurrentSchema(schemaName);
    }
    
    private Collection<ShardingSphereRule> getRules(final String schemaName) {
        Collection<ShardingSphereRule> result;
        result = new LinkedList<>(ProxyContext.getInstance().getMetaDataContexts().getMetaData(schemaName).getRuleMetaData().getRules());
        result.addAll(ProxyContext.getInstance().getMetaDataContexts().getGlobalRuleMetaData().getRules());
        return result;
    }
}
