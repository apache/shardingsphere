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

package com.alibaba.druid.sql.dialect.mysql.parser;

import com.alibaba.druid.sql.ast.SQLDataType;
import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.SQLName;
import com.alibaba.druid.sql.ast.expr.SQLAggregateExpr;
import com.alibaba.druid.sql.ast.expr.SQLBinaryExpr;
import com.alibaba.druid.sql.ast.expr.SQLBinaryOpExpr;
import com.alibaba.druid.sql.ast.expr.SQLBinaryOperator;
import com.alibaba.druid.sql.ast.expr.SQLCharExpr;
import com.alibaba.druid.sql.ast.expr.SQLHexExpr;
import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.expr.SQLIntegerExpr;
import com.alibaba.druid.sql.ast.expr.SQLMethodInvokeExpr;
import com.alibaba.druid.sql.ast.expr.SQLNumericLiteralExpr;
import com.alibaba.druid.sql.ast.expr.SQLUnaryExpr;
import com.alibaba.druid.sql.ast.expr.SQLUnaryOperator;
import com.alibaba.druid.sql.ast.expr.SQLVariantRefExpr;
import com.alibaba.druid.sql.context.LimitContext;
import com.alibaba.druid.sql.context.OffsetLimitToken;
import com.alibaba.druid.sql.context.RowCountLimitToken;
import com.alibaba.druid.sql.context.SelectSQLContext;
import com.alibaba.druid.sql.dialect.mysql.ast.expr.MySqlCharExpr;
import com.alibaba.druid.sql.dialect.mysql.ast.expr.MySqlExtractExpr;
import com.alibaba.druid.sql.dialect.mysql.ast.expr.MySqlIntervalExpr;
import com.alibaba.druid.sql.dialect.mysql.ast.expr.MySqlIntervalUnit;
import com.alibaba.druid.sql.dialect.mysql.ast.expr.MySqlMatchAgainstExpr;
import com.alibaba.druid.sql.dialect.mysql.ast.expr.MySqlMatchAgainstExpr.SearchModifier;
import com.alibaba.druid.sql.dialect.mysql.ast.expr.MySqlUserName;
import com.alibaba.druid.sql.lexer.Token;
import com.alibaba.druid.sql.parser.ParserException;
import com.alibaba.druid.sql.parser.ParserUnsupportedException;
import com.alibaba.druid.sql.parser.SQLExprParser;
import com.alibaba.druid.sql.parser.SQLSelectParser;
import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.exception.SQLParserException;

import java.util.List;

public class MySqlExprParser extends SQLExprParser {
    
    private static final String[] AGGREGATE_FUNCTIONS = {"MAX", "MIN", "COUNT", "SUM", "AVG", "STDDEV", "GROUP_CONCAT"};
    
    public MySqlExprParser(final ShardingRule shardingRule, final List<Object> parameters, final String sql) {
        super(shardingRule, parameters, new MySqlLexer(sql), AGGREGATE_FUNCTIONS);
        getLexer().nextToken();
    }
    
    public final void skipPartition() {
        if (getLexer().skipIfEqual(Token.PARTITION)) {
            getLexer().skipParentheses();
        }
    }
    
    public SQLExpr relationalRest(SQLExpr expr) {
        if (getLexer().identifierEquals("REGEXP")) {
            getLexer().nextToken();
            SQLExpr rightExp = equality();

            rightExp = relationalRest(rightExp);

            return new SQLBinaryOpExpr(expr, SQLBinaryOperator.RegExp, rightExp);
        }

        return super.relationalRest(expr);
    }
    
    public SQLExpr multiplicativeRest(SQLExpr expr) {
        if (getLexer().equalToken(Token.IDENTIFIER)  && "MOD".equalsIgnoreCase(getLexer().getLiterals())) {
            getLexer().nextToken();
            SQLExpr rightExp = primary();

            rightExp = relationalRest(rightExp);

            return new SQLBinaryOpExpr(expr, SQLBinaryOperator.Modulus, rightExp);
        }

        return super.multiplicativeRest(expr);
    }

    public SQLExpr notRationalRest(SQLExpr expr) {
        if (getLexer().identifierEquals("REGEXP")) {
            getLexer().nextToken();
            SQLExpr rightExp = primary();

            rightExp = relationalRest(rightExp);

            return new SQLBinaryOpExpr(expr, SQLBinaryOperator.NotRegExp, rightExp);
        }

        return super.notRationalRest(expr);
    }

