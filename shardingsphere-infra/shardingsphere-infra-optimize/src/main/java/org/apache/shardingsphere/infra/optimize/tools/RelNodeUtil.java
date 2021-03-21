package org.apache.shardingsphere.infra.optimize.tools;

import org.apache.calcite.rel.core.TableScan;
import org.apache.calcite.util.Util;

import java.util.List;

/**
 * Untility function for {@link org.apache.calcite.rel.RelNode}
 */
public class RelNodeUtil {
    
    /**
     * Get table name from <code>TableScan</code>.
     * @param tableScan table scan
     * @return table name
     */
    public static String getTableName(final TableScan tableScan) {
        List<String> tables = tableScan.getTable().getQualifiedName();
        return Util.last(tables);
    }
}
