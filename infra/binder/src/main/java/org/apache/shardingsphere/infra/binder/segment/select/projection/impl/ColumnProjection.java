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

package org.apache.shardingsphere.infra.binder.segment.select.projection.impl;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.apache.shardingsphere.infra.binder.segment.select.projection.Projection;
import org.apache.shardingsphere.sql.parser.sql.common.enums.QuoteCharacter;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;

import java.util.Optional;

/**
 * Common projection.
 */
@RequiredArgsConstructor
@Getter
@EqualsAndHashCode
@ToString
public final class ColumnProjection implements Projection {
    
    private final IdentifierValue ownerIdentifier;
    
    private final IdentifierValue nameIdentifier;
    
    private final IdentifierValue aliasIdentifier;
    
    public ColumnProjection(final String owner, final String name, final String alias) {
        this(null == owner ? null : new IdentifierValue(owner, QuoteCharacter.NONE), new IdentifierValue(name, QuoteCharacter.NONE),
                null == alias ? null : new IdentifierValue(alias, QuoteCharacter.NONE));
    }
    
    /**
     * Get column name.
     * 
     * @return column name
     */
    public String getName() {
        return nameIdentifier.getValue();
    }
    
    /**
     * Get owner.
     * 
     * @return owner
     */
    public String getOwner() {
        return null == ownerIdentifier ? null : ownerIdentifier.getValue();
    }
    
    @Override
    public String getExpression() {
        return null == getOwner() ? getName() : getOwner() + "." + getName();
    }
    
    @Override
    public String getColumnLabel() {
        return getAlias().orElse(getName());
    }
    
    @Override
    public Optional<String> getAlias() {
        return Optional.ofNullable(aliasIdentifier).map(IdentifierValue::getValue);
    }
    
    @Override
    public Projection cloneWithOwner(final IdentifierValue ownerIdentifier) {
        return new ColumnProjection(ownerIdentifier, nameIdentifier, aliasIdentifier);
    }
}
