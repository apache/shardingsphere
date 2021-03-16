package org.apache.shardingsphere.infra.executor.exec;


import org.apache.calcite.linq4j.Enumerator;
import org.apache.shardingsphere.infra.executor.exec.meta.Row;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResultMetaData;

public interface Executor extends Enumerator<Row>, Iterable<Row> {
    
    QueryResultMetaData getMetaData();
    
    void init();
    
    boolean isInited();
    
}
