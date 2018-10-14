package test;

import io.shardingsphere.parser.antlr.PostgreStatementLexer;
import io.shardingsphere.parser.antlr.PostgreStatementParser;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CodePointCharStream;
import org.antlr.v4.runtime.CommonTokenStream;

/**
 * @author maxiaoguang
 */
public class PostgreAlterIndexTest {
    
    public static void main(String[] args) throws Exception {
        String[] alterIndexs = {
                "ALTER INDEX distributors RENAME TO suppliers",
                "ALTER INDEX distributors SET TABLESPACE fasttablespace",
                "ALTER INDEX distributors SET (fillfactor = 75)"
        };
    
        for (String alterIndex : alterIndexs) {
//            System.out.println("Input expr : " + alterIndex);
            alterIndex(alterIndex);
        }
    }
    
    public static void alterIndex(String expr) throws Exception{
        CodePointCharStream cs = CharStreams.fromString(expr);
        PostgreStatementLexer lexer = new PostgreStatementLexer(cs);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        PostgreStatementParser parser = new PostgreStatementParser(tokens);
        parser.execute();
    }
    
}
