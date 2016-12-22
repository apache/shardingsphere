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

import com.alibaba.druid.sql.ast.SQLCommentHint;
import com.alibaba.druid.sql.ast.SQLDataTypeImpl;
import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.SQLName;
import com.alibaba.druid.sql.ast.SQLOrderBy;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.expr.SQLBinaryOpExpr;
import com.alibaba.druid.sql.ast.expr.SQLBinaryOperator;
import com.alibaba.druid.sql.ast.expr.SQLCharExpr;
import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.expr.SQLIntegerExpr;
import com.alibaba.druid.sql.ast.expr.SQLLiteralExpr;
import com.alibaba.druid.sql.ast.expr.SQLNCharExpr;
import com.alibaba.druid.sql.ast.expr.SQLQueryExpr;
import com.alibaba.druid.sql.ast.expr.SQLVariantRefExpr;
import com.alibaba.druid.sql.ast.statement.SQLAlterTableAddConstraint;
import com.alibaba.druid.sql.ast.statement.SQLAlterTableAddIndex;
import com.alibaba.druid.sql.ast.statement.SQLAlterTableAlterColumn;
import com.alibaba.druid.sql.ast.statement.SQLAlterTableDisableConstraint;
import com.alibaba.druid.sql.ast.statement.SQLAlterTableDisableKeys;
import com.alibaba.druid.sql.ast.statement.SQLAlterTableDropColumnItem;
import com.alibaba.druid.sql.ast.statement.SQLAlterTableDropConstraint;
import com.alibaba.druid.sql.ast.statement.SQLAlterTableDropForeignKey;
import com.alibaba.druid.sql.ast.statement.SQLAlterTableDropIndex;
import com.alibaba.druid.sql.ast.statement.SQLAlterTableDropPrimaryKey;
import com.alibaba.druid.sql.ast.statement.SQLAlterTableEnableConstraint;
import com.alibaba.druid.sql.ast.statement.SQLAlterTableEnableKeys;
import com.alibaba.druid.sql.ast.statement.SQLAlterTableStatement;
import com.alibaba.druid.sql.ast.statement.SQLColumnDefinition;
import com.alibaba.druid.sql.ast.statement.SQLCreateDatabaseStatement;
import com.alibaba.druid.sql.ast.statement.SQLExprTableSource;
import com.alibaba.druid.sql.ast.statement.SQLInsertStatement;
import com.alibaba.druid.sql.ast.statement.SQLInsertStatement.ValuesClause;
import com.alibaba.druid.sql.ast.statement.SQLPrimaryKey;
import com.alibaba.druid.sql.ast.statement.SQLSelect;
import com.alibaba.druid.sql.ast.statement.SQLSelectOrderByItem;
import com.alibaba.druid.sql.ast.statement.SQLSelectStatement;
import com.alibaba.druid.sql.ast.statement.SQLSetStatement;
import com.alibaba.druid.sql.ast.statement.SQLTableSource;
import com.alibaba.druid.sql.ast.statement.SQLUpdateSetItem;
import com.alibaba.druid.sql.ast.statement.SQLUpdateStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.MysqlForeignKey;
import com.alibaba.druid.sql.dialect.mysql.ast.clause.MySqlCaseStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.clause.MySqlCaseStatement.MySqlWhenStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.clause.MySqlCreateProcedureStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.clause.MySqlCursorDeclareStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.clause.MySqlDeclareStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.clause.MySqlElseStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.clause.MySqlIfStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.clause.MySqlIfStatement.MySqlElseIfStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.clause.MySqlIterateStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.clause.MySqlLeaveStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.clause.MySqlLoopStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.clause.MySqlParameter;
import com.alibaba.druid.sql.dialect.mysql.ast.clause.MySqlParameter.ParameterType;
import com.alibaba.druid.sql.dialect.mysql.ast.clause.MySqlRepeatStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.clause.MySqlSelectIntoStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.clause.MySqlWhileStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.CobarShowStatus;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlAlterTableAddColumn;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlAlterTableChangeColumn;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlAlterTableCharacter;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlAlterTableDiscardTablespace;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlAlterTableImportTablespace;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlAlterTableModifyColumn;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlAlterTableOption;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlAlterTableStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlAlterUserStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlAnalyzeStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlBinlogStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlBlockStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlCommitStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlCreateIndexStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlCreateTableStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlCreateUserStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlDeleteStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlDescribeStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlExecuteStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlHelpStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlHintStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlInsertStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlKillStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlLoadDataInFileStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlLoadXmlStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlLockTableStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlLockTableStatement.LockType;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlOptimizeStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlPrepareStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlRenameTableStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlReplaceStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlResetStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlRollbackStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlSelectQueryBlock.Limit;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlSetCharSetStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlSetNamesStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlSetPasswordStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlSetTransactionStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlShowAuthorsStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlShowBinLogEventsStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlShowBinaryLogsStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlShowCharacterSetStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlShowCollationStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlShowColumnsStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlShowContributorsStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlShowCreateDatabaseStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlShowCreateEventStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlShowCreateFunctionStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlShowCreateProcedureStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlShowCreateTableStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlShowCreateTriggerStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlShowCreateViewStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlShowDatabasesStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlShowEngineStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlShowEnginesStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlShowErrorsStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlShowEventsStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlShowFunctionCodeStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlShowFunctionStatusStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlShowGrantsStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlShowIndexesStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlShowKeysStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlShowMasterLogsStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlShowMasterStatusStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlShowOpenTablesStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlShowPluginsStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlShowPrivilegesStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlShowProcedureCodeStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlShowProcedureStatusStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlShowProcessListStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlShowProfileStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlShowProfilesStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlShowRelayLogEventsStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlShowSlaveHostsStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlShowSlaveStatusStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlShowStatusStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlShowTableStatusStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlShowTablesStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlShowTriggersStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlShowVariantsStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlShowWarningsStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlStartTransactionStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlUnlockTablesStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlUpdateStatement;
import com.alibaba.druid.sql.lexer.Token;
import com.alibaba.druid.sql.parser.ParserException;
import com.alibaba.druid.sql.parser.ParserUnsupportedException;
import com.alibaba.druid.sql.parser.SQLSelectParser;
import com.alibaba.druid.sql.parser.SQLStatementParser;
import com.alibaba.druid.util.JdbcConstants;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class MySqlStatementParser extends SQLStatementParser {
    
    public MySqlStatementParser(final String sql) {
        super(new MySqlExprParser(sql));
    }
    
    @Override
    protected SQLSelectStatement parseSelect() {
        return new SQLSelectStatement(new MySqlSelectParser(exprParser).select(), JdbcConstants.MYSQL);
    }
    
    @Override
    protected SQLSelectParser createSQLSelectParser() {
        return new MySqlSelectParser(exprParser);
    }
    
    public SQLUpdateStatement parseUpdateStatement() {
        MySqlUpdateStatement stmt = createUpdateStatement();

        if (getLexer().equalToken(Token.UPDATE)) {
            getLexer().nextToken();

            if (getLexer().identifierEquals(MySqlKeyword.LOW_PRIORITY)) {
                getLexer().nextToken();
                stmt.setLowPriority(true);
            }

            if (getLexer().identifierEquals(MySqlKeyword.IGNORE)) {
                getLexer().nextToken();
                stmt.setIgnore(true);
            }

            SQLTableSource tableSource = this.exprParser.createSelectParser().parseTableSource();
            stmt.setTableSource(tableSource);
        }

        parseUpdateSet(stmt);

        if (getLexer().equalToken(Token.WHERE)) {
            getLexer().nextToken();
            stmt.setWhere(this.exprParser.expr());
        }

        stmt.setOrderBy(this.exprParser.parseOrderBy());

        stmt.setLimit(parseLimit());

        return stmt;
    }

    protected MySqlUpdateStatement createUpdateStatement() {
        return new MySqlUpdateStatement();
    }

    public MySqlDeleteStatement parseDeleteStatement() {
        MySqlDeleteStatement deleteStatement = new MySqlDeleteStatement();
        if (getLexer().equalToken(Token.DELETE)) {
            getLexer().nextToken();

            if (getLexer().equalToken(Token.COMMENT)) {
                getLexer().nextToken();
            }

            if (getLexer().identifierEquals(MySqlKeyword.LOW_PRIORITY)) {
                deleteStatement.setLowPriority(true);
                getLexer().nextToken();
            }

            if (getLexer().identifierEquals("QUICK")) {
                deleteStatement.setQuick(true);
                getLexer().nextToken();
            }

            if (getLexer().identifierEquals(MySqlKeyword.IGNORE)) {
                deleteStatement.setIgnore(true);
                getLexer().nextToken();
            }

            if (getLexer().equalToken(Token.IDENTIFIER)) {
                deleteStatement.setTableSource(createSQLSelectParser().parseTableSource());

                if (getLexer().equalToken(Token.FROM)) {
                    getLexer().nextToken();
                    SQLTableSource tableSource = createSQLSelectParser().parseTableSource();
                    deleteStatement.setFrom(tableSource);
                }
            } else if (getLexer().equalToken(Token.FROM)) {
                getLexer().nextToken();
                deleteStatement.setTableSource(createSQLSelectParser().parseTableSource());
            } else {
                throw new ParserException(getLexer());
            }

            if (getLexer().identifierEquals("USING")) {
                getLexer().nextToken();

                SQLTableSource tableSource = createSQLSelectParser().parseTableSource();
                deleteStatement.setUsing(tableSource);
            }
        }

        if (getLexer().equalToken(Token.WHERE)) {
            getLexer().nextToken();
            SQLExpr where = this.exprParser.expr();
            deleteStatement.setWhere(where);
        }

        if (getLexer().equalToken(Token.ORDER)) {
            SQLOrderBy orderBy = exprParser.parseOrderBy();
            deleteStatement.setOrderBy(orderBy);
        }

        deleteStatement.setLimit(parseLimit());

        return deleteStatement;
    }
    
    public SQLStatement parseCreate() {
        int currentPosition = getLexer().getCurrentPosition();
        accept(Token.CREATE);
        boolean replace = false;
        if (getLexer().equalToken(Token.OR)) {
            getLexer().nextToken();
            accept(Token.REPLACE);
            replace = true;
        }
        List<SQLCommentHint> hints = this.exprParser.parseHints();

        if (getLexer().equalToken(Token.TABLE) || getLexer().identifierEquals(MySqlKeyword.TEMPORARY)) {
            if (replace) {
                getLexer().setCurrentPosition(currentPosition);
                getLexer().setToken(Token.CREATE);
            }
            MySqlCreateTableParser parser = new MySqlCreateTableParser(this.exprParser);
            MySqlCreateTableStatement stmt = parser.parseCrateTable(false);
            stmt.setHints(hints);
            return stmt;
        }

        if (getLexer().equalToken(Token.DATABASE)) {
            if (replace) {
                getLexer().setCurrentPosition(currentPosition);
                getLexer().setToken(Token.CREATE);
            }
            return parseCreateDatabase();
        }

        if (getLexer().equalToken(Token.UNIQUE) || getLexer().equalToken(Token.INDEX) || getLexer().identifierEquals(MySqlKeyword.FULLTEXT)
            || getLexer().identifierEquals(MySqlKeyword.SPATIAL)) {
            if (replace) {
                getLexer().setCurrentPosition(currentPosition);
                getLexer().setToken(Token.CREATE);
            }
            return parseCreateIndex(false);
        }

        if (getLexer().equalToken(Token.USER)) {
            if (replace) {
                getLexer().setCurrentPosition(currentPosition);
                getLexer().setToken(Token.CREATE);
            }
            return parseCreateUser();
        }

        if (getLexer().equalToken(Token.VIEW)) {
            if (replace) {
                getLexer().setCurrentPosition(currentPosition);
                getLexer().setToken(Token.CREATE);
            }
            return parseCreateView();
        }

        if (getLexer().equalToken(Token.TRIGGER)) {
            if (replace) {
                getLexer().setCurrentPosition(currentPosition);
                getLexer().setToken(Token.CREATE);
            }
            return parseCreateTrigger();
        }
        //parse create procedure
         if (getLexer().equalToken(Token.PROCEDURE)) {
             if (replace) {
                 getLexer().setCurrentPosition(currentPosition);
                 getLexer().setToken(Token.CREATE);
             }
             return parseCreateProcedure();
         }
        throw new ParserUnsupportedException(getLexer().getToken());
    }

    public SQLStatement parseCreateIndex(boolean acceptCreate) {
        if (acceptCreate) {
            accept(Token.CREATE);
        }

        MySqlCreateIndexStatement stmt = new MySqlCreateIndexStatement();

        if (getLexer().equalToken(Token.UNIQUE)) {
            stmt.setType("UNIQUE");
            getLexer().nextToken();
        } else if (getLexer().identifierEquals(MySqlKeyword.FULLTEXT)) {
            stmt.setType(MySqlKeyword.FULLTEXT);
            getLexer().nextToken();
        } else if (getLexer().identifierEquals(MySqlKeyword.SPATIAL)) {
            stmt.setType(MySqlKeyword.SPATIAL);
            getLexer().nextToken();
        }

        accept(Token.INDEX);

        stmt.setName(this.exprParser.name());

        parseCreateIndexUsing(stmt);

        accept(Token.ON);

        stmt.setTable(this.exprParser.name());

        accept(Token.LEFT_PAREN);

        while (true) {
            SQLSelectOrderByItem item = this.exprParser.parseSelectOrderByItem();
            item.setParent(stmt);
            stmt.getItems().add(item);
            if (getLexer().equalToken(Token.COMMA)) {
                getLexer().nextToken();
                continue;
            }
            break;
        }
        accept(Token.RIGHT_PAREN);

        parseCreateIndexUsing(stmt);

        return stmt;
    }

    private void parseCreateIndexUsing(MySqlCreateIndexStatement stmt) {
        if (getLexer().identifierEquals("USING")) {
            getLexer().nextToken();

            if (getLexer().identifierEquals("BTREE")) {
                stmt.setUsing("BTREE");
                getLexer().nextToken();
            } else if (getLexer().identifierEquals("HASH")) {
                stmt.setUsing("HASH");
                getLexer().nextToken();
            } else {
                throw new ParserUnsupportedException(getLexer().getToken());
            }
        }
    }

    public SQLStatement parseCreateUser() {
        if (getLexer().equalToken(Token.CREATE)) {
            getLexer().nextToken();
        }

        accept(Token.USER);

        MySqlCreateUserStatement stmt = new MySqlCreateUserStatement();

        while (true) {
            MySqlCreateUserStatement.UserSpecification userSpec = new MySqlCreateUserStatement.UserSpecification();

            SQLExpr expr = exprParser.primary();
            userSpec.setUser(expr);

            if (getLexer().equalToken(Token.IDENTIFIED)) {
                getLexer().nextToken();
                if (getLexer().equalToken(Token.BY)) {
                    getLexer().nextToken();

                    if (getLexer().identifierEquals("PASSWORD")) {
                        getLexer().nextToken();
                        userSpec.setPasswordHash(true);
                    }

                    SQLCharExpr password = (SQLCharExpr) this.exprParser.expr();
                    userSpec.setPassword(password);
                } else if (getLexer().equalToken(Token.WITH)) {
                    getLexer().nextToken();
                    userSpec.setAuthPlugin(this.exprParser.expr());
                }
            }

            stmt.getUsers().add(userSpec);

            if (getLexer().equalToken(Token.COMMA)) {
                getLexer().nextToken();
                continue;
            }

            break;
        }

        return stmt;
    }

    public SQLStatement parseKill() {
        accept(Token.KILL);

        MySqlKillStatement stmt = new MySqlKillStatement();

        if (getLexer().identifierEquals("CONNECTION")) {
            stmt.setType(MySqlKillStatement.Type.CONNECTION);
            getLexer().nextToken();
        } else if (getLexer().identifierEquals("QUERY")) {
            stmt.setType(MySqlKillStatement.Type.QUERY);
            getLexer().nextToken();
        } else {
            throw new ParserUnsupportedException(getLexer().getToken());
        }

        SQLExpr threadId = this.exprParser.expr();
        stmt.setThreadId(threadId);

        return stmt;
    }

    public SQLStatement parseBinlog() {
        acceptIdentifier("binlog");

        MySqlBinlogStatement stmt = new MySqlBinlogStatement();

        SQLExpr expr = this.exprParser.expr();
        stmt.setExpr(expr);

        return stmt;
    }

    public MySqlAnalyzeStatement parseAnalyze() {
        accept(Token.ANALYZE);
        accept(Token.TABLE);

        MySqlAnalyzeStatement stmt = new MySqlAnalyzeStatement();
        List<SQLName> names = new ArrayList<>();
        this.exprParser.names(names, stmt);

        for (SQLName name : names) {
            stmt.getTableSources().add(new SQLExprTableSource(name));
        }
        return stmt;
    }

    public MySqlOptimizeStatement parseOptimize() {
        accept(Token.OPTIMIZE);
        accept(Token.TABLE);

        MySqlOptimizeStatement stmt = new MySqlOptimizeStatement();
        List<SQLName> names = new ArrayList<>();
        this.exprParser.names(names, stmt);

        for (SQLName name : names) {
            stmt.getTableSources().add(new SQLExprTableSource(name));
        }
        return stmt;
    }

    public SQLStatement parseReset() {
        acceptIdentifier(MySqlKeyword.RESET);

        MySqlResetStatement stmt = new MySqlResetStatement();

        while (true) {
            if (getLexer().equalToken(Token.IDENTIFIER)) {
                if (getLexer().identifierEquals("QUERY")) {
                    getLexer().nextToken();
                    accept(Token.CACHE);
                    stmt.getOptions().add("QUERY CACHE");
                } else {
                    stmt.getOptions().add(getLexer().getLiterals());
                    getLexer().nextToken();
                }

                if (getLexer().equalToken(Token.COMMA)) {
                    getLexer().nextToken();
                    continue;
                }
            }
            break;
        }

        return stmt;
    }
    
    @Override
    public boolean parseStatementListDialect(final List<SQLStatement> statementList) {
        if (getLexer().equalToken(Token.KILL)) {
            statementList.add(parseKill());
            return true;
        }
        if (getLexer().equalToken(Token.REPLACE)) {
            statementList.add(parseReplicate());
            return true;
        }
        if (getLexer().equalToken(Token.SHOW)) {
            statementList.add(parseShow());
            return true;
        }
        if (getLexer().equalToken(Token.ANALYZE)) {
            statementList.add(parseAnalyze());
            return true;
        }
        if (getLexer().equalToken(Token.OPTIMIZE)) {
            statementList.add(parseOptimize());
            return true;
        }
        if (getLexer().identifierEquals(MySqlKeyword.BINLOG)) {
            statementList.add(parseBinlog());
            return true;
        }
        if (getLexer().identifierEquals(MySqlKeyword.RESET)) {
            statementList.add(parseReset());
            return true;
        }
        if (getLexer().equalToken(Token.HINT)) {
            statementList.add(parseHint());
            return true;
        }
        if (getLexer().equalToken(Token.BEGIN)) {
            statementList.add(parseBlock());
            return true;
        }
        if (getLexer().identifierEquals("PREPARE")) {
            statementList.add(parsePrepare());
            return true;
        }
        if (getLexer().identifierEquals("EXECUTE")) {
            statementList.add(parseExecute());
            return true;
        }
        if (getLexer().identifierEquals("LOAD")) {
            statementList.add(parseLoad());
            return true;
        }
        if (getLexer().identifierEquals("START")) {
            statementList.add(parseStart());
            return true;
        }
        if (getLexer().identifierEquals("HELP")) {
            getLexer().nextToken();
            MySqlHelpStatement stmt = new MySqlHelpStatement();
            stmt.setContent(exprParser.primary());
            statementList.add(stmt);
            return true;
        }
        if (getLexer().equalToken(Token.DESC) || getLexer().identifierEquals(MySqlKeyword.DESCRIBE)) {
            statementList.add(parseDescribe());
            return true;
        }
        if (getLexer().equalToken(Token.LOCK)) {
            getLexer().nextToken();
            acceptIdentifier(MySqlKeyword.TABLES);
            MySqlLockTableStatement stmt = new MySqlLockTableStatement();
            stmt.setTableSource(this.exprParser.name());
            if (getLexer().identifierEquals(MySqlKeyword.READ)) {
                getLexer().nextToken();
                if (getLexer().identifierEquals(MySqlKeyword.LOCAL)) {
                    getLexer().nextToken();
                    stmt.setLockType(LockType.READ_LOCAL);
                } else {
                    stmt.setLockType(LockType.READ);
                }
            } else if (getLexer().identifierEquals(MySqlKeyword.WRITE)) {
                stmt.setLockType(LockType.WRITE);
            } else if (getLexer().identifierEquals(MySqlKeyword.LOW_PRIORITY)) {
                getLexer().nextToken();
                acceptIdentifier(MySqlKeyword.WRITE);
                stmt.setLockType(LockType.LOW_PRIORITY_WRITE);
            }
            if (getLexer().equalToken(Token.HINT)) {
                stmt.setHints(this.exprParser.parseHints());
            }
            statementList.add(stmt);
            return true;
        }
        if (getLexer().identifierEquals("UNLOCK")) {
            getLexer().nextToken();
            acceptIdentifier(MySqlKeyword.TABLES);
            statementList.add(new MySqlUnlockTablesStatement());
            return true;
        }
        return false;
    }
    
    public MySqlBlockStatement parseBlock() {
        MySqlBlockStatement block = new MySqlBlockStatement();
        accept(Token.BEGIN);
        parseProcedureStatementList(block.getStatementList());
        accept(Token.END);
        return block;
    }

    public MySqlDescribeStatement parseDescribe() {
        if (getLexer().equalToken(Token.DESC) || getLexer().identifierEquals(MySqlKeyword.DESCRIBE)) {
            getLexer().nextToken();
        } else {
            throw new ParserException(getLexer(), Token.DESC);
        }

        MySqlDescribeStatement stmt = new MySqlDescribeStatement();
        stmt.setObject(this.exprParser.name());
        if (getLexer().equalToken(Token.IDENTIFIER)) {
            stmt.setColName(this.exprParser.name());
        }
        return stmt;
    }

    public SQLStatement parseShow() {
        accept(Token.SHOW);

        if (getLexer().equalToken(Token.COMMENT)) {
            getLexer().nextToken();
        }

        boolean full = false;
        if (getLexer().equalToken(Token.FULL)) {
            getLexer().nextToken();
            full = true;
        }

        if (getLexer().identifierEquals("PROCESSLIST")) {
            getLexer().nextToken();
            MySqlShowProcessListStatement stmt = new MySqlShowProcessListStatement();
            stmt.setFull(full);
            return stmt;
        }

        if (getLexer().identifierEquals("COLUMNS") || getLexer().identifierEquals("FIELDS")) {
            getLexer().nextToken();

            MySqlShowColumnsStatement stmt = parseShowColumns();
            stmt.setFull(full);
            return stmt;
        }

        if (getLexer().identifierEquals("COLUMNS")) {
            getLexer().nextToken();
            return parseShowColumns();
        }

        if (getLexer().identifierEquals(MySqlKeyword.TABLES)) {
            getLexer().nextToken();

            MySqlShowTablesStatement stmt = parseShowTabless();
            stmt.setFull(full);
            return stmt;
        }
        if (getLexer().identifierEquals("DATABASES")) {
            getLexer().nextToken();
            return parseShowDatabases();
        }
        if (getLexer().identifierEquals("WARNINGS")) {
            getLexer().nextToken();
            return parseShowWarnings();
        }
        if (getLexer().identifierEquals("COUNT")) {
            getLexer().nextToken();
            accept(Token.LEFT_PAREN);
            accept(Token.STAR);
            accept(Token.RIGHT_PAREN);

            if (getLexer().identifierEquals(MySqlKeyword.ERRORS)) {
                getLexer().nextToken();

                MySqlShowErrorsStatement stmt = new MySqlShowErrorsStatement();
                stmt.setCount(true);

                return stmt;
            } else {
                acceptIdentifier("WARNINGS");

                MySqlShowWarningsStatement stmt = new MySqlShowWarningsStatement();
                stmt.setCount(true);

                return stmt;
            }
        }

        if (getLexer().identifierEquals(MySqlKeyword.ERRORS)) {
            getLexer().nextToken();

            MySqlShowErrorsStatement stmt = new MySqlShowErrorsStatement();
            stmt.setLimit(parseLimit());

            return stmt;
        }

        if (getLexer().identifierEquals(MySqlKeyword.STATUS)) {
            getLexer().nextToken();
            return parseShowStatus();
        }

        if (getLexer().identifierEquals(MySqlKeyword.VARIABLES)) {
            getLexer().nextToken();
            return parseShowVariants();
        }

        if (getLexer().identifierEquals(MySqlKeyword.GLOBAL)) {
            getLexer().nextToken();
            if (getLexer().identifierEquals(MySqlKeyword.STATUS)) {
                getLexer().nextToken();
                MySqlShowStatusStatement stmt = parseShowStatus();
                stmt.setGlobal(true);
                return stmt;
            }
            if (getLexer().identifierEquals(MySqlKeyword.VARIABLES)) {
                getLexer().nextToken();
                MySqlShowVariantsStatement stmt = parseShowVariants();
                stmt.setGlobal(true);
                return stmt;
            }
        }
        if (getLexer().identifierEquals(MySqlKeyword.SESSION)) {
            getLexer().nextToken();
            if (getLexer().identifierEquals(MySqlKeyword.STATUS)) {
                getLexer().nextToken();
                MySqlShowStatusStatement stmt = parseShowStatus();
                stmt.setSession(true);
                return stmt;
            }

            if (getLexer().identifierEquals(MySqlKeyword.VARIABLES)) {
                getLexer().nextToken();
                MySqlShowVariantsStatement stmt = parseShowVariants();
                stmt.setSession(true);
                return stmt;
            }
        }

        if (getLexer().identifierEquals("COBAR_STATUS")) {
            getLexer().nextToken();
            return new CobarShowStatus();
        }

        if (getLexer().identifierEquals("AUTHORS")) {
            getLexer().nextToken();
            return new MySqlShowAuthorsStatement();
        }

        if (getLexer().equalToken(Token.BINARY)) {
            getLexer().nextToken();
            acceptIdentifier("LOGS");
            return new MySqlShowBinaryLogsStatement();
        }

        if (getLexer().identifierEquals("MASTER")) {
            getLexer().nextToken();
            if (getLexer().identifierEquals("LOGS")) {
                getLexer().nextToken();
                return new MySqlShowMasterLogsStatement();
            }
            acceptIdentifier(MySqlKeyword.STATUS);
            return new MySqlShowMasterStatusStatement();
        }

        if (getLexer().identifierEquals(MySqlKeyword.CHARACTER)) {
            getLexer().nextToken();
            accept(Token.SET);
            MySqlShowCharacterSetStatement stmt = new MySqlShowCharacterSetStatement();

            if (getLexer().equalToken(Token.LIKE)){
                getLexer().nextToken();
                stmt.setPattern(this.exprParser.expr());
            }

            if (getLexer().equalToken(Token.WHERE)) {
                getLexer().nextToken();
                stmt.setWhere(this.exprParser.expr());
            }

            return stmt;
        }

        if (getLexer().identifierEquals("COLLATION")) {
            getLexer().nextToken();
            MySqlShowCollationStatement stmt = new MySqlShowCollationStatement();

            if (getLexer().equalToken(Token.LIKE)) {
                getLexer().nextToken();
                stmt.setPattern(this.exprParser.expr());
            }

            if (getLexer().equalToken(Token.WHERE)) {
                getLexer().nextToken();
                stmt.setWhere(this.exprParser.expr());
            }

            return stmt;
        }

        if (getLexer().identifierEquals(MySqlKeyword.BINLOG)) {
            getLexer().nextToken();
            acceptIdentifier(MySqlKeyword.EVENTS);
            MySqlShowBinLogEventsStatement stmt = new MySqlShowBinLogEventsStatement();

            if (getLexer().equalToken(Token.IN)) {
                getLexer().nextToken();
                stmt.setIn(this.exprParser.expr());
            }

            if (getLexer().equalToken(Token.FROM)) {
                getLexer().nextToken();
                stmt.setFrom(this.exprParser.expr());
            }

            stmt.setLimit(parseLimit());

            return stmt;
        }

        if (getLexer().identifierEquals("CONTRIBUTORS")) {
            getLexer().nextToken();
            return new MySqlShowContributorsStatement();
        }

        if (getLexer().equalToken(Token.CREATE)) {
            getLexer().nextToken();

            if (getLexer().equalToken(Token.DATABASE)) {
                getLexer().nextToken();

                MySqlShowCreateDatabaseStatement stmt = new MySqlShowCreateDatabaseStatement();
                stmt.setDatabase(this.exprParser.name());
                return stmt;
            }

            if (getLexer().identifierEquals("EVENT")) {
                getLexer().nextToken();

                MySqlShowCreateEventStatement stmt = new MySqlShowCreateEventStatement();
                stmt.setEventName(this.exprParser.name());
                return stmt;
            }

            if (getLexer().equalToken(Token.FUNCTION)) {
                getLexer().nextToken();

                MySqlShowCreateFunctionStatement stmt = new MySqlShowCreateFunctionStatement();
                stmt.setName(this.exprParser.name());
                return stmt;
            }

            if (getLexer().equalToken(Token.PROCEDURE)) {
                getLexer().nextToken();

                MySqlShowCreateProcedureStatement stmt = new MySqlShowCreateProcedureStatement();
                stmt.setName(this.exprParser.name());
                return stmt;
            }

            if (getLexer().equalToken(Token.TABLE)) {
                getLexer().nextToken();

                MySqlShowCreateTableStatement stmt = new MySqlShowCreateTableStatement();
                stmt.setName(this.exprParser.name());
                return stmt;
            }

            if (getLexer().equalToken(Token.VIEW)) {
                getLexer().nextToken();

                MySqlShowCreateViewStatement stmt = new MySqlShowCreateViewStatement();
                stmt.setName(this.exprParser.name());
                return stmt;
            }

            if (getLexer().equalToken(Token.TRIGGER)) {
                getLexer().nextToken();

                MySqlShowCreateTriggerStatement stmt = new MySqlShowCreateTriggerStatement();
                stmt.setName(this.exprParser.name());
                return stmt;
            }
            throw new ParserUnsupportedException(getLexer().getToken());
        }

        if (getLexer().identifierEquals(MySqlKeyword.ENGINE)) {
            getLexer().nextToken();
            MySqlShowEngineStatement stmt = new MySqlShowEngineStatement();
            stmt.setName(this.exprParser.name());
            stmt.setOption(MySqlShowEngineStatement.Option.valueOf(getLexer().getLiterals().toUpperCase()));
            getLexer().nextToken();
            return stmt;
        }

        if (getLexer().identifierEquals("STORAGE")) {
            getLexer().nextToken();
            acceptIdentifier(MySqlKeyword.ENGINES);
            MySqlShowEnginesStatement stmt = new MySqlShowEnginesStatement();
            stmt.setStorage(true);
            return stmt;
        }

        if (getLexer().identifierEquals(MySqlKeyword.ENGINES)) {
            getLexer().nextToken();
            return new MySqlShowEnginesStatement();
        }

        if (getLexer().identifierEquals(MySqlKeyword.EVENTS)) {
            getLexer().nextToken();
            MySqlShowEventsStatement stmt = new MySqlShowEventsStatement();

            if (getLexer().equalToken(Token.FROM) || getLexer().equalToken(Token.IN)) {
                getLexer().nextToken();
                stmt.setSchema(this.exprParser.name());
            }

            if (getLexer().equalToken(Token.LIKE)) {
                getLexer().nextToken();
                stmt.setLike(this.exprParser.expr());
            }

            if (getLexer().equalToken(Token.WHERE)) {
                getLexer().nextToken();
                stmt.setWhere(this.exprParser.expr());
            }
            return stmt;
        }

        if (getLexer().equalToken(Token.FUNCTION)) {
            getLexer().nextToken();

            if (getLexer().identifierEquals("CODE")) {
                getLexer().nextToken();
                MySqlShowFunctionCodeStatement stmt = new MySqlShowFunctionCodeStatement();
                stmt.setName(this.exprParser.name());
                return stmt;
            }

            acceptIdentifier(MySqlKeyword.STATUS);
            MySqlShowFunctionStatusStatement stmt = new MySqlShowFunctionStatusStatement();

            if (getLexer().equalToken(Token.LIKE)) {
                getLexer().nextToken();
                stmt.setLike(this.exprParser.expr());
            }

            if (getLexer().equalToken(Token.WHERE)) {
                getLexer().nextToken();
                stmt.setWhere(this.exprParser.expr());
            }
            return stmt;
        }

        if (getLexer().identifierEquals(MySqlKeyword.ENGINE)) {
            getLexer().nextToken();
            MySqlShowEngineStatement stmt = new MySqlShowEngineStatement();
            stmt.setName(this.exprParser.name());
            stmt.setOption(MySqlShowEngineStatement.Option.valueOf(getLexer().getLiterals().toUpperCase()));
            getLexer().nextToken();
            return stmt;
        }

        if (getLexer().identifierEquals("STORAGE")) {
            getLexer().nextToken();
            acceptIdentifier(MySqlKeyword.ENGINES);
            MySqlShowEnginesStatement stmt = new MySqlShowEnginesStatement();
            stmt.setStorage(true);
            return stmt;
        }

        if (getLexer().identifierEquals(MySqlKeyword.ENGINES)) {
            getLexer().nextToken();
            return new MySqlShowEnginesStatement();
        }

        if (getLexer().identifierEquals("GRANTS")) {
            getLexer().nextToken();
            MySqlShowGrantsStatement stmt = new MySqlShowGrantsStatement();

            if (getLexer().equalToken(Token.FOR)) {
                getLexer().nextToken();
                stmt.setUser(this.exprParser.expr());
            }

            return stmt;
        }

        if (getLexer().equalToken(Token.INDEX) || getLexer().identifierEquals("INDEXES")) {
            getLexer().nextToken();
            MySqlShowIndexesStatement stmt = new MySqlShowIndexesStatement();

            if (getLexer().equalToken(Token.FROM) || getLexer().equalToken(Token.IN)) {
                getLexer().nextToken();
                SQLName table = exprParser.name();
                stmt.setTable(table);

                if (getLexer().equalToken(Token.FROM) || getLexer().equalToken(Token.IN)) {
                    getLexer().nextToken();
                    SQLName database = exprParser.name();
                    stmt.setDatabase(database);
                }
            }

            if (getLexer().equalToken(Token.HINT)) {
                stmt.setHints(this.exprParser.parseHints());
            }

            return stmt;
        }

        if (getLexer().identifierEquals("KEYS")) {
            getLexer().nextToken();
            MySqlShowKeysStatement stmt = new MySqlShowKeysStatement();

            if (getLexer().equalToken(Token.FROM) || getLexer().equalToken(Token.IN)) {
                getLexer().nextToken();
                SQLName table = exprParser.name();
                stmt.setTable(table);

                if (getLexer().equalToken(Token.FROM) || getLexer().equalToken(Token.IN)) {
                    getLexer().nextToken();
                    SQLName database = exprParser.name();
                    stmt.setDatabase(database);
                }
            }

            return stmt;
        }

        if (getLexer().equalToken(Token.OPEN) || getLexer().identifierEquals("OPEN")) {
            getLexer().nextToken();
            acceptIdentifier(MySqlKeyword.TABLES);
            MySqlShowOpenTablesStatement stmt = new MySqlShowOpenTablesStatement();

            if (getLexer().equalToken(Token.FROM) || getLexer().equalToken(Token.IN)) {
                getLexer().nextToken();
                stmt.setDatabase(this.exprParser.name());
            }

            if (getLexer().equalToken(Token.LIKE)) {
                getLexer().nextToken();
                stmt.setLike(this.exprParser.expr());
            }

            if (getLexer().equalToken(Token.WHERE)) {
                getLexer().nextToken();
                stmt.setWhere(this.exprParser.expr());
            }
            return stmt;
        }

        if (getLexer().identifierEquals("PLUGINS")) {
            getLexer().nextToken();
            return new MySqlShowPluginsStatement();
        }

        if (getLexer().identifierEquals("PRIVILEGES")) {
            getLexer().nextToken();
            return new MySqlShowPrivilegesStatement();
        }

        if (getLexer().equalToken(Token.PROCEDURE)) {
            getLexer().nextToken();

            if (getLexer().identifierEquals("CODE")) {
                getLexer().nextToken();
                MySqlShowProcedureCodeStatement stmt = new MySqlShowProcedureCodeStatement();
                stmt.setName(this.exprParser.name());
                return stmt;
            }

            acceptIdentifier(MySqlKeyword.STATUS);
            MySqlShowProcedureStatusStatement stmt = new MySqlShowProcedureStatusStatement();

            if (getLexer().equalToken(Token.LIKE)) {
                getLexer().nextToken();
                stmt.setLike(this.exprParser.expr());
            }

            if (getLexer().equalToken(Token.WHERE)) {
                getLexer().nextToken();
                stmt.setWhere(this.exprParser.expr());
            }
            return stmt;
        }
        if (getLexer().identifierEquals("PROCESSLIST")) {
            getLexer().nextToken();
            return new MySqlShowProcessListStatement();
        }
        if (getLexer().identifierEquals("PROFILES")) {
            getLexer().nextToken();
            return new MySqlShowProfilesStatement();
        }
        if (getLexer().identifierEquals("PROFILE")) {
            getLexer().nextToken();
            MySqlShowProfileStatement stmt = new MySqlShowProfileStatement();
            while (true) {
                if (getLexer().equalToken(Token.ALL)) {
                    stmt.getTypes().add(MySqlShowProfileStatement.Type.ALL);
                    getLexer().nextToken();
                } else if (getLexer().identifierEquals("BLOCK")) {
                    getLexer().nextToken();
                    acceptIdentifier("IO");
                    stmt.getTypes().add(MySqlShowProfileStatement.Type.BLOCK_IO);
                } else if (getLexer().identifierEquals("CONTEXT")) {
                    getLexer().nextToken();
                    acceptIdentifier("SWITCHES");
                    stmt.getTypes().add(MySqlShowProfileStatement.Type.CONTEXT_SWITCHES);
                } else if (getLexer().identifierEquals("CPU")) {
                    getLexer().nextToken();
                    stmt.getTypes().add(MySqlShowProfileStatement.Type.CPU);
                } else if (getLexer().identifierEquals("IPC")) {
                    getLexer().nextToken();
                    stmt.getTypes().add(MySqlShowProfileStatement.Type.IPC);
                } else if (getLexer().identifierEquals("MEMORY")) {
                    getLexer().nextToken();
                    stmt.getTypes().add(MySqlShowProfileStatement.Type.MEMORY);
                } else if (getLexer().identifierEquals("PAGE")) {
                    getLexer().nextToken();
                    acceptIdentifier("FAULTS");
                    stmt.getTypes().add(MySqlShowProfileStatement.Type.PAGE_FAULTS);
                } else if (getLexer().identifierEquals("SOURCE")) {
                    getLexer().nextToken();
                    stmt.getTypes().add(MySqlShowProfileStatement.Type.SOURCE);
                } else if (getLexer().identifierEquals("SWAPS")) {
                    getLexer().nextToken();
                    stmt.getTypes().add(MySqlShowProfileStatement.Type.SWAPS);
                } else {
                    break;
                }

                if (getLexer().equalToken(Token.COMMA)) {
                    getLexer().nextToken();
                    continue;
                }
                break;
            }

            if (getLexer().equalToken(Token.FOR)) {
                getLexer().nextToken();
                acceptIdentifier("QUERY");
                stmt.setForQuery(this.exprParser.primary());
            }
            stmt.setLimit(this.parseLimit());
            return stmt;
        }
        if (getLexer().identifierEquals("RELAYLOG")) {
            getLexer().nextToken();
            acceptIdentifier(MySqlKeyword.EVENTS);
            MySqlShowRelayLogEventsStatement stmt = new MySqlShowRelayLogEventsStatement();

            if (getLexer().equalToken(Token.IN)) {
                getLexer().nextToken();
                stmt.setLogName(this.exprParser.primary());
            }

            if (getLexer().equalToken(Token.FROM)) {
                getLexer().nextToken();
                stmt.setFrom(this.exprParser.primary());
            }

            stmt.setLimit(this.parseLimit());

            return stmt;
        }

        if (getLexer().identifierEquals("RELAYLOG")) {
            getLexer().nextToken();
            acceptIdentifier(MySqlKeyword.EVENTS);
            MySqlShowRelayLogEventsStatement stmt = new MySqlShowRelayLogEventsStatement();

            if (getLexer().equalToken(Token.IN)) {
                getLexer().nextToken();
                stmt.setLogName(this.exprParser.primary());
            }

            if (getLexer().equalToken(Token.FROM)) {
                getLexer().nextToken();
                stmt.setFrom(this.exprParser.primary());
            }

            stmt.setLimit(this.parseLimit());

            return stmt;
        }

        if (getLexer().identifierEquals("SLAVE")) {
            getLexer().nextToken();
            if (getLexer().identifierEquals(MySqlKeyword.STATUS)) {
                getLexer().nextToken();
                return new MySqlShowSlaveStatusStatement();
            } else {
                acceptIdentifier("HOSTS");
                return new MySqlShowSlaveHostsStatement();
            }
        }

        if (getLexer().equalToken(Token.TABLE)) {
            getLexer().nextToken();
            acceptIdentifier(MySqlKeyword.STATUS);
            MySqlShowTableStatusStatement stmt = new MySqlShowTableStatusStatement();
            if (getLexer().equalToken(Token.FROM) || getLexer().equalToken(Token.IN)) {
                getLexer().nextToken();
                stmt.setDatabase(this.exprParser.name());
            }

            if (getLexer().equalToken(Token.LIKE)) {
                getLexer().nextToken();
                stmt.setLike(this.exprParser.expr());
            }

            if (getLexer().equalToken(Token.WHERE)) {
                getLexer().nextToken();
                stmt.setWhere(this.exprParser.expr());
            }

            return stmt;
        }

        if (getLexer().identifierEquals("TRIGGERS")) {
            getLexer().nextToken();
            MySqlShowTriggersStatement stmt = new MySqlShowTriggersStatement();

            if (getLexer().equalToken(Token.FROM)) {
                getLexer().nextToken();
                SQLName database = exprParser.name();
                stmt.setDatabase(database);
            }

            if (getLexer().equalToken(Token.LIKE)) {
                getLexer().nextToken();
                SQLExpr like = exprParser.expr();
                stmt.setLike(like);
            }

            if (getLexer().equalToken(Token.WHERE)) {
                getLexer().nextToken();
                SQLExpr where = exprParser.expr();
                stmt.setWhere(where);
            }
            return stmt;
        }
        throw new ParserUnsupportedException(getLexer().getToken());
    }

    private MySqlShowStatusStatement parseShowStatus() {
        MySqlShowStatusStatement stmt = new MySqlShowStatusStatement();

        if (getLexer().equalToken(Token.LIKE)) {
            getLexer().nextToken();
            SQLExpr like = exprParser.expr();
            stmt.setLike(like);
        }

        if (getLexer().equalToken(Token.WHERE)) {
            getLexer().nextToken();
            SQLExpr where = exprParser.expr();
            stmt.setWhere(where);
        }

        return stmt;
    }

    private MySqlShowVariantsStatement parseShowVariants() {
        MySqlShowVariantsStatement stmt = new MySqlShowVariantsStatement();

        if (getLexer().equalToken(Token.LIKE)) {
            getLexer().nextToken();
            SQLExpr like = exprParser.expr();
            stmt.setLike(like);
        }

        if (getLexer().equalToken(Token.WHERE)) {
            getLexer().nextToken();
            SQLExpr where = exprParser.expr();
            stmt.setWhere(where);
        }

        return stmt;
    }

    private MySqlShowWarningsStatement parseShowWarnings() {
        MySqlShowWarningsStatement stmt = new MySqlShowWarningsStatement();

        stmt.setLimit(parseLimit());

        return stmt;
    }

    private MySqlShowDatabasesStatement parseShowDatabases() {
        MySqlShowDatabasesStatement stmt = new MySqlShowDatabasesStatement();

        if (getLexer().equalToken(Token.LIKE)) {
            getLexer().nextToken();
            SQLExpr like = exprParser.expr();
            stmt.setLike(like);
        }

        if (getLexer().equalToken(Token.WHERE)) {
            getLexer().nextToken();
            SQLExpr where = exprParser.expr();
            stmt.setWhere(where);
        }

        return stmt;
    }

    private MySqlShowTablesStatement parseShowTabless() {
        MySqlShowTablesStatement stmt = new MySqlShowTablesStatement();

        if (getLexer().equalToken(Token.FROM)) {
            getLexer().nextToken();
            SQLName database = exprParser.name();
            stmt.setDatabase(database);
        }

        if (getLexer().equalToken(Token.LIKE)) {
            getLexer().nextToken();
            SQLExpr like = exprParser.expr();
            stmt.setLike(like);
        }

        if (getLexer().equalToken(Token.WHERE)) {
            getLexer().nextToken();
            SQLExpr where = exprParser.expr();
            stmt.setWhere(where);
        }

        return stmt;
    }

    private MySqlShowColumnsStatement parseShowColumns() {
        MySqlShowColumnsStatement stmt = new MySqlShowColumnsStatement();

        if (getLexer().equalToken(Token.FROM)) {
            getLexer().nextToken();
            SQLName table = exprParser.name();
            stmt.setTable(table);

            if (getLexer().equalToken(Token.FROM) || getLexer().equalToken(Token.IN)) {
                getLexer().nextToken();
                SQLName database = exprParser.name();
                stmt.setDatabase(database);
            }
        }

        if (getLexer().equalToken(Token.LIKE)) {
            getLexer().nextToken();
            SQLExpr like = exprParser.expr();
            stmt.setLike(like);
        }

        if (getLexer().equalToken(Token.WHERE)) {
            getLexer().nextToken();
            SQLExpr where = exprParser.expr();
            stmt.setWhere(where);
        }

        return stmt;
    }

    public MySqlStartTransactionStatement parseStart() {
        acceptIdentifier("START");
        acceptIdentifier("TRANSACTION");

        MySqlStartTransactionStatement stmt = new MySqlStartTransactionStatement();

        if (getLexer().equalToken(Token.WITH)) {
            getLexer().nextToken();
            acceptIdentifier("CONSISTENT");
            acceptIdentifier("SNAPSHOT");
            stmt.setConsistentSnapshot(true);
        }

        if (getLexer().equalToken(Token.BEGIN)) {
            getLexer().nextToken();
            stmt.setBegin(true);
            if (getLexer().identifierEquals("WORK")) {
                getLexer().nextToken();
                stmt.setWork(true);
            }
        }

        if (getLexer().equalToken(Token.HINT)) {
            stmt.setHints(this.exprParser.parseHints());
        }

        return stmt;
    }

    @Override
    public MySqlRollbackStatement parseRollback() {
        acceptIdentifier("ROLLBACK");

        MySqlRollbackStatement stmt = new MySqlRollbackStatement();

        if (getLexer().identifierEquals("WORK")) {
            getLexer().nextToken();
        }

        if (getLexer().equalToken(Token.AND)) {
            getLexer().nextToken();
            if (getLexer().equalToken(Token.NOT)) {
                getLexer().nextToken();
                acceptIdentifier(MySqlKeyword.CHAIN);
                stmt.setChain(Boolean.FALSE);
            } else {
                acceptIdentifier(MySqlKeyword.CHAIN);
                stmt.setChain(Boolean.TRUE);
            }
        }

        if (getLexer().equalToken(Token.TO)) {
            getLexer().nextToken();

            if (getLexer().identifierEquals("SAVEPOINT")) {
                getLexer().nextToken();
            }

            stmt.setTo(this.exprParser.name());
        }

        return stmt;
    }

    public MySqlCommitStatement parseCommit() {
        acceptIdentifier("COMMIT");

        MySqlCommitStatement stmt = new MySqlCommitStatement();

        if (getLexer().identifierEquals("WORK")) {
            getLexer().nextToken();
            stmt.setWork(true);
        }

        if (getLexer().equalToken(Token.AND)) {
            getLexer().nextToken();
            if (getLexer().equalToken(Token.NOT)) {
                getLexer().nextToken();
                acceptIdentifier(MySqlKeyword.CHAIN);
                stmt.setChain(Boolean.FALSE);
            } else {
                acceptIdentifier(MySqlKeyword.CHAIN);
                stmt.setChain(Boolean.TRUE);
            }
        }

        return stmt;
    }

    public MySqlReplaceStatement parseReplicate() {
        MySqlReplaceStatement stmt = new MySqlReplaceStatement();

        accept(Token.REPLACE);

        if (getLexer().equalToken(Token.COMMENT)) {
            getLexer().nextToken();
        }

        if (getLexer().identifierEquals(MySqlKeyword.LOW_PRIORITY)) {
            stmt.setLowPriority(true);
            getLexer().nextToken();
        }

        if (getLexer().identifierEquals(MySqlKeyword.DELAYED)) {
            stmt.setDelayed(true);
            getLexer().nextToken();
        }

        if (getLexer().equalToken(Token.INTO)) {
            getLexer().nextToken();
        }

        SQLName tableName = exprParser.name();
        stmt.setTableName(tableName);

        if (getLexer().equalToken(Token.LEFT_PAREN)) {
            getLexer().nextToken();
            if (getLexer().equalToken(Token.SELECT)) {
                SQLQueryExpr queryExpr = (SQLQueryExpr) this.exprParser.expr();
                stmt.setQuery(queryExpr);
            } else {
                stmt.getColumns().addAll(exprParser.exprList(stmt));
            }
            accept(Token.RIGHT_PAREN);
        }

        if (getLexer().equalToken(Token.VALUES) || getLexer().identifierEquals("VALUE")) {
            getLexer().nextToken();

            parseValueClause(stmt.getValuesList(), 0);
        } else if (getLexer().equalToken(Token.SELECT)) {
            SQLQueryExpr queryExpr = (SQLQueryExpr) this.exprParser.expr();
            stmt.setQuery(queryExpr);
        } else if (getLexer().equalToken(Token.SET)) {
            getLexer().nextToken();

            SQLInsertStatement.ValuesClause values = new SQLInsertStatement.ValuesClause();
            stmt.getValuesList().add(values);
            while (true) {
                stmt.getColumns().add(this.exprParser.name());
                if (getLexer().equalToken(Token.COLON_EQ)) {
                    getLexer().nextToken();
                } else {
                    accept(Token.EQ);
                }
                values.getValues().add(this.exprParser.expr());

                if (getLexer().equalToken(Token.COMMA)) {
                    getLexer().nextToken();
                    continue;
                }

                break;
            }
        } else if (getLexer().equalToken(Token.LEFT_PAREN)) {
            getLexer().nextToken();
            SQLQueryExpr queryExpr = (SQLQueryExpr) this.exprParser.expr();
            stmt.setQuery(queryExpr);
            accept(Token.RIGHT_PAREN);
        }

        return stmt;
    }

    protected SQLStatement parseLoad() {
        acceptIdentifier("LOAD");
        if (getLexer().identifierEquals("DATA")) {
            return parseLoadDataInFile();
        }
        if (getLexer().identifierEquals("XML")) {
            return parseLoadXml();
        }
        throw new ParserUnsupportedException(getLexer().getToken());
    }

    protected MySqlLoadXmlStatement parseLoadXml() {
        acceptIdentifier("XML");

        MySqlLoadXmlStatement stmt = new MySqlLoadXmlStatement();

        if (getLexer().identifierEquals(MySqlKeyword.LOW_PRIORITY)) {
            stmt.setLowPriority(true);
            getLexer().nextToken();
        }

        if (getLexer().identifierEquals("CONCURRENT")) {
            stmt.setConcurrent(true);
            getLexer().nextToken();
        }

        if (getLexer().identifierEquals(MySqlKeyword.LOCAL)) {
            stmt.setLocal(true);
            getLexer().nextToken();
        }

        acceptIdentifier("INFILE");

        SQLLiteralExpr fileName = (SQLLiteralExpr) exprParser.expr();
        stmt.setFileName(fileName);

        if (getLexer().equalToken(Token.REPLACE)) {
            stmt.setReplicate(true);
            getLexer().nextToken();
        }

        if (getLexer().identifierEquals(MySqlKeyword.IGNORE)) {
            stmt.setIgnore(true);
            getLexer().nextToken();
        }

        accept(Token.INTO);
        accept(Token.TABLE);

        SQLName tableName = exprParser.name();
        stmt.setTableName(tableName);

        if (getLexer().identifierEquals(MySqlKeyword.CHARACTER)) {
            getLexer().nextToken();
            accept(Token.SET);

            if (!getLexer().equalToken(Token.LITERAL_CHARS)) {
                throw new ParserException(getLexer());
            }
            String charset = getLexer().getLiterals();
            getLexer().nextToken();
            stmt.setCharset(charset);
        }
        if (getLexer().identifierEquals("ROWS")) {
            getLexer().nextToken();
            accept(Token.IDENTIFIED);
            accept(Token.BY);
            SQLExpr rowsIdentifiedBy = exprParser.expr();
            stmt.setRowsIdentifiedBy(rowsIdentifiedBy);
        }
        if (getLexer().identifierEquals(MySqlKeyword.IGNORE)) {
            throw new ParserUnsupportedException(getLexer().getToken());
        }
        if (getLexer().equalToken(Token.SET)) {
            throw new ParserUnsupportedException(getLexer().getToken());
        }
        return stmt;
    }

    protected MySqlLoadDataInFileStatement parseLoadDataInFile() {

        acceptIdentifier("DATA");

        MySqlLoadDataInFileStatement stmt = new MySqlLoadDataInFileStatement();

        if (getLexer().identifierEquals(MySqlKeyword.LOW_PRIORITY)) {
            stmt.setLowPriority(true);
            getLexer().nextToken();
        }

        if (getLexer().identifierEquals("CONCURRENT")) {
            stmt.setConcurrent(true);
            getLexer().nextToken();
        }

        if (getLexer().identifierEquals(MySqlKeyword.LOCAL)) {
            stmt.setLocal(true);
            getLexer().nextToken();
        }

        acceptIdentifier("INFILE");

        SQLLiteralExpr fileName = (SQLLiteralExpr) exprParser.expr();
        stmt.setFileName(fileName);

        if (getLexer().equalToken(Token.REPLACE)) {
            stmt.setReplicate(true);
            getLexer().nextToken();
        }

        if (getLexer().identifierEquals(MySqlKeyword.IGNORE)) {
            stmt.setIgnore(true);
            getLexer().nextToken();
        }

        accept(Token.INTO);
        accept(Token.TABLE);

        SQLName tableName = exprParser.name();
        stmt.setTableName(tableName);

        if (getLexer().identifierEquals(MySqlKeyword.CHARACTER)) {
            getLexer().nextToken();
            accept(Token.SET);

            if (!getLexer().equalToken(Token.LITERAL_CHARS)) {
                throw new ParserException(getLexer());
            }

            String charset = getLexer().getLiterals();
            getLexer().nextToken();
            stmt.setCharset(charset);
        }

        if (getLexer().identifierEquals("FIELDS") || getLexer().identifierEquals("COLUMNS")) {
            getLexer().nextToken();
            if (getLexer().identifierEquals("TERMINATED")) {
                getLexer().nextToken();
                accept(Token.BY);
                stmt.setColumnsTerminatedBy(new SQLCharExpr(getLexer().getLiterals()));
                getLexer().nextToken();
            }

            if (getLexer().identifierEquals("OPTIONALLY")) {
                stmt.setColumnsEnclosedOptionally(true);
                getLexer().nextToken();
            }

            if (getLexer().identifierEquals("ENCLOSED")) {
                getLexer().nextToken();
                accept(Token.BY);
                stmt.setColumnsEnclosedBy(new SQLCharExpr(getLexer().getLiterals()));
                getLexer().nextToken();
            }

            if (getLexer().identifierEquals("ESCAPED")) {
                getLexer().nextToken();
                accept(Token.BY);
                stmt.setColumnsEscaped(new SQLCharExpr(getLexer().getLiterals()));
                getLexer().nextToken();
            }
        }

        if (getLexer().identifierEquals("LINES")) {
            getLexer().nextToken();
            if (getLexer().identifierEquals("STARTING")) {
                getLexer().nextToken();
                accept(Token.BY);
                stmt.setLinesStartingBy(new SQLCharExpr(getLexer().getLiterals()));
                getLexer().nextToken();
            }

            if (getLexer().identifierEquals("TERMINATED")) {
                getLexer().nextToken();
                accept(Token.BY);
                stmt.setLinesTerminatedBy(new SQLCharExpr(getLexer().getLiterals()));
                getLexer().nextToken();
            }
        }

        if (getLexer().identifierEquals(MySqlKeyword.IGNORE)) {
            getLexer().nextToken();
            stmt.setIgnoreLinesNumber(this.exprParser.expr());
            acceptIdentifier("LINES");
        }

        if (getLexer().equalToken(Token.LEFT_PAREN)) {
            getLexer().nextToken();
            stmt.getColumns().addAll(exprParser.exprList(stmt));
            accept(Token.RIGHT_PAREN);
        }

        if (getLexer().equalToken(Token.SET)) {
            getLexer().nextToken();
            stmt.getSetList().addAll(exprParser.exprList(stmt));
        }
        return stmt;

    }

    public MySqlPrepareStatement parsePrepare() {
        acceptIdentifier("PREPARE");

        SQLName name = exprParser.name();
        accept(Token.FROM);
        SQLExpr from = exprParser.expr();

        return new MySqlPrepareStatement(name, from);
    }

    public MySqlExecuteStatement parseExecute() {
        acceptIdentifier("EXECUTE");

        MySqlExecuteStatement stmt = new MySqlExecuteStatement();

        SQLName statementName = exprParser.name();
        stmt.setStatementName(statementName);

        if (getLexer().identifierEquals("USING")) {
            getLexer().nextToken();
            stmt.getParameters().addAll(exprParser.exprList(stmt));
        }

        return stmt;
    }
    
    @Override
    protected SQLInsertStatement parseInsert() {
        getLexer().nextToken();
        MySqlInsertStatement result = createSQLInsertStatement();
        parseInsertInto(result);
        parseColumns(result);
        if (getValuesIdentifiers().contains(getLexer().getLiterals())) {
            parseValues(result);
        } else if (getLexer().equalToken(Token.SELECT)) {
            parseInsertSelect(result);
        } else if (getLexer().equalToken(Token.SET)) {
            getLexer().nextToken();
            SQLInsertStatement.ValuesClause values = new SQLInsertStatement.ValuesClause();
            result.getValuesList().add(values);
            while (true) {
                result.getColumns().add(exprParser.name());
                if (getLexer().equalToken(Token.EQ)) {
                    getLexer().nextToken();
                } else {
                    accept(Token.COLON_EQ);
                }
                values.getValues().add(exprParser.expr());
                if (getLexer().equalToken(Token.COMMA)) {
                    getLexer().nextToken();
                    continue;
                }
                break;
            }
        } else if (getLexer().equalToken(Token.LEFT_PAREN)) {
            getLexer().nextToken();
            SQLSelect select = exprParser.createSelectParser().select();
            select.setParent(result);
            result.setQuery(select);
            accept(Token.RIGHT_PAREN);
        }
        if (getAppendixIdentifiers().contains(getLexer().getLiterals())) {
            parseAppendices(result);
        }
        return result;
    }
    
    @Override
    protected MySqlInsertStatement createSQLInsertStatement() {
        return new MySqlInsertStatement();
    }
    
    @Override
    protected void parseValues(final SQLInsertStatement sqlInsertStatement) {
        MySqlInsertStatement mySqlInsertStatement = (MySqlInsertStatement) sqlInsertStatement;
        do {
            getLexer().nextToken();
            accept(Token.LEFT_PAREN);
            SQLInsertStatement.ValuesClause values = new SQLInsertStatement.ValuesClause();
            values.getValues().addAll(exprParser.exprList(values));
            mySqlInsertStatement.getValuesList().add(values);
            accept(Token.RIGHT_PAREN);
        }
        while (getLexer().equalToken(Token.COMMA));
    }
    
    @Override
    protected Set<String> getIdentifiersBetweenTableAndValues() {
        Set<String> result = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        result.add(Token.PARTITION.getName());
        return result;
    }
    
    @Override
    protected Set<String> getValuesIdentifiers() {
        Set<String> result = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        result.add(Token.VALUES.getName());
        result.add("VALUE");
        return result;
    }
    
    @Override
    protected Set<String> getAppendixIdentifiers() {
        Set<String> result = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        result.add(Token.ON.getName());
        return result;
    }
    
    private void parseValueClause(final List<ValuesClause> valueClauseList, final int columnSize) {
        while (true) {
            if (!getLexer().equalToken(Token.LEFT_PAREN)) {
                throw new ParserException(getLexer(), Token.LEFT_PAREN);
            }
            getLexer().nextTokenValue();
            if (!getLexer().equalToken(Token.RIGHT_PAREN)) {
                List<SQLExpr> valueExprList;
                if (columnSize > 0) {
                    valueExprList = new ArrayList<>(columnSize);
                } else {
                    valueExprList = new ArrayList<>();
                }

                while (true) {
                    SQLExpr expr;
                    if (getLexer().equalToken(Token.LITERAL_INT)) {
                        expr = new SQLIntegerExpr(getLexer().integerValue());
                        getLexer().nextTokenCommaOrRightParen();
                    } else if (getLexer().equalToken(Token.LITERAL_CHARS)) {
                        expr = new SQLCharExpr(getLexer().getLiterals());
                        getLexer().nextTokenCommaOrRightParen();
                    } else if (getLexer().equalToken(Token.LITERAL_NCHARS)) {
                        expr = new SQLNCharExpr(getLexer().getLiterals());
                        getLexer().nextTokenCommaOrRightParen();
                    } else {
                        expr = exprParser.expr();
                    }

                    if (getLexer().equalToken(Token.COMMA)) {
                        valueExprList.add(expr);
                        getLexer().nextTokenValue();
                    } else if (getLexer().equalToken(Token.RIGHT_PAREN)) {
                        valueExprList.add(expr);
                        break;
                    } else {
                        expr = this.exprParser.primaryRest(expr);
                        if (!getLexer().equalToken(Token.COMMA) && !getLexer().equalToken(Token.RIGHT_PAREN)) {
                            expr = this.exprParser.exprRest(expr);
                        }

                        valueExprList.add(expr);
                        if (getLexer().equalToken(Token.COMMA)) {
                            getLexer().nextToken();
                        } else {
                            break;
                        }
                    }
                }

                SQLInsertStatement.ValuesClause values = new SQLInsertStatement.ValuesClause(valueExprList);
                valueClauseList.add(values);
            } else {
                SQLInsertStatement.ValuesClause values = new SQLInsertStatement.ValuesClause(new ArrayList<SQLExpr>(0));
                valueClauseList.add(values);
            }

            if (!getLexer().equalToken(Token.RIGHT_PAREN)) {
                throw new ParserException(getLexer(), Token.RIGHT_PAREN);
            }
            getLexer().nextTokenCommaOrRightParen();
            if (getLexer().equalToken(Token.COMMA)) {
                getLexer().nextTokenLeftParen();
            } else {
                break;
            }
        }
    }
    
    public SQLStatement parseSet() {
        accept(Token.SET);
        if (getLexer().identifierEquals("PASSWORD")) {
            getLexer().nextToken();
            MySqlSetPasswordStatement stmt = new MySqlSetPasswordStatement();

            if (getLexer().equalToken(Token.FOR)) {
                getLexer().nextToken();
                stmt.setUser(this.exprParser.name());
            }

            accept(Token.EQ);

            stmt.setPassword(this.exprParser.expr());

            return stmt;
        }

        Boolean global = null;
        if (getLexer().identifierEquals(MySqlKeyword.GLOBAL)) {
            global = Boolean.TRUE;
            getLexer().nextToken();
        } else if (getLexer().identifierEquals(MySqlKeyword.SESSION)) {
            global = Boolean.FALSE;
            getLexer().nextToken();
        }

        if (getLexer().identifierEquals("TRANSACTION")) {
            MySqlSetTransactionStatement stmt = new MySqlSetTransactionStatement();
            stmt.setGlobal(global);

            getLexer().nextToken();
            if (getLexer().identifierEquals("ISOLATION")) {
                getLexer().nextToken();
                acceptIdentifier("LEVEL");

                if (getLexer().identifierEquals(MySqlKeyword.READ)) {
                    getLexer().nextToken();

                    if (getLexer().identifierEquals("UNCOMMITTED")) {
                        stmt.setIsolationLevel("READ UNCOMMITTED");
                        getLexer().nextToken();
                    } else if (getLexer().identifierEquals(MySqlKeyword.WRITE)) {
                        stmt.setIsolationLevel("READ WRITE");
                        getLexer().nextToken();
                    } else if (getLexer().identifierEquals("ONLY")) {
                        stmt.setIsolationLevel("READ ONLY");
                        getLexer().nextToken();
                    } else if (getLexer().identifierEquals("COMMITTED")) {
                        stmt.setIsolationLevel("READ COMMITTED");
                        getLexer().nextToken();
                    } else {
                        throw new ParserException("UNKOWN TRANSACTION LEVEL : " + getLexer().getLiterals());
                    }
                } else if (getLexer().identifierEquals("SERIALIZABLE")) {
                    stmt.setIsolationLevel("SERIALIZABLE");
                    getLexer().nextToken();
                } else if (getLexer().identifierEquals("REPEATABLE")) {
                    getLexer().nextToken();
                    if (getLexer().identifierEquals(MySqlKeyword.READ)) {
                        stmt.setIsolationLevel("REPEATABLE READ");
                        getLexer().nextToken();
                    } else {
                        throw new ParserException("UNKOWN TRANSACTION LEVEL : " + getLexer().getLiterals());
                    }
                } else {
                    throw new ParserException("UNKOWN TRANSACTION LEVEL : " + getLexer().getLiterals());
                }
            } else if (getLexer().identifierEquals(MySqlKeyword.READ)) {
                getLexer().nextToken();
                if (getLexer().identifierEquals("ONLY")) {
                    stmt.setAccessModel("ONLY");
                    getLexer().nextToken();
                } else if (getLexer().identifierEquals("WRITE")) {
                    stmt.setAccessModel("WRITE");
                    getLexer().nextToken();
                } else {
                    throw new ParserException("UNKOWN ACCESS MODEL : " + getLexer().getLiterals());
                }
            }

            return stmt;
        } else if (getLexer().identifierEquals("NAMES")) {
            getLexer().nextToken();

            MySqlSetNamesStatement stmt = new MySqlSetNamesStatement();
            if (getLexer().equalToken(Token.DEFAULT)) {
                getLexer().nextToken();
                stmt.setDefault(true);
            } else {
                String charSet = getLexer().getLiterals();
                stmt.setCharSet(charSet);
                getLexer().nextToken();
                if (getLexer().identifierEquals(MySqlKeyword.COLLATE2)) {
                    getLexer().nextToken();

                    String collate = getLexer().getLiterals();
                    stmt.setCollate(collate);
                    getLexer().nextToken();
                }
            }
            return stmt;
        } else if (getLexer().identifierEquals(MySqlKeyword.CHARACTER)) {
            getLexer().nextToken();

            accept(Token.SET);

            MySqlSetCharSetStatement stmt = new MySqlSetCharSetStatement();
            if (getLexer().equalToken(Token.DEFAULT)) {
                getLexer().nextToken();
                stmt.setDefault(true);
            } else {
                String charSet = getLexer().getLiterals();
                stmt.setCharSet(charSet);
                getLexer().nextToken();
                if (getLexer().identifierEquals(MySqlKeyword.COLLATE2)) {
                    getLexer().nextToken();

                    String collate = getLexer().getLiterals();
                    stmt.setCollate(collate);
                    getLexer().nextToken();
                }
            }
            return stmt;
        } else {
            SQLSetStatement stmt = new SQLSetStatement(getDbType());

            parseAssignItems(stmt.getItems(), stmt);

            if (null != global && global) {
                SQLVariantRefExpr varRef = (SQLVariantRefExpr) stmt.getItems().get(0).getTarget();
                varRef.setGlobal(true);
            }

            if (getLexer().equalToken(Token.HINT)) {
                stmt.getHints().clear();
                stmt.getHints().addAll(exprParser.parseHints());
            }

            return stmt;
        }
    }

    public Limit parseLimit() {
        return ((MySqlExprParser) this.exprParser).parseLimit();
    }

    public SQLStatement parseAlter() {
        accept(Token.ALTER);

        if (getLexer().equalToken(Token.USER)) {
            return parseAlterUser();
        }

        boolean ignore = false;

        if (getLexer().identifierEquals(MySqlKeyword.IGNORE)) {
            ignore = true;
            getLexer().nextToken();
        }

        if (getLexer().equalToken(Token.TABLE)) {
            getLexer().nextToken();

            MySqlAlterTableStatement stmt = new MySqlAlterTableStatement();
            stmt.setIgnore(ignore);
            stmt.setName(this.exprParser.name());

            while (true) {
                if (getLexer().equalToken(Token.DROP)) {
                    parseAlterDrop(stmt);
                } else if (getLexer().identifierEquals("ADD")) {
                    getLexer().nextToken();

                    if (getLexer().equalToken(Token.COLUMN)) {
                        getLexer().nextToken();
                        parseAlterTableAddColumn(stmt);
                    } else if (getLexer().equalToken(Token.INDEX)) {
                        SQLAlterTableAddIndex item = parseAlterTableAddIndex();
                        item.setParent(stmt);
                        stmt.getItems().add(item);
                    } else if (getLexer().equalToken(Token.UNIQUE)) {
                        SQLAlterTableAddIndex item = parseAlterTableAddIndex();
                        item.setParent(stmt);
                        stmt.getItems().add(item);
                    } else if (getLexer().equalToken(Token.PRIMARY)) {
                        SQLPrimaryKey primaryKey = this.exprParser.parsePrimaryKey();
                        SQLAlterTableAddConstraint item = new SQLAlterTableAddConstraint(primaryKey);
                        stmt.getItems().add(item);
                    } else if (getLexer().equalToken(Token.KEY)) {
                        SQLAlterTableAddIndex item = parseAlterTableAddIndex();
                        item.setParent(stmt);
                        stmt.getItems().add(item);
                    } else if (getLexer().equalToken(Token.CONSTRAINT)) {
                        getLexer().nextToken();
                        SQLName constraintName = this.exprParser.name();

                        if (getLexer().equalToken(Token.PRIMARY)) {
                            SQLPrimaryKey primaryKey = ((MySqlExprParser) this.exprParser).parsePrimaryKey();

                            primaryKey.setName(constraintName);

                            SQLAlterTableAddConstraint item = new SQLAlterTableAddConstraint(primaryKey);
                            item.setParent(stmt);

                            stmt.getItems().add(item);
                        } else if (getLexer().equalToken(Token.FOREIGN)) {
                            MysqlForeignKey fk = this.getExprParser().parseForeignKey();
                            fk.setName(constraintName);
                            fk.setHasConstraint(true);

                            SQLAlterTableAddConstraint item = new SQLAlterTableAddConstraint(fk);

                            item.setParent(stmt);

                            stmt.getItems().add(item);
                        } else {
                            throw new ParserUnsupportedException(getLexer().getToken());
                        }
                    } else if (getLexer().identifierEquals(MySqlKeyword.FULLTEXT)) {
                        throw new ParserUnsupportedException(getLexer().getToken());
                    } else if (getLexer().identifierEquals(MySqlKeyword.SPATIAL)) {
                        throw new ParserUnsupportedException(getLexer().getToken());
                    } else {
                        parseAlterTableAddColumn(stmt);
                    }
                } else if (getLexer().equalToken(Token.ALTER)) {
                    getLexer().nextToken();
                    if (getLexer().equalToken(Token.COLUMN)) {
                        getLexer().nextToken();
                    }

                    SQLAlterTableAlterColumn alterColumn = new SQLAlterTableAlterColumn();
                    SQLColumnDefinition columnDef = this.exprParser.parseColumn();
                    alterColumn.setColumn(columnDef);

                    stmt.getItems().add(alterColumn);
                } else if (getLexer().identifierEquals("CHANGE")) {
                    getLexer().nextToken();
                    if (getLexer().equalToken(Token.COLUMN)) {
                        getLexer().nextToken();
                    }
                    MySqlAlterTableChangeColumn item = new MySqlAlterTableChangeColumn();
                    item.setColumnName(this.exprParser.name());
                    item.setNewColumnDefinition(this.exprParser.parseColumn());
                    if (getLexer().identifierEquals("AFTER")) {
                        getLexer().nextToken();
                        item.setAfterColumn(this.exprParser.name());
                    } else if (getLexer().identifierEquals("FIRST")) {
                        getLexer().nextToken();
                        if (getLexer().equalToken(Token.IDENTIFIER)) {
                            item.setFirstColumn(this.exprParser.name());
                        } else {
                            item.setFirst(true);
                        }
                    }
                    stmt.getItems().add(item);
                } else if (getLexer().identifierEquals("MODIFY")) {
                    getLexer().nextToken();
                    if (getLexer().equalToken(Token.COLUMN)) {
                        getLexer().nextToken();
                    }
                    MySqlAlterTableModifyColumn item = new MySqlAlterTableModifyColumn();
                    item.setNewColumnDefinition(this.exprParser.parseColumn());
                    if (getLexer().identifierEquals("AFTER")) {
                        getLexer().nextToken();
                        item.setAfterColumn(this.exprParser.name());
                    } else if (getLexer().identifierEquals("FIRST")) {
                        getLexer().nextToken();
                        if (getLexer().equalToken(Token.IDENTIFIER)) {
                            item.setFirstColumn(this.exprParser.name());
                        } else {
                            item.setFirst(true);
                        }
                    }
                    stmt.getItems().add(item);
                } else if (getLexer().equalToken(Token.DISABLE)) {
                    getLexer().nextToken();

                    if (getLexer().equalToken(Token.CONSTRAINT)) {
                        getLexer().nextToken();
                        SQLAlterTableDisableConstraint item = new SQLAlterTableDisableConstraint();
                        item.setConstraintName(this.exprParser.name());
                        stmt.getItems().add(item);
                    } else {
                        acceptIdentifier("KEYS");
                        SQLAlterTableDisableKeys item = new SQLAlterTableDisableKeys();
                        stmt.getItems().add(item);
                    }
                } else if (getLexer().equalToken(Token.ENABLE)) {
                    getLexer().nextToken();
                    if (getLexer().equalToken(Token.CONSTRAINT)) {
                        getLexer().nextToken();
                        SQLAlterTableEnableConstraint item = new SQLAlterTableEnableConstraint();
                        item.setConstraintName(this.exprParser.name());
                        stmt.getItems().add(item);
                    } else {
                        acceptIdentifier("KEYS");
                        SQLAlterTableEnableKeys item = new SQLAlterTableEnableKeys();
                        stmt.getItems().add(item);
                    }
                } else if (getLexer().identifierEquals("RENAME")) {
                    getLexer().nextToken();
                    if (getLexer().equalToken(Token.TO) || getLexer().equalToken(Token.AS)) {
                        getLexer().nextToken();
                    }
                    MySqlRenameTableStatement renameStmt = new MySqlRenameTableStatement();
                    MySqlRenameTableStatement.Item item = new MySqlRenameTableStatement.Item();
                    item.setName(stmt.getTableSource().getExpr());
                    item.setTo(this.exprParser.name());
                    renameStmt.getItems().add(item);

                    return renameStmt;
                } else if (getLexer().equalToken(Token.ORDER)) {
                    throw new ParserUnsupportedException(getLexer().getToken());
                } else if (getLexer().identifierEquals("CONVERT")) {
                    throw new ParserUnsupportedException(getLexer().getToken());
                } else if (getLexer().equalToken(Token.DEFAULT)) {
                    throw new ParserUnsupportedException(getLexer().getToken());
                } else if (getLexer().identifierEquals("DISCARD")) {
                    getLexer().nextToken();
                    accept(Token.TABLESPACE);
                    MySqlAlterTableDiscardTablespace item = new MySqlAlterTableDiscardTablespace();
                    stmt.getItems().add(item);
                } else if (getLexer().identifierEquals("IMPORT")) {
                    getLexer().nextToken();
                    accept(Token.TABLESPACE);
                    MySqlAlterTableImportTablespace item = new MySqlAlterTableImportTablespace();
                    stmt.getItems().add(item);
                } else if (getLexer().identifierEquals("FORCE")) {
                    throw new ParserUnsupportedException(getLexer().getToken());
                } else if (getLexer().identifierEquals("TRUNCATE")) {
                    throw new ParserUnsupportedException(getLexer().getToken());
                } else if (getLexer().identifierEquals("COALESCE")) {
                    throw new ParserUnsupportedException(getLexer().getToken());
                } else if (getLexer().identifierEquals("REORGANIZE")) {
                    throw new ParserUnsupportedException(getLexer().getToken());
                } else if (getLexer().identifierEquals("EXCHANGE")) {
                    throw new ParserUnsupportedException(getLexer().getToken());
                } else if (getLexer().identifierEquals("ANALYZE")) {
                    throw new ParserUnsupportedException(getLexer().getToken());
                } else if (getLexer().identifierEquals("CHECK")) {
                    throw new ParserUnsupportedException(getLexer().getToken());
                } else if (getLexer().equalToken(Token.OPTIMIZE)) {
                    throw new ParserUnsupportedException(getLexer().getToken());
                } else if (getLexer().identifierEquals("REBUILD")) {
                    throw new ParserUnsupportedException(getLexer().getToken());
                } else if (getLexer().identifierEquals("REPAIR")) {
                    throw new ParserUnsupportedException(getLexer().getToken());
                } else if (getLexer().identifierEquals("REMOVE")) {
                    throw new ParserUnsupportedException(getLexer().getToken());
                } else if (getLexer().identifierEquals("ALGORITHM")) {
                    getLexer().nextToken();
                    accept(Token.EQ);
                    stmt.getItems().add(new MySqlAlterTableOption("ALGORITHM", getLexer().getLiterals()));
                    getLexer().nextToken();
                } else if (getLexer().identifierEquals(MySqlKeyword.ENGINE)) {
                    getLexer().nextToken();
                    accept(Token.EQ);
                    stmt.getItems().add(new MySqlAlterTableOption(MySqlKeyword.ENGINE, getLexer().getLiterals()));
                    getLexer().nextToken();
                } else if (getLexer().identifierEquals(MySqlKeyword.AUTO_INCREMENT)) {
                    getLexer().nextToken();
                    accept(Token.EQ);
                    stmt.getItems().add(new MySqlAlterTableOption(MySqlKeyword.AUTO_INCREMENT, getLexer().integerValue()));
                    getLexer().nextToken();
                } else if (getLexer().identifierEquals(MySqlKeyword.COLLATE2)) {
                    getLexer().nextToken();
                    accept(Token.EQ);
                    stmt.getItems().add(new MySqlAlterTableOption(MySqlKeyword.COLLATE2, getLexer().getLiterals()));
                    getLexer().nextToken();
                } else if (getLexer().identifierEquals("PACK_KEYS")) {
                    getLexer().nextToken();
                    accept(Token.EQ);
                    if (getLexer().identifierEquals("PACK")) {
                        getLexer().nextToken();
                        accept(Token.ALL);
                        stmt.getItems().add(new MySqlAlterTableOption("PACK_KEYS", "PACK ALL"));
                    } else {
                        stmt.getItems().add(new MySqlAlterTableOption("PACK_KEYS", getLexer().getLiterals()));
                        getLexer().nextToken();
                    }
                } else if (getLexer().identifierEquals(MySqlKeyword.CHARACTER)) {
                    getLexer().nextToken();
                    accept(Token.SET);
                    accept(Token.EQ);
                    MySqlAlterTableCharacter item = new MySqlAlterTableCharacter();
                    item.setCharacterSet(this.exprParser.primary());
                    if (getLexer().equalToken(Token.COMMA)) {
                        getLexer().nextToken();
                        acceptIdentifier(MySqlKeyword.COLLATE2);
                        accept(Token.EQ);
                        item.setCollate(this.exprParser.primary());
                    }
                    stmt.getItems().add(item);
                } else if (getLexer().equalToken(Token.COMMENT)) {
                    getLexer().nextToken();
                    if (getLexer().equalToken(Token.EQ)) {
                        accept(Token.EQ);
                    }
                    stmt.getItems().add(new MySqlAlterTableOption("COMMENT", '\'' + getLexer().getLiterals() + '\''));
                    getLexer().nextToken();
                } else {
                    break;
                }

                if (getLexer().equalToken(Token.COMMA)) {
                    getLexer().nextToken();
                    continue;
                }
                break;
            }
            return stmt;
        }
        throw new ParserUnsupportedException(getLexer().getToken());
    }

    protected void parseAlterTableAddColumn(MySqlAlterTableStatement stmt) {
        boolean parenFlag = false;
        if (getLexer().equalToken(Token.LEFT_PAREN)) {
            getLexer().nextToken();
            parenFlag = true;
        }
        
        MySqlAlterTableAddColumn item = new MySqlAlterTableAddColumn();
        while (true) {
            
            SQLColumnDefinition columnDef = this.exprParser.parseColumn();
            item.getColumns().add(columnDef);
            if (getLexer().identifierEquals("AFTER")) {
                getLexer().nextToken();
                item.setAfterColumn(this.exprParser.name());
            } else if (getLexer().identifierEquals("FIRST")) {
                getLexer().nextToken();
                if (getLexer().equalToken(Token.IDENTIFIER)) {
                    item.setFirstColumn(this.exprParser.name());
                } else {
                    item.setFirst(true);
                }
            }
            
            if (parenFlag && getLexer().equalToken(Token.COMMA)) {
                getLexer().nextToken();
                continue;
            }
            
            break;
        }
        
        stmt.getItems().add(item);
        
        if (parenFlag) {
            accept(Token.RIGHT_PAREN);
        }
    }
    
    public void parseAlterDrop(SQLAlterTableStatement stmt) {
        getLexer().nextToken();
        if (getLexer().equalToken(Token.INDEX)) {
            getLexer().nextToken();
            SQLName indexName = this.exprParser.name();
            SQLAlterTableDropIndex item = new SQLAlterTableDropIndex();
            item.setIndexName(indexName);
            stmt.getItems().add(item);
        } else if (getLexer().equalToken(Token.FOREIGN)) {
            getLexer().nextToken();
            accept(Token.KEY);
            SQLName indexName = this.exprParser.name();
            SQLAlterTableDropForeignKey item = new SQLAlterTableDropForeignKey();
            item.setIndexName(indexName);
            stmt.getItems().add(item);
        } else if (getLexer().equalToken(Token.PRIMARY)) {
            getLexer().nextToken();
            accept(Token.KEY);
            SQLAlterTableDropPrimaryKey item = new SQLAlterTableDropPrimaryKey();
            stmt.getItems().add(item);
        } else if (getLexer().equalToken(Token.CONSTRAINT)) {
            getLexer().nextToken();
            SQLAlterTableDropConstraint item = new SQLAlterTableDropConstraint();
            item.setConstraintName(this.exprParser.name());
            stmt.getItems().add(item);
        } else if (getLexer().equalToken(Token.COLUMN)) {
            getLexer().nextToken();
            SQLAlterTableDropColumnItem item = new SQLAlterTableDropColumnItem();

            SQLName name = exprParser.name();
            name.setParent(item);
            item.getColumns().add(name);

            while (getLexer().equalToken(Token.COMMA)) {
                int originalPosition = getLexer().getCurrentPosition();
                getLexer().nextToken();
                if (getLexer().identifierEquals("CHANGE")) {
                    getLexer().setCurrentPosition(originalPosition);
                    break;
                }
                if (getLexer().equalToken(Token.IDENTIFIER)) {
                    name = exprParser.name();
                    name.setParent(item);
                } else {
                    getLexer().setCurrentPosition(originalPosition);
                    break;
                }
            }
            stmt.getItems().add(item);
        } else if (getLexer().equalToken(Token.IDENTIFIER)) {
            SQLAlterTableDropColumnItem item = new SQLAlterTableDropColumnItem();
            this.exprParser.names(item.getColumns());
            stmt.getItems().add(item);
        } else {
            super.parseAlterDrop(stmt);
        }
    }

    public SQLStatement parseRename() {
        MySqlRenameTableStatement stmt = new MySqlRenameTableStatement();

        acceptIdentifier("RENAME");

        accept(Token.TABLE);

        while (true) {
            MySqlRenameTableStatement.Item item = new MySqlRenameTableStatement.Item();
            item.setName(this.exprParser.name());
            accept(Token.TO);
            item.setTo(this.exprParser.name());

            stmt.getItems().add(item);

            if (getLexer().equalToken(Token.COMMA)) {
                getLexer().nextToken();
                continue;
            }

            break;
        }

        return stmt;
    }

    public SQLStatement parseCreateDatabase() {
        if (getLexer().equalToken(Token.CREATE)) {
            getLexer().nextToken();
        }

        accept(Token.DATABASE);

        SQLCreateDatabaseStatement stmt = new SQLCreateDatabaseStatement(JdbcConstants.MYSQL);
        stmt.setName(this.exprParser.name());

        if (getLexer().equalToken(Token.DEFAULT)) {
            getLexer().nextToken();
        }

        if (getLexer().equalToken(Token.HINT)) {
            stmt.setHints(this.exprParser.parseHints());
        }

        return stmt;
    }

    protected void parseUpdateSet(SQLUpdateStatement update) {
        accept(Token.SET);

        while (true) {
            SQLUpdateSetItem item = this.exprParser.parseUpdateSetItem();
            update.addItem(item);

            if (getLexer().getToken() != Token.COMMA) {
                break;
            }

            getLexer().nextToken();
        }
    }

    public MySqlAlterUserStatement parseAlterUser() {
        accept(Token.USER);

        MySqlAlterUserStatement stmt = new MySqlAlterUserStatement();
        while (true) {
            SQLExpr user = this.exprParser.expr();
            acceptIdentifier("PASSWORD");
            acceptIdentifier("EXPIRE");
            stmt.getUsers().add(user);

            if (getLexer().equalToken(Token.COMMA)) {
                getLexer().nextToken();
                continue;
            }

            break;
        }
        return stmt;
    }

    public MySqlExprParser getExprParser() {
        return (MySqlExprParser) exprParser;
    }

    public MySqlHintStatement parseHint() {
        MySqlHintStatement stmt = new MySqlHintStatement();
        stmt.setHints(this.exprParser.parseHints());
        return stmt;
    }

    /**
     * parse create procedure statement
     */
    public MySqlCreateProcedureStatement parseCreateProcedure() {
        /**
         * CREATE OR REPALCE PROCEDURE SP_NAME(parameter_list) BEGIN block_statement END
         */
        MySqlCreateProcedureStatement stmt = new MySqlCreateProcedureStatement();

        accept(Token.CREATE);
        if (getLexer().equalToken(Token.OR)) {
            getLexer().nextToken();
            accept(Token.REPLACE);
            stmt.setOrReplace(true);
        }

        accept(Token.PROCEDURE);

        stmt.setName(this.exprParser.name());

        if (getLexer().equalToken(Token.LEFT_PAREN)) {// match "("
            getLexer().nextToken();
            parserParameters(stmt.getParameters());
            accept(Token.RIGHT_PAREN);// match ")"
        }
        MySqlBlockStatement block = this.parseBlock();

        stmt.setBlock(block);

        return stmt;
    }

    /**
     * parse create procedure parameters
     * @param parameters
     */
    private void parserParameters(List<MySqlParameter> parameters) {
        while (true) {
            MySqlParameter parameter = new MySqlParameter();

            if (getLexer().equalToken(Token.CURSOR)) {
                getLexer().nextToken();

                parameter.setName(this.exprParser.name());

                accept(Token.IS);
                SQLSelect select = this.createSQLSelectParser().select();

                SQLDataTypeImpl dataType = new SQLDataTypeImpl("CURSOR");
                parameter.setDataType(dataType);

                parameter.setDefaultValue(new SQLQueryExpr(select));
                
            } else if (getLexer().equalToken(Token.IN) || getLexer().equalToken(Token.OUT) || getLexer().equalToken(Token.INOUT)) {
                if(getLexer().equalToken(Token.IN)) {
                    parameter.setParamType(ParameterType.IN);
                } else if(getLexer().equalToken(Token.OUT)) {
                    parameter.setParamType(ParameterType.OUT);
                } else if(getLexer().equalToken(Token.INOUT)) {
                    parameter.setParamType(ParameterType.INOUT);
                }
                getLexer().nextToken();

                parameter.setName(this.exprParser.name());

                parameter.setDataType(this.exprParser.parseDataType());
            } else {
                parameter.setParamType(ParameterType.DEFAULT);//default parameter type is in
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

    /**
     * parse procedure statement block
     * @param statementList
     */
    private void parseProcedureStatementList(List<SQLStatement> statementList)
    {
        parseProcedureStatementList(statementList,-1);
    }
    
    /**
     * parse procedure statement block
     */
    private void parseProcedureStatementList(List<SQLStatement> statementList, int max) {

        while (true) {
            if (max != -1) {
                if (statementList.size() >= max) {
                    return;
                }
            }

            if (getLexer().equalToken(Token.EOF)) {
                return;
            }
            if (getLexer().equalToken(Token.END)) {
                return;
            }
            if (getLexer().equalToken(Token.ELSE)) {
                return;
            }
            if (getLexer().equalToken(Token.SEMI)) {
                getLexer().nextToken();
                continue;
            }
            if (getLexer().equalToken(Token.WHEN)) {
                return;
            }
            if (getLexer().equalToken(Token.UNTIL)) {
                return;
            }
            // select into
            if (getLexer().equalToken(Token.SELECT)) {
                statementList.add(this.parseSelectInto());
                continue;
            }
            
            // update
            if (getLexer().equalToken(Token.UPDATE)) {
                statementList.add(parseUpdateStatement());
                continue;
            }
            
            // create
            if (getLexer().equalToken(Token.CREATE)) {
                statementList.add(parseCreate());
                continue;
            }
            
            // insert
            if (getLexer().equalToken(Token.INSERT)) {
                statementList.add(parseInsert());
                continue;
            }
            
            // delete
            if (getLexer().equalToken(Token.DELETE)) {
                statementList.add(parseDeleteStatement());
                continue;
            }
            
            // call
            if (getLexer().equalToken(Token.LEFT_BRACE) || getLexer().identifierEquals("CALL")) {
                statementList.add(this.parseCall());
                continue;
            }

            // begin
            if (getLexer().equalToken(Token.BEGIN)) {
                statementList.add(this.parseBlock());
                continue;
            }

            if (getLexer().equalToken(Token.VARIANT)) {
                SQLExpr variant = this.exprParser.primary();
                if (variant instanceof SQLBinaryOpExpr) {
                    SQLBinaryOpExpr binaryOpExpr = (SQLBinaryOpExpr) variant;
                    if (binaryOpExpr.getOperator() == SQLBinaryOperator.Assignment) {
                        SQLSetStatement stmt = new SQLSetStatement(
                                binaryOpExpr.getLeft(),
                                binaryOpExpr.getRight(), getDbType());
                        statementList.add(stmt);
                        continue;
                    }
                }
                accept(Token.COLON_EQ);
                SQLExpr value = this.exprParser.expr();

                SQLSetStatement stmt = new SQLSetStatement(variant, value,
                        getDbType());
                statementList.add(stmt);
                continue;
            }
            
            // select
            if (getLexer().equalToken(Token.LEFT_PAREN)) {
                int currentPosition = getLexer().getCurrentPosition();
                getLexer().nextToken();

                if (getLexer().equalToken(Token.SELECT)) {
                    getLexer().setCurrentPosition(currentPosition);
                    getLexer().setToken(Token.LEFT_PAREN);
                    statementList.add(this.parseSelect());
                    continue;
                } else {
                    throw new ParserUnsupportedException(getLexer().getToken());
                }
            }
            if (getLexer().equalToken(Token.SET)) {
                statementList.add(this.parseAssign());
                continue;
            }
            
            if (getLexer().equalToken(Token.WHILE)) {
                statementList.add(this.parseWhile());
                continue;
            }
            
            if (getLexer().equalToken(Token.LOOP)) {
                statementList.add(this.parseLoop());
                continue;
            }
            
            if (getLexer().equalToken(Token.IF)) {
                statementList.add(this.parseIf());
                continue;
            }
            
            if (getLexer().equalToken(Token.CASE)) {
                statementList.add(this.parseCase());
                continue;
            }

            if (getLexer().equalToken(Token.DECLARE)) {
                int currentPosition = getLexer().getCurrentPosition();
                getLexer().nextToken();
                getLexer().nextToken();
                if(getLexer().equalToken(Token.CURSOR)) {
                    getLexer().setCurrentPosition(currentPosition);
                    getLexer().setToken(Token.DECLARE);
                    statementList.add(this.parseCursorDeclare());
                } else {
                    getLexer().setCurrentPosition(currentPosition);
                    getLexer().setToken(Token.DECLARE);
                    statementList.add(this.parseDeclare());
                }
                continue;
            }
            
            if (getLexer().equalToken(Token.LEAVE)) {
                statementList.add(this.parseLeave());
                continue;
            }
            
            if (getLexer().equalToken(Token.ITERATE)) {
                statementList.add(this.parseIterate());
                continue;
            }
            
            if (getLexer().equalToken(Token.REPEAT)) {
                statementList.add(this.parseRepeat());
                continue;
            }
            
            if (getLexer().equalToken(Token.OPEN)) {
                statementList.add(this.parseOpen());
                continue;
            }
            
            if (getLexer().equalToken(Token.CLOSE)) {
                statementList.add(this.parseClose());
                continue;
            }
            
            if (getLexer().equalToken(Token.FETCH)) {
                statementList.add(this.parseFetch());
                continue;
            }
            
            if(getLexer().equalToken(Token.IDENTIFIER)) {
                String label=getLexer().getLiterals();
                int currentPosition = getLexer().getCurrentPosition();
                getLexer().nextToken();
                if (getLexer().equalToken(Token.VARIANT) && getLexer().getLiterals().equals(":")) {
                    getLexer().nextToken();
                    if (getLexer().equalToken(Token.LOOP)) {
                        statementList.add(this.parseLoop(label));
                    } else if(getLexer().equalToken(Token.WHILE)) {
                        statementList.add(this.parseWhile(label));
                    } else if(getLexer().equalToken(Token.BEGIN)) {
                        statementList.add(this.parseBlock(label));
                    } else if(getLexer().equalToken(Token.REPEAT)) {
                        statementList.add(this.parseRepeat(label));
                    }
                    continue;
                }
                else {
                    getLexer().setCurrentPosition(currentPosition);
                    getLexer().setToken(Token.IDENTIFIER);
                }
            }
            throw new ParserUnsupportedException(getLexer().getToken());
        }
    }

    /**
     * parse if statement
     * @return MySqlIfStatement
     */
    public MySqlIfStatement parseIf() {
        accept(Token.IF);

        MySqlIfStatement stmt = new MySqlIfStatement();

        stmt.setCondition(this.exprParser.expr());

        accept(Token.THEN);

        this.parseProcedureStatementList(stmt.getStatements());

        while (getLexer().equalToken(Token.ELSE)) {
            getLexer().nextToken();

            if (getLexer().equalToken(Token.IF)) {
                getLexer().nextToken();

                MySqlElseIfStatement elseIf = new MySqlElseIfStatement();

                elseIf.setCondition(this.exprParser.expr());

                accept(Token.THEN);
                this.parseProcedureStatementList(elseIf.getStatements());

                stmt.getElseIfList().add(elseIf);
            } else {
                MySqlElseStatement elseItem = new MySqlElseStatement();
                this.parseProcedureStatementList(elseItem.getStatements());
                stmt.setElseItem(elseItem);
                break;
            }
        }

        accept(Token.END);
        accept(Token.IF);
        accept(Token.SEMI);

        return stmt;
    }

    /**
     * parse while statement
     * @return MySqlWhileStatement
     */
    public MySqlWhileStatement parseWhile() {
        accept(Token.WHILE);
        MySqlWhileStatement stmt = new MySqlWhileStatement();

        stmt.setCondition(this.exprParser.expr());

        accept(Token.DO);

        this.parseProcedureStatementList(stmt.getStatements());

        accept(Token.END);

        accept(Token.WHILE);
        
        accept(Token.SEMI);

        return stmt;

    }
    
    /**
     * parse while statement with label
     * @return MySqlWhileStatement
     */
    public MySqlWhileStatement parseWhile(String label) {
        accept(Token.WHILE);
        
        MySqlWhileStatement stmt = new MySqlWhileStatement();

        stmt.setLabelName(label);
        
        stmt.setCondition(this.exprParser.expr());

        accept(Token.DO);

        this.parseProcedureStatementList(stmt.getStatements());

        accept(Token.END);

        accept(Token.WHILE);
        
        acceptIdentifier(label);
        
        accept(Token.SEMI);

        return stmt;

    }
    
    /**
     * parse case statement
     * @return MySqlCaseStatement
     */
    public MySqlCaseStatement parseCase()
    {
        MySqlCaseStatement stmt=new MySqlCaseStatement();
        accept(Token.CASE);
        
        if(getLexer().equalToken(Token.WHEN))// grammar 1
        {
            while (getLexer().equalToken(Token.WHEN)) {
                MySqlWhenStatement when=new MySqlWhenStatement();
                when.setCondition(exprParser.expr());
                accept(Token.THEN);
                parseProcedureStatementList(when.getStatements());
                stmt.addWhenStatement(when);
            }
            if(getLexer().equalToken(Token.ELSE)) {
                MySqlElseStatement elseStmt=new MySqlElseStatement();
                parseProcedureStatementList(elseStmt.getStatements());
                stmt.setElseItem(elseStmt);
            }
        }
        else// grammar 2
        {
            //case expr
            stmt.setCondition(exprParser.expr());
            
            while (getLexer().equalToken(Token.WHEN)) {
                accept(Token.WHEN);
                MySqlWhenStatement when=new MySqlWhenStatement();
                //when expr
                when.setCondition(exprParser.expr());
                
                accept(Token.THEN);
                
                //when block
                parseProcedureStatementList(when.getStatements());
                
                stmt.addWhenStatement(when);
            }
            if(getLexer().equalToken(Token.ELSE)) {
                accept(Token.ELSE);
                MySqlElseStatement elseStmt=new MySqlElseStatement();
                parseProcedureStatementList(elseStmt.getStatements());
                stmt.setElseItem(elseStmt);
            }
        }
        accept(Token.END);
        accept(Token.CASE);
        accept(Token.SEMI);
        return stmt;
        
    }
    /**
     * parse declare statement
     */
    public MySqlDeclareStatement parseDeclare() {
        MySqlDeclareStatement stmt=new MySqlDeclareStatement();
        accept(Token.DECLARE);
        while(true) {
            SQLExpr var = exprParser.primary();
            if (var instanceof SQLIdentifierExpr) {
                var = new SQLVariantRefExpr(((SQLIdentifierExpr) var).getSimpleName());
            }
            stmt.addVar(var);
            if(getLexer().equalToken(Token.COMMA)) {
                accept(Token.COMMA);
            } else if (getLexer().equalToken(Token.EOF)) {
                //var type
                stmt.setType(exprParser.parseDataType());
                break;
            } else {
                throw new ParserException(getLexer());
            }
        }
        return stmt;
    }
    
    /**
     * parse assign statement
     */
    public SQLSetStatement parseAssign() {
        accept(Token.SET);
        SQLSetStatement stmt = new SQLSetStatement(getDbType());
        parseAssignItems(stmt.getItems(), stmt);
        return stmt;
    }
    
    /**
     * parse select into
     */
    public MySqlSelectIntoStatement parseSelectInto() {
        MySqlSelectIntoParser parse=new MySqlSelectIntoParser(this.exprParser);
        return parse.parseSelectInto();
    }
    
    /**
     * parse loop statement
     */
    public MySqlLoopStatement parseLoop()
    {
        MySqlLoopStatement loopStmt=new MySqlLoopStatement(); 
        accept(Token.LOOP);
        parseProcedureStatementList(loopStmt.getStatements());
        accept(Token.END);
        accept(Token.LOOP);
        accept(Token.SEMI);
        return loopStmt;
    }
    
    /**
     * parse loop statement with label
     */
    public MySqlLoopStatement parseLoop(String label)
    {
        MySqlLoopStatement loopStmt=new MySqlLoopStatement(); 
        loopStmt.setLabelName(label);
        accept(Token.LOOP);
        parseProcedureStatementList(loopStmt.getStatements());
        accept(Token.END);
        accept(Token.LOOP);
        acceptIdentifier(label);
        accept(Token.SEMI);
        return loopStmt;
    }
    
    /**
     * parse loop statement with label
     */
    public MySqlBlockStatement parseBlock(String label) {
        MySqlBlockStatement block = new MySqlBlockStatement();
        block.setLabelName(label);
        accept(Token.BEGIN);
        parseProcedureStatementList(block.getStatementList());
        accept(Token.END);
        acceptIdentifier(label);
        return block;
    }
    
    /**
     * parse leave statement
     */
    public MySqlLeaveStatement parseLeave()
    {
        accept(Token.LEAVE);
        MySqlLeaveStatement leaveStmt=new MySqlLeaveStatement();
        leaveStmt.setLabelName(exprParser.name().getSimpleName());
        accept(Token.SEMI);
        return leaveStmt;
    }
    
    /**
     * parse iterate statement
     */
    public MySqlIterateStatement parseIterate()
    {
        accept(Token.ITERATE);
        MySqlIterateStatement iterateStmt=new MySqlIterateStatement();
        iterateStmt.setLabelName(exprParser.name().getSimpleName());
        accept(Token.SEMI);
        return iterateStmt;
    }
    
    /**
     * parse repeat statement
     * @return
     */
    public MySqlRepeatStatement parseRepeat()
    {
        MySqlRepeatStatement repeatStmt=new MySqlRepeatStatement(); 
        accept(Token.REPEAT);
        parseProcedureStatementList(repeatStmt.getStatements());
        accept(Token.UNTIL);
        repeatStmt.setCondition(exprParser.expr());
        accept(Token.END);
        accept(Token.REPEAT);
        accept(Token.SEMI);
        return repeatStmt;
    }
    
    /**
     * parse repeat statement with label
     * @param label
     * @return
     */
    public MySqlRepeatStatement parseRepeat(String label)
    {
        MySqlRepeatStatement repeatStmt=new MySqlRepeatStatement(); 
        repeatStmt.setLabelName(label);
        accept(Token.REPEAT);
        parseProcedureStatementList(repeatStmt.getStatements());
        accept(Token.UNTIL);
        repeatStmt.setCondition(exprParser.expr());
        accept(Token.END);
        accept(Token.REPEAT);
        acceptIdentifier(label);
        accept(Token.SEMI);
        return repeatStmt;
    }
    
    /**
     * parse cursor declare statement
     * @return
     */
    public MySqlCursorDeclareStatement parseCursorDeclare()
    {
        MySqlCursorDeclareStatement stmt=new MySqlCursorDeclareStatement();
        accept(Token.DECLARE);
        
        stmt.setCursorName(exprParser.name().getSimpleName());
        
        accept(Token.CURSOR);
        
        accept(Token.FOR);
        
        stmt.setSelect(parseSelect());
        
        accept(Token.SEMI);
        
        return stmt;
    }
    
}
