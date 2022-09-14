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

package org.apache.shardingsphere.infra.yaml.config.pojo.data.pipeline;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.shardingsphere.infra.util.yaml.YamlConfiguration;
import org.apache.shardingsphere.infra.yaml.config.pojo.algorithm.YamlAlgorithmConfiguration;

/**
 * YAML pipeline process configuration.
 */
@Getter
@Setter
@ToString
public final class YamlPipelineProcessConfiguration implements YamlConfiguration {
    
    private YamlPipelineReadConfiguration read;
    
    private YamlPipelineWriteConfiguration write;
    
    private YamlAlgorithmConfiguration streamChannel;
    
    /**
     * Copy non-null fields from another.
     *
     * @param another another configuration
     */
    // TODO add unit test
    public void copyNonNullFields(final YamlPipelineProcessConfiguration another) {
        if (null == another) {
            return;
        }
        if (null == read) {
            read = another.read;
        } else {
            read.copyNonNullFields(another.read);
        }
        if (null == write) {
            write = another.write;
        } else {
            write.copyNonNullFields(another.write);
        }
        if (null == streamChannel) {
            streamChannel = another.streamChannel;
        }
    }
    
    /**
     * Set all fields null.
     */
    public void setAllFieldsNull() {
        read = null;
        write = null;
        streamChannel = null;
    }
    
    /**
     * Set field to null.
     *
     * @param nodeName node name
     */
    public void setFieldNull(final String nodeName) {
        switch (nodeName.toUpperCase()) {
            case "READ":
                read = null;
                break;
            case "WRITE":
                write = null;
                break;
            case "STREAM_CHANNEL":
                streamChannel = null;
                break;
            default:
                break;
        }
    }
    
    /**
     * Check all fields is null.
     *
     * @return true if all fields is null, otherwise is false.
     */
    public boolean isAllFieldsNull() {
        return null == read && null == write && null == streamChannel;
    }
}
