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

import com.alibaba.druid.sql.ast.SQLDataTypeImpl;
import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.SQLHint;
import com.alibaba.druid.sql.ast.SQLName;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.expr.SQLBinaryOpExpr;
import com.alibaba.druid.sql.ast.expr.SQLBinaryOperator;
import com.alibaba.druid.sql.ast.expr.SQLCharExpr;
import com.alibaba.druid.sql.ast.expr.SQLNullExpr;
import com.alibaba.druid.sql.ast.expr.SQLQueryExpr;
import com.alibaba.druid.sql.ast.statement.SQLAlterTableAddColumn;
import com.alibaba.druid.sql.ast.statement.SQLAlterTableDisableConstraint;
import com.alibaba.druid.sql.ast.statement.SQLAlterTableDropColumnItem;
import com.alibaba.druid.sql.ast.statement.SQLAlterTableDropConstraint;
import com.alibaba.druid.sql.ast.statement.SQLAlterTableEnableConstraint;
import com.alibaba.druid.sql.ast.statement.SQLAlterTableStatement;
import com.alibaba.druid.sql.ast.statement.SQLColumnDefinition;
import com.alibaba.druid.sql.ast.statement.SQLDropSequenceStatement;
import com.alibaba.druid.sql.ast.statement.SQLDropTableStatement;
import com.alibaba.druid.sql.ast.statement.SQLDropTriggerStatement;
import com.alibaba.druid.sql.ast.statement.SQLDropUserStatement;
import com.alibaba.druid.sql.ast.statement.SQLInsertInto;
import com.alibaba.druid.sql.ast.statement.SQLInsertStatement;
import com.alibaba.druid.sql.ast.statement.SQLRollbackStatement;
import com.alibaba.druid.sql.ast.statement.SQLSelect;
import com.alibaba.druid.sql.ast.statement.SQLSelectOrderByItem;
import com.alibaba.druid.sql.ast.statement.SQLSelectStatement;
import com.alibaba.druid.sql.ast.statement.SQLSetStatement;
import com.alibaba.druid.sql.ast.statement.SQLTableSource;
import com.alibaba.druid.sql.ast.statement.SQLUpdateSetItem;
import com.alibaba.druid.sql.ast.statement.SQLUpdateStatement;
import com.alibaba.druid.sql.dialect.oracle.ast.clause.OracleErrorLoggingClause;
import com.alibaba.druid.sql.dialect.oracle.ast.clause.OracleParameter;
import com.alibaba.druid.sql.dialect.oracle.ast.clause.OracleReturningClause;
import com.alibaba.druid.sql.dialect.oracle.ast.stmt.OracleAlterIndexStatement;
import com.alibaba.druid.sql.dialect.oracle.ast.stmt.OracleAlterProcedureStatement;
import com.alibaba.druid.sql.dialect.oracle.ast.stmt.OracleAlterSessionStatement;
import com.alibaba.druid.sql.dialect.oracle.ast.stmt.OracleAlterSynonymStatement;
import com.alibaba.druid.sql.dialect.oracle.ast.stmt.OracleAlterTableAddConstraint;
import com.alibaba.druid.sql.dialect.oracle.ast.stmt.OracleAlterTableDropPartition;
import com.alibaba.druid.sql.dialect.oracle.ast.stmt.OracleAlterTableModify;
import com.alibaba.druid.sql.dialect.oracle.ast.stmt.OracleAlterTableMoveTablespace;
import com.alibaba.druid.sql.dialect.oracle.ast.stmt.OracleAlterTableSplitPartition;
import com.alibaba.druid.sql.dialect.oracle.ast.stmt.OracleAlterTableSplitPartition.NestedTablePartitionSpec;
import com.alibaba.druid.sql.dialect.oracle.ast.stmt.OracleAlterTableSplitPartition.TableSpaceItem;
import com.alibaba.druid.sql.dialect.oracle.ast.stmt.OracleAlterTableSplitPartition.UpdateIndexesClause;
import com.alibaba.druid.sql.dialect.oracle.ast.stmt.OracleAlterTableStatement;
import com.alibaba.druid.sql.dialect.oracle.ast.stmt.OracleAlterTableTruncatePartition;
import com.alibaba.druid.sql.dialect.oracle.ast.stmt.OracleAlterTablespaceAddDataFile;
import com.alibaba.druid.sql.dialect.oracle.ast.stmt.OracleAlterTablespaceStatement;
import com.alibaba.druid.sql.dialect.oracle.ast.stmt.OracleAlterTriggerStatement;
import com.alibaba.druid.sql.dialect.oracle.ast.stmt.OracleAlterViewStatement;
import com.alibaba.druid.sql.dialect.oracle.ast.stmt.OracleBlockStatement;
import com.alibaba.druid.sql.dialect.oracle.ast.stmt.OracleCommitStatement;
import com.alibaba.druid.sql.dialect.oracle.ast.stmt.OracleConstraint;
import com.alibaba.druid.sql.dialect.oracle.ast.stmt.OracleCreateDatabaseDbLinkStatement;
import com.alibaba.druid.sql.dialect.oracle.ast.stmt.OracleCreateIndexStatement;
import com.alibaba.druid.sql.dialect.oracle.ast.stmt.OracleCreateProcedureStatement;
import com.alibaba.druid.sql.dialect.oracle.ast.stmt.OracleCreateSequenceStatement;
import com.alibaba.druid.sql.dialect.oracle.ast.stmt.OracleDeleteStatement;
import com.alibaba.druid.sql.dialect.oracle.ast.stmt.OracleDropDbLinkStatement;
import com.alibaba.druid.sql.dialect.oracle.ast.stmt.OracleExceptionStatement;
import com.alibaba.druid.sql.dialect.oracle.ast.stmt.OracleExitStatement;
import com.alibaba.druid.sql.dialect.oracle.ast.stmt.OracleExplainStatement;
import com.alibaba.druid.sql.dialect.oracle.ast.stmt.OracleExprStatement;
import com.alibaba.druid.sql.dialect.oracle.ast.stmt.OracleFileSpecification;
import com.alibaba.druid.sql.dialect.oracle.ast.stmt.OracleForStatement;
import com.alibaba.druid.sql.dialect.oracle.ast.stmt.OracleGotoStatement;
import com.alibaba.druid.sql.dialect.oracle.ast.stmt.OracleIfStatement;
import com.alibaba.druid.sql.dialect.oracle.ast.stmt.OracleInsertStatement;
import com.alibaba.druid.sql.dialect.oracle.ast.stmt.OracleLabelStatement;
import com.alibaba.druid.sql.dialect.oracle.ast.stmt.OracleLockTableStatement;
import com.alibaba.druid.sql.dialect.oracle.ast.stmt.OracleLockTableStatement.LockMode;
import com.alibaba.druid.sql.dialect.oracle.ast.stmt.OracleLoopStatement;
import com.alibaba.druid.sql.dialect.oracle.ast.stmt.OracleMergeStatement;
import com.alibaba.druid.sql.dialect.oracle.ast.stmt.OraclePLSQLCommitStatement;
import com.alibaba.druid.sql.dialect.oracle.ast.stmt.OracleSavePointStatement;
import com.alibaba.druid.sql.dialect.oracle.ast.stmt.OracleSetTransactionStatement;
import com.alibaba.druid.sql.dialect.oracle.ast.stmt.OracleStatement;
import com.alibaba.druid.sql.lexer.Token;
import com.alibaba.druid.sql.parser.ParserException;
import com.alibaba.druid.sql.parser.ParserUnsupportedException;
import com.alibaba.druid.sql.parser.SQLStatementParser;
import com.alibaba.druid.util.JdbcConstants;

