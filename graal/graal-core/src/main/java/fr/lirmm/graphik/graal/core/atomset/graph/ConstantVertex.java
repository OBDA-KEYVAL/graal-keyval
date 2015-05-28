/**
 * 
 */
package fr.lirmm.graphik.graal.core.atomset.graph;

import fr.lirmm.graphik.graal.core.term.Constant;

/**
 * @author Clément Sipieter (INRIA) {@literal <clement@6pi.fr>}
 *
 */
final class ConstantVertex extends AbstractTermVertex implements
		Constant {

	private static final long serialVersionUID = -1143798191017134399L;

	private Constant term;

	// /////////////////////////////////////////////////////////////////////////
	// CONSTRUCTORS
	// /////////////////////////////////////////////////////////////////////////

	public ConstantVertex(Constant term) {
		this.term = term;
	}

	// /////////////////////////////////////////////////////////////////////////
	// PUBLIC METHODS
	// /////////////////////////////////////////////////////////////////////////

	@Override
	protected Constant getTerm() {
		return this.term;
	}

	@Override
	public String getLabel() {
		return this.term.getLabel();
	}

	@Override
	public Object getIdentifier() {
		return this.getTerm().getIdentifier();
	}

}
