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

package org.apache.shardingsphere.data.pipeline.cdc.util;

import com.google.common.base.Strings;
import com.google.protobuf.Any;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.response.DataRecordResult;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.response.DataRecordResult.Record;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.response.DataRecordResult.Record.DataChangeType;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.response.DataRecordResult.Record.MetaData;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.response.TableColumn;
import org.apache.shardingsphere.data.pipeline.core.constant.PipelineSQLOperationType;
import org.apache.shardingsphere.data.pipeline.core.ingest.record.Column;
import org.apache.shardingsphere.data.pipeline.core.ingest.record.DataRecord;

import java.util.LinkedList;
import java.util.List;

/**
 * Data record result convert utility class.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DataRecordResultConvertUtils {
    
    /**
     * Convert data record to record.
     *
     * @param database database
     * @param schema schema
     * @param dataRecord data record
     * @return record
     */
    public static Record convertDataRecordToRecord(final String database, final String schema, final DataRecord dataRecord) {
        List<TableColumn> before = new LinkedList<>();
        List<TableColumn> after = new LinkedList<>();
        for (Column column : dataRecord.getColumns()) {
            before.add(TableColumn.newBuilder().setName(column.getName()).setValue(Any.pack(ColumnValueConvertUtils.convertToProtobufMessage(column.getOldValue()))).build());
            after.add(TableColumn.newBuilder().setName(column.getName()).setValue(Any.pack(ColumnValueConvertUtils.convertToProtobufMessage(column.getValue()))).build());
        }
        MetaData metaData = MetaData.newBuilder().setDatabase(database).setSchema(Strings.nullToEmpty(schema)).setTable(dataRecord.getTableName()).build();
        return DataRecordResult.Record.newBuilder().setMetaData(metaData).addAllBefore(before).addAllAfter(after).setTransactionCommitMillis(dataRecord.getCommitTime())
                .setDataChangeType(getDataChangeType(dataRecord.getType())).build();
    }
    
    private static DataChangeType getDataChangeType(final PipelineSQLOperationType type) {
        switch (type) {
            case INSERT:
                return DataChangeType.INSERT;
            case UPDATE:
                return DataChangeType.UPDATE;
            case DELETE:
                return DataChangeType.DELETE;
            default:
                return DataChangeType.UNKNOWN;
        }
    }
}
