/*
 * (C) Copyright IBM Corp. 2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.fhir.term.service.provider;

import static com.ibm.fhir.core.util.LRUCache.createLRUCache;
import static com.ibm.fhir.model.type.String.string;
import static com.ibm.fhir.model.util.FHIRUtil.STRING_DATA_ABSENT_REASON_UNKNOWN;
import static com.ibm.fhir.term.util.CodeSystemSupport.findConcept;
import static com.ibm.fhir.term.util.CodeSystemSupport.getCodeSystem;
import static com.ibm.fhir.term.util.CodeSystemSupport.getConcepts;
import static com.ibm.fhir.term.util.ValueSetSupport.getContains;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.ibm.fhir.model.resource.CodeSystem;
import com.ibm.fhir.model.resource.CodeSystem.Concept;
import com.ibm.fhir.model.resource.ConceptMap;
import com.ibm.fhir.model.resource.ConceptMap.Group;
import com.ibm.fhir.model.resource.ConceptMap.Group.Element;
import com.ibm.fhir.model.resource.ValueSet;
import com.ibm.fhir.model.resource.ValueSet.Expansion;
import com.ibm.fhir.model.resource.ValueSet.Expansion.Contains;
import com.ibm.fhir.model.type.Boolean;
import com.ibm.fhir.model.type.Code;
import com.ibm.fhir.model.type.CodeableConcept;
import com.ibm.fhir.model.type.Coding;
import com.ibm.fhir.model.type.Uri;
import com.ibm.fhir.model.type.code.CodeSystemHierarchyMeaning;
import com.ibm.fhir.model.type.code.ConceptSubsumptionOutcome;
import com.ibm.fhir.term.spi.ExpansionParameters;
import com.ibm.fhir.term.spi.FHIRTermServiceProvider;
import com.ibm.fhir.term.spi.LookupOutcome;
import com.ibm.fhir.term.spi.LookupOutcome.Designation;
import com.ibm.fhir.term.spi.LookupOutcome.Property;
import com.ibm.fhir.term.spi.LookupParameters;
import com.ibm.fhir.term.spi.TranslationOutcome;
import com.ibm.fhir.term.spi.TranslationOutcome.Match;
import com.ibm.fhir.term.spi.TranslationParameters;
import com.ibm.fhir.term.spi.ValidationOutcome;
import com.ibm.fhir.term.spi.ValidationParameters;
import com.ibm.fhir.term.util.ConceptMapSupport;
import com.ibm.fhir.term.util.ValueSetSupport;

/**
 * Default implementation of the FHIRTermServiceProvider interface using CodeSystemSupport, ConceptMapSupport, and ValueSetSupport
 */
public class DefaultTermServiceProvider implements FHIRTermServiceProvider {
    private static final Logger log = Logger.getLogger(DefaultTermServiceProvider.class.getName());

    private static final String VERSION_UNKNOWN = "<version unknown>";
    private static final Map<String, Map<String, Set<String>>> CODE_SET_MAP_CACHE = createLRUCache(1024);

    @Override
    public boolean isExpandable(ValueSet valueSet) {
        return ValueSetSupport.isExpandable(valueSet);
    }

    @Override
    public ValueSet expand(ValueSet valueSet, ExpansionParameters parameters) {
        return ValueSetSupport.expand(valueSet);
    }

    @Override
    public LookupOutcome lookup(Coding coding, LookupParameters parameters) {
        Uri system = coding.getSystem();
        Code code = coding.getCode();
        if (system != null && code != null) {
            String version = (coding.getVersion() != null) ? coding.getVersion().getValue() : null;
            String url = (version != null) ? system.getValue() + "|" + version : system.getValue();
            CodeSystem codeSystem = getCodeSystem(url);
            if (codeSystem != null) {
                return lookup(codeSystem, code);
            }
        }
        return null;
    }

