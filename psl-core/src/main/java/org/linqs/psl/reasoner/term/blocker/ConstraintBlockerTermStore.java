/*
 * This file is part of the PSL software.
 * Copyright 2011-2015 University of Maryland
 * Copyright 2013-2018 The Regents of the University of California
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
package org.linqs.psl.reasoner.term.blocker;

import org.linqs.psl.application.groundrulestore.GroundRuleStore;
import org.linqs.psl.model.atom.RandomVariableAtom;
import org.linqs.psl.model.rule.GroundRule;
import org.linqs.psl.model.rule.WeightedGroundRule;
import org.linqs.psl.model.rule.arithmetic.UnweightedGroundArithmeticRule;
import org.linqs.psl.model.rule.misc.GroundValueConstraint;
import org.linqs.psl.reasoner.term.TermStore;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * A TermStore to hold blocks.
 * See {@link ConstraintBlockerTermGenerator} for details on the constraint blocking process.
 */
public class ConstraintBlockerTermStore implements TermStore<ConstraintBlockerTerm> {
	private List<ConstraintBlockerTerm> blocks;
	private Map<RandomVariableAtom, Integer> rvMap;
	private GroundRuleStore groundRuleStore;

	public ConstraintBlockerTermStore() {
		blocks = new ArrayList<ConstraintBlockerTerm>();
		rvMap = new HashMap<RandomVariableAtom, Integer>();
		groundRuleStore = null;
	}

	public void init(GroundRuleStore groundRuleStore,
			RandomVariableAtom[][] rvBlocks, WeightedGroundRule[][] incidentGRs,
			boolean[] exactlyOne) {
		assert(rvBlocks.length == incidentGRs.length);
		assert(rvBlocks.length == exactlyOne.length);

		this.groundRuleStore = groundRuleStore;

		for (int i = 0; i < rvBlocks.length; i++) {
			Integer blockIndex = new Integer(blocks.size());
			blocks.add(new ConstraintBlockerTerm(rvBlocks[i], incidentGRs[i], exactlyOne[i]));
			for (RandomVariableAtom atom : rvBlocks[i]) {
				rvMap.put(atom, blockIndex);
			}
		}
	}

	/**
	 * Extremely hacky way to allow methods that require this to get ahold of the GroundRuleStore.
	 */
	public GroundRuleStore getGroundRuleStore() {
		return groundRuleStore;
	}

	/**
	 * Get the index of the block (term) associated with the given atom.
	 * @return the index or -1 if the atom is not in any blocks.
	 */
	public int getBlockIndex(RandomVariableAtom atom) {
		Integer index = rvMap.get(atom);

		if (index == null) {
			return -1;
		}

		return index.intValue();
	}

	/**
	 * Randomly initializes the RandomVariableAtoms to a feasible state.
	 */
	public void randomlyInitialize() {
		Random rand = new Random();
		for (ConstraintBlockerTerm block : blocks) {
			block.randomlyInitialize(rand);
		}
	}

	@Override
	public void add(GroundRule rule, ConstraintBlockerTerm term) {
		throw new UnsupportedOperationException("ConstraintBlockerTermStore needs all ground rules at once, use init().");
	}

	@Override
	public void clear() {
		blocks.clear();
		rvMap.clear();
		groundRuleStore = null;
	}

	@Override
	public void close() {
		clear();
		blocks = null;
		rvMap = null;
	}

	@Override
	public ConstraintBlockerTerm get(int index) {
		return blocks.get(index);
	}

	@Override
	public int size() {
		return blocks.size();
	}

	@Override
	public void updateWeight(WeightedGroundRule rule) {
		// The blocks don't care about weights.
	}

	@Override
	public List<Integer> getTermIndices(WeightedGroundRule rule) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Iterator<ConstraintBlockerTerm> iterator() {
		return blocks.iterator();
	}
}
