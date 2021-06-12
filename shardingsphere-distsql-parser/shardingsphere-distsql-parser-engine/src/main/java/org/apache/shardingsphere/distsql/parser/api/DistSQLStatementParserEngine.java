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

package org.apache.shardingsphere.distsql.parser.api;

import lombok.SneakyThrows;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;
import org.apache.shardingsphere.distsql.parser.core.feature.FeatureTypedParseASTNode;
import org.apache.shardingsphere.distsql.parser.core.standard.DistSQLParserFactory;
import org.apache.shardingsphere.distsql.parser.core.standard.DistSQLVisitor;
import org.apache.shardingsphere.distsql.parser.spi.FeatureTypedSQLParserFacade;
import org.apache.shardingsphere.distsql.parser.spi.FeatureTypedSQLVisitorFacade;
import org.apache.shardingsphere.sql.parser.api.parser.SQLParser;
import org.apache.shardingsphere.sql.parser.api.visitor.SQLVisitor;
import org.apache.shardingsphere.sql.parser.core.ParseASTNode;
import org.apache.shardingsphere.sql.parser.core.SQLParserFactory;
import org.apache.shardingsphere.sql.parser.exception.SQLParsingException;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * Dist SQL statement parser engine.
 */
public final class DistSQLStatementParserEngine {
    
    private static final Collection<FeatureTypedSQLParserFacade> FEATURE_TYPED_PARSER_FACADES = new LinkedList<>();
    
    private static final Map<String, FeatureTypedSQLVisitorFacade> FEATURE_TYPED_VISITOR_FACADES = new HashMap<>();
    
    static {
        for (FeatureTypedSQLParserFacade each : ServiceLoader.load(FeatureTypedSQLParserFacade.class)) {
            FEATURE_TYPED_PARSER_FACADES.add(each);
        }
        for (FeatureTypedSQLVisitorFacade each : ServiceLoader.load(FeatureTypedSQLVisitorFacade.class)) {
            FEATURE_TYPED_VISITOR_FACADES.put(each.getFeatureType(), each);
        }
    }
    
    /**
     * Parse SQL.
     *
     * @param sql SQL to be parsed
     * @return AST node
     */
    @SneakyThrows(ReflectiveOperationException.class)
    public SQLStatement parse(final String sql) {
        try {
            ParseASTNode parseASTNode = parseFromStandardParser(sql);
            return getSQLStatement(sql, parseASTNode, new DistSQLVisitor());
        } catch (final ParseCancellationException ex) {
            FeatureTypedParseASTNode featureTypedParseASTNode = parseFromFeatureTypedParsers(sql);
            return getSQLStatement(sql, 
                    featureTypedParseASTNode.getParseASTNode(), FEATURE_TYPED_VISITOR_FACADES.get(featureTypedParseASTNode.getFeatureType()).getVisitorClass().newInstance());
        }
    }
    
    private ParseASTNode parseFromStandardParser(final String sql) {
        SQLParser sqlParser = DistSQLParserFactory.newInstance(sql);
        try {
            return (ParseASTNode) sqlParser.parse();
        } catch (final ParseCancellationException ex) {
            throw new SQLParsingException("You have an error in your SQL syntax.");
        }
    }
    
    private FeatureTypedParseASTNode parseFromFeatureTypedParsers(final String sql) {
        for (FeatureTypedSQLParserFacade each : FEATURE_TYPED_PARSER_FACADES) {
            try {
                ParseASTNode parseASTNode = (ParseASTNode) SQLParserFactory.newInstance(sql, each.getLexerClass(), each.getParserClass()).parse();
                return new FeatureTypedParseASTNode(each.getFeatureType(), parseASTNode);
            } catch (final ParseCancellationException ignored) {
            }
        }
        throw new SQLParsingException("You have an error in your SQL syntax.");
    }
    
    @SuppressWarnings("rawtypes")
    private SQLStatement getSQLStatement(final String sql, final ParseASTNode parseASTNode, final SQLVisitor visitor) {
        if (parseASTNode.getRootNode() instanceof ErrorNode) {
            throw new SQLParsingException("Unsupported SQL of `%s`", sql);
        }
        return (SQLStatement) ((ParseTreeVisitor) visitor).visit(parseASTNode.getRootNode());
    }
}
