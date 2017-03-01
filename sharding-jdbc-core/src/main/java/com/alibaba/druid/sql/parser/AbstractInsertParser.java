package com.alibaba.druid.sql.parser;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.expr.SQLNumberExpr;
import com.alibaba.druid.sql.ast.expr.SQLVariantRefExpr;
import com.alibaba.druid.sql.ast.statement.AbstractSQLInsertStatement;
import com.alibaba.druid.sql.context.InsertSQLContext;
import com.alibaba.druid.sql.context.ItemsToken;
import com.alibaba.druid.sql.context.TableContext;
import com.alibaba.druid.sql.context.TableToken;
import com.alibaba.druid.sql.lexer.Token;
import com.alibaba.druid.sql.visitor.SQLEvalVisitor;
import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.parser.result.router.Condition;
import com.dangdang.ddframe.rdb.sharding.parser.visitor.ParseContext;
import com.dangdang.ddframe.rdb.sharding.parser.visitor.basic.mysql.MySQLEvalVisitor;
import com.dangdang.ddframe.rdb.sharding.util.SQLUtil;
import com.google.common.base.Optional;
import lombok.Getter;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Insert语句解析器.
 *
 * @author zhangliang
 */
@Getter
public abstract class AbstractInsertParser extends SQLParser {
    
    private final SQLExprParser exprParser;
    
    private final ShardingRule shardingRule;
    
    private final List<Object> parameters;
    
    public AbstractInsertParser(final ShardingRule shardingRule, final List<Object> parameters, final SQLExprParser exprParser) {
        super(exprParser.getLexer(), exprParser.getDbType());
        this.exprParser = exprParser;
        this.shardingRule = shardingRule;
        this.parameters = parameters;
    }
    
    /**
     * 解析Insert语句.
     * 
     * @return 解析结果
     */
    public final InsertSQLContext parse() {
        getLexer().nextToken();
        InsertSQLContext result = new InsertSQLContext(getLexer().getInput());
        parseInto(result);
        Collection<Condition.Column> columns = parseColumns(result);
        if (getValuesIdentifiers().contains(getLexer().getLiterals())) {
            parseValues(columns, result);
        } else if (getLexer().equalToken(Token.SELECT) || getLexer().equalToken(Token.LEFT_PAREN)) {
            // TODO 暂时不做
            //parseSelect(result);
        } else if (getCustomizedInsertIdentifiers().contains(getLexer().getToken().getName())) {
            parseCustomizedInsert(result);
        }
        return result;
    }
    
    protected Set<String> getUnsupportedIdentifiers() {
        return Collections.emptySet();
    }
    
    private void parseInto(final InsertSQLContext sqlContext) {
        if (getLexer().equalToken(Token.HINT)) {
            getLexer().nextToken();
        }
        if (getUnsupportedIdentifiers().contains(getLexer().getLiterals())) {
            throw new UnsupportedOperationException(String.format("Cannot support %s for %s.", getLexer().getLiterals(), getDbType()));
        }
        parseBetweenInsertAndInfo();
        getLexer().accept(Token.INTO);
        parseBetweenIntoAndTable();
        parseTable(sqlContext);
        parseBetweenTableAndValues();
    }
    
    private void parseBetweenInsertAndInfo() {
        while (!getLexer().equalToken(Token.INTO) && !getLexer().equalToken(Token.EOF)) {
            getLexer().nextToken();
        }
    }
    
    private void parseBetweenIntoAndTable() {
        while (getIdentifiersBetweenIntoAndTable().contains(getLexer().getLiterals())) {
            getLexer().nextToken();
        }
    }
    
    protected Set<String> getIdentifiersBetweenIntoAndTable() {
        return Collections.emptySet();
    }
    
    private TableContext parseTable(final InsertSQLContext insertSQLContext) {
        int beginPosition = getLexer().getCurrentPosition() - getLexer().getLiterals().length();
        String tableName = getLexer().getLiterals();
        getLexer().nextToken();
        TableContext result = new TableContext(tableName, SQLUtil.getExactlyValue(tableName), Optional.<String>absent());
        insertSQLContext.getSqlTokens().add(new TableToken(beginPosition, tableName, result.getName()));
        insertSQLContext.getTables().add(result);
        return result;
    }
    
    private void parseBetweenTableAndValues() {
        while (getIdentifiersBetweenTableAndValues().contains(getLexer().getLiterals())) {
            getLexer().nextToken();
            if (getLexer().equalToken(Token.LEFT_PAREN)) {
                do {
                    getLexer().nextToken();
                }
                while (!getLexer().equalToken(Token.RIGHT_PAREN) && !getLexer().equalToken(Token.EOF));
                getLexer().accept(Token.RIGHT_PAREN);
            }
        }
    }
    
    protected Set<String> getIdentifiersBetweenTableAndValues() {
        return Collections.emptySet();
    }
    
