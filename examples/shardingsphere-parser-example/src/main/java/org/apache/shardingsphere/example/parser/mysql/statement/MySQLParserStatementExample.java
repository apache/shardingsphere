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

package org.apache.shardingsphere.example.parser.mysql.statement;

import org.antlr.v4.runtime.tree.ParseTree;
import org.apache.shardingsphere.sql.parser.api.SQLParserEngine;
import org.apache.shardingsphere.sql.parser.api.SQLVisitorEngine;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.MySQLStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLShowColumnsStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.ddl.MySQLAlterTableStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.ddl.MySQLCreateTableStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.ddl.MySQLDropTableStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLDeleteStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLInsertStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLSelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLUpdateStatement;

import java.util.Properties;

public class MySQLParserStatementExample {
    
    private static final String DML_SELECT_SQL = "select t.id, t.name, age from table1 as t order by t.id desc;";
    
    private static final String DML_INSERT_SQL = "insert into table1 (name, age) values ('z', 18);";
    
    private static final String DML_UPDATE_SQL = "update table1 set name = 'j' where id = 1;";
    
    private static final String DML_DELETE_SQL = "delete from table1 as t1 where id = 1;";
    
    private static final String DDL_CREAT_SQL = "create table table2 (id bigint(20) PRIMARY KEY, name varchar(20), age int(2))";
    
    private static final String DDL_DROP_SQL = "drop table table1, table2";
    
    private static final String DDL_ALTER_SQL = "alter table table1 drop age";
    
    private static final String DDL_SHOW_SQL = "show columns from table1;";
    
    public static void main(String[] args) {
        SQLParserEngine parserEngine = new SQLParserEngine("MySQL");
        ParseTree tree = parserEngine.parse(DDL_SHOW_SQL, false);
        SQLVisitorEngine visitorEngine = new SQLVisitorEngine("MySQL", "STATEMENT", new Properties());
        MySQLStatement sqlStatement = visitorEngine.visit(tree);
        if (sqlStatement instanceof MySQLInsertStatement) {
            System.out.println((((MySQLInsertStatement) sqlStatement).getColumns()).stream().findFirst().get().getIdentifier().getValue());
        } else if (sqlStatement instanceof MySQLSelectStatement) {
            System.out.println(((MySQLSelectStatement) sqlStatement).getFrom().getAlias().get());
        } else if (sqlStatement instanceof MySQLDeleteStatement) {
            System.out.println(((MySQLDeleteStatement) sqlStatement).getTableSegment().getAlias());
        } else if (sqlStatement instanceof MySQLUpdateStatement) {
            System.out.println(((MySQLUpdateStatement) sqlStatement).getSetAssignment().getAssignments().stream()
                    .findFirst().get().getColumn().getIdentifier().getValue());
        } else if (sqlStatement instanceof MySQLCreateTableStatement) {
            System.out.println(((MySQLCreateTableStatement) sqlStatement).getColumnDefinitions().stream().findFirst()
                    .get().getColumnName().getIdentifier().getValue());
        } else if (sqlStatement instanceof MySQLDropTableStatement) {
            System.out.println(((MySQLDropTableStatement) sqlStatement).getTables().stream().findFirst().get()
                    .getTableName().getIdentifier().getValue());
        } else if (sqlStatement instanceof MySQLAlterTableStatement) {
            System.out.println(((MySQLAlterTableStatement) sqlStatement).getDropColumnDefinitions().stream().findFirst().get()
                    .getColumns().stream().findFirst().get().getIdentifier().getValue());
        } else if (sqlStatement instanceof MySQLShowColumnsStatement) {
            System.out.println(((MySQLShowColumnsStatement) sqlStatement).getTable().getTableName().getIdentifier().getValue());
        }
    }
}
