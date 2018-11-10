grammar OracleStatement;

import OracleKeyword, Keyword, OracleBase, OracleCreateIndex, OracleAlterIndex
       , OracleDropIndex, OracleCreateTable, OracleAlterTable, OracleDropTable, OracleTruncateTable
       , OracleTCLStatement, OracleDCLStatement
       ;

execute
    : createIndex
    | alterIndex
    | dropIndex
    | createTable
    | alterTable
    | dropTable
    | truncateTable
    | setTransaction
    | commit
    | rollback
    | savepoint
    | grant
    | revoke
    | createUser
    | alterUser
    | dropUser
    | createRole
    | alterRole
    | dropRole
    ;
