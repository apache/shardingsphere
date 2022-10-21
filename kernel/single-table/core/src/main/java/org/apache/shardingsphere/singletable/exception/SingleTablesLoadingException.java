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

package org.apache.shardingsphere.singletable.exception;

import org.apache.shardingsphere.infra.exception.MetaDataSQLException;
import org.apache.shardingsphere.infra.util.exception.external.sql.sqlstate.XOpenSQLState;

import java.sql.SQLException;

/**
 * Single tables loading exception.
 */
public final class SingleTablesLoadingException extends MetaDataSQLException {
    
    private static final long serialVersionUID = 698261896187918188L;
    
    public SingleTablesLoadingException(final String databaseName, final String dataSourceName, final SQLException cause) {
        super(XOpenSQLState.GENERAL_ERROR, 22, "Can not load table with database name `%s` and data source name `%s`, reason is: %s", databaseName, dataSourceName, cause.getMessage());
    }
}
