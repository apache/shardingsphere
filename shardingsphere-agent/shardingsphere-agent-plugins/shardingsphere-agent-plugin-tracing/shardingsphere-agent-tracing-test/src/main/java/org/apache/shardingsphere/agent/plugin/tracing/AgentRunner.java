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
import org.apache.shardingsphere.agent.api.advice.AdviceTargetObject;
import org.apache.shardingsphere.agent.plugin.tracing.advice.AdviceTestBase;
import org.apache.shardingsphere.agent.plugin.tracing.rule.CollectorRule;
import org.junit.rules.TestRule;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

public final class AgentRunner extends BlockJUnit4ClassRunner {
    
    private static final String EXTRA_DATA = "_$EXTRA_DATA$_";
    
    private static ResettableClassFileTransformer byteBuddyAgent;
    
    private static final String[] ENHANCEMENT_CLASSES = {
            "org.apache.shardingsphere.proxy.frontend.command.CommandExecutorTask",
            "org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutorCallback",
            "org.apache.shardingsphere.infra.parser.ShardingSphereSQLParserEngine",
    };
    
    private CollectorRule collectorRule;
    
    public AgentRunner(final Class<?> testClass) throws InitializationError {
        super(testClass);
    }
    
    @Override
    protected Statement withBeforeClasses(final Statement statement) {
        ByteBuddyAgent.install();
        Collection<String> classes = new HashSet<>(Arrays.asList(ENHANCEMENT_CLASSES));
        byteBuddyAgent = new AgentBuilder.Default()
                .with(new ByteBuddy().with(TypeValidation.ENABLED))
                .type(ElementMatchers.namedOneOf(ENHANCEMENT_CLASSES))
                .transform((builder, typeDescription, classLoader, module) -> {
                    if (classes.contains(typeDescription.getTypeName())) {
                        return builder.defineField(EXTRA_DATA, Object.class, Opcodes.ACC_PRIVATE | Opcodes.ACC_VOLATILE)
                                .implement(AdviceTargetObject.class)
                                .intercept(FieldAccessor.ofField(EXTRA_DATA));
                    }
                    return builder;
                }).installOnByteBuddyAgent();
        // load them into current classloader
        classes.forEach(each -> {
            try {
                Class<?> klass = Class.forName(each);
            } catch (final ClassNotFoundException ignored) {
            }
        });
        return super.withBeforeClasses(statement);
    }
    
    @Override
    protected List<TestRule> classRules() {
        List<TestRule> result = super.classRules();
        collectorRule = result.stream().filter(each -> each instanceof CollectorRule).findFirst().map(optional -> (CollectorRule) optional).orElse(() -> {
        });
        return result;
    }
    
    @Override
    protected Statement withBefores(final FrameworkMethod method, final Object target, final Statement statement) {
        if (target instanceof AdviceTestBase) {
            ((AdviceTestBase) target).prepare();
        }
        return super.withBefores(method, target, statement);
    }
    
    @Override
    protected Statement withAfters(final FrameworkMethod method, final Object target, final Statement statement) {
        return super.withAfters(method, target, new Statement() {
            
            @Override
            public void evaluate() throws Throwable {
                try {
                    statement.evaluate();
                } finally {
                    collectorRule.cleanup();
                }
            }
        });
    }
    
    @Override
    protected Statement withAfterClasses(final Statement statement) {
        byteBuddyAgent.reset(ByteBuddyAgent.getInstrumentation(), AgentBuilder.RedefinitionStrategy.RETRANSFORMATION);
        return super.withAfterClasses(statement);
    }
}
