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

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.SQLName;
import com.alibaba.druid.sql.ast.SQLObject;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.expr.SQLCharExpr;
import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.expr.SQLQueryExpr;
import com.alibaba.druid.sql.ast.expr.SQLVariantRefExpr;
import com.alibaba.druid.sql.ast.statement.SQLAlterTableAddColumn;
import com.alibaba.druid.sql.ast.statement.SQLAlterTableAddConstraint;
import com.alibaba.druid.sql.ast.statement.SQLAlterTableAddIndex;
import com.alibaba.druid.sql.ast.statement.SQLAlterTableAddPartition;
import com.alibaba.druid.sql.ast.statement.SQLAlterTableAlterColumn;
import com.alibaba.druid.sql.ast.statement.SQLAlterTableDisableConstraint;
import com.alibaba.druid.sql.ast.statement.SQLAlterTableDisableKeys;
import com.alibaba.druid.sql.ast.statement.SQLAlterTableDisableLifecycle;
import com.alibaba.druid.sql.ast.statement.SQLAlterTableDropColumnItem;
import com.alibaba.druid.sql.ast.statement.SQLAlterTableDropConstraint;
import com.alibaba.druid.sql.ast.statement.SQLAlterTableDropPartition;
import com.alibaba.druid.sql.ast.statement.SQLAlterTableEnableConstraint;
import com.alibaba.druid.sql.ast.statement.SQLAlterTableEnableKeys;
import com.alibaba.druid.sql.ast.statement.SQLAlterTableEnableLifecycle;
import com.alibaba.druid.sql.ast.statement.SQLAlterTableItem;
import com.alibaba.druid.sql.ast.statement.SQLAlterTableRename;
import com.alibaba.druid.sql.ast.statement.SQLAlterTableRenameColumn;
import com.alibaba.druid.sql.ast.statement.SQLAlterTableRenamePartition;
import com.alibaba.druid.sql.ast.statement.SQLAlterTableSetComment;
import com.alibaba.druid.sql.ast.statement.SQLAlterTableSetLifecycle;
import com.alibaba.druid.sql.ast.statement.SQLAlterTableStatement;
import com.alibaba.druid.sql.ast.statement.SQLAlterTableTouch;
import com.alibaba.druid.sql.ast.statement.SQLAlterViewRenameStatement;
import com.alibaba.druid.sql.ast.statement.SQLAssignItem;
import com.alibaba.druid.sql.ast.statement.SQLCallStatement;
import com.alibaba.druid.sql.ast.statement.SQLCheck;
import com.alibaba.druid.sql.ast.statement.SQLCloseStatement;
import com.alibaba.druid.sql.ast.statement.SQLColumnDefinition;
import com.alibaba.druid.sql.ast.statement.SQLCommentStatement;
import com.alibaba.druid.sql.ast.statement.SQLConstraint;
import com.alibaba.druid.sql.ast.statement.SQLCreateDatabaseStatement;
import com.alibaba.druid.sql.ast.statement.SQLCreateIndexStatement;
import com.alibaba.druid.sql.ast.statement.SQLCreateTriggerStatement;
import com.alibaba.druid.sql.ast.statement.SQLCreateViewStatement;
import com.alibaba.druid.sql.ast.statement.SQLDeleteStatement;
import com.alibaba.druid.sql.ast.statement.SQLDropDatabaseStatement;
import com.alibaba.druid.sql.ast.statement.SQLDropFunctionStatement;
import com.alibaba.druid.sql.ast.statement.SQLDropIndexStatement;
import com.alibaba.druid.sql.ast.statement.SQLDropProcedureStatement;
import com.alibaba.druid.sql.ast.statement.SQLDropSequenceStatement;
import com.alibaba.druid.sql.ast.statement.SQLDropTableSpaceStatement;
import com.alibaba.druid.sql.ast.statement.SQLDropTableStatement;
import com.alibaba.druid.sql.ast.statement.SQLDropTriggerStatement;
import com.alibaba.druid.sql.ast.statement.SQLDropUserStatement;
import com.alibaba.druid.sql.ast.statement.SQLDropViewStatement;
import com.alibaba.druid.sql.ast.statement.SQLExplainStatement;
import com.alibaba.druid.sql.ast.statement.SQLExprTableSource;
import com.alibaba.druid.sql.ast.statement.SQLFetchStatement;
import com.alibaba.druid.sql.ast.statement.SQLGrantStatement;
import com.alibaba.druid.sql.ast.statement.SQLInsertInto;
import com.alibaba.druid.sql.ast.statement.SQLInsertStatement;
import com.alibaba.druid.sql.ast.statement.SQLObjectType;
import com.alibaba.druid.sql.ast.statement.SQLOpenStatement;
import com.alibaba.druid.sql.ast.statement.SQLPrimaryKey;
import com.alibaba.druid.sql.ast.statement.SQLReleaseSavePointStatement;
import com.alibaba.druid.sql.ast.statement.SQLRevokeStatement;
import com.alibaba.druid.sql.ast.statement.SQLRollbackStatement;
import com.alibaba.druid.sql.ast.statement.SQLSavePointStatement;
import com.alibaba.druid.sql.ast.statement.SQLSelectOrderByItem;
import com.alibaba.druid.sql.ast.statement.SQLSelectStatement;
import com.alibaba.druid.sql.ast.statement.SQLSetStatement;
import com.alibaba.druid.sql.ast.statement.SQLTableSource;
import com.alibaba.druid.sql.ast.statement.SQLTruncateStatement;
import com.alibaba.druid.sql.ast.statement.SQLUpdateSetItem;
import com.alibaba.druid.sql.ast.statement.SQLUpdateStatement;
import com.alibaba.druid.sql.ast.statement.SQLUseStatement;
import com.alibaba.druid.sql.lexer.Token;
import lombok.AccessLevel;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * SQL解析器.
 *
 * @author zhangliang
 */
@Getter(AccessLevel.PROTECTED)
public class SQLStatementParser extends SQLParser {
    
    protected final SQLExprParser exprParser;
    
    public SQLStatementParser(final SQLExprParser exprParser) {
        super(exprParser.getLexer(), exprParser.getDbType());
        this.exprParser = exprParser;
    }
    
    /**
     * 解析SQL.
     * 
     * @return SQL解析对象
     */
    public SQLStatement parseStatement() {
        return parseStatementList(1).get(0);
    }
    
    public List<SQLStatement> parseStatementList() {
        return parseStatementList(-1);
    }
    
