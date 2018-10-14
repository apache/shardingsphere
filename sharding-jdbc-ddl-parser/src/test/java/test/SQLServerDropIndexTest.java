package test;

import io.shardingsphere.parser.antlr.SQLServerStatementLexer;
import io.shardingsphere.parser.antlr.SQLServerStatementParser;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CodePointCharStream;
import org.antlr.v4.runtime.CommonTokenStream;

/**
 * @author maxiaoguang
 */
public class SQLServerDropIndexTest {
    
    public static void main(String[] args) throws Exception {
        String[] dropIndexs = {
                "DROP INDEX sxi_index ON tbl"
        };
    
        for (String dropIndex : dropIndexs) {
            System.out.println("Input expr : " + dropIndex);
            dropIndex(dropIndex);
        }
    }
    
    public static void dropIndex(String expr) throws Exception{
        CodePointCharStream cs = CharStreams.fromString(expr);
        SQLServerStatementLexer lexer = new SQLServerStatementLexer(cs);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        SQLServerStatementParser parser = new SQLServerStatementParser(tokens);
        parser.execute();
    }
    
}
