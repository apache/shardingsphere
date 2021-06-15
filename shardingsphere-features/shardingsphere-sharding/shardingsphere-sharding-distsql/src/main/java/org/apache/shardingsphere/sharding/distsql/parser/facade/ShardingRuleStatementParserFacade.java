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

package org.apache.shardingsphere.sharding.distsql.parser.facade;

import org.apache.shardingsphere.distsql.parser.spi.RuleSQLStatementParserFacade;
import org.apache.shardingsphere.sharding.distsql.parser.core.ShardingRuleDistSQLStatementVisitor;
import org.apache.shardingsphere.sharding.distsql.parser.core.ShardingRuleLexer;
import org.apache.shardingsphere.sharding.distsql.parser.core.ShardingRuleParser;
import org.apache.shardingsphere.sql.parser.api.parser.SQLLexer;
import org.apache.shardingsphere.sql.parser.api.parser.SQLParser;
import org.apache.shardingsphere.sql.parser.api.visitor.SQLVisitor;

/**
 * SQL parser facade for sharding rule statement.
 */
public final class ShardingRuleStatementParserFacade implements RuleSQLStatementParserFacade {
    
    @Override
    public Class<? extends SQLLexer> getLexerClass() {
        return ShardingRuleLexer.class;
    }
    
    @Override
    public Class<? extends SQLParser> getParserClass() {
        return ShardingRuleParser.class;
    }
    
    @Override
    public Class<? extends SQLVisitor> getVisitorClass() {
        return ShardingRuleDistSQLStatementVisitor.class;
    }
    
    @Override
    public String getRuleType() {
        return "Sharding";
    }
}
