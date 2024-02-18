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
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.dal.FlushStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.dml.DeleteStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.dml.UpdateStatementContext;
import org.apache.shardingsphere.proxy.backend.hbase.converter.type.HBaseDeleteOperationConverter;
import org.apache.shardingsphere.proxy.backend.hbase.converter.type.HBaseInsertOperationConverter;
import org.apache.shardingsphere.proxy.backend.hbase.converter.type.HBaseSelectOperationConverter;
import org.apache.shardingsphere.proxy.backend.hbase.converter.type.HBaseUpdateOperationConverter;
import org.apache.shardingsphere.proxy.backend.hbase.converter.type.HBaseRegionReloadOperationConverter;
import org.apache.shardingsphere.proxy.backend.hbase.exception.HBaseOperationException;

/**
 * HBase operation converter factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class HBaseOperationConverterFactory {
    
    /**
     * Create new instance of HBase operation converter.
     *
     * @param sqlStatementContext SQL statement context
     * @return instance of converter
     * @throws HBaseOperationException HBase operation exception
     */
    public static HBaseOperationConverter newInstance(final SQLStatementContext sqlStatementContext) {
        if (sqlStatementContext instanceof SelectStatementContext) {
            return new HBaseSelectOperationConverter(sqlStatementContext);
        }
        if (sqlStatementContext instanceof InsertStatementContext) {
            return new HBaseInsertOperationConverter(sqlStatementContext);
        }
        if (sqlStatementContext instanceof DeleteStatementContext) {
            return new HBaseDeleteOperationConverter(sqlStatementContext);
        }
        if (sqlStatementContext instanceof UpdateStatementContext) {
            return new HBaseUpdateOperationConverter(sqlStatementContext);
        }
        if (sqlStatementContext instanceof FlushStatementContext) {
            return new HBaseRegionReloadOperationConverter(sqlStatementContext);
        }
        throw new HBaseOperationException("Can not find HBase converter.");
    }
}
