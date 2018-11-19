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

import io.shardingsphere.core.parsing.antlr.extractor.statement.handler.fillor.CollectionHandlerResultFillor;
import io.shardingsphere.core.parsing.antlr.extractor.statement.handler.fillor.ColumnDefinitionHandlerResultFillor;
import io.shardingsphere.core.parsing.antlr.extractor.statement.handler.fillor.DropColumnHandlerResultFillor;
import io.shardingsphere.core.parsing.antlr.extractor.statement.handler.fillor.DropPrimaryKeyHandlerResultFillor;
import io.shardingsphere.core.parsing.antlr.extractor.statement.handler.fillor.HandlerResultFillor;
import io.shardingsphere.core.parsing.antlr.extractor.statement.handler.fillor.IndexHandlerResultFillor;
import io.shardingsphere.core.parsing.antlr.extractor.statement.handler.fillor.PrimaryKeyHandlerResultFillor;
import io.shardingsphere.core.parsing.antlr.extractor.statement.handler.fillor.TableHandlerResultFillor;
import io.shardingsphere.core.parsing.antlr.extractor.statement.handler.fillor.TableJoinHandlerResultFillor;
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
    private static final Map<Class<?>,HandlerResultFillor> fillors = new HashMap<>();
    
    static{
        registry(DropColumnExtractResult.class, new DropColumnHandlerResultFillor());
        registry(PrimaryKeyExtractResult.class, new PrimaryKeyHandlerResultFillor());
        registry(DropPrimaryKeyExtractResult.class, new DropPrimaryKeyHandlerResultFillor());
        registry(TableExtractResult.class, new TableHandlerResultFillor());
        registry(ColumnDefinitionExtractResult.class, new ColumnDefinitionHandlerResultFillor());
        registry(TableJoinExtractResult.class, new TableJoinHandlerResultFillor());
        registry(IndexExtractResult.class, new IndexHandlerResultFillor());
        registry(Collection.class, new CollectionHandlerResultFillor());
    }
    
    public static void registry(Class<?> clazz, HandlerResultFillor fillor) {
        fillors.put(clazz, fillor);
    }
    
    public static HandlerResultFillor getFillor(Object object) {
        if(object instanceof Collection) {
            return fillors.get(Collection.class); 
        }
        return fillors.get(object.getClass());
    }
}
