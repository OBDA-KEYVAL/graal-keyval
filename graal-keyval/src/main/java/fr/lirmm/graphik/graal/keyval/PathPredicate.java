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

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

import fr.lirmm.graphik.graal.api.core.Predicate;

import javax.sql.RowSet;

import org.apache.commons.lang3.NotImplementedException;

import fr.lirmm.graphik.util.MethodNotImplementedError;

/**
 * @author Clément Sipieter (INRIA) {@literal <clement@6pi.fr>}
 *	@author Barthélémy Antonin (UM) {@literal <antonin.barthelemy@etu.umontpellier.fr}
 *	
 *	On choisie de ne pas interphaser l'objet sur javax.sql.rowSet.Predicat
 */
public class PathPredicate {

	///////////////////////////
	//	Attributs
	///////////////////////////
	private ArrayList<Predicate> predicatesList ;
	private Integer pathSize;
	
	//////////////////////////
	//	Methodes
	//////////////////////////
	public PathPredicate(){
		pathSize = 0;
		predicatesList = new ArrayList<Predicate>();
	}
	
	public PathPredicate(ArrayList<Predicate> arrPredicates){
		predicatesList = arrPredicates;
		pathSize = arrPredicates.size();
	}
	
	public void addPredicate(Predicate predicate){
		predicatesList.add(predicate);
		pathSize++;
	}
	
	public void addPredicate(Predicate predicate, int position){
		this.predicatesList.add(position, predicate);
		pathSize++;
	}
	
	public ArrayList<String> predicatesToStrings(){
		ArrayList<String> keysPred = new ArrayList<String>();
		for(Predicate pred : predicatesList){
			keysPred.add((String) pred.getIdentifier());
		}
		return keysPred;
	}
	
	public Integer getSizePath(){
		return pathSize;
	}
	
	public ArrayList<Predicate> getPredicates() {
		return predicatesList;
	}
	
	public Predicate getPredicateIndex(Integer i){
		if(i>=pathSize)
			return null;
		else
			return predicatesList.get(i);
	}
	
	public String toFieldName(){
		String res = "";
		Iterator<Predicate> itrPre = predicatesList.iterator();
		while(itrPre.hasNext()){
			String pre = (String)itrPre.next().getIdentifier();
			if(!itrPre.hasNext()){
				res = res+pre;
			}
			else{
				res = res + pre + ".";
			}
		}
		return res;
	}
	
	public String toString(){
		return predicatesList+":"+pathSize;
	}
	
	

}
