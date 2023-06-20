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

package org.apache.shardingsphere.sqlfederation.compiler.operator.physical.enumerable;

import com.google.common.collect.ImmutableList;
import lombok.Getter;
import org.apache.calcite.adapter.enumerable.EnumerableConvention;
import org.apache.calcite.adapter.enumerable.EnumerableRel;
import org.apache.calcite.adapter.enumerable.EnumerableRelImplementor;
import org.apache.calcite.adapter.enumerable.PhysType;
import org.apache.calcite.adapter.enumerable.PhysTypeImpl;
import org.apache.calcite.linq4j.tree.Blocks;
import org.apache.calcite.linq4j.tree.Expressions;
import org.apache.calcite.linq4j.tree.Primitive;
import org.apache.calcite.plan.RelOptCluster;
import org.apache.calcite.plan.RelOptCost;
import org.apache.calcite.plan.RelOptPlanner;
import org.apache.calcite.plan.RelOptTable;
import org.apache.calcite.plan.RelTraitSet;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.RelWriter;
import org.apache.calcite.rel.core.TableScan;
import org.apache.calcite.rel.metadata.RelMetadataQuery;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.rel.type.RelDataTypeField;
import org.apache.calcite.rex.RexCall;
import org.apache.calcite.rex.RexInputRef;
import org.apache.calcite.rex.RexNode;
import org.apache.shardingsphere.sqlfederation.compiler.metadata.schema.SQLFederationTable;
import org.apache.shardingsphere.sqlfederation.compiler.planner.rule.transformation.PushFilterIntoScanRule;
import org.apache.shardingsphere.sqlfederation.compiler.planner.rule.transformation.PushProjectIntoScanRule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Enumerable push down table scan.
 */
@Getter
public final class EnumerablePushDownTableScan extends TableScan implements EnumerableRel {
    
    private final SQLFederationTable sqlFederationTable;
    
    private final int[] fields;
    
    private final List<RexNode> filters;
    
    private final int number;
    
    private final List<RexNode> expressions;
    
    public EnumerablePushDownTableScan(final RelOptCluster cluster, final RelOptTable table, final SQLFederationTable sqlFederationTable, final int[] fields) {
        super(cluster, cluster.traitSetOf(EnumerableConvention.INSTANCE), ImmutableList.of(), table);
        this.sqlFederationTable = sqlFederationTable;
        this.fields = fields;
        this.number = fields.length;
        this.filters = null;
        this.expressions = new ArrayList<>();
    }
    
    public EnumerablePushDownTableScan(final RelOptCluster cluster, final RelOptTable table, final SQLFederationTable sqlFederationTable, final int[] fields, final int number) {
        super(cluster, cluster.traitSetOf(EnumerableConvention.INSTANCE), ImmutableList.of(), table);
        this.sqlFederationTable = sqlFederationTable;
        this.fields = fields;
        this.number = number;
        this.filters = null;
        this.expressions = new ArrayList<>();
    }
    
    public EnumerablePushDownTableScan(final RelOptCluster cluster, final RelOptTable table, final SQLFederationTable sqlFederationTable,
                                       final List<RexNode> filters, final int[] fields) {
        super(cluster, cluster.traitSetOf(EnumerableConvention.INSTANCE), ImmutableList.of(), table);
        this.sqlFederationTable = sqlFederationTable;
        this.fields = fields;
        this.number = fields.length;
        this.filters = filters;
        this.expressions = new ArrayList<>();
    }
    
    public EnumerablePushDownTableScan(final RelOptCluster cluster, final RelOptTable table, final SQLFederationTable sqlFederationTable,
                                       final List<RexNode> filters, final int[] fields, final int number, final List<RexNode> expressions) {
        super(cluster, cluster.traitSetOf(EnumerableConvention.INSTANCE), ImmutableList.of(), table);
        this.sqlFederationTable = sqlFederationTable;
        this.fields = fields;
        this.number = number;
        this.filters = filters;
        this.expressions = expressions;
    }
    
    @Override
    public RelNode copy(final RelTraitSet traitSet, final List<RelNode> inputs) {
        return new EnumerablePushDownTableScan(getCluster(), table, sqlFederationTable, fields, number);
    }
    
