/*
 * Copyright 1999-2101 Alibaba Group Holding Ltd.
 *
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
 */

package com.alibaba.druid.sql.parser;

import com.alibaba.druid.sql.SQLEvalConstants;
import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.SQLIgnoreExpr;
import com.alibaba.druid.sql.ast.expr.SQLCharExpr;
import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.expr.SQLIntegerExpr;
import com.alibaba.druid.sql.ast.expr.SQLLiteralExpr;
import com.alibaba.druid.sql.ast.expr.SQLNCharExpr;
import com.alibaba.druid.sql.ast.expr.SQLNumberExpr;
import com.alibaba.druid.sql.ast.expr.SQLPropertyExpr;
import com.alibaba.druid.sql.ast.expr.SQLVariantRefExpr;
import com.alibaba.druid.sql.context.AggregationSelectItemContext;
import com.alibaba.druid.sql.context.CommonSelectItemContext;
import com.alibaba.druid.sql.context.OrderByContext;
import com.alibaba.druid.sql.context.SQLContext;
import com.alibaba.druid.sql.context.SelectItemContext;
import com.alibaba.druid.sql.context.SelectSQLContext;
import com.alibaba.druid.sql.context.TableContext;
import com.alibaba.druid.sql.context.TableToken;
import com.alibaba.druid.sql.lexer.Lexer;
import com.alibaba.druid.sql.lexer.Token;
import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.parser.result.merger.AggregationColumn;
import com.dangdang.ddframe.rdb.sharding.parser.result.merger.OrderByColumn;
import com.dangdang.ddframe.rdb.sharding.parser.result.router.Condition;
import com.dangdang.ddframe.rdb.sharding.parser.result.router.ConditionContext;
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
public class SQLExprParser {
    
    @Getter
    private final Lexer lexer;
    
    @Getter
    private final ShardingRule shardingRule;
    
    @Getter
    private final List<Object> parameters;
    
    @Getter
    @Setter
    private int parametersIndex;
    
    public SQLExprParser(final ShardingRule shardingRule, final List<Object> parameters, final Lexer lexer) {
        this.lexer = lexer;
        this.shardingRule = shardingRule;
        this.parameters = parameters;
    }
    
    protected Optional<String> as() {
        if (lexer.skipIfEqual(Token.AS)) {
            // TODO 判断Literals是符号则返回null, 目前仅判断为LEFT_PAREN
            if (lexer.equalToken(Token.LEFT_PAREN)) {
                return Optional.absent();
            }
            String result = SQLUtil.getExactlyValue(lexer.getLiterals());
            lexer.nextToken();
            return Optional.of(result);
        }
        // TODO 增加哪些数据库识别哪些关键字作为别名的配置
        if (lexer.equalToken(Token.IDENTIFIER, Token.LITERAL_ALIAS, Token.LITERAL_CHARS, Token.USER, Token.END, Token.CASE, Token.KEY, Token.INTERVAL, Token.CONSTRAINT)) {
            String result = SQLUtil.getExactlyValue(lexer.getLiterals());
            lexer.nextToken();
            return Optional.of(result);
        }
        return Optional.absent();
    }
    
