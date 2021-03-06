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
package fr.lirmm.graphik.graal.api.core;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * 
 * This class implements a comparator of Term that doesn't make difference on
 * Term Type.
 *
 * @author Clément Sipieter (INRIA) <clement@6pi.fr>
 *
 */
public class TermValueComparator implements Comparator<Term>, Serializable {

	private static final long          serialVersionUID = -4231328676676157296L;

	private static TermValueComparator instance;

	protected TermValueComparator() {
		super();
	}

	public static synchronized TermValueComparator instance() {
		if (instance == null)
			instance = new TermValueComparator();

		return instance;
	}

	@Override
	public int compare(Term term0, Term term1) {
		return term0.getIdentifier().toString().compareTo(term1.getIdentifier().toString());
	}

	public int compare(List<Term> l1, List<Term> l2) {
		if (l1.size() != l2.size()) {
			return l1.size() - l2.size();
		}
		Iterator<Term> it1 = l1.iterator();
		Iterator<Term> it2 = l2.iterator();
		while (it1.hasNext() && it2.hasNext()) {
			int val = this.compare(it1.next(), it2.next());
			if (val != 0) {
				return val;
			}
		}

		return 0;
	}
};