    protected List<SQLStatement> parseStatementList(final int max) {
        List<SQLStatement> result = new ArrayList<>(-1 == max ? 16 : max);
        while (true) {
            if (-1 != max && result.size() >= max) {
                return result;
            }
            if (getLexer().isEndToken()) {
                return result;
            }
            if (getLexer().equalToken(Token.SEMI)) {
                getLexer().nextToken();
                continue;
            }
            if (getLexer().equalToken(Token.SELECT)) {
                result.add(parseSelect());
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
                SQLStatement insertStatement = parseInsert();
                result.add(insertStatement);
                continue;
            }
            if (getLexer().equalToken(Token.DELETE)) {
                result.add(parseDeleteStatement());
                continue;
            }
            if (getLexer().equalToken(Token.EXPLAIN)) {
                result.add(parseExplain());
                continue;
            }
            if (getLexer().equalToken(Token.SET)) {
                result.add(parseSet());
                continue;
            }
            if (getLexer().equalToken(Token.ALTER)) {
                result.add(parseAlter());
                continue;
            }
            if (getLexer().equalToken(Token.DROP)) {
                getLexer().nextToken();
                if (getLexer().equalToken(Token.TABLE) || getLexer().identifierEquals("TEMPORARY")) {
                    SQLDropTableStatement stmt = parseDropTable(false);
                    result.add(stmt);
                    continue;
                } else if (getLexer().equalToken(Token.USER)) {
                    SQLStatement stmt = parseDropUser();
                    result.add(stmt);
                    continue;
                } else if (getLexer().equalToken(Token.INDEX)) {
                    SQLStatement stmt = parseDropIndex();
                    result.add(stmt);
                    continue;
                } else if (getLexer().equalToken(Token.VIEW)) {
                    SQLStatement stmt = parseDropView(false);
                    result.add(stmt);
                    continue;
                } else if (getLexer().equalToken(Token.TRIGGER)) {
                    SQLStatement stmt = parseDropTrigger(false);
                    result.add(stmt);
                    continue;
                } else if (getLexer().equalToken(Token.DATABASE)) {
                    SQLStatement stmt = parseDropDatabase(false);
                    result.add(stmt);
                    continue;
                } else if (getLexer().equalToken(Token.FUNCTION)) {
                    SQLStatement stmt = parseDropFunction(false);
                    result.add(stmt);
                    continue;
                } else if (getLexer().equalToken(Token.TABLESPACE)) {
                    SQLStatement stmt = parseDropTablespace(false);
                    result.add(stmt);
                    continue;
                } else if (getLexer().equalToken(Token.PROCEDURE)) {
                    SQLStatement stmt = parseDropProcedure(false);
                    result.add(stmt);
                    continue;
                } else {
                    throw new ParserUnsupportedException(getLexer().getToken());
                }
            }
            if (getLexer().equalToken(Token.TRUNCATE)) {
                SQLStatement stmt = parseTruncate();
                result.add(stmt);
                continue;
            }
            if (getLexer().equalToken(Token.USE)) {
                SQLStatement stmt = parseUse();
                result.add(stmt);
                continue;
            }
            if (getLexer().equalToken(Token.GRANT)) {
                SQLStatement stmt = parseGrant();
                result.add(stmt);
                continue;
            }
            if (getLexer().equalToken(Token.REVOKE)) {
                SQLStatement stmt = parseRevoke();
                result.add(stmt);
                continue;
            }
            if (getLexer().equalToken(Token.LEFT_BRACE) || getLexer().identifierEquals("CALL")) {
                SQLCallStatement stmt = parseCall();
                result.add(stmt);
                continue;
            }
            if (getLexer().identifierEquals("RENAME")) {
                SQLStatement stmt = parseRename();
                result.add(stmt);
                continue;
            }
            if (getLexer().identifierEquals("RELEASE")) {
                SQLStatement stmt = parseReleaseSavePoint();
                result.add(stmt);
                continue;
            }
            if (getLexer().identifierEquals("SAVEPOINT")) {
                SQLStatement stmt = parseSavePoint();
                result.add(stmt);
                continue;
            }
            if (getLexer().identifierEquals("ROLLBACK")) {
                SQLRollbackStatement stmt = parseRollback();
                result.add(stmt);
                continue;
            }
            if (getLexer().identifierEquals("COMMIT")) {
                SQLStatement stmt = parseCommit();
                result.add(stmt);
                continue;
            }
            if (getLexer().equalToken(Token.SHOW)) {
                SQLStatement stmt = parseShow();
                result.add(stmt);
                continue;
            }
            if (getLexer().equalToken(Token.LEFT_PAREN)) {
                int currentPosition = getLexer().getCurrentPosition();
                getLexer().nextToken();
                if (getLexer().equalToken(Token.SELECT)) {
                    getLexer().setCurrentPosition(currentPosition);
                    getLexer().setToken(Token.LEFT_PAREN);
                    SQLStatement stmt = parseSelect();
                    result.add(stmt);
                    continue;
                }
            }
            if (parseStatementListDialect(result)) {
                continue;
            }
            if (getLexer().equalToken(Token.COMMENT)) {
                result.add(this.parseComment());
                continue;
            }
            throw new ParserException(getLexer());
        }
    }
    
    protected SQLSelectStatement parseSelect() {
        return new SQLSelectStatement(createSQLSelectParser().select());
    }
    
    protected SQLSelectParser createSQLSelectParser() {
        return new SQLSelectParser(exprParser);
    }
    
    // TODO 整合parseStatementList
//    private SQLStatement parseStatement0() {
//        if (getLexer().equalToken(Token.SELECT)) {
//            return parseSelect();
//        }
//        if (getLexer().equalToken(Token.UPDATE)) {
//            return parseUpdateStatement();
//        }
//        if (getLexer().equalToken(Token.CREATE)) {
//            return parseCreate();
//        }
//        if (getLexer().equalToken(Token.INSERT)) {
//            return  parseInsert();
//        }
//        if (getLexer().equalToken(Token.DELETE)) {
//            return parseDeleteStatement();
//        }
//        if (getLexer().equalToken(Token.EXPLAIN)) {
//            return parseExplain();
//        }
//        if (getLexer().equalToken(Token.SET)) {
//            return parseSet();
//        }
//        if (getLexer().equalToken(Token.ALTER)) {
//            return parseAlter();
//        }
//        if (getLexer().equalToken(Token.DROP)) {
//            getLexer().nextToken();
//            if (getLexer().equalToken(Token.TABLE) || getLexer().identifierEquals("TEMPORARY")) {
//                return parseDropTable(false);
//            } else if (getLexer().equalToken(Token.USER)) {
//                return parseDropUser();
//            } else if (getLexer().equalToken(Token.INDEX)) {
//                return parseDropIndex();
//            } else if (getLexer().equalToken(Token.VIEW)) {
//                return parseDropView(false);
//            } else if (getLexer().equalToken(Token.TRIGGER)) {
//                return parseDropTrigger(false);
//            } else if (getLexer().equalToken(Token.DATABASE)) {
//                return parseDropDatabase(false);
//            } else if (getLexer().equalToken(Token.FUNCTION)) {
//                return parseDropFunction(false);
//            } else if (getLexer().equalToken(Token.TABLESPACE)) {
//                return parseDropTablespace(false);
//            } else if (getLexer().equalToken(Token.PROCEDURE)) {
//                return parseDropProcedure(false);
//            } else {
//                throw new ParserException("TODO " + getLexer().getToken());
//            }
//        }
//        if (getLexer().equalToken(Token.TRUNCATE)) {
//            return parseTruncate();
//        }
//        if (getLexer().equalToken(Token.USE)) {
//            return parseUse();
//        }
//        if (getLexer().equalToken(Token.GRANT)) {
//            return parseGrant();
//        }
//        if (getLexer().equalToken(Token.REVOKE)) {
//            return parseRevoke();
//        }
//        if (getLexer().equalToken(Token.LEFT_BRACE) || getLexer().identifierEquals("CALL")) {
//            return parseCall();
//        }
//        if (getLexer().identifierEquals("RENAME")) {
//            return parseRename();
//        }
//        if (getLexer().identifierEquals("RELEASE")) {
//            return parseReleaseSavePoint();
//        }
//        if (getLexer().identifierEquals("SAVEPOINT")) {
//            return parseSavePoint();
//        }
//        if (getLexer().identifierEquals("ROLLBACK")) {
//            return parseRollback();
//        }
//        if (getLexer().identifierEquals("COMMIT")) {
//            return parseCommit();
//        }
//        if (getLexer().equalToken(Token.SHOW)) {
//            return parseShow();
//        }
//        if (getLexer().equalToken(Token.LEFT_PAREN)) {
//            int currentPosition = getLexer().getCurrentPosition();
//            getLexer().nextToken();
//            if (getLexer().equalToken(Token.SELECT)) {
//                getLexer().setCurrentPosition(currentPosition);
//                getLexer().setToken(Token.LEFT_PAREN);
//                return parseSelect();
//            }
//        }
//        return null;
//    }
    
    public SQLRollbackStatement parseRollback() {
        getLexer().nextToken();

        if (getLexer().identifierEquals("WORK")) {
            getLexer().nextToken();
        }

        SQLRollbackStatement stmt = new SQLRollbackStatement(getDbType());

        if (getLexer().equalToken(Token.TO)) {
            getLexer().nextToken();

            if (getLexer().identifierEquals("SAVEPOINT")) {
                getLexer().nextToken();
            }

            stmt.setTo(this.exprParser.name());
        }
        return stmt;
    }

    public SQLStatement parseCommit() {
        throw new ParserUnsupportedException(getLexer().getToken());
    }

    public SQLStatement parseShow() {
        throw new ParserUnsupportedException(getLexer().getToken());
    }

    public SQLUseStatement parseUse() {
        accept(Token.USE);
        SQLUseStatement stmt = new SQLUseStatement(getDbType());
        stmt.setDatabase(this.exprParser.name());
        return stmt;
    }

