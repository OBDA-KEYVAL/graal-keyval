/*
 * Copyright (C) Inria Sophia Antipolis - Méditerranée / LIRMM
 * (Université de Montpellier & CNRS) (2014 - 2015)
 *
 * Contributors :
 *
 * Clément SIPIETER <clement.sipieter@inria.fr>
 * Mélanie KÖNIG
 * Swan ROCHER
 * Jean-François BAGET
 * Michel LECLÈRE
 * Marie-Laure MUGNIER <mugnier@lirmm.fr>
 *
 *
 * This file is part of Graal <https://graphik-team.github.io/graal/>.
 *
 * This software is governed by the CeCILL  license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and,  more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL license and that you accept its terms.
 */
/**
 * 
 */
package fr.lirmm.graphik.graal.forward_chaining.rule_applier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.lirmm.graphik.graal.api.core.Atom;
import fr.lirmm.graphik.graal.api.core.AtomSet;
import fr.lirmm.graphik.graal.api.core.AtomSetException;
import fr.lirmm.graphik.graal.api.core.ConjunctiveQuery;
import fr.lirmm.graphik.graal.api.core.Rule;
import fr.lirmm.graphik.graal.api.core.Substitution;
import fr.lirmm.graphik.graal.api.core.Term;
import fr.lirmm.graphik.graal.api.core.VariableGenerator;
import fr.lirmm.graphik.graal.api.forward_chaining.ChaseHaltingCondition;
import fr.lirmm.graphik.graal.api.forward_chaining.RuleApplicationException;
import fr.lirmm.graphik.graal.api.forward_chaining.RuleApplier;
import fr.lirmm.graphik.graal.api.homomorphism.Homomorphism;
import fr.lirmm.graphik.graal.api.homomorphism.HomomorphismException;
import fr.lirmm.graphik.graal.api.homomorphism.HomomorphismFactoryException;
import fr.lirmm.graphik.graal.core.DefaultVariableGenerator;
import fr.lirmm.graphik.graal.core.factory.ConjunctiveQueryFactory;
import fr.lirmm.graphik.graal.forward_chaining.halting_condition.RestrictedChaseStopCondition;
import fr.lirmm.graphik.util.stream.CloseableIterator;
import fr.lirmm.graphik.util.stream.GIterator;

/**
 * This Applier executes a call to the chaseStopCondition for all homomorphisms
 * of the rule body found. And not only for homomorphisms which are unique
 * projected on frontier variables.
 * 
 * @author Clément Sipieter (INRIA) {@literal <clement@6pi.fr>}
 *
 */
public class ExhaustiveRuleApplier<T extends AtomSet> implements RuleApplier<Rule, T> {

	private static final Logger LOGGER = LoggerFactory.getLogger(ExhaustiveRuleApplier.class);

	private ChaseHaltingCondition haltingCondition;
	private Homomorphism<ConjunctiveQuery, T> solver;
	private VariableGenerator existentialGen;

	// //////////////////////////////////////////////////////////////////////////
	// CONSTRUCTORS
	// //////////////////////////////////////////////////////////////////////////

	/**
	 * Construct a DefaultRuleApplier with a RestrictedChaseStopCondition and
	 * the given homomorphism solver.
	 */
	public ExhaustiveRuleApplier(Homomorphism<ConjunctiveQuery, T> homomorphismSolver) {
		this(homomorphismSolver, new RestrictedChaseStopCondition());
	}

	/**
	 * Construct a DefaultRuleApplier with the given HaltingCondition.
	 * 
	 * @param haltingCondition
	 */
	public ExhaustiveRuleApplier(Homomorphism<ConjunctiveQuery, T> homomorphismSolver,
	        ChaseHaltingCondition haltingCondition) {
		this(homomorphismSolver, haltingCondition, new DefaultVariableGenerator("E"));
	}

	/**
	 * Construct a DefaultRuleApplier with the given HaltingCondition,
	 * homomorphism solver and SymbolGenerator. The SymbolGenerator is used to
	 * generate new existential variables.
	 * 
	 * @param haltingCondition
	 * @param homomorphismSolver
	 * @param existentialVarGenerator
	 */
	public ExhaustiveRuleApplier(Homomorphism<ConjunctiveQuery, T> homomorphismSolver,
	        ChaseHaltingCondition haltingCondition, VariableGenerator existentialVarGenerator) {
		this.haltingCondition = haltingCondition;
		this.solver = homomorphismSolver;
		this.existentialGen = existentialVarGenerator;
	}

	// //////////////////////////////////////////////////////////////////////////
	// METHODS
	// //////////////////////////////////////////////////////////////////////////

	@Override
	public boolean apply(Rule rule, T atomSet) throws RuleApplicationException {
		boolean isChanged = false;
		ConjunctiveQuery query = ConjunctiveQueryFactory.instance().create(rule.getBody());
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Rule to execute: " + rule);
			LOGGER.debug("       -- Query: " + query);
		}

		try {
			CloseableIterator<Substitution> subIt = this.executeQuery(query, atomSet);
			while (subIt.hasNext()) {
				Substitution substitution = subIt.next();
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("-- Found homomorphism: " + substitution);
				}

				GIterator<Atom> it = this.getHaltingCondition().apply(rule, substitution, atomSet);
				if (it.hasNext()) {
					atomSet.addAll(it);
					isChanged = true;
				}
			}
			subIt.close();
		} catch (HomomorphismFactoryException e) {
			throw new RuleApplicationException("Error during rule application", e);
		} catch (HomomorphismException e) {
			throw new RuleApplicationException("Error during rule application", e);
		} catch (AtomSetException e) {
			throw new RuleApplicationException("Error during rule application", e);
		}

		return isChanged;
	}

	// //////////////////////////////////////////////////////////////////////////
	//
	// //////////////////////////////////////////////////////////////////////////

	protected ChaseHaltingCondition getHaltingCondition() {
		return this.haltingCondition;
	}

	protected CloseableIterator<Substitution> executeQuery(ConjunctiveQuery query, T atomSet)
	                                                                                         throws HomomorphismFactoryException,
	                                                                                         HomomorphismException {
		return this.solver.execute(query, atomSet);
	}

	protected Term getFreeVar() {
		return this.existentialGen.getFreshVar();
	}

}