    public SQLExpr primary() {
        final Token tok = getLexer().getToken();

        if (getLexer().identifierEquals("outfile")) {
            getLexer().nextToken();
            SQLExpr file = primary();
            return primaryRest(file);

        }

        switch (tok) {
            case LITERAL_ALIAS:
                String aliasValue = getLexer().getLiterals();
                getLexer().nextToken();
                return primaryRest(new SQLCharExpr(aliasValue));
            case VARIANT:
                SQLVariantRefExpr varRefExpr = new SQLVariantRefExpr(getLexer().getLiterals());
                getLexer().nextToken();
                if (varRefExpr.getName().equalsIgnoreCase("@@global")) {
                    getLexer().accept(Token.DOT);
                    varRefExpr = new SQLVariantRefExpr(getLexer().getLiterals(), true);
                    getLexer().nextToken();
                } else if (varRefExpr.getName().equals("@") && getLexer().equalToken(Token.LITERAL_CHARS)) {
                    varRefExpr.setName("@'" + getLexer().getLiterals() + "'");
                    getLexer().nextToken();
                } else if (varRefExpr.getName().equals("@@") && getLexer().equalToken(Token.LITERAL_CHARS)) {
                    varRefExpr.setName("@@'" + getLexer().getLiterals() + "'");
                    getLexer().nextToken();
                }
                return primaryRest(varRefExpr);
            case VALUES:
                getLexer().nextToken();
                if (!getLexer().equalToken(Token.LEFT_PAREN)) {
                    throw new ParserException("syntax error, illegal values clause");
                }
                return this.methodRest(new SQLIdentifierExpr("VALUES"), true);
            case BINARY:
                getLexer().nextToken();
                if (getLexer().equalToken(Token.COMMA) || getLexer().equalToken(Token.SEMI) || getLexer().equalToken(Token.EOF)) {
                    return new SQLIdentifierExpr("BINARY");
                } else {
                    SQLUnaryExpr binaryExpr = new SQLUnaryExpr(SQLUnaryOperator.BINARY, expr());
                    return primaryRest(binaryExpr);
                }
            case CACHE:
            case GROUP:
                getLexer().nextToken();
                return primaryRest(new SQLIdentifierExpr(getLexer().getLiterals()));
            default:
                return super.primary();
        }

    }

