package test;

import io.shardingsphere.core.parsing.antler.ast.MySQLStatementParseTreeBuilder;
import io.shardingsphere.parser.antlr.MySQLStatementLexer;
import io.shardingsphere.parser.antlr.MySQLStatementParser;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CodePointCharStream;
import org.antlr.v4.runtime.CommonTokenStream;

/**
 * @author maxiaoguang
 */
public class MySQLCreateIndexTest {
    
    public static void main(String[] args) throws Exception {
        String[] createIndexs = {
                "CREATE INDEX part_of_name ON customer (name(10))",
                "CREATE INDEX id_index ON lookup (id) USING BTREE",
                "CREATE INDEX id_index ON t1 (id) COMMENT 'MERGE_THRESHOLD=40'",
                "create index index1 on t1(col1) comment 'test index' comment 'some test' using btree",
                "create unique index index2 using btree on t2(_1c desc, `_` asc)",
                "create index index3 using hash on antlr_tokens(token(30) asc)"
        };
    
        for (String createIndex : createIndexs) {
//            System.out.println("Input expr : " + createIndex);
            createIndex(createIndex);
        }
    }
    
    public static void createIndex(String expr) throws Exception{
        new MySQLStatementParseTreeBuilder().parse(expr);
        CodePointCharStream cs = CharStreams.fromString(expr);
        MySQLStatementLexer lexer = new MySQLStatementLexer(cs);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        MySQLStatementParser parser = new MySQLStatementParser(tokens);
        parser.execute();
    }
    
}
