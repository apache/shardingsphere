grammar MySQLStatement;

import MySQLKeyword, Keyword, MySQLComments, Symbol, MySQLDQLStatement, MySQLBase, MySQLDMLStatement, MySQLDDLStatement, MySQLTCLStatement, MySQLDCLStatement;

execute
    : (select
    | insert
    | update
    | delete
    | createIndex
    | dropIndex
    | createTable
    | alterTable
    | dropTable
    | truncateTable
    | beginTransaction
    | setAutoCommit
    | commit
    | rollback
    | setTransaction
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
