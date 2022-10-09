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

package org.apache.shardingsphere.infra.exception;

import org.apache.shardingsphere.infra.util.exception.external.sql.sqlstate.XOpenSQLState;

/**
 * Overall connection not enough exception.
 */
public final class OverallConnectionNotEnoughException extends ConnectionSQLException {
    
    private static final long serialVersionUID = -1297088138042287804L;
    
    public OverallConnectionNotEnoughException(final int desiredSize, final int actualSize) {
        super(XOpenSQLState.CONNECTION_EXCEPTION, 20, "Can not get %d connections one time, partition succeed connection(%d) have released. "
                + "Please consider increasing the `maxPoolSize` of the data sources or decreasing the `max-connections-size-per-query` in properties.", desiredSize, actualSize);
    }
}
