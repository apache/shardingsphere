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

package org.apache.shardingsphere.proxy.backend.handler.distsql.rdl;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.distsql.handler.type.rdl.resource.ResourceDefinitionExecutor;
import org.apache.shardingsphere.distsql.handler.type.rdl.resource.aware.DatabaseAwareResourceDefinitionExecutor;
import org.apache.shardingsphere.distsql.statement.rdl.RDLStatement;
import org.apache.shardingsphere.distsql.statement.rdl.resource.ResourceDefinitionStatement;
import org.apache.shardingsphere.distsql.statement.rdl.rule.RuleDefinitionStatement;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.handler.ProxyBackendHandler;
import org.apache.shardingsphere.proxy.backend.handler.distsql.DistSQLBackendHandler;
import org.apache.shardingsphere.proxy.backend.handler.distsql.rdl.resource.ResourceDefinitionBackendHandler;
import org.apache.shardingsphere.proxy.backend.handler.distsql.rdl.rule.RuleDefinitionBackendHandler;
import org.apache.shardingsphere.proxy.backend.handler.distsql.rdl.rule.legacy.LegacyRuleDefinitionBackendHandler;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.util.DatabaseNameUtils;

/**
 * RDL backend handler factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RDLBackendHandlerFactory {
    
    /**
     * Create new instance of RDL backend handler.
     * 
     * @param sqlStatement RDL statement
     * @param connectionSession connection session
     * @return RDL backend handler
     */
    public static ProxyBackendHandler newInstance(final RDLStatement sqlStatement, final ConnectionSession connectionSession) {
        if (sqlStatement instanceof ResourceDefinitionStatement) {
            return getResourceBackendHandler((ResourceDefinitionStatement) sqlStatement, connectionSession);
        }
        return getRuleBackendHandler((RuleDefinitionStatement) sqlStatement, connectionSession);
    }
    
    @SuppressWarnings("rawtypes")
    private static ResourceDefinitionBackendHandler getResourceBackendHandler(final ResourceDefinitionStatement sqlStatement, final ConnectionSession connectionSession) {
        ResourceDefinitionExecutor executor = TypedSPILoader.getService(ResourceDefinitionExecutor.class, sqlStatement.getClass());
        if (executor instanceof DatabaseAwareResourceDefinitionExecutor) {
            ((DatabaseAwareResourceDefinitionExecutor<?>) executor).setDatabase(ProxyContext.getInstance().getDatabase(DatabaseNameUtils.getDatabaseName(sqlStatement, connectionSession)));
        }
        return new ResourceDefinitionBackendHandler(sqlStatement, executor);
    }
    
    private static DistSQLBackendHandler getRuleBackendHandler(final RuleDefinitionStatement sqlStatement, final ConnectionSession connectionSession) {
        // TODO Remove when metadata structure adjustment completed. #25485
        String modeType = ProxyContext.getInstance().getContextManager().getInstanceContext().getModeConfiguration().getType();
        if ("Cluster".equals(modeType) || "Standalone".equals(modeType)) {
            return new RuleDefinitionBackendHandler(sqlStatement, connectionSession);
        }
        return new LegacyRuleDefinitionBackendHandler(sqlStatement, connectionSession);
    }
}
