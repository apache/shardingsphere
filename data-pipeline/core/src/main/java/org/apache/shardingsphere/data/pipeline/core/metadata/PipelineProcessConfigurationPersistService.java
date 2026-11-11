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

package org.apache.shardingsphere.data.pipeline.core.metadata;

import com.google.common.base.Strings;
import org.apache.bval.jsr.ApacheValidationProvider;
import org.apache.shardingsphere.data.pipeline.core.context.PipelineContextKey;
import org.apache.shardingsphere.data.pipeline.core.job.api.PipelineAPIFactory;
import org.apache.shardingsphere.data.pipeline.core.job.progress.config.PipelineProcessConfiguration;
import org.apache.shardingsphere.data.pipeline.core.job.progress.config.yaml.config.YamlPipelineProcessConfiguration;
import org.apache.shardingsphere.data.pipeline.core.job.progress.config.yaml.swapper.YamlPipelineProcessConfigurationSwapper;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Pipeline process configuration persist service.
 */
public final class PipelineProcessConfigurationPersistService implements PipelineMetaDataPersistService<PipelineProcessConfiguration> {
    
    private static final Validator VALIDATOR = Validation.byProvider(ApacheValidationProvider.class).configure().buildValidatorFactory().getValidator();
    
    private final YamlPipelineProcessConfigurationSwapper swapper = new YamlPipelineProcessConfigurationSwapper();
    
    @Override
    public PipelineProcessConfiguration load(final PipelineContextKey contextKey, final String jobType) {
        String yamlText = PipelineAPIFactory.getPipelineGovernanceFacade(contextKey).getMetaDataFacade().getProcessConfiguration().load(jobType);
        if (Strings.isNullOrEmpty(yamlText)) {
            return null;
        }
        YamlPipelineProcessConfiguration yamlConfig = YamlEngine.unmarshal(yamlText, YamlPipelineProcessConfiguration.class, true);
        PipelineProcessConfiguration result = swapper.swapToObject(yamlConfig);
        validate(result);
        return result;
    }
    
    @Override
    public void persist(final PipelineContextKey contextKey, final String jobType, final PipelineProcessConfiguration processConfig) {
        validate(processConfig);
        String yamlText = YamlEngine.marshal(swapper.swapToYamlConfiguration(processConfig));
        PipelineAPIFactory.getPipelineGovernanceFacade(contextKey).getMetaDataFacade().getProcessConfiguration().persist(jobType, yamlText);
    }
    
    private void validate(final PipelineProcessConfiguration processConfig) {
        if (null == processConfig) {
            return;
        }
        Collection<ConstraintViolation<PipelineProcessConfiguration>> violations = VALIDATOR.validate(processConfig);
        ShardingSpherePreconditions.checkMustEmpty(violations,
                () -> new IllegalArgumentException(violations.stream().map(ConstraintViolation::getMessage).distinct().sorted().collect(Collectors.joining("; "))));
    }
}
