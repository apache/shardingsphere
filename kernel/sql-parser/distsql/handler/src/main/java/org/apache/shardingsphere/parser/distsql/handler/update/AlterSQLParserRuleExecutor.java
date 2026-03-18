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

import lombok.Setter;
import org.apache.shardingsphere.distsql.handler.engine.update.rdl.rule.spi.global.GlobalRuleDefinitionExecutor;
import org.apache.shardingsphere.parser.config.SQLParserRuleConfiguration;
import org.apache.shardingsphere.parser.distsql.segment.CacheOptionSegment;
import org.apache.shardingsphere.parser.distsql.statement.updatable.AlterSQLParserRuleStatement;
import org.apache.shardingsphere.parser.rule.SQLParserRule;
import org.apache.shardingsphere.sql.parser.engine.api.CacheOption;

/**
 * Alter SQL parser rule executor.
 */
@Setter
public final class AlterSQLParserRuleExecutor implements GlobalRuleDefinitionExecutor<AlterSQLParserRuleStatement, SQLParserRule> {
    
    private SQLParserRule rule;
    
    @Override
    public SQLParserRuleConfiguration buildToBeAlteredRuleConfiguration(final AlterSQLParserRuleStatement sqlStatement) {
        CacheOption parseTreeCache = null == sqlStatement.getParseTreeCache()
                ? rule.getConfiguration().getParseTreeCache()
                : createCacheOption(rule.getConfiguration().getParseTreeCache(), sqlStatement.getParseTreeCache());
        CacheOption sqlStatementCache = null == sqlStatement.getSqlStatementCache()
                ? rule.getConfiguration().getSqlStatementCache()
                : createCacheOption(rule.getConfiguration().getSqlStatementCache(), sqlStatement.getSqlStatementCache());
        return new SQLParserRuleConfiguration(parseTreeCache, sqlStatementCache);
    }
    
    private CacheOption createCacheOption(final CacheOption cacheOption, final CacheOptionSegment segment) {
        int initialCapacity = null == segment.getInitialCapacity() ? cacheOption.getInitialCapacity() : segment.getInitialCapacity();
        long maximumSize = null == segment.getMaximumSize() ? cacheOption.getMaximumSize() : segment.getMaximumSize();
        return new CacheOption(initialCapacity, maximumSize);
    }
    
    @Override
    public Class<SQLParserRule> getRuleClass() {
        return SQLParserRule.class;
    }
    
    @Override
    public Class<AlterSQLParserRuleStatement> getType() {
        return AlterSQLParserRuleStatement.class;
    }
}
