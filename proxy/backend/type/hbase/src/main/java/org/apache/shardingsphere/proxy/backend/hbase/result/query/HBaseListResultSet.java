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

package org.apache.shardingsphere.proxy.backend.hbase.result.query;

import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.dal.ShowTablesStatementContext;
import org.apache.shardingsphere.proxy.backend.hbase.bean.HBaseCluster;
import org.apache.shardingsphere.proxy.backend.hbase.context.HBaseContext;
import org.apache.shardingsphere.proxy.backend.hbase.executor.HBaseExecutor;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLShowTablesStatement;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Result set for HBase.
 */
public final class HBaseListResultSet implements HBaseQueryResultSet {
    
    private Iterator<Entry<String, String>> iterator;
    
    /**
     * Init data.
     *
     * @param sqlStatementContext SQL statement context
     */
    @Override
    public void init(final SQLStatementContext sqlStatementContext) {
        ShowTablesStatementContext context = (ShowTablesStatementContext) sqlStatementContext;
        Map<String, String> result;
        if (context.getSqlStatement().getFromSchema().isPresent()) {
            String clusterName = context.getSqlStatement().getFromSchema().get().getSchema().getIdentifier().getValue();
            result = listTablesInHBaseByFromSchema(clusterName);
        } else {
            result = listTablesInHBase();
        }
        iterator = result.entrySet().iterator();
    }
    
    private Map<String, String> listTablesInHBaseByFromSchema(final String clusterName) {
        HTableDescriptor[] tables = HBaseExecutor.executeAdmin(HBaseContext.getInstance().getConnectionByClusterName(clusterName), Admin::listTables);
        Map<String, String> result = new HashMap<>(tables.length);
        for (HTableDescriptor tableDescriptor : tables) {
            result.put(tableDescriptor.getNameAsString(), clusterName);
        }
        return result;
    }
    
    private Map<String, String> listTablesInHBase() {
        Map<String, String> result = new HashMap<>(HBaseContext.getInstance().getTableConnectionMap().size(), 1F);
        for (Entry<String, HBaseCluster> entry : HBaseContext.getInstance().getTableConnectionMap().entrySet()) {
            result.put(entry.getKey(), entry.getValue().getClusterName());
        }
        return result;
    }
    
    /**
     * Get result set column names.
     *
     * @return result set column names
     */
    @Override
    public Collection<String> getColumnNames() {
        return Arrays.asList("hbase cluster name", "table name");
    }
    
    /**
     * Go to next data.
     *
     * @return true if next data exist
     */
    @Override
    public boolean next() {
        return iterator.hasNext();
    }
    
    /**
     * Get row data.
     *
     * @return row data
     */
    @Override
    public Collection<Object> getRowData() {
        Entry<String, String> entry = iterator.next();
        return Arrays.asList(entry.getValue(), entry.getKey());
    }
    
    /**
     * Get type.
     * 
     * @return type name
     */
    @Override
    public Class<MySQLShowTablesStatement> getType() {
        return MySQLShowTablesStatement.class;
    }
}