    public SQLGrantStatement parseGrant() {
        accept(Token.GRANT);
        SQLGrantStatement stmt = new SQLGrantStatement(getDbType());

        parsePrivileages(stmt.getPrivileges(), stmt);

        if (getLexer().equalToken(Token.ON)) {
            getLexer().nextToken();

            if (getLexer().equalToken(Token.PROCEDURE)) {
                getLexer().nextToken();
                stmt.setObjectType(SQLObjectType.PROCEDURE);
            } else if (getLexer().equalToken(Token.FUNCTION)) {
                getLexer().nextToken();
                stmt.setObjectType(SQLObjectType.FUNCTION);
            } else if (getLexer().equalToken(Token.TABLE)) {
                getLexer().nextToken();
                stmt.setObjectType(SQLObjectType.TABLE);
            } else if (getLexer().equalToken(Token.USER)) {
                getLexer().nextToken();
                stmt.setObjectType(SQLObjectType.USER);
            } else if (getLexer().equalToken(Token.DATABASE)) {
                getLexer().nextToken();
                stmt.setObjectType(SQLObjectType.DATABASE);
            }

            if (stmt.getObjectType() != null && getLexer().equalToken(Token.DOUBLE_COLON)) {
                getLexer().nextToken(); // sql server
            }

            SQLExpr expr = this.exprParser.expr();
            if (stmt.getObjectType() == SQLObjectType.TABLE || stmt.getObjectType() == null) {
                stmt.setOn(new SQLExprTableSource(expr));
            } else {
                stmt.setOn(expr);
            }
        }

        if (getLexer().equalToken(Token.TO)) {
            getLexer().nextToken();
            stmt.setTo(this.exprParser.expr());
        }

        if (getLexer().equalToken(Token.WITH)) {
            getLexer().nextToken();

            while (true) {
                if (getLexer().identifierEquals("MAX_QUERIES_PER_HOUR")) {
                    getLexer().nextToken();
                    stmt.setMaxQueriesPerHour(this.exprParser.primary());
                    continue;
                }

                if (getLexer().identifierEquals("MAX_UPDATES_PER_HOUR")) {
                    getLexer().nextToken();
                    stmt.setMaxUpdatesPerHour(this.exprParser.primary());
                    continue;
                }

                if (getLexer().identifierEquals("MAX_CONNECTIONS_PER_HOUR")) {
                    getLexer().nextToken();
                    stmt.setMaxConnectionsPerHour(this.exprParser.primary());
                    continue;
                }

                if (getLexer().identifierEquals("MAX_USER_CONNECTIONS")) {
                    getLexer().nextToken();
                    stmt.setMaxUserConnections(this.exprParser.primary());
                    continue;
                }

                break;
            }
        }

        if (getLexer().identifierEquals("ADMIN")) {
            getLexer().nextToken();
            acceptIdentifier("OPTION");
            stmt.setAdminOption(true);
        }

        if (getLexer().equalToken(Token.IDENTIFIED)) {
            getLexer().nextToken();
            accept(Token.BY);
            stmt.setIdentifiedBy(this.exprParser.expr());
        }

        return stmt;
    }

    protected void parsePrivileages(List<SQLExpr> privileges, SQLObject parent) {
        while (true) {
            String privilege = null;
            if (getLexer().equalToken(Token.ALL)) {
                getLexer().nextToken();
                if (getLexer().identifierEquals("PRIVILEGES")) {
                    privilege = "ALL PRIVILEGES";
                } else {
                    privilege = "ALL";
                }
            } else if (getLexer().equalToken(Token.SELECT)) {
                privilege = "SELECT";
                getLexer().nextToken();
            } else if (getLexer().equalToken(Token.UPDATE)) {
                privilege = "UPDATE";
                getLexer().nextToken();
            } else if (getLexer().equalToken(Token.DELETE)) {
                privilege = "DELETE";
                getLexer().nextToken();
            } else if (getLexer().equalToken(Token.INSERT)) {
                privilege = "INSERT";
                getLexer().nextToken();
            } else if (getLexer().equalToken(Token.INDEX)) {
                getLexer().nextToken();
                privilege = "INDEX";
            } else if (getLexer().equalToken(Token.TRIGGER)) {
                getLexer().nextToken();
                privilege = "TRIGGER";
            } else if (getLexer().equalToken(Token.REFERENCES)) {
                privilege = "REFERENCES";
                getLexer().nextToken();
            } else if (getLexer().equalToken(Token.CREATE)) {
                getLexer().nextToken();

                if (getLexer().equalToken(Token.TABLE)) {
                    privilege = "CREATE TABLE";
                    getLexer().nextToken();
                } else if (getLexer().equalToken(Token.SESSION)) {
                    privilege = "CREATE SESSION";
                    getLexer().nextToken();
                } else if (getLexer().equalToken(Token.TABLESPACE)) {
                    privilege = "CREATE TABLESPACE";
                    getLexer().nextToken();
                } else if (getLexer().equalToken(Token.USER)) {
                    privilege = "CREATE USER";
                    getLexer().nextToken();
                } else if (getLexer().equalToken(Token.VIEW)) {
                    privilege = "CREATE VIEW";
                    getLexer().nextToken();
                } else if (getLexer().equalToken(Token.ANY)) {
                    getLexer().nextToken();

                    if (getLexer().equalToken(Token.TABLE)) {
                        getLexer().nextToken();
                        privilege = "CREATE ANY TABLE";
                    } else if (getLexer().identifierEquals("MATERIALIZED")) {
                        getLexer().nextToken();
                        accept(Token.VIEW);
                        privilege = "CREATE ANY MATERIALIZED VIEW";
                    } else {
                        throw new ParserUnsupportedException(getLexer().getToken());
                    }
                } else if (getLexer().identifierEquals("SYNONYM")) {
                    privilege = "CREATE SYNONYM";
                    getLexer().nextToken();
                } else if (getLexer().identifierEquals("ROUTINE")) {
                    privilege = "CREATE ROUTINE";
                    getLexer().nextToken();
                } else if (getLexer().identifierEquals("TEMPORARY")) {
                    getLexer().nextToken();
                    accept(Token.TABLE);
                    privilege = "CREATE TEMPORARY TABLE";
                } else {
                    throw new ParserUnsupportedException(getLexer().getToken());
                }
            } else if (getLexer().equalToken(Token.ALTER)) {
                getLexer().nextToken();
                if (getLexer().equalToken(Token.TABLE)) {
                    privilege = "ALTER TABLE";
                    getLexer().nextToken();
                } else if (getLexer().equalToken(Token.SESSION)) {
                    privilege = "ALTER SESSION";
                    getLexer().nextToken();
                } else if (getLexer().equalToken(Token.ANY)) {
                    getLexer().nextToken();

                    if (getLexer().equalToken(Token.TABLE)) {
                        getLexer().nextToken();
                        privilege = "ALTER ANY TABLE";
                    } else if (getLexer().identifierEquals("MATERIALIZED")) {
                        getLexer().nextToken();
                        accept(Token.VIEW);
                        privilege = "ALTER ANY MATERIALIZED VIEW";
                    } else {
                        throw new ParserUnsupportedException(getLexer().getToken());
                    }
                } else {
                    throw new ParserUnsupportedException(getLexer().getToken());
                }
            } else if (getLexer().equalToken(Token.DROP)) {
                getLexer().nextToken();
                if (getLexer().equalToken(Token.DROP)) {
                    privilege = "DROP TABLE";
                    getLexer().nextToken();
                } else if (getLexer().equalToken(Token.SESSION)) {
                    privilege = "DROP SESSION";
                    getLexer().nextToken();
                } else if (getLexer().equalToken(Token.ANY)) {
                    getLexer().nextToken();

                    if (getLexer().equalToken(Token.TABLE)) {
                        getLexer().nextToken();
                        privilege = "DROP ANY TABLE";
                    } else if (getLexer().identifierEquals("MATERIALIZED")) {
                        getLexer().nextToken();
                        accept(Token.VIEW);
                        privilege = "DROP ANY MATERIALIZED VIEW";
                    } else {
                        throw new ParserUnsupportedException(getLexer().getToken());
                    }
                } else {
                    privilege = "DROP";
                }
            } else if (getLexer().identifierEquals("USAGE")) {
                privilege = "USAGE";
                getLexer().nextToken();
            } else if (getLexer().identifierEquals("EXECUTE")) {
                privilege = "EXECUTE";
                getLexer().nextToken();
            } else if (getLexer().identifierEquals("PROXY")) {
                privilege = "PROXY";
                getLexer().nextToken();
            } else if (getLexer().identifierEquals("QUERY")) {
                getLexer().nextToken();
                acceptIdentifier("REWRITE");
                privilege = "QUERY REWRITE";
            } else if (getLexer().identifierEquals("GLOBAL")) {
                getLexer().nextToken();
                acceptIdentifier("QUERY");
                acceptIdentifier("REWRITE");
                privilege = "GLOBAL QUERY REWRITE";
            } else if (getLexer().identifierEquals("INHERIT")) {
                getLexer().nextToken();
                acceptIdentifier("PRIVILEGES");
                privilege = "INHERIT PRIVILEGES";
            } else if (getLexer().identifierEquals("EVENT")) {
                getLexer().nextToken();
                privilege = "EVENT";
            } else if (getLexer().identifierEquals("FILE")) {
                getLexer().nextToken();
                privilege = "FILE";
            } else if (getLexer().equalToken(Token.GRANT)) {
                getLexer().nextToken();
                acceptIdentifier("OPTION");
                privilege = "GRANT OPTION";
            } else if (getLexer().equalToken(Token.LOCK)) {
                getLexer().nextToken();
                acceptIdentifier("TABLES");
                privilege = "LOCK TABLES";
            } else if (getLexer().identifierEquals("PROCESS")) {
                getLexer().nextToken();
                privilege = "PROCESS";
            } else if (getLexer().identifierEquals("RELOAD")) {
                getLexer().nextToken();
                privilege = "RELOAD";
            } else if (getLexer().identifierEquals("REPLICATION")) {
                getLexer().nextToken();
                if (getLexer().identifierEquals("SLAVE")) {
                    getLexer().nextToken();
                    privilege = "REPLICATION SLAVE";
                } else {
                    acceptIdentifier("CLIENT");
                    privilege = "REPLICATION CLIENT";
                }
            } else if (getLexer().equalToken(Token.SHOW)) {
                getLexer().nextToken();

                if (getLexer().equalToken(Token.VIEW)) {
                    getLexer().nextToken();
                    privilege = "SHOW VIEW";
                } else {
                    acceptIdentifier("DATABASES");
                    privilege = "SHOW DATABASES";
                }
            } else if (getLexer().identifierEquals("SHUTDOWN")) {
                getLexer().nextToken();
                privilege = "SHUTDOWN";
            } else if (getLexer().identifierEquals("SUPER")) {
                getLexer().nextToken();
                privilege = "SUPER";

            } else if (getLexer().identifierEquals("CONTROL")) { // sqlserver
                getLexer().nextToken();
                privilege = "CONTROL";
            } else if (getLexer().identifierEquals("IMPERSONATE")) { // sqlserver
                getLexer().nextToken();
                privilege = "IMPERSONATE";
            }

            if (privilege != null) {
                SQLExpr expr = new SQLIdentifierExpr(privilege);

                if (getLexer().equalToken(Token.LEFT_PAREN)) {
                    expr = this.exprParser.primaryRest(expr);
                }

                expr.setParent(parent);
                privileges.add(expr);
            }

            if (getLexer().equalToken(Token.COMMA)) {
                getLexer().nextToken();
                continue;
            }
            break;
        }
    }

