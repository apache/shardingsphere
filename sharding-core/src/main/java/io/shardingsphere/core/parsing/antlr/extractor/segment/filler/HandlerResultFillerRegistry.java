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

package io.shardingsphere.core.parsing.antlr.extractor.segment.filler;

import io.shardingsphere.core.parsing.antlr.sql.segment.AddPrimaryKeySegment;
import io.shardingsphere.core.parsing.antlr.sql.segment.ColumnDefinitionSegment;
import io.shardingsphere.core.parsing.antlr.sql.segment.DropColumnSegment;
import io.shardingsphere.core.parsing.antlr.sql.segment.DropPrimaryKeySegment;
import io.shardingsphere.core.parsing.antlr.sql.segment.IndexSegment;
import io.shardingsphere.core.parsing.antlr.sql.segment.TableJoinSegment;
import io.shardingsphere.core.parsing.antlr.sql.segment.TableSegment;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Handler result filler registry.
 *
 * @author duhongjun
 */
public final class HandlerResultFillerRegistry {
    
    private static final Map<Class<?>, HandlerResultFiller> FILLERS = new HashMap<>();
    
    static {
        registry(DropColumnSegment.class, new DropColumnHandlerResultFiller());
        registry(AddPrimaryKeySegment.class, new PrimaryKeyHandlerResultFiller());
        registry(DropPrimaryKeySegment.class, new DropPrimaryKeyHandlerResultFiller());
        registry(TableSegment.class, new TableHandlerResultFiller());
        registry(ColumnDefinitionSegment.class, new ColumnDefinitionHandlerResultFiller());
        registry(TableJoinSegment.class, new TableJoinHandlerResultFiller());
        registry(IndexSegment.class, new IndexHandlerResultFiller());
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
