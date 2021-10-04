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

package org.apache.shardingsphere.transaction.xa.atomikos.manager;

import com.atomikos.datasource.xa.jdbc.JdbcTransactionalResource;
import org.apache.shardingsphere.transaction.xa.spi.SingleXAResource;

import javax.sql.XADataSource;
import javax.transaction.xa.XAResource;

/**
 * Atomikos XA recoverable resource.
 */
public final class AtomikosXARecoverableResource extends JdbcTransactionalResource {
    
    private final String resourceName;
    
    AtomikosXARecoverableResource(final String serverName, final XADataSource xaDataSource) {
        super(serverName, xaDataSource);
        resourceName = serverName;
    }
    
    @Override
    public boolean usesXAResource(final XAResource xaResource) {
        return resourceName.equals(((SingleXAResource) xaResource).getResourceName());
    }
}
