package test;

import io.shardingsphere.core.parsing.antler.ast.MySQLStatementParseTreeBuilder;
import io.shardingsphere.parser.antlr.MySQLStatementLexer;
import io.shardingsphere.parser.antlr.MySQLStatementParser;
import org.antlr.v4.runtime.ANTLRErrorListener;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CodePointCharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;

/**
 * @author maxiaoguang
 */
public class MySQLCreateTableTest {
    
    public static void main(String[] args) throws Exception {
        String[] createTables = {
                "CREATE TABLE orig_tbl (c CHAR(20) CHARACTER SET utf8 COLLATE utf8_bin)",
                "CREATE TABLE new_tbl_1 LIKE orig_tbl",
                "CREATE TABLE new_tbl_2 AS SELECT * FROM orig_tbl",
                "CREATE TABLE test (blob_col BLOB, INDEX(blob_col(10)))",
                "CREATE TABLE t1 (\n" +
                        "    c1 INT STORAGE DISK,\n" +
                        "    c2 INT STORAGE MEMORY\n" +
                        ") ENGINE NDB",
                "CREATE TABLE t2 (\n" +
                        "    c1 INT STORAGE DISK,\n" +
                        "    c2 INT STORAGE MEMORY\n" +
                        ") TABLESPACE ts_1 ENGINE NDB",
                "CREATE TABLE lookup\n" +
                        "    (id INT, INDEX USING BTREE (id))\n" +
                        "    ENGINE = MEMORY",
                "CREATE TABLE t3 (\n" +
                        "    c1 INT NOT NULL AUTO_INCREMENT PRIMARY KEY,\n" +
                        "    c2 VARCHAR(100),\n" +
                        "    c3 VARCHAR(100) )\n" +
                        "ENGINE=NDB\n" +
                        "COMMENT=\"NDB_TABLE=READ_BACKUP=0,PARTITION_BALANCE=FOR_RP_BY_NODE\"",
                "CREATE TABLE t4 (col1 INT, col2 CHAR(5))\n" +
                        "    PARTITION BY HASH(col1)",
                "CREATE TABLE t5 (col1 INT, col2 CHAR(5), col3 DATETIME)\n" +
                        "    PARTITION BY HASH ( YEAR(col3) )",
                "CREATE TABLE t6 (col1 INT, col2 CHAR(5), col3 DATE)\n" +
                        "    PARTITION BY KEY(col3)\n" +
                        "    PARTITIONS 4",
                "CREATE TABLE t7 (col1 INT, col2 CHAR(5), col3 DATE)\n" +
                        "    PARTITION BY LINEAR KEY(col3)\n" +
                        "    PARTITIONS 5",
                "CREATE TABLE t8 (a INT)\n" +
                        "/*!50100 PARTITION BY KEY */ /*!50611 ALGORITHM = 1 */ /*!50100 ()\n" +
                        "      PARTITIONS 3 */",
                "CREATE TABLE t9 (\n" +
                        "    year_col  INT,\n" +
                        "    some_data INT\n" +
                        ")\n" +
                        "PARTITION BY RANGE (year_col) (\n" +
                        "    PARTITION p0 VALUES LESS THAN (1991),\n" +
                        "    PARTITION p1 VALUES LESS THAN (1995),\n" +
                        "    PARTITION p2 VALUES LESS THAN (1999),\n" +
                        "    PARTITION p3 VALUES LESS THAN (2002),\n" +
                        "    PARTITION p4 VALUES LESS THAN (2006),\n" +
                        "    PARTITION p5 VALUES LESS THAN MAXVALUE\n" +
                        ")",
                "CREATE TABLE rc (\n" +
                        "    a INT NOT NULL,\n" +
                        "    b INT NOT NULL\n" +
                        ")\n" +
                        "PARTITION BY RANGE COLUMNS(a,b) (\n" +
                        "    PARTITION p0 VALUES LESS THAN (10,5),\n" +
                        "    PARTITION p1 VALUES LESS THAN (20,10),\n" +
                        "    PARTITION p2 VALUES LESS THAN (50,MAXVALUE),\n" +
                        "    PARTITION p3 VALUES LESS THAN (65,MAXVALUE),\n" +
                        "    PARTITION p4 VALUES LESS THAN (MAXVALUE,MAXVALUE)\n" +
                        ")",
                "CREATE TABLE client_firms (\n" +
                        "    id   INT,\n" +
                        "    name VARCHAR(35)\n" +
                        ")\n" +
                        "PARTITION BY LIST (id) (\n" +
                        "    PARTITION r0 VALUES IN (1, 5, 9, 13, 17, 21),\n" +
                        "    PARTITION r1 VALUES IN (2, 6, 10, 14, 18, 22),\n" +
                        "    PARTITION r2 VALUES IN (3, 7, 11, 15, 19, 23),\n" +
                        "    PARTITION r3 VALUES IN (4, 8, 12, 16, 20, 24)\n" +
                        ")",
                "CREATE TABLE lc (\n" +
                        "    a INT NULL,\n" +
                        "    b INT NULL\n" +
                        ")\n" +
                        "PARTITION BY LIST COLUMNS(a,b) (\n" +
                        "    PARTITION p0 VALUES IN( (0,0), (NULL,NULL) ),\n" +
                        "    PARTITION p1 VALUES IN( (0,1), (0,2), (0,3), (1,1), (1,2) ),\n" +
                        "    PARTITION p2 VALUES IN( (1,0), (2,0), (2,1), (3,0), (3,1) ),\n" +
                        "    PARTITION p3 VALUES IN( (1,3), (2,2), (2,3), (3,2), (3,3) )\n" +
                        ")",
                "CREATE TABLE th (id INT, name VARCHAR(30), adate DATE)\n" +
                        "PARTITION BY LIST(_YEAR(adate))\n" +
                        "(\n" +
                        "  PARTITION p1999 VALUES IN (1995, 1999, 2003)\n" +
                        "    DATA DIRECTORY = '/var/appdata/95/data'\n" +
                        "    INDEX DIRECTORY = '/var/appdata/95/idx',\n" +
                        "  PARTITION p2000 VALUES IN (1996, 2000, 2004)\n" +
                        "    DATA DIRECTORY = '/var/appdata/96/data'\n" +
                        "    INDEX DIRECTORY = '/var/appdata/96/idx',\n" +
                        "  PARTITION p2001 VALUES IN (1997, 2001, 2005)\n" +
                        "    DATA DIRECTORY = '/var/appdata/97/data'\n" +
                        "    INDEX DIRECTORY = '/var/appdata/97/idx',\n" +
                        "  PARTITION p2002 VALUES IN (1998, 2002, 2006)\n" +
                        "    DATA DIRECTORY = '/var/appdata/98/data'\n" +
                        "    INDEX DIRECTORY = '/var/appdata/98/idx'\n" +
                        ")",
                "CREATE TABLE t10 (\n" +
                        "  s1 INT,\n" +
                        "  s2 INT AS (EXP(s1)) STORED\n" +
                        ")\n" +
                        "PARTITION BY LIST (s2) (\n" +
                        "  PARTITION p1 VALUES IN (1)\n" +
                        ")",
                "create table new_t  (like t1)",
                "create table log_table(_row varchar(512))",
                "create table ships(name varchar(255), class_id int, id int)",
                "create table ships_guns(guns_id int, ship_id int)",
                "create table guns(id int, power decimal(7,2), callibr decimal(10,3))",
                "create table ship_class(id int, class_name varchar(100), tonange decimal(10,2), max_length decimal(10,2), start_build year, end_build year(4), max_guns_size int)",
//                "create table `some table $$`(id int auto_increment key, class varchar(10), data binary) engine=MYISAM",
                "create table `parent_table`(id int primary key, column1 varchar(30), index parent_table_i1(column1(20)), check(char_length(column1)>10)) engine InnoDB",
                "create table child_table(id int unsigned auto_increment primary key, id_parent int references parent_table(id) match full on update cascade on delete set null) engine=InnoDB",
//                "create table `another some table $$` like `some table $$`",
                "create table `actor` (`last_update` timestamp default CURRENT_TIMESTAMP, `birthday` datetime default CURRENT_TIMESTAMP ON UPDATE LOCALTIMESTAMP)",
//                "create table boolean_table(c1 bool, c2 boolean default true)",
                "create table default_table(c1 int default 42, c2 int default -42)",
                "create table ts_table(\n" +
                        "  ts1 TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,\n" +
                        "  ts2 TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE LOCALTIME,\n" +
                        "  ts3 TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE LOCALTIMESTAMP,\n" +
                        "  ts4 TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP(),\n" +
                        "  ts5 TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE LOCALTIME(),\n" +
                        "  ts6 TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE LOCALTIMESTAMP(),\n" +
                        "  ts7 TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE NOW(),\n" +
                        "  ts8 TIMESTAMP(6) NOT NULL,\n" +
                        "  ts9 TIMESTAMP(6) NOT NULL DEFAULT NOW(6) ON UPDATE NOW(6)\n" +
                        ") COMPRESSION 'ZLIB'"
        };
    
        for (String createTable : createTables) {
//            System.out.println("Input expr : " + createTable);
            createTable(createTable);
        }
    }
    
    public static void createTable(String expr) throws Exception{
        new MySQLStatementParseTreeBuilder().parse(expr);
        CodePointCharStream cs = CharStreams.fromString(expr);
        MySQLStatementLexer lexer = new MySQLStatementLexer(cs);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        MySQLStatementParser parser = new MySQLStatementParser(tokens);
        parser.addErrorListener(new BaseErrorListener() {
            public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
                throw new RuntimeException();
            }
        });
        parser.execute();
    }
    
}