    public SQLRevokeStatement parseRevoke() {
        accept(Token.REVOKE);

        SQLRevokeStatement stmt = new SQLRevokeStatement(getDbType());

        parsePrivileages(stmt.getPrivileges(), stmt);

        if (getLexer().equalToken(Token.ON)) {
            getLexer().nextToken();

            if (getLexer().equalToken(Token.PROCEDURE)) {
                getLexer().nextToken();
                stmt.setObjectType(SQLObjectType.PROCEDURE);
            } else if (getLexer().equalToken(Token.FUNCTION)) {
                getLexer().nextToken();
                stmt.setObjectType(SQLObjectType.FUNCTION);
            } else if (getLexer().equalToken(Token.TABLE)) {
                getLexer().nextToken();
                stmt.setObjectType(SQLObjectType.TABLE);
            } else if (getLexer().equalToken(Token.USER)) {
                getLexer().nextToken();
                stmt.setObjectType(SQLObjectType.USER);
            }

            SQLExpr expr = this.exprParser.expr();
            if (stmt.getObjectType() == SQLObjectType.TABLE || stmt.getObjectType() == null) {
                stmt.setOn(new SQLExprTableSource(expr));
            } else {
                stmt.setOn(expr);
            }
        }

        if (getLexer().equalToken(Token.FROM)) {
            getLexer().nextToken();
            stmt.setFrom(this.exprParser.expr());
        }

        return stmt;
    }

    public SQLStatement parseSavePoint() {
        acceptIdentifier("SAVEPOINT");
        SQLSavePointStatement stmt = new SQLSavePointStatement(getDbType());
        stmt.setName(this.exprParser.name());
        return stmt;
    }

    public SQLStatement parseReleaseSavePoint() {
        acceptIdentifier("RELEASE");
        acceptIdentifier("SAVEPOINT");
        SQLReleaseSavePointStatement stmt = new SQLReleaseSavePointStatement(getDbType());
        stmt.setName(this.exprParser.name());
        return stmt;
    }

