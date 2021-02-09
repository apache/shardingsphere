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
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.PostgreSQLDatabaseType;
import org.apache.shardingsphere.test.integration.cases.SQLCommandType;
import org.apache.shardingsphere.test.integration.engine.param.domain.ParameterizedWrapper;
import org.apache.shardingsphere.test.integration.env.IntegrationTestEnvironment;
import org.junit.runners.model.RunnerScheduler;
import org.junit.runners.parameterized.BlockJUnit4ClassRunnerWithParameters;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

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
    
    private final Map<String, ITRunnerExecutor> runnerExecutors;
    
    @SneakyThrows
    public ITRunnerScheduler() {
        parametersField = BlockJUnit4ClassRunnerWithParameters.class.getDeclaredField("parameters");
        parametersField.setAccessible(true);
        runnerExecutors = new HashMap<>();
        initRunnerExecutors();
    }
    
    private void initRunnerExecutors() {
        for (DatabaseType each : IntegrationTestEnvironment.getInstance().getDataSourceEnvironments().keySet()) {
            runnerExecutors.put(getRunnerExecutorKey(each.getName(), SQLCommandType.DQL.name()), new ITRunnerParallelExecutor());
            if (each instanceof PostgreSQLDatabaseType) {
                runnerExecutors.put(getRunnerExecutorKey(each.getName(), SQLCommandType.DDL.name()), new ITRunnerSerialExecutor());
            } else {
                runnerExecutors.put(getRunnerExecutorKey(each.getName(), SQLCommandType.DDL.name()), new ITRunnerScenariosExecutor());
            }
            runnerExecutors.put(getRunnerExecutorKey(each.getName(), ""), new ITRunnerScenariosExecutor());
        }
    }
    
    private String getRunnerExecutorKey(final String databaseType, final String sqlCommandType) {
        return String.join("_", databaseType, sqlCommandType);
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
        getITRunnerExecutor(parameterizedWrapper).execute(parameterizedWrapper, childStatement);
    }
    
    private ITRunnerExecutor getITRunnerExecutor(final ParameterizedWrapper parameterizedWrapper) {
        switch (parameterizedWrapper.getSqlCommandType()) {
            case DQL:
                return runnerExecutors.get(getRunnerExecutorKey(parameterizedWrapper.getDatabaseType().getName(), SQLCommandType.DQL.name()));
            case DDL:
                return runnerExecutors.get(getRunnerExecutorKey(parameterizedWrapper.getDatabaseType().getName(), SQLCommandType.DDL.name()));
            default:
                return runnerExecutors.get(getRunnerExecutorKey(parameterizedWrapper.getDatabaseType().getName(), ""));
        }
    }
    
    @Override
    public void finished() {
        if (null != runnerExecutors) {
            runnerExecutors.values().forEach(ITRunnerExecutor::finished);
        }
    }
}
