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
import com.alibaba.druid.sql.ast.SQLDataType;
import com.alibaba.druid.sql.ast.SQLDataTypeImpl;
import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.SQLName;
import com.alibaba.druid.sql.ast.SQLObject;
import com.alibaba.druid.sql.ast.SQLOver;
import com.alibaba.druid.sql.ast.expr.SQLAggregateExpr;
import com.alibaba.druid.sql.ast.expr.SQLAggregateOption;
import com.alibaba.druid.sql.ast.expr.SQLAllColumnExpr;
import com.alibaba.druid.sql.ast.expr.SQLAllExpr;
import com.alibaba.druid.sql.ast.expr.SQLAnyExpr;
import com.alibaba.druid.sql.ast.expr.SQLBetweenExpr;
import com.alibaba.druid.sql.ast.expr.SQLBinaryOpExpr;
import com.alibaba.druid.sql.ast.expr.SQLBinaryOperator;
import com.alibaba.druid.sql.ast.expr.SQLBooleanExpr;
import com.alibaba.druid.sql.ast.expr.SQLCaseExpr;
import com.alibaba.druid.sql.ast.expr.SQLCastExpr;
import com.alibaba.druid.sql.ast.expr.SQLCharExpr;
import com.alibaba.druid.sql.ast.expr.SQLCurrentOfCursorExpr;
import com.alibaba.druid.sql.ast.expr.SQLDefaultExpr;
import com.alibaba.druid.sql.ast.expr.SQLHexExpr;
import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.expr.SQLInListExpr;
import com.alibaba.druid.sql.ast.expr.SQLInSubQueryExpr;
import com.alibaba.druid.sql.ast.expr.SQLIntegerExpr;
import com.alibaba.druid.sql.ast.expr.SQLListExpr;
import com.alibaba.druid.sql.ast.expr.SQLMethodInvokeExpr;
import com.alibaba.druid.sql.ast.expr.SQLNCharExpr;
import com.alibaba.druid.sql.ast.expr.SQLNotExpr;
import com.alibaba.druid.sql.ast.expr.SQLNullExpr;
import com.alibaba.druid.sql.ast.expr.SQLNumberExpr;
import com.alibaba.druid.sql.ast.expr.SQLPropertyExpr;
import com.alibaba.druid.sql.ast.expr.SQLQueryExpr;
import com.alibaba.druid.sql.ast.expr.SQLSomeExpr;
import com.alibaba.druid.sql.ast.expr.SQLUnaryExpr;
import com.alibaba.druid.sql.ast.expr.SQLUnaryOperator;
import com.alibaba.druid.sql.ast.expr.SQLVariantRefExpr;
import com.alibaba.druid.sql.ast.statement.SQLCharacterDataType;
import com.alibaba.druid.sql.ast.statement.SQLSelectItem;
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
import com.google.common.base.Preconditions;
import lombok.Getter;
import lombok.Setter;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * SQL解析器.
 *
 * @author zhangliang
 */
public class SQLExprParser {
    
    private final Set<String> aggregateFunctions = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
    
    @Getter
    private final Lexer lexer;
    
    @Getter
    private final ShardingRule shardingRule;
    
    @Getter
    private final List<Object> parameters;
    
    @Getter
    @Setter
    private int parametersIndex;
    
    public SQLExprParser(final ShardingRule shardingRule, final List<Object> parameters, final Lexer lexer, final String... aggregateFunctions) {
        this.lexer = lexer;
        this.shardingRule = shardingRule;
        this.parameters = parameters;
        this.aggregateFunctions.addAll(Arrays.asList(aggregateFunctions));
    }
    
