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

package io.shardingsphere.proxy.backend.jdbc.transaction;

import io.shardingsphere.proxy.config.RuleRegistry;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Create transaction engine based on current transaction type.
 *
 * @author zhaojun
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TransactionEngineFactory {
    
    /**
     * Create transaction engine from SQL.
     * 
     * @param sql SQL
     * @return transaction engine
     */
    public static TransactionEngine create(final String sql) {
        switch (RuleRegistry.getInstance().getTransactionType()) {
            case XA:
                return new XaTransactionEngine(sql);
            default:
                return new DefaultTransactionEngine(sql);
        }
    }
}
