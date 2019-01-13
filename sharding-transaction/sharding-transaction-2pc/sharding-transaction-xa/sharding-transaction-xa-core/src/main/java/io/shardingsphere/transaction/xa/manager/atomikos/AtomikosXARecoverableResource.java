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

package io.shardingsphere.transaction.xa.manager.atomikos;

import com.atomikos.datasource.xa.jdbc.JdbcTransactionalResource;
import io.shardingsphere.transaction.xa.jta.resource.ShardingXAResource;

import javax.sql.XADataSource;
import javax.transaction.xa.XAResource;

/**
 * Sharding recovery resource.
 *
 * @author zhaojun
 */
public final class AtomikosXARecoverableResource extends JdbcTransactionalResource {
    
    private final String resourceName;
    
    public AtomikosXARecoverableResource(final String serverName, final XADataSource xaDataSource) {
        super(serverName, xaDataSource);
        resourceName = serverName;
    }
    
    @Override
    public boolean usesXAResource(final XAResource xaResource) {
        return resourceName.equals(((ShardingXAResource) xaResource).getResourceName());
    }
}
