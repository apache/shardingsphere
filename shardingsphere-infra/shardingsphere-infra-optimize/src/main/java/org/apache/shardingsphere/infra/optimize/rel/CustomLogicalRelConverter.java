package org.apache.shardingsphere.infra.optimize.rel;

import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.RelShuttleImpl;
import org.apache.calcite.rel.core.TableScan;
import org.apache.shardingsphere.infra.optimize.rel.logical.LogicalScan;

public class CustomLogicalRelConverter extends RelShuttleImpl {
    
    private CustomLogicalRelConverter() {}
    
    @Override
    public RelNode visit(final TableScan scan) {
        return LogicalScan.create(scan);
    }
    
    public static RelNode convert(RelNode relNode) {
        return relNode.accept(new CustomLogicalRelConverter());
    }
}
