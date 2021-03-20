package org.apache.shardingsphere.infra.executor.exec;


import org.apache.calcite.linq4j.Enumerator;
import org.apache.shardingsphere.infra.executor.exec.meta.Row;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResultMetaData;

/**
 * An instance of <code>Executor</code> the implementation of physical rational 
 * operator {@link org.apache.shardingsphere.infra.optimize.rel.physical.SSRel}.
 */
public interface Executor extends Enumerator<Row>, Iterable<Row> {
    
    /**
     * get the meta data of this <code>Executor</code>
     * @return
     */
    QueryResultMetaData getMetaData();
    
    /**
     * Initialize this Executor instance. This method should be invoked before {@link #moveNext()} method is invoked.
     */
    void init();
    
    /**
     * Whether this <code>Executor</code> instance has been initialized. In other word, 
     * if the {@link #init()} method has been invoked or not.
     * @return true, if this instance has been initialized, or false.
     */
    boolean isInited();
    
}
