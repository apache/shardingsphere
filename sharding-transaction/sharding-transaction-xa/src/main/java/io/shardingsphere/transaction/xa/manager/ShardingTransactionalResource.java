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

package io.shardingsphere.transaction.xa.manager;

import com.atomikos.datasource.xa.jdbc.JdbcTransactionalResource;

import javax.sql.XADataSource;

/**
 * Sharding transactional recovery resource.
 *
 * @author zhaojun
 */
final class ShardingTransactionalResource extends JdbcTransactionalResource {
    
    /**
     * Constructs a new instance with a given name and XADataSource.
     *
     * @param serverName The unique name.
     * @param xaDataSource XA data source
     */
    ShardingTransactionalResource(final String serverName, final XADataSource xaDataSource) {
        super(serverName, xaDataSource);
        super.setAcceptAllXAResources(true);
    }
}
