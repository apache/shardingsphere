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


package io.shardingsphere.core.parsing.antlr.extractor.registry;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import io.shardingsphere.core.parsing.antlr.extractor.statement.handler.fillor.CollectionHandlerResultFiller;
import io.shardingsphere.core.parsing.antlr.extractor.statement.handler.fillor.ColumnDefinitionHandlerResultFiller;
import io.shardingsphere.core.parsing.antlr.extractor.statement.handler.fillor.DropColumnHandlerResultFiller;
import io.shardingsphere.core.parsing.antlr.extractor.statement.handler.fillor.DropPrimaryKeyHandlerResultFiller;
import io.shardingsphere.core.parsing.antlr.extractor.statement.handler.fillor.HandlerResultFiller;
import io.shardingsphere.core.parsing.antlr.extractor.statement.handler.fillor.IndexHandlerResultFiller;
import io.shardingsphere.core.parsing.antlr.extractor.statement.handler.fillor.PrimaryKeyHandlerResultFiller;
import io.shardingsphere.core.parsing.antlr.extractor.statement.handler.fillor.TableHandlerResultFiller;
import io.shardingsphere.core.parsing.antlr.extractor.statement.handler.fillor.TableJoinHandlerResultFiller;
import io.shardingsphere.core.parsing.antlr.extractor.statement.handler.result.ColumnDefinitionExtractResult;
import io.shardingsphere.core.parsing.antlr.extractor.statement.handler.result.DropColumnExtractResult;
import io.shardingsphere.core.parsing.antlr.extractor.statement.handler.result.DropPrimaryKeyExtractResult;
import io.shardingsphere.core.parsing.antlr.extractor.statement.handler.result.IndexExtractResult;
import io.shardingsphere.core.parsing.antlr.extractor.statement.handler.result.PrimaryKeyExtractResult;
import io.shardingsphere.core.parsing.antlr.extractor.statement.handler.result.TableExtractResult;
import io.shardingsphere.core.parsing.antlr.extractor.statement.handler.result.TableJoinExtractResult;

/**
 * Handler result fillor registry.
 * 
 * @author duhongjun
 */
public final class HandlerResultFillorRegistry {
    private static final Map<Class<?>,HandlerResultFiller> fillors = new HashMap<>();
    
    static{
        registry(DropColumnExtractResult.class, new DropColumnHandlerResultFiller());
        registry(PrimaryKeyExtractResult.class, new PrimaryKeyHandlerResultFiller());
        registry(DropPrimaryKeyExtractResult.class, new DropPrimaryKeyHandlerResultFiller());
        registry(TableExtractResult.class, new TableHandlerResultFiller());
        registry(ColumnDefinitionExtractResult.class, new ColumnDefinitionHandlerResultFiller());
        registry(TableJoinExtractResult.class, new TableJoinHandlerResultFiller());
        registry(IndexExtractResult.class, new IndexHandlerResultFiller());
        registry(Collection.class, new CollectionHandlerResultFiller());
    }
    
    public static void registry(Class<?> clazz, HandlerResultFiller fillor) {
        fillors.put(clazz, fillor);
    }
    
    public static HandlerResultFiller getFillor(Object object) {
        if(object instanceof Collection) {
            return fillors.get(Collection.class); 
        }
        return fillors.get(object.getClass());
    }
}
