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

package org.apache.shardingsphere.mcp.core.tool.handler.execute;

import java.util.Locale;

final class SQLStatementToken {
    
    private final String text;
    
    private final String upperText;
    
    private final boolean quotedIdentifier;
    
    SQLStatementToken(final String text, final boolean quotedIdentifier) {
        this.text = text;
        upperText = text.toUpperCase(Locale.ENGLISH);
        this.quotedIdentifier = quotedIdentifier;
    }
    
    String text() {
        return text;
    }
    
    String upperText() {
        return upperText;
    }
    
    boolean quotedIdentifier() {
        return quotedIdentifier;
    }
    
    boolean identifier() {
        return quotedIdentifier || !text.isEmpty() && isIdentifierStart(text.charAt(0));
    }
    
    private boolean isIdentifierStart(final char value) {
        return Character.isLetterOrDigit(value) || '_' == value || '$' == value;
    }
}
