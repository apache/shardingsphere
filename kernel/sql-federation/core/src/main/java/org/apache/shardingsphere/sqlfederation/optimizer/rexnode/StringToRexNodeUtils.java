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

package org.apache.shardingsphere.sqlfederation.optimizer.rexnode;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.apache.calcite.jdbc.JavaTypeFactoryImpl;
import org.apache.calcite.rel.type.RelDataTypeSystem;
import org.apache.calcite.rex.RexBuilder;
import org.apache.calcite.rex.RexNode;
import org.apache.shardingsphere.rexnode.autogen.SQLOptimizerRexNodeLexer;
import org.apache.shardingsphere.rexnode.autogen.SQLOptimizerRexNodeParser;

import java.util.Map;

/**
 * Parsing string and generate rex node utility class.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class StringToRexNodeUtils {
    
    /**
     * Parse string and generate rex node.
     * 
     * @param filterValue filter condition
     * @param rexBuilder used to build rex node
     * @param parameters parameters for SQL placeholder
     * @param columnMap mapping of column id and column type
     * @return rex node
     */
    public static RexNode buildRexNode(final String filterValue, final RexBuilder rexBuilder, final Map<String, Object> parameters, final Map<Integer, Integer> columnMap) {
        CharStream input = CharStreams.fromString(filterValue);
        SQLOptimizerRexNodeLexer lexer = new SQLOptimizerRexNodeLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        SQLOptimizerRexNodeParser parser = new SQLOptimizerRexNodeParser(tokens);
        ParseTree tree = parser.expression();
        SQLOptimizerRexNodeVisitor visitor = new SQLOptimizerRexNodeVisitor(rexBuilder, new JavaTypeFactoryImpl(RelDataTypeSystem.DEFAULT), parameters, columnMap);
        return visitor.visit(tree);
    }
}
