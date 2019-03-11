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

package org.apache.shardingsphere.core.parsing.parser.sql.dml.insert;

import com.google.common.base.Optional;
import lombok.AccessLevel;
import lombok.Getter;
import org.apache.shardingsphere.core.metadata.table.ShardingTableMetaData;
import org.apache.shardingsphere.core.parsing.lexer.LexerEngine;
import org.apache.shardingsphere.core.parsing.lexer.token.DefaultKeyword;
import org.apache.shardingsphere.core.parsing.lexer.token.Symbol;
import org.apache.shardingsphere.core.parsing.parser.clause.facade.AbstractInsertClauseParserFacade;
import org.apache.shardingsphere.core.parsing.parser.context.condition.Column;
import org.apache.shardingsphere.core.parsing.parser.sql.SQLParser;
import org.apache.shardingsphere.core.parsing.parser.sql.dml.DMLStatement;
import org.apache.shardingsphere.core.rule.ShardingRule;

/**
 * Insert parser.
 *
 * @author zhangliang
 * @author panjuan
 * @author maxiaoguang
 */
public abstract class AbstractInsertParser implements SQLParser {
    
    @Getter(AccessLevel.PROTECTED)
    private final ShardingRule shardingRule;
    
    @Getter(AccessLevel.PROTECTED)
    private final ShardingTableMetaData shardingTableMetaData;
    
    @Getter(AccessLevel.PROTECTED)
    private final LexerEngine lexerEngine;
    
    private final AbstractInsertClauseParserFacade insertClauseParserFacade;
    
    public AbstractInsertParser(
            final ShardingRule shardingRule, final ShardingTableMetaData shardingTableMetaData, final LexerEngine lexerEngine, final AbstractInsertClauseParserFacade insertClauseParserFacade) {
        this.shardingRule = shardingRule;
        this.shardingTableMetaData = shardingTableMetaData;
        this.lexerEngine = lexerEngine;
        this.insertClauseParserFacade = insertClauseParserFacade;
    }
    
    @Override
    public final DMLStatement parse() {
        lexerEngine.nextToken();
        InsertStatement result = new InsertStatement();
        insertClauseParserFacade.getInsertIntoClauseParser().parse(result);
        insertClauseParserFacade.getInsertColumnsClauseParser().parse(result, shardingTableMetaData);
        if (lexerEngine.equalAny(DefaultKeyword.SELECT, Symbol.LEFT_PAREN)) {
            throw new UnsupportedOperationException("Cannot INSERT SELECT");
        }
        insertClauseParserFacade.getInsertValuesClauseParser().parse(result);
        insertClauseParserFacade.getInsertSetClauseParser().parse(result);
        insertClauseParserFacade.getInsertDuplicateKeyUpdateClauseParser().parse(result);
        return result;
    }
}
