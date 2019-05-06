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

package io.shardingsphere.core.parsing.parser.dialect.mysql.clause.facade;

import io.shardingsphere.core.parsing.lexer.LexerEngine;
import io.shardingsphere.core.parsing.parser.clause.HavingClauseParser;
import io.shardingsphere.core.parsing.parser.clause.facade.AbstractSelectClauseParserFacade;
import io.shardingsphere.core.parsing.parser.dialect.mysql.clause.MySQLGroupByClauseParser;
import io.shardingsphere.core.parsing.parser.dialect.mysql.clause.MySQLOrderByClauseParser;
import io.shardingsphere.core.parsing.parser.dialect.mysql.clause.MySQLSelectListClauseParser;
import io.shardingsphere.core.parsing.parser.dialect.mysql.clause.MySQLSelectRestClauseParser;
import io.shardingsphere.core.parsing.parser.dialect.mysql.clause.MySQLTableReferencesClauseParser;
import io.shardingsphere.core.parsing.parser.dialect.mysql.clause.MySQLWhereClauseParser;
import io.shardingsphere.core.rule.ShardingRule;

/**
 * Select clause parser facade for MySQL.
 *
 * @author zhangliang
 */
public final class MySQLSelectClauseParserFacade extends AbstractSelectClauseParserFacade {
    
    public MySQLSelectClauseParserFacade(final ShardingRule shardingRule, final LexerEngine lexerEngine) {
        super(new MySQLSelectListClauseParser(shardingRule, lexerEngine),
                new MySQLTableReferencesClauseParser(shardingRule, lexerEngine), new MySQLWhereClauseParser(lexerEngine), new MySQLGroupByClauseParser(lexerEngine),
                new HavingClauseParser(lexerEngine), new MySQLOrderByClauseParser(lexerEngine), new MySQLSelectRestClauseParser(lexerEngine));
    }
}
