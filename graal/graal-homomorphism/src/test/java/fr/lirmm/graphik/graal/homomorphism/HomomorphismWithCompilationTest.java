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

import org.junit.Assert;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import fr.lirmm.graphik.graal.api.core.AtomSet;
import fr.lirmm.graphik.graal.api.core.ConjunctiveQuery;
import fr.lirmm.graphik.graal.api.core.InMemoryAtomSet;
import fr.lirmm.graphik.graal.api.core.RuleSet;
import fr.lirmm.graphik.graal.api.core.RulesCompilation;
import fr.lirmm.graphik.graal.api.core.Substitution;
import fr.lirmm.graphik.graal.api.homomorphism.HomomorphismException;
import fr.lirmm.graphik.graal.api.homomorphism.HomomorphismWithCompilation;
import fr.lirmm.graphik.graal.core.atomset.LinkedListAtomSet;
import fr.lirmm.graphik.graal.core.compilation.IDCompilation;
import fr.lirmm.graphik.graal.core.ruleset.LinkedListRuleSet;
import fr.lirmm.graphik.graal.core.term.DefaultTermFactory;
import fr.lirmm.graphik.graal.io.dlp.DlgpParser;
import fr.lirmm.graphik.util.stream.CloseableIterator;

/**
 * @author Clément Sipieter (INRIA) {@literal <clement@6pi.fr>}
 *
 */
@RunWith(Theories.class)
public class HomomorphismWithCompilationTest {

	@DataPoints
	public static HomomorphismWithCompilation[] writeableStore() {
		return TestUtil.getHomomorphismsWithCompilation();
	}

	@Theory
	public void test1(HomomorphismWithCompilation<ConjunctiveQuery, AtomSet> h) throws HomomorphismException {
		InMemoryAtomSet store = new LinkedListAtomSet();

		store.add(DlgpParser.parseAtom("p(a)."));

		RuleSet rules = new LinkedListRuleSet();
		rules.add(DlgpParser.parseRule("q(X) :- p(X)."));

		RulesCompilation comp = new IDCompilation();
		comp.compile(rules.iterator());

		CloseableIterator<Substitution> results = h.execute(DlgpParser.parseQuery("?(X) :- q(X)."), store, comp);
		Assert.assertTrue(results.hasNext());
		Substitution next = results.next();
		Assert.assertEquals(DefaultTermFactory.instance().createConstant("a"),
		    next.createImageOf(DefaultTermFactory.instance().createVariable("X")));
		Assert.assertFalse(results.hasNext());
		results.close();
	}

	@Theory
	public void test2(HomomorphismWithCompilation<ConjunctiveQuery, AtomSet> h) throws HomomorphismException {
		InMemoryAtomSet store = new LinkedListAtomSet();

		store.add(DlgpParser.parseAtom("p(a,b)."));

		RuleSet rules = new LinkedListRuleSet();
		rules.add(DlgpParser.parseRule("q(X,Y) :- p(Y,X)."));

		RulesCompilation comp = new IDCompilation();
		comp.compile(rules.iterator());

		CloseableIterator<Substitution> results = h.execute(DlgpParser.parseQuery("?(X,Y) :- q(X,Y)."), store, comp);

		Assert.assertTrue(results.hasNext());
		Substitution next = results.next();
		Assert.assertEquals(DefaultTermFactory.instance().createConstant("a"),
		    next.createImageOf(DefaultTermFactory.instance().createVariable("Y")));
		Assert.assertEquals(DefaultTermFactory.instance().createConstant("b"),
		    next.createImageOf(DefaultTermFactory.instance().createVariable("X")));
		Assert.assertFalse(results.hasNext());

		results.close();
	}

}
