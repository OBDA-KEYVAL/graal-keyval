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
package fr.lirmm.graphik.graal.store.test;

import java.util.Iterator;

import org.junit.Assert;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import fr.lirmm.graphik.graal.api.core.Atom;
import fr.lirmm.graphik.graal.api.core.AtomSet;
import fr.lirmm.graphik.graal.api.core.AtomSetException;
import fr.lirmm.graphik.graal.api.core.Predicate;
import fr.lirmm.graphik.graal.api.core.Term;
import fr.lirmm.graphik.graal.io.dlp.DlgpParser;

/**
 * @author Clément Sipieter (INRIA) <clement@6pi.fr>
 *
 */
@RunWith(Theories.class)
public class NoTripleStoreTest {

	@DataPoints
	public static AtomSet[] getAtomset() {
		return TestUtil.getAtomSet();
	}

	@Theory
	public void predicateArityTest(AtomSet store) throws AtomSetException {
		store.add(DlgpParser.parseAtom("p(a)."));
		store.add(DlgpParser.parseAtom("p(a,b)."));
		store.add(DlgpParser.parseAtom("p(a,c)."));

		int i = 0;
		for (Iterator<Predicate> it = store.predicatesIterator(); it.hasNext(); it.next()) {
			++i;
		}

		Assert.assertEquals(2, i);
	}

	@Theory
	public void getTerms(AtomSet store) throws AtomSetException {
		store.add(DlgpParser.parseAtom("p(a,b)."));
		store.add(DlgpParser.parseAtom("p(b,c)."));
		store.add(DlgpParser.parseAtom("p(b,c,X,Y)."));

		int i = 0;
		for (Iterator<Term> it = store.getTerms().iterator(); it.hasNext(); it
				.next()) {
			++i;
		}

		Assert.assertEquals("Wrong number of terms", 5, i);

		i = 0;
		for (Iterator<Term> it = store.getTerms(Term.Type.CONSTANT).iterator(); it
				.hasNext(); it.next()) {
			++i;
		}

		Assert.assertEquals("Wrong number of constant", 3, i);

		i = 0;
		for (Iterator<Term> it = store.getTerms(Term.Type.VARIABLE).iterator(); it
				.hasNext(); it.next()) {
			++i;
		}
		Assert.assertEquals("Wrong number of variable", 2, i);
	}

	@Theory
	public void termsOrder(AtomSet store) throws AtomSetException {
		Atom a1 = DlgpParser.parseAtom("p(a,b,c,d,e,f).");
		Atom a2 = DlgpParser.parseAtom("p(f,e,d,c,b,a).");

		store.add(a1);
		store.add(a2);

		for (Atom a : store) {
			Assert.assertTrue(a.equals(a1) || a.equals(a2));
		}
	}

}
