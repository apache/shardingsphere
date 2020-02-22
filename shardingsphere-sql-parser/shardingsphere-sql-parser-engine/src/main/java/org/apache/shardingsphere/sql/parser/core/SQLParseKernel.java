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

package org.apache.shardingsphere.sql.parser.core;

import lombok.RequiredArgsConstructor;
import org.antlr.v4.runtime.tree.ParseTree;
import org.apache.shardingsphere.sql.parser.core.parser.SQLParserEngine;
import org.apache.shardingsphere.sql.parser.core.visitor.ParseTreeVisitorFactory;
import org.apache.shardingsphere.sql.parser.sql.statement.SQLStatement;

/**
 * SQL parse kernel.
 */
@RequiredArgsConstructor
public final class SQLParseKernel {
    
    private final String databaseTypeName;
    
    private final String sql;
    
    /**
     * Parse SQL.
     *
     * @return SQL statement
     */
    public SQLStatement parse() {
        ParseTree parseTree = new SQLParserEngine(databaseTypeName, sql).parse();
        return (SQLStatement) ParseTreeVisitorFactory.newInstance(databaseTypeName, parseTree).visit(parseTree);
    }
}
