grammar MySQLStatement;

import MySQLKeyword, Keyword, MySQLBase, MySQLDQL, MySQLDML, DQLBase, DMLBase, MySQLCreateIndex
       , MySQLDropIndex, MySQLCreateTable, MySQLAlterTable, MySQLDropTable, MySQLTruncateTable
       , MySQLTCLStatement
       ;

execute
    : select
    | insert
    | update
    | delete
    | createIndex
    | dropIndex
    | createTable
    | alterTable
    | dropTable
    | truncateTable
    | setTransaction
    | commit
    | rollback
    | savepoint
    | beginWork
    | setVariable
    ;
 