    public SQLStatement parseAlter() {
        accept(Token.ALTER);

        if (getLexer().equalToken(Token.TABLE)) {
            getLexer().nextToken();

            SQLAlterTableStatement stmt = new SQLAlterTableStatement(getDbType());
            stmt.setName(this.exprParser.name());

            while (true) {
                if (getLexer().equalToken(Token.DROP)) {
                    parseAlterDrop(stmt);
                } else if (getLexer().identifierEquals("ADD")) {
                    getLexer().nextToken();

                    boolean ifNotExists = false;

                    if (getLexer().equalToken(Token.IF)) {
                        getLexer().nextToken();
                        accept(Token.NOT);
                        accept(Token.EXISTS);
                        ifNotExists = true;
                    }

                    if (getLexer().equalToken(Token.PRIMARY)) {
                        SQLPrimaryKey primaryKey = this.exprParser.parsePrimaryKey();
                        SQLAlterTableAddConstraint item = new SQLAlterTableAddConstraint(primaryKey);
                        stmt.getItems().add(item);
                    } else if (getLexer().equalToken(Token.IDENTIFIER)) {
                        SQLAlterTableAddColumn item = parseAlterTableAddColumn();
                        stmt.getItems().add(item);
                    } else if (getLexer().equalToken(Token.COLUMN)) {
                        getLexer().nextToken();
                        SQLAlterTableAddColumn item = parseAlterTableAddColumn();
                        stmt.getItems().add(item);
                    } else if (getLexer().equalToken(Token.CHECK)) {
                        SQLCheck check = this.exprParser.parseCheck();
                        SQLAlterTableAddConstraint item = new SQLAlterTableAddConstraint(check);
                        stmt.getItems().add(item);
                    } else if (getLexer().equalToken(Token.CONSTRAINT)) {
                        SQLConstraint constraint = this.exprParser.parseConstraint();
                        SQLAlterTableAddConstraint item = new SQLAlterTableAddConstraint(constraint);
                        stmt.getItems().add(item);
                    } else if (getLexer().equalToken(Token.FOREIGN)) {
                        SQLConstraint constraint = this.exprParser.parseForeignKey();
                        SQLAlterTableAddConstraint item = new SQLAlterTableAddConstraint(constraint);
                        stmt.getItems().add(item);
                    } else if (getLexer().equalToken(Token.PARTITION)) {
                        getLexer().nextToken();
                        SQLAlterTableAddPartition addPartition = new SQLAlterTableAddPartition();

                        addPartition.setIfNotExists(ifNotExists);

                        accept(Token.LEFT_PAREN);

                        parseAssignItems(addPartition.getPartition(), addPartition);

                        accept(Token.RIGHT_PAREN);

                        stmt.getItems().add(addPartition);
                    } else {
                        throw new ParserUnsupportedException(getLexer().getToken());
                    }
                } else if (getLexer().equalToken(Token.DISABLE)) {
                    getLexer().nextToken();

                    if (getLexer().equalToken(Token.CONSTRAINT)) {
                        getLexer().nextToken();
                        SQLAlterTableDisableConstraint item = new SQLAlterTableDisableConstraint();
                        item.setConstraintName(this.exprParser.name());
                        stmt.getItems().add(item);
                    } else if (getLexer().identifierEquals("LIFECYCLE")) {
                        getLexer().nextToken();
                        SQLAlterTableDisableLifecycle item = new SQLAlterTableDisableLifecycle();
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
                    } else if (getLexer().identifierEquals("LIFECYCLE")) {
                        getLexer().nextToken();
                        SQLAlterTableEnableLifecycle item = new SQLAlterTableEnableLifecycle();
                        stmt.getItems().add(item);
                    } else {
                        acceptIdentifier("KEYS");
                        SQLAlterTableEnableKeys item = new SQLAlterTableEnableKeys();
                        stmt.getItems().add(item);
                    }
                } else if (getLexer().equalToken(Token.ALTER)) {
                    getLexer().nextToken();
                    if (getLexer().equalToken(Token.COLUMN)) {
                        SQLAlterTableAlterColumn alterColumn = parseAlterColumn();
                        stmt.getItems().add(alterColumn);
                    } else if (getLexer().equalToken(Token.LITERAL_ALIAS)) {
                        SQLAlterTableAlterColumn alterColumn = parseAlterColumn();
                        stmt.getItems().add(alterColumn);
                    } else {
                        throw new ParserUnsupportedException(getLexer().getToken());
                    }
                } else if (getLexer().equalToken(Token.WITH)) {
                    getLexer().nextToken();
                    acceptIdentifier("NOCHECK");
                    acceptIdentifier("ADD");
                    SQLConstraint check = this.exprParser.parseConstraint();

                    SQLAlterTableAddConstraint addCheck = new SQLAlterTableAddConstraint();
                    addCheck.setWithNoCheck(true);
                    addCheck.setConstraint(check);
                    stmt.getItems().add(addCheck);
                } else if (getLexer().identifierEquals("RENAME")) {
                    stmt.getItems().add(parseAlterTableRename());
                } else if (getLexer().equalToken(Token.SET)) {
                    getLexer().nextToken();
                    
                    if (getLexer().equalToken(Token.COMMENT)) {
                        getLexer().nextToken();
                        SQLAlterTableSetComment setComment = new SQLAlterTableSetComment();
                        setComment.setComment(this.exprParser.primary());
                        stmt.getItems().add(setComment);
                    } else if (getLexer().identifierEquals("LIFECYCLE")) {
                        getLexer().nextToken();
                        SQLAlterTableSetLifecycle setLifecycle = new SQLAlterTableSetLifecycle();
                        setLifecycle.setLifecycle(this.exprParser.primary());
                        stmt.getItems().add(setLifecycle);
                    } else {
                        throw new ParserUnsupportedException(getLexer().getToken());
                    }
                } else if (getLexer().equalToken(Token.PARTITION)) {
                    getLexer().nextToken();

                    SQLAlterTableRenamePartition renamePartition = new SQLAlterTableRenamePartition();

                    accept(Token.LEFT_PAREN);

                    parseAssignItems(renamePartition.getPartition(), renamePartition);

                    accept(Token.RIGHT_PAREN);
                    
                    if (getLexer().equalToken(Token.ENABLE)) {
                        getLexer().nextToken();
                        if(getLexer().identifierEquals("LIFECYCLE")) {
                            getLexer().nextToken();
                        }
                        
                        SQLAlterTableEnableLifecycle enableLifeCycle = new SQLAlterTableEnableLifecycle();
                        for (SQLAssignItem condition : renamePartition.getPartition()) {
                            enableLifeCycle.getPartition().add(condition);
                            condition.setParent(enableLifeCycle);
                        }
                        stmt.getItems().add(enableLifeCycle);
                        
                        continue;
                    }
                    
                    if (getLexer().equalToken(Token.DISABLE)) {
                        getLexer().nextToken();
                        if(getLexer().identifierEquals("LIFECYCLE")) {
                            getLexer().nextToken();
                        }
                        
                        SQLAlterTableDisableLifecycle disableLifeCycle = new SQLAlterTableDisableLifecycle();
                        for (SQLAssignItem condition : renamePartition.getPartition()) {
                            disableLifeCycle.getPartition().add(condition);
                            condition.setParent(disableLifeCycle);
                        }
                        stmt.getItems().add(disableLifeCycle);
                        
                        continue;
                    }
                    
                    acceptIdentifier("RENAME");
                    accept(Token.TO);
                    accept(Token.PARTITION);
                    
                    accept(Token.LEFT_PAREN);

                    parseAssignItems(renamePartition.getTo(), renamePartition);

                    accept(Token.RIGHT_PAREN);

                    stmt.getItems().add(renamePartition);
                } else if(getLexer().identifierEquals("TOUCH")) {
                    getLexer().nextToken();
                    SQLAlterTableTouch item = new SQLAlterTableTouch();
                    
                    if (getLexer().equalToken(Token.PARTITION)) {
                        getLexer().nextToken();
    
                        accept(Token.LEFT_PAREN);
                        parseAssignItems(item.getPartition(), item);
                        accept(Token.RIGHT_PAREN);
                    }
                    
                    stmt.getItems().add(item);
                } else {
                    break;
                }
            }

            return stmt;
        } else if (getLexer().equalToken(Token.VIEW)) {
            getLexer().nextToken();
            SQLName viewName = this.exprParser.name();

            if (getLexer().identifierEquals("RENAME")) {
                getLexer().nextToken();
                accept(Token.TO);

                SQLAlterViewRenameStatement stmt = new SQLAlterViewRenameStatement();
                stmt.setName(viewName);

                SQLName newName = this.exprParser.name();

                stmt.setTo(newName);

                return stmt;
            }
            throw new ParserUnsupportedException(getLexer().getToken());
        }
        throw new ParserUnsupportedException(getLexer().getToken());
    }

    protected SQLAlterTableItem parseAlterTableRename() {
        acceptIdentifier("RENAME");

        if (getLexer().equalToken(Token.COLUMN)) {
            getLexer().nextToken();
            SQLAlterTableRenameColumn renameColumn = new SQLAlterTableRenameColumn();
            renameColumn.setColumn(this.exprParser.name());
            accept(Token.TO);
            renameColumn.setTo(this.exprParser.name());
            return renameColumn;
        }

        if (getLexer().equalToken(Token.TO)) {
            getLexer().nextToken();
            SQLAlterTableRename item = new SQLAlterTableRename();
            item.setTo(this.exprParser.name());
            return item;
        }
        throw new ParserUnsupportedException(getLexer().getToken());
    }

    protected SQLAlterTableAlterColumn parseAlterColumn() {
        getLexer().nextToken();
        SQLColumnDefinition column = this.exprParser.parseColumn();

        SQLAlterTableAlterColumn alterColumn = new SQLAlterTableAlterColumn();
        alterColumn.setColumn(column);
        return alterColumn;
    }

    public void parseAlterDrop(SQLAlterTableStatement stmt) {
        getLexer().nextToken();

        boolean ifNotExists = false;

        if (getLexer().equalToken(Token.IF)) {
            getLexer().nextToken();
            accept(Token.NOT);
            accept(Token.EXISTS);
            ifNotExists = true;
        }

        if (getLexer().equalToken(Token.CONSTRAINT)) {
            getLexer().nextToken();
            SQLAlterTableDropConstraint item = new SQLAlterTableDropConstraint();
            item.setConstraintName(this.exprParser.name());
            stmt.getItems().add(item);
        } else if (getLexer().equalToken(Token.COLUMN)) {
            getLexer().nextToken();
            SQLAlterTableDropColumnItem item = new SQLAlterTableDropColumnItem();
            this.exprParser.names(item.getColumns());

            if (getLexer().equalToken(Token.CASCADE)) {
                item.setCascade(true);
                getLexer().nextToken();
            }

            stmt.getItems().add(item);
        } else if (getLexer().equalToken(Token.LITERAL_ALIAS)) {
            SQLAlterTableDropColumnItem item = new SQLAlterTableDropColumnItem();
            this.exprParser.names(item.getColumns());

            if (getLexer().equalToken(Token.CASCADE)) {
                item.setCascade(true);
                getLexer().nextToken();
            }

            stmt.getItems().add(item);
        } else if (getLexer().equalToken(Token.PARTITION)) {
            getLexer().nextToken();
            SQLAlterTableDropPartition dropPartition = new SQLAlterTableDropPartition();

            dropPartition.setIfNotExists(ifNotExists);

            accept(Token.LEFT_PAREN);

            parseAssignItems(dropPartition.getPartition(), dropPartition);

            accept(Token.RIGHT_PAREN);
            
            if (getLexer().identifierEquals("PURGE")) {
                getLexer().nextToken();
                dropPartition.setPurge(true);
            }

            stmt.getItems().add(dropPartition);
        } else {
            throw new ParserUnsupportedException(getLexer().getToken());
        }
    }
    
    public SQLStatement parseRename() {
        throw new ParserUnsupportedException(getLexer().getToken());
    }
    
