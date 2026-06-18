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

package org.apache.shardingsphere.infra.binder.engine.segment.dml.prepare;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.binder.engine.statement.SQLStatementBinderContext;
import org.apache.shardingsphere.infra.binder.engine.statement.dml.DeleteStatementBinder;
import org.apache.shardingsphere.infra.binder.engine.statement.dml.InsertStatementBinder;
import org.apache.shardingsphere.infra.binder.engine.statement.dml.SelectStatementBinder;
import org.apache.shardingsphere.infra.binder.engine.statement.dml.UpdateStatementBinder;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.prepare.PrepareStatementQuerySegment;

/**
 * Prepare statement query segment binder.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PrepareStatementQuerySegmentBinder {
    
    /**
     * Bind prepare statement query segment.
     *
     * @param segment prepare statement query segment
     * @param binderContext SQL statement binder context
     * @return bound prepare statement query segment
     */
    public static PrepareStatementQuerySegment bind(final PrepareStatementQuerySegment segment, final SQLStatementBinderContext binderContext) {
        PrepareStatementQuerySegment result = new PrepareStatementQuerySegment(segment.getStartIndex(), segment.getStopIndex());
        segment.getSelect().ifPresent(optional -> result.setSelect(new SelectStatementBinder().bind(optional, binderContext)));
        segment.getInsert().ifPresent(optional -> result.setInsert(new InsertStatementBinder().bind(optional, binderContext)));
        segment.getUpdate().ifPresent(optional -> result.setUpdate(new UpdateStatementBinder().bind(optional, binderContext)));
        segment.getDelete().ifPresent(optional -> result.setDelete(new DeleteStatementBinder().bind(optional, binderContext)));
        return result;
    }
}
