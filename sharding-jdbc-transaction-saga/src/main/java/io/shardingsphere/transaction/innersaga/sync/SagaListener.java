package io.shardingsphere.transaction.innersaga.sync;

import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;
import io.shardingsphere.core.executor.event.DMLExecutionEvent;
import io.shardingsphere.core.routing.SQLUnit;
import io.shardingsphere.transaction.innersaga.api.SagaSoftTransactionManager;
import io.shardingsphere.transaction.innersaga.api.config.SagaSoftTransactionConfiguration;
import io.shardingsphere.transaction.innersaga.mock.CancelSql;
import io.shardingsphere.transaction.innersaga.mock.SagaSubTransaction;
import io.shardingsphere.transaction.innersaga.mock.SagaTransaction;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * Saga soft transaction event listener
 *
 * @author yangyi
 */

@Slf4j
public class SagaListener {

    /**
     * Listen event.
     *
     * @param event dml execution event
     */
    @Subscribe
    @AllowConcurrentEvents
    public void listen(final DMLExecutionEvent event) {
        SagaTransaction sagaTransaction = SagaSoftTransactionManager.getCurrentTransaction().get().getSagaTransaction();
        SagaSoftTransactionConfiguration sagaSoftTransactionConfiguration = SagaSoftTransactionManager.getCurrentTransactionConfiguration().get();

        switch (event.getEventExecutionType()) {
            case BEFORE_EXECUTE:
                String confirm = getConfirm(event.getSqlUnit());
                SagaSubTransaction sagaSubTransaction = new SagaSubTransaction(event.getId(), event.getDataSource(),
                        confirm, CancelSql.getCancelSql(confirm));
                sagaTransaction.register(sagaSubTransaction);
                break;
            case EXECUTE_FAILURE:
                sagaTransaction.fail(event.getId());
                break;
            case EXECUTE_SUCCESS:
                sagaTransaction.success(event.getId());
                break;
            default:
                throw new UnsupportedOperationException(event.getEventExecutionType().toString());
        }
    }

    private String getConfirm(SQLUnit sqlUnit) {
        String sql = sqlUnit.getSql();
        if (sql.contains("?")) {
            List<List<Object>> list = sqlUnit.getParameterSets();
            for (List<Object> subList : list) {
                for (Object parameter : subList) {
                    sql = sql.replace("?", parameter.toString());
                }
            }
        }
        return sql;
    }
}
