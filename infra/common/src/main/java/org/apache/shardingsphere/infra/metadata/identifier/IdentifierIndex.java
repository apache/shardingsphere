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

package org.apache.shardingsphere.infra.metadata.identifier;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.database.connector.core.metadata.database.enums.QuoteCharacter;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierCasePolicy;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierScope;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.LookupMode;
import org.apache.shardingsphere.infra.exception.kernel.metadata.AmbiguousIdentifierException;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

/**
 * Identifier index for metadata objects.
 *
 * @param <T> metadata object type
 */
@Slf4j
@RequiredArgsConstructor
public final class IdentifierIndex<T> {
    
    private final DatabaseIdentifierContext databaseIdentifierContext;
    
    private final IdentifierScope identifierScope;
    
    private volatile Snapshot<T> snapshot = Snapshot.empty();
    
    /**
     * Rebuild identifier index by actual names.
     *
     * @param values actual name and metadata object map
     */
    public synchronized void rebuild(final Map<String, T> values) {
        Map<String, T> newExactValues = new LinkedHashMap<>(values.size(), 1F);
        Map<String, Collection<String>> newNormalizedIdentifiers = new LinkedHashMap<>(values.size(), 1F);
        IdentifierCasePolicy policy = databaseIdentifierContext.getMetaDataPolicy(identifierScope);
        for (Entry<String, T> entry : values.entrySet()) {
            newExactValues.put(entry.getKey(), entry.getValue());
            addNormalizedIdentifier(newNormalizedIdentifiers, policy, entry.getKey());
        }
        snapshot = createSnapshot(newExactValues, newNormalizedIdentifiers, policy);
    }
    
    /**
     * Get all metadata objects.
     *
     * @return all metadata objects
     */
    public Collection<T> getAll() {
        return snapshot.getExactValues().values();
    }
    
    /**
     * Get all actual names.
     *
     * @return all actual names
     */
    public Collection<String> getAllNames() {
        return snapshot.getExactValues().keySet();
    }
    
    /**
     * Put metadata object by actual name.
     *
     * @param name actual name
     * @param value metadata object
     */
    public synchronized void put(final String name, final T value) {
        Snapshot<T> currentSnapshot = snapshot;
        Map<String, T> newExactValues = new LinkedHashMap<>(currentSnapshot.getExactValues());
        Map<String, Collection<String>> newNormalizedIdentifiers = copyNormalizedIdentifiers(currentSnapshot.getNormalizedIdentifierNames());
        IdentifierCasePolicy policy = databaseIdentifierContext.getMetaDataPolicy(identifierScope);
        if (newExactValues.containsKey(name)) {
            removeNormalizedIdentifier(newNormalizedIdentifiers, policy, name);
        }
        newExactValues.put(name, value);
        addNormalizedIdentifier(newNormalizedIdentifiers, policy, name);
        snapshot = createSnapshot(newExactValues, newNormalizedIdentifiers, policy);
    }
    
    /**
     * Remove metadata object by actual name.
     *
     * @param name actual name
     * @return removed metadata object
     */
    public synchronized T remove(final String name) {
        Snapshot<T> currentSnapshot = snapshot;
        if (!currentSnapshot.getExactValues().containsKey(name)) {
            return null;
        }
        Map<String, T> newExactValues = new LinkedHashMap<>(currentSnapshot.getExactValues());
        Map<String, Collection<String>> newNormalizedIdentifiers = copyNormalizedIdentifiers(currentSnapshot.getNormalizedIdentifierNames());
        IdentifierCasePolicy policy = databaseIdentifierContext.getMetaDataPolicy(identifierScope);
        T result = newExactValues.remove(name);
        removeNormalizedIdentifier(newNormalizedIdentifiers, policy, name);
        snapshot = createSnapshot(newExactValues, newNormalizedIdentifiers, policy);
        return result;
    }
    
    /**
     * Judge whether identifier index is empty or not.
     *
     * @return identifier index is empty or not
     */
    public boolean isEmpty() {
        return snapshot.getExactValues().isEmpty();
    }
    
    /**
     * Get size of identifier index.
     *
     * @return size of identifier index
     */
    public int size() {
        return snapshot.getExactValues().size();
    }
    
    /**
     * Judge whether contains metadata object by unquoted identifier or not.
     *
     * @param identifier unquoted identifier
     * @return contains metadata object by unquoted identifier or not
     */
    public boolean contains(final String identifier) {
        return null != get(identifier);
    }
    
    /**
     * Get metadata object by unquoted identifier.
     *
     * @param identifier unquoted identifier
     * @return matched metadata object
     */
    public T get(final String identifier) {
        Snapshot<T> currentSnapshot = snapshot;
        IdentifierCasePolicy policy = databaseIdentifierContext.getMetaDataPolicy(identifierScope);
        if (LookupMode.EXACT == policy.getLookupMode(QuoteCharacter.NONE)) {
            return currentSnapshot.getExactValues().get(identifier);
        }
        return getByNormalizedIdentifier(currentSnapshot, policy, identifier);
    }
    
