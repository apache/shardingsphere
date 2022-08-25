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

package org.apache.shardingsphere.encrypt.distsql.parser.segment;

import com.google.common.base.Strings;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.distsql.parser.segment.AlgorithmSegment;
import org.apache.shardingsphere.sql.parser.api.visitor.ASTNode;

/**
 * Encrypt column segment.
 */
@AllArgsConstructor
@RequiredArgsConstructor
@Getter
public final class EncryptColumnSegment implements ASTNode {
    
    private final String name;
    
    private final String cipherColumn;
    
    private final String plainColumn;
    
    private final String assistedQueryColumn;
    
    private String dataType;
    
    private String cipherDataType;
    
    private String plainDataType;
    
    private String assistedQueryDataType;
    
    private final AlgorithmSegment encryptor;
    
    private final AlgorithmSegment assistedQueryEncryptor;
    
    /**
     * Is the data type correct.
     *
     * @return correct or not
     */
    public boolean isCorrectDataType() {
        boolean requireDataType = !Strings.isNullOrEmpty(dataType);
        return isCorrectDataType(requireDataType, name, dataType) && isCorrectDataType(requireDataType, plainColumn, plainDataType)
                && isCorrectDataType(requireDataType, cipherColumn, cipherDataType) && isCorrectDataType(requireDataType, assistedQueryColumn, assistedQueryDataType);
    }
    
    private boolean isCorrectDataType(final boolean requireDataType, final String field, final String fieldDataType) {
        boolean noDataTypeRequired = !requireDataType && Strings.isNullOrEmpty(fieldDataType);
        boolean requireDataTypeAndFieldExisted = requireDataType && !Strings.isNullOrEmpty(field) && !Strings.isNullOrEmpty(fieldDataType);
        boolean requireDataTypeAndFieldNotExisted = requireDataType && Strings.isNullOrEmpty(field) && Strings.isNullOrEmpty(fieldDataType);
        return noDataTypeRequired || requireDataTypeAndFieldExisted || requireDataTypeAndFieldNotExisted;
    }
}
