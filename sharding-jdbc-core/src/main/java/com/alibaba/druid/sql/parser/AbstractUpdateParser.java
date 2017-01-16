package com.alibaba.druid.sql.parser;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.expr.SQLCharExpr;
import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.expr.SQLIntegerExpr;
import com.alibaba.druid.sql.ast.expr.SQLNCharExpr;
import com.alibaba.druid.sql.ast.expr.SQLNumberExpr;
import com.alibaba.druid.sql.ast.expr.SQLPropertyExpr;
import com.alibaba.druid.sql.ast.expr.SQLVariantRefExpr;
import com.alibaba.druid.sql.ast.statement.AbstractSQLUpdateStatement;
import com.alibaba.druid.sql.ast.statement.SQLExprTableSource;
import com.alibaba.druid.sql.ast.statement.SQLUpdateSetItem;
import com.alibaba.druid.sql.context.UpdateSQLContext;
import com.alibaba.druid.sql.lexer.Token;
import com.alibaba.druid.sql.visitor.SQLEvalVisitor;
import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.constants.DatabaseType;
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
import java.util.Set;

/**
 * Update语句解析器.
 *
 * @author zhangliang
 */
@Getter
public abstract class AbstractUpdateParser extends SQLParser {
    
    private final ShardingRule shardingRule;
    
    private final SQLExprParser exprParser;
    
    private final UpdateSQLContext updateSQLContext;
    
    private final List<Object> parameters;
    
    private int parametersIndex;
    
    public AbstractUpdateParser(final ShardingRule shardingRule, final List<Object> parameters, final SQLExprParser exprParser) {
        super(exprParser.getLexer(), exprParser.getDbType());
        this.shardingRule = shardingRule;
        this.parameters = parameters;
        this.exprParser = exprParser;
        this.updateSQLContext = new UpdateSQLContext();
    }
    
    /**
     * 解析Update语句.
     *
     * @return 解析结果
     */
    public AbstractSQLUpdateStatement parse() {
        getLexer().nextToken();
        parseBetweenUpdateAndTable();
        parseIdentifiersBetweenUpdateAndTable();
        updateSQLContext.append(getLexer().getInput().substring(0, getLexer().getCurrentPosition() - getLexer().getLiterals().length()));
        Table table = parseTableSource();
        updateSQLContext.setTable(table);
        updateSQLContext.appendToken(table.getName());
        // TODO 应该使用计算offset而非output AS + alias的方式生成sql
        if (table.getAlias().isPresent()) {
            updateSQLContext.append(" AS " + table.getAlias().get());
        }
        int afterTablePosition = getLexer().getCurrentPosition() - getLexer().getLiterals().length();
        updateSQLContext.append(" " + getLexer().getInput().substring(afterTablePosition, getLexer().getInput().length()));
        parseUpdateSet();
        parseBetweenSetAndWhere();
        parseWhere(table);
        AbstractSQLUpdateStatement result = createUpdateStatement();
        result.setSqlContext(updateSQLContext);
        return result;
    }
    
    private Table parseTableSource() {
        String tableName = getLexer().getLiterals();
        getLexer().nextToken();
        if (getLexer().equalToken(Token.AS)) {
            getLexer().nextToken();
        }
        Optional<String> alias = getLexer().equalToken(Token.IDENTIFIER) ? Optional.of(SQLUtil.getExactlyValue(getLexer().getLiterals())) : Optional.<String>absent();
        if (!getLexer().equalToken(Token.SET)) {
            getLexer().nextToken();
        }
        return new Table(SQLUtil.getExactlyValue(tableName), alias);
    }
    
    protected abstract AbstractSQLUpdateStatement createUpdateStatement();
    
    protected void parseBetweenUpdateAndTable() {
    }
    
    protected void parseBetweenSetAndWhere() {
    }
    
    protected Set<String> getIdentifiersBetweenUpdateAndTable() {
        return Collections.emptySet();
    }
    
    private void parseIdentifiersBetweenUpdateAndTable() {
        while (getIdentifiersBetweenUpdateAndTable().contains(getLexer().getLiterals())) {
            getLexer().nextToken();
        }
    }
    
    private void parseUpdateSet() {
        accept(Token.SET);
        while (true) {
            SQLUpdateSetItem item = exprParser.parseUpdateSetItem();
            if (item.getValue() instanceof SQLVariantRefExpr) {
                parametersIndex++;
            }
            if (!getLexer().equalToken(Token.COMMA)) {
                break;
            }
            getLexer().nextToken();
        }
    }
    
    private void parseWhere(final Table table) {
        if (getLexer().equalToken(Token.WHERE)) {
            getLexer().nextToken();
            ParseContext parseContext = getParseContext(table);
            parseConditions(parseContext);
            updateSQLContext.getConditionContexts().add(parseContext.getCurrentConditionContext());
        }
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
            getLexer().nextToken();
            SQLExpr right = getSqlExprWithVariant();
            // TODO DatabaseType.MySQL
            parseContext.addCondition(left, Condition.BinaryOperator.EQUAL, Collections.singletonList(right), DatabaseType.MySQL, parameters);
        } else if (getLexer().equalToken(Token.IN)) {
            getLexer().nextToken();
            accept(Token.LEFT_PAREN);
            List<SQLExpr> rights = new LinkedList<>();
            do {
                if (getLexer().equalToken(Token.COMMA)) {
                    getLexer().nextToken();
                }
                rights.add(getSqlExprWithVariant());
            } while (!getLexer().equalToken(Token.RIGHT_PAREN));
            // TODO DatabaseType.MySQL
            parseContext.addCondition(left, Condition.BinaryOperator.IN, rights, DatabaseType.MySQL, parameters);
            getLexer().nextToken();
        } else if (getLexer().equalToken(Token.BETWEEN)) {
            getLexer().nextToken();
            List<SQLExpr> rights = new LinkedList<>();
            rights.add(getSqlExprWithVariant());
            accept(Token.AND);
            rights.add(getSqlExprWithVariant());
            // TODO DatabaseType.MySQL
            parseContext.addCondition(left, Condition.BinaryOperator.BETWEEN, rights, DatabaseType.MySQL, parameters);
            getLexer().nextToken();
        } else if (getLexer().equalToken(Token.LT) || getLexer().equalToken(Token.GT) || getLexer().equalToken(Token.LT_EQ) || getLexer().equalToken(Token.GT_EQ)) {
            getLexer().nextToken();
            getSqlExprWithVariant();
        }
    }
    
    private SQLExpr getSqlExprWithVariant() {
        SQLExpr right = parseSQLExpr();
        if (right instanceof SQLVariantRefExpr) {
            ((SQLVariantRefExpr) right).setIndex(++parametersIndex);
            right.getAttributes().put(SQLEvalVisitor.EVAL_VALUE, parameters.get(parametersIndex - 1));
            right.getAttributes().put(MySQLEvalVisitor.EVAL_VAR_INDEX, parametersIndex - 1);
        }
        return right;
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
        SQLExpr result = getSqlExpr(literals);
        getLexer().nextToken();
        return result;
    }
    
    private SQLExpr getSqlExpr(final String literals) {
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
