package test;

import io.shardingsphere.parser.antlr.SQLServerStatementLexer;
import io.shardingsphere.parser.antlr.SQLServerStatementParser;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CodePointCharStream;
import org.antlr.v4.runtime.CommonTokenStream;

/**
 * @author maxiaoguang
 */
public class SQLServerDropTableTest {
    
    public static void main(String[] args) throws Exception {
        String[] dropTables = {
                "DROP TABLE ProductVendor1",
                "DROP TABLE AdventureWorks2012.dbo.SalesPerson2",
                "DROP TABLE #temptable",
                "DROP TABLE IF EXISTS T1",
                "DROP TABLE IF EXISTS T1, T2"
        };
    
        for (String dropTable : dropTables) {
            System.out.println("Input expr : " + dropTable);
            dropTable(dropTable);
        }
    }
    
    public static void dropTable(String expr) throws Exception{
        CodePointCharStream cs = CharStreams.fromString(expr);
        SQLServerStatementLexer lexer = new SQLServerStatementLexer(cs);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        SQLServerStatementParser parser = new SQLServerStatementParser(tokens);
        parser.execute();
    }
    
}
