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

package org.apache.shardingsphere.proxy.backend.hbase.converter;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dal.FlushStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.DeleteStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.UpdateStatementContext;
import org.apache.shardingsphere.proxy.backend.hbase.exception.HBaseOperationException;

/**
 * HBase database converter factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class HBaseDatabaseConverterFactory {
    
    /**
     * Create new instance of HBase database converter.
     *
     * @param sqlStatementContext sql statement context
     * @return instance of converter
     */
    public static HBaseDatabaseConverter newInstance(final SQLStatementContext<?> sqlStatementContext) {
        if (sqlStatementContext instanceof SelectStatementContext) {
            return new HBaseDatabaseSelectConverter(sqlStatementContext);
        } else if (sqlStatementContext instanceof InsertStatementContext) {
            return new HBaseDatabaseInsertConverter(sqlStatementContext);
        } else if (sqlStatementContext instanceof DeleteStatementContext) {
            return new HBaseDatabaseDeleteConverter(sqlStatementContext);
        } else if (sqlStatementContext instanceof UpdateStatementContext) {
            return new HBaseDatabaseUpdateConverter(sqlStatementContext);
        } else if (sqlStatementContext instanceof FlushStatementContext) {
            return new HBaseRegionReloadConverter(sqlStatementContext);
        } else {
            throw new HBaseOperationException("Can't found converter");
        }
    }
}
