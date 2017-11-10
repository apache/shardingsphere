package io.shardingjdbc.core.executor.event;

import com.google.common.base.Optional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.sql.SQLException;
import java.util.List;

/**
 * SQL execution event.
 * 
 * @author gaohongtao
 */
@RequiredArgsConstructor
@Getter
public abstract class AbstractSQLExecutionEvent extends AbstractExecutionEvent {
    
    private final String dataSource;
    
    private final String sql;
    
    private final List<Object> parameters;
    
    public Optional<SQLException> getException() {
        Optional<? extends Exception> ex = super.getException();
        if (ex.isPresent()) {
            return Optional.of((SQLException) ex.get());
        }
        return Optional.absent();
    }
}
