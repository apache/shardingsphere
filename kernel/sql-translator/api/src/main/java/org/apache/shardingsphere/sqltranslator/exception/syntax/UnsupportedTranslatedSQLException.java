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

package org.apache.shardingsphere.sqltranslator.exception.syntax;

import org.apache.shardingsphere.infra.util.exception.external.sql.sqlstate.XOpenSQLState;
import org.apache.shardingsphere.sqltranslator.exception.SQLTranslationException;

/**
 * Unsupported translated SQL exception.
 */
public final class UnsupportedTranslatedSQLException extends SQLTranslationException {
    
    private static final long serialVersionUID = -1419778194546662319L;
    
    public UnsupportedTranslatedSQLException(final String sql) {
        super(XOpenSQLState.SYNTAX_ERROR, 41, "Translation error, SQL is: %s", sql);
    }
}
