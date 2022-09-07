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

package org.apache.shardingsphere.parser.distsql.handler.update;

import org.apache.shardingsphere.infra.distsql.exception.DistSQLException;
import org.apache.shardingsphere.infra.distsql.update.GlobalRuleRALUpdater;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.parser.config.SQLParserRuleConfiguration;
import org.apache.shardingsphere.parser.distsql.parser.segment.CacheOptionSegment;
import org.apache.shardingsphere.parser.distsql.parser.statement.updatable.AlterSQLParserRuleStatement;
import org.apache.shardingsphere.parser.rule.SQLParserRule;
import org.apache.shardingsphere.sql.parser.api.CacheOption;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;

import java.util.Collection;

/**
 * Alter SQL parser rule statement handler.
 */
public final class AlterSQLParserRuleStatementUpdater implements GlobalRuleRALUpdater {
    
    @Override
    public void executeUpdate(final ShardingSphereMetaData metaData, final SQLStatement sqlStatement) throws DistSQLException {
        SQLParserRuleConfiguration toBeAlteredRuleConfig = createToBeAlteredRuleConfiguration(metaData.getGlobalRuleMetaData(), sqlStatement);
        Collection<ShardingSphereRule> globalRules = metaData.getGlobalRuleMetaData().getRules();
        globalRules.removeIf(each -> each instanceof SQLParserRule);
        globalRules.add(new SQLParserRule(toBeAlteredRuleConfig));
    }
    
    private SQLParserRuleConfiguration createToBeAlteredRuleConfiguration(final ShardingSphereRuleMetaData ruleMetaData, final SQLStatement sqlStatement) {
        AlterSQLParserRuleStatement ruleStatement = (AlterSQLParserRuleStatement) sqlStatement;
        SQLParserRuleConfiguration currentConfig = ruleMetaData.getSingleRule(SQLParserRule.class).getConfiguration();
        boolean sqlCommentParseEnabled = null == ruleStatement.getSqlCommentParseEnable() ? currentConfig.isSqlCommentParseEnabled() : ruleStatement.getSqlCommentParseEnable();
        CacheOption parseTreeCache =
                null == ruleStatement.getParseTreeCache() ? currentConfig.getParseTreeCache() : createCacheOption(currentConfig.getParseTreeCache(), ruleStatement.getParseTreeCache());
        CacheOption sqlStatementCache =
                null == ruleStatement.getSqlStatementCache() ? currentConfig.getSqlStatementCache() : createCacheOption(currentConfig.getSqlStatementCache(), ruleStatement.getSqlStatementCache());
        return new SQLParserRuleConfiguration(sqlCommentParseEnabled, parseTreeCache, sqlStatementCache);
    }
    
    private CacheOption createCacheOption(final CacheOption cacheOption, final CacheOptionSegment segment) {
        int initialCapacity = null == segment.getInitialCapacity() ? cacheOption.getInitialCapacity() : segment.getInitialCapacity();
        long maximumSize = null == segment.getMaximumSize() ? cacheOption.getMaximumSize() : segment.getMaximumSize();
        return new CacheOption(initialCapacity, maximumSize);
    }
    
    @Override
    public String getType() {
        return AlterSQLParserRuleStatement.class.getName();
    }
}
