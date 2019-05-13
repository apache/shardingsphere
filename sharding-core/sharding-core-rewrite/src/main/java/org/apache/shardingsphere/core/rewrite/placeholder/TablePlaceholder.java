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

import com.google.common.base.Joiner;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.core.parse.antlr.constant.QuoteCharacter;
import org.apache.shardingsphere.core.route.type.RoutingUnit;

import java.util.Map;

/**
 * Table placeholder for rewrite.
 *
 * @author zhangliang
 * @author panjuan
 */
@RequiredArgsConstructor
@Getter
public final class TablePlaceholder implements ShardingPlaceholder, Alterable {
    
    private final String logicTableName;
    
    private final QuoteCharacter quoteCharacter;
    
    @Override
    public String toString(final RoutingUnit routingUnit, final Map<String, String> logicAndActualTables) {
        String actualTableName = logicAndActualTables.get(logicTableName);
        actualTableName = null == actualTableName ? logicTableName : actualTableName;
        return Joiner.on("").join(quoteCharacter.getStartDelimiter(), actualTableName, quoteCharacter.getEndDelimiter());
    }
}
