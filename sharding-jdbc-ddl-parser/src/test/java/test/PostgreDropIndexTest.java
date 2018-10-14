package test;

import io.shardingsphere.parser.antlr.PostgreStatementLexer;
import io.shardingsphere.parser.antlr.PostgreStatementParser;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CodePointCharStream;
import org.antlr.v4.runtime.CommonTokenStream;

/**
 * @author maxiaoguang
 */
public class PostgreDropIndexTest {
    
    public static void main(String[] args) throws Exception {
        String[] dropIndexs = {
                "DROP INDEX title_idx"
        };
    
        for (String dropIndex : dropIndexs) {
            System.out.println("Input expr : " + dropIndex);
            dropIndex(dropIndex);
        }
    }
    
    public static void dropIndex(String expr) throws Exception{
        CodePointCharStream cs = CharStreams.fromString(expr);
        PostgreStatementLexer lexer = new PostgreStatementLexer(cs);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        PostgreStatementParser parser = new PostgreStatementParser(tokens);
        parser.execute();
    }
    
}
