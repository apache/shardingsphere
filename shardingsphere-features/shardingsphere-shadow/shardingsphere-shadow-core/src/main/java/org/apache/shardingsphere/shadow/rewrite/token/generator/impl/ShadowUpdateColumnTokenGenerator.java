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

package org.apache.shardingsphere.shadow.rewrite.token.generator.impl;

import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.UpdateStatementContext;
import org.apache.shardingsphere.infra.rewrite.sql.token.generator.CollectionSQLTokenGenerator;
import org.apache.shardingsphere.infra.rewrite.sql.token.pojo.SQLToken;
import org.apache.shardingsphere.infra.rewrite.sql.token.pojo.generic.RemoveToken;
import org.apache.shardingsphere.shadow.rewrite.token.generator.BaseShadowSQLTokenGenerator;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.assignment.AssignmentSegment;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * Remove update column token generator for shadow.
 */
public final class ShadowUpdateColumnTokenGenerator extends BaseShadowSQLTokenGenerator implements CollectionSQLTokenGenerator<UpdateStatementContext> {
    
    @Override
    protected boolean isGenerateSQLTokenForShadow(final SQLStatementContext sqlStatementContext) {
        return sqlStatementContext instanceof UpdateStatementContext && isContainShadowColumn(((UpdateStatementContext) sqlStatementContext).getSqlStatement().getSetAssignment().getAssignments());
    }
    
    private boolean isContainShadowColumn(final Collection<AssignmentSegment> assignments) {
        return assignments.stream().anyMatch(each -> each.getColumns().get(0).getIdentifier().getValue().equals(getShadowColumn()));
    }
    
    @Override
    public Collection<? extends SQLToken> generateSQLTokens(final UpdateStatementContext sqlStatementContext) {
        return generateRemoveTokenForShadow(sqlStatementContext.getSqlStatement().getSetAssignment().getAssignments());
    }
    
    private Collection<RemoveToken> generateRemoveTokenForShadow(final Collection<AssignmentSegment> assignments) {
        LinkedList<RemoveToken> removeTokens = new LinkedList<>();
        int index = 0;
        int previousElementStopIndex = 0;
        Iterator<AssignmentSegment> iterator = assignments.iterator();
        while (iterator.hasNext()) {
            AssignmentSegment each = iterator.next();
            if (getShadowColumn().equals(each.getColumns().get(0).getIdentifier().getValue())) {
                removeTokens.add(isLastElement(index, assignments.size()) ? new RemoveToken(previousElementStopIndex + 1, each.getStopIndex())
                        : new RemoveToken(each.getStartIndex(), iterator.next().getStartIndex() - 1));
            }
            previousElementStopIndex = each.getValue().getStopIndex();
            index++;
        }
        return removeTokens;
    }
    
    private boolean isLastElement(final int index, final int size) {
        return size - 1 == index;
    }
}