    public List<OrderByContext> parseOrderBy(final SQLContext sqlContext) {
        if (!getLexer().skipIfEqual(Token.ORDER)) {
            return Collections.emptyList();
        }
        List<OrderByContext> result = new LinkedList<>();
        getLexer().skipIfEqual(Token.SIBLINGS);
        getLexer().accept(Token.BY);
        OrderByContext orderByContext = parseSelectOrderByItem(sqlContext);
        if (null != orderByContext) {
            result.add(orderByContext);
        }
        while (getLexer().equalToken(Token.COMMA)) {
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
        if (getLexer().equalToken(Token.ASC)) {
            getLexer().nextToken();
        } else if (getLexer().equalToken(Token.DESC)) {
            getLexer().nextToken();
            orderByType = OrderByColumn.OrderByType.DESC;
        }
        if (expr instanceof SQLIntegerExpr) {
            return new OrderByContext(((SQLIntegerExpr) expr).getNumber().intValue(), orderByType);
        }
        if (expr instanceof SQLIdentifierExpr) {
            return new OrderByContext(SQLUtil.getExactlyValue(((SQLIdentifierExpr) expr).getSimpleName()), orderByType);
        }
        if (expr instanceof SQLPropertyExpr) {
            SQLPropertyExpr sqlPropertyExpr = (SQLPropertyExpr) expr;
            return new OrderByContext(SQLUtil.getExactlyValue(sqlPropertyExpr.getOwner().toString()), SQLUtil.getExactlyValue(sqlPropertyExpr.getSimpleName()), orderByType);
        }
        return null;
    }
    
    protected final void parseSingleTable(final SQLContext sqlContext) {
        boolean hasParentheses = false;
        if (getLexer().skipIfEqual(Token.LEFT_PAREN)) {
            if (getLexer().equalToken(Token.SELECT)) {
                throw new UnsupportedOperationException("Cannot support subquery");
            }
            hasParentheses = true;
        }
        TableContext tableContext;
        int beginPosition = getLexer().getCurrentPosition() - getLexer().getLiterals().length();
        String literals = getLexer().getLiterals();
        getLexer().nextToken();
        if (getLexer().skipIfEqual(Token.DOT)) {
            String tableName = getLexer().getLiterals();
            getLexer().nextToken();
            if (hasParentheses) {
                getLexer().accept(Token.RIGHT_PAREN);
            }
            tableContext = new TableContext(tableName, SQLUtil.getExactlyValue(literals), as());
        } else {
            if (hasParentheses) {
                getLexer().accept(Token.RIGHT_PAREN);
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
        if (getLexer().skipIfEqual(Token.LEFT, Token.RIGHT, Token.FULL)) {
            getLexer().skipIfEqual(Token.OUTER);
            getLexer().accept(Token.JOIN);
            return true;
        } else if (getLexer().skipIfEqual(Token.INNER)) {
            getLexer().accept(Token.JOIN);
            return true;
        } else if (getLexer().skipIfEqual(Token.JOIN, Token.COMMA, Token.STRAIGHT_JOIN)) {
            return true;
        } else if (getLexer().skipIfEqual(Token.CROSS)) {
            if (getLexer().skipIfEqual(Token.JOIN, Token.APPLY)) {
                return true;
            }
        } else if (getLexer().skipIfEqual(Token.OUTER)) {
            if (getLexer().skipIfEqual(Token.APPLY)) {
                return true;
            }
        }
        return false;
    }
    
    public final SelectItemContext parseSelectItem(final int index, final SelectSQLContext sqlContext) {
        getLexer().skipIfEqual(Token.CONNECT_BY_ROOT);
        String literals = getLexer().getLiterals();
        if (getLexer().equalToken(Token.STAR) || Token.STAR.getName().equals(SQLUtil.getExactlyValue(literals))) {
            getLexer().nextToken();
            return new CommonSelectItemContext(Token.STAR.getName(), as(), index, true);
        }
        if (getLexer().skipIfEqual(Token.MAX, Token.MIN, Token.SUM, Token.AVG, Token.COUNT)) {
            return new AggregationSelectItemContext(getLexer().skipParentheses(), as(), index, AggregationColumn.AggregationType.valueOf(literals.toUpperCase()));
        }
        StringBuilder expression = new StringBuilder();
        // FIXME 无as的alias解析, 应该做成倒数第二个token不是运算符,倒数第一个token是Identifier或char,则为别名, 不过CommonSelectItemContext类型并不关注expression和alias
        // FIXME 解析xxx.*
        while (!getLexer().equalToken(Token.AS) && !getLexer().equalToken(Token.COMMA) && !getLexer().equalToken(Token.FROM) && !getLexer().equalToken(Token.EOF)) {
            String value = getLexer().getLiterals();
            int position = getLexer().getCurrentPosition() - value.length();
            expression.append(value);
            getLexer().nextToken();
            if (getLexer().equalToken(Token.DOT)) {
                sqlContext.getSqlTokens().add(new TableToken(position, value, SQLUtil.getExactlyValue(value)));
            }
        }
        return new CommonSelectItemContext(SQLUtil.getExactlyValue(expression.toString()), as(), index, false);
    }
    
    public Optional<ConditionContext> parseWhere(final SQLContext sqlContext) {
        if (lexer.skipIfEqual(Token.WHERE)) {
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
        } while (lexer.skipIfEqual(Token.AND));
        if (lexer.equalToken(Token.OR)) {
            throw new ParserUnsupportedException(lexer.getToken());
        }
    }
    
    // TODO 解析组合expr
    public void parseComparisonCondition(final SQLContext sqlContext, final ParseContext parseContext) {
        getLexer().skipIfEqual(Token.LEFT_PAREN);
        SQLExpr left = parseExpr(sqlContext);
        if (lexer.equalToken(Token.EQ)) {
            parseEqualCondition(sqlContext, parseContext, left);
        } else if (lexer.equalToken(Token.IN)) {
            parseInCondition(sqlContext, parseContext, left);
        } else if (lexer.equalToken(Token.BETWEEN)) {
            parseBetweenCondition(sqlContext, parseContext, left);
        } else if (lexer.equalToken(Token.LT) || lexer.equalToken(Token.GT)
                || lexer.equalToken(Token.LT_EQ) || lexer.equalToken(Token.GT_EQ)) {
            parserOtherCondition(sqlContext);
        }
        getLexer().skipIfEqual(Token.LEFT_PAREN);
    }
    
    private void parseEqualCondition(final SQLContext sqlContext, final ParseContext parseContext, final SQLExpr left) {
        lexer.nextToken();
        SQLExpr right = parseExpr(sqlContext);
        // TODO 如果有多表,且找不到column是哪个表的,则不加入condition,以后需要解析binding table
        if ((1 == sqlContext.getTables().size() || left instanceof SQLPropertyExpr) && (right instanceof SQLLiteralExpr || right instanceof SQLVariantRefExpr)) {
            parseContext.addCondition(left, Condition.BinaryOperator.EQUAL, Collections.singletonList(right), parameters);
        }
    }
    
    private void parseInCondition(final SQLContext sqlContext, final ParseContext parseContext, final SQLExpr left) {
        lexer.nextToken();
        lexer.accept(Token.LEFT_PAREN);
        List<SQLExpr> rights = new LinkedList<>();
        do {
            if (lexer.equalToken(Token.COMMA)) {
                lexer.nextToken();
            }
            rights.add(parseExpr(sqlContext));
        } while (!lexer.equalToken(Token.RIGHT_PAREN));
        parseContext.addCondition(left, Condition.BinaryOperator.IN, rights, parameters);
        lexer.nextToken();
    }
    
    private void parseBetweenCondition(final SQLContext sqlContext, final ParseContext parseContext, final SQLExpr left) {
        lexer.nextToken();
        List<SQLExpr> rights = new LinkedList<>();
        rights.add(parseExpr(sqlContext));
        lexer.accept(Token.AND);
        rights.add(parseExpr(sqlContext));
        parseContext.addCondition(left, Condition.BinaryOperator.BETWEEN, rights, parameters);
    }
    
    private void parserOtherCondition(final SQLContext sqlContext) {
        lexer.nextToken();
        parseExpr(sqlContext);
    }
    
    public SQLExpr parseExpr(final SQLContext sqlContext) {
        int beginPosition = lexer.getCurrentPosition();
        SQLExpr result = parseExpr();
        if (result instanceof SQLPropertyExpr) {
            String tableName = sqlContext.getTables().get(0).getName();
            String owner = ((SQLIdentifierExpr) ((SQLPropertyExpr) result).getOwner()).getSimpleName();
            if (tableName.equalsIgnoreCase(SQLUtil.getExactlyValue(owner))) {
                sqlContext.getSqlTokens().add(new TableToken(beginPosition - owner.length(), owner, tableName));
            }
        }
        return result;
    }
    
    public SQLExpr parseExpr() {
        String literals = lexer.getLiterals();
        if (lexer.equalToken(Token.IDENTIFIER)) {
            SQLExpr result = getSQLExpr(SQLUtil.getExactlyValue(literals));
            getLexer().nextToken();
            if (lexer.skipIfEqual(Token.DOT)) {
                String property = lexer.getLiterals();
                getLexer().nextToken();
                if (!lexer.equalToken(Token.PLUS, Token.MINUS, Token.STAR, Token.SLASH)) {
                    return new SQLPropertyExpr(new SQLIdentifierExpr(literals), property);
                }
                skipRest();
                return new SQLIgnoreExpr();
            }
            if (lexer.equalToken(Token.LEFT_PAREN)) {
                getLexer().skipParentheses();
                skipRest();
                return new SQLIgnoreExpr();
            }
            if (!lexer.equalToken(Token.PLUS, Token.MINUS, Token.STAR, Token.SLASH)) {
                return result;
            }
            skipRest();
            return new SQLIgnoreExpr();
        }
        SQLExpr result = getSQLExpr(literals);
        getLexer().nextToken();
        if (!lexer.equalToken(Token.PLUS, Token.MINUS, Token.STAR, Token.SLASH)) {
            return result;
        }
        getLexer().skipParentheses();
        skipRest();
        return new SQLIgnoreExpr();
    }
    
    private void skipRest() {
        while (lexer.skipIfEqual(Token.PLUS, Token.MINUS, Token.STAR, Token.SLASH)) {
            if (getLexer().equalToken(Token.QUESTION)) {
                ++parametersIndex;
            }
            getLexer().nextToken();
            if (lexer.skipIfEqual(Token.DOT)) {
                getLexer().nextToken();
            }
            getLexer().skipParentheses();
        }
    }
    
    private SQLExpr getSQLExpr(final String literals) {
        if (lexer.equalToken(Token.VARIANT) || lexer.equalToken(Token.QUESTION)) {
            SQLVariantRefExpr result = new SQLVariantRefExpr("?");
            result.setIndex(++parametersIndex);
            result.getAttributes().put(SQLEvalConstants.EVAL_VALUE, parameters.get(parametersIndex - 1));
            result.getAttributes().put(SQLEvalConstants.EVAL_VAR_INDEX, parametersIndex - 1);
            return result;
        }
        if (lexer.equalToken(Token.LITERAL_CHARS)) {
            return new SQLCharExpr(literals);
        }
        if (lexer.equalToken(Token.LITERAL_NCHARS)) {
            return new SQLNCharExpr(literals);
        }
        if (lexer.equalToken(Token.LITERAL_INT)) {
            return new SQLIntegerExpr(Integer.parseInt(literals));
        }
        if (lexer.equalToken(Token.LITERAL_FLOAT)) {
            return new SQLNumberExpr(Double.parseDouble(literals));
        }
        if (lexer.equalToken(Token.LITERAL_HEX)) {
            return new SQLNumberExpr(Integer.parseInt(literals, 16));
        }
        if (lexer.equalToken(Token.IDENTIFIER)) {
            return new SQLIdentifierExpr(literals);
        }
        return new SQLIgnoreExpr();
    }
}