    public final SQLExpr primaryRest(SQLExpr expr) {
        if (expr == null) {
            throw new IllegalArgumentException("expr");
        }

        if (getLexer().equalToken(Token.LITERAL_CHARS)) {
            if (expr instanceof SQLIdentifierExpr) {
                SQLIdentifierExpr identExpr = (SQLIdentifierExpr) expr;
                String ident = identExpr.getSimpleName();

                if (ident.equalsIgnoreCase("x")) {
                    String charValue = getLexer().getLiterals();
                    getLexer().nextToken();
                    expr = new SQLHexExpr(charValue);

                    return primaryRest(expr);
                } else if (ident.equalsIgnoreCase("b")) {
                    String charValue = getLexer().getLiterals();
                    getLexer().nextToken();
                    expr = new SQLBinaryExpr(charValue);

                    return primaryRest(expr);
                } else if (ident.startsWith("_")) {
                    String charValue = getLexer().getLiterals();
                    getLexer().nextToken();

                    MySqlCharExpr mysqlCharExpr = new MySqlCharExpr(charValue);
                    mysqlCharExpr.setCharset(identExpr.getSimpleName());
                    if (getLexer().identifierEquals("COLLATE")) {
                        getLexer().nextToken();

                        String collate = getLexer().getLiterals();
                        mysqlCharExpr.setCollate(collate);
                        getLexer().accept(Token.IDENTIFIER);
                    }

                    expr = mysqlCharExpr;

                    return primaryRest(expr);
                }
            } else if (expr instanceof SQLCharExpr) {
                SQLMethodInvokeExpr concat = new SQLMethodInvokeExpr("CONCAT");
                concat.addParameter(expr);
                do {
                    String chars = getLexer().getLiterals();
                    concat.addParameter(new SQLCharExpr(chars));
                    getLexer().nextToken();
                } while (getLexer().equalToken(Token.LITERAL_CHARS) || getLexer().equalToken(Token.LITERAL_ALIAS));
                expr = concat;
            }
        } else if (getLexer().equalToken(Token.IDENTIFIER)) {
            if (expr instanceof SQLHexExpr) {
                if (getLexer().skipIfEqual(Token.USING)) {
                    if (!getLexer().equalToken(Token.IDENTIFIER)) {
                        throw new ParserException("syntax error, illegal hex");
                    }
                    String charSet = getLexer().getLiterals();
                    getLexer().nextToken();
                    expr.getAttributes().put("USING", charSet);

                    return primaryRest(expr);
                }
            } else if ("COLLATE".equalsIgnoreCase(getLexer().getLiterals())) {
                getLexer().nextToken();

                if (getLexer().equalToken(Token.EQ)) {
                    getLexer().nextToken();
                }

                if (!getLexer().equalToken(Token.IDENTIFIER)) {
                    throw new ParserException(getLexer());
                }
                String collate = getLexer().getLiterals();
                getLexer().nextToken();
                expr = new SQLBinaryOpExpr(expr, SQLBinaryOperator.COLLATE, new SQLIdentifierExpr(collate));
                return primaryRest(expr);
            } else if (expr instanceof SQLVariantRefExpr) {
                if ("COLLATE".equalsIgnoreCase(getLexer().getLiterals())) {
                    getLexer().nextToken();

                    if (!getLexer().equalToken(Token.IDENTIFIER)) {
                        throw new ParserException(getLexer());
                    }

                    String collate = getLexer().getLiterals();
                    getLexer().nextToken();

                    expr.putAttribute("COLLATE", collate);

                    return primaryRest(expr);
                }
            } else if (expr instanceof SQLIntegerExpr) {
                SQLIntegerExpr intExpr = (SQLIntegerExpr) expr;
                String binaryString = getLexer().getLiterals();
                if (intExpr.getNumber().intValue() == 0 && binaryString.startsWith("b")) {
                    getLexer().nextToken();
                    expr = new SQLBinaryExpr(binaryString.substring(1));

                    return primaryRest(expr);
                }
            }
        }

        if (getLexer().equalToken(Token.LEFT_PAREN) && expr instanceof SQLIdentifierExpr) {
            SQLIdentifierExpr identExpr = (SQLIdentifierExpr) expr;
            String ident = identExpr.getSimpleName();

            if ("EXTRACT".equalsIgnoreCase(ident)) {
                getLexer().nextToken();

                if (!getLexer().equalToken(Token.IDENTIFIER)) {
                    throw new ParserException(getLexer());
                }

                String unitVal = getLexer().getLiterals();
                MySqlIntervalUnit unit = MySqlIntervalUnit.valueOf(unitVal.toUpperCase());
                getLexer().nextToken();
    
                getLexer().accept(Token.FROM);

                SQLExpr value = expr();

                MySqlExtractExpr extract = new MySqlExtractExpr();
                extract.setValue(value);
                extract.setUnit(unit);
                getLexer().accept(Token.RIGHT_PAREN);

                expr = extract;

                return primaryRest(expr);
            } else if ("SUBSTRING".equalsIgnoreCase(ident)) {
                getLexer().nextToken();
                SQLMethodInvokeExpr methodInvokeExpr = new SQLMethodInvokeExpr(ident);
                while (true) {
                    SQLExpr param = expr();
                    methodInvokeExpr.addParameter(param);

                    if (getLexer().equalToken(Token.COMMA)) {
                        getLexer().nextToken();
                    } else if (getLexer().equalToken(Token.FROM)) {
                        getLexer().nextToken();
                        SQLExpr from = expr();
                        methodInvokeExpr.addParameter(from);

                        if (getLexer().equalToken(Token.FOR)) {
                            getLexer().nextToken();
                            SQLExpr forExpr = expr();
                            methodInvokeExpr.addParameter(forExpr);
                        }
                        break;
                    } else if (getLexer().equalToken(Token.RIGHT_PAREN)) {
                        break;
                    } else {
                        throw new ParserException(getLexer());
                    }
                }
    
                getLexer().accept(Token.RIGHT_PAREN);
                expr = methodInvokeExpr;

                return primaryRest(expr);
            } else if ("TRIM".equalsIgnoreCase(ident)) {
                getLexer().nextToken();
                SQLMethodInvokeExpr methodInvokeExpr = new SQLMethodInvokeExpr(ident);

                if (getLexer().equalToken(Token.IDENTIFIER)) {
                    String flagVal = getLexer().getLiterals();
                    if ("LEADING".equalsIgnoreCase(flagVal)) {
                        getLexer().nextToken();
                        methodInvokeExpr.getAttributes().put("TRIM_TYPE", "LEADING");
                    } else if ("BOTH".equalsIgnoreCase(flagVal)) {
                        getLexer().nextToken();
                        methodInvokeExpr.getAttributes().put("TRIM_TYPE", "BOTH");
                    } else if ("TRAILING".equalsIgnoreCase(flagVal)) {
                        getLexer().nextToken();
                        methodInvokeExpr.putAttribute("TRIM_TYPE", "TRAILING");
                    }
                }

                SQLExpr param = expr();
                methodInvokeExpr.addParameter(param);

                if (getLexer().equalToken(Token.FROM)) {
                    getLexer().nextToken();
                    SQLExpr from = expr();
                    methodInvokeExpr.putAttribute("FROM", from);
                }
    
                getLexer().accept(Token.RIGHT_PAREN);
                expr = methodInvokeExpr;

                return primaryRest(expr);
            } else if ("MATCH".equalsIgnoreCase(ident)) {
                getLexer().nextToken();
                MySqlMatchAgainstExpr matchAgainstExpr = new MySqlMatchAgainstExpr();
                if (getLexer().equalToken(Token.RIGHT_PAREN)) {
                    getLexer().nextToken();
                } else {
                    matchAgainstExpr.getColumns().addAll(exprList(matchAgainstExpr));
                    getLexer().accept(Token.RIGHT_PAREN);
                }
    
                getLexer().accept("AGAINST");
    
                getLexer().accept(Token.LEFT_PAREN);
                SQLExpr against = primary();
                matchAgainstExpr.setAgainst(against);

                if (getLexer().equalToken(Token.IN)) {
                    getLexer().nextToken();
                    if (getLexer().identifierEquals("NATURAL")) {
                        getLexer().nextToken();
                        getLexer().accept("LANGUAGE");
                        getLexer().accept("MODE");
                        if (getLexer().equalToken(Token.WITH)) {
                            getLexer().nextToken();
                            getLexer().accept("QUERY");
                            getLexer().accept("EXPANSION");
                            matchAgainstExpr.setSearchModifier(SearchModifier.IN_NATURAL_LANGUAGE_MODE_WITH_QUERY_EXPANSION);
                        } else {
                            matchAgainstExpr.setSearchModifier(SearchModifier.IN_NATURAL_LANGUAGE_MODE);
                        }
                    } else if (getLexer().identifierEquals("BOOLEAN")) {
                        getLexer().nextToken();
                        getLexer().accept("MODE");
                        matchAgainstExpr.setSearchModifier(SearchModifier.IN_BOOLEAN_MODE);
                    } else {
                        throw new ParserUnsupportedException(getLexer().getToken());
                    }
                } else if (getLexer().equalToken(Token.WITH)) {
                    throw new ParserUnsupportedException(getLexer().getToken());
                }
    
                getLexer().accept(Token.RIGHT_PAREN);

                expr = matchAgainstExpr;

                return primaryRest(expr);
            } else if (("CONVERT".equalsIgnoreCase(ident)) || ("CHAR".equalsIgnoreCase(ident))) {
                getLexer().nextToken();
                SQLMethodInvokeExpr methodInvokeExpr = new SQLMethodInvokeExpr(ident);
                if (!getLexer().equalToken(Token.RIGHT_PAREN)) {
                    methodInvokeExpr.getParameters().addAll(exprList(methodInvokeExpr));
                }
                if (getLexer().skipIfEqual(Token.USING)) {
                    if (!getLexer().equalToken(Token.IDENTIFIER)) {
                        throw new ParserException(getLexer());
                    }
                    String charset = getLexer().getLiterals();
                    getLexer().nextToken();
                    methodInvokeExpr.putAttribute("USING", charset);
                }
    
                getLexer().accept(Token.RIGHT_PAREN);

                expr = methodInvokeExpr;

                return primaryRest(expr);
            } else if ("POSITION".equalsIgnoreCase(ident)) {
                getLexer().accept(Token.LEFT_PAREN);
                SQLExpr subStr = this.primary();
                getLexer().accept(Token.IN);
                SQLExpr str = this.expr();
                getLexer().accept(Token.RIGHT_PAREN);

                SQLMethodInvokeExpr locate = new SQLMethodInvokeExpr("LOCATE");
                locate.addParameter(subStr);
                locate.addParameter(str);

                expr = locate;
                return primaryRest(expr);
            }
        }

        if (getLexer().equalToken(Token.VARIANT) && "@".equals(getLexer().getLiterals())) {
            getLexer().nextToken();
            MySqlUserName userName = new MySqlUserName();
            if (expr instanceof SQLCharExpr) {
                userName.setUserName(expr.toString());
            } else {
                userName.setUserName(((SQLIdentifierExpr) expr).getSimpleName());
            }

            if (getLexer().equalToken(Token.LITERAL_CHARS)) {
                userName.setHost("'" + getLexer().getLiterals() + "'");
            } else {
                userName.setHost(getLexer().getLiterals());
            }
            getLexer().nextToken();
            return userName;
        }

        if (getLexer().equalToken(Token.ERROR)) {
            throw new ParserException(getLexer());
        }

        return super.primaryRest(expr);
    }
    