import java.util.ArrayList;
import java.util.List;

public class OracleStatementParser extends SQLStatementParser {
    
    public OracleStatementParser(String sql){
        super(new OracleExprParser(sql));
    }
    
    @Override
    protected OracleSelectParser createSQLSelectParser() {
        return new OracleSelectParser(exprParser);
    }
    
    @Override
    public OracleExprParser getExprParser() {
        return (OracleExprParser) exprParser;
    }
    
    public void parseHints(final List<SQLHint> hints) {
        getExprParser().parseHints(hints);
    }

    public OracleCreateTableParser getSQLCreateTableParser() {
        return new OracleCreateTableParser(getLexer());
    }

    @Override
    protected List<SQLStatement> parseStatementList(int max) {
        List<SQLStatement> result = new ArrayList<>(-1 == max ? 16 : max);
        while (true) {
            if (-1 != max && result.size() >= max) {
                return result;
            }
            if (getLexer().isEndToken()) {
                return result;
            }
            if (getLexer().equalToken(Token.ELSE)) {
                return result;
            }
            if (getLexer().equalToken(Token.SEMI)) {
                getLexer().nextToken();
                continue;
            }
            if (getLexer().equalToken(Token.SELECT)) {
                SQLSelectStatement stmt = new SQLSelectStatement(new OracleSelectParser(this.exprParser).select(), JdbcConstants.ORACLE);
                result.add(stmt);
                continue;
            }

            if (getLexer().equalToken(Token.UPDATE)) {
                result.add(parseUpdateStatement());
                continue;
            }

            if (getLexer().equalToken(Token.CREATE)) {
                result.add(parseCreate());
                continue;
            }

            if (getLexer().equalToken(Token.INSERT)) {
                result.add(parseInsert());
                continue;
            }

            if (getLexer().equalToken(Token.DELETE)) {
                result.add(parseDeleteStatement());
                continue;
            }

            if (getLexer().equalToken(Token.SLASH)) {
                getLexer().nextToken();
                result.add(new OraclePLSQLCommitStatement());
                continue;
            }

            if (getLexer().equalToken(Token.ALTER)) {
                result.add(parserAlter());
                continue;
            }

            if (getLexer().equalToken(Token.WITH)) {
                result.add(new SQLSelectStatement(new OracleSelectParser(this.exprParser).select()));
                continue;
            }

            if (getLexer().equalToken(Token.LEFT_BRACE) || getLexer().identifierEquals("CALL")) {
                result.add(this.parseCall());
                continue;
            }

            if (getLexer().equalToken(Token.MERGE)) {
                result.add(this.parseMerge());
                continue;
            }

            if (getLexer().equalToken(Token.BEGIN)) {
                result.add(this.parseBlock());
                continue;
            }

            if (getLexer().equalToken(Token.DECLARE)) {
                result.add(this.parseBlock());
                continue;
            }

            if (getLexer().equalToken(Token.LOCK)) {
                result.add(this.parseLock());
                continue;
            }

            if (getLexer().equalToken(Token.TRUNCATE)) {
                result.add(this.parseTruncate());
                continue;
            }

            if (getLexer().equalToken(Token.VARIANT)) {
                SQLExpr variant = this.exprParser.primary();
                if (variant instanceof SQLBinaryOpExpr) {
                    SQLBinaryOpExpr binaryOpExpr = (SQLBinaryOpExpr) variant;
                    if (binaryOpExpr.getOperator() == SQLBinaryOperator.Assignment) {
                        SQLSetStatement stmt = new SQLSetStatement(binaryOpExpr.getLeft(), binaryOpExpr.getRight(), getDbType());
                        result.add(stmt);
                        continue;
                    }
                }
                accept(Token.COLON_EQ);
                SQLExpr value = this.exprParser.expr();

                SQLSetStatement stmt = new SQLSetStatement(variant, value, getDbType());
                result.add(stmt);
                continue;
            }

            if (getLexer().equalToken(Token.EXCEPTION)) {
                result.add(this.parseException());
                continue;
            }

            if (getLexer().identifierEquals("EXIT")) {
                getLexer().nextToken();
                OracleExitStatement stmt = new OracleExitStatement();
                if (getLexer().equalToken(Token.WHEN)) {
                    getLexer().nextToken();
                    stmt.setWhen(this.exprParser.expr());
                }
                result.add(stmt);
                continue;
            }

            if (getLexer().equalToken(Token.FETCH) || getLexer().identifierEquals("FETCH")) {
                SQLStatement stmt = parseFetch();
                result.add(stmt);
                continue;
            }

            if (getLexer().identifierEquals("ROLLBACK")) {
                SQLRollbackStatement stmt = parseRollback();

                result.add(stmt);
                continue;
            }

            if (getLexer().equalToken(Token.EXPLAIN)) {
                result.add(this.parseExplain());
                continue;
            }

            if (getLexer().equalToken(Token.IDENTIFIER)) {
                SQLExpr expr = exprParser.expr();
                OracleExprStatement stmt = new OracleExprStatement(expr);
                result.add(stmt);
                continue;
            }

            if (getLexer().equalToken(Token.LEFT_PAREN)) {
                int currentPosition = getLexer().getCurrentPosition();
                getLexer().nextToken();

                if (getLexer().equalToken(Token.SELECT)) {
                    getLexer().setCurrentPosition(currentPosition);
                    getLexer().setToken(Token.LEFT_PAREN);
                    result.add(this.parseSelect());
                    continue;
                }
                throw new ParserUnsupportedException(getLexer().getToken());
            }

            if (getLexer().equalToken(Token.SET)) {
                result.add(this.parseSet());
                continue;
            }

            if (getLexer().equalToken(Token.GRANT)) {
                result.add(this.parseGrant());
                continue;
            }
            
            if (getLexer().equalToken(Token.REVOKE)) {
                result.add(this.parseRevoke());
                continue;
            }
            
            if (getLexer().equalToken(Token.COMMENT)) {
                result.add(this.parseComment());
                continue;
            }
            if (getLexer().equalToken(Token.FOR)) {
                result.add(this.parseFor());
                continue;
            }
            if (getLexer().equalToken(Token.LOOP)) {
                result.add(this.parseLoop());
                continue;
            }
            if (getLexer().equalToken(Token.IF)) {
                result.add(this.parseIf());
                continue;
            }

            if (getLexer().equalToken(Token.GOTO)) {
                getLexer().nextToken();
                SQLName label = this.exprParser.name();
                OracleGotoStatement stmt = new OracleGotoStatement(label);
                result.add(stmt);
                continue;
            }

            if (getLexer().equalToken(Token.COMMIT)) {
                getLexer().nextToken();

                if (getLexer().identifierEquals("WORK")) {
                    getLexer().nextToken();
                }
                OracleCommitStatement stmt = new OracleCommitStatement();

                if (getLexer().identifierEquals("WRITE")) {
                    stmt.setWrite(true);
                    getLexer().nextToken();

                    while (true) {
                        if (getLexer().equalToken(Token.WAIT)) {
                            getLexer().nextToken();
                            stmt.setWait(Boolean.TRUE);
                            continue;
                        } else if (getLexer().equalToken(Token.NOWAIT)) {
                            getLexer().nextToken();
                            stmt.setWait(Boolean.FALSE);
                            continue;
                        } else if (getLexer().equalToken(Token.IMMEDIATE)) {
                            getLexer().nextToken();
                            stmt.setImmediate(Boolean.TRUE);
                            continue;
                        } else if (getLexer().identifierEquals("BATCH")) {
                            getLexer().nextToken();
                            stmt.setImmediate(Boolean.FALSE);
                            continue;
                        }

                        break;
                    }
                }

                result.add(stmt);
                continue;
            }

            if (getLexer().equalToken(Token.SAVEPOINT)) {
                getLexer().nextToken();

                OracleSavePointStatement stmt = new OracleSavePointStatement();

                if (getLexer().equalToken(Token.TO)) {
                    getLexer().nextToken();
                    stmt.setTo(this.exprParser.name());
                }

                result.add(stmt);
                continue;
            }

            if (getLexer().equalToken(Token.DOUBLE_LT)) {
                getLexer().nextToken();
                SQLName label = this.exprParser.name();
                OracleLabelStatement stmt = new OracleLabelStatement(label);
                accept(Token.DOUBLE_GT);
                result.add(stmt);
                continue;
            }

            if (getLexer().equalToken(Token.DROP)) {
                getLexer().nextToken();

                if (getLexer().equalToken(Token.TABLE)) {
                    SQLDropTableStatement stmt = parseDropTable(false);
                    result.add(stmt);
                    continue;
                }

                boolean isPublic = false;
                if (getLexer().identifierEquals("PUBLIC")) {
                    getLexer().nextToken();
                    isPublic = true;
                }

                if (getLexer().equalToken(Token.DATABASE)) {
                    getLexer().nextToken();

                    if (getLexer().identifierEquals("LINK")) {
                        getLexer().nextToken();

                        OracleDropDbLinkStatement stmt = new OracleDropDbLinkStatement();
                        if (isPublic) {
                            stmt.setPublic(true);
                        }

                        stmt.setName(this.exprParser.name());

                        result.add(stmt);
                        continue;
                    }
                }

                if (getLexer().equalToken(Token.INDEX)) {
                    SQLStatement stmt = parseDropIndex();
                    result.add(stmt);
                    continue;
                }

                if (getLexer().equalToken(Token.VIEW)) {
                    SQLStatement stmt = parseDropView(false);
                    result.add(stmt);
                    continue;
                }

                if (getLexer().equalToken(Token.SEQUENCE)) {
                    SQLDropSequenceStatement stmt = parseDropSequence(false);
                    result.add(stmt);
                    continue;
                }

                if (getLexer().equalToken(Token.TRIGGER)) {
                    SQLDropTriggerStatement stmt = parseDropTrigger(false);
                    result.add(stmt);
                    continue;
                }

                if (getLexer().equalToken(Token.USER)) {
                    SQLDropUserStatement stmt = parseDropUser();
                    result.add(stmt);
                    continue;
                }
                throw new ParserUnsupportedException(getLexer().getToken());
            }

            if (getLexer().equalToken(Token.NULL)) {
                getLexer().nextToken();
                OracleExprStatement stmt = new OracleExprStatement(new SQLNullExpr());
                result.add(stmt);
                continue;
            }
            
            if (getLexer().equalToken(Token.OPEN)) {
                SQLStatement stmt = this.parseOpen();
                result.add(stmt);
                continue;
            }
            throw new ParserUnsupportedException(getLexer().getToken());
        }
    }

