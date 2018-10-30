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

package io.shardingsphere.core.parsing.parser.sql.dml.insert;

import com.google.common.base.Optional;
import io.shardingsphere.core.metadata.table.ShardingTableMetaData;
import io.shardingsphere.core.parsing.lexer.LexerEngine;
import io.shardingsphere.core.parsing.lexer.token.DefaultKeyword;
import io.shardingsphere.core.parsing.lexer.token.Symbol;
import io.shardingsphere.core.parsing.parser.clause.facade.AbstractInsertClauseParserFacade;
import io.shardingsphere.core.parsing.parser.context.condition.Column;
import io.shardingsphere.core.parsing.parser.sql.SQLParser;
import io.shardingsphere.core.parsing.parser.sql.dml.DMLStatement;
import io.shardingsphere.core.parsing.parser.token.ItemsToken;
import io.shardingsphere.core.rule.ShardingRule;
import lombok.AccessLevel;
import lombok.Getter;

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
        processGeneratedKey(result);
        return result;
    }
    
    private void processGeneratedKey(final InsertStatement insertStatement) {
        String tableName = insertStatement.getTables().getSingleTableName();
        Optional<Column> generateKeyColumn = shardingRule.getGenerateKeyColumn(tableName);
        if (-1 != insertStatement.getGenerateKeyColumnIndex() || !generateKeyColumn.isPresent()) {
            return;
        }
        if (DefaultKeyword.VALUES.equals(insertStatement.getInsertValues().getInsertValues().get(0).getType())) {
            if (!insertStatement.getItemsTokens().isEmpty()) {
                insertStatement.getItemsTokens().get(0).getItems().add(generateKeyColumn.get().getName());
            } else {
                ItemsToken columnsToken = new ItemsToken(insertStatement.getColumnsListLastPosition());
                columnsToken.getItems().add(generateKeyColumn.get().getName());
                insertStatement.addSQLToken(columnsToken);
            }
        }
    }
}
