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

import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;
import io.shardingsphere.core.transaction.event.XaTransactionEvent;
import lombok.AllArgsConstructor;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;

/**
 * XA Transaction Listener.
 *
 * @author zhaojun
 */
@AllArgsConstructor
public class XaTransactionListener {
    
    private AtomikosXaTransaction atomikosXaTransaction;
    
    /**
     * Listen event.
     *
     * @param xaTransactionEvent xaTransactionEvent XA TCL transaction event
     *
     * @throws SystemException @see javax.transaction.SystemException
     * @throws NotSupportedException @see javax.transaction.NotSupportedException
     * @throws HeuristicRollbackException @See javax.transaction.HeuristicRollbackException
     * @throws HeuristicMixedException @See javax.transaction.HeuristicMixedException
     * @throws RollbackException @See javax.transaction.RollbackException
     */
    @Subscribe
    @AllowConcurrentEvents
    public void listen(final XaTransactionEvent xaTransactionEvent) throws SystemException, NotSupportedException, HeuristicRollbackException, HeuristicMixedException, RollbackException {
        switch (xaTransactionEvent.getTclType()) {
            case BEGIN:
                atomikosXaTransaction.begin(xaTransactionEvent);
                break;
            case COMMIT:
                atomikosXaTransaction.commit(xaTransactionEvent);
                break;
            case ROLLBACK:
                atomikosXaTransaction.rollback(xaTransactionEvent);
                break;
            default:
        }
    }
}
