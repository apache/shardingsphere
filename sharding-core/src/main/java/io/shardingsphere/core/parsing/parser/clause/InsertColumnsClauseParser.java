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

package io.shardingsphere.core.parsing.parser.clause;

import com.google.common.base.Optional;
import io.shardingsphere.core.metadata.ShardingMetaData;
import io.shardingsphere.core.parsing.lexer.LexerEngine;
import io.shardingsphere.core.parsing.lexer.token.Assist;
import io.shardingsphere.core.parsing.lexer.token.Symbol;
import io.shardingsphere.core.parsing.parser.context.condition.Column;
import io.shardingsphere.core.parsing.parser.sql.dml.insert.InsertStatement;
import io.shardingsphere.core.parsing.parser.token.InsertColumnToken;
import io.shardingsphere.core.parsing.parser.token.ItemsToken;
import io.shardingsphere.core.rule.ShardingRule;
import io.shardingsphere.core.util.SQLUtil;
import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

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
     * @param shardingMetaData sharding meta data
     */
    public void parse(final InsertStatement insertStatement, final ShardingMetaData shardingMetaData) {
        Collection<Column> result = new LinkedList<>();
        String tableName = insertStatement.getTables().getSingleTableName();
        Optional<Column> generateKeyColumn = shardingRule.getGenerateKeyColumn(tableName);
        int count = 0;
        if (lexerEngine.equalAny(Symbol.LEFT_PAREN)) {
            do {
                lexerEngine.nextToken();
                String columnName = SQLUtil.getExactlyValue(lexerEngine.getCurrentToken().getLiterals());
                result.add(new Column(columnName, tableName));
                lexerEngine.nextToken();
                if (generateKeyColumn.isPresent() && generateKeyColumn.get().getName().equalsIgnoreCase(columnName)) {
                    insertStatement.setGenerateKeyColumnIndex(count);
                }
                count++;
            } while (!lexerEngine.equalAny(Symbol.RIGHT_PAREN) && !lexerEngine.equalAny(Assist.END));
            insertStatement.setColumnsListLastPosition(lexerEngine.getCurrentToken().getEndPosition() - lexerEngine.getCurrentToken().getLiterals().length());
            lexerEngine.nextToken();
        } else {
            List<String> columnNames = shardingMetaData.getTableMetaDataMap().get(tableName).getAllColumnNames();
            int beginPosition = lexerEngine.getCurrentToken().getEndPosition() - lexerEngine.getCurrentToken().getLiterals().length() - 1;
            insertStatement.getSqlTokens().add(new InsertColumnToken(beginPosition, "("));
            ItemsToken columnsToken = new ItemsToken(beginPosition);
            columnsToken.setFirstOfItemsSpecial(true);
            for (String columnName : columnNames) {
                result.add(new Column(columnName, tableName));
                if (generateKeyColumn.isPresent() && generateKeyColumn.get().getName().equalsIgnoreCase(columnName)) {
                    insertStatement.setGenerateKeyColumnIndex(count);
                }
                columnsToken.getItems().add(columnName);
                count++;
            }
            insertStatement.getSqlTokens().add(columnsToken);
            insertStatement.getSqlTokens().add(new InsertColumnToken(beginPosition, ")"));
            insertStatement.setColumnsListLastPosition(beginPosition);
        }
        insertStatement.getColumns().addAll(result);
    }
}
