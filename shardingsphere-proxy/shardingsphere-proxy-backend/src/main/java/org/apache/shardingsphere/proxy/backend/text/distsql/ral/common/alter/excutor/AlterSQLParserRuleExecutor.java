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

package org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.alter.excutor;

import lombok.AllArgsConstructor;
import org.apache.shardingsphere.distsql.parser.segment.CacheOptionSegment;
import org.apache.shardingsphere.distsql.parser.statement.ral.common.alter.AlterSQLParserRuleStatement;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistService;
import org.apache.shardingsphere.parser.config.SQLParserRuleConfiguration;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.alter.AlterStatementExecutor;
import org.apache.shardingsphere.sql.parser.api.CacheOption;

import java.util.Collection;
import java.util.Optional;

/**
 * Alter SQL parser rule statement executor.
 */
@AllArgsConstructor
public final class AlterSQLParserRuleExecutor implements AlterStatementExecutor {
    
    private final AlterSQLParserRuleStatement sqlStatement;
    
    @Override
    public ResponseHeader execute() {
        updateSQLParserRule();
        return new UpdateResponseHeader(sqlStatement);
    }
    
    private void updateSQLParserRule() {
        MetaDataContexts metaDataContexts = ProxyContext.getInstance().getContextManager().getMetaDataContexts();
        Collection<RuleConfiguration> globalRuleConfigurations = metaDataContexts.getGlobalRuleMetaData().getConfigurations();
        RuleConfiguration ruleConfiguration = globalRuleConfigurations.stream().filter(configuration -> configuration instanceof SQLParserRuleConfiguration).findFirst().orElse(null);
        if (ruleConfiguration instanceof SQLParserRuleConfiguration) {
            SQLParserRuleConfiguration toBeAdd = buildSQLParserRuleConfiguration((SQLParserRuleConfiguration) ruleConfiguration);
            globalRuleConfigurations.removeIf(configuration -> configuration instanceof SQLParserRuleConfiguration);
            globalRuleConfigurations.add(toBeAdd);
            Optional<MetaDataPersistService> metaDataPersistService = metaDataContexts.getMetaDataPersistService();
            if (metaDataPersistService.isPresent() && null != metaDataPersistService.get().getPropsService()) {
                metaDataPersistService.get().getGlobalRuleService().persist(globalRuleConfigurations, true);
            }
        }
    }

    private SQLParserRuleConfiguration buildSQLParserRuleConfiguration(final SQLParserRuleConfiguration ruleConfiguration) {
        SQLParserRuleConfiguration result = new SQLParserRuleConfiguration();
        result.setSqlCommentParseEnabled(null == sqlStatement.getSqlCommentParseEnable() ? ruleConfiguration.isSqlCommentParseEnabled() 
                : sqlStatement.getSqlCommentParseEnable());
        result.setParseTreeCache(null == sqlStatement.getParserTreeCache() ? ruleConfiguration.getParseTreeCache()
                : buildCacheOption(ruleConfiguration.getParseTreeCache(), sqlStatement.getParserTreeCache()));
        result.setSqlStatementCache(null == sqlStatement.getSqlStatementCache() ? ruleConfiguration.getSqlStatementCache()
                : buildCacheOption(ruleConfiguration.getSqlStatementCache(), sqlStatement.getSqlStatementCache()));
        return result;
    }
    
    private CacheOption buildCacheOption(final CacheOption cacheOption, final CacheOptionSegment segment) {
        CacheOption result = new CacheOption();
        result.setInitialCapacity(null == segment.getInitialCapacity() ? cacheOption.getInitialCapacity() : segment.getInitialCapacity());
        result.setMaximumSize(null == segment.getMaximumSize() ? cacheOption.getMaximumSize() : segment.getMaximumSize());
        result.setConcurrencyLevel(null == segment.getConcurrencyLevel() ? cacheOption.getConcurrencyLevel() : segment.getConcurrencyLevel());
        return result;
    }
}