    protected SQLDropTableStatement parseDropTable(boolean acceptDrop) {
        if (acceptDrop) {
            accept(Token.DROP);
        }
        SQLDropTableStatement stmt = new SQLDropTableStatement(getDbType());
        if (getLexer().identifierEquals("TEMPORARY")) {
            getLexer().nextToken();
            stmt.setTemporary(true);
        }
        accept(Token.TABLE);
        if (getLexer().equalToken(Token.IF)) {
            getLexer().nextToken();
            accept(Token.EXISTS);
            stmt.setIfExists(true);
        }

        while (true) {
            SQLName name = this.exprParser.name();
            stmt.getTableSources().add(new SQLExprTableSource(name));
            if (getLexer().equalToken(Token.COMMA)) {
                getLexer().nextToken();
                continue;
            }
            break;
        }

        while (true) {
            if (getLexer().identifierEquals("RESTRICT")) {
                getLexer().nextToken();
                stmt.setRestrict(true);
                continue;
            }

            if (getLexer().identifierEquals("CASCADE")) {
                getLexer().nextToken();
                stmt.setCascade(true);

                if (getLexer().identifierEquals("CONSTRAINTS")) { // for oracle
                    getLexer().nextToken();
                }

                continue;
            }

            if (getLexer().equalToken(Token.PURGE)) {
                getLexer().nextToken();
                stmt.setPurge(true);
                continue;
            }

            break;
        }

        return stmt;
    }

    protected SQLDropSequenceStatement parseDropSequence(boolean acceptDrop) {
        if (acceptDrop) {
            accept(Token.DROP);
        }

        getLexer().nextToken();

        SQLName name = this.exprParser.name();

        SQLDropSequenceStatement stmt = new SQLDropSequenceStatement(getDbType());
        stmt.setName(name);
        return stmt;
    }

    protected SQLDropTriggerStatement parseDropTrigger(boolean acceptDrop) {
        if (acceptDrop) {
            accept(Token.DROP);
        }

        getLexer().nextToken();

        SQLName name = this.exprParser.name();

        SQLDropTriggerStatement stmt = new SQLDropTriggerStatement(getDbType());
        stmt.setName(name);
        return stmt;
    }

    protected SQLDropViewStatement parseDropView(boolean acceptDrop) {
        if (acceptDrop) {
            accept(Token.DROP);
        }

        SQLDropViewStatement stmt = new SQLDropViewStatement(getDbType());

        accept(Token.VIEW);

        if (getLexer().equalToken(Token.IF)) {
            getLexer().nextToken();
            accept(Token.EXISTS);
            stmt.setIfExists(true);
        }

        while (true) {
            SQLName name = this.exprParser.name();
            stmt.getTableSources().add(new SQLExprTableSource(name));
            if (getLexer().equalToken(Token.COMMA)) {
                getLexer().nextToken();
                continue;
            }
            break;
        }

        if (getLexer().identifierEquals("RESTRICT")) {
            getLexer().nextToken();
            stmt.setRestrict(true);
        } else if (getLexer().identifierEquals("CASCADE")) {
            getLexer().nextToken();

            if (getLexer().identifierEquals("CONSTRAINTS")) { // for oracle
                getLexer().nextToken();
            }

            stmt.setCascade(true);
        }

        return stmt;
    }

    protected SQLDropDatabaseStatement parseDropDatabase(boolean acceptDrop) {
        if (acceptDrop) {
            accept(Token.DROP);
        }

        SQLDropDatabaseStatement stmt = new SQLDropDatabaseStatement(getDbType());

        accept(Token.DATABASE);

        if (getLexer().equalToken(Token.IF)) {
            getLexer().nextToken();
            accept(Token.EXISTS);
            stmt.setIfExists(true);
        }

        SQLName name = this.exprParser.name();
        stmt.setDatabase(name);

        return stmt;
    }

    protected SQLDropFunctionStatement parseDropFunction(boolean acceptDrop) {
        if (acceptDrop) {
            accept(Token.DROP);
        }

        SQLDropFunctionStatement stmt = new SQLDropFunctionStatement(getDbType());

        accept(Token.FUNCTION);

        if (getLexer().equalToken(Token.IF)) {
            getLexer().nextToken();
            accept(Token.EXISTS);
            stmt.setIfExists(true);
        }

        SQLName name = this.exprParser.name();
        stmt.setName(name);

        return stmt;
    }

    protected SQLDropTableSpaceStatement parseDropTablespace(boolean acceptDrop) {
        SQLDropTableSpaceStatement stmt = new SQLDropTableSpaceStatement(getDbType());
        if (acceptDrop) {
            accept(Token.DROP);
        }
        
        accept(Token.TABLESPACE);

        if (getLexer().equalToken(Token.IF)) {
            getLexer().nextToken();
            accept(Token.EXISTS);
            stmt.setIfExists(true);
        }

        SQLName name = this.exprParser.name();
        stmt.setName(name);

        return stmt;
    }

    protected SQLDropProcedureStatement parseDropProcedure(boolean acceptDrop) {
        if (acceptDrop) {
            accept(Token.DROP);
        }

        SQLDropProcedureStatement stmt = new SQLDropProcedureStatement(getDbType());

        accept(Token.PROCEDURE);

        if (getLexer().equalToken(Token.IF)) {
            getLexer().nextToken();
            accept(Token.EXISTS);
            stmt.setIfExists(true);
        }

        SQLName name = this.exprParser.name();
        stmt.setName(name);

        return stmt;
    }

    public SQLStatement parseTruncate() {
        accept(Token.TRUNCATE);
        if (getLexer().equalToken(Token.TABLE)) {
            getLexer().nextToken();
        }
        SQLTruncateStatement stmt = new SQLTruncateStatement(getDbType());

        if (getLexer().equalToken(Token.ONLY)) {
            getLexer().nextToken();
            stmt.setOnly(true);
        }

        while (true) {
            SQLName name = this.exprParser.name();
            stmt.addTableSource(name);

            if (getLexer().equalToken(Token.COMMA)) {
                getLexer().nextToken();
                continue;
            }

            break;
        }

        while (true) {
            if (getLexer().equalToken(Token.PURGE)) {
                getLexer().nextToken();

                if (getLexer().identifierEquals("SNAPSHOT")) {
                    getLexer().nextToken();
                    acceptIdentifier("LOG");
                    stmt.setPurgeSnapshotLog(true);
                } else {
                    throw new ParserUnsupportedException(getLexer().getToken());
                }
                continue;
            }

            if (getLexer().equalToken(Token.RESTART)) {
                getLexer().nextToken();
                accept(Token.IDENTITY);
                stmt.setRestartIdentity(Boolean.TRUE);
                continue;
            } else if (getLexer().equalToken(Token.SHARE)) {
                getLexer().nextToken();
                accept(Token.IDENTITY);
                stmt.setRestartIdentity(Boolean.FALSE);
                continue;
            }

            if (getLexer().equalToken(Token.CASCADE)) {
                getLexer().nextToken();
                stmt.setCascade(Boolean.TRUE);
                continue;
            } else if (getLexer().equalToken(Token.RESTRICT)) {
                getLexer().nextToken();
                stmt.setCascade(Boolean.FALSE);
                continue;
            }

            break;
        }

        return stmt;
    }

    public SQLStatement parseInsert() {
        SQLInsertStatement insertStatement = new SQLInsertStatement();

        if (getLexer().equalToken(Token.INSERT)) {
            accept(Token.INSERT);
        }

        parseInsert0(insertStatement);
        return insertStatement;
    }

    protected void parseInsert0(SQLInsertInto insertStatement) {
        parseInsert0(insertStatement, true);
    }

    protected void parseInsert0_hinits(SQLInsertInto insertStatement) {

    }

    protected void parseInsert0(SQLInsertInto insertStatement, boolean acceptSubQuery) {
        if (getLexer().equalToken(Token.INTO)) {
            getLexer().nextToken();

            SQLName tableName = this.exprParser.name();
            insertStatement.setTableName(tableName);

            if (getLexer().equalToken(Token.LITERAL_ALIAS)) {
                insertStatement.setAlias(as());
            }

            parseInsert0_hinits(insertStatement);

            if (getLexer().equalToken(Token.IDENTIFIER)) {
                insertStatement.setAlias(getLexer().getLiterals());
                getLexer().nextToken();
            }
        }

        if (getLexer().equalToken(Token.LEFT_PAREN)) {
            getLexer().nextToken();
            this.exprParser.exprList(insertStatement.getColumns(), insertStatement);
            accept(Token.RIGHT_PAREN);
        }

        if (getLexer().equalToken(Token.VALUES)) {
            getLexer().nextToken();
            accept(Token.LEFT_PAREN);
            SQLInsertStatement.ValuesClause values = new SQLInsertStatement.ValuesClause();
            this.exprParser.exprList(values.getValues(), values);
            insertStatement.setValues(values);
            accept(Token.RIGHT_PAREN);
        } else if (acceptSubQuery && (getLexer().equalToken(Token.SELECT) || getLexer().equalToken(Token.LEFT_PAREN))) {
            SQLQueryExpr queryExpr = (SQLQueryExpr) this.exprParser.expr();
            insertStatement.setQuery(queryExpr.getSubQuery());
        }
    }

