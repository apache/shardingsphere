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

import io.shardingsphere.transaction.event.xa.XATransactionEvent;
import io.shardingsphere.core.rule.DataSourceParameter;
import io.shardingsphere.transaction.manager.ShardingTransactionManager;

import javax.sql.DataSource;
import javax.sql.XADataSource;

/**
 * XA transaction manager.
 *
 * @author zhangliang
 */
public interface XATransactionManager extends ShardingTransactionManager<XATransactionEvent> {
    /**
     * Wrap the specific {@link XADataSource} and enroll it with a JTA.
     *
     * @param dataSource the data source to wrap
     * @param dataSourceName the data source name
     * @param dataSourceParameter the data source parameter
     * @throws Exception if can not wrap the data source
     * @return the wrapped data source
     */
    DataSource wrapDataSource(XADataSource dataSource, String dataSourceName, DataSourceParameter dataSourceParameter) throws Exception;
}
