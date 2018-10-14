package test;

import io.shardingsphere.parser.antlr.MySQLStatementLexer;
import io.shardingsphere.parser.antlr.MySQLStatementParser;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CodePointCharStream;
import org.antlr.v4.runtime.CommonTokenStream;

/**
 * @author maxiaoguang
 */
public class MySQLDropTableTest {
    
    public static void main(String[] args) throws Exception {
        String[] dropTables = {
            "drop temporary table if exists temp_t1",
            "drop temporary table `some_temp_table`",
            "drop table antlr_all_tokens, antlr_function_tokens, antlr_keyword_tokens, antlr_tokens, childtable, guns, log_table, new_t, parenttable, ship_class, ships, ships_guns, t1, t2, t3, t4, tab1"
        };
    
        for (String dropTable : dropTables) {
//            System.out.println("Input expr : " + dropTable);
            dropTable(dropTable);
        }
    }
    
    public static void dropTable(String expr) throws Exception{
        CodePointCharStream cs = CharStreams.fromString(expr);
        MySQLStatementLexer lexer = new MySQLStatementLexer(cs);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        MySQLStatementParser parser = new MySQLStatementParser(tokens);
        parser.execute();
    }
    
}
