package org.apache.shardingsphere.infra.optimize.tools;

import org.apache.calcite.sql.SqlNode;

/**
 * Untility function for {@link SqlNode}.
 */
public class SqlNodeUtil {
    
    /**
     * Clone this <code>SqlNode</code>.
     * @param e sql node
     * @param <E> sub-class of {@link SqlNode} 
     * @return clone result
     */
    public static <E extends SqlNode> E clone(final E e) {
        if (e != null) {
            return SqlNode.clone(e);
        }
        return null;
    }
}
