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

import com.alibaba.druid.sql.ast.SQLName;
import com.alibaba.druid.sql.ast.statement.SQLCheck;
import com.alibaba.druid.sql.ast.statement.SQLColumnDefinition;
import com.alibaba.druid.sql.ast.statement.SQLCreateTableStatement;
import com.alibaba.druid.sql.ast.statement.SQLForeignKeyConstraint;
import com.alibaba.druid.sql.ast.statement.SQLSelect;
import com.alibaba.druid.sql.ast.statement.SQLTableConstraint;
import com.alibaba.druid.sql.ast.statement.SQLTableSource;
import com.alibaba.druid.sql.dialect.mysql.ast.MySqlKey;
import com.alibaba.druid.sql.dialect.mysql.ast.MySqlPrimaryKey;
import com.alibaba.druid.sql.dialect.mysql.ast.MySqlUnique;
import com.alibaba.druid.sql.dialect.mysql.ast.MysqlForeignKey;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlCreateTableStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlCreateTableStatement.TableSpaceOption;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlPartitionByHash;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlPartitionByKey;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlPartitionByList;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlPartitionByRange;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlPartitioningClause;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlPartitioningDef;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlTableIndex;
import com.alibaba.druid.sql.lexer.Token;
import com.alibaba.druid.sql.parser.ParserUnsupportedException;
import com.alibaba.druid.sql.parser.SQLCreateTableParser;
import com.alibaba.druid.sql.parser.SQLExprParser;

public class MySqlCreateTableParser extends SQLCreateTableParser {
    
    public MySqlCreateTableParser(SQLExprParser exprParser){
        super(exprParser);
    }
    
    public MySqlExprParser getExprParser() {
        return (MySqlExprParser) exprParser;
    }
    
