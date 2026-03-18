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

package org.apache.shardingsphere.encrypt.checker.cryptographic;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.encrypt.rewrite.token.comparator.EncryptorComparator;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.exception.generic.UnsupportedSQLOperationException;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.bound.ColumnSegmentBoundInfo;

import java.util.Collection;

/**
 * Join conditions encryptor comparator.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class JoinConditionsEncryptorChecker {
    
    /**
     * Check whether same encryptor.
     *
     * @param joinConditions join conditions
     * @param encryptRule encrypt rule
     * @param scenario scenario
     */
    public static void checkIsSame(final Collection<BinaryOperationExpression> joinConditions, final EncryptRule encryptRule, final String scenario) {
        for (BinaryOperationExpression each : joinConditions) {
            if (!(each.getLeft() instanceof ColumnSegment) || !(each.getRight() instanceof ColumnSegment)) {
                continue;
            }
            ColumnSegmentBoundInfo leftColumnInfo = ((ColumnSegment) each.getLeft()).getColumnBoundInfo();
            ColumnSegmentBoundInfo rightColumnInfo = ((ColumnSegment) each.getRight()).getColumnBoundInfo();
            ShardingSpherePreconditions.checkState(EncryptorComparator.isSame(encryptRule, leftColumnInfo, rightColumnInfo),
                    () -> new UnsupportedSQLOperationException("Can not use different encryptor for " + leftColumnInfo + " and " + rightColumnInfo + " in " + scenario));
        }
    }
}
