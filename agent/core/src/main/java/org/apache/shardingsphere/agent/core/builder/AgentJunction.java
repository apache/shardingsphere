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

package org.apache.shardingsphere.agent.core.builder;

import lombok.RequiredArgsConstructor;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatcher.Junction;
import org.apache.shardingsphere.agent.core.advisor.config.AdvisorConfiguration;

import java.util.Map;

/**
 * Agent junction.
 */
@RequiredArgsConstructor
public final class AgentJunction implements Junction<TypeDescription> {
    
    private final Map<String, AdvisorConfiguration> advisorConfigs;
    
    @Override
    public boolean matches(final TypeDescription target) {
        return advisorConfigs.containsKey(target.getTypeName());
    }
    
    @SuppressWarnings("NullableProblems")
    @Override
    public <U extends TypeDescription> Junction<U> and(final ElementMatcher<? super U> other) {
        return null;
    }
    
    @SuppressWarnings("NullableProblems")
    @Override
    public <U extends TypeDescription> Junction<U> or(final ElementMatcher<? super U> other) {
        return null;
    }
}
