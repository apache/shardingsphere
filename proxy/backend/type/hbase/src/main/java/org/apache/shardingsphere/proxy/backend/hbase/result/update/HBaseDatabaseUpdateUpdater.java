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

package org.apache.shardingsphere.proxy.backend.hbase.result.update;

import org.apache.hadoop.hbase.client.Put;
import org.apache.shardingsphere.infra.executor.sql.execute.result.update.UpdateResult;
import org.apache.shardingsphere.proxy.backend.hbase.bean.HBaseOperation;
import org.apache.shardingsphere.proxy.backend.hbase.converter.HBaseUpdateOperationAdapter;
import org.apache.shardingsphere.proxy.backend.hbase.executor.HBaseExecutor;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLUpdateStatement;
import java.util.Collection;
import java.util.Collections;

/**
 * HBase database update updater.
 */
public final class HBaseDatabaseUpdateUpdater implements HBaseDatabaseUpdater {
    
    /**
     * Execute HBase operation.
     *
     * @param hbaseOperation HBase operation
     * @return affected rows
     */
    @Override
    public Collection<UpdateResult> executeUpdate(final HBaseOperation hbaseOperation) {
        if (hbaseOperation.getOperation() instanceof HBaseUpdateOperationAdapter) {
            HBaseExecutor.executeUpdate(hbaseOperation.getTableName(), table -> table.put(((HBaseUpdateOperationAdapter) hbaseOperation.getOperation()).getPuts()));
            return Collections.singletonList(new UpdateResult(((HBaseUpdateOperationAdapter) hbaseOperation.getOperation()).getPuts().size(), 0));
        } else {
            HBaseExecutor.executeUpdate(hbaseOperation.getTableName(), table -> table.put((Put) hbaseOperation.getOperation()));
            return Collections.singletonList(new UpdateResult(1, 0));
        }
    }
    
    @Override
    public String getType() {
        return MySQLUpdateStatement.class.getCanonicalName();
    }
}
