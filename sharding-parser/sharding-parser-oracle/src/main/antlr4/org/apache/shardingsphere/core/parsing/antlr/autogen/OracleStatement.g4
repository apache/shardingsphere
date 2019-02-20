grammar OracleStatement;

import OracleKeyword, Keyword, Symbol, OracleBase, OracleDDLStatement, OracleTCLStatement, OracleDCLStatement;

execute
    : (createTable
    | alterTable
    | dropTable
    | truncateTable
    | createIndex
    | dropIndex
    | alterIndex
    | commit
    | rollback
    | setTransaction
    | savepoint
    | grant
    | revoke
    | createUser
    | dropUser
    | alterUser
    | createRole
    | dropRole
    | alterRole
    ) SEMI_?
    ;
