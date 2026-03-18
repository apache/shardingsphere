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

package org.apache.shardingsphere.data.pipeline.core.job.id;

import com.google.common.base.Preconditions;
import com.google.common.primitives.Shorts;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.shardingsphere.data.pipeline.core.context.PipelineContextKey;
import org.apache.shardingsphere.data.pipeline.core.exception.job.PipelineJobNotFoundException;
import org.apache.shardingsphere.data.pipeline.core.job.api.PipelineAPIFactory;
import org.apache.shardingsphere.data.pipeline.core.job.type.PipelineJobType;
import org.apache.shardingsphere.elasticjob.infra.pojo.JobConfigurationPOJO;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.instance.metadata.InstanceType;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;

import java.nio.charset.StandardCharsets;

/**
 * Pipeline job id utility class.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@SuppressWarnings("rawtypes")
public final class PipelineJobIdUtils {
    
    /**
     * Marshal job id prefix.
     *
     * @param jobId pipeline job id
     * @return job id
     */
    public static String marshal(final PipelineJobId jobId) {
        return marshalPrefix(jobId.getJobType(), jobId.getContextKey()) + jobId.marshalSuffix();
    }
    
    private static String marshalPrefix(final PipelineJobType jobType, final PipelineContextKey contextKey) {
        InstanceType instanceType = contextKey.getInstanceType();
        String databaseName = instanceType == InstanceType.PROXY ? "" : contextKey.getDatabaseName();
        String databaseNameHex = Hex.encodeHexString(databaseName.getBytes(StandardCharsets.UTF_8), true);
        String databaseNameLengthHex = Hex.encodeHexString(Shorts.toByteArray((short) databaseNameHex.length()), true);
        return 'j' + jobType.getOption().getCode() + PipelineJobId.CURRENT_VERSION + instanceType.getCode() + databaseNameLengthHex + databaseNameHex;
    }
    
    /**
     * Parse job type.
     *
     * @param jobId job id
     * @return job type
     */
    public static PipelineJobType parseJobType(final String jobId) {
        verifyJobId(jobId);
        return TypedSPILoader.getService(PipelineJobType.class, jobId.substring(1, 3));
    }
    
    /**
     * Parse context key.
     *
     * @param jobId job id
     * @return pipeline context key
     */
    @SneakyThrows(DecoderException.class)
    public static PipelineContextKey parseContextKey(final String jobId) {
        verifyJobId(jobId);
        String formatVersion = jobId.substring(3, 5);
        Preconditions.checkArgument(PipelineJobId.CURRENT_VERSION.equals(formatVersion), "Format version doesn't match, format version: " + formatVersion);
        char instanceTypeCode = jobId.charAt(5);
        short databaseNameLength = Shorts.fromByteArray(Hex.decodeHex(jobId.substring(6, 10)));
        String databaseName = new String(Hex.decodeHex(jobId.substring(10, 10 + databaseNameLength)), StandardCharsets.UTF_8);
        return new PipelineContextKey(databaseName, InstanceType.valueOf(instanceTypeCode));
    }
    
    private static void verifyJobId(final String jobId) {
        Preconditions.checkArgument(jobId.length() > 10, "Invalid job id length, job id: `%s`", jobId);
        Preconditions.checkArgument('j' == jobId.charAt(0), "Invalid job id, first char: `%s`", jobId.charAt(0));
    }
    
    /**
     * Get ElasticJob configuration POJO.
     *
     * @param jobId job id
     * @return ElasticJob configuration POJO
     */
    public static JobConfigurationPOJO getElasticJobConfigurationPOJO(final String jobId) {
        JobConfigurationPOJO result = PipelineAPIFactory.getJobConfigurationAPI(parseContextKey(jobId)).getJobConfiguration(jobId);
        ShardingSpherePreconditions.checkNotNull(result, () -> new PipelineJobNotFoundException(jobId));
        return result;
    }
}
