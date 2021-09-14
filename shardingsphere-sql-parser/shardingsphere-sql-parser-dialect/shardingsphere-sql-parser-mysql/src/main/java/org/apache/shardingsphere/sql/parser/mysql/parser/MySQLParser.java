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

package org.apache.shardingsphere.sql.parser.mysql.parser;

import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.TokenStream;
import org.apache.shardingsphere.sql.parser.api.visitor.ASTNode;
import org.apache.shardingsphere.sql.parser.api.parser.SQLParser;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser;
import org.apache.shardingsphere.sql.parser.core.ParseASTNode;

import java.util.Properties;

/**
 * SQL parser for MySQL.
 */
public final class MySQLParser extends MySQLStatementParser implements SQLParser {
    
    private final Properties props;
    
    public MySQLParser(final TokenStream input, final Properties props) {
        super(input);
        this.props = props;
    }
    
    @Override
    public ASTNode parse() {
        return props.containsKey(ENABLE_SQL_COMMENT_PARSE) && Boolean.parseBoolean(props.get(ENABLE_SQL_COMMENT_PARSE).toString()) ? new ParseASTNode(execute(), (CommonTokenStream) getTokenStream())
                : new ParseASTNode(execute());
    }
}
