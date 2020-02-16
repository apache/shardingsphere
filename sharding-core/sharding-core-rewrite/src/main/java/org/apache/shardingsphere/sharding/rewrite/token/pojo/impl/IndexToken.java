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

package org.apache.shardingsphere.sharding.rewrite.token.pojo.impl;

import lombok.Getter;
import org.apache.shardingsphere.sharding.rewrite.token.pojo.LogicAndActualTablesAware;
import org.apache.shardingsphere.sql.parser.sql.value.identifier.IdentifierValue;
import org.apache.shardingsphere.underlying.rewrite.sql.token.pojo.SQLToken;
import org.apache.shardingsphere.underlying.rewrite.sql.token.pojo.Substitutable;

import java.util.Map;

/**
 * Index token.
 */
public final class IndexToken extends SQLToken implements Substitutable, LogicAndActualTablesAware {
    
    @Getter
    private final int stopIndex;
    
    private final IdentifierValue identifier;
    
    public IndexToken(final int startIndex, final int stopIndex, final IdentifierValue identifier) {
        super(startIndex);
        this.stopIndex = stopIndex;
        this.identifier = identifier;
    }
    
    @Override
    public String toString(final Map<String, String> logicAndActualTables) {
        StringBuilder result = new StringBuilder();
        result.append(identifier.getQuoteCharacter().getStartDelimiter()).append(identifier.getValue());
        if (!logicAndActualTables.isEmpty()) {
            result.append("_").append(logicAndActualTables.values().iterator().next());
        }
        result.append(identifier.getQuoteCharacter().getEndDelimiter());
        return result.toString();
    }
}
