package test;

import io.shardingsphere.parser.antlr.SQLServerStatementLexer;
import io.shardingsphere.parser.antlr.SQLServerStatementParser;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CodePointCharStream;
import org.antlr.v4.runtime.CommonTokenStream;

/**
 * @author maxiaoguang
 */
public class SQLServerAlterIndexTest {
    
    public static void main(String[] args) throws Exception {
        String[] alterIndexs = {
                "ALTER INDEX index1 ON table1 REBUILD",
                "ALTER INDEX ALL ON table1 REBUILD",
                "ALTER INDEX ALL ON dbo.table1 REBUILD",
                "ALTER INDEX idxcci_cci_target ON cci_target REORGANIZE WITH (COMPRESS_ALL_ROW_GROUPS = ON)",
                "ALTER INDEX cci_FactInternetSales2 ON FactInternetSales2 REORGANIZE",
                "ALTER INDEX cci_FactInternetSales2 ON FactInternetSales2 REORGANIZE PARTITION = 0",
                "ALTER INDEX cci_FactInternetSales2 ON FactInternetSales2 REORGANIZE WITH (COMPRESS_ALL_ROW_GROUPS = ON)",
                "ALTER INDEX cci_FactInternetSales2 ON FactInternetSales2 REORGANIZE PARTITION = 0 WITH (COMPRESS_ALL_ROW_GROUPS = ON)",
                "ALTER INDEX cci_FactInternetSales2 ON FactInternetSales2 REBUILD",
                "ALTER INDEX cci_fact3 ON fact3 REBUILD PARTITION = 12",
                "ALTER INDEX cci_SimpleTable ON SimpleTable REBUILD WITH (DATA_COMPRESSION = COLUMNSTORE_ARCHIVE)",
                "ALTER INDEX cci_SimpleTable ON SimpleTable REBUILD WITH (DATA_COMPRESSION = COLUMNSTORE)",
                "ALTER INDEX PK_Employee_EmployeeID ON HumanResources.Employee REBUILD",
                "ALTER INDEX ALL ON Production.Product REBUILD WITH (FILLFACTOR = 80, SORT_IN_TEMPDB = ON, STATISTICS_NORECOMPUTE = ON)",
                "ALTER INDEX ALL ON Production.Product  \n" +
                        "REBUILD WITH   \n" +
                        "(  \n" +
                        "    FILLFACTOR = 80,   \n" +
                        "    SORT_IN_TEMPDB = ON,  \n" +
                        "    STATISTICS_NORECOMPUTE = ON,  \n" +
                        "    ONLINE = ON ( WAIT_AT_LOW_PRIORITY ( MAX_DURATION = 4 MINUTES, ABORT_AFTER_WAIT = BLOCKERS ) ),   \n" +
                        "    DATA_COMPRESSION = ROW  \n" +
                        ")",
//                "ALTER INDEX PK_ProductPhoto_ProductPhotoID ON Production.ProductPhoto REORGANIZE WITH (LOB_COMPACTION)",
                "ALTER INDEX AK_SalesOrderHeader_SalesOrderNumber ON  \n" +
                        "    Sales.SalesOrderHeader  \n" +
                        "SET (  \n" +
                        "    STATISTICS_NORECOMPUTE = ON,  \n" +
                        "    IGNORE_DUP_KEY = ON,  \n" +
                        "    ALLOW_PAGE_LOCKS = ON  \n" +
                        "    )",
                "ALTER INDEX IX_Employee_ManagerID ON HumanResources.Employee DISABLE",
                "ALTER INDEX PK_Department_DepartmentID ON HumanResources.Department DISABLE",
                "ALTER INDEX PK_Department_DepartmentID ON HumanResources.Department REBUILD",
                "ALTER INDEX IX_TransactionHistory_TransactionDate  \n" +
                        "ON Production.TransactionHistory  \n" +
                        "REBUILD Partition = 5   \n" +
                        "   WITH (ONLINE = ON (WAIT_AT_LOW_PRIORITY (MAX_DURATION = 10 minutes, ABORT_AFTER_WAIT = SELF)))",
                "ALTER INDEX IX_INDEX1   \n" +
                        "ON T1  \n" +
                        "REBUILD   \n" +
                        "WITH (DATA_COMPRESSION = PAGE)",
                "ALTER INDEX test_idx on test_table REBUILD WITH (ONLINE=ON, MAXDOP=1, RESUMABLE=ON)",
                "ALTER INDEX test_idx on test_table REBUILD WITH (ONLINE=ON, RESUMABLE=ON, MAX_DURATION=240)",
                "ALTER INDEX test_idx on test_table PAUSE",
                "ALTER INDEX test_idx on test_table RESUME WITH (MAXDOP=4)",
                "ALTER INDEX test_idx on test_table  \n" +
                        "      RESUME WITH (MAXDOP=2, MAX_DURATION= 240 MINUTES, \n" +
                        "      WAIT_AT_LOW_PRIORITY (MAX_DURATION=10, ABORT_AFTER_WAIT=BLOCKERS))",
                "ALTER INDEX test_idx on test_table ABORT"
        };
    
        for (String alterIndex : alterIndexs) {
//            System.out.println("Input expr : " + alterIndex);
            alterIndex(alterIndex);
        }
    }
    
    public static void alterIndex(String expr) throws Exception{
        CodePointCharStream cs = CharStreams.fromString(expr);
        SQLServerStatementLexer lexer = new SQLServerStatementLexer(cs);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        SQLServerStatementParser parser = new SQLServerStatementParser(tokens);
        parser.execute();
    }
    
}
