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

package org.apache.shardingsphere.data.pipeline.core.preparer.inventory.calculator.position.exact;

import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.inventory.query.Range;
import org.apache.shardingsphere.data.pipeline.core.ingest.position.type.pk.UniqueKeyIngestPosition;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Binary position handler.
 *
 * <p>Handles VARBINARY/BINARY unique key type, using Base64 encoding for text serialization.</p>
 */
public final class BinaryPositionHandler implements DataTypePositionHandler<byte[]> {
    
    @Override
    public byte[] readColumnValue(final ResultSet resultSet, final int columnIndex) throws SQLException {
        return resultSet.getBytes(columnIndex);
    }
    
    @Override
    public void setPreparedStatementValue(final PreparedStatement preparedStatement, final int parameterIndex, final byte[] value) throws SQLException {
        preparedStatement.setBytes(parameterIndex, value);
    }
    
    @Override
    public UniqueKeyIngestPosition<byte[]> createIngestPosition(final Range<byte[]> range) {
        return UniqueKeyIngestPosition.ofBinary(range);
    }
}