    public SQLStatement parseIf() {
        accept(Token.IF);

        OracleIfStatement stmt = new OracleIfStatement();

        stmt.setCondition(this.exprParser.expr());

        accept(Token.THEN);
    
        stmt.getStatements().addAll(parseStatementList());

        while (getLexer().equalToken(Token.ELSE)) {
            getLexer().nextToken();

            if (getLexer().equalToken(Token.IF)) {
                getLexer().nextToken();

                OracleIfStatement.ElseIf elseIf = new OracleIfStatement.ElseIf();

                elseIf.setCondition(this.exprParser.expr());

                accept(Token.THEN);
                elseIf.getStatements().addAll(parseStatementList());

                stmt.getElseIfList().add(elseIf);
            } else {
                OracleIfStatement.Else elseItem = new OracleIfStatement.Else();
                parseStatementList().addAll(elseItem.getStatements());
                stmt.setElseItem(elseItem);
                break;
            }
        }

        accept(Token.END);
        accept(Token.IF);

        return stmt;
    }

    public OracleForStatement parseFor() {
        accept(Token.FOR);

        OracleForStatement stmt = new OracleForStatement();

        stmt.setIndex(this.exprParser.name());
        accept(Token.IN);
        stmt.setRange(this.exprParser.expr());
        accept(Token.LOOP);
    
        stmt.getStatements().addAll(parseStatementList());
        accept(Token.END);
        accept(Token.LOOP);
        return stmt;
    }

    public OracleLoopStatement parseLoop() {
        accept(Token.LOOP);

        OracleLoopStatement stmt = new OracleLoopStatement();
    
        stmt.getStatements().addAll(parseStatementList());
        accept(Token.END);
        accept(Token.LOOP);
        return stmt;
    }

    public SQLStatement parseSet() {
        accept(Token.SET);
        acceptIdentifier("TRANSACTION");

        OracleSetTransactionStatement stmt = new OracleSetTransactionStatement();

        if (getLexer().identifierEquals("READ")) {
            getLexer().nextToken();
            acceptIdentifier("ONLY");
            stmt.setReadOnly(true);
        }

        acceptIdentifier("NAME");

        stmt.setName(this.exprParser.expr());
        return stmt;
    }

