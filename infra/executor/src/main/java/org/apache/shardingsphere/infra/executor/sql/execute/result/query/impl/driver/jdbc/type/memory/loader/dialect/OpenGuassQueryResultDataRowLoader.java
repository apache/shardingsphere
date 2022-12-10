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

package org.apache.shardingsphere.infra.executor.sql.execute.result.query.impl.driver.jdbc.type.memory.loader.dialect;

import org.apache.shardingsphere.infra.executor.sql.execute.result.query.impl.driver.jdbc.type.memory.loader.AbstractQueryResultDataRowLoader;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * OpenGauss query result data row loader.
 */
public final class OpenGuassQueryResultDataRowLoader extends AbstractQueryResultDataRowLoader {
    
    @Override
    protected Object getSmallintValue(final ResultSet resultSet, final int columnIndex) throws SQLException {
        return resultSet.getShort(columnIndex);
    }
    
    @Override
    protected Object getDateValue(final ResultSet resultSet, final int columnIndex) throws SQLException {
        return resultSet.getDate(columnIndex);
    }
    
    @Override
    public String getType() {
        return "openGauss";
    }
}
