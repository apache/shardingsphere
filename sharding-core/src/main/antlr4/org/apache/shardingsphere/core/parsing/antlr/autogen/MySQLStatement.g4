grammar MySQLStatement;

import MySQLKeyword, Keyword, MySQLComments, MySQLDQLStatement, MySQLBase, Symbol, MySQLDMLStatement, MySQLDDLStatement, MySQLTCLStatement, MySQLDCLStatement
    ;

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
    | grantProxy
    | grantRole
    | revoke
    | revokeAll
    | revokeProxy
    | revokeRole
    | createUser
    | alterUser
    | alterCurrentUser
    | alterUserRole
    | dropUser
    | renameUser
    | createRole
    | dropRole
    | setPassword
    | setDefaultRole
    | setRole
    )SEMI_? 
    ;
