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
package fr.lirmm.graphik.graal.keyval;

import java.util.ArrayList;
import java.util.Set;

import org.json.JSONObject;

import fr.lirmm.graphik.graal.api.core.InMemoryAtomSet;
import fr.lirmm.graphik.graal.api.core.Predicate;
import fr.lirmm.graphik.graal.api.core.Rule;
import fr.lirmm.graphik.graal.api.core.Term;
import fr.lirmm.graphik.graal.api.core.Term.Type;
import fr.lirmm.graphik.util.MethodNotImplementedError;

/**
 * @author Clément Sipieter (INRIA) {@literal <clement@6pi.fr>}
 *
 */
public class NoRule implements Rule {

	private RuleParser parser;
	
	private PathAtom body;
	private PathAtom head;

	public NoRule(PathAtom body, PathAtom head){
		this.body = body;
		this.head = head;
		this.parser = new RuleParser();
	}
	
	public JSONObject exportJson(){
		return this.parser.getJsonRule(this);
	}
	
	public PathAtom getBodyPathAtom(){
		return this.body;
	}
	
	public PathAtom getHeadPathAtom(){
		return this.head;
	}
	
	public void setBodyPathAtom(PathAtom body){
		this.body = body;
	}
	
	public void setHeadPathAtom(PathAtom head){
		this.head = head;
	}
	
	public boolean isNoRL1(){
		ArrayList<Predicate> predsBody = this.body.getPathPredicate().getPredicates();
		ArrayList<Predicate> predsHead = this.head.getPathPredicate().getPredicates();
		if(predsBody.size() == 1 && predsHead.size() == 1){
			return true;
		}
		return false;
	}
	
	public boolean isNoRL2(){
		ArrayList<Predicate> predsBody = this.body.getPathPredicate().getPredicates();
		if(predsBody.size() > 1){
			return true;
		}
		return false;
	}
	
	public boolean isNoRL3(){
		ArrayList<Predicate> predsHead = this.head.getPathPredicate().getPredicates();
		if(predsHead.size() > 1){
			return true;
		}
		return false;
	}
	
	public int compareTo(Rule o) {
		// TODO implement this method
		throw new MethodNotImplementedError();
	}

	
	public void appendTo(StringBuilder sb) {
		// TODO implement this method
		throw new MethodNotImplementedError();
	}

	
	public String getLabel() {
		// TODO implement this method
		return "";
	}

	
	public void setLabel(String label) {
		// TODO implement this method
	}

	
	public InMemoryAtomSet getBody() {
		// TODO implement this method
		throw new MethodNotImplementedError();
	}

	
	public InMemoryAtomSet getHead() {
		// TODO implement this method
		throw new MethodNotImplementedError();
	}

	
	public Set<Term> getFrontier() {
		// TODO implement this method
		throw new MethodNotImplementedError();
	}

	
	public Set<Term> getExistentials() {
		// TODO implement this method
		throw new MethodNotImplementedError();
	}

	
	public Set<Term> getTerms(Type type) {
		// TODO implement this method
		throw new MethodNotImplementedError();
	}

	
	public Set<Term> getTerms() {
		// TODO implement this method
		throw new MethodNotImplementedError();
	}

	public String toString(){
		return this.body.toString() + " -> " + this.head.toString();
	}
	
	public String toField(){
		return this.body.getPathPredicate().toFieldName()+" : ("+this.body.getTerm().getLabel() + ") -> " + this.head.getPathPredicate().toFieldName()+" : (" +this.head.getTerm().getLabel()+')';
	}
	
	

}
