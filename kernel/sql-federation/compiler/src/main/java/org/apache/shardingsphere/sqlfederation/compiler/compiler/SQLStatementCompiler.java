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

package org.apache.shardingsphere.sqlfederation.compiler.compiler;

import lombok.RequiredArgsConstructor;
import org.apache.calcite.plan.Convention;
import org.apache.calcite.plan.RelOptPlanner;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.metadata.JaninoRelMetadataProvider;
import org.apache.calcite.rel.metadata.RelMetadataQueryBase;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.sql.SqlNode;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sqlfederation.compiler.SQLFederationExecutionPlan;
import org.apache.shardingsphere.sqlfederation.compiler.planner.builder.SQLFederationPlannerBuilder;
import org.apache.shardingsphere.sqlfederation.compiler.rel.converter.SQLFederationRelConverter;
import org.apache.shardingsphere.sqlfederation.compiler.rel.rewriter.LogicalScanRelRewriter;
import org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.SQLNodeConverterEngine;

/**
 * SQL statement compiler.
 */
@RequiredArgsConstructor
public final class SQLStatementCompiler {
    
    private final SQLFederationRelConverter converter;
    
    private final Convention convention;
    
    /**
     * Compile sql statement to execution plan.
     *
     * @param sqlStatement SQL statement
     * @param databaseType database type
     * @return sql federation execution plan
     */
    public SQLFederationExecutionPlan compile(final SQLStatement sqlStatement, final String databaseType) {
        RelMetadataQueryBase.THREAD_PROVIDERS.set(JaninoRelMetadataProvider.DEFAULT);
        SqlNode sqlNode = SQLNodeConverterEngine.convert(sqlStatement);
        RelNode logicalPlan = converter.convertQuery(sqlNode, true, true).rel;
        RelDataType resultColumnType = converter.getValidatedNodeType(sqlNode);
        RelNode rewritePlan = rewrite(logicalPlan, databaseType);
        RelNode physicalPlan = optimize(rewritePlan, converter);
        RelMetadataQueryBase.THREAD_PROVIDERS.remove();
        return new SQLFederationExecutionPlan(physicalPlan, resultColumnType);
    }
    
    private RelNode rewrite(final RelNode logicalPlan, final String databaseType) {
        RelNode rewrittenPlan = LogicalScanRelRewriter.rewrite(logicalPlan, databaseType);
        RelOptPlanner hepPlanner = SQLFederationPlannerBuilder.buildHepPlanner();
        hepPlanner.setRoot(rewrittenPlan);
        return hepPlanner.findBestExp();
    }
    
    private RelNode optimize(final RelNode rewritePlan, final SQLFederationRelConverter converter) {
        RelOptPlanner planner = converter.getCluster().getPlanner();
        if (rewritePlan.getTraitSet().equals(converter.getCluster().traitSet().replace(convention))) {
            planner.setRoot(rewritePlan);
        } else {
            planner.setRoot(planner.changeTraits(rewritePlan, converter.getCluster().traitSet().replace(convention)));
        }
        return planner.findBestExp();
    }
}
