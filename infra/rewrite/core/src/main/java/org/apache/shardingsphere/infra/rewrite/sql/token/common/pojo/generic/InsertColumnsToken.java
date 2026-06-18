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

package org.apache.shardingsphere.infra.rewrite.sql.token.common.pojo.generic;

import org.apache.shardingsphere.database.connector.core.metadata.database.enums.QuoteCharacter;
import org.apache.shardingsphere.infra.rewrite.sql.token.common.pojo.Attachable;
import org.apache.shardingsphere.infra.rewrite.sql.token.common.pojo.SQLToken;

import java.util.List;
import java.util.StringJoiner;

/**
 * Insert columns token.
 */
public final class InsertColumnsToken extends SQLToken implements Attachable {
    
    private final List<String> columns;
    
    private final QuoteCharacter quoteCharacter;
    
    public InsertColumnsToken(final int startIndex, final List<String> columns) {
        super(startIndex);
        this.columns = columns;
        quoteCharacter = QuoteCharacter.NONE;
    }
    
    public InsertColumnsToken(final int startIndex, final List<String> columns, final QuoteCharacter quoteCharacter) {
        super(startIndex);
        this.columns = columns;
        this.quoteCharacter = quoteCharacter;
    }
    
    @Override
    public String toString() {
        if (columns.isEmpty()) {
            return "";
        }
        StringJoiner result = new StringJoiner(", ", ", ", "");
        columns.forEach(each -> result.add(quoteCharacter.wrap(each)));
        return result.toString();
    }
    
    @Override
    public int getStopIndex() {
        return getStartIndex();
    }
}
