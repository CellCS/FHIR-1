/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.linuxforhealth.fhir.persistence.jdbc.domain;

import org.linuxforhealth.fhir.persistence.exception.FHIRPersistenceException;
import org.linuxforhealth.fhir.search.parameters.QueryParameter;

/**
 * A string search parameter
 */
public class StringSearchParam extends SearchParam {

    /**
     * Public constructor
     * @param rootResourceType
     * @param name
     * @param queryParameter the search query parameter being wrapped
     */
    public StringSearchParam(String rootResourceType, String name, QueryParameter queryParameter) {
        super(rootResourceType, name, queryParameter);
    }

    @Override
    public <T> T visit(T queryData, SearchQueryVisitor<T> visitor) throws FHIRPersistenceException {
        return visitor.addStringParam(queryData, getRootResourceType(), getQueryParameter());
    }
}