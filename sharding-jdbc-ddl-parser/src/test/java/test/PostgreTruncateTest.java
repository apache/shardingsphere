package test;

import io.shardingsphere.parser.antlr.PostgreStatementLexer;
import io.shardingsphere.parser.antlr.PostgreStatementParser;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CodePointCharStream;
import org.antlr.v4.runtime.CommonTokenStream;

/**
 * @author maxiaoguang
 */
public class PostgreTruncateTest {
    
    public static void main(String[] args) throws Exception {
        String[] truncates = {
                "TRUNCATE bigtable *, fattable",
                "TRUNCATE bigtable, fattable * RESTART IDENTITY",
                "TRUNCATE othertable CASCADE"
        };
    
        for (String truncate : truncates) {
            System.out.println("Input expr : " + truncate);
            truncate(truncate);
        }
    }
    
    public static void truncate(String expr) throws Exception{
        CodePointCharStream cs = CharStreams.fromString(expr);
        PostgreStatementLexer lexer = new PostgreStatementLexer(cs);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        PostgreStatementParser parser = new PostgreStatementParser(tokens);
        parser.execute();
    }
    
}
