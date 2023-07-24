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

package org.apache.shardingsphere.test.e2e.data.pipeline.framework.param;

import com.google.common.base.Preconditions;
import org.apache.shardingsphere.infra.database.spi.DatabaseType;
import org.apache.shardingsphere.infra.util.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.test.e2e.data.pipeline.env.PipelineE2EEnvironment;
import org.apache.shardingsphere.test.e2e.data.pipeline.framework.param.PipelineE2ESettings.PipelineE2EDatabaseSettings;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Pipeline E2E test case arguments provider.
 */
public final class PipelineE2ETestCaseArgumentsProvider implements ArgumentsProvider {
    
    @Override
    public Stream<? extends Arguments> provideArguments(final ExtensionContext extensionContext) {
        Collection<Arguments> result = new LinkedList<>();
        PipelineE2ESettings settings = extensionContext.getRequiredTestClass().getAnnotation(PipelineE2ESettings.class);
        Preconditions.checkNotNull(settings, "Annotation PipelineE2ESettings is required.");
        for (PipelineE2EDatabaseSettings each : settings.database()) {
            result.addAll(provideArguments(settings, each));
        }
        return result.stream();
    }
    
    private Collection<Arguments> provideArguments(final PipelineE2ESettings settings, final PipelineE2EDatabaseSettings databaseSettings) {
        DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, databaseSettings.type());
        List<String> storageContainerImages = PipelineE2EEnvironment.getInstance().listStorageContainerImages(databaseType);
        return settings.fetchSingle() && !storageContainerImages.isEmpty()
                ? provideArguments(databaseSettings.scenarioFiles(), databaseType, storageContainerImages.get(0))
                : storageContainerImages.stream().flatMap(each -> provideArguments(databaseSettings.scenarioFiles(), databaseType, each).stream()).collect(Collectors.toList());
    }
    
    private Collection<Arguments> provideArguments(final String[] scenarioFiles, final DatabaseType databaseType, final String storageContainerImage) {
        return Arrays.stream(scenarioFiles).map(each -> Arguments.of(new PipelineTestParameter(databaseType, storageContainerImage, each))).collect(Collectors.toList());
    }
}
