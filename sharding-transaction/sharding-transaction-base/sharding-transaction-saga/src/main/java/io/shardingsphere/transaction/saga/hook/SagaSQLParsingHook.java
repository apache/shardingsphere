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

package io.shardingsphere.transaction.saga.hook;

import io.shardingsphere.spi.parsing.ParsingHook;
import io.shardingsphere.transaction.saga.SagaTransaction;
import io.shardingsphere.transaction.saga.manager.SagaTransactionManager;

/**
 * Saga SQL parsing hook.
 *
 * @author yangyi
 */
public final class SagaSQLParsingHook implements ParsingHook {
    
    private final SagaTransaction sagaTransaction = SagaTransactionManager.getInstance().getTransaction();
    
    @Override
    public void start(final String sql) {
    
    }
    
    @Override
    public void finishSuccess() {
        if (null != sagaTransaction) {
            sagaTransaction.nextLogicSQL();
        }
    }
    
    @Override
    public void finishFailure(final Exception cause) {
    
    }
}
