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

package org.apache.shardingsphere.core.rewrite.token.generator;

import com.google.common.base.Optional;
import org.apache.shardingsphere.core.parse.sql.segment.dml.InsertValuesSegment;
import org.apache.shardingsphere.core.parse.sql.statement.SQLStatement;
import org.apache.shardingsphere.core.parse.sql.statement.dml.InsertStatement;
import org.apache.shardingsphere.core.parse.sql.token.impl.InsertValuesToken;
import org.apache.shardingsphere.core.rule.BaseRule;

import java.util.Collection;

/**
 * Insert values token generator.
 *
 * @author panjuan
 */
public final class InsertValuesTokenGenerator implements OptionalSQLTokenGenerator<BaseRule> {
    
    @Override
    public Optional<InsertValuesToken> generateSQLToken(final SQLStatement sqlStatement, final BaseRule baseRule) {
        Collection<InsertValuesSegment> insertValuesSegments = sqlStatement.findSQLSegments(InsertValuesSegment.class);
        if (!(sqlStatement instanceof InsertStatement) || insertValuesSegments.isEmpty()) {
            return Optional.absent();
        }
        return createInsertValuesToken(insertValuesSegments);
    }
    
    private Optional<InsertValuesToken> createInsertValuesToken(final Collection<InsertValuesSegment> insertValuesSegments) {
        int startIndex = insertValuesSegments.iterator().next().getStartIndex();
        int stopIndex = insertValuesSegments.iterator().next().getStopIndex();
        for (InsertValuesSegment each : insertValuesSegments) {
            startIndex = startIndex > each.getStartIndex() ? each.getStartIndex() : startIndex;
            stopIndex = stopIndex < each.getStopIndex() ? each.getStopIndex() : stopIndex;
        }
        return Optional.of(new InsertValuesToken(startIndex, stopIndex));
    }
}