    /**
     * Find metadata object by identifier value.
     *
     * @param identifierValue identifier value
     * @return matched metadata object
     */
    public Optional<T> find(final IdentifierValue identifierValue) {
        Snapshot<T> currentSnapshot = snapshot;
        IdentifierCasePolicy policy = databaseIdentifierContext.getMetaDataPolicy(identifierScope);
        if (LookupMode.EXACT == policy.getLookupMode(identifierValue.getQuoteCharacter())) {
            return Optional.ofNullable(currentSnapshot.getExactValues().get(identifierValue.getValue()));
        }
        return QuoteCharacter.NONE == identifierValue.getQuoteCharacter()
                ? Optional.ofNullable(getByNormalizedIdentifier(currentSnapshot, policy, identifierValue.getValue()))
                : findByQuotedNormalizedIdentifier(currentSnapshot, policy, identifierValue);
    }
    
    private T getByNormalizedIdentifier(final Snapshot<T> currentSnapshot, final IdentifierCasePolicy policy, final String identifier) {
        NormalizedBucket<T> normalizedBucket = currentSnapshot.getNormalizedBuckets().get(policy.normalizeForLookup(identifier));
        if (null == normalizedBucket) {
            return null;
        }
        return getByUnquotedNormalizedIdentifier(currentSnapshot, normalizedBucket, identifier);
    }
    
    private T getByUnquotedNormalizedIdentifier(final Snapshot<T> currentSnapshot, final NormalizedBucket<T> normalizedBucket, final String identifier) {
        if (!normalizedBucket.hasUnquotedIdentifier()) {
            return null;
        }
        if (normalizedBucket.hasSingleUnquotedIdentifier()) {
            return normalizedBucket.getSingleUnquotedValue();
        }
        T exactMatchedValue = findExactMatchedValue(currentSnapshot.getExactValues(), normalizedBucket.getUnquotedIdentifiers(), identifier);
        if (null != exactMatchedValue) {
            return exactMatchedValue;
        }
        throw new AmbiguousIdentifierException(identifier, normalizedBucket.getUnquotedIdentifiers());
    }
    
    private Optional<T> findByQuotedNormalizedIdentifier(final Snapshot<T> currentSnapshot, final IdentifierCasePolicy policy, final IdentifierValue identifierValue) {
        NormalizedBucket<T> normalizedBucket = currentSnapshot.getNormalizedBuckets().get(policy.normalizeForLookup(identifierValue.getValue()));
        if (null == normalizedBucket) {
            return Optional.empty();
        }
        if (normalizedBucket.hasSingleIdentifier()) {
            return policy.matches(normalizedBucket.getSingleIdentifier(), identifierValue.getValue(), identifierValue.getQuoteCharacter())
                    ? Optional.ofNullable(normalizedBucket.getSingleValue())
                    : Optional.empty();
        }
        String matchedIdentifier = null;
        Collection<String> ambiguousIdentifiers = null;
        for (String each : normalizedBucket.getIdentifiers()) {
            if (!policy.matches(each, identifierValue.getValue(), identifierValue.getQuoteCharacter())) {
                continue;
            }
            if (null == matchedIdentifier) {
                matchedIdentifier = each;
                continue;
            }
            if (null == ambiguousIdentifiers) {
                ambiguousIdentifiers = new LinkedList<>();
                ambiguousIdentifiers.add(matchedIdentifier);
            }
            ambiguousIdentifiers.add(each);
        }
        if (null == matchedIdentifier) {
            return Optional.empty();
        }
        if (null == ambiguousIdentifiers) {
            return Optional.ofNullable(currentSnapshot.getExactValues().get(matchedIdentifier));
        }
        T exactMatchedValue = findExactMatchedValue(currentSnapshot.getExactValues(), ambiguousIdentifiers, identifierValue.getValue());
        if (null != exactMatchedValue) {
            return Optional.of(exactMatchedValue);
        }
        throw new AmbiguousIdentifierException(identifierValue.getValue(), ambiguousIdentifiers);
    }
    
    private T findExactMatchedValue(final Map<String, T> exactValues, final Collection<String> matchedIdentifiers, final String identifierValue) {
        if (!matchedIdentifiers.contains(identifierValue)) {
            return null;
        }
        log.warn("Identifier '{}' matched multiple actual identifiers {}. Fallback to exact identifier '{}'.", identifierValue, matchedIdentifiers, identifierValue);
        return exactValues.get(identifierValue);
    }
    
    private void addNormalizedIdentifier(final Map<String, Collection<String>> values, final IdentifierCasePolicy policy, final String name) {
        String normalizedName = policy.normalizeForLookup(name);
        values.computeIfAbsent(normalizedName, key -> new LinkedList<>()).add(name);
    }
    
