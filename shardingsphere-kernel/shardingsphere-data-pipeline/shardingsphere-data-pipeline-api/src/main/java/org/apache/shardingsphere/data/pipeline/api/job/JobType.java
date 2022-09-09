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

package org.apache.shardingsphere.data.pipeline.api.job;

import com.google.common.base.Preconditions;
import lombok.Getter;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Job type.
 */
@Getter
public enum JobType {
    
    MIGRATION("MIGRATION", "01");
    
    private static final Map<String, JobType> CODE_JOB_TYPE_MAP;
    
    private static final Map<String, JobType> NAME_JOB_TYPE_MAP;
    
    static {
        CODE_JOB_TYPE_MAP = Arrays.stream(JobType.values()).collect(Collectors.toMap(JobType::getTypeCode, each -> each));
        NAME_JOB_TYPE_MAP = Arrays.stream(JobType.values()).collect(Collectors.toMap(JobType::getUppercaseTypeName, each -> each));
    }
    
    private final String typeName;
    
    private final String lowercaseTypeName;
    
    /**
     * Used in DistSQL.
     */
    private final String uppercaseTypeName;
    
    private final String typeCode;
    
    JobType(final String typeName, final String typeCode) {
        Preconditions.checkArgument(StringUtils.isAlpha(typeName), "type name must be character of [a-z]");
        this.typeName = typeName;
        lowercaseTypeName = typeName.toLowerCase();
        uppercaseTypeName = typeName.toUpperCase();
        Preconditions.checkArgument(typeCode.length() == 2, "code length is not 2");
        this.typeCode = typeCode;
    }
    
    /**
     * Value of by code.
     *
     * @param typeCode type code
     * @return job type, might be null
     */
    public static JobType valueOfByCode(final String typeCode) {
        return CODE_JOB_TYPE_MAP.get(typeCode);
    }
    
    /**
     * Value of by type name.
     *
     * @param typeName type name
     * @return job type, might be null
     */
    public static JobType valueOfByTypeName(@NonNull final String typeName) {
        return NAME_JOB_TYPE_MAP.get(typeName.toLowerCase());
    }
}
