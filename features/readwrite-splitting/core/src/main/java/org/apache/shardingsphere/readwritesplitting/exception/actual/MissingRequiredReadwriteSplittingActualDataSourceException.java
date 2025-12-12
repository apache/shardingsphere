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
 * Missing readwrite-splitting required actual data source exception.
 */
public final class MissingRequiredReadwriteSplittingActualDataSourceException extends ReadwriteSplittingSQLException {
    
    private static final long serialVersionUID = 3795576963060485964L;
    
    public MissingRequiredReadwriteSplittingActualDataSourceException(final ReadwriteSplittingDataSourceType dataSourceType, final ReadwriteSplittingRuleExceptionIdentifier exceptionIdentifier) {
        super(XOpenSQLState.NOT_FOUND, 2, "Readwrite-splitting %s data source is required in %s.", dataSourceType, exceptionIdentifier);
    }
}