    @Override
    public String toString() {
        if (null == filters) {
            return "EnumerablePushDownTableScan{sqlFederationTable=" + sqlFederationTable + ", fields=" + Arrays.toString(fields) + '}';
        }
        String[] filterValues = new String[number];
        addFilter(filters, filterValues);
        return "EnumerablePushDownTableScan{sqlFederationTable=" + sqlFederationTable + ", fields=" + Arrays.toString(fields) + ", filters=" + Arrays.toString(filterValues) + '}';
    }
    
    @Override
    public RelWriter explainTerms(final RelWriter relWriter) {
        if (null == filters) {
            return super.explainTerms(relWriter).item("fields", Primitive.asList(fields));
        }
        String[] filterValues = new String[number];
        addFilter(filters, filterValues);
        return super.explainTerms(relWriter).item("fields", Primitive.asList(fields)).item("filters", Primitive.asList(filterValues));
    }
    
    @Override
    public RelDataType deriveRowType() {
        List<RelDataTypeField> fieldList = table.getRowType().getFieldList();
        RelDataTypeFactory.Builder builder = getCluster().getTypeFactory().builder();
        for (int field : fields) {
            builder.add(fieldList.get(field));
        }
        return builder.build();
    }
    
    @Override
    public void register(final RelOptPlanner planner) {
        planner.addRule(PushFilterIntoScanRule.INSTANCE);
        planner.addRule(PushProjectIntoScanRule.INSTANCE);
    }
    
    @Override
    public RelOptCost computeSelfCost(final RelOptPlanner planner, final RelMetadataQuery mq) {
        return super.computeSelfCost(planner, mq).multiplyBy((number + 2D) / (table.getRowType().getFieldCount() + 2D));
    }
    
    @Override
    public Result implement(final EnumerableRelImplementor implementor, final Prefer pref) {
        PhysType physType = PhysTypeImpl.of(implementor.getTypeFactory(), getRowType(), pref.preferArray());
        if (null == filters) {
            return generateCodeForNullFilters(implementor, physType);
        } else {
            String[] filterValues = new String[number];
            addFilter(filters, filterValues);
            return generateCodeForFilters(implementor, physType, filterValues);
        }
    }
    
    private Result generateCodeForNullFilters(final EnumerableRelImplementor implementor, final PhysType physType) {
        if (fields.length == 1) {
            return implementor.result(physType, Blocks.toBlock(Expressions.call(table.getExpression(SQLFederationTable.class),
                    "projectScalar", implementor.getRootExpression(), Expressions.constant(fields))));
        }
        return implementor.result(physType, Blocks.toBlock(Expressions.call(table.getExpression(SQLFederationTable.class),
                "project", implementor.getRootExpression(), Expressions.constant(fields))));
    }
    
    private Result generateCodeForFilters(final EnumerableRelImplementor implementor, final PhysType physType, final String[] filterValues) {
        if (fields.length == 1) {
            return implementor.result(physType, Blocks.toBlock(Expressions.call(table.getExpression(SQLFederationTable.class),
                    "projectAndFilterScalar", implementor.getRootExpression(), Expressions.constant(filterValues), Expressions.constant(fields))));
        }
        return implementor.result(physType, Blocks.toBlock(Expressions.call(table.getExpression(SQLFederationTable.class),
                "projectAndFilter", implementor.getRootExpression(), Expressions.constant(filterValues), Expressions.constant(fields))));
    }
    
    private void addFilter(final List<RexNode> filters, final String[] filterValues) {
        int index = 0;
        for (RexNode each : filters) {
            RexCall call = (RexCall) each;
            String columnMap = generateColumnMap(call);
            filterValues[index] = each + columnMap;
            index++;
        }
    }
    
    private String generateColumnMap(final RexCall call) {
        Map<Integer, Integer> result = new HashMap<>();
        traverseRexCall(call, result);
        return result.toString();
    }
    
    private void traverseRexCall(final RexCall call, final Map<Integer, Integer> columnMap) {
        for (RexNode each : call.getOperands()) {
            if (each instanceof RexInputRef) {
                RexInputRef reference = (RexInputRef) each;
                String referenceName = reference.getName();
                int columnId = Integer.parseInt(referenceName.replace("$", ""));
                int columnType = sqlFederationTable.getColumnType(columnId);
                columnMap.put(columnId, columnType);
            }
            if (each instanceof RexCall) {
                traverseRexCall((RexCall) each, columnMap);
            }
        }
    }
}
