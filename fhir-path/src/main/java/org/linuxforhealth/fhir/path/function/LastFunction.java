/*
 * (C) Copyright IBM Corp. 2019
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.linuxforhealth.fhir.path.function;

import static org.linuxforhealth.fhir.path.util.FHIRPathUtil.empty;
import static org.linuxforhealth.fhir.path.util.FHIRPathUtil.isUnordered;
import static org.linuxforhealth.fhir.path.util.FHIRPathUtil.singleton;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.linuxforhealth.fhir.path.FHIRPathNode;
import org.linuxforhealth.fhir.path.evaluator.FHIRPathEvaluator.EvaluationContext;

public class LastFunction extends FHIRPathAbstractFunction {
    @Override
    public String getName() {
        return "last";
    }

    @Override
    public int getMinArity() {
        return 0;
    }

    @Override
    public int getMaxArity() {
        return 0;
    }

    @Override
    public Collection<FHIRPathNode> apply(EvaluationContext evaluationContext, Collection<FHIRPathNode> context, List<Collection<FHIRPathNode>> arguments) {
        if (isUnordered(context)) {
            throw new IllegalArgumentException("Context must be an ordered collection for function: 'last'");
        }
        if (!context.isEmpty()) {
            List<?> list = (context instanceof List) ? (List<?>) context : new ArrayList<>(context);
            return singleton((FHIRPathNode) list.get(list.size() - 1));
        }
        return empty();
    }
}
