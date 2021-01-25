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

package org.apache.shardingsphere.sql.parser.api;

import lombok.RequiredArgsConstructor;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;
import org.apache.shardingsphere.sql.parser.core.visitor.SQLVisitorFactory;
import org.apache.shardingsphere.sql.parser.core.visitor.SQLVisitorRule;

import java.util.Properties;

/**
 * SQL visitor engine.
 */
@RequiredArgsConstructor
public final class SQLVisitorEngine {
    
    private final String databaseType;
    
    private final String visitorType;

    private final Properties props;
    
    /**
     * Visit parse tree.
     *
     * @param parseTree parse tree
     * @param <T> type of SQL visitor result
     * @return SQL visitor result
     */
    public <T> T visit(final ParseTree parseTree) {
        ParseTreeVisitor<T> visitor = SQLVisitorFactory.newInstance(databaseType, visitorType, SQLVisitorRule.valueOf(parseTree.getClass()), props);
        return parseTree.accept(visitor);
    }
}
