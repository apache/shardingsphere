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

package org.apache.shardingsphere.encrypt.checker.sql;

import org.apache.shardingsphere.encrypt.checker.sql.combine.EncryptCombineClauseSupportedChecker;
import org.apache.shardingsphere.encrypt.checker.sql.insert.EncryptInsertSelectSupportedChecker;
import org.apache.shardingsphere.encrypt.checker.sql.orderby.EncryptOrderByItemSupportedChecker;
import org.apache.shardingsphere.encrypt.checker.sql.predicate.EncryptPredicateColumnSupportedChecker;
import org.apache.shardingsphere.encrypt.checker.sql.projection.EncryptInsertSelectProjectionSupportedChecker;
import org.apache.shardingsphere.encrypt.checker.sql.projection.EncryptSelectProjectionSupportedChecker;
import org.apache.shardingsphere.encrypt.checker.sql.with.EncryptWithClauseSupportedChecker;
import org.apache.shardingsphere.encrypt.constant.EncryptOrder;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.infra.annotation.HighFrequencyInvocation;
import org.apache.shardingsphere.infra.checker.SupportedSQLChecker;
import org.apache.shardingsphere.infra.checker.SupportedSQLCheckersBuilder;

import java.util.Arrays;
import java.util.Collection;

/**
 * Encrypt SQL supported checker factory.
 */
public final class EncryptSupportedSQLCheckersBuilder implements SupportedSQLCheckersBuilder<EncryptRule> {
    
    private final Collection<SupportedSQLChecker<?, EncryptRule>> supportedSQLCheckers = Arrays.asList(
            new EncryptSelectProjectionSupportedChecker(),
            new EncryptInsertSelectProjectionSupportedChecker(),
            new EncryptPredicateColumnSupportedChecker(),
            new EncryptOrderByItemSupportedChecker(),
            new EncryptWithClauseSupportedChecker(),
            new EncryptCombineClauseSupportedChecker(),
            new EncryptInsertSelectSupportedChecker());
    
    @HighFrequencyInvocation
    @Override
    public Collection<SupportedSQLChecker<?, EncryptRule>> getSupportedSQLCheckers() {
        return supportedSQLCheckers;
    }
    
    @Override
    public int getOrder() {
        return EncryptOrder.ORDER;
    }
    
    @Override
    public Class<EncryptRule> getTypeClass() {
        return EncryptRule.class;
    }
}
