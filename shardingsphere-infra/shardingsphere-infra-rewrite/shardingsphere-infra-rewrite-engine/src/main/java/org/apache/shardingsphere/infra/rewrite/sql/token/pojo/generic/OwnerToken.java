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

package org.apache.shardingsphere.infra.rewrite.sql.token.pojo.generic;

import lombok.Getter;
import org.apache.shardingsphere.infra.rewrite.sql.token.pojo.RouteUnitAware;
import org.apache.shardingsphere.infra.rewrite.sql.token.pojo.SQLToken;
import org.apache.shardingsphere.infra.rewrite.sql.token.pojo.Substitutable;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.sql.parser.sql.common.constant.QuoteCharacter;

import java.util.Objects;
import java.util.Set;

/**
 * Owner token.
 */
public final class OwnerToken extends SQLToken implements Substitutable, RouteUnitAware {

    @Getter
    private final int stopIndex;

    private final String ownerName;

    private final String tableName;

    private final QuoteCharacter quoteCharacter;

    public OwnerToken(final int startIndex, final int stopIndex, final String ownerName, final String tableName, final QuoteCharacter quoteCharacter) {
        super(startIndex);
        this.stopIndex = stopIndex;
        this.ownerName = ownerName;
        this.tableName = tableName;
        this.quoteCharacter = quoteCharacter;
    }

    @Override
    public String toString(final RouteUnit routeUnit) {
        if (Objects.nonNull(ownerName) && tableName.equals(ownerName)) {
            Set<String> actualTableNames = routeUnit.getActualTableNames(tableName);
            String actualTableName = actualTableNames.isEmpty() ? tableName.toLowerCase() : actualTableNames.iterator().next();
            return getQuoteCharacter().wrap(actualTableName) + ".";
        }
        return toString();
    }

    @Override
    public String toString() {
        return Objects.isNull(ownerName) ? "" : getQuoteCharacter().wrap(ownerName) + ".";
    }

    @Override
    public int getStopIndex() {
        return stopIndex;
    }

    /**
     * get QuoteCharacter.
     * @return column QuoteCharacter
     */
    public QuoteCharacter getQuoteCharacter() {
        return Objects.nonNull(quoteCharacter) ? quoteCharacter : QuoteCharacter.NONE;
    }
}
