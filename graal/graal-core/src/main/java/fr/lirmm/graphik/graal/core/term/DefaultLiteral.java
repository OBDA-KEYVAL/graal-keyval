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
package fr.lirmm.graphik.graal.core.term;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import fr.lirmm.graphik.graal.api.core.AbstractTerm;
import fr.lirmm.graphik.graal.api.core.Literal;
import fr.lirmm.graphik.graal.api.core.Term;
import fr.lirmm.graphik.util.URI;
import fr.lirmm.graphik.util.URIUtils;

/**
 * Not immutable but it is not possible with an Object as constructor parameter
 * 
 * @author Clément Sipieter (INRIA) {@literal <clement@6pi.fr>}
 *
 */
final class DefaultLiteral extends AbstractTerm implements Literal {

	private static final long serialVersionUID = -8168240181900479256L;
	private static Pattern    pattern          = Pattern.compile("\"(.*)\"\\^\\^<(.*)>");


	private final Object      value;
	private final URI         datatype;
	private final String      identifier;

	// /////////////////////////////////////////////////////////////////////////
	// CONSTRUCTORS
	// /////////////////////////////////////////////////////////////////////////

	public DefaultLiteral(Literal lit) {
		this(lit.getDatatype(), lit.getValue());
	}

	public DefaultLiteral(Object value) {
		boolean test = false;
		Matcher m = null;
		if(value instanceof String) {
			m = pattern.matcher((String) value);
			test = m.matches();
		}
		if (test) {
			this.datatype = URIUtils.createURI(m.group(2));
			this.value = m.group(1);
		} else {
			this.datatype = URIUtils.createURI("java:"
				+ StringUtils.reverseDelimited(value.getClass().getCanonicalName(), '.'));
			this.value = value;
		}
		this.identifier = "\"" + this.value.toString() + "\"^^<" + this.getDatatype().toString() + ">";
	}

	public DefaultLiteral(URI datatype, Object value) {
		this.datatype = datatype;
		this.value = value;
		this.identifier = "\"" + this.value.toString() + "\"^^<" + this.getDatatype().toString() + ">";
	}

	// /////////////////////////////////////////////////////////////////////////
	// PUBLIC METHODS
	// /////////////////////////////////////////////////////////////////////////

	@Override
	public boolean isConstant() {
		return true;
	}

	@Override
	public Term.Type getType() {
		return Term.Type.LITERAL;
	}

	@Override
	public Object getValue() {
		return this.value;
	}

	@Override
	public String getLabel() {
		return this.value.toString();
	}

	@Override
	public URI getDatatype() {
		return this.datatype;
	}

	@Override
	public String getIdentifier() {
		return this.identifier;
	}

	// /////////////////////////////////////////////////////////////////////////
	// OBJECT OVERRIDE METHODS
	// /////////////////////////////////////////////////////////////////////////


}
