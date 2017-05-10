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

package com.dangdang.ddframe.rdb.sharding.parsing.parser;

import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.AggregationSelectItemContext;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.CommonSelectItemContext;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.ConditionContext;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.SQLBuilderContext;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.SQLContext;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.SelectItemContext;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.ShardingColumnContext;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.TableContext;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.TableToken;
import com.dangdang.ddframe.rdb.sharding.constant.AggregationType;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.exception.SQLParsingUnsupportedException;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.expr.SQLTextExpr;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.expr.SQLExpr;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.expr.SQLIdentifierExpr;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.expr.SQLIgnoreExpr;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.expr.SQLNumberExpr;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.expr.SQLPlaceholderExpr;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.expr.SQLPropertyExpr;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.Lexer;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.Assist;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.DefaultKeyword;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.Literals;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.Symbol;
import com.dangdang.ddframe.rdb.sharding.util.SQLUtil;
import com.google.common.base.Optional;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedList;
import java.util.List;

/**
 * SQL解析器.
 *
 * @author zhangliang
 */
@Getter
public class SQLParser extends AbstractParser {
    
    private final ShardingRule shardingRule;
    
    private final SQLBuilderContext sqlBuilderContext;
    
    @Setter
    private int parametersIndex;
    
    public SQLParser(final Lexer lexer, final ShardingRule shardingRule) {
        super(lexer);
        this.shardingRule = shardingRule;
        sqlBuilderContext = new SQLBuilderContext(lexer.getInput());
    }
    
    /**
     * 解析表达式.
     *
     * @param sqlContext SQL上下文
     * @return 表达式
     */
    public final SQLExpr parseExpression(final SQLContext sqlContext) {
        int beginPosition = getLexer().getCurrentToken().getEndPosition();
        SQLExpr result = parseExpression();
        if (result instanceof SQLPropertyExpr) {
            setTableToken(sqlContext, beginPosition, (SQLPropertyExpr) result);
        }
        return result;
    }
    
    /**
     * 解析表达式.
     *
     * @return 表达式
     */
    public final SQLExpr parseExpression() {
        String literals = getLexer().getCurrentToken().getLiterals();
        final SQLExpr expression = getExpression(literals);
        if (skipIfEqual(Literals.IDENTIFIER)) {
            if (skipIfEqual(Symbol.DOT)) {
                String property = getLexer().getCurrentToken().getLiterals();
                getLexer().nextToken();
                return skipIfCompositeExpression() ? new SQLIgnoreExpr() : new SQLPropertyExpr(new SQLIdentifierExpr(literals), property);
            }
            if (equalAny(Symbol.LEFT_PAREN)) {
                skipParentheses();
                skipRestCompositeExpression();
                return new SQLIgnoreExpr();
            }
            return skipIfCompositeExpression() ? new SQLIgnoreExpr() : expression;
        }
        getLexer().nextToken();
        return skipIfCompositeExpression() ? new SQLIgnoreExpr() : expression;
    }
    
    private SQLExpr getExpression(final String literals) {
        if (equalAny(Symbol.QUESTION)) {
            parametersIndex++;
            return new SQLPlaceholderExpr(parametersIndex - 1);
        }
        if (equalAny(Literals.CHARS)) {
            return new SQLTextExpr(literals);
        }
        // TODO 考虑long的情况
        if (equalAny(Literals.INT)) {
            return new SQLNumberExpr(Integer.parseInt(literals));
        }
        if (equalAny(Literals.FLOAT)) {
            return new SQLNumberExpr(Double.parseDouble(literals));
        }
        // TODO 考虑long的情况
        if (equalAny(Literals.HEX)) {
            return new SQLNumberExpr(Integer.parseInt(literals, 16));
        }
        if (equalAny(Literals.IDENTIFIER)) {
            return new SQLIdentifierExpr(SQLUtil.getExactlyValue(literals));
        }
        return new SQLIgnoreExpr();
    }
    
    private boolean skipIfCompositeExpression() {
        if (equalAny(Symbol.PLUS, Symbol.SUB, Symbol.STAR, Symbol.SLASH, Symbol.PERCENT, Symbol.AMP, Symbol.BAR, Symbol.DOUBLE_AMP, Symbol.DOUBLE_BAR, Symbol.CARET, Symbol.DOT, Symbol.LEFT_PAREN)) {
            skipParentheses();
            skipRestCompositeExpression();
            return true;
        }
        return false;
    }
    
