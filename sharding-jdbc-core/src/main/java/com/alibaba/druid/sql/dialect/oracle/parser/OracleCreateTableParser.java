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
package com.alibaba.druid.sql.dialect.oracle.parser;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.SQLName;
import com.alibaba.druid.sql.dialect.oracle.ast.clause.OracleLobStorageClause;
import com.alibaba.druid.sql.dialect.oracle.ast.clause.OraclePartitionByRangeClause;
import com.alibaba.druid.sql.dialect.oracle.ast.clause.OracleRangeValuesClause;
import com.alibaba.druid.sql.dialect.oracle.ast.clause.OracleStorageClause;
import com.alibaba.druid.sql.dialect.oracle.ast.stmt.OracleCreateTableStatement;
import com.alibaba.druid.sql.dialect.oracle.ast.stmt.OracleCreateTableStatement.DeferredSegmentCreation;
import com.alibaba.druid.sql.dialect.oracle.ast.stmt.OracleSelect;
import com.alibaba.druid.sql.lexer.Lexer;
import com.alibaba.druid.sql.lexer.Token;
import com.alibaba.druid.sql.parser.ParserUnsupportedException;
import com.alibaba.druid.sql.parser.SQLCreateTableParser;

public class OracleCreateTableParser extends SQLCreateTableParser {

    public OracleCreateTableParser(Lexer lexer){
        super(new OracleExprParser(lexer));
    }

    protected OracleCreateTableStatement newCreateStatement() {
        return new OracleCreateTableStatement();
    }

