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

package org.apache.shardingsphere.sharding.rewrite.token.pojo;

import org.apache.shardingsphere.infra.rewrite.sql.token.pojo.Attachable;
import org.apache.shardingsphere.infra.rewrite.sql.token.pojo.RouteUnitAware;
import org.apache.shardingsphere.infra.rewrite.sql.token.pojo.SQLToken;
import org.apache.shardingsphere.infra.route.context.RouteUnit;

import java.util.Collection;
import java.util.Map;

/**
 * Projections token.
 */
public final class ProjectionsToken extends SQLToken implements Attachable, RouteUnitAware {
    
    private final Map<RouteUnit, Collection<String>> projections;
    
    public ProjectionsToken(final int startIndex, final Map<RouteUnit, Collection<String>> projections) {
        super(startIndex);
        this.projections = projections;
    }
    
    @Override
    public String toString(final RouteUnit routeUnit) {
        StringBuilder result = new StringBuilder();
        for (String each : projections.get(routeUnit)) {
            result.append(", ");
            result.append(each);
        }
        return result.toString();
    }
}
