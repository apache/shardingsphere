package org.apache.shardingsphere.infra.executor.exec;

import com.google.common.base.Preconditions;
import lombok.Getter;
import org.apache.shardingsphere.infra.executor.exec.meta.Row;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResultMetaData;

import java.util.List;

/**
 * Executor with multi input Executor instance.
 */
@Getter
public class MultiExecutor extends AbstractExecutor implements Executor {
    
    private int queryResultIdx = 0;
    
    private List<Executor> executors;
    
    public MultiExecutor(final ExecContext execContext, List<Executor> executors) {
        super(execContext);
        Preconditions.checkArgument(!executors.isEmpty());
        this.executors = executors;
    }
    
    @Override
    public boolean executeMove() {
        if(queryResultIdx >= executors.size()) {
            return false;
        }
        while(true) {
            Executor queryResult = executors.get(queryResultIdx);
            if (queryResult.moveNext()) {
                return true;
            }
            queryResultIdx++;
            if (queryResultIdx >= executors.size()) {
                return false;
            }
        }
    }
    
    @Override
    protected void executeInit() {
        executors.forEach(Executor::init);
    }
    
    @Override
    public QueryResultMetaData getMetaData() {
        return executors.get(0).getMetaData();
    }
    
    @Override
    public Row current() {
        if(queryResultIdx >= executors.size()) {
            return null;
        }
        return executors.get(queryResultIdx).current();
    }
}
