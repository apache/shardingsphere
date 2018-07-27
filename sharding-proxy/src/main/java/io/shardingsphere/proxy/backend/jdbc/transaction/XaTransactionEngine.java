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

import com.google.common.base.Optional;
import io.shardingsphere.core.constant.TCLType;
import io.shardingsphere.core.transaction.TransactionContext;
import io.shardingsphere.core.transaction.TransactionContextHolder;
import io.shardingsphere.core.transaction.event.XaTransactionEvent;
import io.shardingsphere.core.util.EventBusInstance;
import io.shardingsphere.proxy.config.RuleRegistry;

import javax.transaction.Status;

/**
 * Execute XA transaction intercept.
 *
 * @author zhaojun
 */
public final class XaTransactionEngine extends TransactionEngine {
    
    private final RuleRegistry ruleRegistry;
    
    public XaTransactionEngine(final String sql) {
        super(sql);
        ruleRegistry = RuleRegistry.getInstance();
    }
    
    @Override
    public XaTransactionEngine execute() throws Exception {
        Optional<TCLType> tclType = parseSQL();
        if (tclType.isPresent() && isInTransaction(tclType.get())) {
            TransactionContextHolder.set(new TransactionContext(ruleRegistry.getTransactionManager(), ruleRegistry.getTransactionType(), XaTransactionEvent.class));
            EventBusInstance.getInstance().post(new XaTransactionEvent(tclType.get(), getSql()));
            setSkipAccessBackend(true);
        }
        return this;
    }
    
    private boolean isInTransaction(final TCLType tclType) throws Exception {
        return TCLType.ROLLBACK != tclType || Status.STATUS_NO_TRANSACTION != ruleRegistry.getTransactionManager().getStatus();
    }
}
