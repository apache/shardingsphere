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

package org.apache.shardingsphere.sqlfederation.compiler.context;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.parser.rule.SQLParserRule;
import org.apache.shardingsphere.sqlfederation.compiler.context.parser.OptimizerParserContext;
import org.apache.shardingsphere.sqlfederation.compiler.context.planner.OptimizerPlannerContext;

import java.util.Map;

/**
 * Optimizer context.
 */
@RequiredArgsConstructor
public final class OptimizerContext {
    
    @Getter
    private final SQLParserRule sqlParserRule;
    
    private final Map<String, OptimizerParserContext> parserContexts;
    
    private final Map<String, OptimizerPlannerContext> plannerContexts;
    
    /**
     * Get parser context.
     * 
     * @param databaseName database name
     * @return parser context
     */
    public OptimizerParserContext getParserContext(final String databaseName) {
        return parserContexts.get(databaseName.toLowerCase());
    }
    
    /**
     * Put parser context.
     *
     * @param databaseName database name
     * @param parserContext parser context
     */
    public void putParserContext(final String databaseName, final OptimizerParserContext parserContext) {
        parserContexts.put(databaseName.toLowerCase(), parserContext);
    }
    
    /**
     * Remove parser context.
     *
     * @param databaseName database name
     */
    public void removeParserContext(final String databaseName) {
        parserContexts.remove(databaseName.toLowerCase());
    }
    
    /**
     * Get planner context.
     *
     * @param databaseName database name
     * @return Planner
     */
    public OptimizerPlannerContext getPlannerContext(final String databaseName) {
        return plannerContexts.get(databaseName.toLowerCase());
    }
    
    /**
     * Put planner context.
     * 
     * @param databaseName database name
     * @param plannerContext planner context
     */
    public void putPlannerContext(final String databaseName, final OptimizerPlannerContext plannerContext) {
        plannerContexts.put(databaseName.toLowerCase(), plannerContext);
    }
    
    /**
     * Remove planner context.
     * 
     * @param databaseName database name
     */
    public void removePlannerContext(final String databaseName) {
        plannerContexts.remove(databaseName.toLowerCase());
    }
}
