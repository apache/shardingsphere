package io.shardingsphere.transaction.innersaga.mock;

/**
 * Actual saga transaction
 * need service comb implement
 *
 * @author yangyi
 */

public interface SagaTransaction {

    /**
     * get transaction id of current id
     *
     * @return transaction id String
     */
    String getTransactionId();

    /**
     * register one child transaction included confirm and cancel sql
     *
     * @param sagaSubTransaction child transaction
     */
    void register(SagaSubTransaction sagaSubTransaction);

    /**
     * post success signal for one child transaction
     *
     * @param subId the child transaction id which execute successfully
     */
    void success(String subId);

    /**
     * post fail signal for one child transaction
     * @param subId the child transaction id which execute unsuccessfully
     */
    void fail(String subId);

    void begin();

    void commit() throws Exception;

    void rollback() throws Exception;

    int getStatus();

}
