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

import com.alibaba.druid.sql.ast.SQLCommentHint;
import com.alibaba.druid.sql.ast.SQLDataType;
import com.alibaba.druid.sql.ast.SQLDataTypeImpl;
import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.SQLName;
import com.alibaba.druid.sql.ast.SQLObject;
import com.alibaba.druid.sql.ast.SQLOrderBy;
import com.alibaba.druid.sql.ast.SQLOrderingSpecification;
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
import com.alibaba.druid.sql.ast.expr.SQLExistsExpr;
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
import com.alibaba.druid.sql.ast.statement.NotNullConstraint;
import com.alibaba.druid.sql.ast.statement.SQLAssignItem;
import com.alibaba.druid.sql.ast.statement.SQLCharacterDataType;
import com.alibaba.druid.sql.ast.statement.SQLCheck;
import com.alibaba.druid.sql.ast.statement.SQLColumnCheck;
import com.alibaba.druid.sql.ast.statement.SQLColumnDefinition;
import com.alibaba.druid.sql.ast.statement.SQLColumnPrimaryKey;
import com.alibaba.druid.sql.ast.statement.SQLColumnReference;
import com.alibaba.druid.sql.ast.statement.SQLColumnUniqueKey;
import com.alibaba.druid.sql.ast.statement.SQLConstraint;
import com.alibaba.druid.sql.ast.statement.SQLForeignKeyConstraint;
import com.alibaba.druid.sql.ast.statement.SQLForeignKeyImpl;
import com.alibaba.druid.sql.ast.statement.SQLPrimaryKey;
import com.alibaba.druid.sql.ast.statement.SQLPrimaryKeyImpl;
import com.alibaba.druid.sql.ast.statement.SQLSelect;
import com.alibaba.druid.sql.ast.statement.SQLSelectItem;
import com.alibaba.druid.sql.ast.statement.SQLSelectOrderByItem;
import com.alibaba.druid.sql.ast.statement.SQLUnique;
import com.alibaba.druid.sql.ast.statement.SQLUpdateSetItem;
import com.alibaba.druid.sql.lexer.Lexer;
import com.alibaba.druid.sql.lexer.Token;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class SQLExprParser extends SQLParser {
    
    private final Set<String> aggregateFunctions;
    
    public SQLExprParser(Lexer lexer, String dbType, final String... aggregateFunctions){
        super(lexer, dbType);
        this.aggregateFunctions = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        this.aggregateFunctions.addAll(Arrays.asList(aggregateFunctions));
    }
    
    public SQLExpr expr() {
        if (getLexer().equalToken(Token.STAR)) {
            getLexer().nextToken();
            SQLExpr expr = new SQLAllColumnExpr();
            if (getLexer().equalToken(Token.DOT)) {
                getLexer().nextToken();
                accept(Token.STAR);
                return new SQLPropertyExpr(expr, "*");
            }
            return expr;
        }
        SQLExpr expr = primary();
        if (getLexer().equalToken(Token.COMMA)) {
            return expr;
        }
        return exprRest(expr);
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
    
    public final SQLExpr bitXor() {
        SQLExpr expr = primary();
        return bitXorRest(expr);
    }
    
    public SQLExpr bitXorRest(SQLExpr expr) {
        if (getLexer().equalToken(Token.CARET)) {
            getLexer().nextToken();
            SQLExpr rightExp = primary();
            expr = new SQLBinaryOpExpr(expr, SQLBinaryOperator.BitwiseXor, rightExp, getDbType());
            expr = bitXorRest(expr);
        }
        return expr;
    }
    
    public final SQLExpr multiplicative() {
        SQLExpr expr = bitXor();
        return multiplicativeRest(expr);
    }
    
    public SQLExpr multiplicativeRest(SQLExpr expr) {
        if (getLexer().equalToken(Token.STAR)) {
            getLexer().nextToken();
            SQLExpr rightExp = bitXor();
            expr = new SQLBinaryOpExpr(expr, SQLBinaryOperator.Multiply, rightExp, getDbType());
            expr = multiplicativeRest(expr);
        } else if (getLexer().equalToken(Token.SLASH)) {
            getLexer().nextToken();
            SQLExpr rightExp = bitXor();
            expr = new SQLBinaryOpExpr(expr, SQLBinaryOperator.Divide, rightExp, getDbType());
            expr = multiplicativeRest(expr);
        } else if (getLexer().equalToken(Token.PERCENT)) {
            getLexer().nextToken();
            SQLExpr rightExp = bitXor();
            expr = new SQLBinaryOpExpr(expr, SQLBinaryOperator.Modulus, rightExp, getDbType());
            expr = multiplicativeRest(expr);
        }
        return expr;
    }
    
    public SQLExpr primary() {
        SQLExpr sqlExpr = null;
        final Token tok = getLexer().getToken();
        switch (tok) {
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
                accept(Token.RIGHT_PAREN);
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

                accept(Token.WHEN);
                SQLExpr testExpr = expr();
                accept(Token.THEN);
                SQLExpr valueExpr = expr();
                SQLCaseExpr.Item caseItem = new SQLCaseExpr.Item(testExpr, valueExpr);
                caseExpr.addItem(caseItem);

                while (getLexer().equalToken(Token.WHEN)) {
                    getLexer().nextToken();
                    testExpr = expr();
                    accept(Token.THEN);
                    valueExpr = expr();
                    caseItem = new SQLCaseExpr.Item(testExpr, valueExpr);
                    caseExpr.getItems().add(caseItem);
                }

                if (getLexer().equalToken(Token.ELSE)) {
                    getLexer().nextToken();
                    caseExpr.setElseExpr(expr());
                }

                accept(Token.END);

                sqlExpr = caseExpr;
                break;
            case EXISTS:
                getLexer().nextToken();
                accept(Token.LEFT_PAREN);
                sqlExpr = new SQLExistsExpr(createSelectParser().select());
                accept(Token.RIGHT_PAREN);
                break;
            case NOT:
                getLexer().nextToken();
                if (getLexer().equalToken(Token.EXISTS)) {
                    getLexer().nextToken();
                    accept(Token.LEFT_PAREN);
                    sqlExpr = new SQLExistsExpr(createSelectParser().select(), true);
                    accept(Token.RIGHT_PAREN);
                } else if (getLexer().equalToken(Token.LEFT_PAREN)) {
                    getLexer().nextToken();

                    SQLExpr notTarget = expr();

                    accept(Token.RIGHT_PAREN);
                    notTarget = exprRest(notTarget);

                    sqlExpr = new SQLNotExpr(notTarget);

                    return primaryRest(sqlExpr);
                } else {
                    SQLExpr restExpr = relational();
                    sqlExpr = new SQLNotExpr(restExpr);
                }
                break;
            case SELECT:
                sqlExpr = new SQLQueryExpr(createSelectParser().select());
                break;
            case CAST:
                getLexer().nextToken();
                accept(Token.LEFT_PAREN);
                SQLCastExpr cast = new SQLCastExpr();
                cast.setExpr(expr());
                accept(Token.AS);
                cast.setDataType(parseDataType());
                accept(Token.RIGHT_PAREN);
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
                        accept(Token.RIGHT_PAREN);
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
                        accept(Token.RIGHT_PAREN);
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
                sqlExpr = parseAliasExpr(getLexer().getLiterals());
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
                throw new ParserException(getLexer(), tok);
        }
        return primaryRest(sqlExpr);
    }
    
    protected SQLExpr parseAll() {
        SQLExpr sqlExpr;
        getLexer().nextToken();
        SQLAllExpr allExpr = new SQLAllExpr();
        accept(Token.LEFT_PAREN);
        SQLSelect allSubQuery = createSelectParser().select();
        allExpr.setSubQuery(allSubQuery);
        accept(Token.RIGHT_PAREN);
        allSubQuery.setParent(allExpr);
        sqlExpr = allExpr;
        return sqlExpr;
    }
    
    protected SQLExpr parseSome() {
        SQLExpr sqlExpr;
        getLexer().nextToken();
        SQLSomeExpr someExpr = new SQLSomeExpr();
        accept(Token.LEFT_PAREN);
        SQLSelect someSubQuery = createSelectParser().select();
        someExpr.setSubQuery(someSubQuery);
        accept(Token.RIGHT_PAREN);
        someSubQuery.setParent(someExpr);
        sqlExpr = someExpr;
        return sqlExpr;
    }
    
    protected SQLExpr parseAny() {
        SQLExpr sqlExpr;
        getLexer().nextToken();
        if (getLexer().equalToken(Token.LEFT_PAREN)) {
            accept(Token.LEFT_PAREN);
            if (getLexer().equalToken(Token.IDENTIFIER)) {
                SQLExpr expr = this.expr();
                SQLMethodInvokeExpr methodInvokeExpr = new SQLMethodInvokeExpr("ANY");
                methodInvokeExpr.addParameter(expr);
                accept(Token.RIGHT_PAREN);
                return methodInvokeExpr;
            }
            SQLAnyExpr anyExpr = new SQLAnyExpr();
            SQLSelect anySubQuery = createSelectParser().select();
            anyExpr.setSubQuery(anySubQuery);
            accept(Token.RIGHT_PAREN);
            anySubQuery.setParent(anyExpr);
            sqlExpr = anyExpr;
        } else {
            sqlExpr = new SQLIdentifierExpr("ANY");
        }
        return sqlExpr;
    }
    
    protected SQLExpr parseAliasExpr(String alias) {
        return new SQLIdentifierExpr('"' + alias + '"');
    }

    protected SQLExpr parseInterval() {
        throw new ParserUnsupportedException(getLexer().getToken());
    }

    public SQLSelectParser createSelectParser() {
        return new SQLSelectParser(this);
    }

    public SQLExpr primaryRest(SQLExpr expr) {
        if (null == expr) {
            throw new IllegalArgumentException("expr");
        }
        if (getLexer().equalToken(Token.OF)) {
            if (expr instanceof SQLIdentifierExpr) {
                String name = ((SQLIdentifierExpr) expr).getName();
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
    
    protected SQLExpr methodRest(SQLExpr expr, boolean acceptLPAREN) {
        if (acceptLPAREN) {
            accept(Token.LEFT_PAREN);
        }
        if (expr instanceof SQLName || expr instanceof SQLDefaultExpr) {
            String methodName;
            SQLMethodInvokeExpr methodInvokeExpr;
            if (expr instanceof SQLPropertyExpr) {
                methodName = ((SQLPropertyExpr) expr).getName();
                methodInvokeExpr = new SQLMethodInvokeExpr(methodName);
                methodInvokeExpr.setOwner(((SQLPropertyExpr) expr).getOwner());
            } else {
                methodName = expr.toString();
                methodInvokeExpr = new SQLMethodInvokeExpr(methodName);
            }
            if (aggregateFunctions.contains(methodName)) {
                return parseAggregateExpr(methodName);
            }
            if (!getLexer().equalToken(Token.RIGHT_PAREN)) {
                exprList(methodInvokeExpr.getParameters(), methodInvokeExpr);
            }
            accept(Token.RIGHT_PAREN);
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
            String name;
            if (getLexer().equalToken(Token.IDENTIFIER) || getLexer().equalToken(Token.LITERAL_CHARS)
                || getLexer().equalToken(Token.LITERAL_ALIAS)) {
                name = getLexer().getLiterals();
                getLexer().nextToken();
            } else if (getLexer().containsToken()) {
                name = getLexer().getLiterals();
                getLexer().nextToken();
            } else {
                throw new ParserException(getLexer());
            }
            if (getLexer().equalToken(Token.LEFT_PAREN)) {
                getLexer().nextToken();
                SQLMethodInvokeExpr methodInvokeExpr = new SQLMethodInvokeExpr(name);
                methodInvokeExpr.setOwner(expr);
                if (getLexer().equalToken(Token.RIGHT_PAREN)) {
                    getLexer().nextToken();
                } else {
                    if (getLexer().equalToken(Token.PLUS)) {
                        methodInvokeExpr.addParameter(new SQLIdentifierExpr("+"));
                        getLexer().nextToken();
                    } else {
                        exprList(methodInvokeExpr.getParameters(), methodInvokeExpr);
                    }
                    accept(Token.RIGHT_PAREN);
                }
                expr = methodInvokeExpr;
            } else {
                expr = new SQLPropertyExpr(expr, name);
            }
        }
        expr = primaryRest(expr);
        return expr;
    }
    
    public final void names(final Collection<SQLName> exprCol) {
        names(exprCol, null);
    }
    
    public final void names(final Collection<SQLName> exprCol, final SQLObject parent) {
        if (getLexer().equalToken(Token.RIGHT_BRACE)) {
            return;
        }
        if (getLexer().equalToken(Token.EOF)) {
            return;
        }
        SQLName name = name();
        name.setParent(parent);
        exprCol.add(name);
        while (getLexer().equalToken(Token.COMMA)) {
            getLexer().nextToken();
            name = name();
            name.setParent(parent);
            exprCol.add(name);
        }
    }
    
    public final void exprList(Collection<SQLExpr> exprCol, SQLObject parent) {
        if (getLexer().equalToken(Token.RIGHT_PAREN) || getLexer().equalToken(Token.RIGHT_BRACKET)) {
            return;
        }
        if (getLexer().equalToken(Token.EOF)) {
            return;
        }
        SQLExpr expr = expr();
        expr.setParent(parent);
        exprCol.add(expr);
        while (getLexer().equalToken(Token.COMMA)) {
            getLexer().nextToken();
            expr = expr();
            expr.setParent(parent);
            exprCol.add(expr);
        }
    }
    
    public SQLName name() {
        String identName;
        if (getLexer().equalToken(Token.LITERAL_ALIAS)) {
            identName = '"' + getLexer().getLiterals() + '"';
            getLexer().nextToken();
        } else if (getLexer().equalToken(Token.IDENTIFIER)) {
            identName = getLexer().getLiterals();

            getLexer().nextToken();
        } else if (getLexer().equalToken(Token.LITERAL_CHARS)) {
            identName = '\'' + getLexer().getLiterals() + '\'';
            getLexer().nextToken();
        } else if (getLexer().equalToken(Token.VARIANT)) {
            identName = getLexer().getLiterals();
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
                    identName = getLexer().getLiterals();
                    getLexer().nextToken();
                    break;
                default:
                    throw new ParserException(getLexer());
            }
        }
        SQLName name = new SQLIdentifierExpr(identName);
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

            if (!getLexer().equalToken(Token.LITERAL_ALIAS) && !getLexer().equalToken(Token.IDENTIFIER)
                && (!getLexer().containsToken())) {
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
    
    protected SQLAggregateExpr parseAggregateExpr(String methodName) {
        methodName = methodName.toUpperCase();
        SQLAggregateExpr aggregateExpr;
        if (getLexer().equalToken(Token.ALL)) {
            aggregateExpr = new SQLAggregateExpr(methodName, SQLAggregateOption.ALL);
            getLexer().nextToken();
        } else if (getLexer().equalToken(Token.DISTINCT)) {
            aggregateExpr = new SQLAggregateExpr(methodName, SQLAggregateOption.DISTINCT);
            getLexer().nextToken();
        } else if (getLexer().identifierEquals("DEDUPLICATION")) { //just for nut
            aggregateExpr = new SQLAggregateExpr(methodName, SQLAggregateOption.DEDUPLICATION);
            getLexer().nextToken();
        } else {
            aggregateExpr = new SQLAggregateExpr(methodName);
        }
        exprList(aggregateExpr.getArguments(), aggregateExpr);
        parseAggregateExprRest(aggregateExpr);
        accept(Token.RIGHT_PAREN);
        if (getLexer().equalToken(Token.OVER)) {
            over(aggregateExpr);
        }
        return aggregateExpr;
    }
    
    protected void over(SQLAggregateExpr aggregateExpr) {
        getLexer().nextToken();
        SQLOver over = new SQLOver();
        accept(Token.LEFT_PAREN);
        if (getLexer().equalToken(Token.PARTITION) || getLexer().identifierEquals("PARTITION")) {
            getLexer().nextToken();
            accept(Token.BY);
            if (getLexer().equalToken(Token.LEFT_PAREN)) {
                getLexer().nextToken();
                exprList(over.getPartitionBy(), over);
                accept(Token.RIGHT_PAREN);
            } else {
                exprList(over.getPartitionBy(), over);
            }
        }
        over.setOrderBy(parseOrderBy());
        accept(Token.RIGHT_PAREN);
        aggregateExpr.setOver(over);
    }

    protected SQLAggregateExpr parseAggregateExprRest(SQLAggregateExpr aggregateExpr) {
        return aggregateExpr;
    }
    
    public SQLOrderBy parseOrderBy() {
        if (getLexer().equalToken(Token.ORDER)) {
            SQLOrderBy orderBy = new SQLOrderBy();
            getLexer().nextToken();
            accept(Token.BY);
            orderBy.addItem(parseSelectOrderByItem());
            while (getLexer().equalToken(Token.COMMA)) {
                getLexer().nextToken();
                orderBy.addItem(parseSelectOrderByItem());
            }
            return orderBy;
        }
        return null;
    }
    
    public SQLSelectOrderByItem parseSelectOrderByItem() {
        SQLSelectOrderByItem item = new SQLSelectOrderByItem();
        item.setExpr(expr());
        if (getLexer().equalToken(Token.ASC)) {
            getLexer().nextToken();
            item.setType(SQLOrderingSpecification.ASC);
        } else if (getLexer().equalToken(Token.DESC)) {
            getLexer().nextToken();
            item.setType(SQLOrderingSpecification.DESC);
        }
        return item;
    }
    
    public SQLUpdateSetItem parseUpdateSetItem() {
        SQLUpdateSetItem item = new SQLUpdateSetItem();

        if (getLexer().equalToken(Token.LEFT_PAREN)) {
            getLexer().nextToken();
            SQLListExpr list = new SQLListExpr();
            this.exprList(list.getItems(), list);
            accept(Token.RIGHT_PAREN);
            item.setColumn(list);
        } else {
            item.setColumn(this.primary());
        }
        if (getLexer().equalToken(Token.COLON_EQ)) {
            getLexer().nextToken();
        } else {
            accept(Token.EQ);
        }
        
        item.setValue(this.expr());
        return item;
    }
    
    public final SQLExpr bitAnd() {
        SQLExpr expr = shift();
        return bitAndRest(expr);
    }
    
    public final SQLExpr bitAndRest(SQLExpr expr) {
        while (getLexer().equalToken(Token.AMP)) {
            getLexer().nextToken();
            SQLExpr rightExp = shift();
            expr = new SQLBinaryOpExpr(expr, SQLBinaryOperator.BitwiseAnd, rightExp, getDbType());
        }
        return expr;
    }
    
    public final SQLExpr bitOr() {
        SQLExpr expr = bitAnd();
        return bitOrRest(expr);
    }
    
    public final SQLExpr bitOrRest(SQLExpr expr) {
        while (getLexer().equalToken(Token.BAR)) {
            getLexer().nextToken();
            SQLExpr rightExp = bitAnd();
            expr = new SQLBinaryOpExpr(expr, SQLBinaryOperator.BitwiseOr, rightExp, getDbType());
            expr = bitAndRest(expr);
        }
        return expr;
    }
    
    public final SQLExpr equality() {
        SQLExpr expr = bitOr();
        return equalityRest(expr);
    }
    
    public SQLExpr equalityRest(SQLExpr expr) {
        SQLExpr rightExp;
        if (getLexer().equalToken(Token.EQ)) {
            getLexer().nextToken();
            rightExp = bitOr();
            rightExp = equalityRest(rightExp);
            expr = new SQLBinaryOpExpr(expr, SQLBinaryOperator.Equality, rightExp, getDbType());
        } else if (getLexer().equalToken(Token.BANG_EQ)) {
            getLexer().nextToken();
            rightExp = bitOr();
            rightExp = equalityRest(rightExp);
            expr = new SQLBinaryOpExpr(expr, SQLBinaryOperator.NotEqual, rightExp, getDbType());
        } else if (getLexer().equalToken(Token.COLON_EQ)) {
            getLexer().nextToken();
            rightExp = expr();
            expr = new SQLBinaryOpExpr(expr, SQLBinaryOperator.Assignment, rightExp, getDbType());
        }
        return expr;
    }
    
    public final SQLExpr inRest(SQLExpr expr) {
        if (getLexer().equalToken(Token.IN)) {
            getLexer().nextToken();
            SQLInListExpr inListExpr = new SQLInListExpr(expr);
            if (getLexer().equalToken(Token.LEFT_PAREN)) {
                getLexer().nextToken();
                exprList(inListExpr.getTargetList(), inListExpr);
                accept(Token.RIGHT_PAREN);
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
        SQLExpr expr = multiplicative();
        return additiveRest(expr);
    }
    
    public SQLExpr additiveRest(SQLExpr expr) {
        if (getLexer().equalToken(Token.PLUS)) {
            getLexer().nextToken();
            SQLExpr rightExp = multiplicative();
            expr = new SQLBinaryOpExpr(expr, SQLBinaryOperator.Add, rightExp, getDbType());
            expr = additiveRest(expr);
        } else if (getLexer().equalToken(Token.DOUBLE_BAR)) {
            getLexer().nextToken();
            SQLExpr rightExp = multiplicative();
            expr = new SQLBinaryOpExpr(expr, SQLBinaryOperator.Concat, rightExp, getDbType());
            expr = additiveRest(expr);
        } else if (getLexer().equalToken(Token.SUB)) {
            getLexer().nextToken();
            SQLExpr rightExp = multiplicative();
            expr = new SQLBinaryOpExpr(expr, SQLBinaryOperator.Subtract, rightExp, getDbType());
            expr = additiveRest(expr);
        }
        return expr;
    }
    
    public final SQLExpr shift() {
        SQLExpr expr = additive();
        return shiftRest(expr);
    }
    
    public SQLExpr shiftRest(SQLExpr expr) {
        if (getLexer().equalToken(Token.DOUBLE_LT)) {
            getLexer().nextToken();
            SQLExpr rightExp = additive();
            expr = new SQLBinaryOpExpr(expr, SQLBinaryOperator.LeftShift, rightExp, getDbType());
            expr = shiftRest(expr);
        } else if (getLexer().equalToken(Token.DOUBLE_GT)) {
            getLexer().nextToken();
            SQLExpr rightExp = additive();
            expr = new SQLBinaryOpExpr(expr, SQLBinaryOperator.RightShift, rightExp, getDbType());
            expr = shiftRest(expr);
        }
        return expr;
    }
    
    public SQLExpr and() {
        SQLExpr expr = relational();
        return andRest(expr);
    }
    
    public SQLExpr andRest(SQLExpr expr) {
        while (true) {
            if (getLexer().equalToken(Token.AND) || getLexer().equalToken(Token.DOUBLE_AMP)) {
                getLexer().nextToken();
                SQLExpr rightExp = relational();
                expr = new SQLBinaryOpExpr(expr, SQLBinaryOperator.BooleanAnd, rightExp, getDbType());
            } else {
                break;
            }
        }
        return expr;
    }
    
    public SQLExpr or() {
        SQLExpr expr = and();
        return orRest(expr);
    }
    
    public SQLExpr orRest(SQLExpr expr) {
        while (true) {
            if (getLexer().equalToken(Token.OR)) {
                getLexer().nextToken();
                SQLExpr rightExp = and();
                expr = new SQLBinaryOpExpr(expr, SQLBinaryOperator.BooleanOr, rightExp, getDbType());
            } else if (getLexer().equalToken(Token.XOR)) {
                getLexer().nextToken();
                SQLExpr rightExp = and();
                expr = new SQLBinaryOpExpr(expr, SQLBinaryOperator.BooleanXor, rightExp, getDbType());
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
            rightExp = bitOr();
            expr = new SQLBinaryOpExpr(expr, op, rightExp, getDbType());
        } else if (getLexer().equalToken(Token.LT_EQ)) {
            getLexer().nextToken();
            rightExp = bitOr();
            expr = new SQLBinaryOpExpr(expr, SQLBinaryOperator.LessThanOrEqual, rightExp, getDbType());
        } else if (getLexer().equalToken(Token.LT_EQ_GT)) {
            getLexer().nextToken();
            rightExp = bitOr();
            expr = new SQLBinaryOpExpr(expr, SQLBinaryOperator.LessThanOrEqualOrGreaterThan, rightExp, getDbType());
        } else if (getLexer().equalToken(Token.GT)) {
            SQLBinaryOperator op = SQLBinaryOperator.GreaterThan;
            getLexer().nextToken();
            if (getLexer().equalToken(Token.EQ)) {
                getLexer().nextToken();
                op = SQLBinaryOperator.GreaterThanOrEqual;
            }
            rightExp = bitOr();
            expr = new SQLBinaryOpExpr(expr, op, rightExp, getDbType());
        } else if (getLexer().equalToken(Token.GT_EQ)) {
            getLexer().nextToken();
            rightExp = bitOr();
            // rightExp = relationalRest(rightExp);
            expr = new SQLBinaryOpExpr(expr, SQLBinaryOperator.GreaterThanOrEqual, rightExp, getDbType());
        } else if (getLexer().equalToken(Token.BANG_LT)) {
            getLexer().nextToken();
            rightExp = bitOr();
            expr = new SQLBinaryOpExpr(expr, SQLBinaryOperator.NotLessThan, rightExp, getDbType());
        } else if (getLexer().equalToken(Token.BANG_GT)) {
            getLexer().nextToken();
            rightExp = bitOr();
            rightExp = relationalRest(rightExp);
            expr = new SQLBinaryOpExpr(expr, SQLBinaryOperator.NotGreaterThan, rightExp, getDbType());
        } else if (getLexer().equalToken(Token.LT_GT)) {
            getLexer().nextToken();
            rightExp = bitOr();
            expr = new SQLBinaryOpExpr(expr, SQLBinaryOperator.LessThanOrGreater, rightExp, getDbType());
        } else if (getLexer().equalToken(Token.LIKE)) {
            getLexer().nextToken();
            rightExp = bitOr();
            expr = new SQLBinaryOpExpr(expr, SQLBinaryOperator.Like, rightExp, getDbType());
            if (getLexer().equalToken(Token.ESCAPE)) {
                getLexer().nextToken();
                rightExp = primary();
                expr = new SQLBinaryOpExpr(expr, SQLBinaryOperator.Escape, rightExp, getDbType());
            }
        } else if (getLexer().identifierEquals("RLIKE")) {
            getLexer().nextToken();
            rightExp = equality();
            rightExp = relationalRest(rightExp);
            expr = new SQLBinaryOpExpr(expr, SQLBinaryOperator.RLike, rightExp, getDbType());
        } else if (getLexer().equalToken(Token.NOT)) {
            getLexer().nextToken();
            expr = notRationalRest(expr);
        } else if (getLexer().equalToken(Token.BETWEEN)) {
            getLexer().nextToken();
            SQLExpr beginExpr = bitOr();
            accept(Token.AND);
            SQLExpr endExpr = bitOr();
            expr = new SQLBetweenExpr(expr, beginExpr, endExpr);
        } else if (getLexer().equalToken(Token.IS)) {
            getLexer().nextToken();

            if (getLexer().equalToken(Token.NOT)) {
                getLexer().nextToken();
                SQLExpr rightExpr = primary();
                expr = new SQLBinaryOpExpr(expr, SQLBinaryOperator.IsNot, rightExpr, getDbType());
            } else {
                SQLExpr rightExpr = primary();
                expr = new SQLBinaryOpExpr(expr, SQLBinaryOperator.Is, rightExpr, getDbType());
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

            expr = new SQLBinaryOpExpr(expr, SQLBinaryOperator.NotLike, rightExp, getDbType());

            if (getLexer().getToken() == Token.ESCAPE) {
                getLexer().nextToken();
                rightExp = expr();
                expr = new SQLBinaryOpExpr(expr, SQLBinaryOperator.Escape, rightExp, getDbType());
            }
        } else if (getLexer().getToken() == Token.IN) {
            getLexer().nextToken();
            accept(Token.LEFT_PAREN);

            SQLInListExpr inListExpr = new SQLInListExpr(expr, true);
            exprList(inListExpr.getTargetList(), inListExpr);
            expr = inListExpr;

            accept(Token.RIGHT_PAREN);

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
            SQLExpr beginExpr = bitOr();
            accept(Token.AND);
            SQLExpr endExpr = bitOr();

            expr = new SQLBetweenExpr(expr, true, beginExpr, endExpr);

            return expr;
        } else if (getLexer().identifierEquals("RLIKE")) {
            getLexer().nextToken();
            SQLExpr rightExp = primary();

            rightExp = relationalRest(rightExp);

            return new SQLBinaryOpExpr(expr, SQLBinaryOperator.NotRLike, rightExp, getDbType());
        } else {
            throw new ParserUnsupportedException(getLexer().getToken());
        }
        return expr;
    }

    public SQLDataType parseDataType() {

        if (getLexer().equalToken(Token.DEFAULT) || getLexer().equalToken(Token.NOT) || getLexer().equalToken(Token.NULL)) {
            return null;
        }

        SQLName typeExpr = name();
        String typeName = typeExpr.toString();

        if (isCharType(typeName)) {
            SQLCharacterDataType charType = new SQLCharacterDataType(typeName);

            if (getLexer().equalToken(Token.LEFT_PAREN)) {
                getLexer().nextToken();
                SQLExpr arg = this.expr();
                arg.setParent(charType);
                charType.getArguments().add(arg);
                accept(Token.RIGHT_PAREN);
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

    protected SQLDataType parseDataTypeRest(SQLDataType dataType) {
        if (getLexer().equalToken(Token.LEFT_PAREN)) {
            getLexer().nextToken();
            exprList(dataType.getArguments(), dataType);
            accept(Token.RIGHT_PAREN);
        }

        return dataType;
    }

    protected boolean isCharType(String dataTypeName) {
        return "char".equalsIgnoreCase(dataTypeName) //
               || "varchar".equalsIgnoreCase(dataTypeName)
               || "nchar".equalsIgnoreCase(dataTypeName)
               || "nvarchar".equalsIgnoreCase(dataTypeName)
               || "tinytext".equalsIgnoreCase(dataTypeName)
               || "text".equalsIgnoreCase(dataTypeName)
               || "mediumtext".equalsIgnoreCase(dataTypeName)
               || "longtext".equalsIgnoreCase(dataTypeName)
        ;
    }

    protected SQLDataType parseCharTypeRest(SQLCharacterDataType charType) {
        if (getLexer().equalToken(Token.BINARY)) {
            charType.setHasBinary(true);
            getLexer().nextToken();
        }
        
        if (getLexer().identifierEquals("CHARACTER")) {
            getLexer().nextToken();

            accept(Token.SET);

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
    
    @Override
    public void accept(final Token token) {
        if (getLexer().equalToken(token)) {
            getLexer().nextToken();
        } else {
            throw new ParserException(getLexer(), token);
        }
    }

    public SQLColumnDefinition parseColumn() {
        SQLColumnDefinition column = createColumnDefinition();
        column.setName(name());
        
        if(!getLexer().equalToken(Token.SET) && !getLexer().equalToken(Token.DROP)) {
            column.setDataType(parseDataType());
        }
        return parseColumnRest(column);
    }

    protected SQLColumnDefinition createColumnDefinition() {
        return new SQLColumnDefinition();
    }

    public SQLColumnDefinition parseColumnRest(SQLColumnDefinition column) {
        if (getLexer().equalToken(Token.DEFAULT)) {
            getLexer().nextToken();
            column.setDefaultExpr(bitOr());
            return parseColumnRest(column);
        }
        
        if (getLexer().equalToken(Token.NOT)) {
            getLexer().nextToken();
            accept(Token.NULL);
            column.getConstraints().add(new NotNullConstraint());
            return parseColumnRest(column);
        }

        if (getLexer().equalToken(Token.NULL)) {
            getLexer().nextToken();
            column.setDefaultExpr(new SQLNullExpr());
            return parseColumnRest(column);
        }

        if (getLexer().equalToken(Token.PRIMARY)) {
            getLexer().nextToken();
            accept(Token.KEY);
            column.getConstraints().add(new SQLColumnPrimaryKey());
            return parseColumnRest(column);
        }

        if (getLexer().equalToken(Token.UNIQUE)) {
            getLexer().nextToken();
            if (getLexer().equalToken(Token.KEY)) {
                getLexer().nextToken();
            }
            column.getConstraints().add(new SQLColumnUniqueKey());
            return parseColumnRest(column);
        }

        if (getLexer().equalToken(Token.CONSTRAINT)) {
            getLexer().nextToken();

            SQLName name = this.name();

            if (getLexer().equalToken(Token.PRIMARY)) {
                getLexer().nextToken();
                accept(Token.KEY);
                SQLColumnPrimaryKey pk = new SQLColumnPrimaryKey();
                pk.setName(name);
                column.getConstraints().add(pk);
                return parseColumnRest(column);
            }

            if (getLexer().equalToken(Token.UNIQUE)) {
                getLexer().nextToken();
                SQLColumnUniqueKey uk = new SQLColumnUniqueKey();
                uk.setName(name);
                column.getConstraints().add(uk);
                return parseColumnRest(column);
            }

            if (getLexer().equalToken(Token.REFERENCES)) {
                getLexer().nextToken();
                SQLColumnReference ref = new SQLColumnReference();
                ref.setName(name);
                ref.setTable(this.name());
                accept(Token.LEFT_PAREN);
                this.names(ref.getColumns(), ref);
                accept(Token.RIGHT_PAREN);
                column.getConstraints().add(ref);
                return parseColumnRest(column);
            }

            if (getLexer().equalToken(Token.NOT)) {
                getLexer().nextToken();
                accept(Token.NULL);
                NotNullConstraint notNull = new NotNullConstraint();
                notNull.setName(name);
                column.getConstraints().add(notNull);
                return parseColumnRest(column);
            }

            if (getLexer().equalToken(Token.CHECK)) {
                SQLColumnCheck check = parseColumnCheck();
                check.setName(name);
                check.setParent(column);
                column.getConstraints().add(check);
                return parseColumnRest(column);
            }

            if (getLexer().equalToken(Token.DEFAULT)) {
                getLexer().nextToken();
                SQLExpr expr = this.expr();
                column.setDefaultExpr(expr);
                return parseColumnRest(column);
            }
            throw new ParserUnsupportedException(getLexer().getToken());
        }

        if (getLexer().equalToken(Token.CHECK)) {
            SQLColumnCheck check = parseColumnCheck();
            column.getConstraints().add(check);
            return parseColumnRest(column);
        }

        if (getLexer().equalToken(Token.COMMENT)) {
            getLexer().nextToken();
            column.setComment(primary());
        }

        return column;
    }

    protected SQLColumnCheck parseColumnCheck() {
        getLexer().nextToken();
        SQLExpr expr = this.expr();
        SQLColumnCheck check = new SQLColumnCheck(expr);

        if (getLexer().equalToken(Token.DISABLE)) {
            getLexer().nextToken();
            check.setEnable(false);
        } else if (getLexer().equalToken(Token.ENABLE)) {
            getLexer().nextToken();
            check.setEnable(true);
        }
        return check;
    }

    public SQLPrimaryKey parsePrimaryKey() {
        accept(Token.PRIMARY);
        accept(Token.KEY);

        SQLPrimaryKeyImpl pk = new SQLPrimaryKeyImpl();
        accept(Token.LEFT_PAREN);
        exprList(pk.getColumns(), pk);
        accept(Token.RIGHT_PAREN);

        return pk;
    }

    public SQLUnique parseUnique() {
        accept(Token.UNIQUE);

        SQLUnique unique = new SQLUnique();
        accept(Token.LEFT_PAREN);
        exprList(unique.getColumns(), unique);
        accept(Token.RIGHT_PAREN);

        return unique;
    }

    public SQLAssignItem parseAssignItem() {
        SQLAssignItem item = new SQLAssignItem();

        SQLExpr var = primary();

        if (var instanceof SQLIdentifierExpr) {
            var = new SQLVariantRefExpr(((SQLIdentifierExpr) var).getName());
        }
        item.setTarget(var);
        if (getLexer().equalToken(Token.COLON_EQ)) {
            getLexer().nextToken();
        } else {
            accept(Token.EQ);
        }
        
        if(getLexer().equalToken(Token.ON)) {
            item.setValue(new SQLIdentifierExpr(getLexer().getLiterals()));
            getLexer().nextToken();
        } else {
            if (getLexer().equalToken(Token.ALL)) {
                item.setValue(new SQLIdentifierExpr(getLexer().getLiterals()));
                getLexer().nextToken();
            } else {
                item.setValue(expr());
            }
        }
        return item;
    }
    
    public List<SQLCommentHint> parseHints() {
        List<SQLCommentHint> hints = new ArrayList<>();
        parseHints(hints);
        return hints;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void parseHints(List hints) {
        if (getLexer().equalToken(Token.HINT)) {
            hints.add(new SQLCommentHint(getLexer().getLiterals()));
            getLexer().nextToken();
        }
    }

    public SQLConstraint parseConstraint() {
        SQLName name = null;

        if (getLexer().equalToken(Token.CONSTRAINT)) {
            getLexer().nextToken();
            name = this.name();
        }

        SQLConstraint constraint;
        if (getLexer().equalToken(Token.PRIMARY)) {
            constraint = parsePrimaryKey();
        } else if (getLexer().equalToken(Token.UNIQUE)) {
            constraint = parseUnique();
        } else if (getLexer().equalToken(Token.FOREIGN)) {
            constraint = parseForeignKey();
        } else if (getLexer().equalToken(Token.CHECK)) {
            constraint = parseCheck();
        } else {
            throw new ParserUnsupportedException(getLexer().getToken());
        }
        constraint.setName(name);
        return constraint;
    }

    public SQLCheck parseCheck() {
        accept(Token.CHECK);
        SQLCheck check = createCheck();
        accept(Token.LEFT_PAREN);
        check.setExpr(this.expr());
        accept(Token.RIGHT_PAREN);
        return check;
    }

    protected SQLCheck createCheck() {
        return new SQLCheck();
    }

    public SQLForeignKeyConstraint parseForeignKey() {
        accept(Token.FOREIGN);
        accept(Token.KEY);

        SQLForeignKeyConstraint fk = createForeignKey();

        accept(Token.LEFT_PAREN);
        this.names(fk.getReferencingColumns());
        accept(Token.RIGHT_PAREN);

        accept(Token.REFERENCES);

        fk.setReferencedTableName(this.name());

        if (getLexer().equalToken(Token.LEFT_PAREN)) {
            getLexer().nextToken();
            this.names(fk.getReferencedColumns(), fk);
            accept(Token.RIGHT_PAREN);
        }

        return fk;
    }

    protected SQLForeignKeyConstraint createForeignKey() {
        return new SQLForeignKeyImpl();
    }
    
    public SQLSelectItem parseSelectItem() {
        SQLExpr expr;
        boolean connectByRoot = false;
        if (getLexer().equalToken(Token.IDENTIFIER)) {
            if (getLexer().identifierEquals("CONNECT_BY_ROOT")) {
                connectByRoot = true;
                getLexer().nextToken();
            }
            expr = new SQLIdentifierExpr(getLexer().getLiterals());
            getLexer().nextTokenCommaOrRightParen();

            if (!getLexer().equalToken(Token.COMMA)) {
                expr = this.primaryRest(expr);
                expr = this.exprRest(expr);
            }
        } else {
            expr = expr();
        }
        final String alias = as();

        return new SQLSelectItem(expr, alias, connectByRoot);
    }
}
