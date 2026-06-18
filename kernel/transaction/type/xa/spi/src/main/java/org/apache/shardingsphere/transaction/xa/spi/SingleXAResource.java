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

package org.apache.shardingsphere.transaction.xa.spi;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

/**
 * Single XA resource.
 */
@RequiredArgsConstructor
@Getter
public final class SingleXAResource implements XAResource {
    
    private final String resourceName;
    
    private final String uniqueName;
    
    private final XAResource delegate;
    
    public SingleXAResource(final String resourceName, final XAResource delegate) {
        this.resourceName = resourceName;
        uniqueName = "";
        this.delegate = delegate;
    }
    
    @Override
    public void commit(final Xid xid, final boolean onePhase) throws XAException {
        try {
            delegate.commit(xid, onePhase);
        } catch (final XAException ex) {
            throw mapXAException(ex);
        }
    }
    
    @Override
    public void end(final Xid xid, final int flags) throws XAException {
        try {
            delegate.end(xid, flags);
        } catch (final XAException ex) {
            throw mapXAException(ex);
        }
    }
    
    @Override
    public void forget(final Xid xid) throws XAException {
        try {
            delegate.forget(xid);
        } catch (final XAException ex) {
            throw mapXAException(ex);
        }
    }
    
    @Override
    public int getTransactionTimeout() throws XAException {
        return delegate.getTransactionTimeout();
    }
    
    @Override
    public boolean isSameRM(final XAResource xaResource) {
        SingleXAResource singleXAResource = (SingleXAResource) xaResource;
        return resourceName.equals(singleXAResource.resourceName) && uniqueName.equals(singleXAResource.uniqueName);
    }
    
    @Override
    public int prepare(final Xid xid) throws XAException {
        try {
            return delegate.prepare(xid);
        } catch (final XAException ex) {
            throw mapXAException(ex);
        }
    }
    
    @Override
    public Xid[] recover(final int flags) throws XAException {
        try {
            return delegate.recover(flags);
        } catch (final XAException ex) {
            throw mapXAException(ex);
        }
    }
    
    @Override
    public void rollback(final Xid xid) throws XAException {
        try {
            delegate.rollback(xid);
        } catch (final XAException ex) {
            throw mapXAException(ex);
        }
    }
    
    @Override
    public boolean setTransactionTimeout(final int timeout) throws XAException {
        return delegate.setTransactionTimeout(timeout);
    }
    
    @Override
    public void start(final Xid xid, final int flags) throws XAException {
        try {
            delegate.start(xid, flags);
        } catch (final XAException ex) {
            throw mapXAException(ex);
        }
    }
    
    private XAException mapXAException(final XAException exception) {
        return exception;
    }
}
