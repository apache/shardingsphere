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

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import lombok.SneakyThrows;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Job id codec.
 */
public final class JobIdCodec {
    
    /**
     * Marshal job id.
     *
     * @param jobId job id
     * @return job id text. Format: {formatVersion} + hex({sortedSubTypes}|{currentMetadataVersion}T{newMetadataVersion}|{schemaName})
     */
    public static String marshal(final JobId jobId) {
        List<Integer> subTypes = jobId.getSubTypes();
        Collections.sort(subTypes);
        String text = Joiner.on('-').join(subTypes) + "|" + jobId.getCurrentMetadataVersion() + "T" + jobId.getNewMetadataVersion() + "|" + jobId.getSchemaName();
        return jobId.getFormatVersion() + Hex.encodeHexString(text.getBytes(StandardCharsets.UTF_8), true);
    }
    
    /**
     * Unmarshal from hex text.
     *
     * @param hexText hex text
     * @return job id object
     */
    @SneakyThrows(DecoderException.class)
    public static JobId unmarshal(final String hexText) {
        if (hexText.length() <= 2) {
            throw new IllegalArgumentException("Invalid hex text length, hexText=" + hexText);
        }
        String formatVersion = hexText.substring(0, 2);
        String text = new String(Hex.decodeHex(hexText.substring(2)), StandardCharsets.UTF_8);
        List<String> splittedText = Splitter.on("|").splitToList(text);
        List<Integer> subTypes = Splitter.on('-').splitToList(splittedText.get(0)).stream().map(Integer::parseInt).collect(Collectors.toList());
        List<Integer> metadataVersions = Splitter.on('T').splitToList(splittedText.get(1)).stream().map(Integer::parseInt).collect(Collectors.toList());
        String schemaName = splittedText.get(2);
        return new JobId(formatVersion, subTypes, metadataVersions.get(0), metadataVersions.get(1), schemaName);
    }
}
