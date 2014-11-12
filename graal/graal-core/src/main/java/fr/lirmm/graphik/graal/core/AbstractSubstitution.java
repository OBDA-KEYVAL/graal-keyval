/**
 * 
 */
package fr.lirmm.graphik.graal.core;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import fr.lirmm.graphik.graal.core.atomset.AtomSet;
import fr.lirmm.graphik.graal.core.factory.AtomSetFactory;
import fr.lirmm.graphik.graal.core.factory.RuleFactory;

/**
 * @author Clément Sipieter (INRIA) <clement@6pi.fr>
 * 
 */
public abstract class AbstractSubstitution implements Substitution {

	protected abstract Map<Term, Term> getMap();

	@Override
	public Set<Term> getTerms() {
		return this.getMap().keySet();
	}

	@Override
	public Set<Term> getValues() {
		return new TreeSet<Term>(this.getMap().values());
	}

	@Override
	public Term getSubstitute(Term term) {
		Term substitut = this.getMap().get(term);
		return (substitut == null) ? term : substitut;
	}

	@Override
	public boolean put(Term term, Term substitut) {
		if (Term.Type.CONSTANT.equals(term.getType())
				&& Term.Type.CONSTANT.equals(substitut.getType())) {
			if (!term.equals(substitut)) {
				return false;
			}
		}
		this.getMap().put(term, substitut);
		return true;
	}

	@Override
	public void put(Substitution substitution) {
		for (Term term : substitution.getTerms()) {
			this.put(term, substitution.getSubstitute(term));
		}
	}

	@Override
	public Atom getSubstitut(Atom atom) {
		List<Term> termsSubstitut = new LinkedList<Term>();
		for (Term term : atom.getTerms())
			termsSubstitut.add(this.getSubstitute(term));

		return new DefaultAtom(atom.getPredicate(), termsSubstitut);
	}

	@Override
	public AtomSet getSubstitut(AtomSet src) {
		AtomSet dest = AtomSetFactory.getInstance().createAtomSet();
		this.substitut(src, dest);
		return dest;
	}

	@Override
	public void substitut(AtomSet src, AtomSet dest) {
		for (Atom a : src) {
			dest.add(this.getSubstitut(a));
		}
	}

	@Override
	public Rule getSubstitut(Rule rule) {
		Rule substitut = RuleFactory.getInstance().createRule();
		this.substitut(rule.getBody(), substitut.getBody());
		this.substitut(rule.getHead(), substitut.getHead());
		return substitut;
	}

	@Override
	public boolean compose(Term term, Term substitut) {
		term = this.getSubstitute(term);
		substitut = this.getSubstitute(substitut);

		if (Term.Type.CONSTANT.equals(term.getType())) {
			Term tmp = term;
			term = substitut;
			substitut = tmp;
		}

		for (Term t : this.getTerms()) {
			if (term.equals(this.getSubstitute(t))) {
				if (!this.put(t, substitut)) {
					return false;
				}
			}
		}

		if (!this.put(term, substitut)) {
			return false;
		}
		return true;
	}

	/**
	 * @see fr.lirmm.graphik.graal.core.Substitution#compose(fr.lirmm.graphik.graal.core.Substitution)
	 */
	@Override
	public Substitution compose(Substitution s) {
		Substitution newSub = this.getNewInstance();
		for (Term term : this.getTerms()) {
			if (!newSub.compose(term, this.getSubstitute(term))) {
				return null;
			}
		}
		for (Term term : s.getTerms()) {
			if (!newSub.compose(term, s.getSubstitute(term))) {
				return null;
			}
		}
		return newSub;
	}

	// /////////////////////////////////////////////////////////////////////////
	// OVERRIDE OBJECT METHODS
	// /////////////////////////////////////////////////////////////////////////

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		boolean first = true;
		builder.append('{');
		for (Term key : this.getTerms()) {
			if (first)
				first = false;
			else
				builder.append(',');
			builder.append(key).append("->");
			builder.append(this.getSubstitute(key));
		}
		builder.append('}');
		return builder.toString();
	}

	protected abstract Substitution getNewInstance();

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof Substitution)) {
			return false;
		}
		return this.equals((Substitution) obj);
	}

	public boolean equals(Substitution other) {
		for (Term t : this.getTerms()) {
			if (!this.getSubstitute(t).equals(other.getSubstitute(t)))
				return false;
		}

		for (Term t : other.getTerms()) {
			if (!this.getSubstitute(t).equals(other.getSubstitute(t)))
				return false;
		}

		return true;
	}

};