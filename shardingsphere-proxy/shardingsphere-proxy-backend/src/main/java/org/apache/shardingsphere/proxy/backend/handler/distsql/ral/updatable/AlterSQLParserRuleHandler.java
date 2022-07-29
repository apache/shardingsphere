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

import org.apache.shardingsphere.distsql.parser.segment.CacheOptionSegment;
import org.apache.shardingsphere.distsql.parser.statement.ral.updatable.AlterSQLParserRuleStatement;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.parser.config.SQLParserRuleConfiguration;
import org.apache.shardingsphere.parser.rule.SQLParserRule;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.handler.distsql.ral.UpdatableRALBackendHandler;
import org.apache.shardingsphere.sql.parser.api.CacheOption;

import java.util.Collection;

/**
 * Alter SQL parser rule statement handler.
 */
public final class AlterSQLParserRuleHandler extends UpdatableRALBackendHandler<AlterSQLParserRuleStatement> {
    
    @Override
    protected void update(final ContextManager contextManager) {
        replaceNewRule();
        persistNewRuleConfigurations();
    }
    
    private void replaceNewRule() {
        SQLParserRuleConfiguration toBeAlteredRuleConfig = createToBeAlteredRuleConfiguration();
        Collection<ShardingSphereRule> globalRules = ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().getGlobalRuleMetaData().getRules();
        globalRules.removeIf(each -> each instanceof SQLParserRule);
        globalRules.add(new SQLParserRule(toBeAlteredRuleConfig));
    }
    
    private SQLParserRuleConfiguration createToBeAlteredRuleConfiguration() {
        AlterSQLParserRuleStatement sqlStatement = getSqlStatement();
        SQLParserRuleConfiguration currentConfig = ProxyContext
                .getInstance().getContextManager().getMetaDataContexts().getMetaData().getGlobalRuleMetaData().getSingleRule(SQLParserRule.class).getConfiguration();
        boolean sqlCommentParseEnabled = null == sqlStatement.getSqlCommentParseEnable() ? currentConfig.isSqlCommentParseEnabled() : sqlStatement.getSqlCommentParseEnable();
        CacheOption parseTreeCache =
                null == sqlStatement.getParseTreeCache() ? currentConfig.getParseTreeCache() : createCacheOption(currentConfig.getParseTreeCache(), sqlStatement.getParseTreeCache());
        CacheOption sqlStatementCache =
                null == sqlStatement.getSqlStatementCache() ? currentConfig.getSqlStatementCache() : createCacheOption(currentConfig.getSqlStatementCache(), sqlStatement.getSqlStatementCache());
        return new SQLParserRuleConfiguration(sqlCommentParseEnabled, parseTreeCache, sqlStatementCache);
    }
    
    private CacheOption createCacheOption(final CacheOption cacheOption, final CacheOptionSegment segment) {
        int initialCapacity = null == segment.getInitialCapacity() ? cacheOption.getInitialCapacity() : segment.getInitialCapacity();
        long maximumSize = null == segment.getMaximumSize() ? cacheOption.getMaximumSize() : segment.getMaximumSize();
        return new CacheOption(initialCapacity, maximumSize);
    }
    
    private void persistNewRuleConfigurations() {
        MetaDataContexts metaDataContexts = ProxyContext.getInstance().getContextManager().getMetaDataContexts();
        metaDataContexts.getPersistService().getGlobalRuleService().persist(metaDataContexts.getMetaData().getGlobalRuleMetaData().getConfigurations(), true);
    }
}
