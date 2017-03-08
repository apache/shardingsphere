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

package com.alibaba.druid.sql.dialect.postgresql.parser;

import com.alibaba.druid.sql.ast.SQLDataType;
import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.expr.SQLArrayExpr;
import com.alibaba.druid.sql.ast.expr.SQLBinaryExpr;
import com.alibaba.druid.sql.ast.expr.SQLCharExpr;
import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.expr.SQLTimestampExpr;
import com.alibaba.druid.sql.ast.expr.SQLUnaryExpr;
import com.alibaba.druid.sql.ast.expr.SQLUnaryOperator;
import com.alibaba.druid.sql.ast.expr.SQLVariantRefExpr;
import com.alibaba.druid.sql.dialect.postgresql.ast.expr.PGBoxExpr;
import com.alibaba.druid.sql.dialect.postgresql.ast.expr.PGCidrExpr;
import com.alibaba.druid.sql.dialect.postgresql.ast.expr.PGCircleExpr;
import com.alibaba.druid.sql.dialect.postgresql.ast.expr.PGDateField;
import com.alibaba.druid.sql.dialect.postgresql.ast.expr.PGExtractExpr;
import com.alibaba.druid.sql.dialect.postgresql.ast.expr.PGInetExpr;
import com.alibaba.druid.sql.dialect.postgresql.ast.expr.PGIntervalExpr;
import com.alibaba.druid.sql.dialect.postgresql.ast.expr.PGLineSegmentsExpr;
import com.alibaba.druid.sql.dialect.postgresql.ast.expr.PGMacAddrExpr;
import com.alibaba.druid.sql.dialect.postgresql.ast.expr.PGPointExpr;
import com.alibaba.druid.sql.dialect.postgresql.ast.expr.PGPolygonExpr;
import com.alibaba.druid.sql.dialect.postgresql.ast.expr.PGTypeCastExpr;
import com.alibaba.druid.sql.lexer.Token;
import com.alibaba.druid.sql.parser.SQLExprParser;
import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;

import java.util.List;

public class PGExprParser extends SQLExprParser {
    
    private static final String[] AGGREGATE_FUNCTIONS = {"MAX", "MIN", "COUNT", "SUM", "AVG", "STDDEV", "ROW_NUMBER"};
    
    public PGExprParser(final ShardingRule shardingRule, final List<Object> parameters, final String sql) {
        super(shardingRule, parameters, new PGLexer(sql), AGGREGATE_FUNCTIONS);
        getLexer().nextToken();
    }
    
    @Override
    public SQLDataType parseDataType() {
        getLexer().skipIfEqual(Token.TYPE);
        return super.parseDataType();
    }

    public SQLExpr primary() {
        if (getLexer().equalToken(Token.ARRAY)) {
            SQLArrayExpr array = new SQLArrayExpr();
            array.setExpr(new SQLIdentifierExpr(getLexer().getLiterals()));
            getLexer().nextToken();
            getLexer().accept(Token.LEFT_BRACKET);
            array.getValues().addAll(exprList(array));
            getLexer().accept(Token.RIGHT_BRACKET);
            return primaryRest(array);
        } else if (getLexer().equalToken(Token.POUND)) {
            getLexer().nextToken();
            if (getLexer().equalToken(Token.LEFT_BRACE)) {
                getLexer().nextToken();
                String varName = getLexer().getLiterals();
                getLexer().nextToken();
                getLexer().accept(Token.RIGHT_BRACE);
                SQLVariantRefExpr expr = new SQLVariantRefExpr("#{" + varName + "}");
                return primaryRest(expr);
            } else {
                SQLExpr value = this.primary();
                SQLUnaryExpr expr = new SQLUnaryExpr(SQLUnaryOperator.Pound, value);
                return primaryRest(expr);
            }
        }
        
        return super.primary();
    }
    
    @Override
    protected SQLExpr parseInterval() {
        getLexer().accept(Token.INTERVAL);
        PGIntervalExpr intervalExpr = new PGIntervalExpr();
        if (!getLexer().equalToken(Token.LITERAL_CHARS)) {
            return new SQLIdentifierExpr("INTERVAL");
        }
        intervalExpr.setValue(new SQLCharExpr(getLexer().getLiterals()));
        getLexer().nextToken();
        return intervalExpr;
    }
    
