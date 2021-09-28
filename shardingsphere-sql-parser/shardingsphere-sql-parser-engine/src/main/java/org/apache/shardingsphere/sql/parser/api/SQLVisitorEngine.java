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
import org.antlr.v4.runtime.tree.ParseTreeVisitor;
import org.apache.shardingsphere.sql.parser.core.ParseContext;
import org.apache.shardingsphere.sql.parser.core.database.visitor.SQLVisitorFactory;
import org.apache.shardingsphere.sql.parser.core.database.visitor.SQLVisitorRule;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.CommentSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.AbstractSQLStatement;

import java.util.Collection;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * SQL visitor engine.
 */
@RequiredArgsConstructor
public final class SQLVisitorEngine {
    
    private final String databaseType;
    
    private final String visitorType;
    
    private final Properties props;
    
    /**
     * Visit parse context.
     *
     * @param parseContext parse context
     * @param <T> type of SQL visitor result
     * @return SQL visitor result
     */
    public <T> T visit(final ParseContext parseContext) {
        ParseTreeVisitor<T> visitor = SQLVisitorFactory.newInstance(databaseType, visitorType, SQLVisitorRule.valueOf(parseContext.getParseTree().getClass()), props);
        T result = parseContext.getParseTree().accept(visitor);
        appendSQLComments(parseContext, result);
        return result;
    }
    
    private <T> void appendSQLComments(final ParseContext parseContext, final T visitResult) {
        if (!parseContext.getHiddenTokens().isEmpty() && visitResult instanceof AbstractSQLStatement) {
            Collection<CommentSegment> commentSegments = parseContext.getHiddenTokens().stream().map(each -> new CommentSegment(each.getText(), each.getStartIndex(), each.getStopIndex()))
                    .collect(Collectors.toList());
            ((AbstractSQLStatement) visitResult).getCommentSegments().addAll(commentSegments);
        }
    }
}
