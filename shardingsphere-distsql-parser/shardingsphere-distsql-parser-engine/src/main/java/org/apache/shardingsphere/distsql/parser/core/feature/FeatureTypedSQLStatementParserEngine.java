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

package org.apache.shardingsphere.distsql.parser.core.feature;

import lombok.SneakyThrows;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;
import org.apache.shardingsphere.distsql.parser.spi.FeatureTypedSQLParserFacade;
import org.apache.shardingsphere.distsql.parser.spi.FeatureTypedSQLStatementVisitorFacade;
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
 * Feature type based SQL statement parser engine.
 */
public final class FeatureTypedSQLStatementParserEngine {
    
    private static final Collection<FeatureTypedSQLParserFacade> PARSER_FACADES = new LinkedList<>();
    
    private static final Map<String, FeatureTypedSQLStatementVisitorFacade> VISITOR_FACADES = new HashMap<>();
    
    static {
        for (FeatureTypedSQLParserFacade each : ServiceLoader.load(FeatureTypedSQLParserFacade.class)) {
            PARSER_FACADES.add(each);
        }
        for (FeatureTypedSQLStatementVisitorFacade each : ServiceLoader.load(FeatureTypedSQLStatementVisitorFacade.class)) {
            VISITOR_FACADES.put(each.getFeatureType(), each);
        }
    }
    
    /**
     * Parse SQL.
     *
     * @param sql SQL to be parsed
     * @return SQL statement
     */
    public SQLStatement parse(final String sql) {
        FeatureTypedParseASTNode featureTypedParseASTNode = parseToASTNode(sql);
        return getSQLStatement(sql, featureTypedParseASTNode.getFeatureType(), featureTypedParseASTNode.getParseASTNode());
    }
    
    private FeatureTypedParseASTNode parseToASTNode(final String sql) {
        for (FeatureTypedSQLParserFacade each : PARSER_FACADES) {
            try {
                ParseASTNode parseASTNode = (ParseASTNode) SQLParserFactory.newInstance(sql, each.getLexerClass(), each.getParserClass()).parse();
                return new FeatureTypedParseASTNode(each.getFeatureType(), parseASTNode);
            } catch (final ParseCancellationException ignored) {
            }
        }
        throw new SQLParsingException("You have an error in your SQL syntax.");
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    @SuppressWarnings("rawtypes")
    private SQLStatement getSQLStatement(final String sql, final String featureType, final ParseASTNode parseASTNode) {
        SQLVisitor visitor = VISITOR_FACADES.get(featureType).getVisitorClass().newInstance();
        if (parseASTNode.getRootNode() instanceof ErrorNode) {
            throw new SQLParsingException("Unsupported SQL of `%s`", sql);
        }
        return (SQLStatement) ((ParseTreeVisitor) visitor).visit(parseASTNode.getRootNode());
    }
}
