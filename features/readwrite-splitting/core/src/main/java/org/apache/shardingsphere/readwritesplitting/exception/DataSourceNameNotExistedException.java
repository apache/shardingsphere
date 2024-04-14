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

package org.apache.shardingsphere.readwritesplitting.exception;

import org.apache.shardingsphere.infra.exception.core.external.sql.sqlstate.XOpenSQLState;
import org.apache.shardingsphere.readwritesplitting.constant.ReadwriteSplittingDataSourceType;

/**
 * Data source name not existed exception.
 */
public final class DataSourceNameNotExistedException extends ReadwriteSplittingSQLException {
    
    private static final long serialVersionUID = 1284608200400804784L;
    
    public DataSourceNameNotExistedException(final ReadwriteSplittingDataSourceType dataSourceType, final String dataSourceName, final String databaseName) {
        super(XOpenSQLState.NOT_FOUND, 4, "%s data source name '%s' not in database '%s'.", dataSourceType, dataSourceName, databaseName);
    }
}
