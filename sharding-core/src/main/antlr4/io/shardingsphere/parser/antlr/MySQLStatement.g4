grammar MySQLStatement;

import MySQLKeyword, Keyword, MySQLBase, MySQLDQL, MySQLDML, DQLBase, DMLBase, MySQLCreateIndex, MySQLDropIndex, MySQLCreateTable, MySQLAlterTable, MySQLDropTable, MySQLTruncateTable;

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
    ;
 