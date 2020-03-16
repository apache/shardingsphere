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

package org.apache.shardingsphere.underlying.common.metadata.decorator;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.binder.metadata.schema.SchemaMetaData;
import org.apache.shardingsphere.sql.parser.binder.metadata.table.TableMetaData;
import org.apache.shardingsphere.underlying.common.rule.BaseRule;

import java.util.HashMap;
import java.util.Map;

/**
 * Schema meta data decorator.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SchemaMetaDataDecorator {
    
    /**
     * Decorate schema meta data.
     *
     * @param schemaMetaData schema meta data
     * @param rule rule
     * @param tableMetaDataDecorator table meta data decorator
     * @param <T> type of rule
     * @return decorated schema meta data
     */
    public static <T extends BaseRule> SchemaMetaData decorate(final SchemaMetaData schemaMetaData, final T rule, final TableMetaDataDecorator<T> tableMetaDataDecorator) {
        Map<String, TableMetaData> result = new HashMap<>(schemaMetaData.getAllTableNames().size(), 1);
        for (String each : schemaMetaData.getAllTableNames()) {
            result.put(each, tableMetaDataDecorator.decorate(schemaMetaData.get(each), each, rule));
        }
        return new SchemaMetaData(result);
    }
}
