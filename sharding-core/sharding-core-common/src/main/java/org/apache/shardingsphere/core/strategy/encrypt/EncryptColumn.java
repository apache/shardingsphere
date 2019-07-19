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

package org.apache.shardingsphere.core.strategy.encrypt;

import com.google.common.base.Optional;
import com.google.common.base.Strings;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Encrypt column.
 *
 * @author panjuan
 */
@RequiredArgsConstructor
@Getter
public final class EncryptColumn {
    
    private final String plainColumn;
    
    private final String cipherColumn;
    
    private final String assistedQueryColumn;
    
    private final String encryptor;
    
    /**
     * Get assisted query column.
     * 
     * @return assisted query column
     */
    public Optional<String> getAssistedQueryColumn() {
        return Strings.isNullOrEmpty(assistedQueryColumn) ? Optional.<String>absent() : Optional.of(assistedQueryColumn);
    }
    
    /**
     * Get plain column.
     *
     * @return plain column
     */
    public Optional<String> getPlainColumn() {
        return Strings.isNullOrEmpty(plainColumn) ? Optional.<String>absent() : Optional.of(plainColumn);
    }
}
