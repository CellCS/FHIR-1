/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.linuxforhealth.fhir.term.service.test;

import static org.linuxforhealth.fhir.term.util.ValueSetSupport.getValueSet;

import org.linuxforhealth.fhir.model.resource.ValueSet;
import org.linuxforhealth.fhir.term.service.FHIRTermService;

public class ExpansionPerformanceTest {
    public static final int ITERATIONS = 1000000;

    public static void main(String[] args) {
        long start = System.currentTimeMillis();

        ValueSet valueSet = getValueSet("http://example.com/fhir/ValueSet/vs4|1.0.0");

        for (int i = 0; i < ITERATIONS; i++) {
            FHIRTermService.getInstance().expand(valueSet);
        }

        long end = System.currentTimeMillis();

        System.out.println("Processing time for " + ITERATIONS + " iterations: " + (end - start) + " milliseconds");
    }
}
