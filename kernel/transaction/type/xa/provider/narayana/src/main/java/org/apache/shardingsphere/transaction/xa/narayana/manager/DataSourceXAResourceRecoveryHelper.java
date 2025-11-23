/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.transaction.xa.narayana.manager;

import com.arjuna.ats.jta.recovery.XAResourceRecoveryHelper;
import com.google.common.base.Preconditions;
import lombok.extern.slf4j.Slf4j;

import javax.sql.XAConnection;
import javax.sql.XADataSource;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;
import java.sql.SQLException;

/**
 * XAResourceRecoveryHelper implementation which gets XIDs, which needs to be recovered, from the database.
 */
@Slf4j
public final class DataSourceXAResourceRecoveryHelper implements XAResourceRecoveryHelper, XAResource {
    
    private static final XAResource[] NO_XA_RESOURCES = {};
    
    private final XADataSource xaDataSource;
    
    private final String user;
    
    private final String password;
    
    private XAConnection xaConnection;
    
    private XAResource delegate;
    
    /**
     * Create a new {@link DataSourceXAResourceRecoveryHelper} instance.
     *
     * @param xaDataSource the XA data source
     */
    public DataSourceXAResourceRecoveryHelper(final XADataSource xaDataSource) {
        this(xaDataSource, null, null);
    }
    
    /**
     * Create a new {@link DataSourceXAResourceRecoveryHelper} instance.
     *
     * @param xaDataSource the XA data source
     * @param user the database user or {@code null}
     * @param password the database password or {@code null}
     */
    public DataSourceXAResourceRecoveryHelper(final XADataSource xaDataSource, final String user, final String password) {
        this.xaDataSource = xaDataSource;
        this.user = user;
        this.password = password;
    }
    
    @Override
    public boolean initialise(final String props) {
        return true;
    }
    
    @Override
    public XAResource[] getXAResources() {
        if (connect()) {
            return new XAResource[]{this};
        }
        return NO_XA_RESOURCES;
    }
    
    private boolean connect() {
        if (null == delegate) {
            try {
                xaConnection = getXaConnection();
                delegate = xaConnection.getXAResource();
            } catch (final SQLException ex) {
                log.warn("Failed to create connection", ex);
                return false;
            }
        }
        return true;
    }
    
    private XAConnection getXaConnection() throws SQLException {
        return null == user && null == password ? xaDataSource.getXAConnection() : xaDataSource.getXAConnection(user, password);
    }
    
    @Override
    public Xid[] recover(final int flag) throws XAException {
        try {
            return getDelegate().recover(flag);
        } finally {
            if (flag == TMENDRSCAN) {
                disconnect();
            }
        }
    }
    
    private void disconnect() {
        try {
            xaConnection.close();
        } catch (final SQLException ex) {
            log.warn("Failed to close connection", ex);
        } finally {
            xaConnection = null;
            delegate = null;
        }
    }
    
    @Override
    public void start(final Xid xid, final int flags) throws XAException {
        getDelegate().start(xid, flags);
    }
    
    @Override
    public void end(final Xid xid, final int flags) throws XAException {
        getDelegate().end(xid, flags);
    }
    
    @Override
    public int prepare(final Xid xid) throws XAException {
        return getDelegate().prepare(xid);
    }
    
    @Override
    public void commit(final Xid xid, final boolean onePhase) throws XAException {
        getDelegate().commit(xid, onePhase);
    }
    
    @Override
    public void rollback(final Xid xid) throws XAException {
        getDelegate().rollback(xid);
    }
    
    @Override
    public boolean isSameRM(final XAResource xaResource) throws XAException {
        return getDelegate().isSameRM(xaResource);
    }
    
    @Override
    public void forget(final Xid xid) throws XAException {
        getDelegate().forget(xid);
    }
    
    @Override
    public int getTransactionTimeout() throws XAException {
        return getDelegate().getTransactionTimeout();
    }
    
    @Override
    public boolean setTransactionTimeout(final int seconds) throws XAException {
        return getDelegate().setTransactionTimeout(seconds);
    }
    
    private XAResource getDelegate() {
        Preconditions.checkNotNull(delegate, "Connection has not been opened");
        return delegate;
    }
}
