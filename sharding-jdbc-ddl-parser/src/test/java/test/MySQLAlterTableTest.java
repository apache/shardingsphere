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
public class MySQLAlterTableTest {
    
    public static void main(String[] args) throws Exception {
        String[] alterTables = {
                "ALTER TABLE t2 DROP COLUMN c, DROP COLUMN d",
                "ALTER TABLE t1 ENGINE = InnoDB",
                "ALTER TABLE t1 ROW_FORMAT = COMPRESSED",
                "ALTER TABLE t1 ENCRYPTION='Y'",
                "ALTER TABLE t1 ENCRYPTION='N'",
                "ALTER TABLE t1 AUTO_INCREMENT = 13",
                "ALTER TABLE t1 CHARACTER SET = utf8",
                "ALTER TABLE t1 COMMENT = 'New table comment'",
                "ALTER TABLE distributors DROP COLUMN address RESTRICT",
                "ALTER TABLE t1 COMMENT = \"NDB_TABLE=READ_BACKUP=0,PARTITION_BALANCE=FOR_RA_BY_NODE\"",
                "ALTER TABLE t1 CHANGE a b BIGINT NOT NULL",
                "ALTER TABLE t1 CHANGE b b INT NOT NULL",
                "ALTER TABLE t1 MODIFY b INT NOT NULL",
                "ALTER TABLE t1 CHANGE b a INT NOT NULL",
                "ALTER TABLE t1 MODIFY col1 BIGINT",
                "ALTER TABLE t1 MODIFY col1 BIGINT UNSIGNED DEFAULT 1 COMMENT 'my column'",
                "ALTER TABLE tbl_name DROP FOREIGN KEY fk_symbol",
                "ALTER TABLE tbl_name CONVERT TO CHARACTER SET charset_name",
                "ALTER TABLE t MODIFY latin1_text_col TEXT CHARACTER SET utf8",
                "ALTER TABLE t MODIFY latin1_varchar_col VARCHAR(255) CHARACTER SET utf8",
                "ALTER TABLE t1 CHANGE c1 c1 BLOB",
                "ALTER TABLE tbl_name DEFAULT CHARACTER SET charset_name",
                "alter table ship_class add column ship_spec varchar(150) first, add somecol int after start_build",
                "alter table t3 add column (c2 decimal(10, 2) null comment 'comment`', c3 enum('abc', 'cba', 'aaa')), add index t3_i1 using btree (c2) comment 'some index'",
                "alter table t2 add constraint t2_pk_constraint primary key (_1c), alter column `_` set default 1",
                "alter table ship_class change column somecol col_for_del tinyint first",
                "alter table ship_class drop col_for_del",
                "alter table t3 drop index t3_i1",
                "alter table childtable drop index fk_idParent_parentTable",
                "alter table t2 drop primary key",
                "alter table t3 rename to table3column",
                "alter table childtable add constraint `fk1` foreign key (idParent) references parenttable(id) on delete restrict on update cascade",
                "alter table table3column default character set = cp1251"
        };
    
        for (String alterTable : alterTables) {
//            System.out.println("Input expr : " + alterTable);
            alterTable(alterTable);
        }
    }
    
    public static void alterTable(String expr) throws Exception{
        new MySQLStatementParseTreeBuilder().parse(expr);
        CodePointCharStream cs = CharStreams.fromString(expr);
        MySQLStatementLexer lexer = new MySQLStatementLexer(cs);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        MySQLStatementParser parser = new MySQLStatementParser(tokens);
        parser.execute();
    }
    
}
