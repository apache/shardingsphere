package test;

import io.shardingsphere.parser.antlr.PostgreStatementLexer;
import io.shardingsphere.parser.antlr.PostgreStatementParser;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CodePointCharStream;
import org.antlr.v4.runtime.CommonTokenStream;

/**
 * @author maxiaoguang
 */
public class PostgreDropTableTest {
    
    public static void main(String[] args) throws Exception {
        String[] dropTables = {
                "DROP TABLE films, distributors"
        };
    
        for (String dropTable : dropTables) {
            System.out.println("Input expr : " + dropTable);
            dropTable(dropTable);
        }
    }
    
    public static void dropTable(String expr) throws Exception{
        CodePointCharStream cs = CharStreams.fromString(expr);
        PostgreStatementLexer lexer = new PostgreStatementLexer(cs);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        PostgreStatementParser parser = new PostgreStatementParser(tokens);
        parser.execute();
    }
    
}
