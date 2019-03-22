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

package org.apache.shardingsphere.core.parse.parser.dialect.postgresql.clause.facade;

import org.apache.shardingsphere.core.parse.lexer.LexerEngine;
import org.apache.shardingsphere.core.parse.parser.clause.HavingClauseParser;
import org.apache.shardingsphere.core.parse.parser.clause.facade.AbstractSelectClauseParserFacade;
import org.apache.shardingsphere.core.parse.parser.dialect.postgresql.clause.PostgreSQLGroupByClauseParser;
import org.apache.shardingsphere.core.parse.parser.dialect.postgresql.clause.PostgreSQLOrderByClauseParser;
import org.apache.shardingsphere.core.parse.parser.dialect.postgresql.clause.PostgreSQLSelectListClauseParser;
import org.apache.shardingsphere.core.parse.parser.dialect.postgresql.clause.PostgreSQLSelectRestClauseParser;
import org.apache.shardingsphere.core.parse.parser.dialect.postgresql.clause.PostgreSQLTableReferencesClauseParser;
import org.apache.shardingsphere.core.parse.parser.dialect.postgresql.clause.PostgreSQLWhereClauseParser;
import org.apache.shardingsphere.core.rule.ShardingRule;

/**
 * Select clause parser facade for PostgreSQL.
 *
 * @author zhangliang
 */
public final class PostgreSQLSelectClauseParserFacade extends AbstractSelectClauseParserFacade {
    
    public PostgreSQLSelectClauseParserFacade(final ShardingRule shardingRule, final LexerEngine lexerEngine) {
        super(new PostgreSQLSelectListClauseParser(shardingRule, lexerEngine),
                new PostgreSQLTableReferencesClauseParser(shardingRule, lexerEngine), 
                new PostgreSQLWhereClauseParser(lexerEngine), new PostgreSQLGroupByClauseParser(lexerEngine), new HavingClauseParser(lexerEngine), 
                new PostgreSQLOrderByClauseParser(lexerEngine), new PostgreSQLSelectRestClauseParser(lexerEngine));
    }
}
