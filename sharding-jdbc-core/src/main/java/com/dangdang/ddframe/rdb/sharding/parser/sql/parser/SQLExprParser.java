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

package com.dangdang.ddframe.rdb.sharding.parser.sql.parser;

import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.parser.result.merger.AggregationColumn;
import com.dangdang.ddframe.rdb.sharding.parser.result.merger.OrderByColumn;
import com.dangdang.ddframe.rdb.sharding.parser.result.router.Condition;
import com.dangdang.ddframe.rdb.sharding.parser.result.router.ConditionContext;
import com.dangdang.ddframe.rdb.sharding.parser.sql.context.AggregationSelectItemContext;
import com.dangdang.ddframe.rdb.sharding.parser.sql.context.CommonSelectItemContext;
import com.dangdang.ddframe.rdb.sharding.parser.sql.context.OrderByContext;
import com.dangdang.ddframe.rdb.sharding.parser.sql.context.SQLContext;
import com.dangdang.ddframe.rdb.sharding.parser.sql.context.SelectItemContext;
import com.dangdang.ddframe.rdb.sharding.parser.sql.context.SelectSQLContext;
import com.dangdang.ddframe.rdb.sharding.parser.sql.context.TableContext;
import com.dangdang.ddframe.rdb.sharding.parser.sql.context.TableToken;
import com.dangdang.ddframe.rdb.sharding.parser.sql.expr.SQLCharExpr;
import com.dangdang.ddframe.rdb.sharding.parser.sql.expr.SQLExpr;
import com.dangdang.ddframe.rdb.sharding.parser.sql.expr.SQLIdentifierExpr;
import com.dangdang.ddframe.rdb.sharding.parser.sql.expr.SQLIgnoreExpr;
import com.dangdang.ddframe.rdb.sharding.parser.sql.expr.SQLLiteralExpr;
import com.dangdang.ddframe.rdb.sharding.parser.sql.expr.SQLNumberExpr;
import com.dangdang.ddframe.rdb.sharding.parser.sql.expr.SQLPlaceholderExpr;
import com.dangdang.ddframe.rdb.sharding.parser.sql.expr.SQLPropertyExpr;
import com.dangdang.ddframe.rdb.sharding.parser.sql.lexer.Assist;
import com.dangdang.ddframe.rdb.sharding.parser.sql.lexer.DefaultKeyword;
import com.dangdang.ddframe.rdb.sharding.parser.sql.lexer.Literals;
import com.dangdang.ddframe.rdb.sharding.parser.sql.lexer.Lexer;
import com.dangdang.ddframe.rdb.sharding.parser.sql.lexer.Symbol;
import com.dangdang.ddframe.rdb.sharding.parser.visitor.ParseContext;
import com.dangdang.ddframe.rdb.sharding.util.SQLUtil;
import com.google.common.base.Optional;
import lombok.Getter;
import lombok.Setter;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * SQL解析器.
 *
 * @author zhangliang
 */
public class SQLExprParser extends Parser {
    
    @Getter
    private final ShardingRule shardingRule;
    
    @Getter
    private final List<Object> parameters;
    
    @Getter
    @Setter
    private int parametersIndex;
    
    public SQLExprParser(final ShardingRule shardingRule, final List<Object> parameters, final Lexer lexer) {
        super(lexer);
        this.shardingRule = shardingRule;
        this.parameters = parameters;
    }
    
    protected Optional<String> as() {
        if (skipIfEqual(DefaultKeyword.AS)) {
            // TODO 判断Literals是符号则返回null, 目前仅判断为LEFT_PAREN
            if (equal(Symbol.LEFT_PAREN)) {
                return Optional.absent();
            }
            String result = SQLUtil.getExactlyValue(getLexer().getToken().getLiterals());
            getLexer().nextToken();
            return Optional.of(result);
        }
        // TODO 增加哪些数据库识别哪些关键字作为别名的配置
        if (equal(Literals.IDENTIFIER, Literals.CHARS, DefaultKeyword.USER, DefaultKeyword.END, DefaultKeyword.CASE, DefaultKeyword.KEY, DefaultKeyword.INTERVAL, DefaultKeyword.CONSTRAINT)) {
            String result = SQLUtil.getExactlyValue(getLexer().getToken().getLiterals());
            getLexer().nextToken();
            return Optional.of(result);
        }
        return Optional.absent();
    }
    
