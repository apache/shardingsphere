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

package org.apache.shardingsphere.distsql.parser;

import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.atn.PredictionMode;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.apache.shardingsphere.distsql.parser.sql.visitor.DistSQLStatementVisitor;
import org.apache.shardingsphere.sql.parser.api.parser.SQLParser;
import org.apache.shardingsphere.sql.parser.core.parser.ParseASTNode;
import org.apache.shardingsphere.sql.parser.exception.SQLParsingException;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;

/**
 * Dist SQL statement parser engine.
 */
public final class DistSQLStatementParserEngine {
    
    /**
     * Parse SQL.
     *
     * @param sql SQL to be parsed
     * @return AST node
     */
    public SQLStatement parse(final String sql) {
        ParseASTNode parseASTNode = twoPhaseParse(sql);
        if (parseASTNode.getRootNode() instanceof ErrorNode) {
            throw new SQLParsingException("Unsupported SQL of `%s`", sql);
        }
        return (SQLStatement) new DistSQLStatementVisitor().visit(parseASTNode.getRootNode());
    }
    
    private ParseASTNode twoPhaseParse(final String sql) {
        SQLParser sqlParser = DistSQLParserFactory.newInstance(sql);
        try {
            setPredictionMode((Parser) sqlParser, PredictionMode.SLL);
            return (ParseASTNode) sqlParser.parse();
        } catch (final ParseCancellationException ex) {
            ((Parser) sqlParser).reset();
            setPredictionMode((Parser) sqlParser, PredictionMode.LL);
            return (ParseASTNode) sqlParser.parse();
        }
    }
    
    private void setPredictionMode(final Parser sqlParser, final PredictionMode mode) {
        sqlParser.setErrorHandler(new BailErrorStrategy());
        sqlParser.getInterpreter().setPredictionMode(mode);
    }
}
