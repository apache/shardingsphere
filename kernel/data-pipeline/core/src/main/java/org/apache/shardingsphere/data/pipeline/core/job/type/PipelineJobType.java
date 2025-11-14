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

package org.apache.shardingsphere.data.pipeline.core.job.type;

import com.fasterxml.jackson.annotation.JsonIgnoreType;
import org.apache.shardingsphere.data.pipeline.core.consistencycheck.ConsistencyCheckJobItemProgressContext;
import org.apache.shardingsphere.data.pipeline.core.consistencycheck.PipelineDataConsistencyChecker;
import org.apache.shardingsphere.data.pipeline.core.context.TransmissionProcessContext;
import org.apache.shardingsphere.data.pipeline.core.job.config.PipelineJobConfiguration;
import org.apache.shardingsphere.data.pipeline.core.pojo.PipelineJobTarget;
import org.apache.shardingsphere.infra.spi.annotation.SingletonSPI;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPI;

import java.util.Collection;
import java.util.Collections;

/**
 * Pipeline job type.
 * 
 * @param <T> type of pipeline job configuration
 */
@SingletonSPI
@JsonIgnoreType
public interface PipelineJobType<T extends PipelineJobConfiguration> extends TypedSPI {
    
    /**
     * Get pipeline job option.
     *
     * @return pipeline job option
     */
    PipelineJobOption getOption();
    
    /**
     * Get pipeline job target.
     *
     * @param jobConfig pipeline job configuration
     * @return pipeline job target
     */
    PipelineJobTarget getJobTarget(T jobConfig);
    
    /**
     * Build pipeline data consistency checker.
     *
     * @param jobConfig pipeline job configuration
     * @param processContext process context
     * @param progressContext consistency check job item progress context
     * @return all logic tables check result
     * @throws UnsupportedOperationException unsupported operation exception
     */
    default PipelineDataConsistencyChecker buildDataConsistencyChecker(final T jobConfig,
                                                                       final TransmissionProcessContext processContext, final ConsistencyCheckJobItemProgressContext progressContext) {
        throw new UnsupportedOperationException("Build data consistency checker is not supported.");
    }
    
    @Override
    String getType();
    
    @Override
    default Collection<Object> getTypeAliases() {
        return Collections.singleton(getOption().getCode());
    }
}
