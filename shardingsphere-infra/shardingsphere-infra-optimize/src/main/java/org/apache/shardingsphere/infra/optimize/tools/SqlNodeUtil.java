package org.apache.shardingsphere.infra.optimize.tools;

import org.apache.calcite.sql.SqlNode;

public class SqlNodeUtil {
    
    public static <E extends SqlNode> E clone(E e) {
        if(e != null) {
            return SqlNode.clone(e);
        }
        return null;
    }
}
