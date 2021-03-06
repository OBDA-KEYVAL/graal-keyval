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
package fr.lirmm.graphik.graal.forward_chaining.halting_condition;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import fr.lirmm.graphik.graal.api.core.Atom;
import fr.lirmm.graphik.graal.api.core.AtomSet;
import fr.lirmm.graphik.graal.api.core.ConjunctiveQuery;
import fr.lirmm.graphik.graal.api.core.InMemoryAtomSet;
import fr.lirmm.graphik.graal.api.core.Substitution;
import fr.lirmm.graphik.graal.api.core.Term;
import fr.lirmm.graphik.graal.core.factory.AtomSetFactory;
import fr.lirmm.graphik.graal.core.factory.SubstitutionFactory;
import fr.lirmm.graphik.graal.core.term.DefaultTermFactory;


/**
 * @author Clément Sipieter (INRIA) <clement@6pi.fr>
 * 
 */
public class ConjunctiveQueryWithFixedVariables implements ConjunctiveQuery {

	private InMemoryAtomSet atomSet;
	private List<Term> answerVariables;

	public ConjunctiveQueryWithFixedVariables(AtomSet atomSet, Iterable<Term> fixedTerms) {
		this.atomSet = computeFixedQuery(atomSet, fixedTerms);
        this.answerVariables = new LinkedList(this.atomSet.getTerms(Term.Type.VARIABLE));
    }

	public ConjunctiveQueryWithFixedVariables(/*ReadOnly*/AtomSet atomSet,
			List<Term> responseVariables, Iterable<Term> fixedTerms) {

		this.atomSet = computeFixedQuery(atomSet, fixedTerms);
		this.answerVariables = responseVariables;
		if (this.answerVariables == null) {
			this.answerVariables = new LinkedList<Term>();
		}
	}

	@Override
	public Iterator<Atom> iterator() { return getAtomSet().iterator(); }
	
	// /////////////////////////////////////////////////////////////////////////
	// PUBLIC METHODS
	// /////////////////////////////////////////////////////////////////////////
	
	@Override
	public boolean isBoolean() {
		return this.answerVariables.isEmpty();
	}

	@Override
	public InMemoryAtomSet getAtomSet() {
		return this.atomSet;
	}

	@Override
	public List<Term> getAnswerVariables() {
		return this.answerVariables;
	}
	
	@Override
	public void setAnswerVariables(List<Term> ans) {
		this.answerVariables = ans;
	}
	

	@Override
	public String getLabel() {
		return "";
	}
	
	// /////////////////////////////////////////////////////////////////////////
	// PRIVATE METHODS
	// /////////////////////////////////////////////////////////////////////////
	
	private static InMemoryAtomSet computeFixedQuery(/*ReadOnly*/AtomSet atomSet,
			Iterable<Term> fixedTerms) {
		// create a Substitution for fixed query
		InMemoryAtomSet fixedQuery = AtomSetFactory.instance().createAtomSet();
		Substitution fixSub = SubstitutionFactory.instance().createSubstitution();
		for (Term t : fixedTerms) {
			if (!t.isConstant())
				fixSub.put(
						t,
						DefaultTermFactory.instance().createConstant(
								t.getLabel()));
		}

		// apply substitution
		for (Atom a : atomSet) {
			fixedQuery.add(fixSub.createImageOf(a));
		}
		
		return fixedQuery;
	}

	// /////////////////////////////////////////////////////////////////////////
	// OBJECT METHODS
	// /////////////////////////////////////////////////////////////////////////

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("FIXED(");
		this.appendTo(sb);
		return sb.toString();
	}

	@Override
	public void appendTo(StringBuilder sb) {
		for (Term t : this.atomSet.getTerms(Term.Type.CONSTANT))
			sb.append(t).append(',');

		sb.append("), ANS(");
		for (Term t : this.answerVariables)
			sb.append(t).append(',');

		sb.append(") :- ");
		sb.append(this.atomSet);
	}
	
}
