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
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementLexer;
import org.apache.shardingsphere.sql.parser.spi.SQLParserEntry;
import org.apache.shardingsphere.sql.parser.visitor.impl.MySQLDALVisitor;
import org.apache.shardingsphere.sql.parser.visitor.impl.MySQLDCLVisitor;
import org.apache.shardingsphere.sql.parser.visitor.impl.MySQLDDLVisitor;
import org.apache.shardingsphere.sql.parser.visitor.impl.MySQLDMLVisitor;
import org.apache.shardingsphere.sql.parser.visitor.impl.MySQLTCLVisitor;

/**
 * SQL parser entry for MySQL.
 */
public final class MySQLParserEntry implements SQLParserEntry {
    
    @Override
    public String getDatabaseTypeName() {
        return "MySQL";
    }
    
    @Override
    public Class<? extends Lexer> getLexerClass() {
        return MySQLStatementLexer.class;
    }
    
    @Override
    public Class<? extends SQLParser> getParserClass() {
        return MySQLParser.class;
    }
    
    @Override
    public Class<? extends ParseTreeVisitor> getVisitorClass(final String visitorName) {
        if (MySQLDMLVisitor.class.getSimpleName().contains(visitorName)) {
            return MySQLDMLVisitor.class;
        }
        if (MySQLDDLVisitor.class.getSimpleName().contains(visitorName)) {
            return MySQLDDLVisitor.class;
        }
        if (MySQLTCLVisitor.class.getSimpleName().contains(visitorName)) {
            return MySQLTCLVisitor.class;
        }
        if (MySQLDCLVisitor.class.getSimpleName().contains(visitorName)) {
            return MySQLDCLVisitor.class;
        }
        return MySQLDALVisitor.class;
    }
}
