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

package org.apache.shardingsphere.data.pipeline.mysql.ingest.incremental.binlog.data.binary;

import lombok.NoArgsConstructor;
import org.apache.shardingsphere.data.pipeline.core.metadata.model.PipelineColumnMetaData;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.connector.core.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.database.protocol.mysql.packet.binlog.row.column.value.string.MySQLBinaryString;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;

import java.io.Serializable;
import java.nio.charset.Charset;

/**
 * MySQL binlog binary string handler.
 */
@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public final class MySQLBinlogBinaryStringHandler {
    
    /**
     * Handle binary string value.
     *
     * @param columnMetaData column meta data
     * @param value to be handled value
     * @return handled value
     */
    public static Serializable handle(final PipelineColumnMetaData columnMetaData, final MySQLBinaryString value) {
        return new DatabaseTypeRegistry(TypedSPILoader.getService(DatabaseType.class, "MySQL")).getDialectDatabaseMetaData().getDataTypeOption().isBinaryDataType(columnMetaData.getDataType())
                ? value.getBytes()
                : new String(value.getBytes(), Charset.defaultCharset());
    }
}
