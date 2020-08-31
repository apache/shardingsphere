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

package org.apache.shardingsphere.sql.parser.sql.common.predicate;

import com.google.common.base.Preconditions;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.sql.parser.api.ASTNode;
import org.apache.shardingsphere.sql.parser.sql.common.constant.LogicalOperator;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.predicate.AndPredicate;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.predicate.OrPredicateSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.predicate.PredicateSegment;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Optional;

/**
 * Predicate builder.
 */
@RequiredArgsConstructor
public final class PredicateBuilder {
    
    private final ASTNode left;
    
    private final ASTNode right;
    
    private final String operator;
    
    /**
     * Merge predicate.
     * 
     * @return Or predicate segment
     */
    public OrPredicateSegment mergePredicate() {
        Optional<LogicalOperator> logicalOperator = LogicalOperator.valueFrom(operator);
        Preconditions.checkState(logicalOperator.isPresent());
        return LogicalOperator.OR == logicalOperator.get() ? mergeOrPredicateSegment() : mergeAndPredicateSegment();
    }
    
    private OrPredicateSegment mergeOrPredicateSegment() {
        OrPredicateSegment result = new OrPredicateSegment();
        result.getAndPredicates().addAll(getAndPredicates(left));
        result.getAndPredicates().addAll(getAndPredicates(right));
        return result;
    }
    
    private OrPredicateSegment mergeAndPredicateSegment() {
        OrPredicateSegment result = new OrPredicateSegment();
        Collection<AndPredicate> leftPredicates = getAndPredicates(left);
        Collection<AndPredicate> rightPredicates = getAndPredicates(right);
        addAndPredicates(result, leftPredicates, rightPredicates);
        return result;
    }
    
    private void addAndPredicates(final OrPredicateSegment orPredicateSegment, final Collection<AndPredicate> leftPredicates, final Collection<AndPredicate> rightPredicates) {
        if (leftPredicates.isEmpty() && rightPredicates.isEmpty()) {
            return;
        }
        if (leftPredicates.isEmpty()) {
            orPredicateSegment.getAndPredicates().addAll(rightPredicates);
        }
        if (rightPredicates.isEmpty()) {
            orPredicateSegment.getAndPredicates().addAll(leftPredicates);
        }
        for (AndPredicate eachLeft : leftPredicates) {
            for (AndPredicate eachRight : rightPredicates) {
                orPredicateSegment.getAndPredicates().add(createAndPredicate(eachLeft, eachRight));
            }
        }
    }
    
    private Collection<AndPredicate> getAndPredicates(final ASTNode astNode) {
        if (astNode instanceof OrPredicateSegment) {
            return ((OrPredicateSegment) astNode).getAndPredicates();
        }
        if (astNode instanceof AndPredicate) {
            return Collections.singleton((AndPredicate) astNode);
        }
        if (astNode instanceof PredicateSegment) {
            AndPredicate andPredicate = new AndPredicate();
            andPredicate.getPredicates().add((PredicateSegment) astNode);
            return Collections.singleton(andPredicate);
        }
        return new LinkedList<>();
    }
    
    private AndPredicate createAndPredicate(final AndPredicate left, final AndPredicate right) {
        AndPredicate result = new AndPredicate();
        result.getPredicates().addAll(left.getPredicates());
        result.getPredicates().addAll(right.getPredicates());
        return result;
    }
}
