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

package org.apache.shardingsphere.infra.fixture;

import org.apache.shardingsphere.infra.rule.identifier.scope.SchemaRule;
import org.apache.shardingsphere.infra.rule.identifier.type.DataSourceContainedRule;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public final class ReadWriteSplittingRuleFixture implements SchemaRule, DataSourceContainedRule {
    
    @Override
    public String getType() {
        return ReadWriteSplittingRuleFixture.class.getSimpleName();
    }
    
    @Override
    public Map<String, Collection<String>> getDataSourceMapper() {
        return Collections.emptyMap();
    }
}
