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

package org.apache.shardingsphere.proxy.backend.handler.distsql.rdl.rule;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.distsql.handler.type.rdl.rule.database.DatabaseRuleRDLExecutor;
import org.apache.shardingsphere.distsql.handler.type.rdl.rule.global.GlobalRuleRDLExecutor;
import org.apache.shardingsphere.distsql.statement.rdl.RuleDefinitionStatement;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.handler.distsql.DistSQLBackendHandler;
import org.apache.shardingsphere.proxy.backend.handler.distsql.rdl.rule.database.DatabaseRuleUpdater;
import org.apache.shardingsphere.proxy.backend.handler.distsql.rdl.rule.global.GlobalRuleUpdater;
import org.apache.shardingsphere.proxy.backend.handler.distsql.rdl.rule.legacy.LegacyGlobalRuleUpdater;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;

import java.util.Optional;

/**
 * Rule definition backend handler.
 */
@RequiredArgsConstructor
public final class RuleDefinitionBackendHandler implements DistSQLBackendHandler {
    
    private final RuleDefinitionStatement sqlStatement;
    
    private final ConnectionSession connectionSession;
    
    @SuppressWarnings("rawtypes")
    @Override
    public ResponseHeader execute() {
        Optional<DatabaseRuleRDLExecutor> databaseExecutor = TypedSPILoader.findService(DatabaseRuleRDLExecutor.class, sqlStatement.getClass());
        if (databaseExecutor.isPresent()) {
            new DatabaseRuleUpdater(sqlStatement, connectionSession, databaseExecutor.get()).executeUpdate();
        } else {
            String modeType = ProxyContext.getInstance().getContextManager().getInstanceContext().getModeConfiguration().getType();
            GlobalRuleRDLExecutor globalExecutor = TypedSPILoader.getService(GlobalRuleRDLExecutor.class, sqlStatement.getClass());
            if ("Cluster".equals(modeType) || "Standalone".equals(modeType)) {
                new GlobalRuleUpdater(sqlStatement, globalExecutor).executeUpdate();
            } else {
                new LegacyGlobalRuleUpdater(sqlStatement).executeUpdate();
            }
        }
        return new UpdateResponseHeader(sqlStatement);
    }
}
