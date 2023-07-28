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

package org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item;

import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.AliasAvailable;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.AliasSegment;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;

import java.util.Optional;

/**
 * Column projection segment.
 */
@Setter
@Getter
public final class ColumnProjectionSegment implements ProjectionSegment, AliasAvailable {
    
    private final ColumnSegment column;
    
    private AliasSegment alias;
    
    private boolean visible = true;
    
    public ColumnProjectionSegment(final ColumnSegment columnSegment) {
        column = columnSegment;
    }
    
    @Override
    public String getColumnLabel() {
        return getAliasName().orElse(column.getIdentifier().getValue());
    }
    
    @Override
    public Optional<String> getAliasName() {
        return null == alias ? Optional.empty() : Optional.ofNullable(alias.getIdentifier().getValue());
    }
    
    @Override
    public Optional<IdentifierValue> getAlias() {
        return Optional.ofNullable(alias).map(AliasSegment::getIdentifier);
    }
    
    @Override
    public int getStartIndex() {
        return column.getStartIndex();
    }
    
    @Override
    public int getStopIndex() {
        return null == alias ? column.getStopIndex() : alias.getStopIndex();
    }
    
    /**
     * Get alias segment.
     * @return alias segment
     */
    public Optional<AliasSegment> getAliasSegment() {
        return Optional.ofNullable(alias);
    }
}
