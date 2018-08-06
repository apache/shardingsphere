package io.shardingsphere.transaction.innersaga.api;

import io.shardingsphere.transaction.innersaga.mock.SagaTransaction;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;


/**
 * Saga soft transaction
 *
 * @author yangyi
 */

@RequiredArgsConstructor
public class SagaSoftTransaction {

    @Getter
    private String transactionId;

    @NonNull
    @Getter
    private SagaTransaction sagaTransaction;

    public void begin() {
        transactionId = sagaTransaction.getTransactionId();
    }

    public void commit() throws Exception {
        sagaTransaction.commit();
    }

    public void rollback() throws Exception {
        sagaTransaction.rollback();
    }
}