    private void skipRestCompositeExpression() {
        while (skipIfEqual(Symbol.PLUS, Symbol.SUB, Symbol.STAR, Symbol.SLASH, Symbol.PERCENT, Symbol.AMP, Symbol.BAR, Symbol.DOUBLE_AMP, Symbol.DOUBLE_BAR, Symbol.CARET, Symbol.DOT)) {
            if (equalAny(Symbol.QUESTION)) {
                parametersIndex++;
            }
            getLexer().nextToken();
            skipParentheses();
        }
    }
    
    private void setTableToken(final SQLContext sqlContext, final int beginPosition, final SQLPropertyExpr propertyExpr) {
        String tableName = sqlContext.getTables().get(0).getName();
        String owner = propertyExpr.getOwner().getName();
        if (tableName.equalsIgnoreCase(SQLUtil.getExactlyValue(owner))) {
            sqlBuilderContext.getSqlTokens().add(new TableToken(beginPosition - owner.length(), owner, tableName));
        }
    }
    
    /**
     * 解析别名.
     *
     * @return 别名
     */
    public Optional<String> parseAlias() {
        if (skipIfEqual(DefaultKeyword.AS)) {
            if (equalAny(Symbol.values())) {
                return Optional.absent();
            }
            String result = SQLUtil.getExactlyValue(getLexer().getCurrentToken().getLiterals());
            getLexer().nextToken();
            return Optional.of(result);
        }
        // TODO 增加哪些数据库识别哪些关键字作为别名的配置
        if (equalAny(Literals.IDENTIFIER, Literals.CHARS, DefaultKeyword.USER, DefaultKeyword.END, DefaultKeyword.CASE, DefaultKeyword.KEY, DefaultKeyword.INTERVAL, DefaultKeyword.CONSTRAINT)) {
            String result = SQLUtil.getExactlyValue(getLexer().getCurrentToken().getLiterals());
            getLexer().nextToken();
            return Optional.of(result);
        }
        return Optional.absent();
    }
    
    /**
     * 解析单表.
     *
     * @param sqlContext SQL上下文
     */
    public final void parseSingleTable(final SQLContext sqlContext) {
        boolean hasParentheses = false;
        if (skipIfEqual(Symbol.LEFT_PAREN)) {
            if (equalAny(DefaultKeyword.SELECT)) {
                throw new UnsupportedOperationException("Cannot support subquery");
            }
            hasParentheses = true;
        }
        TableContext tableContext;
        final int beginPosition = getLexer().getCurrentToken().getEndPosition() - getLexer().getCurrentToken().getLiterals().length();
        String literals = getLexer().getCurrentToken().getLiterals();
        getLexer().nextToken();
        if (skipIfEqual(Symbol.DOT)) {
            String tableName = getLexer().getCurrentToken().getLiterals();
            getLexer().nextToken();
            if (hasParentheses) {
                accept(Symbol.RIGHT_PAREN);
            }
            tableContext = new TableContext(tableName, SQLUtil.getExactlyValue(literals), parseAlias());
        } else {
            if (hasParentheses) {
                accept(Symbol.RIGHT_PAREN);
            }
            tableContext = new TableContext(literals, SQLUtil.getExactlyValue(literals), parseAlias());
        }
        if (skipJoin()) {
            throw new UnsupportedOperationException("Cannot support Multiple-Table.");
        }
        sqlBuilderContext.getSqlTokens().add(new TableToken(beginPosition, tableContext.getOriginalLiterals(), tableContext.getName()));
        sqlContext.getTables().add(tableContext);
    }
    
