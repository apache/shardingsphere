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

package org.apache.shardingsphere.underlying.common.metadata.table.loader.impl;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.sql.parser.binder.metadata.table.TableMetaData;
import org.apache.shardingsphere.sql.parser.binder.metadata.table.TableMetas;
import org.apache.shardingsphere.sql.parser.binder.metadata.table.TableMetasLoader;
import org.apache.shardingsphere.underlying.common.metadata.table.loader.TableMetaDataLoader;
import org.apache.shardingsphere.underlying.common.rule.BaseRule;

import javax.sql.DataSource;
import java.sql.SQLException;

/**
 * Default table meta data loader.
 */
@RequiredArgsConstructor
public final class DefaultTableMetaDataLoader implements TableMetaDataLoader {
    
    private final DataSource dataSource;
    
    private final int maxConnectionsSizePerQuery;
    
    @Override
    public TableMetaData load(final String tableName, final BaseRule rule) throws SQLException {
        return TableMetasLoader.load(dataSource, tableName);
    }
    
    @Override
    public TableMetas loadAll(final BaseRule rule) throws SQLException {
        return TableMetasLoader.load(dataSource, maxConnectionsSizePerQuery);
    }
}
