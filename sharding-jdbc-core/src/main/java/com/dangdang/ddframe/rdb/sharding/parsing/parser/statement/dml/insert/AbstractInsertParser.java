/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.rdb.sharding.parsing.parser.statement.dml.insert;

import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.exception.ShardingJdbcException;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.Assist;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.DefaultKeyword;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.Keyword;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.Symbol;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.AbstractSQLParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.GeneratedKey;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.condition.Column;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.condition.Condition;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.condition.Conditions;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.exception.SQLParsingUnsupportedException;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.expression.SQLExpression;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.expression.SQLNumberExpression;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.expression.SQLPlaceholderExpression;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.statement.SQLStatementParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.statement.dml.DMLStatement;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.token.GeneratedKeyToken;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.token.ItemsToken;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.token.MultipleInsertValuesToken;
import com.dangdang.ddframe.rdb.sharding.util.SQLUtil;
import com.google.common.base.Optional;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Insert语句解析器.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
public abstract class AbstractInsertParser implements SQLStatementParser {
    
    @Getter(AccessLevel.PROTECTED)
    private final ShardingRule shardingRule;
    
    @Getter(AccessLevel.PROTECTED)
    private final AbstractSQLParser sqlParser;
    
    private int columnsListLastPosition;
    
    private int afterValuesPosition;
    
    private int valuesListLastPosition;
    
    private int generateKeyColumnIndex = -1;
    
    @Override
    public final DMLStatement parse() {
        sqlParser.getLexer().nextToken();
        InsertStatement result = new InsertStatement();
        parseInto(result);
        parseColumns(result);
        if (sqlParser.equalAny(DefaultKeyword.SELECT, Symbol.LEFT_PAREN)) {
            throw new UnsupportedOperationException("Cannot INSERT SELECT");
        }
        if (sqlParser.skipIfEqual(getValuesKeywords())) {
            afterValuesPosition = sqlParser.getLexer().getCurrentToken().getEndPosition() - sqlParser.getLexer().getCurrentToken().getLiterals().length();
            parseValues(result);
            if (sqlParser.equalAny(Symbol.COMMA)) {
                parseMultipleValues(result);
            }
        } else if (sqlParser.skipIfEqual(getCustomizedInsertKeywords())) {
            parseCustomizedInsert(result);
        }
        appendGenerateKey(result);
        return result;
    }
    
    private void parseInto(final InsertStatement insertStatement) {
        if (sqlParser.equalAny(getUnsupportedKeywordsBeforeInto())) {
            throw new SQLParsingUnsupportedException(sqlParser.getLexer().getCurrentToken().getType());
        }
        sqlParser.skipUntil(DefaultKeyword.INTO);
        sqlParser.getLexer().nextToken();
        sqlParser.parseSingleTable(insertStatement);
        skipBetweenTableAndValues();
    }
    
    protected Keyword[] getUnsupportedKeywordsBeforeInto() {
        return new Keyword[0];
    }
    
    private void skipBetweenTableAndValues() {
        while (sqlParser.skipIfEqual(getSkippedKeywordsBetweenTableAndValues())) {
            sqlParser.getLexer().nextToken();
            if (sqlParser.equalAny(Symbol.LEFT_PAREN)) {
                sqlParser.skipParentheses();
            }
        }
    }
    
    protected Keyword[] getSkippedKeywordsBetweenTableAndValues() {
        return new Keyword[0];
    }
    
    private void parseColumns(final InsertStatement insertStatement) {
        Collection<Column> result = new LinkedList<>();
        if (sqlParser.equalAny(Symbol.LEFT_PAREN)) {
            String tableName = insertStatement.getTables().getSingleTableName();
            Optional<String> generateKeyColumn = shardingRule.getGenerateKeyColumn(tableName);
            int count = 0;
            do {
                sqlParser.getLexer().nextToken();
                String columnName = SQLUtil.getExactlyValue(sqlParser.getLexer().getCurrentToken().getLiterals());
                result.add(new Column(columnName, tableName));
                sqlParser.getLexer().nextToken();
                if (generateKeyColumn.isPresent() && generateKeyColumn.get().equalsIgnoreCase(columnName)) {
                    generateKeyColumnIndex = count;
                }
                count++;
            } while (!sqlParser.equalAny(Symbol.RIGHT_PAREN) && !sqlParser.equalAny(Assist.END));
            columnsListLastPosition = sqlParser.getLexer().getCurrentToken().getEndPosition() - sqlParser.getLexer().getCurrentToken().getLiterals().length();
            sqlParser.getLexer().nextToken();
        }
        insertStatement.getColumns().addAll(result);
    }
    
