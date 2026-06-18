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

package org.apache.shardingsphere.sqltranslator.exception;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.exception.external.sql.sqlstate.XOpenSQLState;

/**
 * Unsupported translated database exception.
 */
public final class UnsupportedTranslatedDatabaseException extends SQLTranslationException {
    
    private static final long serialVersionUID = -8311552562051028033L;
    
    public UnsupportedTranslatedDatabaseException(final DatabaseType databaseType) {
        super(XOpenSQLState.FEATURE_NOT_SUPPORTED, 0, "Can not support database '%s' in SQL translation.", databaseType.getType());
    }
}
