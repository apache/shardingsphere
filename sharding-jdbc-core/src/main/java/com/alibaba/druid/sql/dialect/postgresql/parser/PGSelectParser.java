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

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.SQLSetQuantifier;
import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.statement.SQLExprTableSource;
import com.alibaba.druid.sql.ast.statement.SQLSelectQuery;
import com.alibaba.druid.sql.ast.statement.SQLTableSource;
import com.alibaba.druid.sql.dialect.postgresql.ast.expr.PGParameter;
import com.alibaba.druid.sql.dialect.postgresql.ast.stmt.PGFunctionTableSource;
import com.alibaba.druid.sql.dialect.postgresql.ast.stmt.PGSelectQueryBlock;
import com.alibaba.druid.sql.dialect.postgresql.ast.stmt.PGSelectQueryBlock.IntoOption;
import com.alibaba.druid.sql.dialect.postgresql.ast.stmt.PGSelectQueryBlock.PGLimit;
import com.alibaba.druid.sql.dialect.postgresql.ast.stmt.PGValuesQuery;
import com.alibaba.druid.sql.lexer.Token;
import com.alibaba.druid.sql.parser.ParserException;
import com.alibaba.druid.sql.parser.SQLExprParser;
import com.alibaba.druid.sql.parser.SQLSelectParser;

import java.util.List;

public class PGSelectParser extends SQLSelectParser {
    
    public PGSelectParser(SQLExprParser exprParser){
        super(exprParser);
    }
    
    protected SQLExprParser createExprParser() {
        return new PGExprParser(getLexer());
    }
    
