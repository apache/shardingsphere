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
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementLexer;
import org.apache.shardingsphere.sql.parser.exception.SQLParsingException;
import org.apache.shardingsphere.sql.parser.spi.SQLParserConfiguration;
import org.apache.shardingsphere.sql.parser.visitor.impl.PostgreSQLDALVisitor;
import org.apache.shardingsphere.sql.parser.visitor.impl.PostgreSQLDCLVisitor;
import org.apache.shardingsphere.sql.parser.visitor.impl.PostgreSQLDDLVisitor;
import org.apache.shardingsphere.sql.parser.visitor.impl.PostgreSQLDMLVisitor;
import org.apache.shardingsphere.sql.parser.visitor.impl.PostgreSQLTCLVisitor;

/**
 * SQL parser configuration for PostgreSQL.
 */
public final class PostgreSQLParserConfiguration implements SQLParserConfiguration {
    
    @Override
    public String getDatabaseTypeName() {
        return "PostgreSQL";
    }
    
    @Override
    public Class<? extends Lexer> getLexerClass() {
        return PostgreSQLStatementLexer.class;
    }
    
    @Override
    public Class<? extends SQLParser> getParserClass() {
        return PostgreSQLParser.class;
    }
    
    @Override
    public Class<? extends ParseTreeVisitor> getVisitorClass(final String sqlStatementType) {
        switch (sqlStatementType) {
            case "DML":
                return PostgreSQLDMLVisitor.class;
            case "DDL":
                return PostgreSQLDDLVisitor.class;
            case "TCL":
                return PostgreSQLTCLVisitor.class;
            case "DCL":
                return PostgreSQLDCLVisitor.class;
            case "DAL":
                return PostgreSQLDALVisitor.class;
            default:
                throw new SQLParsingException("Can not support SQL statement type: `%s`", sqlStatementType);
        }
    }
}