    protected Set<String> getCustomizedInsertIdentifiers() {
        return Collections.emptySet();
    }
    
    private Collection<Condition.Column> parseColumns(final InsertSQLContext sqlContext) {
        Collection<Condition.Column> result = new LinkedList<>();
        ParserUtil parserUtil = new ParserUtil(exprParser, shardingRule, parameters, sqlContext.getTables().get(0), sqlContext, 0);
        Collection<String> autoIncrementColumns = parserUtil.getParseContext().getShardingRule().getAutoIncrementColumns(sqlContext.getTables().get(0).getName());
        if (getLexer().equalToken(Token.LEFT_PAREN)) {
            do {
                getLexer().nextToken();
                String columnName = SQLUtil.getExactlyValue(getLexer().getLiterals());
                if (autoIncrementColumns.contains(columnName)) {
                    autoIncrementColumns.remove(columnName);
                }
                result.add(new Condition.Column(columnName, sqlContext.getTables().get(0).getName()));
                getLexer().nextToken();
            }
            while (!getLexer().equalToken(Token.RIGHT_PAREN) && !getLexer().equalToken(Token.EOF));
            ItemsToken itemsToken = new ItemsToken(getLexer().getCurrentPosition() - getLexer().getLiterals().length());
            for (String each : autoIncrementColumns) {
                itemsToken.getItems().add(each);
                result.add(new Condition.Column(each, sqlContext.getTables().get(0).getName(), true));
            }
            if (!itemsToken.getItems().isEmpty()) {
                sqlContext.getSqlTokens().add(itemsToken);
            }
            getLexer().nextToken();
        }
        return result;
    }
    
    protected Set<String> getValuesIdentifiers() {
        Set<String> result = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        result.add(Token.VALUES.getName());
        return result;
    }
    
    private void parseValues(final Collection<Condition.Column> columns, final InsertSQLContext sqlContext) {
        ParserUtil parserUtil = new ParserUtil(exprParser, shardingRule, parameters, sqlContext.getTables().get(0), sqlContext, 0);
        ParseContext parseContext = parserUtil.getParseContext();
        boolean parsed = false;
        do {
            // TODO support multiple insert
            if (parsed) {
                throw new UnsupportedOperationException("Cannot support multiple insert");
            }
            getLexer().nextToken();
            getLexer().accept(Token.LEFT_PAREN);
            List<SQLExpr> sqlExprs = getExprParser().exprList(new AbstractSQLInsertStatement.ValuesClause());
            ItemsToken itemsToken = new ItemsToken(getLexer().getCurrentPosition() - getLexer().getLiterals().length());
            int count = 0;
            for (Condition.Column each : columns) {
                if (each.isAutoIncrement()) {
                    Number autoIncrementedValue = (Number) parseContext.getShardingRule().findTableRule(sqlContext.getTables().get(0).getName()).generateId(each.getColumnName());
                    if (parameters.isEmpty()) {
                        itemsToken.getItems().add(autoIncrementedValue.toString());
                        sqlExprs.add(new SQLNumberExpr(autoIncrementedValue));
                    } else {
                        itemsToken.getItems().add("?");
                        parameters.add(autoIncrementedValue);
                        SQLVariantRefExpr sqlVariantRefExpr = new SQLVariantRefExpr("?");
                        sqlVariantRefExpr.setIndex(parameters.size());
                        sqlVariantRefExpr.getAttributes().put(SQLEvalVisitor.EVAL_VALUE, autoIncrementedValue);
                        sqlVariantRefExpr.getAttributes().put(MySQLEvalVisitor.EVAL_VAR_INDEX, parameters.size() - 1);
                        sqlExprs.add(sqlVariantRefExpr);
                    }
                    sqlContext.getGeneratedKeyContext().getColumns().add(each.getColumnName());
                    sqlContext.getGeneratedKeyContext().putValue(each.getColumnName(), autoIncrementedValue);
                }
                parseContext.addCondition(each.getColumnName(), each.getTableName(), Condition.BinaryOperator.EQUAL, sqlExprs.get(count), exprParser.getDbType(), parameters);
                count++;
            }
            if (!itemsToken.getItems().isEmpty()) {
                sqlContext.getSqlTokens().add(itemsToken);
            }
            getLexer().accept(Token.RIGHT_PAREN);
            parsed = true;
        }
        while (getLexer().equalToken(Token.COMMA));
        sqlContext.getConditionContexts().add(parseContext.getCurrentConditionContext());
    }
    
//    private void parseSelect(final AbstractSQLInsertStatement sqlInsertStatement) {
//        SQLSelect select = exprParser.createSelectParser().select();
//        select.setParent(sqlInsertStatement);
//        sqlInsertStatement.setQuery(select);
//    }
    
    protected void parseCustomizedInsert(final InsertSQLContext sqlContext) {
    }
}
