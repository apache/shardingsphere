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
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementLexer;
import org.apache.shardingsphere.sql.parser.spi.SQLParserEntry;
import org.apache.shardingsphere.sql.parser.visitor.impl.OracleDALVisitor;
import org.apache.shardingsphere.sql.parser.visitor.impl.OracleDCLVisitor;
import org.apache.shardingsphere.sql.parser.visitor.impl.OracleDDLVisitor;
import org.apache.shardingsphere.sql.parser.visitor.impl.OracleDMLVisitor;
import org.apache.shardingsphere.sql.parser.visitor.impl.OracleTCLVisitor;

/**
 * SQL parser entry for Oracle.
 */
public final class OracleParserEntry implements SQLParserEntry {
    
    @Override
    public String getDatabaseTypeName() {
        return "Oracle";
    }
    
    @Override
    public Class<? extends Lexer> getLexerClass() {
        return OracleStatementLexer.class;
    }
    
    @Override
    public Class<? extends SQLParser> getParserClass() {
        return OracleParser.class;
    }
    
    @Override
    public Class<? extends ParseTreeVisitor> getVisitorClass(final String visitorName) {
        if (OracleDMLVisitor.class.getSimpleName().contains(visitorName)) {
            return OracleDMLVisitor.class;
        }
        if (OracleDDLVisitor.class.getSimpleName().contains(visitorName)) {
            return OracleDDLVisitor.class;
        }
        if (OracleTCLVisitor.class.getSimpleName().contains(visitorName)) {
            return OracleTCLVisitor.class;
        }
        if (OracleDCLVisitor.class.getSimpleName().contains(visitorName)) {
            return OracleDCLVisitor.class;
        }
        return OracleDALVisitor.class;
    }
}
