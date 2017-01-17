package com.alibaba.druid.sql.parser;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.expr.SQLCharExpr;
import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.expr.SQLIntegerExpr;
import com.alibaba.druid.sql.ast.expr.SQLListExpr;
import com.alibaba.druid.sql.ast.expr.SQLNCharExpr;
import com.alibaba.druid.sql.ast.expr.SQLNumberExpr;
import com.alibaba.druid.sql.ast.expr.SQLPropertyExpr;
import com.alibaba.druid.sql.ast.expr.SQLVariantRefExpr;
import com.alibaba.druid.sql.ast.statement.SQLExprTableSource;
import com.alibaba.druid.sql.ast.statement.SQLUpdateStatement;
import com.alibaba.druid.sql.context.UpdateSQLContext;
import com.alibaba.druid.sql.lexer.Token;
import com.alibaba.druid.sql.visitor.SQLEvalVisitor;
import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.parser.result.router.Condition;
import com.dangdang.ddframe.rdb.sharding.parser.result.router.Table;
import com.dangdang.ddframe.rdb.sharding.parser.visitor.ParseContext;
import com.dangdang.ddframe.rdb.sharding.parser.visitor.basic.mysql.MySQLEvalVisitor;
import com.dangdang.ddframe.rdb.sharding.util.SQLUtil;
import com.google.common.base.Optional;
import lombok.Getter;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Update语句解析器.
 *
 * @author zhangliang
 */
public abstract class AbstractUpdateParser extends SQLParser {
    
    @Getter
    private final SQLExprParser exprParser;
    
    @Getter
    private final UpdateSQLContext updateSQLContext;
    
    private final ShardingRule shardingRule;
    
    private final List<Object> parameters;
    
    private int parametersIndex;
    
    public AbstractUpdateParser(final ShardingRule shardingRule, final List<Object> parameters, final SQLExprParser exprParser) {
        super(exprParser.getLexer(), exprParser.getDbType());
        this.exprParser = exprParser;
        this.updateSQLContext = new UpdateSQLContext();
        this.shardingRule = shardingRule;
        this.parameters = parameters;
    }
    
    /**
     * 解析Update语句.
     *
     * @return 解析结果
     */
    public SQLUpdateStatement parse() {
        getLexer().nextToken();
        parseBetweenUpdateAndTable();
        updateSQLContext.append(getLexer().getInput().substring(0, getLexer().getCurrentPosition() - getLexer().getLiterals().length()));
        Table table = parseTable();
        updateSQLContext.append(" " + getLexer().getInput().substring(getLexer().getCurrentPosition() - getLexer().getLiterals().length(), getLexer().getInput().length()));
        parseSetItems();
        parseBetweenSetAndWhere();
        parseWhere(table);
        return new SQLUpdateStatement(updateSQLContext);
    }
    
    protected abstract void parseBetweenUpdateAndTable();
    
    private Table parseTable() {
        String tableName = SQLUtil.getExactlyValue(getLexer().getLiterals());
        getLexer().nextToken();
        if (getLexer().equalToken(Token.AS)) {
            getLexer().nextToken();
        }
        Optional<String> alias = getLexer().equalToken(Token.IDENTIFIER) ? Optional.of(SQLUtil.getExactlyValue(getLexer().getLiterals())) : Optional.<String>absent();
        Table result = new Table(tableName, alias);
        updateSQLContext.setTable(result);
        updateSQLContext.appendToken(tableName);
        // TODO 应该使用计算offset而非output AS + alias的方式生成sql
        if (alias.isPresent()) {
            updateSQLContext.append(" AS " + alias.get());
        }
        if (!getLexer().equalToken(Token.SET)) {
            getLexer().nextToken();
        }
        return result;
    }
    
    private void parseSetItems() {
        accept(Token.SET);
        do {
            if (getLexer().equalToken(Token.COMMA)) {
                getLexer().nextToken();
            }
            parseSetItem();
        } while (getLexer().equalToken(Token.COMMA));
    }
    
    private void parseSetItem() {
        if (getLexer().equalToken(Token.LEFT_PAREN)) {
            while (!getLexer().equalToken(Token.RIGHT_PAREN)) {
                getLexer().nextToken();
            }
            getLexer().nextToken();
        } else {
            exprParser.primary();
        }
        if (getLexer().equalToken(Token.COLON_EQ)) {
            getLexer().nextToken();
        } else {
            accept(Token.EQ);
        }
        SQLExpr value = exprParser.expr();
        if (value instanceof SQLListExpr) {
            for (SQLExpr each : ((SQLListExpr) value).getItems()) {
                if (each instanceof SQLVariantRefExpr) {
                    parametersIndex++;
                }
            }
        }
        if (value instanceof SQLVariantRefExpr) {
            parametersIndex++;
        }
    }
    
    protected void parseBetweenSetAndWhere() {
    }
    
    private void parseWhere(final Table table) {
        if (getLexer().equalToken(Token.WHERE)) {
            getLexer().nextToken();
            ParseContext parseContext = getParseContext(table);
            parseConditions(parseContext);
            updateSQLContext.getConditionContexts().add(parseContext.getCurrentConditionContext());
        }
    }
    
