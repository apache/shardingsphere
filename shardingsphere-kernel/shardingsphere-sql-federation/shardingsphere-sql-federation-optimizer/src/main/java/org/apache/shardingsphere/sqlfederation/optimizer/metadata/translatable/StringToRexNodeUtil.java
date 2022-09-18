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

package org.apache.shardingsphere.sqlfederation.optimizer.metadata.translatable;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.apache.calcite.jdbc.JavaTypeFactoryImpl;
import org.apache.calcite.rel.type.RelDataTypeSystem;
import org.apache.calcite.rex.RexBuilder;
import org.apache.calcite.rex.RexNode;
import org.apache.shardingsphere.sqlfederation.optimizer.parser.rexnode.ParseRexNodeLexer;
import org.apache.shardingsphere.sqlfederation.optimizer.parser.rexnode.ParseRexNodeParser;

import java.io.IOException;

/**
 * Utility for parsing string and generate rex node.
 */
public final class StringToRexNodeUtil {
    
    /**
     * Parse string and generate rex node.
     * @param filterValue filter condition
     * @param rexBuilder used to build rex node
     * @return rex node
     * @throws IOException io exception
     */
    public static RexNode buildRexNode(final String filterValue, final RexBuilder rexBuilder) throws IOException {
        CharStream input = CharStreams.fromString(filterValue);
        ParseRexNodeLexer lexer = new ParseRexNodeLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        ParseRexNodeParser parser = new ParseRexNodeParser(tokens);
        ParseTree tree = parser.expression();
        ParseRexNodeVisitorImpl visitor = new ParseRexNodeVisitorImpl(rexBuilder, new JavaTypeFactoryImpl(RelDataTypeSystem.DEFAULT));
        return visitor.visit(tree);
    }
}
