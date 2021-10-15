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
