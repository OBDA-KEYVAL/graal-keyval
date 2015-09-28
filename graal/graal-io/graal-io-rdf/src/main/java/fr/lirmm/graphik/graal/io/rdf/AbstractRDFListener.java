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
package fr.lirmm.graphik.graal.io.rdf;

import org.openrdf.model.Literal;
import org.openrdf.model.Statement;
import org.openrdf.rio.helpers.RDFHandlerBase;

import fr.lirmm.graphik.graal.api.core.Predicate;
import fr.lirmm.graphik.graal.api.core.Term;
import fr.lirmm.graphik.graal.core.DefaultAtom;
import fr.lirmm.graphik.graal.core.term.DefaultTermFactory;

/**
 * @author Clément Sipieter (INRIA) <clement@6pi.fr>
 * 
 */
abstract class AbstractRDFListener extends RDFHandlerBase {

	@Override
	public void handleStatement(Statement st) {
		Predicate predicate = new Predicate(st.getPredicate().toString(), 2);
		Term subject = DefaultTermFactory.instance().createConstant(
				st.getSubject().toString());
		Term object;
		if (st.getObject() instanceof Literal) {
			Literal l = (Literal) st.getObject();
			fr.lirmm.graphik.util.URI datatype = new fr.lirmm.graphik.util.DefaultURI(
					l.getDatatype().getNamespace(), l.getDatatype()
							.getLocalName());
			object = DefaultTermFactory.instance().createLiteral(datatype,
					l.getLabel());
		} else {
			object = DefaultTermFactory.instance().createConstant(
					st.getObject().toString());
		}
		DefaultAtom a = new DefaultAtom(predicate, subject, object);
		
		this.createAtom(a);
	}

	/**
	 * @param a
	 */
	protected abstract void createAtom(DefaultAtom a);
}
