package com.dangdang.ddframe.rdb.sharding.parsing.parser.clause;

import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.LexerEngine;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.Assist;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.Symbol;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.condition.Column;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.sql.dml.insert.InsertStatement;
import com.dangdang.ddframe.rdb.sharding.util.SQLUtil;
import com.google.common.base.Optional;
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
