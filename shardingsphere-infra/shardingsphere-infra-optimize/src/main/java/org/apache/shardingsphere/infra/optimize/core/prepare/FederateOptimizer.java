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

package org.apache.shardingsphere.infra.optimize.core.prepare;

import com.google.common.collect.ImmutableList;
import org.apache.calcite.jdbc.CalcitePrepare;
import org.apache.calcite.jdbc.CalciteSchema;
import org.apache.calcite.plan.Convention;
import org.apache.calcite.prepare.Prepare;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.RelRoot;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.sql.SqlExplainFormat;
import org.apache.calcite.sql.SqlExplainLevel;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.validate.SqlValidator;
import org.apache.calcite.sql.validate.SqlValidatorCatalogReader;
import org.apache.calcite.sql2rel.SqlToRelConverter;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.List;

public class FederateOptimizer extends Prepare {

    public FederateOptimizer(final CalcitePrepare.Context context, final SqlValidatorCatalogReader catalogReader, final Convention resultConvention) {
        super(context, (CatalogReader) catalogReader, resultConvention);
    }

    /**
     * Optimizes a query plan.
     *
     * @param root Root of relational expression tree
     * @return an equivalent optimized relational expression
     */
    public RelRoot optimize(final RelRoot root) {
        return optimize(root, getMaterializations(), getLattices());
    }

    @Override
    protected PreparedResult createPreparedExplanation(@Nullable final RelDataType relDataType, final RelDataType relDataType1, @Nullable final RelRoot relRoot,
                                                       final SqlExplainFormat sqlExplainFormat, final SqlExplainLevel sqlExplainLevel) {
        return null;
    }

    @Override
    protected PreparedResult implement(final RelRoot relRoot) {
        return null;
    }

    @Override
    protected SqlToRelConverter getSqlToRelConverter(final SqlValidator sqlValidator, final CatalogReader catalogReader, final SqlToRelConverter.Config config) {
        return null;
    }

    @Override
    public RelNode flattenTypes(final RelNode relNode, final boolean b) {
        return null;
    }

    @Override
    protected RelNode decorrelate(final SqlToRelConverter sqlToRelConverter, final SqlNode sqlNode, final RelNode relNode) {
        return null;
    }

    @Override
    protected List<Materialization> getMaterializations() {
        return ImmutableList.of();
    }

    @Override
    protected List<CalciteSchema.LatticeEntry> getLattices() {
        return ImmutableList.of();
    }

    @Override
    protected void init(final Class aClass) {
    }

    @Override
    protected SqlValidator getSqlValidator() {
        return null;
    }
}
