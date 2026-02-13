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

package org.apache.shardingsphere.sql.parser.engine.mysql.parser;

import java.nio.CharBuffer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CodePointBuffer;
import org.antlr.v4.runtime.CodePointCharStream;
import org.antlr.v4.runtime.misc.Interval;
import org.apache.shardingsphere.sql.parser.api.parser.SQLLexer;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementLexer;

/**
 * SQL lexer for MySQL.
 */
public final class MySQLLexer extends MySQLStatementLexer implements SQLLexer {
    
    public MySQLLexer(final CharStream input) {
        super(stripExecutableComment(input));
    }

    private static CharStream stripExecutableComment(final CharStream input) {
        String sql = input.getText(Interval.of(0, input.size() - 1));
        String trimmed = sql.trim();
        if (trimmed.endsWith(";")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1).trim();
        }
        if (!trimmed.startsWith("/*!") || !trimmed.endsWith("*/")) {
            return input;
        }
        String content = trimmed.substring(3, trimmed.length() - 2);
        int index = 0;
        while (index < content.length() && content.charAt(index) >= '0' && content.charAt(index) <= '9') {
            index++;
        }
        String innerSQL = content.substring(index).trim();
        if (innerSQL.isEmpty()) {
            return input;
        }
        return CodePointCharStream.fromBuffer(CodePointBuffer.withChars(CharBuffer.wrap(innerSQL.toCharArray())));
    }
}