    public List<OrderByContext> parseOrderBy(final SQLContext sqlContext) {
        if (!skipIfEqual(DefaultKeyword.ORDER)) {
            return Collections.emptyList();
        }
        List<OrderByContext> result = new LinkedList<>();
        skipIfEqual(DefaultKeyword.SIBLINGS);
        accept(DefaultKeyword.BY);
        OrderByContext orderByContext = parseSelectOrderByItem(sqlContext);
        if (null != orderByContext) {
            result.add(orderByContext);
        }
        while (equal(Symbol.COMMA)) {
            getLexer().nextToken();
            orderByContext = parseSelectOrderByItem(sqlContext);
            if (null != orderByContext) {
                result.add(orderByContext);
            }
        }
        return result;
    }
    
    public OrderByContext parseSelectOrderByItem(final SQLContext sqlContext) {
        SQLExpr expr = parseExpr(sqlContext);
        OrderByColumn.OrderByType orderByType = OrderByColumn.OrderByType.ASC;
        if (equal(DefaultKeyword.ASC)) {
            getLexer().nextToken();
        } else if (equal(DefaultKeyword.DESC)) {
            getLexer().nextToken();
            orderByType = OrderByColumn.OrderByType.DESC;
        }
        if (expr instanceof SQLNumberExpr) {
            return new OrderByContext(((SQLNumberExpr) expr).getNumber().intValue(), orderByType);
        }
        if (expr instanceof SQLIdentifierExpr) {
            return new OrderByContext(SQLUtil.getExactlyValue(((SQLIdentifierExpr) expr).getName()), orderByType);
        }
        if (expr instanceof SQLPropertyExpr) {
            SQLPropertyExpr sqlPropertyExpr = (SQLPropertyExpr) expr;
            return new OrderByContext(SQLUtil.getExactlyValue(sqlPropertyExpr.getOwner().getName()), SQLUtil.getExactlyValue(sqlPropertyExpr.getName()), orderByType);
        }
        return null;
    }
    
    protected final void parseSingleTable(final SQLContext sqlContext) {
        boolean hasParentheses = false;
        if (skipIfEqual(Symbol.LEFT_PAREN)) {
            if (equal(DefaultKeyword.SELECT)) {
                throw new UnsupportedOperationException("Cannot support subquery");
            }
            hasParentheses = true;
        }
        TableContext tableContext;
        final int beginPosition = getLexer().getToken().getEndPosition() - getLexer().getToken().getLiterals().length();
        String literals = getLexer().getToken().getLiterals();
        getLexer().nextToken();
        if (skipIfEqual(Symbol.DOT)) {
            String tableName = getLexer().getToken().getLiterals();
            getLexer().nextToken();
            if (hasParentheses) {
                accept(Symbol.RIGHT_PAREN);
            }
            tableContext = new TableContext(tableName, SQLUtil.getExactlyValue(literals), as());
        } else {
            if (hasParentheses) {
                accept(Symbol.RIGHT_PAREN);
            }
            tableContext = new TableContext(literals, SQLUtil.getExactlyValue(literals), as());
        }
        if (isJoin()) {
            throw new UnsupportedOperationException("Cannot support Multiple-Table.");
        }
        sqlContext.getSqlTokens().add(new TableToken(beginPosition, tableContext.getOriginalLiterals(), tableContext.getName()));
        sqlContext.getTables().add(tableContext);
    }
    
    public final boolean isJoin() {
        if (skipIfEqual(DefaultKeyword.LEFT, DefaultKeyword.RIGHT, DefaultKeyword.FULL)) {
            skipIfEqual(DefaultKeyword.OUTER);
            accept(DefaultKeyword.JOIN);
            return true;
        } else if (skipIfEqual(DefaultKeyword.INNER)) {
            accept(DefaultKeyword.JOIN);
            return true;
        } else if (skipIfEqual(DefaultKeyword.JOIN, Symbol.COMMA, DefaultKeyword.STRAIGHT_JOIN)) {
            return true;
        } else if (skipIfEqual(DefaultKeyword.CROSS)) {
            if (skipIfEqual(DefaultKeyword.JOIN, DefaultKeyword.APPLY)) {
                return true;
            }
        } else if (skipIfEqual(DefaultKeyword.OUTER)) {
            if (skipIfEqual(DefaultKeyword.APPLY)) {
                return true;
            }
        }
        return false;
    }
    
