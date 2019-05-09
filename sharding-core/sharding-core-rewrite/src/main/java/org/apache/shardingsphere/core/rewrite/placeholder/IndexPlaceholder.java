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

package org.apache.shardingsphere.core.rewrite.placeholder;

import com.google.common.base.Strings;
import lombok.Getter;
import org.apache.shardingsphere.core.parse.antlr.constant.QuoteCharacter;
import org.apache.shardingsphere.core.parse.util.SQLUtil;
import org.apache.shardingsphere.core.route.type.TableUnit;

import java.util.Map;

/**
 * Index placeholder for rewrite.
 *
 * @author zhangliang
 * @author panjuan
 */
@Getter
public final class IndexPlaceholder implements ShardingPlaceholder {
    
    private final String logicIndexName;
    
    private final String logicTableName;
    
    private final QuoteCharacter quoteCharacter;
    
    public IndexPlaceholder(final String logicIndexLiteral, final String logicTableName) {
        logicIndexName = SQLUtil.getExactlyValue(logicIndexLiteral);
        this.logicTableName = logicTableName;
        quoteCharacter = QuoteCharacter.getQuoteCharacter(logicIndexLiteral);
    }
    
    @Override
    public String toString() {
        return logicIndexName;
    }
    
    public String toString(final TableUnit tableUnit, final Map<String, String> logicAndActualTableMap) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(quoteCharacter.getStartDelimiter()).append(logicIndexName);
        String actualTableName = logicAndActualTableMap.get(logicTableName);
        if (!Strings.isNullOrEmpty(actualTableName)) {
            stringBuilder.append("_").append(actualTableName);
        }
        stringBuilder.append(quoteCharacter.getEndDelimiter());
        return stringBuilder.toString();
    }
}
