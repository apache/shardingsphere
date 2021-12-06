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

package org.apache.shardingsphere.parser.rule;

import lombok.Getter;
import org.apache.shardingsphere.infra.rule.identifier.scope.GlobalRule;
import org.apache.shardingsphere.parser.config.SQLParserRuleConfiguration;
import org.apache.shardingsphere.sql.parser.api.CacheOption;

/**
 * SQL parser rule.
 */
@Getter
public final class SQLParserRule implements GlobalRule {

    private final boolean sqlCommentParseEnabled;

    private final CacheOption sqlStatementCache;

    private final CacheOption parserTreeCache;
    
    public SQLParserRule(final SQLParserRuleConfiguration ruleConfig) {
        this.sqlCommentParseEnabled = ruleConfig.isSqlCommentParseEnabled();
        this.sqlStatementCache = ruleConfig.getSqlStatementCacheOption();
        this.parserTreeCache = ruleConfig.getParseTreeCacheOption();
    }
    
    @Override
    public String getType() {
        return SQLParserRule.class.getSimpleName();
    }
}
