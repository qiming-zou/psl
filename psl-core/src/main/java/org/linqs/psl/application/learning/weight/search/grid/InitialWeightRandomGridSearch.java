/*
 * This file is part of the PSL software.
 * Copyright 2011-2015 University of Maryland
 * Copyright 2013-2020 The Regents of the University of California
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.linqs.psl.application.learning.weight.search.grid;

import org.linqs.psl.application.learning.weight.WeightLearningApplication;
import org.linqs.psl.database.Database;
import org.linqs.psl.model.Model;
import org.linqs.psl.model.rule.Rule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Like InitialWeightGridSearch, but use random grid search instead of exhaustive.
 * See InitialWeightGridSearch for details.
 */
public class InitialWeightRandomGridSearch extends RandomGridSearch {
    private static final Logger log = LoggerFactory.getLogger(InitialWeightRandomGridSearch.class);

    private WeightLearningApplication internalWLA;

    public InitialWeightRandomGridSearch(Model model, WeightLearningApplication internalWLA, Database rvDB, Database observedDB) {
        this(model.getRules(), internalWLA, rvDB, observedDB);
    }

    public InitialWeightRandomGridSearch(List<Rule> rules, WeightLearningApplication internalWLA, Database rvDB, Database observedDB) {
        super(rules, rvDB, observedDB);

        this.internalWLA = internalWLA;
    }

    @Override
    protected void postInitGroundModel() {
        // Init the internal WLA.
        internalWLA.initGroundModel(
            this.inference,
            this.trainingMap
        );
    }

    @Override
    protected double inspectLocation(double[] weights) {
        // Just have the internal WLA learn and then get the loss as the score.
        internalWLA.learn();

        // Save the learned weights.
        for (int i = 0; i < mutableRules.size(); i++) {
            weights[i] = mutableRules.get(i).getWeight();
        }

        return super.inspectLocation(weights);
    }

    @Override
    public void close() {
        super.close();
        internalWLA.close();
    }
}
