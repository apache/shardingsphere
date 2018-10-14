package test;

import io.shardingsphere.parser.antlr.MySQLStatementLexer;
import io.shardingsphere.parser.antlr.MySQLStatementParser;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CodePointCharStream;
import org.antlr.v4.runtime.CommonTokenStream;

/**
 * @author maxiaoguang
 */
public class MySQLDropIndexTest {
    
    public static void main(String[] args) throws Exception {
        String[] dropIndexs = {
                "drop index index1 on t1 algorithm=default",
                "drop index index2 on t2 algorithm=default lock none",
                "drop index index3 on antlr_tokens algorithm default lock=none"
        };
    
        for (String dropIndex : dropIndexs) {
//            System.out.println("Input expr : " + dropIndex);
            dropIndex(dropIndex);
        }
    }
    
    public static void dropIndex(String expr) throws Exception{
        CodePointCharStream cs = CharStreams.fromString(expr);
        MySQLStatementLexer lexer = new MySQLStatementLexer(cs);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        MySQLStatementParser parser = new MySQLStatementParser(tokens);
        parser.execute();
    }
    
}
