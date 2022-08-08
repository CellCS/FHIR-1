/*
 * (C) Copyright IBM Corp. 2016,2019
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.linuxforhealth.fhir.persistence;

import java.util.Properties;

import org.linuxforhealth.fhir.persistence.exception.FHIRPersistenceException;

public interface PropertyBasedFHIRPersistenceFactory {

    FHIRPersistence getInstance(Properties properties) throws FHIRPersistenceException;
    
}