    public OracleCreateTableStatement parseCrateTable(boolean acceptCreate) {
        OracleCreateTableStatement stmt = (OracleCreateTableStatement) super.parseCrateTable(acceptCreate);

        while (true) {
            if (getLexer().equalToken(Token.TABLESPACE)) {
                getLexer().nextToken();
                stmt.setTablespace(this.exprParser.name());
                continue;
            } else if (getLexer().identifierEquals("IN_MEMORY_METADATA")) {
                getLexer().nextToken();
                stmt.setInMemoryMetadata(true);
                continue;
            } else if (getLexer().identifierEquals("CURSOR_SPECIFIC_SEGMENT")) {
                getLexer().nextToken();
                stmt.setCursorSpecificSegment(true);
                continue;
            } else if (getLexer().identifierEquals("NOPARALLEL")) {
                getLexer().nextToken();
                stmt.setParallel(false);
                continue;
            } else if (getLexer().equalToken(Token.LOGGING)) {
                getLexer().nextToken();
                stmt.setLogging(Boolean.TRUE);
                continue;
            } else if (getLexer().equalToken(Token.CACHE)) {
                getLexer().nextToken();
                stmt.setCache(Boolean.TRUE);
                continue;
            } else if (getLexer().equalToken(Token.NOCACHE)) {
                getLexer().nextToken();
                stmt.setCache(Boolean.FALSE);
                continue;
            } else if (getLexer().equalToken(Token.NOCOMPRESS)) {
                getLexer().nextToken();
                stmt.setCompress(Boolean.FALSE);
                continue;
            } else if (getLexer().equalToken(Token.ON)) {
                getLexer().nextToken();
                accept(Token.COMMIT);
                stmt.setOnCommit(true);
                continue;
            } else if (getLexer().identifierEquals("PRESERVE")) {
                getLexer().nextToken();
                acceptIdentifier("ROWS");
                stmt.setPreserveRows(true);
                continue;
            } else if (getLexer().identifierEquals("STORAGE")) {
                OracleStorageClause storage = ((OracleExprParser) this.exprParser).parseStorage();
                stmt.setStorage(storage);
                continue;
            } else if (getLexer().identifierEquals("organization")) {
                getLexer().nextToken();
                accept(Token.INDEX);
                stmt.setOrganizationIndex(true);
                continue;
            } else if (getLexer().equalToken(Token.PCTFREE)) {
                getLexer().nextToken();
                stmt.setPtcfree(this.exprParser.expr());
                continue;
            } else if (getLexer().identifierEquals("PCTUSED")) {
                getLexer().nextToken();
                stmt.setPctused(this.exprParser.expr());
                continue;
            } else if (getLexer().equalToken(Token.STORAGE)) {
                OracleStorageClause storage = ((OracleExprParser) this.exprParser).parseStorage();
                stmt.setStorage(storage);
                continue;
            } else if (getLexer().equalToken(Token.LOB)) {
                OracleLobStorageClause lobStorage = ((OracleExprParser) this.exprParser).parseLobStorage();
                stmt.setLobStorage(lobStorage);
                continue;
            } else if (getLexer().equalToken(Token.INITRANS)) {
                getLexer().nextToken();
                stmt.setInitrans(this.exprParser.expr());
                continue;
            } else if (getLexer().equalToken(Token.MAXTRANS)) {
                getLexer().nextToken();
                stmt.setMaxtrans(this.exprParser.expr());
                continue;
            } else if (getLexer().equalToken(Token.SEGMENT)) {
                getLexer().nextToken();
                accept(Token.CREATION);
                if (getLexer().equalToken(Token.IMMEDIATE)) {
                    getLexer().nextToken();
                    stmt.setDeferredSegmentCreation(DeferredSegmentCreation.IMMEDIATE);
                } else {
                    accept(Token.DEFERRED);
                    stmt.setDeferredSegmentCreation(DeferredSegmentCreation.DEFERRED);
                }
                continue;
            } else if (getLexer().identifierEquals("PARTITION")) {
                getLexer().nextToken();
                accept(Token.BY);

                if (getLexer().identifierEquals("RANGE")) {
                    getLexer().nextToken();
                    accept(Token.LEFT_PAREN);
                    OraclePartitionByRangeClause clause = new OraclePartitionByRangeClause();
                    while (true) {
                        SQLName column = this.exprParser.name();
                        clause.getColumns().add(column);

                        if (getLexer().equalToken(Token.COMMA)) {
                            getLexer().nextToken();
                            continue;
                        }

                        break;
                    }
                    accept(Token.RIGHT_PAREN);

                    if (getLexer().identifierEquals("INTERVAL")) {
                        getLexer().nextToken();
                        clause.setInterval(this.exprParser.expr());
                    }

                    if (getLexer().equalToken(Token.STORE)) {
                        getLexer().nextToken();
                        accept(Token.IN);
                        accept(Token.LEFT_PAREN);
                        while (true) {
                            SQLName tablespace = this.exprParser.name();
                            clause.getStoreIn().add(tablespace);

                            if (getLexer().equalToken(Token.COMMA)) {
                                getLexer().nextToken();
                                continue;
                            }

                            break;
                        }
                        accept(Token.RIGHT_PAREN);
                    }

                    accept(Token.LEFT_PAREN);

                    while (true) {
                        acceptIdentifier("PARTITION");
                        OracleRangeValuesClause range = new OracleRangeValuesClause();
                        range.setName(this.exprParser.name());

                        accept(Token.VALUES);
                        acceptIdentifier("LESS");
                        acceptIdentifier("THAN");

                        accept(Token.LEFT_PAREN);
                        while (true) {
                            SQLExpr rangeValue = this.exprParser.expr();
                            range.getValues().add(rangeValue);

                            if (getLexer().equalToken(Token.COMMA)) {
                                getLexer().nextToken();
                                continue;
                            }

                            break;
                        }
                        accept(Token.RIGHT_PAREN);

                        clause.getRanges().add(range);

                        if (getLexer().equalToken(Token.COMMA)) {
                            getLexer().nextToken();
                            continue;
                        }

                        break;
                    }

                    accept(Token.RIGHT_PAREN);

                    stmt.setPartitioning(clause);
                    continue;
                } else {
                    throw new ParserUnsupportedException(getLexer().getToken());
                }
            }
            break;
        }

        if (getLexer().equalToken(Token.AS)) {
            getLexer().nextToken();

            OracleSelect select = new OracleSelectParser(exprParser).select();
            stmt.setSelect(select);
        }

        return stmt;
    }

}
