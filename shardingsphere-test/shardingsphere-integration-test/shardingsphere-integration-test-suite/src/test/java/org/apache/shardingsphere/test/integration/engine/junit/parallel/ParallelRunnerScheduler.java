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

package org.apache.shardingsphere.test.integration.engine.junit.parallel;

import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.test.integration.cases.SQLCommandType;
import org.apache.shardingsphere.test.integration.engine.junit.parallel.impl.CaseParallelRunnerExecutor;
import org.apache.shardingsphere.test.integration.engine.junit.parallel.impl.ScenarioParallelRunnerExecutor;
import org.apache.shardingsphere.test.integration.engine.param.model.ParameterizedArray;
import org.apache.shardingsphere.test.integration.env.IntegrationTestEnvironment;
import org.junit.runners.model.RunnerScheduler;
import org.junit.runners.parameterized.BlockJUnit4ClassRunnerWithParameters;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * Parallel runner scheduler.
 */
public final class ParallelRunnerScheduler implements RunnerScheduler {
    
    private final Field parametersField;
    
    private final Map<String, ParallelRunnerExecutor> runnerExecutors;
    
    private volatile Field runnerField;
    
    public ParallelRunnerScheduler() {
        parametersField = getParametersField();
        runnerExecutors = getRunnerExecutors();
    }
    
    @SneakyThrows(NoSuchFieldException.class)
    private Field getParametersField() {
        Field result = BlockJUnit4ClassRunnerWithParameters.class.getDeclaredField("parameters");
        result.setAccessible(true);
        return result;
    }
    
    private Map<String, ParallelRunnerExecutor> getRunnerExecutors() {
        Map<String, ParallelRunnerExecutor> result = new HashMap<>(IntegrationTestEnvironment.getInstance().getDataSourceEnvironments().size() * 2, 1);
        for (DatabaseType each : IntegrationTestEnvironment.getInstance().getDataSourceEnvironments().keySet()) {
            result.put(getRunnerExecutorKey(each.getName(), SQLCommandType.DQL.name()), new CaseParallelRunnerExecutor());
            result.put(getRunnerExecutorKey(each.getName(), ""), new ScenarioParallelRunnerExecutor());
        }
        return result;
    }
    
    private String getRunnerExecutorKey(final String databaseType, final String sqlCommandType) {
        return String.join("_", databaseType, sqlCommandType);
    }
    
    @Override
    public void schedule(final Runnable childStatement) {
        Object[] parameters = getParameters(childStatement);
        ParameterizedArray parameterizedArray = (ParameterizedArray) parameters[0];
        getRunnerExecutor(parameterizedArray).execute(parameterizedArray, childStatement);
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private Object[] getParameters(final Runnable childStatement) {
        if (null == runnerField) {
            runnerField = childStatement.getClass().getDeclaredField("val$each");
            runnerField.setAccessible(true);
        }
        return (Object[]) parametersField.get(runnerField.get(childStatement));
    }
    
    private ParallelRunnerExecutor getRunnerExecutor(final ParameterizedArray parameterizedArray) {
        return SQLCommandType.DQL == parameterizedArray.getSqlCommandType() 
                ? runnerExecutors.get(getRunnerExecutorKey(parameterizedArray.getDatabaseType().getName(), SQLCommandType.DQL.name()))
                : runnerExecutors.get(getRunnerExecutorKey(parameterizedArray.getDatabaseType().getName(), ""));
    }
    
    @Override
    public void finished() {
        if (null != runnerExecutors) {
            runnerExecutors.values().forEach(ParallelRunnerExecutor::finished);
        }
    }
}