    public boolean parseStatementListDialect(List<SQLStatement> statementList) {
        return false;
    }

    public SQLDropUserStatement parseDropUser() {
        accept(Token.USER);

        SQLDropUserStatement stmt = new SQLDropUserStatement(getDbType());
        while (true) {
            SQLExpr expr = this.exprParser.expr();
            stmt.getUsers().add(expr);
            if (getLexer().equalToken(Token.COMMA)) {
                getLexer().nextToken();
                continue;
            }
            break;
        }

        return stmt;
    }

    public SQLStatement parseDropIndex() {
        accept(Token.INDEX);
        SQLDropIndexStatement stmt = new SQLDropIndexStatement(getDbType());
        stmt.setIndexName(this.exprParser.name());

        if (getLexer().equalToken(Token.ON)) {
            getLexer().nextToken();
            stmt.setTableName(this.exprParser.name());
        }
        return stmt;
    }

    public SQLCallStatement parseCall() {

        boolean brace = false;
        if (getLexer().equalToken(Token.LEFT_BRACE)) {
            getLexer().nextToken();
            brace = true;
        }

        SQLCallStatement stmt = new SQLCallStatement();

        if (getLexer().equalToken(Token.QUESTION)) {
            getLexer().nextToken();
            accept(Token.EQ);
            stmt.setOutParameter(new SQLVariantRefExpr("?"));
        }

        acceptIdentifier("CALL");

        stmt.setProcedureName(exprParser.name());

        if (getLexer().equalToken(Token.LEFT_PAREN)) {
            getLexer().nextToken();
            exprParser.exprList(stmt.getParameters(), stmt);
            accept(Token.RIGHT_PAREN);
        }

        if (brace) {
            accept(Token.RIGHT_BRACE);
            stmt.setBrace(true);
        }

        return stmt;
    }

    public SQLStatement parseSet() {
        accept(Token.SET);
        SQLSetStatement stmt = new SQLSetStatement(getDbType());

        parseAssignItems(stmt.getItems(), stmt);

        return stmt;
    }
    
    public void parseAssignItems(List<SQLAssignItem> items, SQLObject parent) {
        while (true) {
            SQLAssignItem item = exprParser.parseAssignItem();
            item.setParent(parent);
            items.add(item);
            if (getLexer().equalToken(Token.COMMA)) {
                getLexer().nextToken();
            } else {
                break;
            }
        }
    }
    
    public SQLStatement parseCreate() {
        int currentPosition = getLexer().getCurrentPosition();
        accept(Token.CREATE);
        Token token = getLexer().getToken();
        if (token == Token.TABLE || getLexer().identifierEquals("GLOBAL")) {
            SQLCreateTableParser createTableParser = getSQLCreateTableParser();
            return createTableParser.parseCrateTable(false);
            // TODO NONCLUSTERED only for sql server
        } else if (token == Token.INDEX || token == Token.UNIQUE || getLexer().identifierEquals("NONCLUSTERED")) {
            return parseCreateIndex(false);
        } else if (getLexer().equalToken(Token.SEQUENCE)) {
            return parseCreateSequence(false);
        } else if (token == Token.OR) {
            getLexer().nextToken();
            accept(Token.REPLACE);
            if (getLexer().equalToken(Token.PROCEDURE)) {
                getLexer().setCurrentPosition(currentPosition);
                getLexer().setToken(Token.CREATE);
                return parseCreateProcedure();
            }

            if (getLexer().equalToken(Token.VIEW)) {
                getLexer().setCurrentPosition(currentPosition);
                getLexer().setToken(Token.CREATE);
                return parseCreateView();
            }
            throw new ParserUnsupportedException(getLexer().getToken());
        } else if (token == Token.DATABASE) {
            getLexer().nextToken();
            if (getLexer().identifierEquals("LINK")) {
                getLexer().setCurrentPosition(currentPosition);
                getLexer().setToken(Token.CREATE);
                return parseCreateDbLink();
            }
            getLexer().setCurrentPosition(currentPosition);
            getLexer().setToken(Token.CREATE);
            return parseCreateDatabase();
        } else if (getLexer().identifierEquals("PUBLIC") || getLexer().identifierEquals("SHARE")) {
            getLexer().setCurrentPosition(currentPosition);
            getLexer().setToken(Token.CREATE);
            return parseCreateDbLink();
        } else if (token == Token.VIEW) {
            return parseCreateView();
        } else if (token == Token.TRIGGER) {
            return parseCreateTrigger();
        }
        throw new ParserUnsupportedException(getLexer().getToken());
    }
    
    public SQLStatement parseCreateDbLink() {
        throw new ParserUnsupportedException(getLexer().getToken());
    }
    
    public SQLStatement parseCreateTrigger() {
        accept(Token.TRIGGER);
        SQLCreateTriggerStatement stmt = new SQLCreateTriggerStatement(getDbType());
        stmt.setName(this.exprParser.name());
        if (getLexer().identifierEquals("BEFORE")) {
            stmt.setTriggerType(SQLCreateTriggerStatement.TriggerType.BEFORE);
            getLexer().nextToken();
        } else if (getLexer().identifierEquals("AFTER")) {
            stmt.setTriggerType(SQLCreateTriggerStatement.TriggerType.AFTER);
            getLexer().nextToken();
        } else if (getLexer().identifierEquals("INSTEAD")) {
            getLexer().nextToken();
            accept(Token.OF);
            stmt.setTriggerType(SQLCreateTriggerStatement.TriggerType.INSTEAD_OF);
        }

        while (true) {
            if (getLexer().equalToken(Token.INSERT)) {
                getLexer().nextToken();
                stmt.getTriggerEvents().add(SQLCreateTriggerStatement.TriggerEvent.INSERT);
                continue;
            }

            if (getLexer().equalToken(Token.UPDATE)) {
                getLexer().nextToken();
                stmt.getTriggerEvents().add(SQLCreateTriggerStatement.TriggerEvent.UPDATE);
                continue;
            }

            if (getLexer().equalToken(Token.DELETE)) {
                getLexer().nextToken();
                stmt.getTriggerEvents().add(SQLCreateTriggerStatement.TriggerEvent.DELETE);
                continue;
            }
            break;
        }

        accept(Token.ON);
        stmt.setOn(this.exprParser.name());

        if (getLexer().equalToken(Token.FOR)) {
            getLexer().nextToken();
            acceptIdentifier("EACH");
            accept(Token.ROW);
            stmt.setForEachRow(true);
        }

        List<SQLStatement> body = parseStatementList();
        if (body == null || body.isEmpty()) {
            throw new ParserException(getLexer());
        }
        stmt.setBody(body.get(0));
        return stmt;
    }

    public SQLStatement parseBlock() {
        throw new ParserUnsupportedException(getLexer().getToken());
    }

    public SQLStatement parseCreateDatabase() {
        if (getLexer().equalToken(Token.CREATE)) {
            getLexer().nextToken();
        }

        accept(Token.DATABASE);

        SQLCreateDatabaseStatement stmt = new SQLCreateDatabaseStatement(getDbType());
        stmt.setName(this.exprParser.name());
        return stmt;
    }

    public SQLStatement parseCreateProcedure() {
        throw new ParserUnsupportedException(getLexer().getToken());
    }

    public SQLStatement parseCreateSequence(boolean acceptCreate) {
        throw new ParserUnsupportedException(getLexer().getToken());
    }

