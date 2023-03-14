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

package org.apache.shardingsphere.data.pipeline.core.job;

import com.google.common.base.Preconditions;
import com.google.common.primitives.Shorts;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.shardingsphere.data.pipeline.api.job.PipelineJobId;
import org.apache.shardingsphere.data.pipeline.core.job.util.InstanceTypeUtil;
import org.apache.shardingsphere.data.pipeline.spi.job.JobType;
import org.apache.shardingsphere.data.pipeline.spi.job.JobTypeFactory;

import java.nio.charset.StandardCharsets;

/**
 * Pipeline job id utils.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PipelineJobIdUtils {
    
    /**
     * Marshal job id common prefix.
     *
     * @param pipelineJobId pipeline job id
     * @return job id common prefix
     */
    public static String marshalJobIdCommonPrefix(final PipelineJobId pipelineJobId) {
        String databaseNameHex = Hex.encodeHexString(pipelineJobId.getDatabaseName().getBytes(StandardCharsets.UTF_8), true);
        String databaseNameLengthHex = Hex.encodeHexString(Shorts.toByteArray((short) databaseNameHex.length()), true);
        char instanceType = InstanceTypeUtil.encode(pipelineJobId.getInstanceType());
        return 'j' + pipelineJobId.getJobType().getTypeCode() + pipelineJobId.getFormatVersion() + instanceType + databaseNameLengthHex + databaseNameHex;
    }
    
    /**
     * Parse job id.
     *
     * @param jobId job id
     * @return pipeline job id
     */
    @SneakyThrows(DecoderException.class)
    public static BasePipelineJobId parseBaseJobId(final String jobId) {
        verifyJobId(jobId);
        String typeCode = jobId.substring(1, 3);
        String formatVersion = jobId.substring(3, 5);
        Preconditions.checkArgument(BasePipelineJobId.CURRENT_VERSION.equals(formatVersion), "Format version doesn't match, formatVersion=" + formatVersion);
        char instanceType = jobId.charAt(5);
        short databaseNameLength = Shorts.fromByteArray(Hex.decodeHex(jobId.substring(6, 10)));
        String databaseName = new String(Hex.decodeHex(jobId.substring(10, 10 + databaseNameLength)), StandardCharsets.UTF_8);
        return new BasePipelineJobId(JobTypeFactory.getInstance(typeCode), InstanceTypeUtil.decode(instanceType), databaseName);
    }
    
    private static void verifyJobId(final String jobId) {
        Preconditions.checkArgument(jobId.length() > 10, "Invalid jobId length, jobId=%s", jobId);
        Preconditions.checkArgument('j' == jobId.charAt(0), "Invalid jobId, first char=%s", jobId.charAt(0));
    }
    
    /**
     * Parse job type.
     *
     * @param jobId job id
     * @return job type
     */
    public static JobType parseJobType(final String jobId) {
        verifyJobId(jobId);
        String typeCode = jobId.substring(1, 3);
        return JobTypeFactory.getInstance(typeCode);
    }
}
