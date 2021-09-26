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

package org.apache.shardingsphere.infra.optimize;

import com.google.common.collect.ImmutableList;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.calcite.adapter.enumerable.EnumerableConvention;
import org.apache.calcite.plan.RelOptPlanner;
import org.apache.calcite.plan.RelTraitSet;
import org.apache.calcite.rel.RelCollation;
import org.apache.calcite.rel.RelCollations;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.RelRoot;
import org.apache.calcite.rel.core.Sort;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.sql.SqlKind;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.tools.Program;
import org.apache.calcite.tools.Programs;
import org.apache.calcite.util.ImmutableIntList;
import org.apache.calcite.util.Pair;
import org.apache.shardingsphere.infra.exception.ShardingSphereException;
import org.apache.shardingsphere.infra.optimize.context.customized.CustomizedOptimizerContext;
import org.apache.shardingsphere.infra.optimize.converter.SQLNodeConvertEngine;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;

import java.util.ArrayList;
import java.util.List;

/**
 * ShardingSphere optimizer.
 */
@RequiredArgsConstructor
@Getter
public final class ShardingSphereOptimizer {
    
    private final CustomizedOptimizerContext context;
    
    /**
     * Optimize query execution plan.
     * 
     * @param schemaName schema name
     * @param sqlStatement SQL statement to be optimized
     * @return optimized relational node
     */
    public RelNode optimize(final String schemaName, final SQLStatement sqlStatement) {
        try {
            SqlNode sqlNode = SQLNodeConvertEngine.convert(sqlStatement);
            SqlNode validNode = context.getValidators().get(schemaName).validate(sqlNode);
            RelDataType resultType = context.getValidators().get(schemaName).getValidatedNodeType(sqlNode);
            RelNode queryPlan = context.getConverters().get(schemaName).convertQuery(validNode, false, true).rel;
            return optimize(schemaName, queryPlan, resultType);
        } catch (final UnsupportedOperationException ex) {
            throw new ShardingSphereException(ex);
        }
    }
    
    private RelNode optimize(final String schemaName, final RelNode queryPlan, final RelDataType resultType) {
        RelOptPlanner planner = context.getConverters().get(schemaName).getCluster().getPlanner();
        RelNode node = planner.changeTraits(queryPlan, context.getConverters().get(schemaName).getCluster().traitSet().replace(EnumerableConvention.INSTANCE));
        RelRoot root = constructRoot(node, resultType);
        Program program = Programs.standard();
        return program.run(planner, root.rel, getDesireRootTraitSet(root), ImmutableList.of(), ImmutableList.of());
    }
    
    private RelRoot constructRoot(final RelNode node, final RelDataType resultType) {
        RelDataType rowType = node.getRowType();
        List<Pair<Integer, String>> fields = Pair.zip(ImmutableIntList.identity(rowType.getFieldCount()), rowType.getFieldNames());
        RelCollation collation = node instanceof Sort ? ((Sort) node).collation : RelCollations.EMPTY;
        return new RelRoot(node, resultType, SqlKind.SELECT, fields, collation, new ArrayList<>());
    }
    
    private RelTraitSet getDesireRootTraitSet(final RelRoot root) {
        return root.rel.getTraitSet().replace(EnumerableConvention.INSTANCE).replace(root.collation).simplify();
    }
}
