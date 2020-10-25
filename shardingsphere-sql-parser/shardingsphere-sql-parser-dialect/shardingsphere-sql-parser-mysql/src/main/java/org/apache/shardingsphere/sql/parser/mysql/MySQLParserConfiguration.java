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

import org.apache.shardingsphere.sql.parser.api.lexer.SQLLexer;
import org.apache.shardingsphere.sql.parser.api.parser.SQLParser;
import org.apache.shardingsphere.sql.parser.api.visitor.SQLVisitorFacadeFactory;
import org.apache.shardingsphere.sql.parser.mysql.lexer.MySQLLexer;
import org.apache.shardingsphere.sql.parser.mysql.parser.MySQLParser;
import org.apache.shardingsphere.sql.parser.mysql.visitor.MySQLSQLVisitorFacadeFactory;
import org.apache.shardingsphere.sql.parser.spi.SQLParserConfiguration;

/**
 * SQL parser configuration for MySQL.
 */
public final class MySQLParserConfiguration implements SQLParserConfiguration {
    
    @Override
    public String getDatabaseTypeName() {
        return "MySQL";
    }
    
    @Override
    public Class<? extends SQLLexer> getLexerClass() {
        return MySQLLexer.class;
    }
    
    @Override
    public Class<? extends SQLParser> getParserClass() {
        return MySQLParser.class;
    }
    
    @Override
    public Class<? extends SQLVisitorFacadeFactory> getVisitorFacadeFactoryClass() {
        return MySQLSQLVisitorFacadeFactory.class;
    }
}