    public final SelectItemContext parseSelectItem(final int index, final SelectSQLContext sqlContext) {
        skipIfEqual(DefaultKeyword.CONNECT_BY_ROOT);
        String literals = getLexer().getToken().getLiterals();
        if (equal(Symbol.STAR) || Symbol.STAR.getLiterals().equals(SQLUtil.getExactlyValue(literals))) {
            getLexer().nextToken();
            return new CommonSelectItemContext(Symbol.STAR.getLiterals(), as(), index, true);
        }
        if (skipIfEqual(DefaultKeyword.MAX, DefaultKeyword.MIN, DefaultKeyword.SUM, DefaultKeyword.AVG, DefaultKeyword.COUNT)) {
            return new AggregationSelectItemContext(skipParentheses(), as(), index, AggregationColumn.AggregationType.valueOf(literals.toUpperCase()));
        }
        StringBuilder expression = new StringBuilder();
        // FIXME 无as的alias解析, 应该做成倒数第二个token不是运算符,倒数第一个token是Identifier或char,则为别名, 不过CommonSelectItemContext类型并不关注expression和alias
        // FIXME 解析xxx.*
        while (!equal(DefaultKeyword.AS) && !equal(Symbol.COMMA) && !equal(DefaultKeyword.FROM) && !equal(Assist.EOF)) {
            String value = getLexer().getToken().getLiterals();
            int position = getLexer().getToken().getEndPosition() - value.length();
            expression.append(value);
            getLexer().nextToken();
            if (equal(Symbol.DOT)) {
                sqlContext.getSqlTokens().add(new TableToken(position, value, SQLUtil.getExactlyValue(value)));
            }
        }
        return new CommonSelectItemContext(SQLUtil.getExactlyValue(expression.toString()), as(), index, false);
    }
    
    public Optional<ConditionContext> parseWhere(final SQLContext sqlContext) {
        if (skipIfEqual(DefaultKeyword.WHERE)) {
            ParseContext parseContext = getParseContext(sqlContext);
            parseConditions(sqlContext, parseContext);
            return Optional.of(parseContext.getCurrentConditionContext());
        }
        return Optional.absent();
    }
    
    private ParseContext getParseContext(final SQLContext sqlContext) {
        ParseContext result = new ParseContext(1);
        result.setShardingRule(shardingRule);
        for (TableContext each : sqlContext.getTables()) {
            result.setCurrentTable(each.getName(), each.getAlias());
        }
        return result;
    }
    
    private void parseConditions(final SQLContext sqlContext, final ParseContext parseContext) {
        do {
            parseComparisonCondition(sqlContext, parseContext);
        } while (skipIfEqual(DefaultKeyword.AND));
        if (equal(DefaultKeyword.OR)) {
            throw new ParserUnsupportedException(getLexer().getToken().getType());
        }
    }
    
    // TODO 解析组合expr
    public void parseComparisonCondition(final SQLContext sqlContext, final ParseContext parseContext) {
        skipIfEqual(Symbol.LEFT_PAREN);
        SQLExpr left = parseExpr(sqlContext);
        if (equal(Symbol.EQ)) {
            parseEqualCondition(sqlContext, parseContext, left);
        } else if (equal(DefaultKeyword.IN)) {
            parseInCondition(sqlContext, parseContext, left);
        } else if (equal(DefaultKeyword.BETWEEN)) {
            parseBetweenCondition(sqlContext, parseContext, left);
        } else if (equal(Symbol.LT) || equal(Symbol.GT)
                || equal(Symbol.LT_EQ) || equal(Symbol.GT_EQ)) {
            parserOtherCondition(sqlContext);
        }
        skipIfEqual(Symbol.LEFT_PAREN);
    }
    
    private void parseEqualCondition(final SQLContext sqlContext, final ParseContext parseContext, final SQLExpr left) {
        getLexer().nextToken();
        SQLExpr right = parseExpr(sqlContext);
        // TODO 如果有多表,且找不到column是哪个表的,则不加入condition,以后需要解析binding table
        if ((1 == sqlContext.getTables().size() || left instanceof SQLPropertyExpr) && (right instanceof SQLLiteralExpr || right instanceof SQLPlaceholderExpr)) {
            parseContext.addCondition(left, Condition.BinaryOperator.EQUAL, Collections.singletonList(right));
        }
    }
    
