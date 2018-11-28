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

package io.shardingsphere.core.parsing.antlr.filler;

import com.google.common.base.Optional;
import io.shardingsphere.core.parsing.antlr.filler.engnie.ColumnDefinitionSegmentFiller;
import io.shardingsphere.core.parsing.antlr.filler.engnie.ConstraintDefinitionSegmentFiller;
import io.shardingsphere.core.parsing.antlr.filler.engnie.DropColumnSegmentFiller;
import io.shardingsphere.core.parsing.antlr.filler.engnie.DropPrimaryKeySegmentFiller;
import io.shardingsphere.core.parsing.antlr.filler.engnie.IndexSegmentFiller;
import io.shardingsphere.core.parsing.antlr.filler.engnie.ShowParamSegmentFiller;
import io.shardingsphere.core.parsing.antlr.filler.engnie.TableJoinSegmentFiller;
import io.shardingsphere.core.parsing.antlr.filler.engnie.TableSegmentFiller;
import io.shardingsphere.core.parsing.antlr.sql.segment.ColumnDefinitionSegment;
import io.shardingsphere.core.parsing.antlr.sql.segment.ConstraintDefinitionSegment;
import io.shardingsphere.core.parsing.antlr.sql.segment.DropColumnSegment;
import io.shardingsphere.core.parsing.antlr.sql.segment.DropPrimaryKeySegment;
import io.shardingsphere.core.parsing.antlr.sql.segment.IndexSegment;
import io.shardingsphere.core.parsing.antlr.sql.segment.ShowParamSegment;
import io.shardingsphere.core.parsing.antlr.sql.segment.SQLSegment;
import io.shardingsphere.core.parsing.antlr.sql.segment.TableJoinSegment;
import io.shardingsphere.core.parsing.antlr.sql.segment.TableSegment;

import java.util.HashMap;
import java.util.Map;

/**
 * SQL Segment filler registry.
 *
 * @author duhongjun
 */
public final class SQLSegmentFillerRegistry {
    
    private static final Map<Class<?>, SQLSegmentFiller> FILLERS = new HashMap<>();
    
    static {
        FILLERS.put(DropColumnSegment.class, new DropColumnSegmentFiller());
        FILLERS.put(ConstraintDefinitionSegment.class, new ConstraintDefinitionSegmentFiller());
        FILLERS.put(DropPrimaryKeySegment.class, new DropPrimaryKeySegmentFiller());
        FILLERS.put(TableSegment.class, new TableSegmentFiller());
        FILLERS.put(ColumnDefinitionSegment.class, new ColumnDefinitionSegmentFiller());
        FILLERS.put(TableJoinSegment.class, new TableJoinSegmentFiller());
        FILLERS.put(IndexSegment.class, new IndexSegmentFiller());
        FILLERS.put(ShowParamSegment.class, new ShowParamSegmentFiller());
    }
    
    /**
     * Find SQL segment filler.
     *
     * @param sqlSegment sql segment
     * @return instance of SQL segment filler
     */
    public static Optional<SQLSegmentFiller> findFiller(final SQLSegment sqlSegment) {
        return Optional.fromNullable(FILLERS.get(sqlSegment.getClass()));
    }
}
