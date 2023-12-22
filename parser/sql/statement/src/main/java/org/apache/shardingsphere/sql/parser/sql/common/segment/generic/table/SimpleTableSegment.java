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

package org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.AliasSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.OwnerAvailable;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.OwnerSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.PivotSegment;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Optional;

/**
 * Simple table segment.
 */
@RequiredArgsConstructor
@Getter
public final class SimpleTableSegment implements TableSegment, OwnerAvailable {
    
    private final TableNameSegment tableName;
    
    @Setter
    private OwnerSegment owner;
    
    @Setter
    private AliasSegment alias;
    
    private final Collection<IndexHintSegment> indexHintSegments = new LinkedList<>();
    
    @Setter
    private PivotSegment pivot;
    
    @Setter
    private IdentifierValue dbLink;
    
    @Setter
    private IdentifierValue at;
    
    @Override
    public int getStartIndex() {
        if (null == owner) {
            return tableName.getStartIndex();
        }
        return owner.getOwner().isPresent() ? owner.getOwner().get().getStartIndex() : owner.getStartIndex();
    }
    
    @Override
    public int getStopIndex() {
        return null == alias ? tableName.getStopIndex() : alias.getStopIndex();
    }
    
    @Override
    public Optional<OwnerSegment> getOwner() {
        return Optional.ofNullable(owner);
    }
    
    public Optional<IdentifierValue> getDbLink() {
        return Optional.ofNullable(dbLink);
    }
    
    public Optional<IdentifierValue> getAt() {
        return Optional.ofNullable(at);
    }
    
    @Override
    public Optional<String> getAliasName() {
        return null == alias ? Optional.empty() : Optional.ofNullable(alias.getIdentifier().getValue());
    }
    
    @Override
    public Optional<IdentifierValue> getAlias() {
        return Optional.ofNullable(alias).map(AliasSegment::getIdentifier);
    }
    
    /**
     * Get alias segment.
     * 
     * @return alias segment
     */
    public Optional<AliasSegment> getAliasSegment() {
        return Optional.ofNullable(alias);
    }
    
    /**
     * Get pivot segment.
     * 
     * @return pivot segment
     */
    public Optional<PivotSegment> getPivot() {
        return Optional.ofNullable(pivot);
    }
}