    public MySqlCreateTableStatement parseCrateTable(boolean acceptCreate) {
        if (acceptCreate) {
            accept(Token.CREATE);
        }
        MySqlCreateTableStatement stmt = new MySqlCreateTableStatement();

        if (getLexer().identifierEquals("TEMPORARY")) {
            getLexer().nextToken();
            stmt.setType(SQLCreateTableStatement.Type.GLOBAL_TEMPORARY);
        }

        accept(Token.TABLE);

        if (getLexer().getToken() == Token.IF || getLexer().identifierEquals("IF")) {
            getLexer().nextToken();
            accept(Token.NOT);
            accept(Token.EXISTS);

            stmt.setIfNotExists(true);
        }

        stmt.setName(this.exprParser.name());

        if (getLexer().getToken() == Token.LIKE) {
            getLexer().nextToken();
            SQLName name = this.exprParser.name();
            stmt.setLike(name);
        }

        if (getLexer().getToken() == (Token.LEFT_PAREN)) {
            getLexer().nextToken();

            if (getLexer().getToken() == Token.LIKE) {
                getLexer().nextToken();
                SQLName name = this.exprParser.name();
                stmt.setLike(name);
            } else {
                while (true) {
                    if (getLexer().getToken() == Token.IDENTIFIER //
                        || getLexer().getToken() == Token.LITERAL_CHARS) {
                        SQLColumnDefinition column = this.exprParser.parseColumn();
                        stmt.getTableElementList().add(column);
                    } else if (getLexer().getToken() == Token.CONSTRAINT //
                               || getLexer().getToken() == Token.PRIMARY //
                               || getLexer().getToken() == Token.UNIQUE) {
                        stmt.getTableElementList().add(parseConstraint());
                    } else if (getLexer().getToken() == (Token.INDEX)) {
                        getLexer().nextToken();

                        MySqlTableIndex idx = new MySqlTableIndex();

                        if (getLexer().getToken() == Token.IDENTIFIER) {
                            if (!"USING".equalsIgnoreCase(getLexer().getLiterals())) {
                                idx.setName(this.exprParser.name());
                            }
                        }

                        if (getLexer().identifierEquals("USING")) {
                            getLexer().nextToken();
                            idx.setIndexType(getLexer().getLiterals());
                            getLexer().nextToken();
                        }

                        accept(Token.LEFT_PAREN);
                        while (true) {
                            idx.getColumns().add(this.exprParser.expr());
                            if (!(getLexer().getToken() == (Token.COMMA))) {
                                break;
                            } else {
                                getLexer().nextToken();
                            }
                        }
                        accept(Token.RIGHT_PAREN);

                        stmt.getTableElementList().add(idx);
                    } else if (getLexer().getToken() == (Token.KEY)) {
                        stmt.getTableElementList().add(parseConstraint());
                    } else if (getLexer().getToken() == (Token.PRIMARY)) {
                        SQLTableConstraint pk = parseConstraint();
                        pk.setParent(stmt);
                        stmt.getTableElementList().add(pk);
                    } else if (getLexer().getToken() == (Token.FOREIGN)) {
                        SQLForeignKeyConstraint fk = this.getExprParser().parseForeignKey();
                        fk.setParent(stmt);
                        stmt.getTableElementList().add(fk);
                    } else if (getLexer().getToken() == Token.CHECK) {
                        SQLCheck check = this.exprParser.parseCheck();
                        stmt.getTableElementList().add(check);
                    } else {
                        SQLColumnDefinition column = this.exprParser.parseColumn();
                        stmt.getTableElementList().add(column);
                    }

                    if (!(getLexer().getToken() == (Token.COMMA))) {
                        break;
                    } else {
                        getLexer().nextToken();
                    }
                }
            }

            accept(Token.RIGHT_PAREN);
        }

        while (true) {
            if (getLexer().identifierEquals("ENGINE")) {
                getLexer().nextToken();
                if (getLexer().getToken() == Token.EQ) {
                    getLexer().nextToken();
                }
                stmt.getTableOptions().put("ENGINE", this.exprParser.expr());
                continue;
            }

            if (getLexer().identifierEquals("AUTO_INCREMENT")) {
                getLexer().nextToken();
                if (getLexer().getToken() == Token.EQ) {
                    getLexer().nextToken();
                }
                stmt.getTableOptions().put("AUTO_INCREMENT", this.exprParser.expr());
                continue;
            }

            if (getLexer().identifierEquals("AVG_ROW_LENGTH")) {
                getLexer().nextToken();
                if (getLexer().getToken() == Token.EQ) {
                    getLexer().nextToken();
                }
                stmt.getTableOptions().put("AVG_ROW_LENGTH", this.exprParser.expr());
                continue;
            }

            if (getLexer().getToken() == Token.DEFAULT) {
                getLexer().nextToken();
                parseTableOptionCharsetOrCollate(stmt);
                continue;
            }

            if (parseTableOptionCharsetOrCollate(stmt)) {
                continue;
            }

            if (getLexer().identifierEquals("CHECKSUM")) {
                getLexer().nextToken();
                if (getLexer().getToken() == Token.EQ) {
                    getLexer().nextToken();
                }
                stmt.getTableOptions().put("CHECKSUM", this.exprParser.expr());
                continue;
            }

            if (getLexer().getToken() == Token.COMMENT) {
                getLexer().nextToken();
                if (getLexer().getToken() == Token.EQ) {
                    getLexer().nextToken();
                }
                stmt.getTableOptions().put("COMMENT", this.exprParser.expr());
                continue;
            }

            if (getLexer().identifierEquals("CONNECTION")) {
                getLexer().nextToken();
                if (getLexer().getToken() == Token.EQ) {
                    getLexer().nextToken();
                }
                stmt.getTableOptions().put("CONNECTION", this.exprParser.expr());
                continue;
            }

            if (getLexer().identifierEquals("DATA")) {
                getLexer().nextToken();
                acceptIdentifier("DIRECTORY");
                if (getLexer().getToken() == Token.EQ) {
                    getLexer().nextToken();
                }
                stmt.getTableOptions().put("DATA DIRECTORY", this.exprParser.expr());
                continue;
            }

            if (getLexer().identifierEquals("DELAY_KEY_WRITE")) {
                getLexer().nextToken();
                if (getLexer().getToken() == Token.EQ) {
                    getLexer().nextToken();
                }
                stmt.getTableOptions().put("DELAY_KEY_WRITE", this.exprParser.expr());
                continue;
            }

            if (getLexer().identifierEquals("INDEX")) {
                getLexer().nextToken();
                acceptIdentifier("DIRECTORY");
                if (getLexer().getToken() == Token.EQ) {
                    getLexer().nextToken();
                }
                stmt.getTableOptions().put("INDEX DIRECTORY", this.exprParser.expr());
                continue;
            }

            if (getLexer().identifierEquals("INSERT_METHOD")) {
                getLexer().nextToken();
                if (getLexer().getToken() == Token.EQ) {
                    getLexer().nextToken();
                }
                stmt.getTableOptions().put("INSERT_METHOD", this.exprParser.expr());
                continue;
            }

            if (getLexer().identifierEquals("KEY_BLOCK_SIZE")) {
                getLexer().nextToken();
                if (getLexer().getToken() == Token.EQ) {
                    getLexer().nextToken();
                }
                stmt.getTableOptions().put("KEY_BLOCK_SIZE", this.exprParser.expr());
                continue;
            }

            if (getLexer().identifierEquals("MAX_ROWS")) {
                getLexer().nextToken();
                if (getLexer().getToken() == Token.EQ) {
                    getLexer().nextToken();
                }
                stmt.getTableOptions().put("MAX_ROWS", this.exprParser.expr());
                continue;
            }

            if (getLexer().identifierEquals("MIN_ROWS")) {
                getLexer().nextToken();
                if (getLexer().getToken() == Token.EQ) {
                    getLexer().nextToken();
                }
                stmt.getTableOptions().put("MIN_ROWS", this.exprParser.expr());
                continue;
            }

            if (getLexer().identifierEquals("PACK_KEYS")) {
                getLexer().nextToken();
                if (getLexer().getToken() == Token.EQ) {
                    getLexer().nextToken();
                }
                stmt.getTableOptions().put("PACK_KEYS", this.exprParser.expr());
                continue;
            }

            if (getLexer().identifierEquals("PASSWORD")) {
                getLexer().nextToken();
                if (getLexer().getToken() == Token.EQ) {
                    getLexer().nextToken();
                }
                stmt.getTableOptions().put("PASSWORD", this.exprParser.expr());
                continue;
            }

            if (getLexer().identifierEquals("ROW_FORMAT")) {
                getLexer().nextToken();
                if (getLexer().getToken() == Token.EQ) {
                    getLexer().nextToken();
                }
                stmt.getTableOptions().put("ROW_FORMAT", this.exprParser.expr());
                continue;
            }

            if (getLexer().identifierEquals("STATS_AUTO_RECALC")) {
                getLexer().nextToken();
                if (getLexer().getToken() == Token.EQ) {
                    getLexer().nextToken();
                }

                stmt.getTableOptions().put("STATS_AUTO_RECALC", this.exprParser.expr());
                continue;
            }

            if (getLexer().identifierEquals("STATS_PERSISTENT")) {
                getLexer().nextToken();
                if (getLexer().getToken() == Token.EQ) {
                    getLexer().nextToken();
                }

                stmt.getTableOptions().put("STATS_PERSISTENT", this.exprParser.expr());
                continue;
            }
            
            if (getLexer().identifierEquals("STATS_SAMPLE_PAGES")) {
                getLexer().nextToken();
                if (getLexer().getToken() == Token.EQ) {
                    getLexer().nextToken();
                }

                stmt.getTableOptions().put("STATS_SAMPLE_PAGES", this.exprParser.expr());
                continue;
            }
            
            if (getLexer().getToken() == Token.UNION) {
                getLexer().nextToken();
                if (getLexer().getToken() == Token.EQ) {
                    getLexer().nextToken();
                }

                accept(Token.LEFT_PAREN);
                SQLTableSource tableSrc = this.createSQLSelectParser().parseTableSource();
                stmt.getTableOptions().put("UNION", tableSrc);
                accept(Token.RIGHT_PAREN);
                continue;
            }

            if (getLexer().getToken() == Token.TABLESPACE) {
                getLexer().nextToken();

                TableSpaceOption option = new TableSpaceOption();
                option.setName(this.exprParser.name());

                if (getLexer().identifierEquals("STORAGE")) {
                    getLexer().nextToken();
                    option.setStorage(this.exprParser.name());
                }

                stmt.getTableOptions().put("TABLESPACE", option);
                continue;
            }

            if (getLexer().identifierEquals("TYPE")) {
                getLexer().nextToken();
                accept(Token.EQ);
                stmt.getTableOptions().put("TYPE", this.exprParser.expr());
                getLexer().nextToken();
                continue;
            }

            if (getLexer().identifierEquals("PARTITION")) {
                getLexer().nextToken();
                accept(Token.BY);

                MySqlPartitioningClause partitionClause;

                boolean linera = false;
                if (getLexer().identifierEquals("LINEAR")) {
                    getLexer().nextToken();
                    linera = true;
                }

                if (getLexer().getToken() == Token.KEY) {
                    MySqlPartitionByKey clause = new MySqlPartitionByKey();
                    getLexer().nextToken();

                    if (linera) {
                        clause.setLinear(true);
                    }

                    accept(Token.LEFT_PAREN);
                    while (true) {
                        clause.getColumns().add(this.exprParser.name());
                        if (getLexer().getToken() == Token.COMMA) {
                            getLexer().nextToken();
                            continue;
                        }
                        break;
                    }
                    accept(Token.RIGHT_PAREN);

                    partitionClause = clause;

                    if (getLexer().identifierEquals("PARTITIONS")) {
                        getLexer().nextToken();
                        clause.setPartitionCount(this.exprParser.expr());
                    }
                } else if (getLexer().identifierEquals("HASH")) {
                    getLexer().nextToken();
                    MySqlPartitionByHash clause = new MySqlPartitionByHash();

                    if (linera) {
                        clause.setLinear(true);
                    }

                    accept(Token.LEFT_PAREN);
                    clause.setExpr(this.exprParser.expr());
                    accept(Token.RIGHT_PAREN);
                    partitionClause = clause;

                    if (getLexer().identifierEquals("PARTITIONS")) {
                        getLexer().nextToken();
                        clause.setPartitionCount(this.exprParser.expr());
                    }

                } else if (getLexer().identifierEquals("RANGE")) {
                    getLexer().nextToken();
                    MySqlPartitionByRange clause = new MySqlPartitionByRange();

                    if (getLexer().getToken() == Token.LEFT_PAREN) {
                        getLexer().nextToken();
                        clause.setExpr(this.exprParser.expr());
                        accept(Token.RIGHT_PAREN);
                    } else {
                        acceptIdentifier("COLUMNS");
                        accept(Token.LEFT_PAREN);
                        while (true) {
                            clause.getColumns().add(this.exprParser.name());
                            if (getLexer().getToken() == Token.COMMA) {
                                getLexer().nextToken();
                                continue;
                            }
                            break;
                        }
                        accept(Token.RIGHT_PAREN);
                    }
                    partitionClause = clause;

                    if (getLexer().identifierEquals("PARTITIONS")) {
                        getLexer().nextToken();
                        clause.setPartitionCount(this.exprParser.expr());
                    }
                    //

                } else if (getLexer().identifierEquals("LIST")) {
                    getLexer().nextToken();
                    MySqlPartitionByList clause = new MySqlPartitionByList();

                    if (getLexer().getToken() == Token.LEFT_PAREN) {
                        getLexer().nextToken();
                        clause.setExpr(this.exprParser.expr());
                        accept(Token.RIGHT_PAREN);
                    } else {
                        acceptIdentifier("COLUMNS");
                        accept(Token.LEFT_PAREN);
                        while (true) {
                            clause.getColumns().add(this.exprParser.name());
                            if (getLexer().getToken() == Token.COMMA) {
                                getLexer().nextToken();
                                continue;
                            }
                            break;
                        }
                        accept(Token.RIGHT_PAREN);
                    }
                    partitionClause = clause;

                    if (getLexer().identifierEquals("PARTITIONS")) {
                        getLexer().nextToken();
                        clause.setPartitionCount(this.exprParser.expr());
                    }
                } else {
                    throw new ParserUnsupportedException(getLexer().getToken());
                }
                if (getLexer().getToken() == Token.LEFT_PAREN) {
                    getLexer().nextToken();
                    while (true) {
                        acceptIdentifier("PARTITION");

                        MySqlPartitioningDef partitionDef = new MySqlPartitioningDef();

                        partitionDef.setName(this.exprParser.name());

                        if (getLexer().getToken() == Token.VALUES) {
                            getLexer().nextToken();
                            if (getLexer().getToken() == Token.IN) {
                                getLexer().nextToken();
                                MySqlPartitioningDef.InValues values = new MySqlPartitioningDef.InValues();

                                accept(Token.LEFT_PAREN);
                                this.exprParser.exprList(values.getItems(), values);
                                accept(Token.RIGHT_PAREN);
                                partitionDef.setValues(values);
                            } else {
                                acceptIdentifier("LESS");
                                acceptIdentifier("THAN");

                                MySqlPartitioningDef.LessThanValues values = new MySqlPartitioningDef.LessThanValues();

                                accept(Token.LEFT_PAREN);
                                this.exprParser.exprList(values.getItems(), values);
                                accept(Token.RIGHT_PAREN);
                                partitionDef.setValues(values);
                            }
                        }

                        while (true) {
                            if (getLexer().identifierEquals("DATA")) {
                                getLexer().nextToken();
                                acceptIdentifier("DIRECTORY");
                                if (getLexer().getToken() == Token.EQ) {
                                    getLexer().nextToken();
                                }
                                partitionDef.setDataDirectory(this.exprParser.expr());
                            } else if (getLexer().getToken() == Token.INDEX) {
                                getLexer().nextToken();
                                acceptIdentifier("DIRECTORY");
                                if (getLexer().getToken() == Token.EQ) {
                                    getLexer().nextToken();
                                }
                                partitionDef.setIndexDirectory(this.exprParser.expr());
                            } else {
                                break;
                            }
                        }

                        partitionClause.getPartitions().add(partitionDef);

                        if (getLexer().getToken() == Token.COMMA) {
                            getLexer().nextToken();
                        } else {
                            break;
                        }
                    }
                    accept(Token.RIGHT_PAREN);
                }

                stmt.setPartitioning(partitionClause);
            }

            break;
        }

        if (getLexer().getToken() == (Token.ON)) {
            throw new ParserUnsupportedException(getLexer().getToken());
        }
        
        if (getLexer().getToken() == (Token.AS)) {
            getLexer().nextToken();
        }

        if (getLexer().getToken() == (Token.SELECT)) {
            SQLSelect query = new MySqlSelectParser(this.exprParser).select();
            stmt.setQuery(query);
        }
        
        while (getLexer().getToken() == (Token.HINT)) {
            this.exprParser.parseHints(stmt.getOptionHints());
        }
        return stmt;
    }

