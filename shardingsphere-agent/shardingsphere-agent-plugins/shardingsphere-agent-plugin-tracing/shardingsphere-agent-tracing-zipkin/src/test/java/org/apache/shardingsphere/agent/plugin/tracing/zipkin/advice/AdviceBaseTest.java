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

package org.apache.shardingsphere.agent.plugin.tracing.zipkin.advice;

import brave.Tracing;
import brave.propagation.StrictCurrentTraceContext;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.agent.ByteBuddyAgent;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.dynamic.scaffold.TypeValidation;
import net.bytebuddy.implementation.FieldAccessor;
import net.bytebuddy.jar.asm.Opcodes;
import net.bytebuddy.matcher.ElementMatchers;
import org.apache.shardingsphere.agent.api.advice.AdviceTargetObject;
import zipkin2.Span;

import java.util.concurrent.ConcurrentLinkedDeque;

public abstract class AdviceBaseTest {
    
    public static final ConcurrentLinkedDeque<Span> SPANS = new ConcurrentLinkedDeque<>();
    
    private static final String EXTRA_DATA = "_$EXTRA_DATA$_";
    
    /**
     * Prepare env for testing.
     * @param klass class
     */
    public static void prepare(final String klass) {
        ByteBuddyAgent.install();
        new AgentBuilder.Default().with(new ByteBuddy().with(TypeValidation.ENABLED))
                .with(new ByteBuddy())
                .type(ElementMatchers.named(klass))
                .transform((builder, typeDescription, classLoader, module) -> {
                    if (klass.equals(typeDescription.getTypeName())) {
                        return builder.defineField(EXTRA_DATA, Object.class, Opcodes.ACC_PRIVATE | Opcodes.ACC_VOLATILE)
                                .implement(AdviceTargetObject.class)
                                .intercept(FieldAccessor.ofField(EXTRA_DATA));
                    }
                    return builder;
                }).installOnByteBuddyAgent();
        Tracing.newBuilder()
                .currentTraceContext(StrictCurrentTraceContext.create())
                .spanReporter(SPANS::add)
                .build();
    }
}