    private ParseContext getParseContext(final Table table) {
        ParseContext result = new ParseContext(1);
        result.setShardingRule(shardingRule);
        SQLExprTableSource tableSource = new SQLExprTableSource();
        tableSource.setExpr(new SQLIdentifierExpr(table.getName()));
        if (table.getAlias().isPresent()) {
            tableSource.setAlias(table.getAlias().get());
        }
        result.addTable(tableSource);
        result.setCurrentTable(table.getName(), table.getAlias());
        return result;
    }
    
    private void parseConditions(final ParseContext parseContext) {
        do {
            if (getLexer().equalToken(Token.AND)) {
                getLexer().nextToken();
            }
            parseCondition(parseContext);
        } while (getLexer().equalToken(Token.AND));
        if (getLexer().equalToken(Token.OR)) {
            throw new ParserUnsupportedException(getLexer().getToken());
        }
    }
    
    private void parseCondition(final ParseContext parseContext) {
        SQLExpr left = getSqlExprWithVariant();
        if (getLexer().equalToken(Token.EQ)) {
            parseEqualCondition(parseContext, left);
        } else if (getLexer().equalToken(Token.IN)) {
            parseInCondition(parseContext, left);
        } else if (getLexer().equalToken(Token.BETWEEN)) {
            parseBetweenCondition(parseContext, left);
        } else if (getLexer().equalToken(Token.LT) || getLexer().equalToken(Token.GT) || getLexer().equalToken(Token.LT_EQ) || getLexer().equalToken(Token.GT_EQ)) {
            parserOtherCondition();
        }
    }
    
    private void parseEqualCondition(final ParseContext parseContext, final SQLExpr left) {
        getLexer().nextToken();
        SQLExpr right = getSqlExprWithVariant();
        parseContext.addCondition(left, Condition.BinaryOperator.EQUAL, Collections.singletonList(right), getDbType(), parameters);
    }
    
    private void parseInCondition(final ParseContext parseContext, final SQLExpr left) {
        getLexer().nextToken();
        accept(Token.LEFT_PAREN);
        List<SQLExpr> rights = new LinkedList<>();
        do {
            if (getLexer().equalToken(Token.COMMA)) {
                getLexer().nextToken();
            }
            rights.add(getSqlExprWithVariant());
        } while (!getLexer().equalToken(Token.RIGHT_PAREN));
        parseContext.addCondition(left, Condition.BinaryOperator.IN, rights, getDbType(), parameters);
        getLexer().nextToken();
    }
    
    private void parseBetweenCondition(final ParseContext parseContext, final SQLExpr left) {
        getLexer().nextToken();
        List<SQLExpr> rights = new LinkedList<>();
        rights.add(getSqlExprWithVariant());
        accept(Token.AND);
        rights.add(getSqlExprWithVariant());
        parseContext.addCondition(left, Condition.BinaryOperator.BETWEEN, rights, getDbType(), parameters);
        getLexer().nextToken();
    }
    
    private void parserOtherCondition() {
        getLexer().nextToken();
        getSqlExprWithVariant();
    }
    
    private SQLExpr getSqlExprWithVariant() {
        SQLExpr result = parseSQLExpr();
        if (result instanceof SQLVariantRefExpr) {
            ((SQLVariantRefExpr) result).setIndex(++parametersIndex);
            result.getAttributes().put(SQLEvalVisitor.EVAL_VALUE, parameters.get(parametersIndex - 1));
            result.getAttributes().put(MySQLEvalVisitor.EVAL_VAR_INDEX, parametersIndex - 1);
        }
        return result;
    }
    
    private SQLExpr parseSQLExpr() {
        String literals = getLexer().getLiterals();
        if (getLexer().equalToken(Token.IDENTIFIER)) {
            getLexer().nextToken();
            if (getLexer().equalToken(Token.DOT)) {
                getLexer().nextToken();
                SQLExpr result = new SQLPropertyExpr(new SQLIdentifierExpr(literals), getLexer().getLiterals());
                getLexer().nextToken();
                return result;
            }
            return new SQLIdentifierExpr(literals);
        }
        SQLExpr result = getSQLExpr(literals);
        getLexer().nextToken();
        return result;
    }
    
    private SQLExpr getSQLExpr(final String literals) {
        if (getLexer().equalToken(Token.VARIANT) || getLexer().equalToken(Token.QUESTION)) {
            return new SQLVariantRefExpr("?");
        }
        if (getLexer().equalToken(Token.LITERAL_CHARS)) {
            return new SQLCharExpr(literals);
        }
        if (getLexer().equalToken(Token.LITERAL_NCHARS)) {
            return new SQLNCharExpr(literals);
        }
        if (getLexer().equalToken(Token.LITERAL_INT)) {
            return new SQLIntegerExpr(Integer.parseInt(literals));
        }
        if (getLexer().equalToken(Token.LITERAL_FLOAT)) {
            return new SQLNumberExpr(Double.parseDouble(literals));
        }
        if (getLexer().equalToken(Token.LITERAL_HEX)) {
            return new SQLNumberExpr(Integer.parseInt(literals, 16));
        }
        throw new ParserUnsupportedException(getLexer().getToken());
    }
}
