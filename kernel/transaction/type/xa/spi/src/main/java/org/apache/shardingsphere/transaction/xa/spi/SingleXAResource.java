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
    
    private final XAResource delegate;
    
    @Override
    public void commit(final Xid xid, final boolean onePhase) throws XAException {
        delegate.commit(xid, onePhase);
    }
    
    @Override
    public void end(final Xid xid, final int flags) throws XAException {
        delegate.end(xid, flags);
    }
    
    @Override
    public void forget(final Xid xid) throws XAException {
        delegate.forget(xid);
    }
    
    @Override
    public int getTransactionTimeout() throws XAException {
        return delegate.getTransactionTimeout();
    }
    
    @Override
    public boolean isSameRM(final XAResource xaResource) {
        SingleXAResource singleXAResource = (SingleXAResource) xaResource;
        return resourceName.equals(singleXAResource.resourceName);
    }
    
    @Override
    public int prepare(final Xid xid) throws XAException {
        return delegate.prepare(xid);
    }
    
    @Override
    public Xid[] recover(final int flags) throws XAException {
        return delegate.recover(flags);
    }
    
    @Override
    public void rollback(final Xid xid) throws XAException {
        delegate.rollback(xid);
    }
    
    @Override
    public boolean setTransactionTimeout(final int timeout) throws XAException {
        return delegate.setTransactionTimeout(timeout);
    }
    
    @Override
    public void start(final Xid xid, final int flags) throws XAException {
        delegate.start(xid, flags);
    }
}
