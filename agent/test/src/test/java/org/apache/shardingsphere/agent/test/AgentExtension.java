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

package org.apache.shardingsphere.agent.test;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.agent.ByteBuddyAgent;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.agent.builder.ResettableClassFileTransformer;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType.Builder;
import net.bytebuddy.dynamic.scaffold.TypeValidation;
import net.bytebuddy.implementation.FieldAccessor;
import net.bytebuddy.jar.asm.Opcodes;
import net.bytebuddy.matcher.ElementMatchers;
import org.apache.shardingsphere.agent.api.advice.TargetAdviceObject;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * Agent extension.
 */
public final class AgentExtension implements BeforeAllCallback, AfterAllCallback {
    
    private static final String EXTRA_DATA = "_$EXTRA_DATA$_";
    
    private ResettableClassFileTransformer byteBuddyAgent;
    
    @Override
    public void beforeAll(final ExtensionContext context) {
        AdviceTargetClassSetting adviceTargetSetting = context.getRequiredTestClass().getAnnotation(AdviceTargetClassSetting.class);
        if (null == adviceTargetSetting) {
            return;
        }
        String targetClassName = adviceTargetSetting.value();
        ByteBuddyAgent.install();
        byteBuddyAgent = new AgentBuilder.Default()
                .with(new ByteBuddy().with(TypeValidation.ENABLED))
                .type(ElementMatchers.named(targetClassName))
                .transform((builder, typeDescription, classLoader, module) -> build(builder, typeDescription, targetClassName))
                .with(AgentBuilder.RedefinitionStrategy.RETRANSFORMATION)
                .installOnByteBuddyAgent();
    }
    
    @Override
    public void afterAll(final ExtensionContext context) {
        if (null != byteBuddyAgent) {
            byteBuddyAgent.reset(ByteBuddyAgent.getInstrumentation(), AgentBuilder.RedefinitionStrategy.RETRANSFORMATION);
        }
    }
    
    private Builder<?> build(final Builder<?> builder, final TypeDescription typeDescription, final String targetClassName) {
        Builder<?> result = builder;
        if (targetClassName.equals(typeDescription.getTypeName())) {
            result = builder.defineField(EXTRA_DATA, Object.class, Opcodes.ACC_PRIVATE | Opcodes.ACC_VOLATILE)
                    .implement(TargetAdviceObject.class)
                    .intercept(FieldAccessor.ofField(EXTRA_DATA));
        }
        return result;
    }
}
