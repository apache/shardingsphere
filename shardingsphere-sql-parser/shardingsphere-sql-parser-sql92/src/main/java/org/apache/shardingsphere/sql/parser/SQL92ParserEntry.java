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

package org.apache.shardingsphere.sql.parser;

import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;
import org.apache.shardingsphere.sql.parser.api.SQLParser;
import org.apache.shardingsphere.sql.parser.autogen.SQL92StatementLexer;
import org.apache.shardingsphere.sql.parser.spi.SQLParserEntry;

/**
 * SQL parser entry for SQL92.
 *
 * @author zhangyonglun
 * @author panjuan
 */
public final class SQL92ParserEntry implements SQLParserEntry {
    
    @Override
    public String getDatabaseTypeName() {
        return "SQL92";
    }
    
    @Override
    public Class<? extends Lexer> getLexerClass() {
        return SQL92StatementLexer.class;
    }
    
    @Override
    public Class<? extends SQLParser> getParserClass() {
        return SQL92Parser.class;
    }
    
    @Override
    public Class<? extends ParseTreeVisitor> getVisitorClass(final String visitorName) {
        return SQL92Visitor.class;
    }
}
