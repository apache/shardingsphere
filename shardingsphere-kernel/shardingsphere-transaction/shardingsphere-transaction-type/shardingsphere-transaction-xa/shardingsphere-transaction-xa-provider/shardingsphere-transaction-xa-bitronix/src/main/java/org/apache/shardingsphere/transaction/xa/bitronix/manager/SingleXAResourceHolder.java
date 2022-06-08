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

package org.apache.shardingsphere.transaction.xa.bitronix.manager;

import bitronix.tm.resource.common.AbstractXAResourceHolder;
import bitronix.tm.resource.common.ResourceBean;
import bitronix.tm.resource.common.XAResourceHolder;
import lombok.RequiredArgsConstructor;

import javax.transaction.xa.XAResource;
import java.util.Date;
import java.util.List;

/**
 * Single XA resource holder.
 */
@RequiredArgsConstructor
public final class SingleXAResourceHolder extends AbstractXAResourceHolder {
    
    private final XAResource xaResource;
    
    private final ResourceBean resourceBean;
    
    @Override
    public XAResource getXAResource() {
        return xaResource;
    }
    
    @Override
    public ResourceBean getResourceBean() {
        return resourceBean;
    }
    
    @Override
    public List<XAResourceHolder> getXAResourceHolders() {
        return null;
    }
    
    @Override
    public Object getConnectionHandle() {
        return null;
    }
    
    @Override
    public Date getLastReleaseDate() {
        return null;
    }
    
    @Override
    public void close() {
    }
}
