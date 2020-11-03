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

package org.apache.shardingsphere.infra.route.engine.impl;

import org.apache.shardingsphere.infra.binder.LogicSQL;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.infra.route.engine.SQLRouteExecutor;
import org.apache.shardingsphere.infra.schema.ShardingSphereSchema;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;

/**
 * All SQL route executor.
 */
public final class AllSQLRouteExecutor implements SQLRouteExecutor {
    
    @Override
    public RouteContext route(final LogicSQL logicSQL, final ShardingSphereSchema schema) {
        RouteContext result = new RouteContext();
        for (String each : getAllDataSourceNames(schema)) {
            result.getRouteUnits().add(new RouteUnit(new RouteMapper(each, each), Collections.emptyList()));
        }
        return result;
    }
    
    private Collection<String> getAllDataSourceNames(final ShardingSphereSchema schema) {
        Collection<String> result = new LinkedHashSet<>();
        for (Collection<String> each : schema.getMetaData().getTableAddressingMetaData().getTableDataSourceNamesMapper().values()) {
            result.addAll(each);
        }
        return result;
    }
}