    @Override
    public SQLSelectQuery query() {
        if (getLexer().equalToken(Token.VALUES)) {
            getLexer().nextToken();
            accept(Token.LEFT_PAREN);
            PGValuesQuery valuesQuery = new PGValuesQuery();
            valuesQuery.getValues().addAll(getExprParser().exprList(valuesQuery));
            accept(Token.RIGHT_PAREN);
            return queryRest(valuesQuery);
        }

        if (getLexer().equalToken(Token.LEFT_PAREN)) {
            getLexer().nextToken();
            SQLSelectQuery select = query();
            accept(Token.RIGHT_PAREN);
            return queryRest(select);
        }

        PGSelectQueryBlock queryBlock = new PGSelectQueryBlock();

        if (getLexer().equalToken(Token.SELECT)) {
            getLexer().nextToken();

            if (getLexer().equalToken(Token.COMMENT)) {
                getLexer().nextToken();
            }

            if (getLexer().equalToken(Token.DISTINCT)) {
                queryBlock.setDistionOption(SQLSetQuantifier.DISTINCT);
                getLexer().nextToken();

                if (getLexer().equalToken(Token.ON)) {
                    getLexer().nextToken();

                    while (true) {
                        SQLExpr expr = this.createExprParser().expr();
                        queryBlock.getDistinctOn().add(expr);
                        if (getLexer().equalToken(Token.COMMA)) {
                            getLexer().nextToken();
                        } else {
                            break;
                        }
                    }
                }
            } else if (getLexer().equalToken(Token.ALL)) {
                queryBlock.setDistionOption(SQLSetQuantifier.ALL);
                getLexer().nextToken();
            }

            parseSelectList(queryBlock);

            if (getLexer().equalToken(Token.INTO)) {
                getLexer().nextToken();

                if (getLexer().equalToken(Token.TEMPORARY)) {
                    getLexer().nextToken();
                    queryBlock.setIntoOption(IntoOption.TEMPORARY);
                } else if (getLexer().equalToken(Token.TEMP)) {
                    getLexer().nextToken();
                    queryBlock.setIntoOption(IntoOption.TEMP);
                } else if (getLexer().equalToken(Token.UNLOGGED)) {
                    getLexer().nextToken();
                    queryBlock.setIntoOption(IntoOption.UNLOGGED);
                }

                if (getLexer().equalToken(Token.TABLE)) {
                    getLexer().nextToken();
                }

                SQLExpr name = this.createExprParser().name();

                queryBlock.setInto(new SQLExprTableSource(name));
            }
        }

        parseFrom(queryBlock);

        parseWhere(queryBlock);

        parseGroupBy(queryBlock);

        if (getLexer().equalToken(Token.WINDOW)) {
            getLexer().nextToken();
            PGSelectQueryBlock.WindowClause window = new PGSelectQueryBlock.WindowClause();
            window.setName(getExprParser().expr());
            accept(Token.AS);

            while (true) {
                SQLExpr expr = this.createExprParser().expr();
                window.getDefinition().add(expr);
                if (getLexer().equalToken(Token.COMMA)) {
                    getLexer().nextToken();
                } else {
                    break;
                }
            }
            queryBlock.setWindow(window);
        }

        queryBlock.setOrderBy(this.createExprParser().parseOrderBy());

        while (true) {
            if (getLexer().equalToken(Token.LIMIT)) {
                PGLimit limit = new PGLimit();
    
                getLexer().nextToken();
                if (getLexer().equalToken(Token.ALL)) {
                    limit.setRowCount(new SQLIdentifierExpr("ALL"));
                    getLexer().nextToken();
                } else {
                    limit.setRowCount(getExprParser().expr());
                }

                queryBlock.setLimit(limit);
            } else if (getLexer().equalToken(Token.OFFSET)) {
                PGLimit limit = queryBlock.getLimit();
                if (limit == null) {
                    limit = new PGLimit();
                    queryBlock.setLimit(limit);
                }
                getLexer().nextToken();
                limit.setOffset(getExprParser().expr());

                if (getLexer().equalToken(Token.ROW) || getLexer().equalToken(Token.ROWS)) {
                    getLexer().nextToken();
                }
            } else {
                break;
            }
        }

        if (getLexer().equalToken(Token.FETCH)) {
            getLexer().nextToken();
            PGSelectQueryBlock.FetchClause fetch = new PGSelectQueryBlock.FetchClause();

            if (getLexer().equalToken(Token.FIRST)) {
                fetch.setOption(PGSelectQueryBlock.FetchClause.Option.FIRST);
            } else if (getLexer().equalToken(Token.NEXT)) {
                fetch.setOption(PGSelectQueryBlock.FetchClause.Option.NEXT);
            } else {
                throw new ParserException("expect 'FIRST' or 'NEXT'");
            }

            fetch.setCount(getExprParser().expr());

            if (getLexer().equalToken(Token.ROW) || getLexer().equalToken(Token.ROWS)) {
                getLexer().nextToken();
            } else {
                throw new ParserException("expect 'ROW' or 'ROWS'");
            }

            if (getLexer().equalToken(Token.ONLY)) {
                getLexer().nextToken();
            } else {
                throw new ParserException("expect 'ONLY'");
            }

            queryBlock.setFetch(fetch);
        }

        if (getLexer().equalToken(Token.FOR)) {
            getLexer().nextToken();

            PGSelectQueryBlock.ForClause forClause = new PGSelectQueryBlock.ForClause();

            if (getLexer().equalToken(Token.UPDATE)) {
                forClause.setOption(PGSelectQueryBlock.ForClause.Option.UPDATE);
                getLexer().nextToken();
            } else if (getLexer().equalToken(Token.SHARE)) {
                forClause.setOption(PGSelectQueryBlock.ForClause.Option.SHARE);
                getLexer().nextToken();
            } else {
                throw new ParserException("expect 'FIRST' or 'NEXT'");
            }

            if (getLexer().equalToken(Token.OF)) {
                while (true) {
                    SQLExpr expr = this.createExprParser().expr();
                    forClause.getOf().add(expr);
                    if (getLexer().equalToken(Token.COMMA)) {
                        getLexer().nextToken();
                    } else {
                        break;
                    }
                }
            }

            if (getLexer().equalToken(Token.NOWAIT)) {
                getLexer().nextToken();
                forClause.setNoWait(true);
            }

            queryBlock.setForClause(forClause);
        }

        return queryRest(queryBlock);
    }

    protected SQLTableSource parseTableSourceRest(SQLTableSource tableSource) {
        if (getLexer().equalToken(Token.AS) && tableSource instanceof SQLExprTableSource) {
            getLexer().nextToken();

            String alias = null;
            if (getLexer().equalToken(Token.IDENTIFIER)) {
                alias = getLexer().getLiterals();
                getLexer().nextToken();
            }

            if (getLexer().equalToken(Token.LEFT_PAREN)) {
                SQLExprTableSource exprTableSource = (SQLExprTableSource) tableSource;

                PGFunctionTableSource functionTableSource = new PGFunctionTableSource(exprTableSource.getExpr());
                if (alias != null) {
                    functionTableSource.setAlias(alias);
                }
                
                getLexer().nextToken();
                parserParameters(functionTableSource.getParameters());
                accept(Token.RIGHT_PAREN);

                return super.parseTableSourceRest(functionTableSource);
            }
        }

        return super.parseTableSourceRest(tableSource);
    }

    private void parserParameters(List<PGParameter> parameters) {
        while (true) {
            PGParameter parameter = new PGParameter();

            parameter.setName(getExprParser().name());
            parameter.setDataType(getExprParser().parseDataType());

            parameters.add(parameter);
            if (getLexer().equalToken(Token.COMMA) || getLexer().equalToken(Token.SEMI)) {
                getLexer().nextToken();
            }

            if (!getLexer().equalToken(Token.BEGIN) && !getLexer().equalToken(Token.RIGHT_PAREN)) {
                continue;
            }
            break;
        }
    }
}
