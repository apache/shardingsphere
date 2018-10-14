package test;

import io.shardingsphere.parser.antlr.SQLServerStatementLexer;
import io.shardingsphere.parser.antlr.SQLServerStatementParser;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CodePointCharStream;
import org.antlr.v4.runtime.CommonTokenStream;

/**
 * @author maxiaoguang
 */
public class SQLServerTruncateTest {
    
    public static void main(String[] args) throws Exception {
        String[] truncates = {
                "TRUNCATE TABLE HumanResources.JobCandidate",
                "TRUNCATE TABLE PartitionTable1 WITH (PARTITIONS (2, 4, 6 TO 8))"
        };
    
        for (String truncate : truncates) {
            System.out.println("Input expr : " + truncate);
            truncate(truncate);
        }
    }
    
    public static void truncate(String expr) throws Exception{
        CodePointCharStream cs = CharStreams.fromString(expr);
        SQLServerStatementLexer lexer = new SQLServerStatementLexer(cs);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        SQLServerStatementParser parser = new SQLServerStatementParser(tokens);
        parser.execute();
    }
    
}
