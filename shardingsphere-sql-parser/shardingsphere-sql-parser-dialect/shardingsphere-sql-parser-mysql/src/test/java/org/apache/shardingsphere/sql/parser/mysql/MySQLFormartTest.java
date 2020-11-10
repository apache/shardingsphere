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

package org.apache.shardingsphere.sql.parser.mysql;

import lombok.Getter;
import org.antlr.v4.runtime.CodePointBuffer;
import org.antlr.v4.runtime.CodePointCharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.apache.shardingsphere.sql.parser.core.parser.ParseASTNode;
import org.apache.shardingsphere.sql.parser.mysql.parser.MySQLLexer;
import org.apache.shardingsphere.sql.parser.mysql.parser.MySQLParser;
import org.apache.shardingsphere.sql.parser.mysql.visitor.format.impl.MySQLDMLFormatSQLVisitor;
import org.apache.shardingsphere.sql.parser.mysql.visitor.format.impl.MySQLFormatSQLVisitor;
import org.junit.Test;

import java.nio.CharBuffer;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertTrue;

public class MySQLFormartTest {

    @Getter
    private static List<String[]> testUnits = new LinkedList();

    {
        testUnits.add(new String[]{"test", "select a+1 as b, name n from table1 join table2 where id=1 and name='lu';", "SELECT a + ? AS b, name n\n" +
                "FROM table1 JOIN table2\n" +
                "WHERE \n" +
                "\tid = ?\n" +
                "\tandname = ?;"});
        testUnits.add(new String[]{"", "select id, name, age, sex, ss, yy from table1 where id=1", "SELECT id , name , age , \n" +
                "\tsex , ss , yy \n" +
                "FROM table1\n" +
                "WHERE \n" +
                "\tid = ?;"});
        testUnits.add(new String[]{"", "select id, name, age, count(*) as n, (select id, name, age, sex from table2 where id=2) as sid, yyyy from table1 where id=1", "SELECT id , name , age , \n" +
                "\tCOUNT ( * ) AS n, \n" +
                "\t(\n" +
                "\t\tSELECT id , name , age , \n" +
                "\t\t\tsex \n" +
                "\t\tFROM table2\n" +
                "\t\tWHERE \n" +
                "\t\t\tid = ?\n" +
                "\t) AS sid, yyyy \n" +
                "FROM table1\n" +
                "WHERE \n" +
                "\tid = ?;"});
        testUnits.add(new String[]{"", "select id, name, age, sex, ss, yy from table1 where id=1 and name=1 and a=1 and b=2 and c=4 and d=3", "SELECT id , name , age , \n" +
                "\tsex , ss , yy \n" +
                "FROM table1\n" +
                "WHERE \n" +
                "\tid = ?\n" +
                "\tandname = ?\n" +
                "\tanda = ?\n" +
                "\tandb = ?\n" +
                "\tandc = ?\n" +
                "\tandd = ?;"});
        testUnits.add(new String[]{"", "ALTER TABLE t_log ADD name varchar(10)", "ALTER TABLE t_log ADD name VARCHAR ( 10 )"});
    }

    @Test
    public final void assertTest() {
        for (String[] each : testUnits) {
            CodePointBuffer buffer = CodePointBuffer.withChars(CharBuffer.wrap(each[1].toCharArray()));
            MySQLLexer lexer = new MySQLLexer(CodePointCharStream.fromBuffer(buffer));
            MySQLParser parser = new MySQLParser(new CommonTokenStream(lexer));
            ParseTree tree = ((ParseASTNode) parser.parse()).getRootNode();
            MySQLFormatSQLVisitor visitor = new MySQLDMLFormatSQLVisitor();
            tree.accept(visitor);
            String result = visitor.getResult().toString();
            assertTrue("SQL format error", each[2].equals(result));
        }
    }
}
