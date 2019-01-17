grammar OracleStatement;

import OracleKeyword, Keyword, OracleBase, Symbol, OracleCreateIndex, OracleAlterIndex
       , OracleDropIndex, OracleCreateTable, OracleAlterTable, OracleDropTable, OracleTruncateTable
       , OracleTCLStatement, OracleDCLStatement
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
