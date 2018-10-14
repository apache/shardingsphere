package test;

import io.shardingsphere.parser.antlr.PostgreStatementLexer;
import io.shardingsphere.parser.antlr.PostgreStatementParser;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CodePointCharStream;
import org.antlr.v4.runtime.CommonTokenStream;

/**
 * @author maxiaoguang
 */
public class PostgreAlterTableTest {
    
    public static void main(String[] args) throws Exception {
        String[] alterTables = {
                "ALTER TABLE distributors ADD COLUMN address varchar(30)",
                "ALTER TABLE distributors DROP COLUMN address RESTRICT",
                "ALTER TABLE distributors\n" +
                        "    ALTER COLUMN address TYPE varchar(80),\n" +
                        "    ALTER COLUMN name TYPE varchar(100)",
                "ALTER TABLE foo\n" +
                        "    ALTER COLUMN foo_timestamp SET DATA TYPE timestamp with time zone\n" +
                        "    USING\n" +
                        "        timestamp with time zone 'epoch' + foo_timestamp * interval '1 second'",
                "ALTER TABLE foo\n" +
                        "    ALTER COLUMN foo_timestamp DROP DEFAULT,\n" +
                        "    ALTER COLUMN foo_timestamp TYPE timestamp with time zone\n" +
                        "    USING\n" +
                        "        timestamp with time zone 'epoch' + foo_timestamp * interval '1 second',\n" +
                        "    ALTER COLUMN foo_timestamp SET DEFAULT now()",
                "ALTER TABLE distributors RENAME COLUMN address TO city",
                "ALTER TABLE distributors RENAME TO suppliers",
                "ALTER TABLE distributors RENAME CONSTRAINT zipchk TO zip_check",
                "ALTER TABLE distributors ALTER COLUMN street SET NOT NULL",
                "ALTER TABLE distributors ALTER COLUMN street DROP NOT NULL",
                "ALTER TABLE distributors ADD CONSTRAINT zipchk CHECK (char_length(zipcode) = 5)",
                "ALTER TABLE distributors ADD CONSTRAINT zipchk CHECK (char_length(zipcode) = 5) NO INHERIT",
                "ALTER TABLE distributors DROP CONSTRAINT zipchk",
                "ALTER TABLE ONLY distributors DROP CONSTRAINT zipchk",
                "ALTER TABLE distributors ADD CONSTRAINT distfk FOREIGN KEY (address) REFERENCES addresses (address)",
                "ALTER TABLE distributors ADD CONSTRAINT distfk FOREIGN KEY (address) REFERENCES addresses (address) NOT VALID",
                "ALTER TABLE distributors VALIDATE CONSTRAINT distfk",
                "ALTER TABLE distributors ADD CONSTRAINT dist_id_zipcode_key UNIQUE (dist_id, zipcode)",
                "ALTER TABLE distributors ADD PRIMARY KEY (dist_id)",
                "ALTER TABLE distributors SET TABLESPACE fasttablespace",
                "ALTER TABLE myschema.distributors SET SCHEMA yourschema",
                "ALTER TABLE distributors DROP CONSTRAINT distributors_pkey,\n" +
                        "    ADD CONSTRAINT distributors_pkey PRIMARY KEY USING INDEX dist_id_temp_idx",
                "ALTER TABLE measurement\n" +
                        "    ATTACH PARTITION measurement_y2016m07 FOR VALUES FROM ('2016-07-01') TO ('2016-08-01')",
                "ALTER TABLE cities\n" +
                        "    ATTACH PARTITION cities_ab FOR VALUES IN ('a', 'b')",
                "ALTER TABLE measurement\n" +
                        "    DETACH PARTITION measurement_y2015m12"
        };
    
        for (String alterTable : alterTables) {
//            System.out.println("Input expr : " + alterTable);
            alterTable(alterTable);
        }
    }
    
    public static void alterTable(String expr) throws Exception{
        CodePointCharStream cs = CharStreams.fromString(expr);
        PostgreStatementLexer lexer = new PostgreStatementLexer(cs);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        PostgreStatementParser parser = new PostgreStatementParser(tokens);
        parser.execute();
    }
    
}
