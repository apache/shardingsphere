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

package org.apache.shardingsphere.rdl.parser.engine.engine;

import lombok.RequiredArgsConstructor;
import org.antlr.v4.runtime.tree.ParseTree;
import org.apache.shardingsphere.rdl.parser.engine.executor.RDLSQLParserExecutor;
import org.apache.shardingsphere.rdl.parser.sql.visitor.ShardingSphereVisitor;
import org.apache.shardingsphere.sql.parser.engine.SQLStatementParserEngine;
import org.apache.shardingsphere.sql.parser.hook.ParsingHookRegistry;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;

/**
 * RDL SQL parser engine.
 */
@RequiredArgsConstructor
public final class RDLSQLParserEngine implements SQLStatementParserEngine {
    
    private final ParsingHookRegistry parsingHookRegistry = ParsingHookRegistry.getInstance();
    
    // TODO check skywalking plugin
    /*
     * To make sure SkyWalking will be available at the next release of ShardingSphere,
     * a new plugin should be provided to SkyWalking project if this API changed.
     *
     * @see <a href="https://github.com/apache/skywalking/blob/master/docs/en/guides/Java-Plugin-Development-Guide.md#user-content-plugin-development-guide">Plugin Development Guide</a>
     */
    @Override
    public SQLStatement parse(final String sql, final boolean useCache) {
        parsingHookRegistry.start(sql);
        try {
            SQLStatement result = parse0(sql);
            parsingHookRegistry.finishSuccess(result);
            return result;
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            parsingHookRegistry.finishFailure(ex);
            throw ex;
        }
    }
    
    private SQLStatement parse0(final String sql) {
        ParseTree parseTree = new RDLSQLParserExecutor(sql).execute().getRootNode();
        return (SQLStatement) new ShardingSphereVisitor().visit(parseTree);
    }
}
