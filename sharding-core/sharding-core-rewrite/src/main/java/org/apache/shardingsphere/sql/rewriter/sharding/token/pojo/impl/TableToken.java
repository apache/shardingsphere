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

package org.apache.shardingsphere.sql.rewriter.sharding.token.pojo.impl;

import com.google.common.base.Joiner;
import lombok.Getter;
import org.apache.shardingsphere.sql.parser.core.constant.QuoteCharacter;
import org.apache.shardingsphere.sql.parser.util.SQLUtil;
import org.apache.shardingsphere.sql.rewriter.sharding.token.pojo.LogicAndActualTablesAware;
import org.apache.shardingsphere.sql.rewriter.sql.token.pojo.SQLToken;
import org.apache.shardingsphere.sql.rewriter.sql.token.pojo.Substitutable;

import java.util.Map;

/**
 * Table token.
 *
 * @author zhangliang
 * @author panjuan
 */
public final class TableToken extends SQLToken implements Substitutable, LogicAndActualTablesAware {
    
    @Getter
    private final int stopIndex;
    
    private final String tableName;
    
    private final QuoteCharacter quoteCharacter;
    
    public TableToken(final int startIndex, final int stopIndex, final String tableName, final QuoteCharacter quoteCharacter) {
        super(startIndex);
        this.tableName = SQLUtil.getExactlyValue(tableName);
        this.quoteCharacter = quoteCharacter;
        this.stopIndex = stopIndex;
    }
    
    @Override
    public String toString(final Map<String, String> logicAndActualTables) {
        String actualTableName = logicAndActualTables.get(tableName.toLowerCase());
        actualTableName = null == actualTableName ? tableName.toLowerCase() : actualTableName;
        return Joiner.on("").join(quoteCharacter.getStartDelimiter(), actualTableName, quoteCharacter.getEndDelimiter());
    }
}
