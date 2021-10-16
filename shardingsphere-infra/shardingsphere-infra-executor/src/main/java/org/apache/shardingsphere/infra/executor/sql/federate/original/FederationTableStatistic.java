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

package org.apache.shardingsphere.infra.executor.sql.federate.original;

import com.google.common.collect.ImmutableList;
import java.util.List;
import org.apache.calcite.rel.RelCollation;
import org.apache.calcite.rel.RelDistribution;
import org.apache.calcite.rel.RelDistributionTraitDef;
import org.apache.calcite.rel.RelReferentialConstraint;
import org.apache.calcite.schema.Statistic;
import org.apache.calcite.util.ImmutableBitSet;
import org.checkerframework.checker.nullness.qual.Nullable;

public class FederationTableStatistic implements Statistic {
    
    @Override
    public @Nullable Double getRowCount() {
        return Statistic.super.getRowCount();
    }

    @Override
    public boolean isKey(final ImmutableBitSet columns) {
        return Statistic.super.isKey(columns);
    }

    @Override
    public @Nullable List<ImmutableBitSet> getKeys() {
        return Statistic.super.getKeys();
    }

    @Override
    public @Nullable List<RelReferentialConstraint> getReferentialConstraints() {
        return ImmutableList.of();
    }

    @Override
    public @Nullable List<RelCollation> getCollations() {
        return ImmutableList.of();
    }

    @Override
    public @Nullable RelDistribution getDistribution() {
        return RelDistributionTraitDef.INSTANCE.getDefault();
    }
}