    /**
     * 跳过表关联.
     *
     * @return 是否表关联.
     */
    public final boolean skipJoin() {
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
    
    /**
     * 解析查询列.
     *
     * @param index 参数索引
     * @return 查询列上下文
     */
    public final SelectItemContext parseSelectItem(final int index) {
        skipIfEqual(DefaultKeyword.CONNECT_BY_ROOT);
        String literals = getLexer().getCurrentToken().getLiterals();
        if (equalAny(Symbol.STAR) || Symbol.STAR.getLiterals().equals(SQLUtil.getExactlyValue(literals))) {
            getLexer().nextToken();
            return new CommonSelectItemContext(Symbol.STAR.getLiterals(), parseAlias(), true);
        }
        if (skipIfEqual(DefaultKeyword.MAX, DefaultKeyword.MIN, DefaultKeyword.SUM, DefaultKeyword.AVG, DefaultKeyword.COUNT)) {
            return new AggregationSelectItemContext(skipParentheses(), parseAlias(), index, AggregationType.valueOf(literals.toUpperCase()));
        }
        StringBuilder expression = new StringBuilder();
        // FIXME 无as的alias解析, 应该做成倒数第二个token不是运算符,倒数第一个token是Identifier或char,则为别名, 不过CommonSelectItemContext类型并不关注expression和alias
        // FIXME 解析xxx.*
        while (!equalAny(DefaultKeyword.AS) && !equalAny(Symbol.COMMA) && !equalAny(DefaultKeyword.FROM) && !equalAny(Assist.END)) {
            String value = getLexer().getCurrentToken().getLiterals();
            int position = getLexer().getCurrentToken().getEndPosition() - value.length();
            expression.append(value);
            getLexer().nextToken();
            if (equalAny(Symbol.DOT)) {
                sqlBuilderContext.getSqlTokens().add(new TableToken(position, value, SQLUtil.getExactlyValue(value)));
            }
        }
        return new CommonSelectItemContext(SQLUtil.getExactlyValue(expression.toString()), parseAlias(), false);
    }
    
    /**
     * 解析查询条件.
     *
     * @param sqlContext SQL上下文
     */
    public final void parseWhere(final SQLContext sqlContext) {
        if (skipIfEqual(DefaultKeyword.WHERE)) {
            parseConditions(sqlContext);
        }
    }
    
    private void parseConditions(final SQLContext sqlContext) {
        sqlContext.setConditionContext(new ConditionContext());
        do {
            parseComparisonCondition(sqlContext);
        } while (skipIfEqual(DefaultKeyword.AND));
        if (equalAny(DefaultKeyword.OR)) {
            throw new SQLParsingUnsupportedException(getLexer().getCurrentToken().getType());
        }
    }
    
    // TODO 解析组合expr
    public final void parseComparisonCondition(final SQLContext sqlContext) {
        skipIfEqual(Symbol.LEFT_PAREN);
        SQLExpr left = parseExpression(sqlContext);
        if (equalAny(Symbol.EQ)) {
            parseEqualCondition(sqlContext, left);
            return;
        }
        if (equalAny(DefaultKeyword.IN)) {
            parseInCondition(sqlContext, left);
            return;
        }
        if (equalAny(DefaultKeyword.BETWEEN)) {
            parseBetweenCondition(sqlContext, left);
            return;
        }
        if (equalAny(Symbol.LT) || equalAny(Symbol.GT) || equalAny(Symbol.LT_EQ) || equalAny(Symbol.GT_EQ)) {
            parserOtherCondition(sqlContext);
        }
        skipIfEqual(Symbol.LEFT_PAREN);
    }
    
    private void parseEqualCondition(final SQLContext sqlContext, final SQLExpr left) {
        getLexer().nextToken();
        SQLExpr right = parseExpression(sqlContext);
        // TODO 如果有多表,且找不到column是哪个表的,则不加入condition,以后需要解析binding table
        if ((1 == sqlContext.getTables().size() || left instanceof SQLPropertyExpr) && (right instanceof SQLNumberExpr || right instanceof SQLTextExpr || right instanceof SQLPlaceholderExpr)) {
            Optional<ShardingColumnContext> column = sqlContext.findColumn(left);
            if (column.isPresent() && shardingRule.isShardingColumn(column.get())) {
                sqlContext.getConditionContext().add(new ConditionContext.Condition(column.get(), right));
            }
        }
    }
    
    private void parseInCondition(final SQLContext sqlContext, final SQLExpr left) {
        getLexer().nextToken();
        accept(Symbol.LEFT_PAREN);
        List<SQLExpr> rights = new LinkedList<>();
        do {
            if (equalAny(Symbol.COMMA)) {
                getLexer().nextToken();
            }
            rights.add(parseExpression(sqlContext));
        } while (!equalAny(Symbol.RIGHT_PAREN));
        Optional<ShardingColumnContext> column = sqlContext.findColumn(left);
        if (column.isPresent() && shardingRule.isShardingColumn(column.get())) {
            sqlContext.getConditionContext().add(new ConditionContext.Condition(column.get(), rights));
        }
        getLexer().nextToken();
    }
    
    private void parseBetweenCondition(final SQLContext sqlContext, final SQLExpr left) {
        getLexer().nextToken();
        List<SQLExpr> rights = new LinkedList<>();
        rights.add(parseExpression(sqlContext));
        accept(DefaultKeyword.AND);
        rights.add(parseExpression(sqlContext));
        Optional<ShardingColumnContext> column = sqlContext.findColumn(left);
        if (column.isPresent() && shardingRule.isShardingColumn(column.get())) {
            sqlContext.getConditionContext().add(new ConditionContext.Condition(column.get(), rights.get(0), rights.get(1)));
        }
    }
    
    private void parserOtherCondition(final SQLContext sqlContext) {
        getLexer().nextToken();
        parseExpression(sqlContext);
    }
}
