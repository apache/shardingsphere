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
import io.shardingsphere.core.rule.DataSourceParameter;
import io.shardingsphere.transaction.manager.ShardingTransactionManager;

import javax.sql.DataSource;
import javax.sql.XADataSource;
import javax.transaction.TransactionManager;

/**
 * XA transaction manager.
 *
 * @author zhangliang
 * @author zhaojun
 */
public interface XATransactionManager extends ShardingTransactionManager<XATransactionEvent> {
    
    /**
     * destroy the transaction manager and could be helpful with shutdown gracefully.
     */
    void destroy();
    
    /**
     * Get specific {@link XADataSource} and enroll it with a JTA.
     *
     * @param xaDataSource XA data source
     * @param dataSourceName data source name
     * @param dataSourceParameter data source parameter
     * @return XA data source
     */
    DataSource wrapDataSource(XADataSource xaDataSource, String dataSourceName, DataSourceParameter dataSourceParameter);
    
    /**
     * Get transaction manager for vendor provided.
     *
     * @return transaction manager
     */
    TransactionManager getUnderlyingTransactionManager();
}
