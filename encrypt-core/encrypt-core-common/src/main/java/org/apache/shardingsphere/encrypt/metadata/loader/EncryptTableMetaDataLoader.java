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

package org.apache.shardingsphere.encrypt.metadata.loader;

import org.apache.shardingsphere.encrypt.metadata.decorator.EncryptTableMetaDataDecorator;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.sql.parser.binder.metadata.table.TableMetaData;
import org.apache.shardingsphere.sql.parser.binder.metadata.table.TableMetas;
import org.apache.shardingsphere.sql.parser.binder.metadata.table.TableMetasLoader;
import org.apache.shardingsphere.underlying.common.metadata.table.loader.TableMetaDataLoader;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Table meta data loader for encrypt.
 */
public final class EncryptTableMetaDataLoader implements TableMetaDataLoader<EncryptRule> {
    
    private final DataSource dataSource;
    
    private final int maxConnectionsSizePerQuery;
    
    private final EncryptTableMetaDataDecorator encryptTableMetaDataDecorator;
    
    public EncryptTableMetaDataLoader(final DataSource dataSource, final int maxConnectionsSizePerQuery) {
        this.dataSource = dataSource;
        this.maxConnectionsSizePerQuery = maxConnectionsSizePerQuery;
        encryptTableMetaDataDecorator = new EncryptTableMetaDataDecorator();
    }
    
    @Override
    public TableMetaData load(final String tableName, final EncryptRule encryptRule) throws SQLException {
        return encryptTableMetaDataDecorator.decorate(TableMetasLoader.load(dataSource, tableName), tableName, encryptRule);
    }
    
    @Override
    public TableMetas loadAll(final EncryptRule encryptRule) throws SQLException {
        EncryptTableMetaDataDecorator encryptTableMetaDataDecorator = new EncryptTableMetaDataDecorator();
        TableMetas tableMetas = TableMetasLoader.load(dataSource, maxConnectionsSizePerQuery);
        Collection<String> allTableNames = tableMetas.getAllTableNames();
        Map<String, TableMetaData> result = new HashMap<>(allTableNames.size(), 1);
        for (String each : allTableNames) {
            result.put(each, encryptTableMetaDataDecorator.decorate(tableMetas.get(each), each, encryptRule));
        }
        return new TableMetas(result);
    }
}
