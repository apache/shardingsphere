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

package org.apache.shardingsphere.shadow.rewrite.token.pojo;

import org.apache.shardingsphere.infra.rewrite.sql.token.common.pojo.RouteUnitAware;
import org.apache.shardingsphere.infra.rewrite.sql.token.common.pojo.generic.InsertValue;
import org.apache.shardingsphere.infra.rewrite.sql.token.common.pojo.generic.InsertValuesToken;
import org.apache.shardingsphere.infra.route.context.RouteUnit;

import java.util.Collection;
import java.util.Map;

/**
 * Insert values token for shadow.
 */
public final class ShadowInsertValuesToken extends InsertValuesToken implements RouteUnitAware {
    
    private final Map<String, Collection<Integer>> dataSourceInsertValueIndexes;
    
    public ShadowInsertValuesToken(final int startIndex, final int stopIndex, final Map<String, Collection<Integer>> dataSourceInsertValueIndexes) {
        super(startIndex, stopIndex);
        this.dataSourceInsertValueIndexes = dataSourceInsertValueIndexes;
    }
    
    @Override
    public String toString(final RouteUnit routeUnit) {
        Collection<Integer> insertValueIndexes = null == routeUnit ? null : dataSourceInsertValueIndexes.get(routeUnit.getDataSourceMapper().getActualName());
        if (null == insertValueIndexes) {
            return getAllInsertValues();
        }
        StringBuilder result = new StringBuilder();
        for (int each : insertValueIndexes) {
            result.append(getInsertValues().get(each)).append(", ");
        }
        return 0 == result.length() ? "" : result.delete(result.length() - 2, result.length()).toString();
    }
    
    @Override
    public String toString() {
        return getAllInsertValues();
    }
    
    private String getAllInsertValues() {
        StringBuilder result = new StringBuilder();
        for (InsertValue each : getInsertValues()) {
            result.append(each).append(", ");
        }
        return 0 == result.length() ? "" : result.delete(result.length() - 2, result.length()).toString();
    }
}
