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
import org.apache.shardingsphere.core.parse.sql.segment.dml.predicate.OrPredicateSegment;
import org.apache.shardingsphere.core.parse.sql.statement.SQLStatement;
import org.apache.shardingsphere.core.parse.sql.token.impl.EncryptColumnToken;
import org.apache.shardingsphere.core.rule.ShardingRule;

import java.util.Collection;
import java.util.LinkedList;

/**
 * Encrypt column token generator.
 *
 * @author panjuan
 */
public final class EncryptColumnTokenGenerator implements CollectionSQLTokenGenerator<ShardingRule> {
    
    @Override
    public Collection<EncryptColumnToken> generateSQLTokens(final SQLStatement sqlStatement, final ShardingRule shardingRule) {
        Collection<EncryptColumnToken> result = new LinkedList<>();
        Optional<OrPredicateSegment> orPredicateSegments = sqlStatement.findSQLSegment(OrPredicateSegment.class);
        if (sqlStatement.getEncryptCondition().getOrConditions().isEmpty() || !orPredicateSegments.isPresent()) {
            return result;
        }
        return result;
    }
    
//    private Collection<EncryptColumnToken> createEncryptColumnTokens(final OrPredicateSegment segment, final AndCondition andCondition) {
//        for (int i = 0; i < andCondition.getConditions().size(); i++) {
//            
//        }
//    }
//    
//    private List<PredicateSegment> getPredicateSegments(final OrPredicateSegment segment) {
//        final List<PredicateSegment> result = new LinkedList<>();
//        for (AndPredicate andPredicate : segment.getAndPredicates()) {
//            result.addAll(Collections2.filter(andPredicate.getPredicates(), new Predicate<PredicateSegment>() {
//                
//                @Override
//                public boolean apply(final PredicateSegment input) {
//                    return !result.contains(input);
//                }
//            }));
//        }
//        return result;
//    }
}
