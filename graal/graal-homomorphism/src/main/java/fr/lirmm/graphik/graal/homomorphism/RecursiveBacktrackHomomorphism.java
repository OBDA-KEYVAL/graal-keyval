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
package fr.lirmm.graphik.graal.homomorphism;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.lirmm.graphik.graal.api.core.Atom;
import fr.lirmm.graphik.graal.api.core.AtomSet;
import fr.lirmm.graphik.graal.api.core.AtomSetException;
import fr.lirmm.graphik.graal.api.core.ConjunctiveQuery;
import fr.lirmm.graphik.graal.api.core.Substitution;
import fr.lirmm.graphik.graal.api.core.Term;
import fr.lirmm.graphik.graal.api.homomorphism.Homomorphism;
import fr.lirmm.graphik.graal.api.homomorphism.HomomorphismException;
import fr.lirmm.graphik.graal.core.HashMapSubstitution;
import fr.lirmm.graphik.util.Profiler;
import fr.lirmm.graphik.util.stream.CloseableIterator;
import fr.lirmm.graphik.util.stream.CloseableIteratorAdapter;

/**
 * Recursive implementation of a backtrack solving algorithm.
 * 
 * @author Clément Sipieter (INRIA) <clement@6pi.fr>
 * 
 */
public final class RecursiveBacktrackHomomorphism implements Homomorphism<ConjunctiveQuery, AtomSet> {

	private static final Logger LOGGER = LoggerFactory.getLogger(RecursiveBacktrackHomomorphism.class);

	private static RecursiveBacktrackHomomorphism instance;
	private Set<Term> domain;

	private Profiler                              profiler;

	// /////////////////////////////////////////////////////////////////////////
	// CONSTRUCTORS
	// /////////////////////////////////////////////////////////////////////////

	private RecursiveBacktrackHomomorphism() {
	}

	public static synchronized RecursiveBacktrackHomomorphism instance() {
		if (instance == null)
			instance = new RecursiveBacktrackHomomorphism();

		return instance;
	}

	// /////////////////////////////////////////////////////////////////////////
	// PUBLIC METHODS
	// /////////////////////////////////////////////////////////////////////////

	/***
	 * 
	 * @return A SubstitutionReader that enumerate all substitutions.
	 * @throws AtomSetException
	 */
	@Override
	public CloseableIterator<Substitution> execute(ConjunctiveQuery query, AtomSet facts) throws HomomorphismException {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace(query.toString());
		}
		if (profiler != null) {
			profiler.start("preprocessing time");
		}
		List<Term> orderedVars = order(query.getAtomSet().getTerms(Term.Type.VARIABLE));
		Collection<Atom>[] queryAtomRanked = getAtomRank(query.getAtomSet(), orderedVars);
		if (profiler != null) {
			profiler.stop("preprocessing time");
		}

