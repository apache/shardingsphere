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

package org.apache.shardingsphere.underlying.common.metadata.table.init.loader;

import org.apache.shardingsphere.underlying.common.metadata.table.TableMetaData;
import org.apache.shardingsphere.underlying.common.metadata.table.TableMetas;
import org.apache.shardingsphere.underlying.common.metadata.table.init.TableMetaDataInitializer;
import org.apache.shardingsphere.underlying.common.rule.BaseRule;

import java.sql.SQLException;

/**
 * Table meta data loader.
 *
 * @author zhangliang
 */
public interface TableMetaDataLoader<T extends BaseRule> extends TableMetaDataInitializer {
    
    /**
     * Load table meta data.
     *
     * @param tableName table name
     * @param rule rule
     * @return table meta data
     * @throws SQLException SQL exception
     */
    TableMetaData load(String tableName, T rule) throws SQLException;
    
    /**
     * Load all table metas.
     *
     * @param rule sharding rule
     * @return Table metas
     * @throws SQLException SQL exception
     */
    TableMetas loadAll(T rule) throws SQLException;
}
