/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.infra.optimize.core.local;

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

import lombok.Getter;

import java.util.List;

/**
 * Relational expression representing a scan of a CSV file.
 */
@Getter
public final class LocalTableScan extends TableScan implements EnumerableRel {
    
    private final LocalTranslatableTable csvTable;
    
    private final int[] fields;
    
    public LocalTableScan(final RelOptCluster cluster, final RelOptTable table, final LocalTranslatableTable csvTable, final int[] fields) {
        super(cluster, cluster.traitSetOf(EnumerableConvention.INSTANCE), table);
        this.csvTable = csvTable;
        this.fields = fields;
        assert csvTable != null;
    }
    
    /**
     * Copy rel node.
     * @param traitSet trait set
     * @param inputs list of rel node
     * @return local table scan
     */
    @Override public RelNode copy(final RelTraitSet traitSet, final List<RelNode> inputs) {
        assert inputs.isEmpty();
        return new LocalTableScan(getCluster(), table, csvTable, fields);
    }
    
    /**
     * Explain terms.
     * @param pw rel writer
     * @return rel writer
     */
    @Override public RelWriter explainTerms(final RelWriter pw) {
        return super.explainTerms(pw).item("fields", Primitive.asList(fields));
    }
    
    /**
     * Derive row type.
     * @return rel data type
     */
    @Override public RelDataType deriveRowType() {
        final List<RelDataTypeField> fieldList = table.getRowType().getFieldList();
        final RelDataTypeFactory.Builder builder = getCluster().getTypeFactory().builder();
        for (int field : fields) {
            builder.add(fieldList.get(field));
        }
        return builder.build();
    }
    
    /**
     * Register a rule.
     * @param planner rel opt planner
     */
    @Override public void register(final RelOptPlanner planner) {
        planner.addRule(LocalProjectTableScanRule.INSTANCE);
    }
    
    /**
     * Compute cost.
     * @param planner rel pt planner
     * @param mq rel metadata query
     * @return rel opt cost
     */
    @Override public RelOptCost computeSelfCost(final RelOptPlanner planner, final RelMetadataQuery mq) {
        // Multiply the cost by a factor that makes a scan more attractive if it
        // has significantly fewer fields than the original scan.
        //
        // The "+ 2D" on top and bottom keeps the function fairly smooth.
        //
        // For example, if table has 3 fields, project has 1 field,
        // then factor = (1 + 2) / (3 + 2) = 0.6
        return super.computeSelfCost(planner, mq).multiplyBy(((double) fields.length + 2D) / ((double) table.getRowType().getFieldCount() + 2D));
    }
    
    /**
     * Implement enumberable rel.
     * @param implementor enumerable rel implementor
     * @param pref prefer
     * @return reulst of implement
     */
    public Result implement(final EnumerableRelImplementor implementor, final Prefer pref) {
        PhysType physType = PhysTypeImpl.of(implementor.getTypeFactory(), getRowType(), pref.preferArray());
        return implementor.result(physType, Blocks.toBlock(Expressions.call(table.getExpression(LocalTranslatableTable.class),
                    "project", implementor.getRootExpression(), Expressions.constant(fields))));
    }
}