    private void removeNormalizedIdentifier(final Map<String, Collection<String>> values, final IdentifierCasePolicy policy, final String name) {
        String normalizedName = policy.normalizeForLookup(name);
        Collection<String> candidateIdentifiers = values.get(normalizedName);
        if (null == candidateIdentifiers) {
            return;
        }
        candidateIdentifiers.remove(name);
        if (candidateIdentifiers.isEmpty()) {
            values.remove(normalizedName);
        }
    }
    
    private Map<String, Collection<String>> copyNormalizedIdentifiers(final Map<String, Collection<String>> values) {
        Map<String, Collection<String>> result = new LinkedHashMap<>(values.size(), 1F);
        for (Entry<String, Collection<String>> entry : values.entrySet()) {
            result.put(entry.getKey(), new LinkedList<>(entry.getValue()));
        }
        return result;
    }
    
    private Snapshot<T> createSnapshot(final Map<String, T> exactValues, final Map<String, Collection<String>> normalizedIdentifiers, final IdentifierCasePolicy policy) {
        Map<String, Collection<String>> immutableNormalizedIdentifiers = new LinkedHashMap<>(normalizedIdentifiers.size(), 1F);
        Map<String, NormalizedBucket<T>> normalizedBuckets = new LinkedHashMap<>(normalizedIdentifiers.size(), 1F);
        for (Entry<String, Collection<String>> entry : normalizedIdentifiers.entrySet()) {
            Collection<String> identifiers = Collections.unmodifiableCollection(new LinkedList<>(entry.getValue()));
            immutableNormalizedIdentifiers.put(entry.getKey(), identifiers);
            normalizedBuckets.put(entry.getKey(), createNormalizedBucket(exactValues, identifiers, policy));
        }
        return new Snapshot<>(Collections.unmodifiableMap(new LinkedHashMap<>(exactValues)),
                Collections.unmodifiableMap(immutableNormalizedIdentifiers), Collections.unmodifiableMap(normalizedBuckets));
    }
    
    private NormalizedBucket<T> createNormalizedBucket(final Map<String, T> exactValues, final Collection<String> identifiers, final IdentifierCasePolicy policy) {
        String singleIdentifier = null;
        T singleValue = null;
        if (1 == identifiers.size()) {
            singleIdentifier = identifiers.iterator().next();
            singleValue = exactValues.get(singleIdentifier);
        }
        String singleUnquotedIdentifier = null;
        T singleUnquotedValue = null;
        Collection<String> unquotedIdentifiers = null;
        for (String each : identifiers) {
            if (!policy.matches(each, each, QuoteCharacter.NONE)) {
                continue;
            }
            if (null == singleUnquotedIdentifier) {
                singleUnquotedIdentifier = each;
                singleUnquotedValue = exactValues.get(each);
                continue;
            }
            if (null == unquotedIdentifiers) {
                unquotedIdentifiers = new LinkedList<>();
                unquotedIdentifiers.add(singleUnquotedIdentifier);
            }
            unquotedIdentifiers.add(each);
        }
        return new NormalizedBucket<>(singleIdentifier, singleValue, identifiers, singleUnquotedIdentifier,
                singleUnquotedValue, null == unquotedIdentifiers ? null : Collections.unmodifiableCollection(unquotedIdentifiers));
    }
    
    @Override
    public String toString() {
        return snapshot.getExactValues().toString();
    }
    
    @Getter(AccessLevel.PRIVATE)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    private static final class Snapshot<T> {
        
        private static final Snapshot<?> EMPTY = new Snapshot<>(Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap());
        
        private final Map<String, T> exactValues;
        
        private final Map<String, Collection<String>> normalizedIdentifierNames;
        
        private final Map<String, NormalizedBucket<T>> normalizedBuckets;
        
        @SuppressWarnings("unchecked")
        private static <T> Snapshot<T> empty() {
            return (Snapshot<T>) EMPTY;
        }
    }
    
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    private static final class NormalizedBucket<T> {
        
        @Getter(AccessLevel.PRIVATE)
        private final String singleIdentifier;
        
        @Getter(AccessLevel.PRIVATE)
        private final T singleValue;
        
        @Getter(AccessLevel.PRIVATE)
        private final Collection<String> identifiers;
        
        private final String singleUnquotedIdentifier;
        
        @Getter(AccessLevel.PRIVATE)
        private final T singleUnquotedValue;
        
        @Getter(AccessLevel.PRIVATE)
        private final Collection<String> unquotedIdentifiers;
        
        private boolean hasSingleIdentifier() {
            return null != singleIdentifier;
        }
        
        private boolean hasUnquotedIdentifier() {
            return null != singleUnquotedIdentifier;
        }
        
        private boolean hasSingleUnquotedIdentifier() {
            return null == unquotedIdentifiers && null != singleUnquotedIdentifier;
        }
    }
}
