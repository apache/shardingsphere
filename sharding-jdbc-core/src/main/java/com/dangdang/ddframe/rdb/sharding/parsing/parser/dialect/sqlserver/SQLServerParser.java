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

package com.dangdang.ddframe.rdb.sharding.parsing.parser.dialect.sqlserver;

import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.dialect.sqlserver.SQLServerKeyword;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.dialect.sqlserver.SQLServerLexer;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.DefaultKeyword;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.Literals;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.Symbol;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.SQLParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.limit.Limit;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.limit.OffsetLimit;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.limit.RowCountLimit;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.selectitem.SelectItem;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.exception.SQLParsingException;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.exception.SQLParsingUnsupportedException;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.expression.SQLIdentifierExpression;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.statement.select.SelectStatement;
import com.google.common.base.Optional;

/**
 * SQLServer解析器.
 *
 * @author zhangliang
 */
public final class SQLServerParser extends SQLParser {
    
    public SQLServerParser(final String sql, final ShardingRule shardingRule) {
        super(new SQLServerLexer(sql), shardingRule);
        getLexer().nextToken();
    }
    
    @Override
    protected boolean isRowNumberCondition(final SelectStatement selectStatement, final SQLIdentifierExpression expression) {
        Optional<String> rowNumberAlias = Optional.absent();
        for (SelectItem each : selectStatement.getItems()) {
            if (each.getAlias().isPresent() && "ROW_NUMBER".equalsIgnoreCase(each.getExpression())) {
                rowNumberAlias = each.getAlias();
            }
        }
        return expression.getName().equalsIgnoreCase(rowNumberAlias.orNull());
    }
    
    public void skipTop() {
        if (skipIfEqual(SQLServerKeyword.TOP)) {
            parseExpression();
            skipIfEqual(SQLServerKeyword.PERCENT);
        }
    }
    
    protected void skipOutput() {
        if (equalAny(SQLServerKeyword.OUTPUT)) {
            throw new SQLParsingUnsupportedException(SQLServerKeyword.OUTPUT);
        }
    }
    
    public void parseOffset(final SelectStatement selectStatement) {
        getLexer().nextToken();
        int offset;
        int offsetIndex = -1;
        if (equalAny(Literals.INT)) {
            offset = Integer.parseInt(getLexer().getCurrentToken().getLiterals());
        } else if (equalAny(Symbol.QUESTION)) {
            offsetIndex = getParametersIndex();
            offset = -1;
            setParametersIndex(offsetIndex + 1);
        } else {
            throw new SQLParsingException(getLexer());
        }
        getLexer().nextToken();
        Limit limit;
        if (skipIfEqual(DefaultKeyword.FETCH)) {
            getLexer().nextToken();
            int rowCount;
            int rowCountIndex = -1;
            getLexer().nextToken();
            if (equalAny(Literals.INT)) {
                rowCount = Integer.parseInt(getLexer().getCurrentToken().getLiterals());
            } else if (equalAny(Symbol.QUESTION)) {
                rowCountIndex = getParametersIndex();
                rowCount = -1;
                setParametersIndex(rowCountIndex + 1);
            } else {
                throw new SQLParsingException(getLexer());
            }
            getLexer().nextToken();
            getLexer().nextToken();
            limit = new Limit(true, new OffsetLimit(offset, offsetIndex), new RowCountLimit(rowCount, rowCountIndex));
        } else {
            limit = new Limit(true, new OffsetLimit(offset, offsetIndex));
        }
        selectStatement.setLimit(limit);
    }
}
