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

import java.util.ArrayList;
import java.util.List;
import org.apache.calcite.rel.RelCollation;
import org.apache.calcite.rel.RelDistribution;
import org.apache.calcite.rel.RelDistributionTraitDef;
import org.apache.calcite.rel.RelReferentialConstraint;
import org.apache.calcite.schema.Statistic;
import org.apache.calcite.util.ImmutableBitSet;

/**
 * Statistic of federation table.
 */
public final class FederationTableStatistic implements Statistic {
    
    @Override
    public Double getRowCount() {
        return Statistic.super.getRowCount();
    }

    @Override
    public boolean isKey(final ImmutableBitSet columns) {
        return Statistic.super.isKey(columns);
    }

    @Override
    public List<ImmutableBitSet> getKeys() {
        return Statistic.super.getKeys();
    }

    @Override
    public List<RelReferentialConstraint> getReferentialConstraints() {
        return new ArrayList<>();
    }

    @Override
    public List<RelCollation> getCollations() {
        return new ArrayList<>();
    }

    @Override
    public RelDistribution getDistribution() {
        return RelDistributionTraitDef.INSTANCE.getDefault();
    }
}
