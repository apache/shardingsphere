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

package io.shardingsphere.transaction.manager.xa;

import io.shardingsphere.core.event.transaction.xa.XATransactionEvent;
import lombok.extern.slf4j.Slf4j;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;
import java.sql.SQLException;

@Slf4j
public abstract class AbstractXATransactionManager implements XATransactionManager {
    public AbstractXATransactionManager() {
        try {
            init();
        } catch (Exception e) {
            log.warn("Can not initialize");
        }
    }

    public abstract UserTransaction getUserTransaction();

    @Override
    public void begin(XATransactionEvent transactionEvent) throws SQLException {
        try {
            getUserTransaction().begin();
        } catch (final SystemException | NotSupportedException ex) {
            throw new SQLException(ex);
        }
    }

    @Override
    public void commit(XATransactionEvent transactionEvent) throws SQLException {
        try {
            getUserTransaction().commit();
        } catch (final RollbackException | HeuristicMixedException | HeuristicRollbackException | SystemException ex) {
            throw new SQLException(ex);
        }
    }

    @Override
    public void rollback(XATransactionEvent transactionEvent) throws SQLException {
        try {
            getUserTransaction().rollback();

        } catch (final SystemException ex) {
            throw new SQLException(ex);
        }
    }

    @Override
    public int getStatus() throws SQLException {
        try {
            return getUserTransaction().getStatus();
        } catch (final SystemException ex) {
            throw new SQLException(ex);
        }
    }
}
