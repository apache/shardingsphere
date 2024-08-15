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

package org.apache.shardingsphere.data.pipeline.mysql.ingest.dumper.type;

import lombok.NoArgsConstructor;
import org.apache.shardingsphere.data.pipeline.core.metadata.model.PipelineColumnMetaData;
import org.apache.shardingsphere.data.pipeline.mysql.ingest.dumper.type.binary.MySQLBinlogBinaryDataTypeHandler;
import org.apache.shardingsphere.data.pipeline.mysql.ingest.dumper.type.number.MySQLBinlogNumberDataTypeHandler;
import org.apache.shardingsphere.db.protocol.mysql.packet.binlog.row.column.value.string.MySQLBinaryString;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;

import java.io.Serializable;
import java.util.Optional;

/**
 * MySQL binlog data type handler.
 */
@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public final class MySQLBinlogDataTypeHandler {
    
    /**
     * Handle column value.
     *
     * @param columnMetaData column meta data
     * @param value column value
     * @return handled column value
     */
    public static Serializable handle(final PipelineColumnMetaData columnMetaData, final Serializable value) {
        if (value instanceof MySQLBinaryString) {
            return MySQLBinlogBinaryDataTypeHandler.handle(columnMetaData, value);
        }
        Optional<MySQLBinlogNumberDataTypeHandler> dataTypeHandler = TypedSPILoader.findService(MySQLBinlogNumberDataTypeHandler.class, columnMetaData.getDataTypeName());
        return dataTypeHandler.isPresent() ? dataTypeHandler.get().handle(value) : value;
    }
}