    public OracleStatement parserAlter() {
        accept(Token.ALTER);
        if (getLexer().equalToken(Token.SESSION)) {
            getLexer().nextToken();

            OracleAlterSessionStatement stmt = new OracleAlterSessionStatement();
            if (getLexer().equalToken(Token.SET)) {
                getLexer().nextToken();
                parseAssignItems(stmt.getItems(), stmt);
            } else {
                throw new ParserUnsupportedException(getLexer().getToken());
            }
            return stmt;
        } else if (getLexer().equalToken(Token.PROCEDURE)) {
            getLexer().nextToken();
            OracleAlterProcedureStatement stmt = new OracleAlterProcedureStatement();
            stmt.setName(this.exprParser.name());
            if (getLexer().identifierEquals("COMPILE")) {
                getLexer().nextToken();
                stmt.setCompile(true);
            }

            if (getLexer().identifierEquals("REUSE")) {
                getLexer().nextToken();
                acceptIdentifier("SETTINGS");
                stmt.setReuseSettings(true);
            }

            return stmt;
        } else if (getLexer().equalToken(Token.TABLE)) {
            return parseAlterTable();
        } else if (getLexer().equalToken(Token.INDEX)) {
            getLexer().nextToken();
            OracleAlterIndexStatement stmt = new OracleAlterIndexStatement();
            stmt.setName(this.exprParser.name());

            if (getLexer().identifierEquals("RENAME")) {
                getLexer().nextToken();
                accept(Token.TO);
                stmt.setRenameTo(this.exprParser.name());
            }

            while (true) {
                if (getLexer().identifierEquals("rebuild")) {
                    getLexer().nextToken();

                    OracleAlterIndexStatement.Rebuild rebuild = new OracleAlterIndexStatement.Rebuild();
                    stmt.setRebuild(rebuild);
                    continue;
                } else if (getLexer().identifierEquals("MONITORING")) {
                    getLexer().nextToken();
                    acceptIdentifier("USAGE");
                    stmt.setMonitoringUsage(Boolean.TRUE);
                    continue;
                } else if (getLexer().identifierEquals("PARALLEL")) {
                    getLexer().nextToken();
                    stmt.setParallel(this.exprParser.expr());
                }
                break;
            }

            return stmt;
        } else if (getLexer().equalToken(Token.TRIGGER)) {
            getLexer().nextToken();
            OracleAlterTriggerStatement stmt = new OracleAlterTriggerStatement(exprParser.name());

            while (true) {
                if (getLexer().equalToken(Token.ENABLE)) {
                    getLexer().nextToken();
                    stmt.setEnable(Boolean.TRUE);
                    continue;
                } else if (getLexer().equalToken(Token.DISABLE)) {
                    getLexer().nextToken();
                    stmt.setEnable(Boolean.FALSE);
                    continue;
                } else if (getLexer().identifierEquals("COMPILE")) {
                    getLexer().nextToken();
                    stmt.setCompile(true);
                    continue;
                }
                break;
            }

            return stmt;
        } else if (getLexer().identifierEquals("SYNONYM")) {
            getLexer().nextToken();
            OracleAlterSynonymStatement stmt = new OracleAlterSynonymStatement();
            stmt.setName(this.exprParser.name());

            while (true) {
                if (getLexer().equalToken(Token.ENABLE)) {
                    getLexer().nextToken();
                    stmt.setEnable(Boolean.TRUE);
                    continue;
                } else if (getLexer().equalToken(Token.DISABLE)) {
                    getLexer().nextToken();
                    stmt.setEnable(Boolean.FALSE);
                    continue;
                } else if (getLexer().identifierEquals("COMPILE")) {
                    getLexer().nextToken();
                    stmt.setCompile(true);
                    continue;
                }
                break;
            }

            return stmt;
        } else if (getLexer().equalToken(Token.VIEW)) {
            getLexer().nextToken();
            OracleAlterViewStatement stmt = new OracleAlterViewStatement(exprParser.name());

            while (true) {
                if (getLexer().equalToken(Token.ENABLE)) {
                    getLexer().nextToken();
                    stmt.setEnable(Boolean.TRUE);
                    continue;
                } else if (getLexer().equalToken(Token.DISABLE)) {
                    getLexer().nextToken();
                    stmt.setEnable(Boolean.FALSE);
                    continue;
                } else if (getLexer().identifierEquals("COMPILE")) {
                    getLexer().nextToken();
                    stmt.setCompile(true);
                    continue;
                }
                break;
            }

            return stmt;
        } else if (getLexer().equalToken(Token.TABLESPACE)) {
            getLexer().nextToken();

            OracleAlterTablespaceStatement stmt = new OracleAlterTablespaceStatement();
            stmt.setName(this.exprParser.name());

            if (getLexer().identifierEquals("ADD")) {
                getLexer().nextToken();

                if (getLexer().identifierEquals("DATAFILE")) {
                    getLexer().nextToken();

                    OracleAlterTablespaceAddDataFile item = new OracleAlterTablespaceAddDataFile();

                    while (true) {
                        OracleFileSpecification file = new OracleFileSpecification();

                        while (true) {
                            SQLExpr fileName = this.exprParser.expr();
                            file.getFileNames().add(fileName);

                            if (getLexer().equalToken(Token.COMMA)) {
                                getLexer().nextToken();
                                continue;
                            }

                            break;
                        }

                        if (getLexer().identifierEquals("SIZE")) {
                            getLexer().nextToken();
                            file.setSize(this.exprParser.expr());
                        }

                        if (getLexer().identifierEquals("AUTOEXTEND")) {
                            getLexer().nextToken();
                            if (getLexer().identifierEquals("OFF")) {
                                getLexer().nextToken();
                                file.setAutoExtendOff(true);
                            } else if (getLexer().identifierEquals("ON")) {
                                getLexer().nextToken();
                                file.setAutoExtendOn(this.exprParser.expr());
                            } else {
                                throw new ParserUnsupportedException(getLexer().getToken());
                            }
                        }

                        item.getFiles().add(file);

                        if (getLexer().equalToken(Token.COMMA)) {
                            getLexer().nextToken();
                            continue;
                        }

                        break;
                    }

                    stmt.setItem(item);
                } else {
                    throw new ParserUnsupportedException(getLexer().getToken());
                }
            } else {
                throw new ParserUnsupportedException(getLexer().getToken());
            }

            return stmt;
        }
        throw new ParserUnsupportedException(getLexer().getToken());
    }