    private boolean parseTableOptionCharsetOrCollate(MySqlCreateTableStatement stmt) {
        if (getLexer().identifierEquals("CHARACTER")) {
            getLexer().nextToken();
            accept(Token.SET);
            if (getLexer().getToken() == Token.EQ) {
                getLexer().nextToken();
            }
            stmt.getTableOptions().put("CHARACTER SET", this.exprParser.expr());
            return true;
        }

        if (getLexer().identifierEquals("CHARSET")) {
            getLexer().nextToken();
            if (getLexer().getToken() == Token.EQ) {
                getLexer().nextToken();
            }
            stmt.getTableOptions().put("CHARSET", this.exprParser.expr());
            return true;
        }

        if (getLexer().identifierEquals("COLLATE")) {
            getLexer().nextToken();
            if (getLexer().getToken() == Token.EQ) {
                getLexer().nextToken();
            }
            stmt.getTableOptions().put("COLLATE", this.exprParser.expr());
            return true;
        }

        return false;
    }

    protected SQLTableConstraint parseConstraint() {
        SQLName name = null;
        boolean hasConstaint = false;
        if (getLexer().getToken() == (Token.CONSTRAINT)) {
            hasConstaint = true;
            getLexer().nextToken();
        }

        if (getLexer().getToken() == Token.IDENTIFIER) {
            name = this.exprParser.name();
        }

        if (getLexer().getToken() == (Token.KEY)) {
            getLexer().nextToken();

            MySqlKey key = new MySqlKey();
            key.setHasConstraint(hasConstaint);

//            if (identifierEquals("USING")) {
//                getLexer().nextToken();
//                key.setIndexType(getLexer().stringLiterals());
//                getLexer().nextToken();
//            }

            if (getLexer().getToken() == Token.IDENTIFIER) {
                SQLName indexName = this.exprParser.name();
                if (indexName != null) {
                    key.setIndexName(indexName);
                }
            }
            
            //5.5语法 USING BTREE 放在index 名字后
            if (getLexer().identifierEquals("USING")) {
                getLexer().nextToken();
                key.setIndexType(getLexer().getLiterals());
                getLexer().nextToken();
            }

            accept(Token.LEFT_PAREN);
            while (true) {
                key.getColumns().add(this.exprParser.expr());
                if (!(getLexer().getToken() == (Token.COMMA))) {
                    break;
                } else {
                    getLexer().nextToken();
                }
            }
            accept(Token.RIGHT_PAREN);

            if (name != null) {
                key.setName(name);
            }

            if (getLexer().identifierEquals("USING")) {
                getLexer().nextToken();
                key.setIndexType(getLexer().getLiterals());
                getLexer().nextToken();
            }
            return key;
        }

        if (getLexer().getToken() == Token.PRIMARY) {
            MySqlPrimaryKey pk = this.getExprParser().parsePrimaryKey();
            pk.setName(name);
            pk.setHasConstraint(hasConstaint);
            return pk;
        }

        if (getLexer().getToken() == Token.UNIQUE) {
            MySqlUnique uk = this.getExprParser().parseUnique();
            uk.setName(name);
            uk.setHasConstraint(hasConstaint);
            return uk;
        }

        if (getLexer().getToken() == Token.FOREIGN) {
            MysqlForeignKey fk = this.getExprParser().parseForeignKey();
            fk.setName(name);
            fk.setHasConstraint(hasConstaint);
            return fk;
        }
        throw new ParserUnsupportedException(getLexer().getToken());
    }
}
