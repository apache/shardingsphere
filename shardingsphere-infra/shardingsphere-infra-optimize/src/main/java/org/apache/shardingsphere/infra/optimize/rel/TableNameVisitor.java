package org.apache.shardingsphere.infra.optimize.rel;

import org.apache.calcite.plan.RelOptTable;
import org.apache.calcite.rel.RelHomogeneousShuttle;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.core.TableScan;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Gather all table names in the reltional expression 
 */
public final class TableNameVisitor extends RelHomogeneousShuttle {
    
    private Set<String> tableNames = new HashSet<>();
    
    @Override
    public RelNode visit(TableScan scan) {
        final RelOptTable scanTable = scan.getTable();
        final List<String> qualifiedName = scanTable.getQualifiedName();
        tableNames.add(qualifiedName.get(qualifiedName.size() - 1));
        return super.visit(scan);
    }
    
    public void reset() {
        tableNames.clear();
    }
    
    public Set<String> getTableNames() {
        return tableNames;
    }
}
