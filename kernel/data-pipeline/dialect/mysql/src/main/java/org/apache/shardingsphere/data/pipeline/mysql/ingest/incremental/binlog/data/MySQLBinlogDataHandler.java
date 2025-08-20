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

package org.apache.shardingsphere.data.pipeline.mysql.ingest.incremental.binlog.data;

import lombok.NoArgsConstructor;
import org.apache.shardingsphere.data.pipeline.core.metadata.model.PipelineColumnMetaData;
import org.apache.shardingsphere.data.pipeline.mysql.ingest.incremental.binlog.data.binary.MySQLBinlogBinaryStringHandler;
import org.apache.shardingsphere.data.pipeline.mysql.ingest.incremental.binlog.data.unsigned.MySQLBinlogUnsignedNumberHandlerEngine;
import org.apache.shardingsphere.database.protocol.mysql.packet.binlog.row.column.value.string.MySQLBinaryString;

import java.io.Serializable;
import java.util.Optional;

/**
 * MySQL binlog data handler.
 */
@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public final class MySQLBinlogDataHandler {
    
    /**
     * Handle data value.
     *
     * @param columnMetaData column meta data
     * @param value to be handled value
     * @return handled value
     */
    public static Serializable handle(final PipelineColumnMetaData columnMetaData, final Serializable value) {
        if (null == value) {
            return null;
        }
        if (value instanceof MySQLBinaryString) {
            return MySQLBinlogBinaryStringHandler.handle(columnMetaData, (MySQLBinaryString) value);
        }
        Optional<Serializable> result = MySQLBinlogUnsignedNumberHandlerEngine.handle(columnMetaData, value);
        return result.orElse(value);
    }
}
