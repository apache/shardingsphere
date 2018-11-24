/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.core.parsing.antlr.extractor.sql.segment.filler;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import io.shardingsphere.core.parsing.antlr.extractor.sql.segment.result.ColumnDefinitionExtractResult;
import io.shardingsphere.core.parsing.antlr.extractor.sql.segment.result.DropColumnExtractResult;
import io.shardingsphere.core.parsing.antlr.extractor.sql.segment.result.DropPrimaryKeyExtractResult;
import io.shardingsphere.core.parsing.antlr.extractor.sql.segment.result.GroupByExtractResult;
import io.shardingsphere.core.parsing.antlr.extractor.sql.segment.result.IndexExtractResult;
import io.shardingsphere.core.parsing.antlr.extractor.sql.segment.result.LimitExtractResult;
import io.shardingsphere.core.parsing.antlr.extractor.sql.segment.result.OrderByExtractResult;
import io.shardingsphere.core.parsing.antlr.extractor.sql.segment.result.PrimaryKeyExtractResult;
import io.shardingsphere.core.parsing.antlr.extractor.sql.segment.result.TableAndConditionExtractResult;
import io.shardingsphere.core.parsing.antlr.extractor.sql.segment.result.TableExtractResult;
import io.shardingsphere.core.parsing.antlr.extractor.sql.segment.result.TableJoinExtractResult;

/**
 * Handler result filler registry.
 *
 * @author duhongjun
 */
public final class HandlerResultFillerRegistry {
    
    private static final Map<Class<?>, HandlerResultFiller> FILLERS = new HashMap<>();
    
    static {
        registry(DropColumnExtractResult.class, new DropColumnHandlerResultFiller());
        registry(PrimaryKeyExtractResult.class, new PrimaryKeyHandlerResultFiller());
        registry(DropPrimaryKeyExtractResult.class, new DropPrimaryKeyHandlerResultFiller());
        registry(TableExtractResult.class, new TableHandlerResultFiller());
        registry(ColumnDefinitionExtractResult.class, new ColumnDefinitionHandlerResultFiller());
        registry(TableJoinExtractResult.class, new TableJoinHandlerResultFiller());
        registry(IndexExtractResult.class, new IndexHandlerResultFiller());
        registry(LimitExtractResult.class, new LimitHandlerResultFiller());
        registry(TableAndConditionExtractResult.class, new TableAndConditionHandlerResultFiller());
        registry(GroupByExtractResult.class, new GroupByHandlerResultFiller());
        registry(OrderByExtractResult.class, new OrderByHandlerResultFiller());
        registry(Collection.class, new CollectionHandlerResultFiller());
    }
    
    /**
     * Registry HandlerResultFiller.
     *
     * @param clazz class for HandlerResultFiller
     * @param filler handler result filler
     */
    public static void registry(final Class<?> clazz, final HandlerResultFiller filler) {
        FILLERS.put(clazz, filler);
    }
    
    /**
     * Get HandlerResultFiller by object instance.
     *
     * @param object object instance
     * @return HandlerResultFiller instance
     */
    public static HandlerResultFiller getFiller(final Object object) {
        if (object instanceof Collection) {
            return FILLERS.get(Collection.class);
        }
        return FILLERS.get(object.getClass());
    }
}
