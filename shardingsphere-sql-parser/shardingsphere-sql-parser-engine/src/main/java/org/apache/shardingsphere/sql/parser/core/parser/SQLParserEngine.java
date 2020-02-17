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

package org.apache.shardingsphere.sql.parser.core.parser;

import lombok.RequiredArgsConstructor;
import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.DefaultErrorStrategy;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.atn.PredictionMode;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTree;
import org.apache.shardingsphere.sql.parser.api.SQLParser;
import org.apache.shardingsphere.sql.parser.exception.SQLParsingException;

/**
 * SQL parser engine.
 */
@RequiredArgsConstructor
public final class SQLParserEngine {
    
    private final String databaseTypeName;
    
    private final String sql;
    
    /**
     * Parse SQL to abstract syntax tree.
     *
     * @return abstract syntax tree of SQL
     */
    public ParserRuleContext parse() {
        SQLParser sqlParser = SQLParserFactory.newInstance(databaseTypeName, sql);
        ParseTree parseTree;
        try {
            ((Parser) sqlParser).setErrorHandler(new BailErrorStrategy());
            ((Parser) sqlParser).getInterpreter().setPredictionMode(PredictionMode.SLL);
            parseTree = sqlParser.execute().getChild(0);
        } catch (final ParseCancellationException ex) {
            ((Parser) sqlParser).reset();
            ((Parser) sqlParser).setErrorHandler(new DefaultErrorStrategy());
            ((Parser) sqlParser).getInterpreter().setPredictionMode(PredictionMode.LL);
            parseTree = sqlParser.execute().getChild(0);
        }
        if (parseTree instanceof ErrorNode) {
            throw new SQLParsingException(String.format("Unsupported SQL of `%s`", sql));
        }
        return (ParserRuleContext) parseTree;
    }
}
