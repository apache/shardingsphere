package com.alibaba.druid.sql.parser;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.expr.SQLCharExpr;
import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.expr.SQLIntegerExpr;
import com.alibaba.druid.sql.ast.expr.SQLNCharExpr;
import com.alibaba.druid.sql.ast.expr.SQLNumberExpr;
import com.alibaba.druid.sql.ast.expr.SQLPropertyExpr;
import com.alibaba.druid.sql.ast.expr.SQLVariantRefExpr;
import com.alibaba.druid.sql.ast.statement.SQLExprTableSource;
import com.alibaba.druid.sql.context.SQLContext;
import com.alibaba.druid.sql.context.TableToken;
import com.alibaba.druid.sql.lexer.Token;
import com.alibaba.druid.sql.visitor.SQLEvalVisitor;
import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.parser.result.router.Condition;
import com.dangdang.ddframe.rdb.sharding.parser.result.router.ConditionContext;
import com.dangdang.ddframe.rdb.sharding.parser.result.router.Table;
import com.dangdang.ddframe.rdb.sharding.parser.visitor.ParseContext;
import com.dangdang.ddframe.rdb.sharding.parser.visitor.basic.mysql.MySQLEvalVisitor;
import com.dangdang.ddframe.rdb.sharding.util.SQLUtil;
import com.google.common.base.Optional;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * 语句解析器工具.
 *
 * @author zhangliang
 */
@AllArgsConstructor
public class ParserUtil {
    
    @Getter
    private final SQLExprParser exprParser;
    
    private final ShardingRule shardingRule;
    
    private final List<Object> parameters;
    
    private final Table table;
    
    private final SQLContext sqlContext;
    
    private int parametersIndex;
    
    public Optional<ConditionContext> parseWhere() {
        if (exprParser.getLexer().equalToken(Token.WHERE)) {
            exprParser.getLexer().nextToken();
            ParseContext parseContext = getParseContext();
            parseConditions(parseContext);
            return Optional.of(parseContext.getCurrentConditionContext());
        }
        return Optional.absent();
    }
    
    public ParseContext getParseContext() {
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
            if (exprParser.getLexer().equalToken(Token.AND)) {
                exprParser.getLexer().nextToken();
            }
            parseCondition(parseContext);
        } while (exprParser.getLexer().equalToken(Token.AND));
        if (exprParser.getLexer().equalToken(Token.OR)) {
            throw new ParserUnsupportedException(exprParser.getLexer().getToken());
        }
    }
    
    private void parseCondition(final ParseContext parseContext) {
        SQLExpr left = getSqlExprWithVariant();
        if (exprParser.getLexer().equalToken(Token.EQ)) {
            parseEqualCondition(parseContext, left);
        } else if (exprParser.getLexer().equalToken(Token.IN)) {
            parseInCondition(parseContext, left);
        } else if (exprParser.getLexer().equalToken(Token.BETWEEN)) {
            parseBetweenCondition(parseContext, left);
        } else if (exprParser.getLexer().equalToken(Token.LT) || exprParser.getLexer().equalToken(Token.GT)
                || exprParser.getLexer().equalToken(Token.LT_EQ) || exprParser.getLexer().equalToken(Token.GT_EQ)) {
            parserOtherCondition();
        }
    }
    
    private void parseEqualCondition(final ParseContext parseContext, final SQLExpr left) {
        exprParser.getLexer().nextToken();
        SQLExpr right = getSqlExprWithVariant();
        parseContext.addCondition(left, Condition.BinaryOperator.EQUAL, Collections.singletonList(right), exprParser.getDbType(), parameters);
    }
    
    private void parseInCondition(final ParseContext parseContext, final SQLExpr left) {
        exprParser.getLexer().nextToken();
        exprParser.accept(Token.LEFT_PAREN);
        List<SQLExpr> rights = new LinkedList<>();
        do {
            if (exprParser.getLexer().equalToken(Token.COMMA)) {
                exprParser.getLexer().nextToken();
            }
            rights.add(getSqlExprWithVariant());
        } while (!exprParser.getLexer().equalToken(Token.RIGHT_PAREN));
        parseContext.addCondition(left, Condition.BinaryOperator.IN, rights, exprParser.getDbType(), parameters);
        exprParser.getLexer().nextToken();
    }
    
    private void parseBetweenCondition(final ParseContext parseContext, final SQLExpr left) {
        exprParser.getLexer().nextToken();
        List<SQLExpr> rights = new LinkedList<>();
        rights.add(getSqlExprWithVariant());
        exprParser.accept(Token.AND);
        rights.add(getSqlExprWithVariant());
        parseContext.addCondition(left, Condition.BinaryOperator.BETWEEN, rights, exprParser.getDbType(), parameters);
        exprParser.getLexer().nextToken();
    }
    
    private void parserOtherCondition() {
        exprParser.getLexer().nextToken();
        getSqlExprWithVariant();
    }
    
    public SQLExpr getSqlExprWithVariant() {
        SQLExpr result = parseSQLExpr();
        if (result instanceof SQLVariantRefExpr) {
            ((SQLVariantRefExpr) result).setIndex(++parametersIndex);
            result.getAttributes().put(SQLEvalVisitor.EVAL_VALUE, parameters.get(parametersIndex - 1));
            result.getAttributes().put(MySQLEvalVisitor.EVAL_VAR_INDEX, parametersIndex - 1);
        }
        return result;
    }
    
    private SQLExpr parseSQLExpr() {
        String literals = exprParser.getLexer().getLiterals();
        if (exprParser.getLexer().equalToken(Token.IDENTIFIER)) {
            if (table.getName().equalsIgnoreCase(SQLUtil.getExactlyValue(literals))) {
                sqlContext.getSqlTokens().add(new TableToken(exprParser.getLexer().getCurrentPosition() - literals.length(), literals, table.getName()));
            }
            exprParser.getLexer().nextToken();
            if (exprParser.getLexer().equalToken(Token.DOT)) {
                exprParser.getLexer().nextToken();
                SQLExpr result = new SQLPropertyExpr(new SQLIdentifierExpr(literals), exprParser.getLexer().getLiterals());
                exprParser.getLexer().nextToken();
                return result;
            }
            return new SQLIdentifierExpr(literals);
        }
        SQLExpr result = getSQLExpr(literals);
        exprParser.getLexer().nextToken();
        return result;
    }
    
    private SQLExpr getSQLExpr(final String literals) {
        if (exprParser.getLexer().equalToken(Token.VARIANT) || exprParser.getLexer().equalToken(Token.QUESTION)) {
            return new SQLVariantRefExpr("?");
        }
        if (exprParser.getLexer().equalToken(Token.LITERAL_CHARS)) {
            return new SQLCharExpr(literals);
        }
        if (exprParser.getLexer().equalToken(Token.LITERAL_NCHARS)) {
            return new SQLNCharExpr(literals);
        }
        if (exprParser.getLexer().equalToken(Token.LITERAL_INT)) {
            return new SQLIntegerExpr(Integer.parseInt(literals));
        }
        if (exprParser.getLexer().equalToken(Token.LITERAL_FLOAT)) {
            return new SQLNumberExpr(Double.parseDouble(literals));
        }
        if (exprParser.getLexer().equalToken(Token.LITERAL_HEX)) {
            return new SQLNumberExpr(Integer.parseInt(literals, 16));
        }
        throw new ParserUnsupportedException(exprParser.getLexer().getToken());
    }
}
