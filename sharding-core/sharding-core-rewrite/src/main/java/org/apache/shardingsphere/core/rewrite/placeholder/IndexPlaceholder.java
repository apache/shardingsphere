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
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.core.parse.constant.QuoteCharacter;
import org.apache.shardingsphere.core.route.type.RoutingUnit;

import java.util.Map;

/**
 * Index placeholder for rewrite.
 *
 * @author zhangliang
 * @author panjuan
 */
@RequiredArgsConstructor
@Getter
public final class IndexPlaceholder implements ShardingPlaceholder, Alterable {
    
    private final String logicIndexName;
    
    private final String logicTableName;
    
    private final QuoteCharacter quoteCharacter;
    
    @Override
    public String toString(final RoutingUnit routingUnit, final Map<String, String> logicAndActualTables) {
        StringBuilder result = new StringBuilder();
        result.append(quoteCharacter.getStartDelimiter()).append(logicIndexName);
        String actualTableName = logicAndActualTables.get(logicTableName);
        if (!Strings.isNullOrEmpty(actualTableName)) {
            result.append("_").append(actualTableName);
        }
        result.append(quoteCharacter.getEndDelimiter());
        return result.toString();
    }
}
