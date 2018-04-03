/*
 * Copyright 1999-2015 dangdang.com.
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

package io.shardingjdbc.core.parsing.parser.clause;

import com.google.common.base.Optional;
import io.shardingjdbc.core.parsing.lexer.LexerEngine;
import io.shardingjdbc.core.parsing.lexer.token.Assist;
import io.shardingjdbc.core.parsing.lexer.token.Symbol;
import io.shardingjdbc.core.parsing.parser.context.condition.Column;
import io.shardingjdbc.core.parsing.parser.sql.dml.insert.InsertStatement;
import io.shardingjdbc.core.rule.ShardingRule;
import io.shardingjdbc.core.util.SQLUtil;
import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.LinkedList;

/**
 * Insert columns clause parser.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
public final class InsertColumnsClauseParser implements SQLClauseParser {
    
    private final ShardingRule shardingRule;
    
    private final LexerEngine lexerEngine;
    
    /**
     * Parse insert columns.
     *
     * @param insertStatement insert statement
     */
    public void parse(final InsertStatement insertStatement) {
        Collection<Column> result = new LinkedList<>();
        if (lexerEngine.equalAny(Symbol.LEFT_PAREN)) {
            String tableName = insertStatement.getTables().getSingleTableName();
            Optional<String> generateKeyColumn = shardingRule.getGenerateKeyColumn(tableName);
            int count = 0;
            do {
                lexerEngine.nextToken();
                String columnName = SQLUtil.getExactlyValue(lexerEngine.getCurrentToken().getLiterals());
                result.add(new Column(columnName, tableName));
                lexerEngine.nextToken();
                if (generateKeyColumn.isPresent() && generateKeyColumn.get().equalsIgnoreCase(columnName)) {
                    insertStatement.setGenerateKeyColumnIndex(count);
                }
                count++;
            } while (!lexerEngine.equalAny(Symbol.RIGHT_PAREN) && !lexerEngine.equalAny(Assist.END));
            insertStatement.setColumnsListLastPosition(lexerEngine.getCurrentToken().getEndPosition() - lexerEngine.getCurrentToken().getLiterals().length());
            lexerEngine.nextToken();
        }
        insertStatement.getColumns().addAll(result);
    }
}
