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

import org.apache.shardingsphere.distsql.handler.type.rdl.database.DatabaseRuleRDLExecutor;
import org.apache.shardingsphere.distsql.handler.type.rdl.global.GlobalRuleRDLExecutor;
import org.apache.shardingsphere.distsql.statement.rdl.RuleDefinitionStatement;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.handler.distsql.rdl.RDLBackendHandler;
import org.apache.shardingsphere.proxy.backend.handler.distsql.rdl.rule.legacy.GlobalRuleRDLBackendHandler;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;

import java.util.Optional;

/**
 * TODO Rename to RuleDefinitionBackendHandler when metadata structure adjustment completed. #25485
 * Database rule definition backend handler.
 *
 * @param <T> type of rule definition statement
 */
public final class NewRuleDefinitionBackendHandler<T extends RuleDefinitionStatement> extends RDLBackendHandler<T> {
    
    public NewRuleDefinitionBackendHandler(final T sqlStatement, final ConnectionSession connectionSession) {
        super(sqlStatement, connectionSession);
    }
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    protected ResponseHeader execute(final ShardingSphereDatabase database, final T sqlStatement) {
        Optional<DatabaseRuleRDLExecutor> databaseExecutor = TypedSPILoader.findService(DatabaseRuleRDLExecutor.class, sqlStatement.getClass());
        if (databaseExecutor.isPresent()) {
            return new NewDatabaseRuleDefinitionBackendHandler(sqlStatement, getConnectionSession(), databaseExecutor.get()).execute(database, sqlStatement);
        }
        String modeType = ProxyContext.getInstance().getContextManager().getInstanceContext().getModeConfiguration().getType();
        GlobalRuleRDLExecutor globalExecutor = TypedSPILoader.getService(GlobalRuleRDLExecutor.class, sqlStatement.getClass());
        return "Cluster".equals(modeType) || "Standalone".equals(modeType)
                ? new NewGlobalRuleRDLBackendHandler(sqlStatement, globalExecutor).execute()
                : new GlobalRuleRDLBackendHandler(sqlStatement).execute();
    }
}
