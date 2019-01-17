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

package org.apache.shardingsphere.core.parsing.parser.dialect.oracle.sql;

import org.apache.shardingsphere.core.parsing.lexer.LexerEngine;
import org.apache.shardingsphere.core.parsing.lexer.token.DefaultKeyword;
import org.apache.shardingsphere.core.parsing.lexer.token.Keyword;
import org.apache.shardingsphere.core.parsing.parser.dialect.oracle.clause.facade.OracleDeleteClauseParserFacade;
import org.apache.shardingsphere.core.parsing.parser.sql.dml.delete.AbstractDeleteParser;
import org.apache.shardingsphere.core.rule.ShardingRule;

/**
 * Delete parser for Oracle.
 * 
 * @author zhangliang 
 */
public final class OracleDeleteParser extends AbstractDeleteParser {
    
    public OracleDeleteParser(final ShardingRule shardingRule, final LexerEngine lexerEngine) {
        super(shardingRule, lexerEngine, new OracleDeleteClauseParserFacade(shardingRule, lexerEngine));
    }
    
    @Override
    protected Keyword[] getSkippedKeywordsBetweenDeleteAndTable() {
        return new Keyword[] {DefaultKeyword.FROM};
    }
    
    @Override
    protected Keyword[] getUnsupportedKeywordsBetweenDeleteAndTable() {
        return new Keyword[0];
    }
}
