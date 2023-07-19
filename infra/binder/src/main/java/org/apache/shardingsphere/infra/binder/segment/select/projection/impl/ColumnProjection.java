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
import lombok.Setter;
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
@Setter
@EqualsAndHashCode(exclude = {"originalOwner", "originalName"})
@ToString
public final class ColumnProjection implements Projection {
    
    private final IdentifierValue owner;
    
    private final IdentifierValue name;
    
    private final IdentifierValue alias;
    
    private IdentifierValue originalOwner;
    
    private IdentifierValue originalName;
    
    public ColumnProjection(final String owner, final String name, final String alias) {
        this(null == owner ? null : new IdentifierValue(owner, QuoteCharacter.NONE), new IdentifierValue(name, QuoteCharacter.NONE),
                null == alias ? null : new IdentifierValue(alias, QuoteCharacter.NONE));
    }
    
    /**
     * Get column name.
     * 
     * @return column name
     */
    public IdentifierValue getName() {
        return name;
    }
    
    /**
     * Get owner.
     * 
     * @return owner
     */
    public Optional<IdentifierValue> getOwner() {
        return Optional.ofNullable(owner);
    }
    
    @Override
    public String getColumnName() {
        return null == owner ? name.getValue() : owner.getValue() + "." + name.getValue();
    }
    
    @Override
    public String getColumnLabel() {
        return getAlias().map(IdentifierValue::getValue).orElse(name.getValue());
    }
    
    @Override
    public Optional<IdentifierValue> getAlias() {
        return Optional.ofNullable(alias);
    }
    
    @Override
    public Projection transformSubqueryProjection(final IdentifierValue subqueryTableAlias, final IdentifierValue originalOwner, final IdentifierValue originalName) {
        ColumnProjection result = null == alias ? new ColumnProjection(subqueryTableAlias, name, null) : new ColumnProjection(subqueryTableAlias, alias, null);
        result.setOriginalOwner(originalOwner);
        result.setOriginalName(originalName);
        return result;
    }
    
    /**
     * Get original owner.
     *
     * @return original owner
     */
    public IdentifierValue getOriginalOwner() {
        return null == originalOwner ? owner : originalOwner;
    }
    
    /**
     * Get original name.
     * 
     * @return original name
     */
    public IdentifierValue getOriginalName() {
        return null == originalName ? name : originalName;
    }
}
