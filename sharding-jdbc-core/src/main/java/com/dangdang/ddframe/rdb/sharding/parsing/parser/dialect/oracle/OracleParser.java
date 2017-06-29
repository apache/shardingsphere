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

package com.dangdang.ddframe.rdb.sharding.parsing.parser.dialect.oracle;

import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.dialect.oracle.OracleKeyword;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.dialect.oracle.OracleLexer;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.Symbol;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.SQLParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.limit.Limit;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.limit.OffsetLimit;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.limit.RowCountLimit;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.selectitem.SelectItem;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.expression.SQLExpression;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.expression.SQLIdentifierExpression;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.expression.SQLNumberExpression;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.expression.SQLPlaceholderExpression;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.statement.select.SelectStatement;
import com.google.common.base.Optional;

/**
 * Oracle解析器.
 *
 * @author zhangliang
 */
public final class OracleParser extends SQLParser {
    
    public OracleParser(final String sql, final ShardingRule shardingRule) {
        super(new OracleLexer(sql), shardingRule);
        getLexer().nextToken();
    }
    
    @Override
    public Optional<String> parseAlias() {
        if (equalAny(OracleKeyword.CONNECT)) {
            return Optional.absent();
        }
        return super.parseAlias();
    }
    
    @Override
    protected boolean isSpecialCondition(final SelectStatement selectStatement, final SQLIdentifierExpression expression) {
        Optional<String> rownumAlias = Optional.absent();
        for (SelectItem each : selectStatement.getItems()) {
            if (each.getAlias().isPresent() && "rownum".equalsIgnoreCase(each.getExpression())) {
                rownumAlias = each.getAlias();
            }
        }
        return "rownum".equalsIgnoreCase(expression.getName()) || expression.getName().equalsIgnoreCase(rownumAlias.orNull());
    }
    
    @Override
    protected void parseSpecialCondition(final SelectStatement selectStatement) {
        Symbol symbol = (Symbol) getLexer().getCurrentToken().getType();
        getLexer().nextToken();
        SQLExpression sqlExpression = parseExpression(selectStatement);
        if (null == selectStatement.getLimit()) {
            selectStatement.setLimit(new Limit());
        }
        if (Symbol.LT == symbol || Symbol.LT_EQ == symbol) {
            if (sqlExpression instanceof SQLNumberExpression) {
                selectStatement.getLimit().setRowCountLimit(new RowCountLimit(((SQLNumberExpression) sqlExpression).getNumber().intValue(), -1));
            } else if (sqlExpression instanceof SQLPlaceholderExpression) {
                selectStatement.getLimit().setRowCountLimit(new RowCountLimit(-1, ((SQLPlaceholderExpression) sqlExpression).getIndex()));
            }
        } else if (Symbol.GT == symbol || Symbol.GT_EQ == symbol) {
            if (sqlExpression instanceof SQLNumberExpression) {
                selectStatement.getLimit().setOffsetLimit(new OffsetLimit(((SQLNumberExpression) sqlExpression).getNumber().intValue(), -1));
            } else if (sqlExpression instanceof SQLPlaceholderExpression) {
                selectStatement.getLimit().setOffsetLimit(new OffsetLimit(-1, ((SQLPlaceholderExpression) sqlExpression).getIndex()));
            }
        }
    }
}
