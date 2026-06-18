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

package org.apache.shardingsphere.sql.parser.engine.exception;

import lombok.Getter;
import org.apache.shardingsphere.infra.exception.external.sql.sqlstate.XOpenSQLState;

/**
 * SQL parsing exception.
 */
@Getter
public final class SQLParsingException extends ParseSQLException {
    
    private static final long serialVersionUID = -6408790652103666096L;
    
    private final Object symbol;
    
    private final int line;
    
    public SQLParsingException(final String message) {
        this(message, "", 1);
    }
    
    public SQLParsingException(final String message, final Object symbol, final int line) {
        super(XOpenSQLState.SYNTAX_ERROR, 0, "You have an error in your SQL syntax: %s", message);
        this.symbol = symbol;
        this.line = line;
    }
}
