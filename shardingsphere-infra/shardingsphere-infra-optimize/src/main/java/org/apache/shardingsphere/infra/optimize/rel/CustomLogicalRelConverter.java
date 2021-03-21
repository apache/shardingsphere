package org.apache.shardingsphere.infra.optimize.rel;

import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.RelShuttleImpl;
import org.apache.calcite.rel.core.TableScan;
import org.apache.shardingsphere.infra.optimize.rel.logical.LogicalScan;

public final class CustomLogicalRelConverter extends RelShuttleImpl {
    
    private CustomLogicalRelConverter() {
        
    }
    
    @Override
    public RelNode visit(final TableScan scan) {
        return LogicalScan.create(scan);
    }
    
    /**
     * Convert operator of logical plan to custom operator defined by ShardingSphere, e.g. {@link LogicalScan}.
     * @param relNode logical plan to convert
     * @return converted logical plan
     */
    public static RelNode convert(final RelNode relNode) {
        return relNode.accept(new CustomLogicalRelConverter());
    }
}