    protected Keyword[] getValuesKeywords() {
        return new Keyword[] {DefaultKeyword.VALUES};
    }
    
    private void parseValues(final InsertStatement insertStatement) {
        sqlParser.accept(Symbol.LEFT_PAREN);
        List<SQLExpression> sqlExpressions = new LinkedList<>();
        do {
            sqlExpressions.add(sqlParser.parseExpression());
        } while (sqlParser.skipIfEqual(Symbol.COMMA));
        valuesListLastPosition = sqlParser.getLexer().getCurrentToken().getEndPosition() - sqlParser.getLexer().getCurrentToken().getLiterals().length();
        int count = 0;
        for (Column each : insertStatement.getColumns()) {
            SQLExpression sqlExpression = sqlExpressions.get(count);
            insertStatement.getConditions().add(new Condition(each, sqlExpression), shardingRule);
            if (generateKeyColumnIndex == count) {
                insertStatement.setGeneratedKey(createGeneratedKey(each, sqlExpression));
            }
            count++;
        }
        sqlParser.accept(Symbol.RIGHT_PAREN);
    }
    
    private GeneratedKey createGeneratedKey(final Column column, final SQLExpression sqlExpression) {
        GeneratedKey result;
        if (sqlExpression instanceof SQLPlaceholderExpression) {
            result = new GeneratedKey(column.getName(), ((SQLPlaceholderExpression) sqlExpression).getIndex(), null);
        } else if (sqlExpression instanceof SQLNumberExpression) {
            result = new GeneratedKey(column.getName(), -1, ((SQLNumberExpression) sqlExpression).getNumber());
        } else {
            throw new ShardingJdbcException("Generated key only support number.");
        }
        return result;
    }
    
    private void parseMultipleValues(final InsertStatement insertStatement) {
        insertStatement.getMultipleConditions().add(new Conditions(insertStatement.getConditions()));
        MultipleInsertValuesToken valuesToken = new MultipleInsertValuesToken(afterValuesPosition);
        valuesToken.getValues().add(sqlParser.getLexer().getInput().substring(afterValuesPosition, sqlParser.getLexer().getCurrentToken().getEndPosition() - Symbol.COMMA.getLiterals().length()));
        while (sqlParser.skipIfEqual(Symbol.COMMA)) {
            int beginPosition = sqlParser.getLexer().getCurrentToken().getEndPosition() - sqlParser.getLexer().getCurrentToken().getLiterals().length();
            parseValues(insertStatement);
            insertStatement.getMultipleConditions().add(new Conditions(insertStatement.getConditions()));
            int endPosition = sqlParser.equalAny(Symbol.COMMA)
                    ? sqlParser.getLexer().getCurrentToken().getEndPosition() - Symbol.COMMA.getLiterals().length() : sqlParser.getLexer().getCurrentToken().getEndPosition();
            valuesToken.getValues().add(sqlParser.getLexer().getInput().substring(beginPosition, endPosition));
        }
        insertStatement.getSqlTokens().add(valuesToken);
    }
    
    protected Keyword[] getCustomizedInsertKeywords() {
        return new Keyword[0];
    }
    
    protected void parseCustomizedInsert(final InsertStatement insertStatement) {
    }
    
    private void appendGenerateKey(final InsertStatement insertStatement) {
        String tableName = insertStatement.getTables().getSingleTableName();
        Optional<String> generateKeyColumn = shardingRule.getGenerateKeyColumn(tableName);
        if (!generateKeyColumn.isPresent() || null != insertStatement.getGeneratedKey()) {
            return;
        } 
        ItemsToken columnsToken = new ItemsToken(columnsListLastPosition);
        columnsToken.getItems().add(generateKeyColumn.get());
        insertStatement.getSqlTokens().add(columnsToken);
        insertStatement.getSqlTokens().add(new GeneratedKeyToken(valuesListLastPosition));
    }
}
