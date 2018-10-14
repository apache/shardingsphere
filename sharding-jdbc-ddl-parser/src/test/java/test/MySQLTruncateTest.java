package test;

import io.shardingsphere.parser.antlr.MySQLStatementLexer;
import io.shardingsphere.parser.antlr.MySQLStatementParser;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CodePointCharStream;
import org.antlr.v4.runtime.CommonTokenStream;

/**
 * @author maxiaoguang
 */
public class MySQLTruncateTest {
    
    public static void main(String[] args) throws Exception {
        String[] truncates = {
                "TRUNCATE bigtable"
        };
    
        for (String truncate : truncates) {
//            System.out.println("Input expr : " + truncate);
            truncate(truncate);
        }
    }
    
    public static void truncate(String expr) throws Exception{
        CodePointCharStream cs = CharStreams.fromString(expr);
        MySQLStatementLexer lexer = new MySQLStatementLexer(cs);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        MySQLStatementParser parser = new MySQLStatementParser(tokens);
        parser.execute();
    }
    
}
