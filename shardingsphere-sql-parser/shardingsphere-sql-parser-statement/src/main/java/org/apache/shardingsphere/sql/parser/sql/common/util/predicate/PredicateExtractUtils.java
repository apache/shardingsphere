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

package org.apache.shardingsphere.sql.parser.sql.common.util.predicate;

import com.google.common.base.Preconditions;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.predicate.PredicateSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.OwnerSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableSegment;

import java.util.Collection;
import java.util.LinkedList;

/**
 * Predicate extractor.
 */
@RequiredArgsConstructor
public final class PredicateExtractUtils {
    
    private final Collection<TableSegment> tables;
    
    private final PredicateSegment predicate;
    
    /**
     * Extract tables.
     * 
     * @return table segments
     */
    public Collection<SimpleTableSegment> extractTables() {
        Collection<SimpleTableSegment> result = new LinkedList<>();
        if (isToGenerateTableTokenLeftValue()) {
            Preconditions.checkState(predicate.getColumn().getOwner().isPresent());
            OwnerSegment segment = predicate.getColumn().getOwner().get();
            result.add(new SimpleTableSegment(segment.getStartIndex(), segment.getStopIndex(), segment.getIdentifier()));
        }
        if (isToGenerateTableTokenForRightValue()) {
            Preconditions.checkState(((ColumnSegment) predicate.getRightValue()).getOwner().isPresent());
            OwnerSegment segment = ((ColumnSegment) predicate.getRightValue()).getOwner().get();
            result.add(new SimpleTableSegment(segment.getStartIndex(), segment.getStopIndex(), segment.getIdentifier()));
        }
        return result;
    }
    
    private boolean isToGenerateTableTokenLeftValue() {
        return predicate.getColumn().getOwner().isPresent() && isTable(predicate.getColumn().getOwner().get());
    }
    
    private boolean isToGenerateTableTokenForRightValue() {
        return predicate.getRightValue() instanceof ColumnSegment
                && ((ColumnSegment) predicate.getRightValue()).getOwner().isPresent() && isTable(((ColumnSegment) predicate.getRightValue()).getOwner().get());
    }
    
    private boolean isTable(final OwnerSegment owner) {
        for (TableSegment each : tables) {
            if (owner.getIdentifier().getValue().equals(each.getAlias().orElse(null))) {
                return false;
            }
        }
        return true;
    }
}
