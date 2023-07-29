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

import com.google.common.base.Preconditions;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.dal.ShowCreateTableStatementContext;
import org.apache.shardingsphere.proxy.backend.hbase.context.HBaseContext;
import org.apache.shardingsphere.proxy.backend.hbase.executor.HBaseExecutor;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLShowCreateTableStatement;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Result set for HBase describe.
 */
public final class HBaseDescribeResultSet implements HBaseQueryResultSet {
    
    private Iterator<HTableDescriptor> iterator;
    
    /**
     * Init data.
     *
     * @param sqlStatementContext SQL statement context
     */
    @Override
    public void init(final SQLStatementContext sqlStatementContext) {
        ShowCreateTableStatementContext statementContext = (ShowCreateTableStatementContext) sqlStatementContext;
        String tableName = statementContext.getTablesContext().getTableNames().iterator().next();
        boolean isExists = HBaseExecutor.executeAdmin(HBaseContext.getInstance().getConnection(tableName), admin -> admin.tableExists(TableName.valueOf(tableName)));
        Preconditions.checkArgument(isExists, String.format("Table %s is not exists", tableName));
        HTableDescriptor hTableDescriptor = HBaseExecutor.executeAdmin(HBaseContext.getInstance().getConnection(tableName), admin -> admin.getTableDescriptor(TableName.valueOf(tableName)));
        List<HTableDescriptor> tables = Collections.singletonList(hTableDescriptor);
        iterator = tables.iterator();
    }
    
    /**
     * Get result set column names.
     *
     * @return result set column names
     */
    @Override
    public Collection<String> getColumnNames() {
        return Arrays.asList("Name", "TableAttributes", "FlushPolicyClassName", "MaxFileSize", "MemStoreFlushSize", "Priority", "RegionReplication", "RegionSplitPolicyClassName", "CustomizedValues");
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
        HTableDescriptor descriptor = iterator.next();
        return Arrays.asList(descriptor.getNameAsString(), descriptor.toStringTableAttributes(),
                descriptor.getFlushPolicyClassName(), descriptor.getMaxFileSize(),
                descriptor.getMemStoreFlushSize(), descriptor.getPriority(), descriptor.getRegionReplication(), descriptor.getRegionSplitPolicyClassName(),
                descriptor.toStringCustomizedValues());
    }
    
    /**
     * Get type.
     *
     * @return type name
     */
    @Override
    public String getType() {
        return MySQLShowCreateTableStatement.class.getCanonicalName();
    }
}
