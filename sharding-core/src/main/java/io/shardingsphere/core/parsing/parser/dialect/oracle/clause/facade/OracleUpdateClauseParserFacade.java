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

package io.shardingsphere.core.parsing.parser.dialect.oracle.clause.facade;

import io.shardingsphere.core.parsing.lexer.LexerEngine;
import io.shardingsphere.core.parsing.parser.clause.UpdateSetItemsClauseParser;
import io.shardingsphere.core.parsing.parser.clause.facade.AbstractUpdateClauseParserFacade;
import io.shardingsphere.core.parsing.parser.dialect.oracle.clause.OracleTableReferencesClauseParser;
import io.shardingsphere.core.parsing.parser.dialect.oracle.clause.OracleWhereClauseParser;
import io.shardingsphere.core.rule.ShardingRule;

/**
 * Update clause parser facade for Oracle.
 *
 * @author zhangliang
 */
public final class OracleUpdateClauseParserFacade extends AbstractUpdateClauseParserFacade {
    
    public OracleUpdateClauseParserFacade(final ShardingRule shardingRule, final LexerEngine lexerEngine) {
        super(new OracleTableReferencesClauseParser(shardingRule, lexerEngine), new UpdateSetItemsClauseParser(lexerEngine), new OracleWhereClauseParser(lexerEngine));
    }
}
