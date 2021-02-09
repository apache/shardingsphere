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

package org.apache.shardingsphere.test.integration.engine.junit;

import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.database.type.dialect.PostgreSQLDatabaseType;
import org.apache.shardingsphere.test.integration.cases.SQLCommandType;
import org.apache.shardingsphere.test.integration.engine.param.domain.ParameterizedWrapper;
import org.junit.runners.model.RunnerScheduler;
import org.junit.runners.parameterized.BlockJUnit4ClassRunnerWithParameters;

import java.lang.reflect.Field;

/**
 * Represents a strategy for scheduling when individual test methods should be run (in serial or parallel).
 * 
 * <p>
 * WARNING: still experimental, may go away.
 * </p>
 */
public final class ITRunnerScheduler implements RunnerScheduler {
    
    private final Field parametersField;
    
    private volatile Field runnerField;
    
    private volatile ITRunnerExecutor runnerExecutor;
    
    @SneakyThrows
    public ITRunnerScheduler() {
        parametersField = BlockJUnit4ClassRunnerWithParameters.class.getDeclaredField("parameters");
        parametersField.setAccessible(true);
    }
    
    @SneakyThrows
    @Override
    public void schedule(final Runnable childStatement) {
        // TODO Gets the parameters of the Runnable closure
        if (null == runnerField) {
            runnerField = childStatement.getClass().getDeclaredField("val$each");
            runnerField.setAccessible(true);
        }
        BlockJUnit4ClassRunnerWithParameters runner = (BlockJUnit4ClassRunnerWithParameters) runnerField.get(childStatement);
        Object[] parameters = (Object[]) parametersField.get(runner);
        ParameterizedWrapper parameterizedWrapper = (ParameterizedWrapper) parameters[0];
        if (null == runnerExecutor) {
            initITRunnerExecutor(parameterizedWrapper);
        }
        runnerExecutor.execute(parameterizedWrapper, childStatement);
    }
    
    private synchronized void initITRunnerExecutor(final ParameterizedWrapper parameterizedWrapper) {
        if (null == runnerExecutor) {
            if (parameterizedWrapper.getSqlCommandType() == SQLCommandType.DQL) {
                runnerExecutor = new ITRunnerNonScenariosExecutor();
            } else if (parameterizedWrapper.getSqlCommandType() == SQLCommandType.DDL
                    && parameterizedWrapper.getDatabaseType() instanceof PostgreSQLDatabaseType) {
                runnerExecutor = new ITRunnerExecutor() {
            
                    @Override
                    public void execute(final ParameterizedWrapper parameterizedWrapper, final Runnable childStatement) {
                        childStatement.run();
                    }
            
                    @Override
                    public void finished() {
                    }
                };
            } else {
                runnerExecutor = new ITRunnerScenariosExecutor();
            }
        }
    }
    
    @Override
    public void finished() {
        if (null != runnerExecutor) {
            runnerExecutor.finished();
        }
    }
}
