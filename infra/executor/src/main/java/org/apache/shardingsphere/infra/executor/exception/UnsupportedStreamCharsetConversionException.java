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

package org.apache.shardingsphere.infra.executor.exception;

import org.apache.shardingsphere.infra.exception.DataSQLException;
import org.apache.shardingsphere.infra.util.exception.external.sql.sqlstate.XOpenSQLState;

/**
 * Unsupported stream charset conversion exception.
 */
public final class UnsupportedStreamCharsetConversionException extends DataSQLException {
    
    private static final long serialVersionUID = 4577091201937095156L;
    
    public UnsupportedStreamCharsetConversionException(final String charsetType) {
        super(XOpenSQLState.INVALID_DATA_TYPE, 10, "Unsupported conversion stream charset `%s`.", charsetType);
    }
}
