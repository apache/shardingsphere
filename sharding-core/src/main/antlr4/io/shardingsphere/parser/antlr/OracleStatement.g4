grammar OracleStatement;

import OracleKeyword, Keyword, OracleBase, OracleCreateIndex, OracleAlterIndex, OracleDropIndex, OracleCreateTable, OracleAlterTable, OracleDropTable, OracleTruncateTable;

execute
    : createIndex
    | alterIndex
    | dropIndex
    | createTable
    | alterTable
    | dropTable
    | truncateTable
    ;
 