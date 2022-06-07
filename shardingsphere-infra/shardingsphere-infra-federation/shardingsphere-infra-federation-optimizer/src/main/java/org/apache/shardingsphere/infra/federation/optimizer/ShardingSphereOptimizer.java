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

package org.apache.shardingsphere.infra.federation.optimizer;

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
import org.apache.calcite.sql2rel.SqlToRelConverter;
import org.apache.calcite.tools.Programs;
import org.apache.calcite.util.ImmutableIntList;
import org.apache.calcite.util.Pair;
import org.apache.shardingsphere.infra.exception.ShardingSphereException;
import org.apache.shardingsphere.infra.federation.optimizer.context.OptimizerContext;
import org.apache.shardingsphere.infra.federation.optimizer.converter.SQLNodeConverterEngine;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;

import java.util.ArrayList;
import java.util.List;

/**
 * ShardingSphere optimizer.
 */
@RequiredArgsConstructor
@Getter
public final class ShardingSphereOptimizer {
    
    private final OptimizerContext context;
    
    /**
     * Optimize query execution plan.
     * 
     * @param databaseName database name
     * @param schemaName schema name
     * @param sqlStatement SQL statement to be optimized
     * @return optimized relational node
     */
    public RelNode optimize(final String databaseName, final String schemaName, final SQLStatement sqlStatement) {
        try {
            SqlToRelConverter converter = context.getPlannerContexts().get(databaseName).getConverters().get(schemaName);
            SqlNode sqlNode = SQLNodeConverterEngine.convertToSQLNode(sqlStatement);
            RelNode relNode = converter.convertQuery(sqlNode, true, true).rel;
            RelDataType resultType = context.getPlannerContexts().get(databaseName).getValidators().get(schemaName).getValidatedNodeType(sqlNode);
            return optimize(converter, relNode, resultType);
        } catch (final UnsupportedOperationException ex) {
            throw new ShardingSphereException(ex);
        }
    }
    
    private RelNode optimize(final SqlToRelConverter converter, final RelNode relNode, final RelDataType resultType) {
        RelOptPlanner planner = converter.getCluster().getPlanner();
        RelNode changedRelNode = planner.changeTraits(relNode, converter.getCluster().traitSet().replace(EnumerableConvention.INSTANCE));
        RelRoot relRoot = createRelRoot(changedRelNode, resultType);
        return Programs.standard().run(planner, relRoot.rel, getDesireRootTraitSet(relRoot), ImmutableList.of(), ImmutableList.of());
    }
    
    private RelRoot createRelRoot(final RelNode relNode, final RelDataType resultType) {
        RelDataType rowType = relNode.getRowType();
        List<Pair<Integer, String>> fields = Pair.zip(ImmutableIntList.identity(rowType.getFieldCount()), rowType.getFieldNames());
        RelCollation collation = relNode instanceof Sort ? ((Sort) relNode).collation : RelCollations.EMPTY;
        return new RelRoot(relNode, resultType, SqlKind.SELECT, fields, collation, new ArrayList<>());
    }
    
    private RelTraitSet getDesireRootTraitSet(final RelRoot relRoot) {
        return relRoot.rel.getTraitSet().replace(EnumerableConvention.INSTANCE).replace(relRoot.collation).simplify();
    }
}
