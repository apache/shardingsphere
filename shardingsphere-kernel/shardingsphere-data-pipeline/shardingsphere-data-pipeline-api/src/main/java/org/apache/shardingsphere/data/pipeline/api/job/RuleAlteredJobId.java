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
import com.google.common.base.Splitter;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.ToString;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Job id.
 */
@Getter
@Setter
@ToString(callSuper = true)
// TODO refactor as SPI
public final class RuleAlteredJobId extends AbstractJobId {
    
    public static final String CURRENT_VERSION = "01";
    
    @NonNull
    private Integer currentMetadataVersion;
    
    @NonNull
    private Integer newMetadataVersion;
    
    /**
     * Marshal job id.
     *
     * @return job id text. Format: {type} + hex({formatVersion}|{sortedSubTypes}|{currentMetadataVersion}T{newMetadataVersion}|{databaseName})
     */
    public String marshal() {
        List<String> subTypes = getSubTypes();
        Collections.sort(subTypes);
        String text = getFormatVersion() + "|" + String.join("-", subTypes) + "|" + getCurrentMetadataVersion() + "T" + getNewMetadataVersion() + "|" + getDatabaseName();
        return getType() + Hex.encodeHexString(text.getBytes(StandardCharsets.UTF_8), true);
    }
    
    /**
     * Unmarshal from hex text.
     *
     * @param hexText hex text
     * @return job id object
     */
    @SneakyThrows(DecoderException.class)
    public static RuleAlteredJobId unmarshal(final String hexText) {
        if (hexText.length() <= 2) {
            throw new IllegalArgumentException("Invalid hex text length, hexText=" + hexText);
        }
        String type = hexText.substring(0, 2);
        String text = new String(Hex.decodeHex(hexText.substring(2)), StandardCharsets.UTF_8);
        List<String> splittedText = Splitter.on('|').splitToList(text);
        String formatVersion = splittedText.get(0);
        Preconditions.checkState("01".equals(formatVersion), "Unknown formatVersion=" + formatVersion);
        List<String> subTypes = Splitter.on('-').splitToList(splittedText.get(1));
        List<Integer> metadataVersions = Splitter.on('T').splitToList(splittedText.get(2)).stream().map(Integer::parseInt).collect(Collectors.toList());
        String databaseName = splittedText.get(3);
        RuleAlteredJobId result = new RuleAlteredJobId();
        result.setType(type);
        result.setFormatVersion(formatVersion);
        result.setSubTypes(subTypes);
        result.setCurrentMetadataVersion(metadataVersions.get(0));
        result.setNewMetadataVersion(metadataVersions.get(1));
        result.setDatabaseName(databaseName);
        return result;
    }
}
