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

package org.apache.shardingsphere.agent.plugin.tracing.advice;

import lombok.Getter;
import org.apache.shardingsphere.agent.api.advice.AdviceTargetObject;
import org.apache.shardingsphere.agent.plugin.tracing.AgentRunner;
import org.apache.shardingsphere.infra.parser.ShardingSphereSQLParserEngine;
import org.junit.runner.RunWith;

import static org.mockito.Mockito.mock;

@RunWith(AgentRunner.class)
public abstract class AbstractSQLParserEngineAdviceTest implements AdviceTestBase {
    
    @Getter
    private AdviceTargetObject targetObject;
    
    private Object attachment;
    
    @SuppressWarnings("ConstantConditions")
    @Override
    public final void prepare() {
        Object parserEngine = mock(ShardingSphereSQLParserEngine.class, invocation -> {
            switch (invocation.getMethod().getName()) {
                case "getAttachment":
                    return attachment;
                case "setAttachment":
                    attachment = invocation.getArguments()[0];
                    return null;
                default:
                    return invocation.callRealMethod();
            }
        });
        targetObject = (AdviceTargetObject) parserEngine;
    }
}
