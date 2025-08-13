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

package org.apache.shardingsphere.sqlfederation.compiler.rel.operator.physical;

import org.apache.calcite.adapter.enumerable.EnumerableRel;
import org.apache.calcite.adapter.enumerable.EnumerableRelImplementor;
import org.apache.calcite.plan.RelOptCluster;
import org.apache.calcite.plan.RelOptTable;
import org.apache.calcite.plan.RelTraitSet;
import org.apache.calcite.prepare.Prepare.CatalogReader;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.core.TableModify;
import org.apache.calcite.rex.RexNode;

import java.util.List;

/**
 * Enumerable modify.
 */
public final class EnumerableModify extends TableModify implements EnumerableRel {
    
    public EnumerableModify(final RelOptCluster cluster, final RelTraitSet traitSet, final RelOptTable table, final CatalogReader catalogReader,
                            final RelNode input, final Operation operation, final List<String> updateColumnList, final List<RexNode> sourceExpressionList, final boolean flattened) {
        super(cluster, traitSet, table, catalogReader, input, operation, updateColumnList, sourceExpressionList, flattened);
    }
    
    @Override
    public RelNode copy(final RelTraitSet traitSet, final List<RelNode> inputs) {
        return new EnumerableModify(getCluster(), traitSet, getTable(), getCatalogReader(), sole(inputs), getOperation(), getUpdateColumnList(), getSourceExpressionList(), isFlattened());
    }
    
    @Override
    public Result implement(final EnumerableRelImplementor implementor, final Prefer prefer) {
        // TODO generate modification statements based on dataset and related table information.
        throw new UnsupportedOperationException();
    }
}
