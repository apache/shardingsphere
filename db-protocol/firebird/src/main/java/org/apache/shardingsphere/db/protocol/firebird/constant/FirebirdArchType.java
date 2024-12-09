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

package org.apache.shardingsphere.db.protocol.firebird.constant;

import com.google.common.base.Preconditions;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.SystemUtils;
import org.apache.groovy.util.SystemUtil;
import org.apache.shardingsphere.db.protocol.firebird.constant.protocol.FirebirdProtocolVersion;
import org.apache.shardingsphere.infra.exception.generic.UnsupportedSQLOperationException;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

@RequiredArgsConstructor
@Getter
public enum FirebirdArchType {

    ARCH_GENERIC(1, ""),	// Generic -- always use canonical forms
    ARCH_SUN(3, "SO"),
    ARCH_SUN4(8, "S4"),
    ARCH_SUNX86(9, "SI"),
    ARCH_HPUX(10, "HU"),
    ARCH_RT(14, "PA"),
    ARCH_INTEL_32(29, "WI"),	// generic Intel chip w/32-bit compilation
    ARCH_LINUX_LOONG(36, "LL"), //only for different identifier in ARCHITECTURE
    ARCH_LINUX(36, "LI"),
    ARCH_FREEBSD(37, "FB"),
    ARCH_NETBSD(38, "NB"),
    ARCH_DARWIN_PPC(39, "UP"),
    ARCH_WINNT_64(40, "WI"),
    ARCH_DARWIN_X64_ARM(41, "UA"), //only for different identifier in ARCHITECTURE
    ARCH_DARWIN_X64(41, "UI"),
    ARCH_DARWIN_PPC64(42, "UP"),
    ARCH_ARM(43, "NP"),
    ARCH_MAX(44, "");	// Keep this at the end

    private static final Map<Integer, FirebirdArchType> FIREBIRD_ARCH_TYPE_CACHE = new HashMap<>();
    public static final FirebirdArchType ARCHITECTURE;

    private final int code;
    private final String identifier;

    static {
        for (FirebirdArchType each : values()) {
            FIREBIRD_ARCH_TYPE_CACHE.put(each.code, each);
        }
        //setup user architecture
        if (SystemUtils.IS_OS_SUN_OS) {
            switch (SystemUtils.OS_ARCH.toLowerCase()) {
                case "sparc":
                    ARCHITECTURE = ARCH_SUN4;
                    break;
                case "i386":
                case "amd64":
                    ARCHITECTURE = ARCH_SUNX86;
                    break;
                default:
                    ARCHITECTURE = ARCH_SUN;
            }
        } else if (SystemUtils.IS_OS_HP_UX) {
            ARCHITECTURE = ARCH_HPUX;
        } else if (SystemUtils.IS_OS_AIX) {
            ARCHITECTURE = ARCH_RT;
        } else if (SystemUtils.IS_OS_LINUX) {
            if(!SystemUtils.OS_ARCH.toLowerCase().contains("loong")) {
                ARCHITECTURE = ARCH_LINUX;
            } else {
                ARCHITECTURE = ARCH_LINUX_LOONG;
            }
        } else if (SystemUtils.IS_OS_FREE_BSD) {
            ARCHITECTURE = ARCH_FREEBSD;
        } else if (SystemUtils.IS_OS_NET_BSD) {
            ARCHITECTURE = ARCH_NETBSD;
        } else if (SystemUtils.IS_OS_MAC || SystemUtils.OS_NAME.toLowerCase().contains("darwin")) {
            switch (SystemUtils.OS_ARCH.toLowerCase()) {
                case "ppc":
                case "powerpc":
                    ARCHITECTURE = ARCH_DARWIN_PPC;
                    break;
                case "ppc64":
                    ARCHITECTURE = ARCH_DARWIN_PPC64;
                    break;
                case "arm":
                case "aarch64":
                case "aarch32":
                    ARCHITECTURE = ARCH_DARWIN_X64_ARM;
                    break;
                default:
                    ARCHITECTURE = ARCH_DARWIN_X64;
            }
        } else if (SystemUtils.IS_OS_WINDOWS && SystemUtils.OS_ARCH.equalsIgnoreCase("amd64")) {
            ARCHITECTURE = ARCH_WINNT_64;
        } else if (SystemUtils.OS_ARCH.equalsIgnoreCase("i386")) {
            ARCHITECTURE = ARCH_INTEL_32;
        } else if (Stream.of("arm", "aarch64", "aarch32").anyMatch(SystemUtils.OS_ARCH::equalsIgnoreCase)) {
            ARCHITECTURE = ARCH_ARM;
        } else {
            ARCHITECTURE = ARCH_GENERIC;
        }
    }

    /**
     * Value of.
     *
     * @param code arch type code
     * @return Firebird arch type
     */
    public static FirebirdArchType valueOf(final int code) {
        FirebirdArchType result = FIREBIRD_ARCH_TYPE_CACHE.get(code);
        Preconditions.checkNotNull(result, "Cannot find '%d' in arch type", code);
        return result;
    }

    /**
     * Determines if the architecture is valid
     *
     * @param arch architecture type
     * @return is the architecture valid
     */
    public static boolean isValid(final FirebirdArchType arch) {
        return arch == FirebirdArchType.ARCH_GENERIC || arch == FirebirdArchType.ARCHITECTURE;
    }
}
