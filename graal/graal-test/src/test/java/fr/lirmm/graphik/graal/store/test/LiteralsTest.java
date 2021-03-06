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

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.junit.Assert;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import fr.lirmm.graphik.graal.api.core.Atom;
import fr.lirmm.graphik.graal.api.core.AtomSet;
import fr.lirmm.graphik.graal.api.core.AtomSetException;
import fr.lirmm.graphik.graal.api.core.ConjunctiveQuery;
import fr.lirmm.graphik.graal.api.core.Term;
import fr.lirmm.graphik.graal.api.homomorphism.HomomorphismException;
import fr.lirmm.graphik.graal.api.homomorphism.HomomorphismFactoryException;
import fr.lirmm.graphik.graal.core.DefaultConjunctiveQuery;
import fr.lirmm.graphik.graal.core.atomset.LinkedListAtomSet;
import fr.lirmm.graphik.graal.homomorphism.StaticHomomorphism;
import fr.lirmm.graphik.graal.io.dlp.DlgpParser;

/**
 * @author Clément Sipieter (INRIA) <clement@6pi.fr>
 *
 */
@RunWith(Theories.class)
public class LiteralsTest {

	@DataPoints
	public static AtomSet[] getAtomset() {
		List<AtomSet> list = new LinkedList<AtomSet>();
		list.addAll(Arrays.asList(TestUtil.getAtomSet()));
		list.addAll(Arrays.asList(TestUtil.getTripleStores()));
		return list.toArray(new AtomSet[list.size()]);
	}

	@Theory
	public void string(AtomSet store) throws AtomSetException, HomomorphismFactoryException, HomomorphismException {
		Atom a = DlgpParser.parseAtom("name(a,\"john\").");
		store.add(a);

		ConjunctiveQuery q = new DefaultConjunctiveQuery(new LinkedListAtomSet(a), Collections.<Term> emptyList());
		Assert.assertTrue(StaticHomomorphism.instance().execute(q, store).hasNext());

		Atom b = store.iterator().next();
		Assert.assertEquals(a, b);
	}

	@Theory
	public void integer(AtomSet store) throws AtomSetException, HomomorphismFactoryException, HomomorphismException {
		Atom a = DlgpParser.parseAtom("age(a,1).");
		store.add(a);

		ConjunctiveQuery q = new DefaultConjunctiveQuery(new LinkedListAtomSet(a), Collections.<Term> emptyList());
		Assert.assertTrue(StaticHomomorphism.instance().execute(q, store).hasNext());

		Atom b = store.iterator().next();
		Assert.assertEquals(a, b);
	}

	@Theory
	public void decimal(AtomSet store) throws AtomSetException, HomomorphismFactoryException, HomomorphismException {
		Atom a = DlgpParser.parseAtom("age(a,-5.1).");
		store.add(a);

		ConjunctiveQuery q = new DefaultConjunctiveQuery(new LinkedListAtomSet(a), Collections.<Term> emptyList());
		Assert.assertTrue(StaticHomomorphism.instance().execute(q, store).hasNext());

		Atom b = store.iterator().next();
		Assert.assertEquals(a, b);
	}

	@Theory
	public void ddouble(AtomSet store) throws AtomSetException, HomomorphismFactoryException, HomomorphismException {
		Atom a = DlgpParser.parseAtom("age(a,5.2E20).");
		store.add(a);

		ConjunctiveQuery q = new DefaultConjunctiveQuery(new LinkedListAtomSet(a), Collections.<Term> emptyList());
		Assert.assertTrue(StaticHomomorphism.instance().execute(q, store).hasNext());

		Atom b = store.iterator().next();
		Assert.assertEquals(a, b);
	}

	@Theory
	public void bool(AtomSet store) throws AtomSetException, HomomorphismFactoryException, HomomorphismException {
		Atom a = DlgpParser.parseAtom("ishuman(a,true).");
		store.add(a);

		ConjunctiveQuery q = new DefaultConjunctiveQuery(new LinkedListAtomSet(a), Collections.<Term> emptyList());
		Assert.assertTrue(StaticHomomorphism.instance().execute(q, store).hasNext());

		Atom b = store.iterator().next();
		Assert.assertEquals(a, b);
	}

}
