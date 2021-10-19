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

package org.apache.shardingsphere.sharding.rewrite.parameter;

import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.rewrite.parameter.rewriter.ParameterRewriter;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.junit.Test;

import java.util.Collection;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;

public final class ShardingParameterRewriterBuilderTest {

    @Test
    public void assertGetParameterRewriters() {
        ShardingSphereSchema shardingSphereSchema = mock(ShardingSphereSchema.class);
        ShardingRule shardingRule = mock(ShardingRule.class);
        RouteContext routeContext = mock(RouteContext.class);
        ShardingParameterRewriterBuilder shardingParameterRewriterBuilder = new ShardingParameterRewriterBuilder(shardingRule, routeContext);
        Collection<ParameterRewriter> result = shardingParameterRewriterBuilder.getParameterRewriters(shardingSphereSchema);
        assertFalse(result.isEmpty());
        assertThat(result.size(), is(2));
    }
}