    private OracleStatement parseAlterTable() {
        getLexer().nextToken();
        OracleAlterTableStatement stmt = new OracleAlterTableStatement();
        stmt.setName(this.exprParser.name());

        while (true) {
            if (getLexer().identifierEquals("ADD")) {
                getLexer().nextToken();

                if (getLexer().equalToken(Token.LEFT_PAREN)) {
                    getLexer().nextToken();

                    SQLAlterTableAddColumn item = parseAlterTableAddColumn();

                    stmt.getItems().add(item);

                    accept(Token.RIGHT_PAREN);
                } else if (getLexer().equalToken(Token.CONSTRAINT)) {
                    OracleConstraint constraint = ((OracleExprParser) this.exprParser).parseConstraint();
                    OracleAlterTableAddConstraint item = new OracleAlterTableAddConstraint();
                    constraint.setParent(item);
                    item.setParent(stmt);
                    item.setConstraint(constraint);
                    stmt.getItems().add(item);
                } else if (getLexer().equalToken(Token.IDENTIFIER)) {
                    SQLAlterTableAddColumn item = parseAlterTableAddColumn();
                    stmt.getItems().add(item);
                } else {
                    throw new ParserUnsupportedException(getLexer().getToken());
                }

                continue;
            } else if (getLexer().identifierEquals("MOVE")) {
                getLexer().nextToken();

                if (getLexer().equalToken(Token.TABLESPACE)) {
                    getLexer().nextToken();
                    OracleAlterTableMoveTablespace item = new OracleAlterTableMoveTablespace(exprParser.name());
                    stmt.getItems().add(item);
                } else {
                    throw new ParserUnsupportedException(getLexer().getToken());
                }
            } else if (getLexer().identifierEquals("RENAME")) {
                stmt.getItems().add(parseAlterTableRename());
            } else if (getLexer().identifierEquals("MODIFY")) {
                getLexer().nextToken();

                OracleAlterTableModify item = new OracleAlterTableModify();
                if (getLexer().equalToken(Token.LEFT_PAREN)) {
                    getLexer().nextToken();

                    while (true) {
                        SQLColumnDefinition columnDef = this.exprParser.parseColumn();
                        item.getColumns().add(columnDef);
                        if (getLexer().equalToken(Token.COMMA)) {
                            getLexer().nextToken();
                            continue;
                        }
                        break;
                    }
                    accept(Token.RIGHT_PAREN);

                } else {
                    SQLColumnDefinition columnDef = this.exprParser.parseColumn();
                    item.getColumns().add(columnDef);
                }

                stmt.getItems().add(item);
                continue;
            } else if (getLexer().identifierEquals("SPLIT")) {
                parseAlterTableSplit(stmt);
                continue;
            } else if (getLexer().equalToken(Token.TRUNCATE)) {
                getLexer().nextToken();
                if (getLexer().identifierEquals("PARTITION")) {
                    getLexer().nextToken();
                    OracleAlterTableTruncatePartition item = new OracleAlterTableTruncatePartition(exprParser.name());
                    stmt.getItems().add(item);
                } else {
                    throw new ParserUnsupportedException(getLexer().getToken());
                }
                continue;
            } else if (getLexer().equalToken(Token.DROP)) {
                parseAlterDrop(stmt);
                continue;
            } else if (getLexer().equalToken(Token.DISABLE)) {
                getLexer().nextToken();
                if (getLexer().equalToken(Token.CONSTRAINT)) {
                    getLexer().nextToken();
                    SQLAlterTableEnableConstraint item = new SQLAlterTableEnableConstraint();
                    item.setConstraintName(this.exprParser.name());
                    stmt.getItems().add(item);
                } else {
                    throw new ParserUnsupportedException(getLexer().getToken());
                }
            } else if (getLexer().equalToken(Token.ENABLE)) {
                getLexer().nextToken();
                if (getLexer().equalToken(Token.CONSTRAINT)) {
                    getLexer().nextToken();
                    SQLAlterTableDisableConstraint item = new SQLAlterTableDisableConstraint();
                    item.setConstraintName(this.exprParser.name());
                    stmt.getItems().add(item);
                } else {
                    throw new ParserUnsupportedException(getLexer().getToken());
                }
            }
            break;
        }

        if (getLexer().equalToken(Token.UPDATE)) {
            getLexer().nextToken();

            if (getLexer().identifierEquals("GLOBAL")) {
                getLexer().nextToken();
                acceptIdentifier("INDEXES");
                stmt.setUpdateGlobalIndexes(true);
            } else {
                throw new ParserUnsupportedException(getLexer().getToken());
            }
        }
        return stmt;
    }

    public void parseAlterDrop(SQLAlterTableStatement stmt) {
        getLexer().nextToken();
        if (getLexer().equalToken(Token.CONSTRAINT)) {
            getLexer().nextToken();
            SQLAlterTableDropConstraint item = new SQLAlterTableDropConstraint();
            item.setConstraintName(this.exprParser.name());
            stmt.getItems().add(item);
        } else if (getLexer().equalToken(Token.LEFT_PAREN)) {
            getLexer().nextToken();
            SQLAlterTableDropColumnItem item = new SQLAlterTableDropColumnItem();
            this.exprParser.names(item.getColumns());
            stmt.getItems().add(item);
            accept(Token.RIGHT_PAREN);
        } else if (getLexer().equalToken(Token.COLUMN)) {
            getLexer().nextToken();
            SQLAlterTableDropColumnItem item = new SQLAlterTableDropColumnItem();
            this.exprParser.names(item.getColumns());
            stmt.getItems().add(item);
        } else if (getLexer().identifierEquals("PARTITION")) {
            getLexer().nextToken();
            OracleAlterTableDropPartition item = new OracleAlterTableDropPartition(exprParser.name());
            stmt.getItems().add(item);
        } else {
            throw new ParserUnsupportedException(getLexer().getToken());
        }
    }

    private void parseAlterTableSplit(OracleAlterTableStatement stmt) {
        getLexer().nextToken();
        if (getLexer().identifierEquals("PARTITION")) {
            getLexer().nextToken();
            OracleAlterTableSplitPartition item = new OracleAlterTableSplitPartition();
            item.setName(this.exprParser.name());

            if (getLexer().identifierEquals("AT")) {
                getLexer().nextToken();
                accept(Token.LEFT_PAREN);
                item.getAt().addAll(exprParser.exprList(item));
                accept(Token.RIGHT_PAREN);
            } else {
                throw new ParserUnsupportedException(getLexer().getToken());
            }
            if (getLexer().equalToken(Token.INTO)) {
                getLexer().nextToken();
                accept(Token.LEFT_PAREN);

                while (true) {
                    NestedTablePartitionSpec spec = new NestedTablePartitionSpec();
                    acceptIdentifier("PARTITION");
                    spec.setPartition(this.exprParser.name());

                    while (true) {
                        if (getLexer().equalToken(Token.TABLESPACE)) {
                            getLexer().nextToken();
                            SQLName tablespace = this.exprParser.name();
                            spec.getSegmentAttributeItems().add(new TableSpaceItem(tablespace));
                            continue;
                        } else if (getLexer().identifierEquals("PCTREE")) {
                            throw new ParserUnsupportedException(getLexer().getToken());
                        } else if (getLexer().identifierEquals("PCTUSED")) {
                            throw new ParserUnsupportedException(getLexer().getToken());
                        } else if (getLexer().identifierEquals("INITRANS")) {
                            throw new ParserUnsupportedException(getLexer().getToken());
                        } else if (getLexer().identifierEquals("STORAGE")) {
                            throw new ParserUnsupportedException(getLexer().getToken());

                        } else if (getLexer().identifierEquals("LOGGING")) {
                            throw new ParserUnsupportedException(getLexer().getToken());
                        } else if (getLexer().identifierEquals("NOLOGGING")) {
                            throw new ParserUnsupportedException(getLexer().getToken());
                        } else if (getLexer().identifierEquals("FILESYSTEM_LIKE_LOGGING")) {
                            throw new ParserUnsupportedException(getLexer().getToken());
                        }
                        break;
                    }
                    item.getInto().add(spec);

                    if (getLexer().equalToken(Token.COMMA)) {
                        getLexer().nextToken();
                        continue;
                    }
                    break;
                }
                accept(Token.RIGHT_PAREN);
            }

            if (getLexer().equalToken(Token.UPDATE)) {
                getLexer().nextToken();
                acceptIdentifier("INDEXES");
                UpdateIndexesClause updateIndexes = new UpdateIndexesClause();
                item.setUpdateIndexes(updateIndexes);
            }
            stmt.getItems().add(item);
        } else {
            throw new ParserUnsupportedException(getLexer().getToken());
        }
    }