		try {
			this.domain = facts.getTerms();

			if (profiler != null) {
				profiler.start("backtracking time");
			}
			CloseableIteratorAdapter<Substitution> results;
			if (isHomomorphism(queryAtomRanked[0], facts, new HashMapSubstitution())) {
				results = new CloseableIteratorAdapter<Substitution>(homomorphism(query, queryAtomRanked, facts,
						new HashMapSubstitution(), orderedVars, 1).iterator());

			} else {
				// return false
				results = new CloseableIteratorAdapter<Substitution>(Collections.<Substitution> emptyList().iterator());
			}
			if (profiler != null) {
				profiler.stop("backtracking time");
			}
			return results;

		} catch (Exception e) {
			throw new HomomorphismException(e.getMessage(), e);
		}
	}

	/**
	 * 
	 * @param atomSet1
	 * @param atomSet2
	 * @return
	 * @throws HomomorphismException
	 */
	public boolean exist(AtomSet atomSet1, AtomSet atomSet2) throws HomomorphismException {
		try {
			List<Term> orderedVars = order(atomSet1.getTerms(Term.Type.VARIABLE));
			Collection<Atom>[] queryAtomRanked = getAtomRank(atomSet1, orderedVars);

			if (isHomomorphism(queryAtomRanked[0], atomSet2, new HashMapSubstitution())) {
				return existHomomorphism(atomSet1, queryAtomRanked, atomSet2, new HashMapSubstitution(), orderedVars, 1);
			} else {
				return false;
			}
		} catch (Exception e) {
			throw new HomomorphismException(e.getMessage(), e);
		}
	}

	@Override
	public void setProfiler(Profiler profiler) {
		this.profiler = profiler;
	}

	@Override
	public Profiler getProfiler() {
		return this.profiler;
	}

	// /////////////////////////////////////////////////////////////////////////
	// PRIVATE METHODS
	// /////////////////////////////////////////////////////////////////////////

	/**
	 * 
	 * @param queryAtomRanked
	 * @param facts
	 * @param substitution
	 * @param orderedVars
	 * @param rank
	 * @return
	 * @throws Exception
	 */
	private Collection<Substitution> homomorphism(
												  ConjunctiveQuery query,
												  Collection<Atom>[] queryAtomRanked,
												  AtomSet facts,
												  Substitution substitution,
												  List<Term> orderedVars,
												  int rank) throws Exception {
		Collection<Substitution> substitutionList = new LinkedList<Substitution>();
		if (orderedVars.size() == 0) {
			Substitution filteredSub = new HashMapSubstitution();
			for (Term var : query.getAnswerVariables()) {
				filteredSub.put(var, substitution.createImageOf(var));
			}
			substitutionList.add(filteredSub);
		} else {
			Term var = orderedVars.remove(0);
			for (Term substitut : domain) {
				Substitution tmpSubstitution = new HashMapSubstitution(substitution);
				tmpSubstitution.put(var, substitut);
				// Test partial homomorphism
				if (isHomomorphism(queryAtomRanked[rank], facts, tmpSubstitution))
					substitutionList.addAll(homomorphism(query, queryAtomRanked, facts, tmpSubstitution,
							new LinkedList<Term>(orderedVars), rank + 1));
			}

		}
		return substitutionList;
	}

	/**
	 * 
	 * @param queryAtomRanked
	 * @param facts
	 * @param substitution
	 * @param orderedVars
	 * @param rank
	 * @return
	 * @throws Exception
	 */
	private static boolean existHomomorphism(
											 AtomSet atomSet1,
											 Collection<Atom>[] queryAtomRanked,
											 AtomSet atomSet2,
											 Substitution substitution,
											 List<Term> orderedVars,
											 int rank) throws Exception {
		if (orderedVars.size() == 0) {
			return true;
		} else {
			Term var;
			Set<Term> domaine = atomSet2.getTerms();

			var = orderedVars.remove(0);
			for (Term substitut : domaine) {
				Substitution tmpSubstitution = new HashMapSubstitution(substitution);
				tmpSubstitution.put(var, substitut);
				// Test partial homomorphism
				if (isHomomorphism(queryAtomRanked[rank], atomSet2, tmpSubstitution))
					if (existHomomorphism(atomSet1, queryAtomRanked, atomSet2, tmpSubstitution, new LinkedList<Term>(
							orderedVars), rank + 1)) {
						return true;
					}
			}

		}
		return false;
	}

	private static boolean isHomomorphism(Collection<Atom> atomsFrom, AtomSet atomsTo, Substitution substitution)
																												 throws Exception {
		for (Atom atom : atomsFrom) {
			if (LOGGER.isDebugEnabled())
				LOGGER.debug("contains? " + substitution.createImageOf(atom));

			if (!atomsTo.contains(substitution.createImageOf(atom)))
				return false;
		}
		return true;
	}

	// TODO use an external comparator
	private static List<Term> order(Collection<Term> vars) {
		LinkedList<Term> orderedList = new LinkedList<Term>();
		for (Term var : vars)
			if (!orderedList.contains(var))
				orderedList.add(var);

		return orderedList;
	}

	/**
	 * The index 0 contains the fully instantiated atoms.
	 * 
	 * @param atomset
	 * @param varsOrdered
	 * @return
	 */
	private static Collection<Atom>[] getAtomRank(Iterable<Atom> atomset, List<Term> varsOrdered) {
		int tmp, rank;

		Collection<Atom>[] atomRank = new LinkedList[varsOrdered.size() + 1];
		for (int i = 0; i < atomRank.length; ++i)
			atomRank[i] = new LinkedList<Atom>();

		//
		for (Atom a : atomset) {
			rank = 0;
			for (Term t : a.getTerms()) {
				tmp = varsOrdered.indexOf(t) + 1;
				if (rank < tmp)
					rank = tmp;
			}
			atomRank[rank].add(a);
		}

		return atomRank;
	}

}