    @Override
    public SQLSelectParser createSelectParser(final ShardingRule shardingRule, final List<Object> parameters) {
        return new MySqlSelectParser(shardingRule, parameters, this);
    }

    protected SQLExpr parseInterval() {
        getLexer().accept(Token.INTERVAL);

        if (getLexer().equalToken(Token.LEFT_PAREN)) {
            getLexer().nextToken();
            SQLMethodInvokeExpr methodInvokeExpr = new SQLMethodInvokeExpr("INTERVAL");
            if (!getLexer().equalToken(Token.RIGHT_PAREN)) {
                methodInvokeExpr.getParameters().addAll(exprList(methodInvokeExpr));
            }
            getLexer().accept(Token.RIGHT_PAREN);
            return primaryRest(methodInvokeExpr);
        } else {
            SQLExpr value = expr();

            if (!getLexer().equalToken(Token.IDENTIFIER)) {
                throw new ParserException(getLexer());
            }

            String unit = getLexer().getLiterals();
            getLexer().nextToken();

            MySqlIntervalExpr intervalExpr = new MySqlIntervalExpr();
            intervalExpr.setValue(value);
            intervalExpr.setUnit(MySqlIntervalUnit.valueOf(unit.toUpperCase()));

            return intervalExpr;
        }
    }

