package io.shardingjdbc.core.parsing.parser.clause;

import io.shardingjdbc.core.rule.ShardingRule;
import io.shardingjdbc.core.parsing.lexer.LexerEngine;
import io.shardingjdbc.core.parsing.lexer.token.Assist;
import io.shardingjdbc.core.parsing.lexer.token.Symbol;
import io.shardingjdbc.core.parsing.parser.context.condition.Column;
import io.shardingjdbc.core.parsing.parser.sql.dml.insert.InsertStatement;
import io.shardingjdbc.core.util.SQLUtil;
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
