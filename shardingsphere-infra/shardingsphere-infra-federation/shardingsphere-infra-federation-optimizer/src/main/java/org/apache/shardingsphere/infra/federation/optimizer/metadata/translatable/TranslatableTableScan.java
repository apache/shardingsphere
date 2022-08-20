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

package org.apache.shardingsphere.infra.federation.optimizer.metadata.translatable;

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
import org.apache.calcite.rex.RexLiteral;
import org.apache.calcite.rex.RexNode;
import org.apache.calcite.sql.SqlKind;
import org.codehaus.groovy.runtime.InvokerHelper;

import java.util.Arrays;
import java.util.List;

/**
 * Relational expression representing a scan.
 * Like any table scan, it serves as a leaf node of a query tree.
 */
@Getter
public class TranslatableTableScan extends TableScan implements EnumerableRel {
    
    private final FederationTranslatableTable translatableTable;
    
    private final int[] fields;
    
    private final List<RexNode> filters;
    
    public TranslatableTableScan(final RelOptCluster cluster, final RelOptTable table,
                                 final FederationTranslatableTable translatableTable, final int[] fields) {
        super(cluster, cluster.traitSetOf(EnumerableConvention.INSTANCE), ImmutableList.of(), table);
        this.translatableTable = translatableTable;
        this.fields = fields;
        this.filters = null;
        assert translatableTable != null;
    }
    
    public TranslatableTableScan(final RelOptCluster cluster, final RelOptTable table,
                                 final FederationTranslatableTable translatableTable, final List<RexNode> filters, final int[] fields) {
        super(cluster, cluster.traitSetOf(EnumerableConvention.INSTANCE), ImmutableList.of(), table);
        this.translatableTable = translatableTable;
        this.fields = fields;
        this.filters = filters;
        assert translatableTable != null;
    }
    
    @Override
    public RelNode copy(final RelTraitSet traitSet, final List<RelNode> inputs) {
        assert inputs.isEmpty();
        return new TranslatableTableScan(getCluster(), table, translatableTable, fields);
    }
    
    @Override
    public String toString() {
        if (null != filters) {
            final String[] filterValues = new String[fields.length];
            addFilter(filters, filterValues);
            return "TranslatableTableScan{"
                    + "translatableTable=" + translatableTable
                    + ", fields=" + Arrays.toString(fields)
                    + ", filters=" + Arrays.toString(filterValues)
                    + '}';
        }
        return "TranslatableTableScan{"
                + "translatableTable=" + translatableTable
                + ", fields=" + Arrays.toString(fields)
                + '}';
    }
    
    @Override
    public RelWriter explainTerms(final RelWriter pw) {
        if (null != filters) {
            final String[] filterValues = new String[fields.length];
            addFilter(filters, filterValues);
            return super.explainTerms(pw)
                    .item("fields", Primitive.asList(fields))
                    .item("filters", Primitive.asList(filterValues));
        }
        return super.explainTerms(pw)
                .item("fields", Primitive.asList(fields));
    }
    
    @Override
    public RelDataType deriveRowType() {
        final List<RelDataTypeField> fieldList = table.getRowType().getFieldList();
        final RelDataTypeFactory.Builder builder =
                getCluster().getTypeFactory().builder();
        for (int field : fields) {
            builder.add(fieldList.get(field));
        }
        return builder.build();
    }
    
    @Override
    public void register(final RelOptPlanner planner) {
        planner.addRule(TranslatableProjectFilterRule.INSTANCE);
        planner.addRule(TranslatableFilterRule.INSTANCE);
        planner.addRule(TranslatableProjectRule.INSTANCE);
    }
    
    @Override
    public RelOptCost computeSelfCost(final RelOptPlanner planner,
                                      final RelMetadataQuery mq) {
        return super.computeSelfCost(planner, mq)
                .multiplyBy(((double) fields.length + 2D)
                        / ((double) table.getRowType().getFieldCount() + 2D));
    }
    
    /**
     * Generate code for translatable table scan.
     *
     * @param implementor EnumerableRelImplementor
     * @param pref Prefer
     * @return generated code
     */
    public Result implement(final EnumerableRelImplementor implementor, final Prefer pref) {
        PhysType physType =
                PhysTypeImpl.of(
                        implementor.getTypeFactory(),
                        getRowType(),
                        pref.preferArray());
        
        if (null != filters) {
            final String[] filterValues = new String[fields.length];
            addFilter(filters, filterValues);
            return implementor.result(
                    physType,
                    Blocks.toBlock(
                            Expressions.call(table.getExpression(FederationTranslatableTable.class),
                                    "projectAndFilter", implementor.getRootExpression(),
                                    Expressions.constant(filterValues), Expressions.constant(fields))));
        }
        return implementor.result(
                physType,
                Blocks.toBlock(
                        Expressions.call(table.getExpression(FederationTranslatableTable.class),
                                "project", implementor.getRootExpression(),
                                Expressions.constant(fields))));
    }
    
    private boolean addFilter(final List<RexNode> filters, final String[] filterValues) {
        for (RexNode filter : filters) {
            if (filter.isA(SqlKind.AND)) {
                // We cannot refine(remove) the operands of AND,
                // it will cause o.a.c.i.TableScanNode.createFilterable filters check failed.
                ((RexCall) filter).getOperands().forEach(subFilter -> addFilter(InvokerHelper.asList(subFilter), filterValues));
                continue;
            } else if (filter.isA(SqlKind.EQUALS)) {
                final RexCall call = (RexCall) filter;
                String tmp = call.toString();
                RexNode left = call.getOperands().get(0);
                if (left.isA(SqlKind.CAST)) {
                    left = ((RexCall) left).operands.get(0);
                }
                final RexNode right = call.getOperands().get(1);
                if (!(left instanceof RexInputRef && right instanceof RexLiteral)) {
                    continue;
                }
                final int index = ((RexInputRef) left).getIndex();
                if (filterValues[index] == null) {
                    filterValues[index] = ((RexLiteral) right).getValue2().toString();
                    return true;
                }
            }
        }
        return false;
    }
    
}
