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

package org.apache.shardingsphere.shadow.route.engine.judge.impl;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.shadow.condition.ShadowCondition;
import org.apache.shardingsphere.shadow.condition.ShadowConditionEngine;
import org.apache.shardingsphere.shadow.route.engine.judge.ShadowDataSourceJudgeEngine;
import org.apache.shardingsphere.shadow.route.engine.judge.util.ShadowValueJudgeUtil;
import org.apache.shardingsphere.shadow.rule.ShadowRule;
import org.apache.shardingsphere.sql.parser.binder.segment.insert.values.InsertValueContext;
import org.apache.shardingsphere.sql.parser.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.sql.parser.binder.statement.dml.InsertStatementContext;
import org.apache.shardingsphere.sql.parser.binder.type.WhereAvailable;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

/**
 * Simple shadow data source judge engine.
 */
@RequiredArgsConstructor
public final class SimpleShadowDataSourceJudgeEngine implements ShadowDataSourceJudgeEngine {
    
    private final ShadowRule shadowRule;
    
    private final SQLStatementContext<?> sqlStatementContext;
    
    @Override
    public boolean isShadow() {
        if (sqlStatementContext instanceof InsertStatementContext) {
            for (InsertValueContext each : ((InsertStatementContext) sqlStatementContext).getInsertValueContexts()) {
                if (judgeShadowSqlForInsert(each, (InsertStatementContext) sqlStatementContext)) {
                    return true;
                }
            }
            return false;
        }
        if (sqlStatementContext instanceof WhereAvailable) {
            Optional<ShadowCondition> shadowCondition = new ShadowConditionEngine(shadowRule).createShadowCondition(sqlStatementContext);
            if (!shadowCondition.isPresent()) {
                return false;
            }
            List<Object> values = shadowCondition.get().getValues(Collections.emptyList());
            return !values.isEmpty() && ShadowValueJudgeUtil.isShadowValue(values.get(0));
        }
        return false;
    }
    
    private boolean judgeShadowSqlForInsert(final InsertValueContext insertValueContext, final InsertStatementContext insertStatementContext) {
        Iterator<String> descendingColumnNames = insertStatementContext.getDescendingColumnNames();
        while (descendingColumnNames.hasNext()) {
            String columnName = descendingColumnNames.next();
            if (shadowRule.getColumn().equals(columnName)) {
                int columnIndex = insertStatementContext.getColumnNames().indexOf(columnName);
                return ShadowValueJudgeUtil.isShadowValue(insertValueContext.getValue(columnIndex));
            }
        }
        return false;
    }
}