    public SQLStatement parseCreateIndex(boolean acceptCreate) {
        if (acceptCreate) {
            accept(Token.CREATE);
        }

        SQLCreateIndexStatement stmt = new SQLCreateIndexStatement(getDbType());
        if (getLexer().equalToken(Token.UNIQUE)) {
            getLexer().nextToken();
            if (getLexer().identifierEquals("CLUSTERED")) {
                getLexer().nextToken();
                stmt.setType("UNIQUE CLUSTERED");
            } else {
                stmt.setType("UNIQUE");
            }
        } else if (getLexer().identifierEquals("FULLTEXT")) {
            stmt.setType("FULLTEXT");
            getLexer().nextToken();
        } else if (getLexer().identifierEquals("NONCLUSTERED")) {
            stmt.setType("NONCLUSTERED");
            getLexer().nextToken();
        }

        accept(Token.INDEX);

        stmt.setName(this.exprParser.name());

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

        return stmt;
    }
    
    public SQLCreateTableParser getSQLCreateTableParser() {
        return new SQLCreateTableParser(this.exprParser);
    }

    public SQLUpdateStatement parseUpdateStatement() {
        SQLUpdateStatement udpateStatement = createUpdateStatement();

        if (getLexer().equalToken(Token.UPDATE)) {
            getLexer().nextToken();

            SQLTableSource tableSource = this.exprParser.createSelectParser().parseTableSource();
            udpateStatement.setTableSource(tableSource);
        }

        parseUpdateSet(udpateStatement);

        if (getLexer().equalToken(Token.WHERE)) {
            getLexer().nextToken();
            udpateStatement.setWhere(this.exprParser.expr());
        }

        return udpateStatement;
    }

    protected void parseUpdateSet(SQLUpdateStatement update) {
        accept(Token.SET);

        while (true) {
            SQLUpdateSetItem item = this.exprParser.parseUpdateSetItem();
            update.addItem(item);

            if (!getLexer().equalToken(Token.COMMA)) {
                break;
            }

            getLexer().nextToken();
        }
    }

    protected SQLUpdateStatement createUpdateStatement() {
        return new SQLUpdateStatement(getDbType());
    }

    public SQLDeleteStatement parseDeleteStatement() {
        SQLDeleteStatement deleteStatement = new SQLDeleteStatement(getDbType());

        if (getLexer().equalToken(Token.DELETE)) {
            getLexer().nextToken();
            if (getLexer().equalToken(Token.FROM)) {
                getLexer().nextToken();
            }

            if (getLexer().equalToken(Token.COMMENT)) {
                getLexer().nextToken();
            }

            SQLName tableName = exprParser.name();

            deleteStatement.setTableName(tableName);
        }

        if (getLexer().equalToken(Token.WHERE)) {
            getLexer().nextToken();
            SQLExpr where = this.exprParser.expr();
            deleteStatement.setWhere(where);
        }

        return deleteStatement;
    }

    public SQLCreateViewStatement parseCreateView() {
        SQLCreateViewStatement createView = new SQLCreateViewStatement(getDbType());

        if (getLexer().equalToken(Token.CREATE)) {
            getLexer().nextToken();
        }

        if (getLexer().equalToken(Token.OR)) {
            getLexer().nextToken();
            accept(Token.REPLACE);
            createView.setOrReplace(true);
        }

        this.accept(Token.VIEW);

        if (getLexer().equalToken(Token.IF) || getLexer().identifierEquals("IF")) {
            getLexer().nextToken();
            accept(Token.NOT);
            accept(Token.EXISTS);
            createView.setIfNotExists(true);
        }

        createView.setName(exprParser.name());

        if (getLexer().equalToken(Token.LEFT_PAREN)) {
            getLexer().nextToken();

            while (true) {
                SQLCreateViewStatement.Column column = new SQLCreateViewStatement.Column();
                SQLExpr expr = this.exprParser.expr();
                column.setExpr(expr);

                if (getLexer().equalToken(Token.COMMENT)) {
                    getLexer().nextToken();
                    column.setComment((SQLCharExpr) exprParser.primary());
                }

                column.setParent(createView);
                createView.getColumns().add(column);

                if (getLexer().equalToken(Token.COMMA)) {
                    getLexer().nextToken();
                } else {
                    break;
                }
            }

            accept(Token.RIGHT_PAREN);
        }

        if (getLexer().equalToken(Token.COMMENT)) {
            getLexer().nextToken();
            SQLCharExpr comment = (SQLCharExpr) exprParser.primary();
            createView.setComment(comment);
        }

        this.accept(Token.AS);

        createView.setSubQuery(new SQLSelectParser(this.exprParser).select());
        return createView;
    }

    public SQLCommentStatement parseComment() {
        accept(Token.COMMENT);
        SQLCommentStatement stmt = new SQLCommentStatement();

        accept(Token.ON);

        if (getLexer().equalToken(Token.TABLE)) {
            stmt.setType(SQLCommentStatement.Type.TABLE);
            getLexer().nextToken();
        } else if (getLexer().equalToken(Token.COLUMN)) {
            stmt.setType(SQLCommentStatement.Type.COLUMN);
            getLexer().nextToken();
        }

        stmt.setOn(this.exprParser.name());

        accept(Token.IS);
        stmt.setComment(this.exprParser.expr());

        return stmt;
    }

    protected SQLAlterTableAddColumn parseAlterTableAddColumn() {
        SQLAlterTableAddColumn item = new SQLAlterTableAddColumn();

        while (true) {
            SQLColumnDefinition columnDef = this.exprParser.parseColumn();
            item.getColumns().add(columnDef);
            if (getLexer().equalToken(Token.COMMA)) {
                getLexer().nextToken();
                if (getLexer().identifierEquals("ADD")) {
                    break;
                }
                continue;
            }
            break;
        }
        return item;
    }
    
    public SQLExplainStatement parseExplain() {
        accept(Token.EXPLAIN);
        if (getLexer().identifierEquals("PLAN")) {
            getLexer().nextToken();
        }

        if (getLexer().equalToken(Token.FOR)) {
            getLexer().nextToken();
        }

        SQLExplainStatement explain = new SQLExplainStatement(getDbType());

        if (getLexer().equalToken(Token.HINT)) {
            explain.setHints(this.exprParser.parseHints());
        }

        explain.setStatement(parseStatement());

        return explain;
    }

    protected SQLAlterTableAddIndex parseAlterTableAddIndex() {
        SQLAlterTableAddIndex item = new SQLAlterTableAddIndex();

        if (getLexer().equalToken(Token.UNIQUE)) {
            item.setUnique(true);
            getLexer().nextToken();
            if (getLexer().equalToken(Token.INDEX)) {
                item.setKeyOrIndex(Token.INDEX.getName());
                getLexer().nextToken();
            } else if (getLexer().equalToken(Token.KEY)) {
                item.setKeyOrIndex(Token.KEY.getName());
                getLexer().nextToken();
            }
        } else {
            if (getLexer().equalToken(Token.INDEX)) {
                item.setKeyOrIndex(Token.INDEX.getName());
                accept(Token.INDEX);
            } else if (getLexer().equalToken(Token.KEY)) {
                item.setKeyOrIndex(Token.KEY.getName());
                accept(Token.KEY);
            }
        }

        if (getLexer().equalToken(Token.LEFT_PAREN)) {
            getLexer().nextToken();
        } else {
            item.setName(this.exprParser.name());
            accept(Token.LEFT_PAREN);
        }

        while (true) {
            SQLSelectOrderByItem column = this.exprParser.parseSelectOrderByItem();
            item.getItems().add(column);
            if (getLexer().equalToken(Token.COMMA)) {
                getLexer().nextToken();
                continue;
            }
            break;
        }
        accept(Token.RIGHT_PAREN);
        return item;
    }
    
    /**
     * parse cursor open statement
     */
    public SQLOpenStatement parseOpen() {
        SQLOpenStatement stmt=new SQLOpenStatement();
        accept(Token.OPEN);
        stmt.setCursorName(exprParser.name().getSimpleName());
        accept(Token.SEMI);
        return stmt;
    }
    
    public SQLFetchStatement parseFetch() {
        accept(Token.FETCH);
        
        SQLFetchStatement stmt = new SQLFetchStatement();
        stmt.setCursorName(this.exprParser.name());
        accept(Token.INTO);
        while (true) {
            stmt.getInto().add(this.exprParser.name());
            if (getLexer().equalToken(Token.COMMA)) {
                getLexer().nextToken();
                continue;
            }
            break;
        }
        
        return stmt;
    }
    
    public SQLStatement parseClose() {
        SQLCloseStatement stmt = new SQLCloseStatement();
        accept(Token.CLOSE);
        stmt.setCursorName(exprParser.name().getSimpleName());
        accept(Token.SEMI);
        return stmt;
    }
}