    private void parseInCondition(final SQLContext sqlContext, final ParseContext parseContext, final SQLExpr left) {
        getLexer().nextToken();
        accept(Symbol.LEFT_PAREN);
        List<SQLExpr> rights = new LinkedList<>();
        do {
            if (equal(Symbol.COMMA)) {
                getLexer().nextToken();
            }
            rights.add(parseExpr(sqlContext));
        } while (!equal(Symbol.RIGHT_PAREN));
        parseContext.addCondition(left, Condition.BinaryOperator.IN, rights);
        getLexer().nextToken();
    }
    
    private void parseBetweenCondition(final SQLContext sqlContext, final ParseContext parseContext, final SQLExpr left) {
        getLexer().nextToken();
        List<SQLExpr> rights = new LinkedList<>();
        rights.add(parseExpr(sqlContext));
        accept(DefaultKeyword.AND);
        rights.add(parseExpr(sqlContext));
        parseContext.addCondition(left, Condition.BinaryOperator.BETWEEN, rights);
    }
    
    private void parserOtherCondition(final SQLContext sqlContext) {
        getLexer().nextToken();
        parseExpr(sqlContext);
    }
    
    public SQLExpr parseExpr(final SQLContext sqlContext) {
        int beginPosition = getLexer().getToken().getEndPosition();
        SQLExpr result = parseExpr();
        if (result instanceof SQLPropertyExpr) {
            String tableName = sqlContext.getTables().get(0).getName();
            String owner = (((SQLPropertyExpr) result).getOwner()).getName();
            if (tableName.equalsIgnoreCase(SQLUtil.getExactlyValue(owner))) {
                sqlContext.getSqlTokens().add(new TableToken(beginPosition - owner.length(), owner, tableName));
            }
        }
        return result;
    }
    
    public SQLExpr parseExpr() {
        String literals = getLexer().getToken().getLiterals();
        if (equal(Literals.IDENTIFIER)) {
            SQLExpr result = getSQLExpr(SQLUtil.getExactlyValue(literals));
            getLexer().nextToken();
            if (skipIfEqual(Symbol.DOT)) {
                String property = getLexer().getToken().getLiterals();
                getLexer().nextToken();
                if (!equal(Symbol.PLUS, Symbol.SUB, Symbol.STAR, Symbol.SLASH)) {
                    return new SQLPropertyExpr(new SQLIdentifierExpr(literals), property);
                }
                skipRest();
                return new SQLIgnoreExpr();
            }
            if (equal(Symbol.LEFT_PAREN)) {
                skipParentheses();
                skipRest();
                return new SQLIgnoreExpr();
            }
            if (!equal(Symbol.PLUS, Symbol.SUB, Symbol.STAR, Symbol.SLASH)) {
                return result;
            }
            skipRest();
            return new SQLIgnoreExpr();
        }
        SQLExpr result = getSQLExpr(literals);
        getLexer().nextToken();
        if (!equal(Symbol.PLUS, Symbol.SUB, Symbol.STAR, Symbol.SLASH)) {
            return result;
        }
        skipParentheses();
        skipRest();
        return new SQLIgnoreExpr();
    }
    
    private void skipRest() {
        while (skipIfEqual(Symbol.PLUS, Symbol.SUB, Symbol.STAR, Symbol.SLASH)) {
            if (equal(Symbol.QUESTION)) {
                ++parametersIndex;
            }
            getLexer().nextToken();
            if (skipIfEqual(Symbol.DOT)) {
                getLexer().nextToken();
            }
            skipParentheses();
        }
    }
    
    private SQLExpr getSQLExpr(final String literals) {
        if (equal(Symbol.QUESTION)) {
            parametersIndex++;
            return new SQLPlaceholderExpr(parametersIndex - 1, parameters.get(parametersIndex - 1));
        }
        if (equal(Literals.CHARS)) {
            return new SQLCharExpr(literals);
        }
        if (equal(Literals.INT)) {
            return new SQLNumberExpr(Integer.parseInt(literals));
        }
        if (equal(Literals.FLOAT)) {
            return new SQLNumberExpr(Double.parseDouble(literals));
        }
        if (equal(Literals.HEX)) {
            return new SQLNumberExpr(Integer.parseInt(literals, 16));
        }
        if (equal(Literals.IDENTIFIER)) {
            return new SQLIdentifierExpr(literals);
        }
        return new SQLIgnoreExpr();
    }
}
