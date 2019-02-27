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

package org.apache.shardingsphere.core.parsing.parser.dialect.sqlserver.clause.facade;

import org.apache.shardingsphere.core.parsing.lexer.LexerEngine;
import org.apache.shardingsphere.core.parsing.parser.clause.HavingClauseParser;
import org.apache.shardingsphere.core.parsing.parser.clause.facade.AbstractSelectClauseParserFacade;
import org.apache.shardingsphere.core.parsing.parser.dialect.sqlserver.clause.SQLServerGroupByClauseParser;
import org.apache.shardingsphere.core.parsing.parser.dialect.sqlserver.clause.SQLServerOrderByClauseParser;
import org.apache.shardingsphere.core.parsing.parser.dialect.sqlserver.clause.SQLServerSelectListClauseParser;
import org.apache.shardingsphere.core.parsing.parser.dialect.sqlserver.clause.SQLServerSelectRestClauseParser;
import org.apache.shardingsphere.core.parsing.parser.dialect.sqlserver.clause.SQLServerTableReferencesClauseParser;
import org.apache.shardingsphere.core.parsing.parser.dialect.sqlserver.clause.SQLServerWhereClauseParser;
import org.apache.shardingsphere.core.rule.ShardingRule;

/**
 * Select clause parser facade for SQLServer.
 *
 * @author zhangliang
 */
public final class SQLServerSelectClauseParserFacade extends AbstractSelectClauseParserFacade {
    
    public SQLServerSelectClauseParserFacade(final ShardingRule shardingRule, final LexerEngine lexerEngine) {
        super(new SQLServerSelectListClauseParser(shardingRule, lexerEngine),
                new SQLServerTableReferencesClauseParser(shardingRule, lexerEngine), new SQLServerWhereClauseParser(lexerEngine), new SQLServerGroupByClauseParser(lexerEngine),
                new HavingClauseParser(lexerEngine), new SQLServerOrderByClauseParser(lexerEngine), new SQLServerSelectRestClauseParser(lexerEngine));
    }
}
