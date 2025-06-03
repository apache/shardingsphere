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

package org.apache.shardingsphere.sqlfederation.compiler.planner.rule.converter;

import org.apache.calcite.adapter.enumerable.EnumerableConvention;
import org.apache.calcite.plan.Convention;
import org.apache.calcite.plan.RelTraitSet;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.convert.ConverterRule;
import org.apache.calcite.rel.core.TableModify;
import org.apache.calcite.rel.logical.LogicalTableModify;
import org.apache.calcite.schema.ModifiableTable;
import org.apache.shardingsphere.sqlfederation.compiler.rel.operator.physical.EnumerableModify;

/**
 * Enumerable modify converter rule.
 */
public final class EnumerableModifyConverterRule extends ConverterRule {
    
    public static final Config DEFAULT_CONFIG = Config.INSTANCE.withConversion(LogicalTableModify.class, Convention.NONE, EnumerableConvention.INSTANCE,
            EnumerableModifyConverterRule.class.getSimpleName()).withRuleFactory(EnumerableModifyConverterRule::new);
    
    private EnumerableModifyConverterRule(final Config config) {
        super(config);
    }
    
    @Override
    public RelNode convert(final RelNode rel) {
        TableModify modify = (TableModify) rel;
        ModifiableTable modifiableTable = modify.getTable().unwrap(ModifiableTable.class);
        if (null == modifiableTable) {
            return null;
        } else {
            RelTraitSet traitSet = modify.getTraitSet().replace(EnumerableConvention.INSTANCE);
            return new EnumerableModify(modify.getCluster(), traitSet, modify.getTable(), modify.getCatalogReader(), convert(modify.getInput(), traitSet),
                    modify.getOperation(), modify.getUpdateColumnList(), modify.getSourceExpressionList(), modify.isFlattened());
        }
    }
}