    public OracleLockTableStatement parseLock() {
        accept(Token.LOCK);
        accept(Token.TABLE);

        OracleLockTableStatement stmt = new OracleLockTableStatement();
        stmt.setTable(this.exprParser.name());

        accept(Token.IN);
        if (getLexer().equalToken(Token.SHARE)) {
            stmt.setLockMode(LockMode.SHARE);
            getLexer().nextToken();
        } else if (getLexer().equalToken(Token.EXCLUSIVE)) {
            stmt.setLockMode(LockMode.EXCLUSIVE);
            getLexer().nextToken();
        }
        accept(Token.MODE);

        if (getLexer().equalToken(Token.NOWAIT)) {
            getLexer().nextToken();
        } else if (getLexer().equalToken(Token.WAIT)) {
            getLexer().nextToken();
            stmt.setWait(exprParser.expr());
        }
        return stmt;
    }

    public OracleBlockStatement parseBlock() {
        OracleBlockStatement block = new OracleBlockStatement();

        if (getLexer().equalToken(Token.DECLARE)) {
            getLexer().nextToken();

            parserParameters(block.getParameters());
            for (OracleParameter param : block.getParameters()) {
                param.setParent(block);
            }
        }
        accept(Token.BEGIN);
        block.getStatementList().addAll(parseStatementList());
        accept(Token.END);
        return block;
    }

