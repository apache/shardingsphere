package org.apache.shardingsphere.infra.optimize.tools;

import org.apache.calcite.rel.core.TableScan;
import org.apache.calcite.util.Util;

import java.util.List;

public class RelNodeUtil {
    
    public static String getTableName(TableScan tableScan) {
        List<String> tables = tableScan.getTable().getQualifiedName();
        return Util.last(tables);
    }
}
