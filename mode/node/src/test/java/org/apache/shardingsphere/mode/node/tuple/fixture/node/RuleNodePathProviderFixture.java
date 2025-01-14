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

package org.apache.shardingsphere.mode.node.tuple.fixture.node;

import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.mode.node.path.rule.RuleNodePath;
import org.apache.shardingsphere.mode.node.spi.RuleNodePathProvider;

import java.util.Arrays;

public final class RuleNodePathProviderFixture implements RuleNodePathProvider {
    
    @Override
    public RuleNodePath getRuleNodePath() {
        return new RuleNodePath("node", Arrays.asList("map_value", "gens"),
                Arrays.asList("collection_value", "string_value", "boolean_value", "integer_value", "long_value", "enum_value", "gen", "leaf"));
    }
    
    @Override
    public Class<? extends RuleConfiguration> getType() {
        return RuleConfiguration.class;
    }
}
