/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingjdbc.transaction.bed;

import io.shardingjdbc.transaction.config.AsyncSoftTransactionZookeeperConfiguration;
import io.shardingjdbc.transaction.config.AsyncSoftTransactionJobConfiguration;
import lombok.Getter;
import lombok.Setter;

import javax.sql.DataSource;
import java.util.Map;

/**
 * Best efforts delivery configuration.
 *
 * @author caohao
 */
@Getter
@Setter
public class BestEffortsDeliveryConfiguration {
    
    /**
     * Data source for transaction manager.
     */
    private Map<String, DataSource> targetDataSource;
    
    /**
     * Transaction log data source.
     */
    private Map<String, DataSource> transactionLogDataSource;
    
    /**
     * Zookeeper configuration for B.A.S.E transaction.
     */
    private AsyncSoftTransactionZookeeperConfiguration zkConfig;
    
    /**
     * Asynchronized B.A.S.E transaction job configuration.
     */
    private AsyncSoftTransactionJobConfiguration jobConfig;
    
    public DataSource getTargetDataSource(final String dataSourceName) {
        return targetDataSource.get(dataSourceName);
    }
    
    public DataSource getDefaultTransactionLogDataSource() {
        return transactionLogDataSource.values().iterator().next();
    }
}