    private LookupOutcome lookup(CodeSystem codeSystem, Code code) {
        Concept concept = findConcept(codeSystem, code);
        if (concept != null) {
            return LookupOutcome.builder()
                    .name((codeSystem.getName() != null) ? codeSystem.getName() : STRING_DATA_ABSENT_REASON_UNKNOWN)
                    .version(codeSystem.getVersion())
                    .display((concept.getDisplay() != null) ? concept.getDisplay() : STRING_DATA_ABSENT_REASON_UNKNOWN)
                    .property(concept.getProperty().stream()
                        .map(property -> Property.builder()
                            .code(property.getCode())
                            .value(property.getValue())
                            .build())
                        .collect(Collectors.toList()))
                    .designation(concept.getDesignation().stream()
                        .map(designation -> Designation.builder()
                            .language(designation.getLanguage())
                            .use(designation.getUse())
                            .value(designation.getValue())
                            .build())
                        .collect(Collectors.toList()))
                    .build();
        }
        return null;
    }

    @Override
    public ConceptSubsumptionOutcome subsumes(Coding codingA, Coding codingB) {
        Uri systemA = codingA.getSystem();
        String versionA = (codingA.getVersion() != null) ? codingA.getVersion().getValue() : null;
        Code codeA = codingA.getCode();

        Uri systemB = codingB.getSystem();
        String versionB = (codingB.getVersion() != null) ? codingB.getVersion().getValue() : null;
        Code codeB = codingB.getCode();

        if (systemA != null && systemB != null && codeA != null && codeB != null && systemA.equals(systemB)) {
            String url = systemA.getValue();

            if (versionA != null || versionB != null) {
                if (versionA != null && versionB != null && !versionA.equals(versionB)) {
                    return null;
                }
                url = (versionA != null) ? (url + "|" + versionA) : (url + "|" + versionB);
            }

            CodeSystem codeSystem = getCodeSystem(url);
            if (codeSystem != null && CodeSystemHierarchyMeaning.IS_A.equals(codeSystem.getHierarchyMeaning())) {
                Concept conceptA = findConcept(codeSystem, codeA);
                if (conceptA != null) {
                    Concept conceptB = findConcept(conceptA, codeB);
                    if (conceptB != null) {
                        return conceptA.equals(conceptB) ? ConceptSubsumptionOutcome.EQUIVALENT : ConceptSubsumptionOutcome.SUBSUMES;
                    }
                    conceptB = findConcept(codeSystem, codeB);
                    if (conceptB != null) {
                        conceptA = findConcept(conceptB, codeA);
                        return (conceptA != null) ? ConceptSubsumptionOutcome.SUBSUMED_BY : ConceptSubsumptionOutcome.NOT_SUBSUMED;
                    }
                }
            }
        }

        return null;
    }

    @Override
    public Set<Concept> closure(Coding coding) {
        Uri system = coding.getSystem();
        String version = (coding.getVersion() != null) ? coding.getVersion().getValue() : null;
        Code code = coding.getCode();

        if (system != null && code != null) {
            String url = (version != null) ? system.getValue() + "|" + version : system.getValue();
            CodeSystem codeSystem = getCodeSystem(url);
            if (codeSystem != null && CodeSystemHierarchyMeaning.IS_A.equals(codeSystem.getHierarchyMeaning())) {
                Concept concept = findConcept(codeSystem, code);
                if (concept != null) {
                    return getConcepts(concept);
                }
            }
        }

        return Collections.emptySet();
    }

    @Override
    public ValidationOutcome validateCode(CodeSystem codeSystem, Coding coding, ValidationParameters parameters) {
        LookupOutcome outcome = lookup(codeSystem, coding.getCode());
        return buildValidationOutcome(coding, (outcome != null), outcome);
    }

    @Override
    public ValidationOutcome validateCode(CodeSystem codeSystem, CodeableConcept codeableConcept, ValidationParameters parameters) {
        for (Coding coding : codeableConcept.getCoding()) {
            ValidationOutcome outcome = validateCode(codeSystem, coding);
            if (Boolean.TRUE.equals(outcome.getResult())) {
                return outcome;
            }
        }
        return buildValidationOutcome(null, false, null);
    }

