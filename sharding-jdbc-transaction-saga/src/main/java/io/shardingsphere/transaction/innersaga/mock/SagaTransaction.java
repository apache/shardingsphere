/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.transaction.innersaga.mock;

/**
 * Actual saga transaction.
 * need service comb implement
 *
 * @author yangyi
 */

public interface SagaTransaction {
    
    /**
     * get transaction id of current id.
     *
     * @return transaction id String
     */
    String getTransactionId();
    
    /**
     * register one child transaction included confirm and cancel sql.
     *
     * @param sagaSubTransaction child transaction
     */
    void register(SagaSubTransaction sagaSubTransaction);
    
    /**
     * post success signal for one child transaction.
     *
     * @param subId the child transaction id which execute successfully
     */
    void success(String subId);
    
    /**
     * post fail signal for one child transaction.
     *
     * @param subId the child transaction id which execute unsuccessfully
     */
    void fail(String subId);
    
    /**
     * begin the transaction.
     */
    void begin();
    
    /**
     * Complete the transaction associated with the current thread. When this
     * method completes, the thread is no longer associated with a transaction.
     *
     * @throws Exception commit Exception
     */
    void commit() throws Exception;
    
    /**
     * Roll back the transaction associated with the current thread. When this
     * method completes, the thread is no longer associated with a transaction.
     *
     * @throws Exception rollback Exception
     */
    void rollback() throws Exception;
    
    /**
     * Obtain the status of the transaction associated with the current thread.
     *
     * @return The transaction status. If no transaction is associated with
     *     the current thread, this method returns the Status.NoTransaction value.
     */
    int getStatus();
    
}
