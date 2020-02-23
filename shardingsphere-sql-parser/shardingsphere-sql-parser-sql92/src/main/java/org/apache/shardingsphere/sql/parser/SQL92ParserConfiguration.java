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
import org.apache.shardingsphere.sql.parser.exception.SQLParsingException;
import org.apache.shardingsphere.sql.parser.spi.SQLParserConfiguration;
import org.apache.shardingsphere.sql.parser.visitor.impl.SQL92DALVisitor;
import org.apache.shardingsphere.sql.parser.visitor.impl.SQL92DCLVisitor;
import org.apache.shardingsphere.sql.parser.visitor.impl.SQL92DDLVisitor;
import org.apache.shardingsphere.sql.parser.visitor.impl.SQL92DMLVisitor;
import org.apache.shardingsphere.sql.parser.visitor.impl.SQL92TCLVisitor;

/**
 * SQL parser configuration for SQL92.
 */
public final class SQL92ParserConfiguration implements SQLParserConfiguration {
    
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
    public Class<? extends ParseTreeVisitor> getVisitorClass(final String sqlStatementType) {
        switch (sqlStatementType) {
            case "DML":
                return SQL92DMLVisitor.class;
            case "DDL":
                return SQL92DDLVisitor.class;
            case "TCL":
                return SQL92TCLVisitor.class;
            case "DCL":
                return SQL92DCLVisitor.class;
            case "DAL":
                return SQL92DALVisitor.class;
            default:
                throw new SQLParsingException("Can not support SQL statement type: `%s`", sqlStatementType);
        }
    }
}
