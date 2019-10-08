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

package org.apache.shardingsphere.core.parse;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;
import org.apache.shardingsphere.core.parse.api.SQLParser;
import org.apache.shardingsphere.core.parse.autogen.MySQLStatementLexer;
import org.apache.shardingsphere.core.parse.autogen.MySQLStatementParser;

/**
 * SQL parser for MySQL.
 *
 * @author duhongjun
 */
public final class MySQLParser extends MySQLStatementParser implements SQLParser {

    public MySQLParser(final TokenStream input) {
        super(input);
    }

    public static void main(String[] args){
        CharStream input = CharStreams.fromString("SELECT Y.* FROM TS_ORDER K WHERE K.ID = 3");

        Lexer lexer = new MySQLStatementLexer(input);

        CommonTokenStream tokens = new CommonTokenStream(lexer);

        MySQLParser parser = new MySQLParser(tokens);

        ParseTree tree = parser.execute().getChild(0);
        System.out.println(tree.toStringTree(parser));
    }
}
