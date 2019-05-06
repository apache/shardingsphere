/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.core.parsing.parser.dialect.postgresql.clause.facade;

import io.shardingsphere.core.parsing.lexer.LexerEngine;
import io.shardingsphere.core.parsing.parser.clause.InsertColumnsClauseParser;
import io.shardingsphere.core.parsing.parser.clause.facade.AbstractInsertClauseParserFacade;
import io.shardingsphere.core.parsing.parser.dialect.postgresql.clause.PostgreSQLInsertDuplicateKeyUpdateClauseParser;
import io.shardingsphere.core.parsing.parser.dialect.postgresql.clause.PostgreSQLInsertIntoClauseParser;
import io.shardingsphere.core.parsing.parser.dialect.postgresql.clause.PostgreSQLInsertSetClauseParser;
import io.shardingsphere.core.parsing.parser.dialect.postgresql.clause.PostgreSQLInsertValuesClauseParser;
import io.shardingsphere.core.rule.ShardingRule;

/**
 * Insert clause parser facade for PostgreSQL.
 *
 * @author zhangliang
 */
public final class PostgreSQLInsertClauseParserFacade extends AbstractInsertClauseParserFacade {
    
    public PostgreSQLInsertClauseParserFacade(final ShardingRule shardingRule, final LexerEngine lexerEngine) {
        super(new PostgreSQLInsertIntoClauseParser(shardingRule, lexerEngine), new InsertColumnsClauseParser(shardingRule, lexerEngine), 
                new PostgreSQLInsertValuesClauseParser(shardingRule, lexerEngine), new PostgreSQLInsertSetClauseParser(shardingRule, lexerEngine), 
                new PostgreSQLInsertDuplicateKeyUpdateClauseParser(shardingRule, lexerEngine));
    }
}
