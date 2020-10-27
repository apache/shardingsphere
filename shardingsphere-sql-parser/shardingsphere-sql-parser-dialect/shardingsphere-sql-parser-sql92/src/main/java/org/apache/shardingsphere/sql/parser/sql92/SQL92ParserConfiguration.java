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

package org.apache.shardingsphere.sql.parser.sql92;

import org.apache.shardingsphere.sql.parser.api.lexer.SQLLexer;
import org.apache.shardingsphere.sql.parser.api.parser.SQLParser;
import org.apache.shardingsphere.sql.parser.api.visitor.SQLVisitorFacade;
import org.apache.shardingsphere.sql.parser.spi.SQLParserConfiguration;
import org.apache.shardingsphere.sql.parser.sql92.lexer.SQL92Lexer;
import org.apache.shardingsphere.sql.parser.sql92.parser.SQL92Parser;
import org.apache.shardingsphere.sql.parser.sql92.visitor.format.facade.SQL92FormatSQLVisitorFacade;
import org.apache.shardingsphere.sql.parser.sql92.visitor.statement.facade.SQL92StatementSQLVisitorFacade;

import java.util.HashMap;
import java.util.Map;

/**
 * SQL parser configuration for SQL92.
 */
public final class SQL92ParserConfiguration implements SQLParserConfiguration {
    
    @Override
    public String getDatabaseTypeName() {
        return "SQL92";
    }
    
    @Override
    public Class<? extends SQLLexer> getLexerClass() {
        return SQL92Lexer.class;
    }
    
    @Override
    public Class<? extends SQLParser> getParserClass() {
        return SQL92Parser.class;
    }
    
    @Override
    public Map<String, Class<? extends SQLVisitorFacade>> getSQLVisitorFacadeClasses() {
        Map<String, Class<? extends SQLVisitorFacade>> result = new HashMap<>(2, 1);
        result.put("STATEMENT", SQL92StatementSQLVisitorFacade.class);
        result.put("FORMAT", SQL92FormatSQLVisitorFacade.class);
        return result;
    }
}
