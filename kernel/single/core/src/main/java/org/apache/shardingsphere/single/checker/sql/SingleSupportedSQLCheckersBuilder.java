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

package org.apache.shardingsphere.single.checker.sql;

import org.apache.shardingsphere.infra.annotation.HighFrequencyInvocation;
import org.apache.shardingsphere.infra.checker.SupportedSQLChecker;
import org.apache.shardingsphere.infra.checker.SupportedSQLCheckersBuilder;
import org.apache.shardingsphere.single.checker.sql.schema.SingleDropSchemaSupportedChecker;
import org.apache.shardingsphere.single.checker.sql.table.SingleDropTableSupportedChecker;
import org.apache.shardingsphere.single.constant.SingleOrder;
import org.apache.shardingsphere.single.rule.SingleRule;

import java.util.Arrays;
import java.util.Collection;

/**
 * Single SQL supported checker factory.
 */
public final class SingleSupportedSQLCheckersBuilder implements SupportedSQLCheckersBuilder<SingleRule> {
    
    private final Collection<SupportedSQLChecker<?, SingleRule>> supportedSQLCheckers = Arrays.asList(
            new SingleDropSchemaSupportedChecker(),
            new SingleDropTableSupportedChecker());
    
    @HighFrequencyInvocation
    @Override
    public Collection<SupportedSQLChecker<?, SingleRule>> getSupportedSQLCheckers() {
        return supportedSQLCheckers;
    }
    
    @Override
    public int getOrder() {
        return SingleOrder.ORDER;
    }
    
    @Override
    public Class<SingleRule> getTypeClass() {
        return SingleRule.class;
    }
}