    protected SQLDataType parseDataTypeRest(SQLDataType dataType) {
        super.parseDataTypeRest(dataType);
        if (getLexer().identifierEquals("UNSIGNED")) {
            getLexer().nextToken();
            dataType.getAttributes().put("UNSIGNED", true);
        }
        if (getLexer().identifierEquals("ZEROFILL")) {
            getLexer().nextToken();
            dataType.getAttributes().put("ZEROFILL", true);
        }
        return dataType;
    }

    public SQLExpr orRest(SQLExpr expr) {

        while (true) {
            if (getLexer().equalToken(Token.OR) || getLexer().equalToken(Token.DOUBLE_BAR)) {
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

    public SQLExpr additiveRest(SQLExpr expr) {
        if (getLexer().equalToken(Token.PLUS)) {
            getLexer().nextToken();
            SQLExpr rightExp = multiplicativeRest(bitXorRest(primary()));

            expr = new SQLBinaryOpExpr(expr, SQLBinaryOperator.Add, rightExp);
            expr = additiveRest(expr);
        } else if (getLexer().equalToken(Token.SUB)) {
            getLexer().nextToken();
            SQLExpr rightExp = multiplicativeRest(bitXorRest(primary()));
            expr = new SQLBinaryOpExpr(expr, SQLBinaryOperator.Subtract, rightExp);
            expr = additiveRest(expr);
        }

        return expr;
    }

    public SQLName nameRest(final SQLName name) {
        if (getLexer().equalToken(Token.VARIANT) && "@".equals(getLexer().getLiterals())) {
            getLexer().nextToken();
            MySqlUserName userName = new MySqlUserName();
            userName.setUserName(name.getSimpleName());

            if (getLexer().equalToken(Token.LITERAL_CHARS)) {
                userName.setHost("'" + getLexer().getLiterals() + "'");
            } else {
                userName.setHost(getLexer().getLiterals());
            }
            getLexer().nextToken();
            return userName;
        }
        return super.nameRest(name);
    }
    
    public LimitContext parseLimit(final int parametersIndex, final SelectSQLContext sqlContext) {
        getLexer().skipIfEqual(Token.LIMIT);
        int valueIndex = -1;
        int valueBeginPosition = getLexer().getCurrentPosition();
        SQLExpr expr = expr();
        int value;
        boolean isParameterForValue = false;
        if (expr instanceof SQLNumericLiteralExpr) {
            value = ((SQLNumericLiteralExpr) expr).getNumber().intValue();
            valueBeginPosition = valueBeginPosition - (value + "").length();
        } else {
            valueIndex = parametersIndex;
            value = (int) getParameters().get(valueIndex);
            valueBeginPosition--;
            isParameterForValue = true;
        }
        if (getLexer().skipIfEqual(Token.COMMA)) {
            int rowCountBeginPosition = getLexer().getCurrentPosition();
            SQLExpr rowCountExpr = expr();
            int rowCount;
            int rowCountIndex = -1;
            boolean isParameterForRowCount = false;
            if (rowCountExpr instanceof SQLNumericLiteralExpr) {
                rowCount = ((SQLNumericLiteralExpr) rowCountExpr).getNumber().intValue();
                rowCountBeginPosition = rowCountBeginPosition - (rowCount + "").length();
            } else {
                rowCountIndex = -1 == valueIndex ? parametersIndex : valueIndex + 1;
                rowCount = (int) getParameters().get(rowCountIndex);
                rowCountBeginPosition--;
                isParameterForRowCount = true;
            }
            if (!isParameterForValue) {
                sqlContext.getSqlTokens().add(new OffsetLimitToken(valueBeginPosition, value));
            }
            if (!isParameterForRowCount) {
                sqlContext.getSqlTokens().add(new RowCountLimitToken(rowCountBeginPosition, rowCount));
            }
            if (value < 0 || rowCount < 0) {
                throw new SQLParserException("LIMIT offset and row count can not be a negative value");
            }
            return new LimitContext(value, rowCount, valueIndex, rowCountIndex);
        }
        if (getLexer().skipIfEqual(Token.OFFSET)) {
            int offsetBeginPosition = getLexer().getCurrentPosition();
            SQLExpr offsetExpr = expr();
            int offset;
            int offsetIndex = -1;
            boolean isParameterForOffset = false;
            if (offsetExpr instanceof SQLNumericLiteralExpr) {
                offset = ((SQLNumericLiteralExpr) offsetExpr).getNumber().intValue();
                offsetBeginPosition = offsetBeginPosition - (offset + "").length();
            } else {
                offsetIndex = -1 == valueIndex ? parametersIndex : valueIndex + 1;
                offset = (int) getParameters().get(offsetIndex);
                offsetBeginPosition--;
                isParameterForOffset = true;
            }
            if (!isParameterForOffset) {
                sqlContext.getSqlTokens().add(new OffsetLimitToken(offsetBeginPosition, offset));
            }
            if (!isParameterForValue) {
                sqlContext.getSqlTokens().add(new RowCountLimitToken(valueBeginPosition, value)); 
            }
            if (value < 0 || offset < 0) {
                throw new SQLParserException("LIMIT offset and row count can not be a negative value");
            }
            return new LimitContext(offset, value, offsetIndex, valueIndex);
        }
        if (!isParameterForValue) {
            sqlContext.getSqlTokens().add(new RowCountLimitToken(valueBeginPosition, value));
        }
        if (value < 0) {
            throw new SQLParserException("LIMIT offset and row count can not be a negative value");
        }
        return new LimitContext(value, valueIndex);
    }
    
    protected SQLAggregateExpr parseAggregateExprRest(final SQLAggregateExpr aggregateExpr) {
        if (getLexer().equalToken(Token.ORDER)) {
            aggregateExpr.putAttribute("ORDER BY", parseOrderBy());
        }
        if (getLexer().identifierEquals("SEPARATOR")) {
            getLexer().nextToken();
            aggregateExpr.putAttribute("SEPARATOR", primary());
        }
        return aggregateExpr;
    }
}
