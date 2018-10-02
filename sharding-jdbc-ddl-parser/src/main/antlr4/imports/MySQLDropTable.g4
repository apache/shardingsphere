grammar MySQLDropTable;
import MySQLKeyword, Keyword, BaseRule;

dropTable
    : DROP TEMPORARY? TABLE ifExists?
    tableNames
    ;