    protected Optional<String> as() {
        if (lexer.skipIfEqual(Token.AS)) {
            // TODO 判断Literals是符号则返回null, 目前仅判断为LEFT_PAREN
            if (lexer.equalToken(Token.LEFT_PAREN)) {
                return Optional.absent();
            }
            String result = lexer.getLiterals();
            lexer.nextToken();
            return Optional.of(result);
        }
        // TODO 增加哪些数据库识别哪些关键字作为别名的配置
        if (lexer.equalToken(Token.IDENTIFIER, Token.LITERAL_ALIAS, Token.LITERAL_CHARS, Token.USER, Token.END, Token.CASE, Token.KEY, Token.INTERVAL, Token.CONSTRAINT)) {
            String result = lexer.getLiterals();
            lexer.nextToken();
            return Optional.of(result);
        }
        return Optional.absent();
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
    
    public final SQLExpr expr(final SQLObject parent) {
        SQLExpr result = expr();
        result.setParent(parent);
        return result;
    }
    
    public SQLExpr expr() {
        if (getLexer().equalToken(Token.STAR)) {
            getLexer().nextToken();
            SQLExpr sqlExpr = new SQLAllColumnExpr();
            if (getLexer().equalToken(Token.DOT)) {
                getLexer().nextToken();
                getLexer().accept(Token.STAR);
                return new SQLPropertyExpr(sqlExpr, "*");
            }
            return sqlExpr;
        }
        SQLExpr expr = primary();
        if (getLexer().equalToken(Token.COMMA)) {
            return expr;
        }
        return exprRest(expr);
    }
    
    public SQLExpr primary() {
        SQLExpr sqlExpr = null;
        Token token = getLexer().getToken();
        switch (token) {
            case LEFT_PAREN:
                getLexer().nextToken();
                sqlExpr = expr();
                if (getLexer().equalToken(Token.COMMA)) {
                    SQLListExpr listExpr = new SQLListExpr();
                    listExpr.getItems().add(sqlExpr);
                    do {
                        getLexer().nextToken();
                        listExpr.getItems().add(expr());
                    } while (getLexer().equalToken(Token.COMMA));
                    sqlExpr = listExpr;
                }
                getLexer().accept(Token.RIGHT_PAREN);
                break;
            case INSERT:
                getLexer().nextToken();
                if (!getLexer().equalToken(Token.LEFT_PAREN)) {
                    throw new ParserException(getLexer());
                }
                sqlExpr = new SQLIdentifierExpr("INSERT");
                break;
            case IDENTIFIER:
                sqlExpr = new SQLIdentifierExpr(getLexer().getLiterals());
                getLexer().nextToken();
                break;
            case NEW:
                throw new ParserUnsupportedException(getLexer().getToken());
            case LITERAL_INT:
                sqlExpr = new SQLIntegerExpr(getLexer().integerValue());
                getLexer().nextToken();
                break;
            case LITERAL_FLOAT:
                sqlExpr = new SQLNumberExpr(getLexer().decimalValue());
                getLexer().nextToken();
                break;
            case LITERAL_CHARS:
                sqlExpr = new SQLCharExpr(getLexer().getLiterals());
                getLexer().nextToken();
                break;
            case LITERAL_NCHARS:
                sqlExpr = new SQLNCharExpr(getLexer().getLiterals());
                getLexer().nextToken();
                break;
            case VARIANT:
                SQLVariantRefExpr varRefExpr = new SQLVariantRefExpr(getLexer().getLiterals());
                getLexer().nextToken();
                if (varRefExpr.getName().equals("@") && getLexer().equalToken(Token.LITERAL_CHARS)) {
                    varRefExpr.setName("@'" + getLexer().getLiterals() + "'");
                    getLexer().nextToken();
                } else if (varRefExpr.getName().equals("@@") && getLexer().equalToken(Token.LITERAL_CHARS)) {
                    varRefExpr.setName("@@'" + getLexer().getLiterals() + "'");
                    getLexer().nextToken();
                }
                sqlExpr = varRefExpr;
                break;
            case DEFAULT:
                sqlExpr = new SQLDefaultExpr();
                getLexer().nextToken();
                break;
            case DUAL:
            case KEY:
            case DISTINCT:
            case LIMIT:
            case SCHEMA:
            case COLUMN:
            case IF:
            case END:
            case COMMENT:
            case COMPUTE:
            case ENABLE:
            case DISABLE:
            case INITIALLY:
            case SEQUENCE:
            case USER:
            case EXPLAIN:
            case WITH:
            case GRANT:
            case REPLACE:
            case INDEX:
            case MODEL:
            case PCTFREE:
            case INITRANS:
            case MAXTRANS:
            case SEGMENT:
            case CREATION:
            case IMMEDIATE:
            case DEFERRED:
            case STORAGE:
            case NEXT:
            case MINEXTENTS:
            case MAXEXTENTS:
            case MAXSIZE:
            case PCTINCREASE:
            case FLASH_CACHE:
            case CELL_FLASH_CACHE:
            case KEEP:
            case NONE:
            case LOB:
            case STORE:
            case ROW:
            case CHUNK:
            case CACHE:
            case NOCACHE:
            case LOGGING:
            case NOCOMPRESS:
            case KEEP_DUPLICATES:
            case EXCEPTIONS:
            case PURGE:
            case FULL:
            case TO:
            case IDENTIFIED:
            case PASSWORD:
            case BINARY:
            case WINDOW:
            case OFFSET:
            case SHARE:
            case START:
            case CONNECT:
            case MATCHED:
            case ERRORS:
            case REJECT:
            case UNLIMITED:
            case BEGIN:
            case EXCLUSIVE:
            case MODE:
            case ADVISE:
            case VIEW:
            case ESCAPE:
            case OVER:
            case ORDER:
            case CONSTRAINT:
            case TYPE:
            case OPEN:
            case REPEAT:
                sqlExpr = new SQLIdentifierExpr(getLexer().getLiterals());
                getLexer().nextToken();
                break;
            case CASE:
                SQLCaseExpr caseExpr = new SQLCaseExpr();
                getLexer().nextToken();
                if (!getLexer().equalToken(Token.WHEN)) {
                    caseExpr.setValueExpr(expr());
                }
                getLexer().accept(Token.WHEN);
                SQLExpr testExpr = expr();
                getLexer().accept(Token.THEN);
                SQLExpr valueExpr = expr();
                SQLCaseExpr.Item caseItem = new SQLCaseExpr.Item(testExpr, valueExpr);
                caseExpr.addItem(caseItem);
                while (getLexer().equalToken(Token.WHEN)) {
                    getLexer().nextToken();
                    testExpr = expr();
                    getLexer().accept(Token.THEN);
                    valueExpr = expr();
                    caseItem = new SQLCaseExpr.Item(testExpr, valueExpr);
                    caseExpr.getItems().add(caseItem);
                }
                if (getLexer().equalToken(Token.ELSE)) {
                    getLexer().nextToken();
                    caseExpr.setElseExpr(expr());
                }
                getLexer().accept(Token.END);
                sqlExpr = caseExpr;
                break;
            // TODO exist 需要处理
            case EXISTS:
                getLexer().nextToken();
                getLexer().skipParentheses();
                break;
            // TODO not 需要处理
            case NOT:
                getLexer().nextToken();
                if (getLexer().equalToken(Token.EXISTS)) {
                    getLexer().nextToken();
                    getLexer().skipParentheses();
                } else if (getLexer().equalToken(Token.LEFT_PAREN)) {
                    getLexer().nextToken();
                    SQLExpr notTarget = expr();
                    getLexer().accept(Token.RIGHT_PAREN);
                    notTarget = exprRest(notTarget);
                    sqlExpr = new SQLNotExpr(notTarget);
                    return primaryRest(sqlExpr);
                } else {
                    sqlExpr = new SQLNotExpr(relational());
                }
                break;
            case SELECT:
                throw new UnsupportedOperationException("Cannot support subquery");
            case CAST:
                getLexer().nextToken();
                getLexer().accept(Token.LEFT_PAREN);
                SQLCastExpr cast = new SQLCastExpr();
                cast.setExpr(expr());
                getLexer().accept(Token.AS);
                cast.setDataType(parseDataType());
                getLexer().accept(Token.RIGHT_PAREN);
                sqlExpr = cast;
                break;
            case SUB:
                getLexer().nextToken();
                switch (getLexer().getToken()) {
                    case LITERAL_INT:
                        Number integerValue = getLexer().integerValue();
                        if (integerValue instanceof Integer) {
                            int intVal = integerValue.intValue();
                            if (intVal == Integer.MIN_VALUE) {
                                integerValue = ((long) intVal) * -1;
                            } else {
                                integerValue = intVal * -1;
                            }
                        } else if (integerValue instanceof Long) {
                            long longVal = integerValue.longValue();
                            if (longVal == 2147483648L) {
                                integerValue = (int) (longVal * -1);
                            } else {
                                integerValue = longVal * -1;
                            }
                        } else {
                            integerValue = ((BigInteger) integerValue).negate();
                        }
                        sqlExpr = new SQLIntegerExpr(integerValue);
                        getLexer().nextToken();
                        break;
                    case LITERAL_FLOAT:
                        sqlExpr = new SQLNumberExpr(getLexer().decimalValue().negate());
                        getLexer().nextToken();
                        break;
                    case IDENTIFIER: // 当负号后面为字段的情况
                        sqlExpr = new SQLIdentifierExpr(getLexer().getLiterals());
                        getLexer().nextToken();
                        if (getLexer().equalToken(Token.LEFT_PAREN) || getLexer().equalToken(Token.LEFT_BRACKET)) {
                            sqlExpr = primaryRest(sqlExpr);
                        }
                        sqlExpr = new SQLUnaryExpr(SQLUnaryOperator.Negative, sqlExpr);
                        break;
                    case QUESTION:
                        sqlExpr = new SQLUnaryExpr(SQLUnaryOperator.Negative, new SQLVariantRefExpr("?"));
                        getLexer().nextToken();
                        break;
                    case LEFT_PAREN:
                        getLexer().nextToken();
                        sqlExpr = expr();
                        getLexer().accept(Token.RIGHT_PAREN);
                        sqlExpr = new SQLUnaryExpr(SQLUnaryOperator.Negative, sqlExpr);
                        break;
                    default:
                        throw new ParserUnsupportedException(getLexer().getToken());
                }
                break;
            case PLUS:
                getLexer().nextToken();
                switch (getLexer().getToken()) {
                    case LITERAL_INT:
                        sqlExpr = new SQLIntegerExpr(getLexer().integerValue());
                        getLexer().nextToken();
                        break;
                    case LITERAL_FLOAT:
                        sqlExpr = new SQLNumberExpr(getLexer().decimalValue());
                        getLexer().nextToken();
                        break;
                    case IDENTIFIER: // 当+号后面为字段的情况
                        sqlExpr = new SQLIdentifierExpr(getLexer().getLiterals());
                        sqlExpr = new SQLUnaryExpr(SQLUnaryOperator.Plus, sqlExpr);
                        getLexer().nextToken();
                        break;
                    case LEFT_PAREN:
                        getLexer().nextToken();
                        sqlExpr = expr();
                        getLexer().accept(Token.RIGHT_PAREN);
                        sqlExpr = new SQLUnaryExpr(SQLUnaryOperator.Plus, sqlExpr);
                        break;
                    default:
                        throw new ParserUnsupportedException(getLexer().getToken());
                }
                break;
            case TILDE:
                getLexer().nextToken();
                sqlExpr = new SQLUnaryExpr(SQLUnaryOperator.Compl, expr());
                break;
            case QUESTION:
                getLexer().nextToken();
                SQLVariantRefExpr quesVarRefExpr = new SQLVariantRefExpr("?");
                quesVarRefExpr.setIndex(getLexer().nextVarIndex());
                sqlExpr = quesVarRefExpr;
                break;
            case LEFT:
                sqlExpr = new SQLIdentifierExpr("LEFT");
                getLexer().nextToken();
                break;
            case RIGHT:
                sqlExpr = new SQLIdentifierExpr("RIGHT");
                getLexer().nextToken();
                break;
            case DATABASE:
                sqlExpr = new SQLIdentifierExpr("DATABASE");
                getLexer().nextToken();
                break;
            case LOCK:
                sqlExpr = new SQLIdentifierExpr("LOCK");
                getLexer().nextToken();
                break;
            case NULL:
                sqlExpr = new SQLNullExpr();
                getLexer().nextToken();
                break;
            case BANG:
                getLexer().nextToken();
                SQLExpr bangExpr = primary();
                sqlExpr = new SQLUnaryExpr(SQLUnaryOperator.Not, bangExpr);
                break;
            case LITERAL_HEX:
                sqlExpr = new SQLHexExpr(getLexer().getTerm().getValue());
                getLexer().nextToken();
                break;
            case INTERVAL:
                sqlExpr = parseInterval();
                break;
            case COLON:
                getLexer().nextToken();
                if (getLexer().equalToken(Token.LITERAL_ALIAS)) {
                    sqlExpr = new SQLVariantRefExpr(":\"" + getLexer().getLiterals() + "\"");
                    getLexer().nextToken();
                }
                break;
            case ANY:
                sqlExpr = parseAny();
                break;
            case SOME:
                sqlExpr = parseSome();
                break;
            case ALL:
                sqlExpr = parseAll();
                break;
            case LITERAL_ALIAS:
                sqlExpr = new SQLIdentifierExpr('"' + getLexer().getLiterals() + '"');
                getLexer().nextToken();
                break;
            case EOF:
                throw new ParserException(getLexer());
            case TRUE:
                getLexer().nextToken();
                sqlExpr = new SQLBooleanExpr(true);
                break;
            case FALSE:
                getLexer().nextToken();
                sqlExpr = new SQLBooleanExpr(false);
                break;
            default:
                throw new ParserException(getLexer(), token);
        }
        return primaryRest(sqlExpr);
    }
    
    public SQLExpr exprRest(SQLExpr expr) {
        expr = bitXorRest(expr);
        expr = multiplicativeRest(expr);
        expr = additiveRest(expr);
        expr = shiftRest(expr);
        expr = bitAndRest(expr);
        expr = bitOrRest(expr);
        expr = inRest(expr);
        expr = relationalRest(expr);
        expr = equalityRest(expr);
        expr = andRest(expr);
        expr = orRest(expr);
        return expr;
    }
    
    public SQLExpr bitXorRest(SQLExpr expr) {
        if (getLexer().equalToken(Token.CARET)) {
            getLexer().nextToken();
            expr = new SQLBinaryOpExpr(expr, SQLBinaryOperator.BitwiseXor, primary());
            expr = bitXorRest(expr);
        }
        return expr;
    }
    
    public SQLExpr multiplicativeRest(SQLExpr expr) {
        if (getLexer().equalToken(Token.STAR)) {
            getLexer().nextToken();
            SQLExpr rightExp = bitXorRest(primary());
            expr = new SQLBinaryOpExpr(expr, SQLBinaryOperator.Multiply, rightExp);
            expr = multiplicativeRest(expr);
        } else if (getLexer().equalToken(Token.SLASH)) {
            getLexer().nextToken();
            SQLExpr rightExp = bitXorRest(primary());
            expr = new SQLBinaryOpExpr(expr, SQLBinaryOperator.Divide, rightExp);
            expr = multiplicativeRest(expr);
        } else if (getLexer().equalToken(Token.PERCENT)) {
            getLexer().nextToken();
            SQLExpr rightExp = bitXorRest(primary());
            expr = new SQLBinaryOpExpr(expr, SQLBinaryOperator.Modulus, rightExp);
            expr = multiplicativeRest(expr);
        }
        return expr;
    }
    
    // FIXME skip all for subquery
    protected SQLExpr parseAll() {
        getLexer().nextToken();
        getLexer().skipParentheses();
        return new SQLAllExpr();
    }
    
    // FIXME skip some for subquery
    protected SQLExpr parseSome() {
        getLexer().nextToken();
        getLexer().skipParentheses();
        return new SQLSomeExpr();
    }
    
    // FIXME skip any for subquery
    protected SQLExpr parseAny() {
        getLexer().nextToken();
        if (getLexer().equalToken(Token.LEFT_PAREN)) {
            getLexer().skipParentheses();
            return new SQLAnyExpr();
        } else {
            return new SQLIdentifierExpr("ANY");
        }
    }
    
    protected SQLExpr parseInterval() {
        throw new ParserUnsupportedException(getLexer().getToken());
    }
    
    public SQLExpr primaryRest(SQLExpr expr) {
        Preconditions.checkNotNull(expr);
        // TODO 解析游标, 未来删除
        if (getLexer().equalToken(Token.OF)) {
            if (expr instanceof SQLIdentifierExpr) {
                String name = ((SQLIdentifierExpr) expr).getSimpleName();
                if ("CURRENT".equalsIgnoreCase(name)) {
                    getLexer().nextToken();
                    SQLName cursorName = this.name();
                    return new SQLCurrentOfCursorExpr(cursorName);
                }
            }
        }
        if (getLexer().equalToken(Token.DOT)) {
            getLexer().nextToken();
            if (expr instanceof SQLCharExpr) {
                String text = ((SQLCharExpr) expr).getText();
                expr = new SQLIdentifierExpr(text);
            }
            expr = dotRest(expr);
            return primaryRest(expr);
        } else {
            if (getLexer().equalToken(Token.LEFT_PAREN)) {
                return methodRest(expr, true);
            }
        }
        return expr;
    }
    
    protected SQLExpr methodRest(final SQLExpr expr, final boolean acceptLeftParen) {
        if (acceptLeftParen) {
            getLexer().accept(Token.LEFT_PAREN);
        }
        if (expr instanceof SQLName || expr instanceof SQLDefaultExpr) {
            String methodName;
            SQLMethodInvokeExpr methodInvokeExpr;
            if (expr instanceof SQLPropertyExpr) {
                methodName = ((SQLPropertyExpr) expr).getSimpleName();
                methodInvokeExpr = new SQLMethodInvokeExpr(methodName, ((SQLPropertyExpr) expr).getOwner());
            } else {
                methodName = expr.toString();
                methodInvokeExpr = new SQLMethodInvokeExpr(methodName);
            }
            if (aggregateFunctions.contains(methodName)) {
                return parseAggregateExpr(methodName);
            }
            if (!getLexer().equalToken(Token.RIGHT_PAREN)) {
                methodInvokeExpr.getParameters().addAll(exprList(methodInvokeExpr));
            }
            getLexer().accept(Token.RIGHT_PAREN);
            if (getLexer().equalToken(Token.OVER)) {
                SQLAggregateExpr aggregateExpr = new SQLAggregateExpr(methodName);
                aggregateExpr.getArguments().addAll(methodInvokeExpr.getParameters());
                over(aggregateExpr);
                return primaryRest(aggregateExpr);
            }
            return primaryRest(methodInvokeExpr);
        }
        throw new ParserUnsupportedException(getLexer().getToken());
    }
    
    protected SQLExpr dotRest(SQLExpr expr) {
        if (getLexer().equalToken(Token.STAR)) {
            getLexer().nextToken();
            expr = new SQLPropertyExpr(expr, "*");
        } else {
            String name = getLexer().getLiterals();
            getLexer().nextToken();
            if (getLexer().equalToken(Token.LEFT_PAREN)) {
                getLexer().nextToken();
                SQLMethodInvokeExpr methodInvokeExpr = new SQLMethodInvokeExpr(name, expr);
                if (getLexer().equalToken(Token.RIGHT_PAREN)) {
                    getLexer().nextToken();
                } else {
                    if (getLexer().equalToken(Token.PLUS)) {
                        methodInvokeExpr.addParameter(new SQLIdentifierExpr("+"));
                        getLexer().nextToken();
                    } else {
                        methodInvokeExpr.getParameters().addAll(exprList(methodInvokeExpr));
                    }
                    getLexer().accept(Token.RIGHT_PAREN);
                }
                expr = methodInvokeExpr;
            } else {
                expr = new SQLPropertyExpr(expr, name);
            }
        }
        expr = primaryRest(expr);
        return expr;
    }
    
    public final List<SQLExpr> exprList(final SQLObject parent) {
        List<SQLExpr> result = new LinkedList<>();
        if (getLexer().equalToken(Token.RIGHT_PAREN) || getLexer().equalToken(Token.RIGHT_BRACKET) || getLexer().equalToken(Token.EOF)) {
            return result;
        }
        result.add(expr(parent));
        while (getLexer().equalToken(Token.COMMA)) {
            getLexer().nextToken();
            result.add(expr(parent));
        }
        return result;
    }
    
    public SQLName name() {
        String identifierName;
        if (getLexer().equalToken(Token.LITERAL_ALIAS)) {
            identifierName = '"' + getLexer().getLiterals() + '"';
            getLexer().nextToken();
        } else if (getLexer().equalToken(Token.IDENTIFIER)) {
            identifierName = getLexer().getLiterals();
            getLexer().nextToken();
        } else if (getLexer().equalToken(Token.LITERAL_CHARS)) {
            identifierName = '\'' + getLexer().getLiterals() + '\'';
            getLexer().nextToken();
        } else if (getLexer().equalToken(Token.VARIANT)) {
            identifierName = getLexer().getLiterals();
            getLexer().nextToken();
        } else {
            switch (getLexer().getToken()) {
                case MODEL:
                case PCTFREE:
                case INITRANS:
                case MAXTRANS:
                case SEGMENT:
                case CREATION:
                case IMMEDIATE:
                case DEFERRED:
                case STORAGE:
                case NEXT:
                case MINEXTENTS:
                case MAXEXTENTS:
                case MAXSIZE:
                case PCTINCREASE:
                case FLASH_CACHE:
                case CELL_FLASH_CACHE:
                case KEEP:
                case NONE:
                case LOB:
                case STORE:
                case ROW:
                case CHUNK:
                case CACHE:
                case NOCACHE:
                case LOGGING:
                case NOCOMPRESS:
                case KEEP_DUPLICATES:
                case EXCEPTIONS:
                case PURGE:
                case INITIALLY:
                case END:
                case COMMENT:
                case ENABLE:
                case DISABLE:
                case SEQUENCE:
                case USER:
                case ANALYZE:
                case OPTIMIZE:
                case GRANT:
                case REVOKE:
                //binary有很多含义，getLexer()识别了这个token，实际上应该当做普通IDENTIFIER
                case BINARY:
                    identifierName = getLexer().getLiterals();
                    getLexer().nextToken();
                    break;
                default:
                    throw new ParserException(getLexer());
            }
        }
        SQLName name = new SQLIdentifierExpr(identifierName);
        name = nameRest(name);
        return name;
    }
    
    public SQLName nameRest(SQLName name) {
        if (getLexer().equalToken(Token.DOT)) {
            getLexer().nextToken();

            if (getLexer().equalToken(Token.KEY)) {
                name = new SQLPropertyExpr(name, "KEY");
                getLexer().nextToken();
                return name;
            }

            if (!getLexer().equalToken(Token.LITERAL_ALIAS) && !getLexer().equalToken(Token.IDENTIFIER)) {
                throw new ParserException(getLexer());
            }

            if (getLexer().equalToken(Token.LITERAL_ALIAS)) {
                name = new SQLPropertyExpr(name, '"' + getLexer().getLiterals() + '"');
            } else {
                name = new SQLPropertyExpr(name, getLexer().getLiterals());
            }
            getLexer().nextToken();
            name = nameRest(name);
        }
        return name;
    }
    
    protected SQLAggregateExpr parseAggregateExpr(final String methodName) {
        String upperCaseMethodName = methodName.toUpperCase();
        SQLAggregateExpr result;
        if (getLexer().equalToken(Token.ALL)) {
            result = new SQLAggregateExpr(upperCaseMethodName, SQLAggregateOption.ALL);
            getLexer().nextToken();
        } else if (getLexer().equalToken(Token.DISTINCT)) {
            result = new SQLAggregateExpr(upperCaseMethodName, SQLAggregateOption.DISTINCT);
            getLexer().nextToken();
        } else if (getLexer().identifierEquals("DEDUPLICATION")) { //just for nut
            result = new SQLAggregateExpr(upperCaseMethodName, SQLAggregateOption.DEDUPLICATION);
            getLexer().nextToken();
        } else {
            result = new SQLAggregateExpr(upperCaseMethodName);
        }
        result.getArguments().addAll(exprList(result));
        parseAggregateExprRest(result);
        getLexer().accept(Token.RIGHT_PAREN);
        if (getLexer().equalToken(Token.OVER)) {
            over(result);
        }
        return result;
    }
    
    protected void over(final SQLAggregateExpr aggregateExpr) {
        getLexer().nextToken();
        SQLOver over = new SQLOver();
        getLexer().accept(Token.LEFT_PAREN);
        if (getLexer().equalToken(Token.PARTITION) || getLexer().identifierEquals("PARTITION")) {
            getLexer().nextToken();
            getLexer().accept(Token.BY);
            if (getLexer().equalToken(Token.LEFT_PAREN)) {
                getLexer().nextToken();
                over.getPartitionBy().addAll(exprList(over));
                getLexer().accept(Token.RIGHT_PAREN);
            } else {
                over.getPartitionBy().addAll(exprList(over));
            }
        }
        // TODO 弄明白
        //over.setOrderBy(parseOrderBy());
        getLexer().accept(Token.RIGHT_PAREN);
        aggregateExpr.setOver(over);
    }
    
    protected SQLAggregateExpr parseAggregateExprRest(final SQLAggregateExpr aggregateExpr) {
        return aggregateExpr;
    }
    
    public List<OrderByContext> parseOrderBy() {
        if (!getLexer().skipIfEqual(Token.ORDER)) {
            return Collections.emptyList();
        }
        List<OrderByContext> result = new LinkedList<>();
        getLexer().skipIfEqual(Token.SIBLINGS);
        getLexer().accept(Token.BY);
        OrderByContext orderByContext = parseSelectOrderByItem();
        if (null != orderByContext) {
            result.add(orderByContext);
        }
        while (getLexer().equalToken(Token.COMMA)) {
            getLexer().nextToken();
            orderByContext = parseSelectOrderByItem();
            if (null != orderByContext) {
                result.add(orderByContext);
            }
        }
        return result;
    }
    
    public OrderByContext parseSelectOrderByItem() {
        SQLExpr expr = expr();
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
    
    public final SQLExpr bitAndRest(SQLExpr expr) {
        while (getLexer().equalToken(Token.AMP)) {
            getLexer().nextToken();
            SQLExpr rightExp = shift();
            expr = new SQLBinaryOpExpr(expr, SQLBinaryOperator.BitwiseAnd, rightExp);
        }
        return expr;
    }
    
    public final SQLExpr bitOrRest(SQLExpr expr) {
        while (getLexer().equalToken(Token.BAR)) {
            getLexer().nextToken();
            SQLExpr rightExp = bitAndRest(shift());
            expr = new SQLBinaryOpExpr(expr, SQLBinaryOperator.BitwiseOr, rightExp);
            expr = bitAndRest(expr);
        }
        return expr;
    }
    
    public final SQLExpr equality() {
        return equalityRest(bitOrRest(bitAndRest(shift())));
    }
    
    public SQLExpr equalityRest(SQLExpr expr) {
        SQLExpr rightExp;
        if (getLexer().equalToken(Token.EQ)) {
            getLexer().nextToken();
            int rightStartPosition = getLexer().getCurrentPosition() - getLexer().getLiterals().length();
            rightExp = bitOrRest(bitAndRest(shift()));
            rightExp = equalityRest(rightExp);
            SQLBinaryOpExpr sqlBinaryOpExpr = new SQLBinaryOpExpr(expr, SQLBinaryOperator.Equality, rightExp);
            sqlBinaryOpExpr.setRightStartPosition(rightStartPosition);
            expr = sqlBinaryOpExpr;
        } else if (getLexer().equalToken(Token.BANG_EQ)) {
            getLexer().nextToken();
            rightExp = bitOrRest(bitAndRest(shift()));
            rightExp = equalityRest(rightExp);
            expr = new SQLBinaryOpExpr(expr, SQLBinaryOperator.NotEqual, rightExp);
        } else if (getLexer().equalToken(Token.COLON_EQ)) {
            getLexer().nextToken();
            rightExp = expr();
            expr = new SQLBinaryOpExpr(expr, SQLBinaryOperator.Assignment, rightExp);
        }
        return expr;
    }
    
    public final SQLExpr inRest(SQLExpr expr) {
        if (getLexer().equalToken(Token.IN)) {
            getLexer().nextToken();
            SQLInListExpr inListExpr = new SQLInListExpr(expr, false);
            if (getLexer().equalToken(Token.LEFT_PAREN)) {
                getLexer().nextToken();
                inListExpr.getTargetList().addAll(exprList(inListExpr));
                getLexer().accept(Token.RIGHT_PAREN);
            } else {
                SQLExpr itemExpr = primary();
                itemExpr.setParent(inListExpr);
                inListExpr.getTargetList().add(itemExpr);
            }
            expr = inListExpr;
            if (inListExpr.getTargetList().size() == 1) {
                SQLExpr targetExpr = inListExpr.getTargetList().get(0);
                if (targetExpr instanceof SQLQueryExpr) {
                    SQLInSubQueryExpr inSubQueryExpr = new SQLInSubQueryExpr();
                    inSubQueryExpr.setExpr(inListExpr.getExpr());
                    inSubQueryExpr.setSubQuery(((SQLQueryExpr) targetExpr).getSubQuery());
                    expr = inSubQueryExpr;
                }
            }
        }
        return expr;
    }
    
    public final SQLExpr additive() {
        return additiveRest(multiplicativeRest(bitXorRest(primary())));
    }
    
    public SQLExpr additiveRest(SQLExpr expr) {
        if (getLexer().equalToken(Token.PLUS)) {
            getLexer().nextToken();
            SQLExpr rightExp = multiplicativeRest(bitXorRest(primary()));
            expr = new SQLBinaryOpExpr(expr, SQLBinaryOperator.Add, rightExp);
            expr = additiveRest(expr);
        } else if (getLexer().equalToken(Token.DOUBLE_BAR)) {
            getLexer().nextToken();
            SQLExpr rightExp = multiplicativeRest(bitXorRest(primary()));
            expr = new SQLBinaryOpExpr(expr, SQLBinaryOperator.Concat, rightExp);
            expr = additiveRest(expr);
        } else if (getLexer().equalToken(Token.SUB)) {
            getLexer().nextToken();
            SQLExpr rightExp = multiplicativeRest(bitXorRest(primary()));
            expr = new SQLBinaryOpExpr(expr, SQLBinaryOperator.Subtract, rightExp);
            expr = additiveRest(expr);
        }
        return expr;
    }
    
    public final SQLExpr shift() {
        return shiftRest(additive());
    }
    
    public SQLExpr shiftRest(SQLExpr expr) {
        if (getLexer().equalToken(Token.DOUBLE_LT)) {
            getLexer().nextToken();
            SQLExpr rightExp = additive();
            expr = new SQLBinaryOpExpr(expr, SQLBinaryOperator.LeftShift, rightExp);
            expr = shiftRest(expr);
        } else if (getLexer().equalToken(Token.DOUBLE_GT)) {
            getLexer().nextToken();
            SQLExpr rightExp = additive();
            expr = new SQLBinaryOpExpr(expr, SQLBinaryOperator.RightShift, rightExp);
            expr = shiftRest(expr);
        }
        return expr;
    }
    
    public SQLExpr and() {
        return andRest(relational());
    }
    
    public SQLExpr andRest(SQLExpr expr) {
        while (true) {
            if (getLexer().equalToken(Token.AND) || getLexer().equalToken(Token.DOUBLE_AMP)) {
                getLexer().nextToken();
                SQLExpr rightExp = relational();
                expr = new SQLBinaryOpExpr(expr, SQLBinaryOperator.BooleanAnd, rightExp);
            } else {
                break;
            }
        }
        return expr;
    }
    
    public SQLExpr or() {
        return orRest(and());
    }
    
    public SQLExpr orRest(SQLExpr expr) {
        while (true) {
            if (getLexer().equalToken(Token.OR)) {
                getLexer().nextToken();
                SQLExpr rightExp = and();
                expr = new SQLBinaryOpExpr(expr, SQLBinaryOperator.BooleanOr, rightExp);
            } else if (getLexer().equalToken(Token.XOR)) {
                getLexer().nextToken();
                SQLExpr rightExp = and();
                expr = new SQLBinaryOpExpr(expr, SQLBinaryOperator.BooleanXor, rightExp);
            } else {
                break;
            }
        }
        return expr;
    }
    
    public SQLExpr relational() {
        return relationalRest(equality());
    }

    public SQLExpr relationalRest(SQLExpr expr) {
        SQLExpr rightExp;
        if (getLexer().equalToken(Token.LT)) {
            SQLBinaryOperator op = SQLBinaryOperator.LessThan;
            getLexer().nextToken();
            if (getLexer().equalToken(Token.EQ)) {
                getLexer().nextToken();
                op = SQLBinaryOperator.LessThanOrEqual;
            }
            rightExp = bitOrRest(bitAndRest(shift()));
            expr = new SQLBinaryOpExpr(expr, op, rightExp);
        } else if (getLexer().equalToken(Token.LT_EQ)) {
            getLexer().nextToken();
            rightExp = bitOrRest(bitAndRest(shift()));
            expr = new SQLBinaryOpExpr(expr, SQLBinaryOperator.LessThanOrEqual, rightExp);
        } else if (getLexer().equalToken(Token.LT_EQ_GT)) {
            getLexer().nextToken();
            rightExp = bitOrRest(bitAndRest(shift()));
            expr = new SQLBinaryOpExpr(expr, SQLBinaryOperator.LessThanOrEqualOrGreaterThan, rightExp);
        } else if (getLexer().equalToken(Token.GT)) {
            SQLBinaryOperator op = SQLBinaryOperator.GreaterThan;
            getLexer().nextToken();
            if (getLexer().equalToken(Token.EQ)) {
                getLexer().nextToken();
                op = SQLBinaryOperator.GreaterThanOrEqual;
            }
            rightExp = bitOrRest(bitAndRest(shift()));
            expr = new SQLBinaryOpExpr(expr, op, rightExp);
        } else if (getLexer().equalToken(Token.GT_EQ)) {
            getLexer().nextToken();
            rightExp = bitOrRest(bitAndRest(shift()));
            expr = new SQLBinaryOpExpr(expr, SQLBinaryOperator.GreaterThanOrEqual, rightExp);
        } else if (getLexer().equalToken(Token.BANG_LT)) {
            getLexer().nextToken();
            rightExp = bitOrRest(bitAndRest(shift()));
            expr = new SQLBinaryOpExpr(expr, SQLBinaryOperator.NotLessThan, rightExp);
        } else if (getLexer().equalToken(Token.BANG_GT)) {
            getLexer().nextToken();
            rightExp = bitOrRest(bitAndRest(shift()));
            rightExp = relationalRest(rightExp);
            expr = new SQLBinaryOpExpr(expr, SQLBinaryOperator.NotGreaterThan, rightExp);
        } else if (getLexer().equalToken(Token.LT_GT)) {
            getLexer().nextToken();
            rightExp = bitOrRest(bitAndRest(shift()));
            expr = new SQLBinaryOpExpr(expr, SQLBinaryOperator.LessThanOrGreater, rightExp);
        } else if (getLexer().equalToken(Token.LIKE)) {
            getLexer().nextToken();
            rightExp = bitOrRest(bitAndRest(shift()));
            expr = new SQLBinaryOpExpr(expr, SQLBinaryOperator.Like, rightExp);
            if (getLexer().equalToken(Token.ESCAPE)) {
                getLexer().nextToken();
                rightExp = primary();
                expr = new SQLBinaryOpExpr(expr, SQLBinaryOperator.Escape, rightExp);
            }
        } else if (getLexer().identifierEquals("RLIKE")) {
            getLexer().nextToken();
            rightExp = equality();
            rightExp = relationalRest(rightExp);
            expr = new SQLBinaryOpExpr(expr, SQLBinaryOperator.RLike, rightExp);
        } else if (getLexer().equalToken(Token.NOT)) {
            getLexer().nextToken();
            expr = notRationalRest(expr);
        } else if (getLexer().equalToken(Token.BETWEEN)) {
            getLexer().nextToken();
            SQLExpr beginExpr = bitOrRest(bitAndRest(shift()));
            getLexer().accept(Token.AND);
            SQLExpr endExpr = bitOrRest(bitAndRest(shift()));
            expr = new SQLBetweenExpr(expr, beginExpr, endExpr);
        } else if (getLexer().equalToken(Token.IS)) {
            getLexer().nextToken();
            if (getLexer().equalToken(Token.NOT)) {
                getLexer().nextToken();
                SQLExpr rightExpr = primary();
                expr = new SQLBinaryOpExpr(expr, SQLBinaryOperator.IsNot, rightExpr);
            } else {
                SQLExpr rightExpr = primary();
                expr = new SQLBinaryOpExpr(expr, SQLBinaryOperator.Is, rightExpr);
            }
        } else if (getLexer().equalToken(Token.IN)) {
            expr = inRest(expr);
        }
        return expr;
    }

    public SQLExpr notRationalRest(SQLExpr expr) {
        if (getLexer().equalToken(Token.LIKE)) {
            getLexer().nextToken();
            SQLExpr rightExp = equality();
            rightExp = relationalRest(rightExp);
            expr = new SQLBinaryOpExpr(expr, SQLBinaryOperator.NotLike, rightExp);
            if (getLexer().getToken() == Token.ESCAPE) {
                getLexer().nextToken();
                rightExp = expr();
                expr = new SQLBinaryOpExpr(expr, SQLBinaryOperator.Escape, rightExp);
            }
        } else if (getLexer().getToken() == Token.IN) {
            getLexer().nextToken();
            getLexer().accept(Token.LEFT_PAREN);
            SQLInListExpr inListExpr = new SQLInListExpr(expr, true);
            inListExpr.getTargetList().addAll(exprList(inListExpr));
            expr = inListExpr;
            getLexer().accept(Token.RIGHT_PAREN);
            if (inListExpr.getTargetList().size() == 1) {
                SQLExpr targetExpr = inListExpr.getTargetList().get(0);
                if (targetExpr instanceof SQLQueryExpr) {
                    SQLInSubQueryExpr inSubQueryExpr = new SQLInSubQueryExpr();
                    inSubQueryExpr.setNot(true);
                    inSubQueryExpr.setExpr(inListExpr.getExpr());
                    inSubQueryExpr.setSubQuery(((SQLQueryExpr) targetExpr).getSubQuery());
                    expr = inSubQueryExpr;
                }
            }
            expr = relationalRest(expr);
            return expr;
        } else if (getLexer().equalToken(Token.BETWEEN)) {
            getLexer().nextToken();
            SQLExpr beginExpr = bitOrRest(bitAndRest(shift()));
            getLexer().accept(Token.AND);
            SQLExpr endExpr = bitOrRest(bitAndRest(shift()));
            expr = new SQLBetweenExpr(expr, true, beginExpr, endExpr);
            return expr;
        } else if (getLexer().identifierEquals("RLIKE")) {
            getLexer().nextToken();
            SQLExpr rightExp = primary();
            rightExp = relationalRest(rightExp);
            return new SQLBinaryOpExpr(expr, SQLBinaryOperator.NotRLike, rightExp);
        } else {
            throw new ParserUnsupportedException(getLexer().getToken());
        }
        return expr;
    }
    
    public SQLDataType parseDataType() {
        if (getLexer().equalToken(Token.DEFAULT) || getLexer().equalToken(Token.NOT) || getLexer().equalToken(Token.NULL)) {
            return null;
        }
        String typeName = name().toString();
        if (isCharType(typeName)) {
            SQLCharacterDataType charType = new SQLCharacterDataType(typeName);
            if (getLexer().equalToken(Token.LEFT_PAREN)) {
                getLexer().nextToken();
                SQLExpr arg = this.expr();
                arg.setParent(charType);
                charType.getArguments().add(arg);
                getLexer().accept(Token.RIGHT_PAREN);
            }
            return parseCharTypeRest(charType);
        }
        if ("character".equalsIgnoreCase(typeName) && "varying".equalsIgnoreCase(getLexer().getLiterals())) {
            typeName += ' ' + getLexer().getLiterals();
            getLexer().nextToken();
        }
        SQLDataType dataType = new SQLDataTypeImpl(typeName);
        return parseDataTypeRest(dataType);
    }
    
    protected SQLDataType parseDataTypeRest(final SQLDataType dataType) {
        if (getLexer().equalToken(Token.LEFT_PAREN)) {
            getLexer().nextToken();
            dataType.getArguments().addAll(exprList(dataType));
            getLexer().accept(Token.RIGHT_PAREN);
        }
        return dataType;
    }
    
    protected boolean isCharType(final String dataTypeName) {
        return "char".equalsIgnoreCase(dataTypeName)
               || "varchar".equalsIgnoreCase(dataTypeName)
               || "nchar".equalsIgnoreCase(dataTypeName)
               || "nvarchar".equalsIgnoreCase(dataTypeName)
               || "tinytext".equalsIgnoreCase(dataTypeName)
               || "text".equalsIgnoreCase(dataTypeName)
               || "mediumtext".equalsIgnoreCase(dataTypeName)
               || "longtext".equalsIgnoreCase(dataTypeName);
    }
    
    protected SQLDataType parseCharTypeRest(final SQLCharacterDataType charType) {
        if (getLexer().equalToken(Token.BINARY)) {
            charType.setHasBinary(true);
            getLexer().nextToken();
        }
        if (getLexer().identifierEquals("CHARACTER")) {
            getLexer().nextToken();
            getLexer().accept(Token.SET);
            if (!getLexer().equalToken(Token.IDENTIFIER) && !getLexer().equalToken(Token.LITERAL_CHARS)) {
                throw new ParserException(getLexer());
            }
            charType.setCharSetName(getLexer().getLiterals());
            getLexer().nextToken();
        }
        if (getLexer().equalToken(Token.BINARY)) {
            charType.setHasBinary(true);
            getLexer().nextToken();
        }
        if (getLexer().equalToken(Token.IDENTIFIER)) {
            if (getLexer().getLiterals().equalsIgnoreCase("COLLATE")) {
                getLexer().nextToken();
                if (!getLexer().equalToken(Token.IDENTIFIER)) {
                    throw new ParserException(getLexer());
                }
                charType.setCollate(getLexer().getLiterals());
                getLexer().nextToken();
            }
        }
        return charType;
    }
    
    public final SQLSelectItem parseSelectItem(final int index, final SelectSQLContext sqlContext) {
        getLexer().skipIfEqual(Token.CONNECT_BY_ROOT);
        String literals = getLexer().getLiterals();
        if (literals.equalsIgnoreCase(AggregationColumn.AggregationType.MAX.name())
                || getLexer().getLiterals().equalsIgnoreCase(AggregationColumn.AggregationType.MIN.name())
                || getLexer().getLiterals().equalsIgnoreCase(AggregationColumn.AggregationType.SUM.name())
                || getLexer().getLiterals().equalsIgnoreCase(AggregationColumn.AggregationType.COUNT.name())
                || getLexer().getLiterals().equalsIgnoreCase(AggregationColumn.AggregationType.AVG.name())) {
            AggregationColumn.AggregationType aggregationType = AggregationColumn.AggregationType.valueOf(literals.toUpperCase());
            getLexer().nextToken();
            if (getLexer().equalToken(Token.LEFT_PAREN)) {
                String expression = literals + getLexer().skipParentheses();
                String alias = as().orNull();
                SQLSelectItem result = new SQLSelectItem(new SQLIdentifierExpr(expression), alias);
                SelectItemContext selectItemContext = new AggregationSelectItemContext(SQLUtil.getExactlyValue(expression), SQLUtil.getExactlyValue(alias), index, aggregationType);
                result.setSelectItemContext(selectItemContext);
                return result;
            }
        }
        StringBuilder expression = new StringBuilder();
        boolean isStar = false;
        // FIXME 无as的alias解析, 应该做成倒数第二个token不是运算符,倒数第一个token是Identifier或char,则为别名, 不过CommonSelectItemContext类型并不关注expression和alias
        // FIXME *解析不完全正确,乘号也会解析为star
        while (!getLexer().equalToken(Token.AS) && !getLexer().equalToken(Token.COMMA) && !getLexer().equalToken(Token.FROM) && !getLexer().equalToken(Token.EOF)) {
            String value = getLexer().getLiterals();
            int position = getLexer().getCurrentPosition() - value.length();
            expression.append(value);
            if (getLexer().equalToken(Token.STAR)) {
                isStar = true;
            }
            getLexer().nextToken();
            if (getLexer().equalToken(Token.DOT)) {
                sqlContext.getSqlTokens().add(new TableToken(position, value, SQLUtil.getExactlyValue(value)));
            }
        }
        String alias = as().orNull();
        SQLSelectItem result = new SQLSelectItem(new SQLIdentifierExpr(expression.toString()), alias);
        SelectItemContext selectItemContext = new CommonSelectItemContext(SQLUtil.getExactlyValue(expression.toString()), SQLUtil.getExactlyValue(alias), index, isStar);
        result.setSelectItemContext(selectItemContext);
        return result;
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
            parseCondition(sqlContext, parseContext);
        } while (lexer.skipIfEqual(Token.AND));
        if (lexer.equalToken(Token.OR)) {
            throw new ParserUnsupportedException(lexer.getToken());
        }
    }
    
    private void parseCondition(final SQLContext sqlContext, final ParseContext parseContext) {
        SQLExpr left = getSqlExprWithVariant(sqlContext);
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
    }
    
    private void parseEqualCondition(final SQLContext sqlContext, final ParseContext parseContext, final SQLExpr left) {
        lexer.nextToken();
        SQLExpr right = getSqlExprWithVariant(sqlContext);
        // TODO 如果有多表,且找不到column是哪个表的,则不加入condition,以后需要解析binding table
        if (1 == sqlContext.getTables().size() || left instanceof SQLPropertyExpr) {
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
            rights.add(getSqlExprWithVariant(sqlContext));
        } while (!lexer.equalToken(Token.RIGHT_PAREN));
        parseContext.addCondition(left, Condition.BinaryOperator.IN, rights, parameters);
        lexer.nextToken();
    }
    
    private void parseBetweenCondition(final SQLContext sqlContext, final ParseContext parseContext, final SQLExpr left) {
        lexer.nextToken();
        List<SQLExpr> rights = new LinkedList<>();
        rights.add(getSqlExprWithVariant(sqlContext));
        lexer.accept(Token.AND);
        rights.add(getSqlExprWithVariant(sqlContext));
        parseContext.addCondition(left, Condition.BinaryOperator.BETWEEN, rights, parameters);
    }
    
    private void parserOtherCondition(final SQLContext sqlContext) {
        lexer.nextToken();
        getSqlExprWithVariant(sqlContext);
    }
    
    private SQLExpr getSqlExprWithVariant(final SQLContext sqlContext) {
        SQLExpr result = parseSQLExpr(sqlContext);
        if (result instanceof SQLVariantRefExpr) {
            ((SQLVariantRefExpr) result).setIndex(++parametersIndex);
            result.getAttributes().put(SQLEvalConstants.EVAL_VALUE, parameters.get(parametersIndex - 1));
            result.getAttributes().put(SQLEvalConstants.EVAL_VAR_INDEX, parametersIndex - 1);
        }
        return result;
    }
    
    private SQLExpr parseSQLExpr(final SQLContext sqlContext) {
        String literals = lexer.getLiterals();
        if (lexer.equalToken(Token.IDENTIFIER)) {
            String tableName = sqlContext.getTables().get(0).getName();
            if (tableName.equalsIgnoreCase(SQLUtil.getExactlyValue(literals))) {
                sqlContext.getSqlTokens().add(new TableToken(lexer.getCurrentPosition() - literals.length(), literals, tableName));
            }
            lexer.nextToken();
            if (lexer.equalToken(Token.DOT)) {
                lexer.nextToken();
                SQLExpr result = new SQLPropertyExpr(new SQLIdentifierExpr(literals), lexer.getLiterals());
                lexer.nextToken();
                return result;
            }
            return new SQLIdentifierExpr(literals);
        }
        SQLExpr result = getSQLExpr(literals);
        lexer.nextToken();
        return result;
    }
    
    private SQLExpr getSQLExpr(final String literals) {
        if (lexer.equalToken(Token.VARIANT) || lexer.equalToken(Token.QUESTION)) {
            return new SQLVariantRefExpr("?");
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
        throw new ParserUnsupportedException(lexer.getToken());
    }
}
