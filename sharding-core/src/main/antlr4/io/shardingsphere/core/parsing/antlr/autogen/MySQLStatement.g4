grammar MySQLStatement;

import MySQLKeyword, Keyword, MySQLBase, MySQLDQL, MySQLDML, DQLBase, DMLBase, MySQLCreateIndex
       , MySQLDropIndex, MySQLCreateTable, MySQLAlterTable, MySQLDropTable, MySQLTruncateTable
       , MySQLTCLStatement, MySQLDCLStatement
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
    | grantPriveleges
    | grantProxy
    | grantRoles
    | revokePriveleges
    | revokeAllPriveleges
    | revokeProxy
    | revokeRoles
    | createUser
    | alterUser
    | alterCurrentUser
    | alterUserRole
    ;
 