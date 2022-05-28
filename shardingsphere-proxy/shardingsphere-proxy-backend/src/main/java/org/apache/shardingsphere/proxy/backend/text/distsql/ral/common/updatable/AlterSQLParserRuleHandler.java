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

package org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.updatable;

import org.apache.shardingsphere.distsql.parser.segment.CacheOptionSegment;
import org.apache.shardingsphere.distsql.parser.statement.ral.common.updatable.AlterSQLParserRuleStatement;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistService;
import org.apache.shardingsphere.parser.config.SQLParserRuleConfiguration;
import org.apache.shardingsphere.parser.rule.builder.DefaultSQLParserRuleConfigurationBuilder;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.UpdatableRALBackendHandler;
import org.apache.shardingsphere.sql.parser.api.CacheOption;

import java.util.Collection;
import java.util.Optional;

/**
 * Alter SQL parser rule statement handler.
 */
public final class AlterSQLParserRuleHandler extends UpdatableRALBackendHandler<AlterSQLParserRuleStatement, AlterSQLParserRuleHandler> {
    
    @Override
    protected void update(final ContextManager contextManager, final AlterSQLParserRuleStatement sqlStatement) {
        Optional<SQLParserRuleConfiguration> currentConfig = findCurrentConfiguration();
        SQLParserRuleConfiguration toBeAlteredRuleConfig = createSQLParserRuleConfiguration(currentConfig.orElseGet(() -> new DefaultSQLParserRuleConfigurationBuilder().build()));
        Collection<RuleConfiguration> globalRuleConfigs = ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().getGlobalRuleMetaData().getConfigurations();
        globalRuleConfigs.removeIf(each -> each instanceof SQLParserRuleConfiguration);
        globalRuleConfigs.add(toBeAlteredRuleConfig);
        persistNewRuleConfigurations(globalRuleConfigs);
    }
    
    private Optional<SQLParserRuleConfiguration> findCurrentConfiguration() {
        return ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().getGlobalRuleMetaData().findRuleConfigurations(SQLParserRuleConfiguration.class).stream().findAny();
    }
    
    private SQLParserRuleConfiguration createSQLParserRuleConfiguration(final SQLParserRuleConfiguration currentConfig) {
        SQLParserRuleConfiguration result = new SQLParserRuleConfiguration();
        result.setSqlCommentParseEnabled(null == sqlStatement.getSqlCommentParseEnable() ? currentConfig.isSqlCommentParseEnabled() : sqlStatement.getSqlCommentParseEnable());
        result.setParseTreeCache(null == sqlStatement.getParseTreeCache() ? currentConfig.getParseTreeCache() : createCacheOption(currentConfig.getParseTreeCache(), sqlStatement.getParseTreeCache()));
        result.setSqlStatementCache(null == sqlStatement.getSqlStatementCache() ? currentConfig.getSqlStatementCache()
                : createCacheOption(currentConfig.getSqlStatementCache(), sqlStatement.getSqlStatementCache()));
        return result;
    }
    
    private CacheOption createCacheOption(final CacheOption cacheOption, final CacheOptionSegment segment) {
        int initialCapacity = null == segment.getInitialCapacity() ? cacheOption.getInitialCapacity() : segment.getInitialCapacity();
        long maximumSize = null == segment.getMaximumSize() ? cacheOption.getMaximumSize() : segment.getMaximumSize();
        return new CacheOption(initialCapacity, maximumSize);
    }
    
    private void persistNewRuleConfigurations(final Collection<RuleConfiguration> globalRuleConfigs) {
        Optional<MetaDataPersistService> metaDataPersistService = ProxyContext.getInstance().getContextManager().getMetaDataContexts().getPersistService();
        if (metaDataPersistService.isPresent() && null != metaDataPersistService.get().getGlobalRuleService()) {
            metaDataPersistService.get().getGlobalRuleService().persist(globalRuleConfigs, true);
        }
    }
}
