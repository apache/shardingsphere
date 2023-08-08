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

import org.apache.shardingsphere.distsql.handler.ral.update.GlobalRuleRALUpdater;
import org.apache.shardingsphere.parser.config.SQLParserRuleConfiguration;
import org.apache.shardingsphere.parser.distsql.parser.segment.CacheOptionSegment;
import org.apache.shardingsphere.parser.distsql.parser.statement.updatable.AlterSQLParserRuleStatement;
import org.apache.shardingsphere.sql.parser.api.CacheOption;

/**
 * Alter SQL parser rule statement updater.
 */
public final class AlterSQLParserRuleStatementUpdater implements GlobalRuleRALUpdater<AlterSQLParserRuleStatement, SQLParserRuleConfiguration> {
    
    @Override
    public void checkSQLStatement(final SQLParserRuleConfiguration currentRuleConfig, final AlterSQLParserRuleStatement sqlStatement) {
    }
    
    @Override
    public SQLParserRuleConfiguration buildAlteredRuleConfiguration(final SQLParserRuleConfiguration currentRuleConfig, final AlterSQLParserRuleStatement sqlStatement) {
        boolean sqlCommentParseEnabled = null == sqlStatement.getSqlCommentParseEnabled() ? currentRuleConfig.isSqlCommentParseEnabled() : sqlStatement.getSqlCommentParseEnabled();
        CacheOption parseTreeCache = null == sqlStatement.getParseTreeCache()
                ? currentRuleConfig.getParseTreeCache()
                : createCacheOption(currentRuleConfig.getParseTreeCache(), sqlStatement.getParseTreeCache());
        CacheOption sqlStatementCache = null == sqlStatement.getSqlStatementCache()
                ? currentRuleConfig.getSqlStatementCache()
                : createCacheOption(currentRuleConfig.getSqlStatementCache(), sqlStatement.getSqlStatementCache());
        return new SQLParserRuleConfiguration(sqlCommentParseEnabled, parseTreeCache, sqlStatementCache);
    }
    
    private CacheOption createCacheOption(final CacheOption cacheOption, final CacheOptionSegment segment) {
        int initialCapacity = null == segment.getInitialCapacity() ? cacheOption.getInitialCapacity() : segment.getInitialCapacity();
        long maximumSize = null == segment.getMaximumSize() ? cacheOption.getMaximumSize() : segment.getMaximumSize();
        return new CacheOption(initialCapacity, maximumSize);
    }
    
    @Override
    public Class<SQLParserRuleConfiguration> getRuleConfigurationClass() {
        return SQLParserRuleConfiguration.class;
    }
    
    @Override
    public Class<AlterSQLParserRuleStatement> getType() {
        return AlterSQLParserRuleStatement.class;
    }
}
