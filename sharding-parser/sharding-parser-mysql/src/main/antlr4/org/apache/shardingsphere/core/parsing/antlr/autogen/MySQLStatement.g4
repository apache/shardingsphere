grammar MySQLStatement;

import MySQLKeyword, Keyword, MySQLComments, Symbol, MySQLDQLStatement, MySQLBase, MySQLDMLStatement, MySQLDDLStatement, MySQLTCLStatement, MySQLDCLStatement;

execute
    : (select
    | insert
    | update
    | delete
    | createTable
    | alterTable
    | dropTable
    | truncateTable
    | createIndex
    | dropIndex
    | setTransaction
    | beginTransaction
    | setAutoCommit
    | commit
    | rollback
    | savepoint
    | grant
    | revoke
    | createUser
    | dropUser
    | alterUser
    | renameUser
    | createRole
    | dropRole
    | setRole
    | setPassword
    )SEMI_? 
    ;
