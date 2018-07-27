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

package io.shardingsphere.transaction.xa;

import com.atomikos.icatch.jta.UserTransactionManager;

/**
 * Hold singleton atomikos userTransaction.
 *
 * @author zhaojun
 */
public class AtomikosUserTransaction {
    
    private static volatile UserTransactionManager transactionManager;
    
    /**
     * Get singleton {@code UserTransactionManager} lazily.
     *
     * @return {@code UserTransactionManager}
     */
    public static UserTransactionManager getInstance() {
        if (null == transactionManager) {
            synchronized (AtomikosUserTransaction.class) {
                if (null == transactionManager) {
                    transactionManager = new UserTransactionManager();
                }
            }
        }
        return transactionManager;
    }
}
