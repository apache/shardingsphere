/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.sql.parser.mysql.visitor.format;

import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.util.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sql.parser.api.CacheOption;
import org.apache.shardingsphere.sql.parser.api.SQLFormatEngine;
import org.apache.shardingsphere.test.util.PropertiesBuilder;
import org.apache.shardingsphere.test.util.PropertiesBuilder.Property;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class MySQLFormatVisitorIT {
    
    @ParameterizedTest(name = "{0}")
    @ArgumentsSource(TestCaseArgumentsProvider.class)
    void assertSQLFormat(final String caseId, final String inputSQL, final String expectFormattedSQL, final String expectFormattedParameterizedSQL) {
        SQLFormatEngine sqlFormatEngine = new SQLFormatEngine(TypedSPILoader.getService(DatabaseType.class, "MySQL"), new CacheOption(1, 1L));
        assertThat(sqlFormatEngine.format(inputSQL, false, PropertiesBuilder.build(new Property("parameterized", Boolean.FALSE.toString()))), is(expectFormattedSQL));
        assertThat(sqlFormatEngine.format(inputSQL, false, PropertiesBuilder.build(new Property("parameterized", Boolean.TRUE.toString()))), is(expectFormattedParameterizedSQL));
    }
    
    private static class TestCaseArgumentsProvider implements ArgumentsProvider {
        
        @Override
        public Stream<? extends Arguments> provideArguments(final ExtensionContext extensionContext) {
            return Stream.of(Arguments.of("select_with_union",
                    "select a+1 as b, name n from table1 join table2 where id=1 and name='lu';",
                    "SELECT a + 1 AS b, name n\nFROM table1 JOIN table2\nWHERE \n\tid = 1\n\tand name = 'lu';",
                    "SELECT a + ? AS b, name n\nFROM table1 JOIN table2\nWHERE \n\tid = ?\n\tand name = ?;"),
                    Arguments.of("select_item_nums",
                            "select id, name, age, sex, ss, yy from table1 where id=1",
                            "SELECT id , name , age , \n\tsex , ss , yy \nFROM table1\nWHERE \n\tid = 1;",
                            "SELECT id , name , age , \n\tsex , ss , yy \nFROM table1\nWHERE \n\tid = ?;"),
                    Arguments.of("select_with_subquery",
                            "select id, name, age, count(*) as n, (select id, name, age, sex from table2 where id=2) as sid, yyyy from table1 where id=1",
                            "SELECT id , name , age , \n\tCOUNT(*) AS n, \n\t(\n\t\tSELECT id , name , age , \n\t\t\tsex \n\t\t"
                                    + "FROM table2\n\t\tWHERE \n\t\t\tid = 2\n\t) AS sid, yyyy \nFROM table1\nWHERE \n\tid = 1;",
                            "SELECT id , name , age , \n\tCOUNT(*) AS n, \n\t(\n\t\tSELECT id , name , age , \n\t\t\tsex \n\t\t"
                                    + "FROM table2\n\t\tWHERE \n\t\t\tid = ?\n\t) AS sid, yyyy \nFROM table1\nWHERE \n\tid = ?;"),
                    Arguments.of("select_where_num",
                            "select id, name, age, sex, ss, yy from table1 where id=1 and name=1 and a=1 and b=2 and c=4 and d=3",
                            "SELECT id , name , age , \n\tsex , ss , yy \nFROM table1\nWHERE \n\tid = 1\n\tand name = 1\n\tand a = 1\n\tand b = 2\n\tand c = 4\n\tand d = 3;",
                            "SELECT id , name , age , \n\tsex , ss , yy \nFROM table1\nWHERE \n\tid = ?\n\tand name = ?\n\tand a = ?\n\tand b = ?\n\tand c = ?\n\tand d = ?;"),
                    Arguments.of("alter_table",
                            "ALTER TABLE t_order ADD column4 DATE, ADD column5 DATETIME, engine ss max_rows 10,min_rows 2, ADD column6 TIMESTAMP, ADD column7 TIME;",
                            "ALTER TABLE t_order\n\tADD column4 DATE,\n\tADD column5 DATETIME,\n\tENGINE ss\n\tMAX_ROWS 10,\n\tMIN_ROWS 2,\n\tADD column6 TIMESTAMP,\n\tADD column7 TIME",
                            "ALTER TABLE t_order\n\tADD column4 DATE,\n\tADD column5 DATETIME,\n\tENGINE ss\n\tMAX_ROWS 10,\n\tMIN_ROWS 2,\n\tADD column6 TIMESTAMP,\n\tADD column7 TIME"),
                    Arguments.of("create_table",
                            "CREATE TABLE IF NOT EXISTS `runoob_tbl`(\n"
                                    + "`runoob_id` INT UNSIGNED AUTO_INCREMENT,\n"
                                    + "`runoob_title` VARCHAR(100) NOT NULL,\n"
                                    + "`runoob_author` VARCHAR(40) NOT NULL,\n"
                                    + "`runoob_test` NATIONAL CHAR(40),\n"
                                    + "`submission_date` DATE,\n"
                                    + "PRIMARY KEY (`runoob_id`)\n"
                                    + ")ENGINE=InnoDB DEFAULT CHARSET=utf8;",
                            "CREATE TABLE IF NOT EXISTS `runoob_tbl` (\n"
                                    + "\t`runoob_id` INT UNSIGNED AUTO_INCREMENT,\n"
                                    + "\t`runoob_title` VARCHAR(100) NOT NULL,\n"
                                    + "\t`runoob_author` VARCHAR(40) NOT NULL,\n"
                                    + "\t`runoob_test` NATIONAL CHAR(40),\n"
                                    + "\t`submission_date` DATE,\n"
                                    + "\tPRIMARY KEY (`runoob_id`)\n"
                                    + ") ENGINE = InnoDB DEFAULT CHARSET = utf8",
                            "CREATE TABLE IF NOT EXISTS `runoob_tbl` (\n"
                                    + "\t`runoob_id` INT UNSIGNED AUTO_INCREMENT,\n"
                                    + "\t`runoob_title` VARCHAR(100) NOT NULL,\n"
                                    + "\t`runoob_author` VARCHAR(40) NOT NULL,\n"
                                    + "\t`runoob_test` NATIONAL CHAR(40),\n"
                                    + "\t`submission_date` DATE,\n"
                                    + "\tPRIMARY KEY (`runoob_id`)\n"
                                    + ") ENGINE = InnoDB DEFAULT CHARSET = utf8"),
                    Arguments.of("insert_with_muti_value",
                            "INSERT INTO t_order_item(order_id, user_id, status, creation_date) values (1, 1, 'insert', '2017-08-08'), "
                                    + "(2, 2, 'insert', '2017-08-08') ON DUPLICATE KEY UPDATE status = 'init'",
                            "INSERT  INTO t_order_item (order_id , user_id , status , creation_date)\nVALUES\n\t(1, 1, 'insert', '2017-08-08'),\n"
                                    + "\t(2, 2, 'insert', '2017-08-08')\nON DUPLICATE KEY UPDATE status = 'init'",
                            "INSERT  INTO t_order_item (order_id , user_id , status , creation_date)\nVALUES\n\t(?, ?, ?, ?),\n"
                                    + "\t(?, ?, ?, ?)\nON DUPLICATE KEY UPDATE status = ?"),
                    Arguments.of("insert_with_muti_set",
                            "INSERT INTO t_order SET order_id = 1, user_id = 1, status = convert(to_base64(aes_encrypt(1, 'key')) USING utf8) ON DUPLICATE KEY UPDATE status = VALUES(status)",
                            "INSERT  INTO t_order SET order_id = 1,\n\tuser_id = 1,\n\tstatus = CONVERT(to_base64(aes_encrypt(1 , 'key')) USING utf8)\n"
                                    + "ON DUPLICATE KEY UPDATE status = VALUES(status)",
                            "INSERT  INTO t_order SET order_id = ?,\n\tuser_id = ?,\n\tstatus = CONVERT(to_base64(aes_encrypt(? , ?)) USING utf8)\n"
                                    + "ON DUPLICATE KEY UPDATE status = VALUES(status)"),
                    Arguments.of("insert_with_select",
                            "INSERT INTO t_order (order_id, user_id, status) SELECT order_id, user_id, status FROM t_order WHERE order_id = 1",
                            "INSERT  INTO t_order (order_id , user_id , status) \nSELECT order_id , user_id , status \nFROM t_order\nWHERE \n\torder_id = 1;",
                            "INSERT  INTO t_order (order_id , user_id , status) \nSELECT order_id , user_id , status \nFROM t_order\nWHERE \n\torder_id = ?;"),
                    Arguments.of("only_comment", "/* c_zz_xdba_test_4 login */", "", ""),
                    Arguments.of("select_with_Variable",
                            "SELECT @@SESSION.auto_increment_increment AS auto_increment_increment, @@character_set_client AS character_set_client, "
                                    + "@@character_set_connection AS character_set_connection, @@character_set_results AS character_set_results, @@character_set_server AS character_set_server, "
                                    + "@@collation_server AS collation_server, @@collation_connection AS collation_connection, "
                                    + "@@init_connect AS init_connect, @@interactive_timeout AS interactive_timeout, "
                                    + "@@license AS license, @@lower_case_table_names AS lower_case_table_names, "
                                    + "@@max_allowed_packet AS max_allowed_packet, @@net_buffer_length AS net_buffer_length, "
                                    + "@@net_write_timeout AS net_write_timeout, @@query_cache_size AS query_cache_size, @@query_cache_type AS query_cache_type, @@sql_mode AS sql_mode, "
                                    + "@@system_time_zone AS system_time_zone, @@time_zone AS time_zone, @@tx_isolation AS transaction_isolation, @@wait_timeout AS wait_timeout",
                            "SELECT "
                                    + "@@SESSION.auto_increment_increment AS auto_increment_increment, @@character_set_client AS character_set_client, "
                                    + "@@character_set_connection AS character_set_connection, \n"
                                    + "\t@@character_set_results AS character_set_results, @@character_set_server AS character_set_server, @@collation_server AS collation_server, \n"
                                    + "\t@@collation_connection AS collation_connection, @@init_connect AS init_connect, @@interactive_timeout AS interactive_timeout, \n"
                                    + "\t@@license AS license, @@lower_case_table_names AS lower_case_table_names, @@max_allowed_packet AS max_allowed_packet, \n"
                                    + "\t@@net_buffer_length AS net_buffer_length, @@net_write_timeout AS net_write_timeout, @@query_cache_size AS query_cache_size, \n"
                                    + "\t@@query_cache_type AS query_cache_type, @@sql_mode AS sql_mode, @@system_time_zone AS system_time_zone, \n"
                                    + "\t@@time_zone AS time_zone, @@tx_isolation AS transaction_isolation, @@wait_timeout AS wait_timeout;",
                            "SELECT "
                                    + "@@SESSION.auto_increment_increment AS auto_increment_increment, @@character_set_client AS character_set_client, "
                                    + "@@character_set_connection AS character_set_connection, \n"
                                    + "\t@@character_set_results AS character_set_results, @@character_set_server AS character_set_server, @@collation_server AS collation_server, \n"
                                    + "\t@@collation_connection AS collation_connection, @@init_connect AS init_connect, @@interactive_timeout AS interactive_timeout, \n"
                                    + "\t@@license AS license, @@lower_case_table_names AS lower_case_table_names, @@max_allowed_packet AS max_allowed_packet, \n"
                                    + "\t@@net_buffer_length AS net_buffer_length, @@net_write_timeout AS net_write_timeout, @@query_cache_size AS query_cache_size, \n"
                                    + "\t@@query_cache_type AS query_cache_type, @@sql_mode AS sql_mode, @@system_time_zone AS system_time_zone, \n"
                                    + "\t@@time_zone AS time_zone, @@tx_isolation AS transaction_isolation, @@wait_timeout AS wait_timeout;"));
        }
    }
}
