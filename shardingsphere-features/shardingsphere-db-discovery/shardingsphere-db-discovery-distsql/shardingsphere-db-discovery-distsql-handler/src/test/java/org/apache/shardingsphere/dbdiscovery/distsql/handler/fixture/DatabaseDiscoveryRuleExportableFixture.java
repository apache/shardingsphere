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

package org.apache.shardingsphere.dbdiscovery.distsql.handler.fixture;

import org.apache.shardingsphere.infra.distsql.constant.ExportableConstants;
import org.apache.shardingsphere.infra.rule.identifier.type.ExportableRule;

import java.util.Collections;
import java.util.Map;
import java.util.function.Supplier;

public final class DatabaseDiscoveryRuleExportableFixture implements ExportableRule {
    
    @Override
    public String getType() {
        return null;
    }
    
    @Override
    public Map<String, Supplier<Object>> getExportedMethods() {
        return Collections.singletonMap(ExportableConstants.EXPORT_DB_DISCOVERY_PRIMARY_DATA_SOURCES, this::exportedMethod);
    }
    
    private Map<String, String> exportedMethod() {
        return Collections.singletonMap("ms_group", "ds_0");
    }
}
