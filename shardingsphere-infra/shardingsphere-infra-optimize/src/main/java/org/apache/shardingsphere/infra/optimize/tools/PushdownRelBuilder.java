package org.apache.shardingsphere.infra.optimize.tools;

import com.google.common.collect.ImmutableList;
import org.apache.calcite.plan.Contexts;
import org.apache.calcite.plan.RelOptCluster;
import org.apache.calcite.plan.RelOptSchema;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.core.RelFactories;
import org.apache.calcite.rel.core.TableScan;
import org.apache.calcite.rex.RexLiteral;
import org.apache.calcite.rex.RexNode;
import org.apache.calcite.tools.RelBuilder;

public class PushdownRelBuilder extends RelBuilder {
    
    private PushdownRelBuilder(RelOptCluster cluster, RelOptSchema relOptSchema) {
        super(Contexts.of(RelFactories.DEFAULT_STRUCT), cluster, relOptSchema);
    }
    
    public PushdownRelBuilder sortLimit(RexNode offsetRex, RexNode fetchRex, Iterable<? extends RexNode> nodes) {
        int offset = offsetRex == null ? 0 : RexLiteral.intValue(offsetRex);
        if(fetchRex instanceof RexLiteral) {
            int fetch = fetchRex == null ? -1 : RexLiteral.intValue(fetchRex);
            super.sortLimit(offset, fetch, nodes);
        } else {
            // TODO 
            // fetchRex is a RexCall of which one of the operand is RexDynamicParam
            ImmutableList<RexNode> fields = super.fields();
            throw new UnsupportedOperationException();
        }
        return this;
    }
    
    private void replaceTop(RelNode node) {
        this.pop();
        super.push(node);
    }
    
    protected RelNode pop() {
        return super.build();
    }
    
    
    
    public static PushdownRelBuilder create(TableScan tableScan) {
        return new PushdownRelBuilder(tableScan.getCluster(), tableScan.getTable().getRelOptSchema());
    }
}
