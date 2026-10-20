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

package org.apache.shardingsphere.test.e2e.operation.pipeline.framework.param;

import com.google.common.base.Preconditions;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.test.e2e.env.runtime.E2ETestEnvironment;
import org.apache.shardingsphere.test.e2e.operation.pipeline.framework.param.PipelineE2ESettings.PipelineE2EDatabaseSettings;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.support.ParameterDeclarations;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.stream.Stream;

/**
 * Pipeline E2E test case arguments provider.
 */
public final class PipelineE2ETestCaseArgumentsProvider implements ArgumentsProvider {
    
    @Override
    public Stream<? extends Arguments> provideArguments(final ParameterDeclarations parameters, final ExtensionContext context) {
        Collection<Arguments> result = new LinkedList<>();
        PipelineE2ESettings settings = context.getRequiredTestClass().getAnnotation(PipelineE2ESettings.class);
        Preconditions.checkNotNull(settings, "Annotation PipelineE2ESettings is required.");
        for (PipelineE2EDatabaseSettings each : settings.database()) {
            result.addAll(provideArguments(settings, each));
        }
        return result.stream();
    }
    
    private Collection<Arguments> provideArguments(final PipelineE2ESettings settings, final PipelineE2EDatabaseSettings databaseSettings) {
        DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, databaseSettings.type());
        Collection<String> databaseImages = E2ETestEnvironment.getInstance().getDockerEnvironment().getDatabaseImages(databaseType);
        return settings.fetchSingle() && !databaseImages.isEmpty()
                ? provideArguments(databaseType, databaseImages.iterator().next(), databaseSettings.tableStructures(), databaseSettings.storageContainerCount())
                : databaseImages.stream().flatMap(each -> provideArguments(databaseType, each, databaseSettings.tableStructures(), databaseSettings.storageContainerCount()).stream()).toList();
    }
    
    private Collection<Arguments> provideArguments(final DatabaseType databaseType, final String databaseContainerImage,
                                                   final String[] tableStructures, final int storageContainerCount) {
        return Arrays.stream(tableStructures).map(each -> Arguments.of(new PipelineTestParameter(databaseType, databaseContainerImage, each, storageContainerCount))).toList();
    }
}