    public SQLExpr primaryRest(SQLExpr expr) {
        if (getLexer().equalToken(Token.DOUBLE_COLON)) {
            getLexer().nextToken();
            SQLDataType dataType = this.parseDataType();
            
            PGTypeCastExpr castExpr = new PGTypeCastExpr();
            
            castExpr.setExpr(expr);
            castExpr.setDataType(dataType);

            return primaryRest(castExpr);
        }
        
        if (getLexer().equalToken(Token.LEFT_BRACKET)) {
            SQLArrayExpr array = new SQLArrayExpr();
            array.setExpr(expr);
            getLexer().nextToken();
            array.getValues().addAll(exprList(array));
            getLexer().accept(Token.RIGHT_BRACKET);
            return primaryRest(array);
        }
        
        if (expr.getClass() == SQLIdentifierExpr.class) {
            String ident = ((SQLIdentifierExpr) expr).getSimpleName();
            if ("TIMESTAMP".equalsIgnoreCase(ident)) {
                if (!getLexer().equalToken(Token.LITERAL_ALIAS)
                        && !getLexer().equalToken(Token.LITERAL_CHARS)
                        && !getLexer().equalToken(Token.WITH)) {
                    return new SQLIdentifierExpr("TIMESTAMP");
                }

                SQLTimestampExpr timestamp = new SQLTimestampExpr();
                
                if (getLexer().equalToken(Token.WITH)) {
                    getLexer().nextToken();
                    getLexer().accept("TIME");
                    getLexer().accept("ZONE");
                    timestamp.setWithTimeZone(true);
                }

                String literal = getLexer().getLiterals();
                timestamp.setLiteral(literal);
                getLexer().accept(Token.LITERAL_CHARS);

                if (getLexer().identifierEquals("AT")) {
                    getLexer().nextToken();
                    getLexer().accept("TIME");
                    getLexer().accept("ZONE");

                    String timezone = getLexer().getLiterals();
                    timestamp.setTimeZone(timezone);
                    getLexer().accept(Token.LITERAL_CHARS);
                }

                
                return primaryRest(timestamp);     
            } else if ("EXTRACT".equalsIgnoreCase(ident)) {
                getLexer().accept(Token.LEFT_PAREN);
                
                PGExtractExpr extract = new PGExtractExpr();
                
                String fieldName = getLexer().getLiterals();
                PGDateField field = PGDateField.valueOf(fieldName.toUpperCase());
                getLexer().nextToken();
                
                extract.setField(field);
    
                getLexer().accept(Token.FROM);
                SQLExpr source = this.expr();
                
                extract.setSource(source);
    
                getLexer().accept(Token.RIGHT_PAREN);
                
                return primaryRest(extract);     
            } else if ("POINT".equalsIgnoreCase(ident)) {
                PGPointExpr point = new PGPointExpr(primary());
                return primaryRest(point);
            } else if ("BOX".equalsIgnoreCase(ident)) {
                SQLExpr value = this.primary();
                PGBoxExpr box = new PGBoxExpr(value);
                return primaryRest(box);
            } else if ("macaddr".equalsIgnoreCase(ident)) {
                PGMacAddrExpr macaddr = new PGMacAddrExpr(primary());
                return primaryRest(macaddr);
            } else if ("inet".equalsIgnoreCase(ident)) {
                PGInetExpr inet = new PGInetExpr(primary());
                return primaryRest(inet);
            } else if ("cidr".equalsIgnoreCase(ident)) {
                PGCidrExpr cidr = new PGCidrExpr(primary());
                return primaryRest(cidr);
            } else if ("polygon".equalsIgnoreCase(ident)) {
                PGPolygonExpr polygon = new PGPolygonExpr(primary());
                return primaryRest(polygon);
            } else if ("circle".equalsIgnoreCase(ident)) {
                PGCircleExpr circle = new PGCircleExpr(primary());
                return primaryRest(circle);
            } else if ("lseg".equalsIgnoreCase(ident)) {
                PGLineSegmentsExpr lseg = new PGLineSegmentsExpr(primary());
                return primaryRest(lseg);
            } else if ("b".equalsIgnoreCase(ident) && getLexer().equalToken(Token.LITERAL_CHARS)) {
                String charValue = getLexer().getLiterals();
                getLexer().nextToken();
                expr = new SQLBinaryExpr(charValue);

                return primaryRest(expr);
            }
        }
        return super.primaryRest(expr);
    }
}
