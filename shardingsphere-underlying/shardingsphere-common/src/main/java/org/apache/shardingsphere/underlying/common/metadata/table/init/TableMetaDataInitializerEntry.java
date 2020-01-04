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

package org.apache.shardingsphere.underlying.common.metadata.table.init;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.underlying.common.metadata.table.TableMetaData;
import org.apache.shardingsphere.underlying.common.metadata.table.TableMetas;
import org.apache.shardingsphere.underlying.common.metadata.table.init.decorator.TableMetaDataDecorator;
import org.apache.shardingsphere.underlying.common.metadata.table.init.loader.TableMetaDataLoader;
import org.apache.shardingsphere.underlying.common.rule.BaseRule;

import java.sql.SQLException;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Table meta data initializer entry.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
public final class TableMetaDataInitializerEntry {
    
    private final Map<BaseRule, TableMetaDataInitializer> initializes;
    
    /**
     * Initialize table meta data.
     *
     * @param tableName table name
     * @return table meta data
     * @throws SQLException SQL exception
     */
    public TableMetaData init(final String tableName) throws SQLException {
        return decorate(tableName, load(tableName));
    }
    
    @SuppressWarnings("unchecked")
    private TableMetaData load(final String tableName) throws SQLException {
        for (Entry<BaseRule, TableMetaDataInitializer> entry : initializes.entrySet()) {
            if (entry.getValue() instanceof TableMetaDataLoader) {
                return ((TableMetaDataLoader) entry.getValue()).load(tableName, entry.getKey());
            }
        }
        throw new IllegalStateException("Cannot find class `TableMetaDataLoader`");
    }
    
    @SuppressWarnings("unchecked")
    private TableMetaData decorate(final String tableName, final TableMetaData tableMetaData) {
        TableMetaData result = tableMetaData;
        for (Entry<BaseRule, TableMetaDataInitializer> entry : initializes.entrySet()) {
            if (entry.getValue() instanceof TableMetaDataDecorator) {
                result = ((TableMetaDataDecorator) entry.getValue()).decorate(result, tableName, entry.getKey());
            }
        }
        return result;
    }
    
    /**
     * Initialize all table meta data.
     *
     * @return table metas
     * @throws SQLException SQL exception
     */
    public TableMetas initAll() throws SQLException {
        return decorateAll(loadAll());
    }
    
    @SuppressWarnings("unchecked")
    private TableMetas loadAll() throws SQLException {
        for (Entry<BaseRule, TableMetaDataInitializer> entry : initializes.entrySet()) {
            if (entry.getValue() instanceof TableMetaDataLoader) {
                return ((TableMetaDataLoader) entry.getValue()).loadAll(entry.getKey());
            }
        }
        throw new IllegalStateException("Cannot find class `TableMetaDataLoader`");
    }
    
    @SuppressWarnings("unchecked")
    private TableMetas decorateAll(final TableMetas tableMetas) {
        TableMetas result = tableMetas;
        for (Entry<BaseRule, TableMetaDataInitializer> entry : initializes.entrySet()) {
            if (entry.getValue() instanceof TableMetaDataDecorator) {
                result = ((TableMetaDataDecorator) entry.getValue()).decorate(result, entry.getKey());
            }
        }
        return result;
    }
}
