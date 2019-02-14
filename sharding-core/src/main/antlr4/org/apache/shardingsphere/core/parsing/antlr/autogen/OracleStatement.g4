grammar OracleStatement;

import OracleKeyword, Keyword, Symbol, OracleBase, OracleDDLStatement, OracleTCLStatement, OracleDCLStatement
       ;

execute
    : (createIndex
    | alterIndex
    | dropIndex
    | createTable
    | alterTable
    | dropTable
    | truncateTable
    | commit
    | rollback
    | setTransaction
    | savepoint
    | grant
    | revoke
    | createUser
    | alterUser
    | dropUser
    | createRole
    | alterRole
    | dropRole
    ) SEMI_?
    ;