    @Override
    public ValidationOutcome validateCode(ValueSet valueSet, Coding coding, ValidationParameters parameters) {
        boolean result = validateCode(getCodeSetMap(valueSet), coding);
        LookupOutcome outcome = result ? lookup(coding) : null;
        return buildValidationOutcome(coding, result, outcome);
    }

    @Override
    public ValidationOutcome validateCode(ValueSet valueSet, CodeableConcept codeableConcept, ValidationParameters parameters) {
        Map<String, Set<String>> codeSetMap = getCodeSetMap(valueSet);
        for (Coding coding : codeableConcept.getCoding()) {
            boolean result = validateCode(codeSetMap, coding);
            if (result) {
                LookupOutcome outcome = lookup(coding);
                return buildValidationOutcome(coding, result, outcome);
            }
        }
        return buildValidationOutcome(null, false, null);
    }

    @Override
    public TranslationOutcome translate(ConceptMap conceptMap, Coding coding, TranslationParameters parameters) {
        List<Match> match = ConceptMapSupport.translate(conceptMap, coding).stream()
                .map(group -> match(getSource(conceptMap), group))
                .flatMap(List::stream)
                .collect(Collectors.toList());
        return TranslationOutcome.builder()
                .result(!match.isEmpty() ? Boolean.TRUE : Boolean.FALSE)
                .message(match.isEmpty() ? string("No matches found") : null)
                .match(match)
                .build();
    }

    @Override
    public TranslationOutcome translate(ConceptMap conceptMap, CodeableConcept codeableConcept, TranslationParameters parameters) {
        for (Coding coding : codeableConcept.getCoding()) {
            TranslationOutcome outcome = translate(conceptMap, coding);
            if (Boolean.TRUE.equals(outcome.getResult())) {
                return outcome;
            }
        }
        return TranslationOutcome.builder()
                .result(Boolean.FALSE)
                .message(string("No matches found"))
                .build();
    }

    private ValidationOutcome buildValidationOutcome(Coding coding, boolean result, LookupOutcome outcome) {
        String message = null;
        if (!result && coding != null && coding.getCode() != null) {
            message = String.format("Code '%s' is invalid", coding.getCode().getValue());
        }
        if (result && outcome != null && coding != null && outcome.getDisplay() != null && coding.getDisplay() != null && !outcome.getDisplay().equals(coding.getDisplay())) {
            message = String.format("The display '%s' is incorrect for code '%s' from code system '%s'", coding.getDisplay().getValue(), coding.getCode().getValue(), coding.getSystem().getValue());
            result = false;
        }
        return ValidationOutcome.builder()
                .result(result ? Boolean.TRUE : Boolean.FALSE)
                .message((message != null) ? string(message) : null)
                .display((outcome != null) ? outcome.getDisplay() : null)
                .build();
    }

    private boolean validateCode(Map<String, Set<String>> codeSetMap, Coding coding) {
        String system = (coding.getSystem() != null) ? coding.getSystem().getValue() : null;
        String version = (coding.getVersion() != null) ? coding.getVersion().getValue() : null;
        String code = (coding.getCode() != null) ? coding.getCode().getValue() : null;
        return validateCode(codeSetMap, system, version, code);
    }

