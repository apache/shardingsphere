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

import org.apache.shardingsphere.data.pipeline.core.ingest.position.type.pk.type.IntegerPrimaryKeyIngestPosition;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Integer position handler.
 */
public final class IntegerPositionHandler implements DataTypePositionHandler<Long> {
    
    @Override
    public Long readColumnValue(final ResultSet resultSet, final int columnIndex) throws SQLException {
        return resultSet.getLong(columnIndex);
    }
    
    @Override
    public void setPreparedStatementValue(final PreparedStatement preparedStatement, final int parameterIndex, final Long value) throws SQLException {
        preparedStatement.setLong(parameterIndex, value);
    }
    
    @Override
    public IntegerPrimaryKeyIngestPosition createIngestPosition(final Long lowerValue, final Long upperValue) {
        return new IntegerPrimaryKeyIngestPosition(lowerValue, upperValue);
    }
}
