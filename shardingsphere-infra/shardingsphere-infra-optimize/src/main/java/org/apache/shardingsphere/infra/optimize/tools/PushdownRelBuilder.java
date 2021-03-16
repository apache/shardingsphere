package org.apache.shardingsphere.infra.optimize.tools;

import org.apache.calcite.plan.Contexts;
import org.apache.calcite.plan.RelOptCluster;
import org.apache.calcite.plan.RelOptSchema;
import org.apache.calcite.rel.core.RelFactories;
import org.apache.calcite.rel.core.TableScan;
import org.apache.calcite.tools.RelBuilder;

public class PushdownRelBuilder extends RelBuilder {
    
    private PushdownRelBuilder(RelOptCluster cluster, RelOptSchema relOptSchema) {
        super(Contexts.of(RelFactories.DEFAULT_STRUCT), cluster, relOptSchema);
    }
    
    
    
    public static PushdownRelBuilder create(TableScan tableScan) {
        return new PushdownRelBuilder(tableScan.getCluster(), tableScan.getTable().getRelOptSchema());
    }
}
