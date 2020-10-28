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

package org.apache.shardingsphere.sql.parser.sqlserver.parser;

import org.antlr.v4.runtime.TokenStream;
import org.apache.shardingsphere.sql.parser.api.visitor.ASTNode;
import org.apache.shardingsphere.sql.parser.api.parser.SQLParser;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser;
import org.apache.shardingsphere.sql.parser.core.parser.ParseASTNode;

/**
 * SQL parser for SQLServer.
 */
public final class SQLServerParser extends SQLServerStatementParser implements SQLParser {
    
    public SQLServerParser(final TokenStream input) {
        super(input);
    }
    
    @Override
    public ASTNode parse() {
        return new ParseASTNode(execute());
    }
}
