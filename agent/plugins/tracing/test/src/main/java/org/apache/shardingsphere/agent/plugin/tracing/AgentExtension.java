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

package org.apache.shardingsphere.agent.plugin.tracing;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.agent.ByteBuddyAgent;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.agent.builder.ResettableClassFileTransformer;
import net.bytebuddy.dynamic.scaffold.TypeValidation;
import net.bytebuddy.implementation.FieldAccessor;
import net.bytebuddy.jar.asm.Opcodes;
import net.bytebuddy.matcher.ElementMatchers;
import org.apache.shardingsphere.agent.api.advice.TargetAdviceObject;
import org.apache.shardingsphere.agent.plugin.tracing.advice.AdviceTestBase;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

public final class AgentExtension implements BeforeAllCallback, AfterAllCallback, BeforeEachCallback {
    
    private static final String EXTRA_DATA = "_$EXTRA_DATA$_";
    
    private static ResettableClassFileTransformer byteBuddyAgent;
    
    private static final String[] ENHANCEMENT_CLASSES = {
            "org.apache.shardingsphere.proxy.frontend.command.CommandExecutorTask",
            "org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutorCallback",
            "org.apache.shardingsphere.infra.parser.ShardingSphereSQLParserEngine",
    };
    
    @Override
    public void beforeAll(final ExtensionContext context) {
        ByteBuddyAgent.install();
        Collection<String> classes = new HashSet<>(Arrays.asList(ENHANCEMENT_CLASSES));
        byteBuddyAgent = new AgentBuilder.Default()
                .with(new ByteBuddy().with(TypeValidation.ENABLED))
                .type(ElementMatchers.namedOneOf(ENHANCEMENT_CLASSES))
                .transform((builder, typeDescription, classLoader, module) -> {
                    if (classes.contains(typeDescription.getTypeName())) {
                        return builder.defineField(EXTRA_DATA, Object.class, Opcodes.ACC_PRIVATE | Opcodes.ACC_VOLATILE)
                                .implement(TargetAdviceObject.class)
                                .intercept(FieldAccessor.ofField(EXTRA_DATA));
                    }
                    return builder;
                }).installOnByteBuddyAgent();
        // load them into current classloader
        classes.forEach(each -> {
            try {
                Class.forName(each);
            } catch (final ClassNotFoundException ignored) {
            }
        });
    }
    
    @Override
    public void afterAll(final ExtensionContext context) {
        byteBuddyAgent.reset(ByteBuddyAgent.getInstrumentation(), AgentBuilder.RedefinitionStrategy.RETRANSFORMATION);
    }
    
    @Override
    public void beforeEach(final ExtensionContext context) {
        Object testInstance = context.getRequiredTestInstance();
        if (testInstance instanceof AdviceTestBase) {
            ((AdviceTestBase) testInstance).prepare();
        }
    }
}