    private void parserParameters(List<OracleParameter> parameters) {
        while (true) {
            OracleParameter parameter = new OracleParameter();

            if (getLexer().equalToken(Token.CURSOR)) {
                getLexer().nextToken();

                parameter.setName(this.exprParser.name());

                accept(Token.IS);
                SQLSelect select = this.createSQLSelectParser().select();

                SQLDataTypeImpl dataType = new SQLDataTypeImpl("CURSOR");
                parameter.setDataType(dataType);

                parameter.setDefaultValue(new SQLQueryExpr(select));
            } else {
                parameter.setName(this.exprParser.name());
                parameter.setDataType(this.exprParser.parseDataType());

                if (getLexer().equalToken(Token.COLON_EQ)) {
                    getLexer().nextToken();
                    parameter.setDefaultValue(this.exprParser.expr());
                }
            }

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
    
    public OracleMergeStatement parseMerge() {
        accept(Token.MERGE);

        OracleMergeStatement stmt = new OracleMergeStatement();

        parseHints(stmt.getHints());

        accept(Token.INTO);
        stmt.setInto(exprParser.name());

        stmt.setAlias(as());

        accept(Token.USING);

        SQLTableSource using = this.createSQLSelectParser().parseTableSource();
        stmt.setUsing(using);

        accept(Token.ON);
        stmt.setOn(exprParser.expr());

        boolean insertFlag = false;
        if (getLexer().equalToken(Token.WHEN)) {
            getLexer().nextToken();
            if (getLexer().equalToken(Token.MATCHED)) {
                OracleMergeStatement.MergeUpdateClause updateClause = new OracleMergeStatement.MergeUpdateClause();
                getLexer().nextToken();
                accept(Token.THEN);
                accept(Token.UPDATE);
                accept(Token.SET);

                while (true) {
                    SQLUpdateSetItem item = this.exprParser.parseUpdateSetItem();

                    updateClause.getItems().add(item);
                    item.setParent(updateClause);

                    if (getLexer().equalToken(Token.COMMA)) {
                        getLexer().nextToken();
                        continue;
                    }

                    break;
                }

                if (getLexer().equalToken(Token.WHERE)) {
                    getLexer().nextToken();
                    updateClause.setWhere(exprParser.expr());
                }

                if (getLexer().equalToken(Token.DELETE)) {
                    getLexer().nextToken();
                    accept(Token.WHERE);
                    updateClause.setWhere(exprParser.expr());
                }

                stmt.setUpdateClause(updateClause);
            } else if (getLexer().equalToken(Token.NOT)) {
                getLexer().nextToken();
                insertFlag = true;
            }
        }

        if (!insertFlag) {
            if (getLexer().equalToken(Token.WHEN)) {
                getLexer().nextToken();
            }

            if (getLexer().equalToken(Token.NOT)) {
                getLexer().nextToken();
                insertFlag = true;
            }
        }

        if (insertFlag) {
            OracleMergeStatement.MergeInsertClause insertClause = new OracleMergeStatement.MergeInsertClause();

            accept(Token.MATCHED);
            accept(Token.THEN);
            accept(Token.INSERT);

            if (getLexer().equalToken(Token.LEFT_PAREN)) {
                accept(Token.LEFT_PAREN);
                insertClause.getColumns().addAll(exprParser.exprList(insertClause));
                accept(Token.RIGHT_PAREN);
            }
            accept(Token.VALUES);
            accept(Token.LEFT_PAREN);
            insertClause.getValues().addAll(exprParser.exprList(insertClause));
            accept(Token.RIGHT_PAREN);

            if (getLexer().equalToken(Token.WHERE)) {
                getLexer().nextToken();
                insertClause.setWhere(exprParser.expr());
            }

            stmt.setInsertClause(insertClause);
        }
        OracleErrorLoggingClause errorClause = parseErrorLoggingClause();
        stmt.setErrorLoggingClause(errorClause);
        return stmt;
    }
    
    @Override
    protected OracleStatement parseInsert() {
        accept(Token.INSERT);
        List<SQLHint> hints = new ArrayList<>();
        parseHints(hints);
        if (getLexer().equalToken(Token.ALL) || Token.FIRST.getName().equalsIgnoreCase(getLexer().getLiterals())) {
            throw new UnsupportedOperationException("Cannot support multi_table_insert for oracle");
        }
        if (getLexer().equalToken(Token.INTO)) {
            OracleInsertStatement result = new OracleInsertStatement();
            result.getHints().addAll(hints);
            parseInsert0(result, true);
            result.setReturning(parseReturningClause());
            result.setErrorLogging(parseErrorLoggingClause());
            return result;
        }
        // TODO 不会发生
        throw new ParserException("");
    }
    
    private void parseInsert0(final SQLInsertInto insertStatement, final boolean acceptSubQuery) {
        if (getLexer().equalToken(Token.INTO)) {
            getLexer().nextToken();
            SQLName tableName = exprParser.name();
            insertStatement.setTableName(tableName);
            if (getLexer().equalToken(Token.LITERAL_ALIAS)) {
                insertStatement.setAlias(as());
            }

            parseInsertHints(insertStatement);

            if (getLexer().equalToken(Token.IDENTIFIER)) {
                insertStatement.setAlias(getLexer().getLiterals());
                getLexer().nextToken();
            }
        }

        if (getLexer().equalToken(Token.LEFT_PAREN)) {
            getLexer().nextToken();
            insertStatement.getColumns().addAll(exprParser.exprList(insertStatement));
            accept(Token.RIGHT_PAREN);
        }

        if (getLexer().equalToken(Token.VALUES)) {
            getLexer().nextToken();
            accept(Token.LEFT_PAREN);
            SQLInsertStatement.ValuesClause values = new SQLInsertStatement.ValuesClause();
            values.getValues().addAll(exprParser.exprList(values));
            insertStatement.setValues(values);
            accept(Token.RIGHT_PAREN);
        } else if (acceptSubQuery && (getLexer().equalToken(Token.SELECT) || getLexer().equalToken(Token.LEFT_PAREN))) {
            SQLQueryExpr queryExpr = (SQLQueryExpr) this.exprParser.expr();
            insertStatement.setQuery(queryExpr.getSubQuery());
        }
    }
    
    private void parseInsertHints(final SQLInsertInto insertStatement) {
        if (insertStatement instanceof OracleInsertStatement) {
            OracleInsertStatement stmt = (OracleInsertStatement) insertStatement;
            getExprParser().parseHints(stmt.getHints());
        } else {
            List<SQLHint> hints = new ArrayList<>(1);
            getExprParser().parseHints(hints);
        }
    }

    private OracleExceptionStatement parseException() {
        accept(Token.EXCEPTION);
        OracleExceptionStatement stmt = new OracleExceptionStatement();

        while (true) {
            accept(Token.WHEN);
            OracleExceptionStatement.Item item = new OracleExceptionStatement.Item();
            item.setWhen(this.exprParser.expr());
            accept(Token.THEN);
            item.getStatements().addAll(parseStatementList());
            stmt.getItems().add(item);
            if (!getLexer().equalToken(Token.WHEN)) {
                break;
            }
        }
        return stmt;
    }

    private OracleErrorLoggingClause parseErrorLoggingClause() {
        if (getLexer().identifierEquals("LOG")) {
            OracleErrorLoggingClause errorClause = new OracleErrorLoggingClause();

            getLexer().nextToken();
            accept(Token.ERRORS);
            if (getLexer().equalToken(Token.INTO)) {
                getLexer().nextToken();
                errorClause.setInto(exprParser.name());
            }

            if (getLexer().equalToken(Token.LEFT_PAREN)) {
                getLexer().nextToken();
                errorClause.setSimpleExpression(exprParser.expr());
                accept(Token.RIGHT_PAREN);
            }

            if (getLexer().equalToken(Token.REJECT)) {
                getLexer().nextToken();
                accept(Token.LIMIT);
                errorClause.setLimit(exprParser.expr());
            }

            return errorClause;
        }
        return null;
    }

    public OracleReturningClause parseReturningClause() {
        OracleReturningClause clause = null;

        if (getLexer().equalToken(Token.RETURNING)) {
            getLexer().nextToken();
            clause = new OracleReturningClause();

            while (true) {
                SQLExpr item = exprParser.expr();
                clause.getItems().add(item);
                if (getLexer().equalToken(Token.COMMA)) {
                    getLexer().nextToken();
                    continue;
                }
                break;
            }
            accept(Token.INTO);
            while (true) {
                SQLExpr item = exprParser.expr();
                clause.getValues().add(item);
                if (getLexer().equalToken(Token.COMMA)) {
                    getLexer().nextToken();
                    continue;
                }
                break;
            }
        }
        return clause;
    }

    public OracleExplainStatement parseExplain() {
        accept(Token.EXPLAIN);
        acceptIdentifier("PLAN");
        OracleExplainStatement stmt = new OracleExplainStatement();

        if (getLexer().equalToken(Token.SET)) {
            getLexer().nextToken();
            acceptIdentifier("STATEMENT_ID");
            accept(Token.EQ);
            stmt.setStatementId((SQLCharExpr) this.exprParser.primary());
        }

        if (getLexer().equalToken(Token.INTO)) {
            getLexer().nextToken();
            stmt.setInto(this.exprParser.name());
        }

        accept(Token.FOR);
        stmt.setStatement(parseStatement());

        return stmt;
    }

    public OracleDeleteStatement parseDeleteStatement() {
        OracleDeleteStatement deleteStatement = new OracleDeleteStatement();

        if (getLexer().equalToken(Token.DELETE)) {
            getLexer().nextToken();

            if (getLexer().equalToken(Token.COMMENT)) {
                getLexer().nextToken();
            }

            parseHints(deleteStatement.getHints());

            if (getLexer().equalToken(Token.FROM)) {
                getLexer().nextToken();
            }

            if (getLexer().identifierEquals("ONLY")) {
                getLexer().nextToken();
                accept(Token.LEFT_PAREN);

                SQLName tableName = exprParser.name();
                deleteStatement.setTableName(tableName);

                accept(Token.RIGHT_PAREN);
            } else {
                SQLName tableName = exprParser.name();
                deleteStatement.setTableName(tableName);
            }

            deleteStatement.setAlias(as());
        }

        if (getLexer().equalToken(Token.WHERE)) {
            getLexer().nextToken();
            deleteStatement.setWhere(this.exprParser.expr());
        }

        if (getLexer().equalToken(Token.RETURNING)) {
            OracleReturningClause clause = this.parseReturningClause();
            deleteStatement.setReturning(clause);
        }
        if (getLexer().identifierEquals("RETURN") || getLexer().identifierEquals("RETURNING")) {
            throw new ParserUnsupportedException(getLexer().getToken());
        }

        if (getLexer().identifierEquals("LOG")) {
            throw new ParserUnsupportedException(getLexer().getToken());
        }

        return deleteStatement;
    }

    public SQLStatement parseCreateDbLink() {
        accept(Token.CREATE);

        OracleCreateDatabaseDbLinkStatement dbLink = new OracleCreateDatabaseDbLinkStatement();

        if (getLexer().identifierEquals("SHARED")) {
            dbLink.setShared(true);
            getLexer().nextToken();
        }

        if (getLexer().identifierEquals("PUBLIC")) {
            dbLink.set_public(true);
            getLexer().nextToken();
        }

        accept(Token.DATABASE);
        acceptIdentifier("LINK");

        dbLink.setName(this.exprParser.name());

        if (getLexer().equalToken(Token.CONNECT)) {
            getLexer().nextToken();
            accept(Token.TO);

            dbLink.setUser(this.exprParser.name());

            if (getLexer().equalToken(Token.IDENTIFIED)) {
                getLexer().nextToken();
                accept(Token.BY);
                dbLink.setPassword(getLexer().getLiterals());
                accept(Token.IDENTIFIER);
            }
        }

        if (getLexer().identifierEquals("AUTHENTICATED")) {
            getLexer().nextToken();
            accept(Token.BY);
            dbLink.setAuthenticatedUser(this.exprParser.name());

            accept(Token.IDENTIFIED);
            accept(Token.BY);
            dbLink.setPassword(getLexer().getLiterals());
            accept(Token.IDENTIFIER);
        }

        if (getLexer().equalToken(Token.USING)) {
            getLexer().nextToken();
            dbLink.setUsing(this.exprParser.expr());
        }

        return dbLink;
    }

    public OracleCreateIndexStatement parseCreateIndex(boolean acceptCreate) {
        if (acceptCreate) {
            accept(Token.CREATE);
        }

        OracleCreateIndexStatement stmt = new OracleCreateIndexStatement();
        if (getLexer().equalToken(Token.UNIQUE)) {
            stmt.setType("UNIQUE");
            getLexer().nextToken();
        } else if (getLexer().identifierEquals("BITMAP")) {
            stmt.setType("BITMAP");
            getLexer().nextToken();
        }

        accept(Token.INDEX);

        stmt.setName(this.exprParser.name());

        accept(Token.ON);

        stmt.setTable(this.exprParser.name());

        accept(Token.LEFT_PAREN);

        while (true) {
            SQLSelectOrderByItem item = this.exprParser.parseSelectOrderByItem();
            stmt.getItems().add(item);
            if (getLexer().equalToken(Token.COMMA)) {
                getLexer().nextToken();
                continue;
            }
            break;
        }
        accept(Token.RIGHT_PAREN);

        while (true) {
            if (getLexer().equalToken(Token.TABLESPACE)) {
                getLexer().nextToken();
                stmt.setTablespace(this.exprParser.name());
            } else if (getLexer().equalToken(Token.PCTFREE)) {
                getLexer().nextToken();
                stmt.setPtcfree(this.exprParser.expr());
            } else if (getLexer().equalToken(Token.INITRANS)) {
                getLexer().nextToken();
                stmt.setInitrans(this.exprParser.expr());
            } else if (getLexer().equalToken(Token.MAXTRANS)) {
                getLexer().nextToken();
                stmt.setMaxtrans(this.exprParser.expr());
            } else if (getLexer().equalToken(Token.COMPUTE)) {
                getLexer().nextToken();
                acceptIdentifier("STATISTICS");
                stmt.setComputeStatistics(true);
            } else if (getLexer().equalToken(Token.ENABLE)) {
                getLexer().nextToken();
                stmt.setEnable(true);
            } else if (getLexer().equalToken(Token.DISABLE)) {
                getLexer().nextToken();
                stmt.setEnable(false);
            } else if (getLexer().identifierEquals("ONLINE")) {
                getLexer().nextToken();
                stmt.setOnline(true);
            } else if (getLexer().identifierEquals("NOPARALLEL")) {
                getLexer().nextToken();
                stmt.setNoParallel(true);
            } else if (getLexer().identifierEquals("PARALLEL")) {
                getLexer().nextToken();
                stmt.setParallel(this.exprParser.expr());
            } else if (getLexer().equalToken(Token.INDEX)) {
                getLexer().nextToken();
                acceptIdentifier("ONLY");
                acceptIdentifier("TOPLEVEL");
                stmt.setIndexOnlyTopLevel(true);
            } else {
                break;
            }
        }
        return stmt;
    }

    public OracleCreateSequenceStatement parseCreateSequence(boolean acceptCreate) {
        if (acceptCreate) {
            accept(Token.CREATE);
        }

        accept(Token.SEQUENCE);

        OracleCreateSequenceStatement stmt = new OracleCreateSequenceStatement(exprParser.name());

        while (true) {
            if (getLexer().equalToken(Token.START)) {
                getLexer().nextToken();
                accept(Token.WITH);
                stmt.setStartWith(this.exprParser.expr());
                continue;
            } else if (getLexer().identifierEquals("INCREMENT")) {
                getLexer().nextToken();
                accept(Token.BY);
                stmt.setIncrementBy(this.exprParser.expr());
                continue;
            } else if (getLexer().equalToken(Token.CACHE)) {
                getLexer().nextToken();
                stmt.setCache(Boolean.TRUE);
                continue;
            } else if (getLexer().equalToken(Token.NOCACHE)) {
                getLexer().nextToken();
                stmt.setCache(Boolean.FALSE);
                continue;
            } else if (getLexer().identifierEquals("CYCLE")) {
                getLexer().nextToken();
                stmt.setCycle(Boolean.TRUE);
                continue;
            } else if (getLexer().identifierEquals("NOCYCLE")) {
                getLexer().nextToken();
                stmt.setCycle(Boolean.FALSE);
                continue;
            } else if (getLexer().identifierEquals("MINVALUE")) {
                getLexer().nextToken();
                stmt.setMinValue(this.exprParser.expr());
                continue;
            } else if (getLexer().identifierEquals("MAXVALUE")) {
                getLexer().nextToken();
                stmt.setMaxValue(this.exprParser.expr());
                continue;
            } else if (getLexer().identifierEquals("NOMAXVALUE")) {
                getLexer().nextToken();
                stmt.setNoMaxValue(true);
                continue;
            } else if (getLexer().identifierEquals("NOMINVALUE")) {
                getLexer().nextToken();
                stmt.setNoMinValue(true);
                continue;
            }
            break;
        }

        return stmt;
    }

    public OracleCreateProcedureStatement parseCreateProcedure() {
        OracleCreateProcedureStatement stmt = new OracleCreateProcedureStatement();
        accept(Token.CREATE);
        if (getLexer().equalToken(Token.OR)) {
            getLexer().nextToken();
            accept(Token.REPLACE);
            stmt.setOrReplace(true);
        }

        accept(Token.PROCEDURE);

        stmt.setName(this.exprParser.name());

        if (getLexer().equalToken(Token.LEFT_PAREN)) {
            getLexer().nextToken();
            parserParameters(stmt.getParameters());
            accept(Token.RIGHT_PAREN);
        }

        accept(Token.AS);

        OracleBlockStatement block = this.parseBlock();

        stmt.setBlock(block);

        return stmt;
    }

    public SQLUpdateStatement parseUpdateStatement() {
        return new OracleUpdateParser(this.getLexer()).parseUpdateStatement();
    }
}
