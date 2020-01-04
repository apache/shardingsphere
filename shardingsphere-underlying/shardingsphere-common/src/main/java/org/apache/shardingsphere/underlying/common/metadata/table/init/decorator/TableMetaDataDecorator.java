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

package org.apache.shardingsphere.underlying.common.metadata.table.init.decorator;

import org.apache.shardingsphere.underlying.common.metadata.table.TableMetaData;
import org.apache.shardingsphere.underlying.common.metadata.table.TableMetas;
import org.apache.shardingsphere.underlying.common.metadata.table.init.TableMetaDataInitializer;
import org.apache.shardingsphere.underlying.common.rule.BaseRule;

/**
 * Table meta data decorator.
 *
 * @author zhangliang
 */
public interface TableMetaDataDecorator<T extends BaseRule> extends TableMetaDataInitializer {
    
    /**
     * Decorate table metas.
     *
     * @param tableMetas table metas
     * @param rule rule
     * @return decorated table metas
     */
    TableMetas decorate(TableMetas tableMetas, T rule);
    
    /**
     * Decorate table meta data.
     *
     * @param tableMetaData table meta data
     * @param tableName table name
     * @param rule rule
     * @return decorated table meta data
     */
    TableMetaData decorate(TableMetaData tableMetaData, String tableName, T rule);
}
