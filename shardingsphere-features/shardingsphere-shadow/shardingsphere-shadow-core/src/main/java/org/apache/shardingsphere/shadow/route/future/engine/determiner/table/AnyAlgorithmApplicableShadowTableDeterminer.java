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

package org.apache.shardingsphere.shadow.route.future.engine.determiner.table;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.binder.statement.dml.InsertStatementContext;
import org.apache.shardingsphere.shadow.route.future.engine.determiner.ShadowAlgorithmDeterminer;
import org.apache.shardingsphere.shadow.route.future.engine.determiner.ShadowTableDeterminer;
import org.apache.shardingsphere.shadow.rule.ShadowRule;

import java.util.Collection;

/**
 * any algorithm applicable shadow table determiner.
 */
@RequiredArgsConstructor
public final class AnyAlgorithmApplicableShadowTableDeterminer implements ShadowTableDeterminer {
    
    private final Collection<ShadowAlgorithmDeterminer> shadowAlgorithmDeterminers;
    
    @Override
    public boolean isShadow(final InsertStatementContext insertStatementContext, final ShadowRule shadowRule, final String tableName) {
        Collection<String> shadowTableNames = shadowRule.getAllShadowTableNames();
        for (ShadowAlgorithmDeterminer each : shadowAlgorithmDeterminers) {
            if (each.isShadow(insertStatementContext, shadowTableNames, tableName)) {
                return true;
            }
        }
        return false;
    }
}
