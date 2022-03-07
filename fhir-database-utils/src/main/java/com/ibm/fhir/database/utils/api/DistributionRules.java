/*
 * (C) Copyright IBM Corp. 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */
 
package com.ibm.fhir.database.utils.api;


/**
 * Rules for distributing a table in a distributed RDBMS such as Citus
 */
public class DistributionRules {

    // If this table is distributed, which column is used for sharding
    private final String distributionColumn;
    // Is this table a reference table
    private final boolean referenceTable;

    /**
     * Public constructor
     * @param distributionColumn
     * @param referenceTable
     */
    public DistributionRules(String distributionColumn, boolean referenceTable) {
        this.distributionColumn = distributionColumn;
        this.referenceTable = referenceTable;
        if (this.referenceTable && this.distributionColumn != null) {
            // variables are mutually exclusive
            throw new IllegalArgumentException("Reference tables do not use a distributionColumn");
        }
    }

    /**
     * Getter for distributionColumn value
     * @return
     */
    public String getDistributionColumn() {
        return this.distributionColumn;
    }

    /**
     * Getter for referenceTableValue
     * @return
     */
    public boolean isReferenceTable() {
        return this.referenceTable;
    }
}