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

package org.apache.shardingsphere.readwritesplitting.exception.actual;

import org.apache.shardingsphere.infra.exception.external.sql.sqlstate.XOpenSQLState;
import org.apache.shardingsphere.readwritesplitting.constant.ReadwriteSplittingDataSourceType;
import org.apache.shardingsphere.readwritesplitting.exception.ReadwriteSplittingRuleExceptionIdentifier;
import org.apache.shardingsphere.readwritesplitting.exception.ReadwriteSplittingSQLException;

/**
 * Readwrite-splitting actual data source not found exception.
 */
public final class ReadwriteSplittingActualDataSourceNotFoundException extends ReadwriteSplittingSQLException {
    
    private static final long serialVersionUID = 1284608200400804784L;
    
    public ReadwriteSplittingActualDataSourceNotFoundException(final ReadwriteSplittingDataSourceType dataSourceType,
                                                               final String dataSourceName, final ReadwriteSplittingRuleExceptionIdentifier exceptionIdentifier) {
        super(XOpenSQLState.NOT_FOUND, 3, "Can not find readwrite-splitting %s data source '%s' in %s.", dataSourceType, dataSourceName, exceptionIdentifier);
    }
}
