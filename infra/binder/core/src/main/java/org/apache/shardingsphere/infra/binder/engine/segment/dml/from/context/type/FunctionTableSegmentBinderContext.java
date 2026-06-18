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

package org.apache.shardingsphere.infra.binder.engine.segment.dml.from.context.type;

import org.apache.shardingsphere.infra.binder.engine.segment.dml.from.context.TableSegmentBinderContext;
import org.apache.shardingsphere.sql.parser.statement.core.enums.TableSourceType;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionSegment;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

/**
 * Function table segment binder context.
 */
public final class FunctionTableSegmentBinderContext implements TableSegmentBinderContext {
    
    @Override
    public Optional<ProjectionSegment> findProjectionSegmentByColumnLabel(final String columnLabel) {
        return Optional.empty();
    }
    
    @Override
    public Collection<ProjectionSegment> getProjectionSegments() {
        return Collections.emptyList();
    }
    
    @Override
    public TableSourceType getTableSourceType() {
        return TableSourceType.TEMPORARY_TABLE;
    }
}
