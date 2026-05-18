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

package org.apache.shardingsphere.test.it.rewriter.engine.parameter;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.test.it.rewriter.engine.type.SQLExecuteType;
import org.apache.shardingsphere.test.it.rewriter.entity.RewriteAssertionEntity;
import org.apache.shardingsphere.test.it.rewriter.entity.RewriteAssertionsRootEntity;
import org.apache.shardingsphere.test.it.rewriter.entity.RewriteOutputEntity;
import org.apache.shardingsphere.test.it.rewriter.loader.RewriteAssertionsRootEntityLoader;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Test parameters for SQL rewrite engine builder.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SQLRewriteEngineTestParametersBuilder {
    
    private static final String COMMA_WITH_SPACE_SEPARATOR = ", ";
    
    private static final String DATABASE_TYPE_PLACEHOLDER = "${databaseType}";
    
    private static final String DATABASE_TYPE_LOWER_PLACEHOLDER = "${databaseTypeLower}";
    
    private static final String HEXTORAW_PARAMETER_PREFIX = "HEXTORAW:";
    
    private static final String ESCAPED_COMMA = "{ESCAPE_COMMA}";
    
    private static final String ESCAPED_COLON = "{ESCAPE_COLON}";
    
    private static final Pattern IFNULL_PARAMETER_COLUMN_PATTERN = Pattern.compile("IFNULL\\s*\\(\\s*([`\\w.]+)\\s*,\\s*\\?", Pattern.CASE_INSENSITIVE);
    
    private static final Pattern DATE_PARAMETER_PATTERN = Pattern.compile("\\d{4}-\\d{2}-\\d{2}");
    
    private static final Pattern DATETIME_PARAMETER_PATTERN = Pattern.compile("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}(\\.\\d{1,6})?");
    
    private static final Pattern TIME_PARAMETER_PATTERN = Pattern.compile("\\d{2}:\\d{2}(:\\d{2}(\\.\\d{1,6})?)?");
    
    /**
     * Load test parameters.
     *
     * @param type type
     * @param path path
     * @param targetClass target class
     * @return test parameters list for SQL rewrite engine
     */
    public static Collection<SQLRewriteEngineTestParameters> loadTestParameters(final String type, final String path, final Class<?> targetClass) {
        Collection<SQLRewriteEngineTestParameters> result = new LinkedList<>();
        for (Entry<String, RewriteAssertionsRootEntity> entry : loadAllRewriteAssertionsRootEntities(type, path, targetClass).entrySet()) {
            result.addAll(createTestParameters(type, entry.getKey(), entry.getValue()));
        }
        return result;
    }
    
    private static Map<String, RewriteAssertionsRootEntity> loadAllRewriteAssertionsRootEntities(final String type, final String path, final Class<?> targetClass) {
        Map<String, RewriteAssertionsRootEntity> result = new LinkedHashMap<>();
        File file = new File(targetClass.getProtectionDomain().getCodeSource().getLocation().getPath() + "/" + path);
        for (File each : Objects.requireNonNull(file.listFiles())) {
            if (each.isFile()) {
                appendFromFile(type, each, path, result);
            } else {
                appendFromDirectory(type, each, path + "/" + each.getName(), result);
            }
        }
        return result;
    }
    
    private static void appendFromDirectory(final String type, final File directory, final String path, final Map<String, RewriteAssertionsRootEntity> result) {
        for (File each : Objects.requireNonNull(directory.listFiles())) {
            if (each.isFile()) {
                appendFromFile(type, each, path, result);
            } else {
                appendFromDirectory(type, each, path + "/" + each.getName(), result);
            }
        }
    }
    
    private static void appendFromFile(final String type, final File file, final String path, final Map<String, RewriteAssertionsRootEntity> result) {
        if (file.getName().endsWith(".xml")) {
            String key = path.toLowerCase().replace(type.toLowerCase() + "/", "") + "/" + file.getName();
            result.put(key, new RewriteAssertionsRootEntityLoader().load(path + "/" + file.getName()));
        }
    }
    
    private static Collection<SQLRewriteEngineTestParameters> createTestParameters(final String type, final String fileName, final RewriteAssertionsRootEntity rootAssertions) {
        Collection<SQLRewriteEngineTestParameters> result = new LinkedList<>();
        for (RewriteAssertionEntity each : rootAssertions.getAssertions()) {
            String ruleFile = Strings.isNullOrEmpty(each.getYamlRule()) ? rootAssertions.getYamlRule() : each.getYamlRule();
            for (String databaseType : getDatabaseTypes(each.getDatabaseTypes())) {
                String actualRuleFile = resolveRuleFile(ruleFile, databaseType);
                boolean success = setSQLIfSQLFileNotNull(each);
                if (success) {
                    appendLiteralCases(type, fileName, actualRuleFile, each, databaseType, result);
                    continue;
                }
                SQLExecuteType sqlExecuteType = null == each.getInput().getParameters() || each.getInput().getParameters().isEmpty() ? SQLExecuteType.LITERAL : SQLExecuteType.PLACEHOLDER;
                result.add(new SQLRewriteEngineTestParameters(type, each.getId(), fileName, actualRuleFile, each.getInput().getSql(),
                        createParameters(each.getInput().getParameters(), each.getInput().getSql()), createOutputSQLs(each.getOutputs()), createOutputGroupedParameters(each.getOutputs()),
                        databaseType, sqlExecuteType));
            }
        }
        return result;
    }
    
    private static String resolveRuleFile(final String ruleFile, final String databaseType) {
        return ruleFile.replace(DATABASE_TYPE_PLACEHOLDER, databaseType).replace(DATABASE_TYPE_LOWER_PLACEHOLDER, databaseType.toLowerCase(Locale.ENGLISH));
    }
    
    private static Collection<String> getDatabaseTypes(final String databaseTypes) {
        return Strings.isNullOrEmpty(databaseTypes) ? getAllDatabaseTypes() : Splitter.on(',').trimResults().splitToList(databaseTypes);
    }
    
    private static Collection<String> getAllDatabaseTypes() {
        return Arrays.asList("MySQL", "PostgreSQL", "Oracle", "SQLServer", "SQL92", "openGauss");
    }
    
    private static boolean setSQLIfSQLFileNotNull(final RewriteAssertionEntity entity) {
        boolean result = false;
        if (null != entity.getInput().getSqlFile()) {
            URL resource = SQLRewriteEngineTestParametersBuilder.class.getClassLoader().getResource(entity.getInput().getSqlFile());
            ShardingSpherePreconditions.checkNotNull(resource, () -> new IllegalArgumentException(String.format("Resource '%s' is not found.", entity.getInput().getSqlFile())));
            entity.getInput().setSql(getSQLFromFilePath(resource));
            result = true;
        }
        for (RewriteOutputEntity each : entity.getOutputs()) {
            if (null != each.getSqlFile()) {
                each.setSql(getSQLFromFilePath(Objects.requireNonNull(SQLRewriteEngineTestParametersBuilder.class.getClassLoader().getResource(each.getSqlFile()))));
            }
        }
        return result;
    }
    
    private static void appendLiteralCases(final String type, final String fileName, final String ruleFile, final RewriteAssertionEntity assertionEntity,
                                           final String databaseType, final Collection<SQLRewriteEngineTestParameters> result) {
        if (null == assertionEntity.getInput().getParameters() || assertionEntity.getInput().getParameters().isEmpty()) {
            result.add(new SQLRewriteEngineTestParameters(type, assertionEntity.getId(), fileName, ruleFile, assertionEntity.getInput().getSql(),
                    createParameters(assertionEntity.getInput().getParameters(), null), createOutputSQLs(assertionEntity.getOutputs()), createOutputGroupedParameters(assertionEntity.getOutputs()),
                    databaseType, SQLExecuteType.LITERAL));
        } else {
            result.add(new SQLRewriteEngineTestParameters(type, assertionEntity.getId(), fileName, ruleFile, assertionEntity.getInput().getSql(),
                    createParameters(assertionEntity.getInput().getParameters(), assertionEntity.getInput().getSql()), createOutputSQLs(assertionEntity.getOutputs()),
                    createOutputGroupedParameters(assertionEntity.getOutputs()), databaseType, SQLExecuteType.PLACEHOLDER));
            result.add(new SQLRewriteEngineTestParameters(type, assertionEntity.getId(), fileName, ruleFile,
                    getLiteralSQL(assertionEntity.getInput().getSql(), assertionEntity.getInput().getParameters()), Collections.emptyList(), createLiteralOutputSQLs(assertionEntity.getOutputs()),
                    createLiteralOutputGroupedParameters(assertionEntity.getOutputs()), databaseType, SQLExecuteType.LITERAL));
        }
    }
    
    private static String getLiteralSQL(final String sql, final String parameters) {
        List<String> params =
                Splitter.on(COMMA_WITH_SPACE_SEPARATOR).omitEmptyStrings().trimResults().splitToList(parameters).stream().map(SQLRewriteEngineTestParametersBuilder::createLiteralParameter)
                        .collect(Collectors.toList());
        return params.isEmpty() ? sql : String.format(sql.replace("%", "ÿ").replace("?", "%s"), params.toArray()).replace("ÿ", "%").replace("%%", "%").replace("'%'", "'%%'");
    }
    
    private static String createLiteralParameter(final String value) {
        String actualValue = replaceEscapedChars(value);
        return actualValue.startsWith(HEXTORAW_PARAMETER_PREFIX) ? String.format("HEXTORAW('%s')", actualValue.substring(HEXTORAW_PARAMETER_PREFIX.length())) : "'" + actualValue + "'";
    }
    
    @SneakyThrows({IOException.class, URISyntaxException.class})
    private static String getSQLFromFilePath(final URL fileURL) {
        return Files.readAllLines(Paths.get(fileURL.toURI())).stream().collect(Collectors.joining(System.lineSeparator()));
    }
    
    private static List<Object> createParameters(final String inputParams, final String sql) {
        if (null == inputParams) {
            return Collections.emptyList();
        }
        List<String> inputParamValues = splitParameters(inputParams);
        List<String> ifNullColumnNames = getIfNullColumnNames(sql);
        List<Object> result = new ArrayList<>(inputParamValues.size());
        for (int i = 0; i < inputParamValues.size(); i++) {
            result.add(createInputParameter(inputParamValues.get(i), i < ifNullColumnNames.size() ? ifNullColumnNames.get(i) : null));
        }
        return result;
    }
    
    private static Object createInputParameter(final String inputParam, final String ifNullColumnName) {
        String actualInputParam = replaceEscapedChars(inputParam);
        if ("NULL".equalsIgnoreCase(actualInputParam)) {
            return null;
        }
        if (NumberUtils.isCreatable(actualInputParam) && null != ifNullColumnName) {
            String actualColumnName = ifNullColumnName.toLowerCase();
            if (actualColumnName.contains("_decimal") || actualColumnName.contains("_numeric")) {
                return new BigDecimal(actualInputParam);
            }
            if (actualColumnName.contains("_float") || actualColumnName.contains("_real")) {
                return Float.parseFloat(actualInputParam);
            }
            if (actualColumnName.contains("_double")) {
                return Double.parseDouble(actualInputParam);
            }
        }
        if (DATETIME_PARAMETER_PATTERN.matcher(actualInputParam).matches()) {
            return Timestamp.valueOf(actualInputParam);
        }
        if (DATE_PARAMETER_PATTERN.matcher(actualInputParam).matches()) {
            return Date.valueOf(LocalDate.parse(actualInputParam));
        }
        if (TIME_PARAMETER_PATTERN.matcher(actualInputParam).matches()) {
            return LocalTime.parse(actualInputParam);
        }
        if (NumberUtils.isCreatable(actualInputParam) && actualInputParam.contains(".")) {
            return new BigDecimal(actualInputParam);
        }
        if (NumberUtils.isCreatable(actualInputParam)) {
            return NumberUtils.createNumber(actualInputParam);
        }
        return actualInputParam;
    }
    
    private static List<String> splitParameters(final String parameters) {
        return null == parameters ? Collections.emptyList() : Splitter.on(",").omitEmptyStrings().trimResults().splitToList(parameters);
    }
    
    private static List<String> getIfNullColumnNames(final String sql) {
        if (null == sql) {
            return Collections.emptyList();
        }
        List<String> result = new LinkedList<>();
        Matcher matcher = IFNULL_PARAMETER_COLUMN_PATTERN.matcher(sql);
        while (matcher.find()) {
            result.add(matcher.group(1).replace("`", ""));
        }
        return result;
    }
    
    private static String replaceEscapedChars(final String value) {
        return value.replace(ESCAPED_COMMA, ",").replace(ESCAPED_COLON, ":");
    }
    
    private static List<String> createOutputSQLs(final List<RewriteOutputEntity> outputs) {
        List<String> result = new ArrayList<>(outputs.size());
        for (RewriteOutputEntity each : outputs) {
            result.add(each.getSql());
        }
        return result;
    }
    
    private static List<String> createLiteralOutputSQLs(final List<RewriteOutputEntity> outputs) {
        List<String> result = new ArrayList<>(outputs.size());
        for (RewriteOutputEntity each : outputs) {
            result.add(getLiteralSQL(each.getSql(), each.getParameters()));
        }
        return result;
    }
    
    private static List<List<Object>> createOutputGroupedParameters(final List<RewriteOutputEntity> outputs) {
        List<List<Object>> result = new ArrayList<>(outputs.size());
        for (RewriteOutputEntity each : outputs) {
            result.add(createParameters(each.getParameters(), null));
        }
        return result;
    }
    
    private static List<List<Object>> createLiteralOutputGroupedParameters(final List<RewriteOutputEntity> outputs) {
        List<List<Object>> result = new ArrayList<>(outputs.size());
        outputs.forEach(each -> result.add(Collections.emptyList()));
        return result;
    }
}
