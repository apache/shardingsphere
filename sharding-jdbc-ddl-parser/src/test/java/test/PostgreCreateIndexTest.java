package test;

import io.shardingsphere.parser.antlr.PostgreStatementLexer;
import io.shardingsphere.parser.antlr.PostgreStatementParser;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CodePointCharStream;
import org.antlr.v4.runtime.CommonTokenStream;

/**
 * @author maxiaoguang
 */
public class PostgreCreateIndexTest {
    
    public static void main(String[] args) throws Exception {
        String[] createIndexs = {
                "CREATE UNIQUE INDEX title_idx ON films (title)",
                "CREATE INDEX title_idx_german ON films (title COLLATE \"de_DE\")",
                "CREATE INDEX title_idx_nulls_low ON films (title NULLS FIRST)",
                "CREATE UNIQUE INDEX title_idx ON films (title) WITH (fillfactor = 70)",
                "CREATE INDEX gin_idx ON documents_table USING GIN (locations) WITH (fastupdate = off)",
                "CREATE INDEX code_idx ON films (code) TABLESPACE indexspace",
                "CREATE INDEX pointloc ON points USING gist ((box(location,location)))",
                "CREATE INDEX CONCURRENTLY sales_quantity_index ON sales_table (quantity)",
                "CREATE UNIQUE INDEX title_idx ON films (title) WHERE title = 'test'"
        };
    
        for (String createIndex : createIndexs) {
//            System.out.println("Input expr : " + createIndex);
            createIndex(createIndex);
        }
    }
    
    public static void createIndex(String expr) throws Exception{
        CodePointCharStream cs = CharStreams.fromString(expr);
        PostgreStatementLexer lexer = new PostgreStatementLexer(cs);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        PostgreStatementParser parser = new PostgreStatementParser(tokens);
        parser.execute();
    }
    
}