    /**
     * Determine whether the provided code is in the codeSet associated with the provided system and version.
     *
     * <p>If the system and version are non-null, then they are concatenated to form a key into the codeSetMap. If
     * not found, then the system is concatenated with the "VERSION_UNKNOWN" constant (in cases where the expanded
     * value set did not have a version available during the expansion). If only the system is non-null, then the
     * codeSetMap keys are checked for startsWith(system). Finally, if both system and version are null, map keys
     * are ignored and the values of the map are checked directly.
     *
     * @param codeSetMap
     *     the code set map
     * @param system
     *     the system of the focal coded element (can be null)
     * @param version
     *     the version of the focal coded element (can be null)
     * @param code
     *     the code used in the membership check
     * @return
     *     true if a codeSet is found and the provided code is a member of that codeSet, false otherwise
     */
    private boolean validateCode(Map<String, Set<String>> codeSetMap, String system, String version, String code) {
        if (system != null && version != null) {
            Set<String> codeSet = codeSetMap.get(system + "|" + version);
            if (codeSet != null) {
                if (codeSet.contains(code)) {
                    return true;
                } else {
                    codeSet = codeSetMap.get(system + "|" + VERSION_UNKNOWN);
                    if (codeSet != null) {
                        return codeSet.contains(code);
                    }
                }
            }
        } else if (system != null) {
            String prefix = system + "|";
            for (String key : codeSetMap.keySet()) {
                if (key.startsWith(prefix)) {
                    return codeSetMap.get(key).contains(code);
                }
            }
        } else {
            for (Set<String> codeSet : codeSetMap.values()) {
                if (codeSet.contains(code)) {
                    return true;
                }
            }
        }
        return false;
    }

    private Map<String, Set<String>> getCodeSetMap(ValueSet valueSet) {
        if (valueSet.getUrl() == null || valueSet.getVersion() == null) {
            return computeCodeSetMap(valueSet);
        }
        String url = valueSet.getUrl().getValue() + "|" + valueSet.getVersion().getValue();
        return CODE_SET_MAP_CACHE.computeIfAbsent(url, k -> computeCodeSetMap(valueSet));
    }

    private Map<String, Set<String>> computeCodeSetMap(ValueSet valueSet) {
        try {
            ValueSet expanded = expand(valueSet);
            if (expanded == null || expanded.getExpansion() == null) {
                return Collections.emptyMap();
            }
            Map<String, Set<String>> codeSetMap = new LinkedHashMap<>();
            Expansion expansion = expanded.getExpansion();
            for (Contains contains : getContains(expansion)) {
                String system = (contains.getSystem() != null) ? contains.getSystem().getValue() : null;
                String version = (contains.getVersion() != null && contains.getVersion().getValue() != null) ? contains.getVersion().getValue() : VERSION_UNKNOWN;
                String code = (contains.getCode() != null) ? contains.getCode().getValue() : null;
                if (system != null && code != null) {
                    codeSetMap.computeIfAbsent(system + "|" + version, k -> new LinkedHashSet<>()).add(code);
                }
            }
            return codeSetMap;
        } catch (Exception e) {
            String url = (valueSet.getUrl() != null) ? valueSet.getUrl().getValue() : "<no url>";
            String version = (valueSet.getVersion() != null) ? valueSet.getVersion().getValue() : "<no version>";
            log.log(Level.WARNING, String.format("Unable to expand value set with url: %s and version: %s", url, version), e);
        }
        return Collections.emptyMap();
    }

    private List<Match> match(Uri source, Group group) {
        return group.getElement().stream()
                .map(element -> match(source, group.getTarget(), group.getTargetVersion(), element))
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    private List<Match> match(Uri source, Uri system, com.ibm.fhir.model.type.String version, Element element) {
        return element.getTarget().stream()
                .map(target -> Match.builder()
                    .equivalence(target.getEquivalence())
                    .concept(Coding.builder()
                        .system(system)
                        .version(version)
                        .code(target.getCode())
                        .display(target.getDisplay())
                        .build())
                    .source(source)
                    .build())
                .collect(Collectors.toList());
    }

    private Uri getSource(ConceptMap conceptMap) {
        StringBuilder sb = new StringBuilder();
        sb.append(conceptMap.getUrl().getValue());
        if (conceptMap.getVersion() != null) {
            sb.append("|").append(conceptMap.getVersion().getValue());
        }
        return Uri.of(sb.toString());
    }
}