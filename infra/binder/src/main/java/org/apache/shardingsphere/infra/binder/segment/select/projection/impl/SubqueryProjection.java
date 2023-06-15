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

import com.google.common.base.Strings;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.apache.shardingsphere.infra.binder.segment.select.projection.Projection;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.OracleDatabaseType;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;

import java.util.Optional;

/**
 * Subquery projection.
 */
@RequiredArgsConstructor
@Getter
@EqualsAndHashCode
@ToString
public final class SubqueryProjection implements Projection {
    
    private final String expression;
    
    private final Projection projection;
    
    private final String alias;
    
    private final DatabaseType databaseType;
    
    @Override
    public Optional<String> getAlias() {
        return Strings.isNullOrEmpty(alias) ? buildDefaultAlias(databaseType) : Optional.of(alias);
    }
    
    private Optional<String> buildDefaultAlias(final DatabaseType databaseType) {
        if (databaseType instanceof OracleDatabaseType) {
            return Optional.of(expression.replace(" ", "").toUpperCase());
        }
        return Optional.of(expression);
    }
    
    @Override
    public String getColumnLabel() {
        return getAlias().orElse(expression);
    }
    
    @Override
    public Projection cloneWithOwner(final IdentifierValue ownerIdentifier) {
        return new SubqueryProjection(expression, projection, alias, databaseType);
    }
}
