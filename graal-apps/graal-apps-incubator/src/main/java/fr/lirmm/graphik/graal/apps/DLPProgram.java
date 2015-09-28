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
package fr.lirmm.graphik.graal.apps;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;

import fr.lirmm.graphik.graal.api.core.Atom;
import fr.lirmm.graphik.graal.api.core.ConjunctiveQuery;
import fr.lirmm.graphik.graal.api.core.InMemoryAtomSet;
import fr.lirmm.graphik.graal.api.core.NegativeConstraint;
import fr.lirmm.graphik.graal.api.core.Rule;
import fr.lirmm.graphik.graal.api.core.RuleSet;
import fr.lirmm.graphik.graal.api.core.Substitution;
import fr.lirmm.graphik.graal.api.forward_chaining.ChaseException;
import fr.lirmm.graphik.graal.api.homomorphism.HomomorphismException;
import fr.lirmm.graphik.graal.api.homomorphism.HomomorphismFactoryException;
import fr.lirmm.graphik.graal.core.factory.AtomSetFactory;
import fr.lirmm.graphik.graal.core.ruleset.LinkedListRuleSet;
import fr.lirmm.graphik.graal.forward_chaining.StaticChase;
import fr.lirmm.graphik.graal.homomorphism.StaticHomomorphism;
import fr.lirmm.graphik.graal.io.dlp.DlgpParser;
import fr.lirmm.graphik.graal.io.dlp.DlgpWriter;

/**
 * @author Clément Sipieter (INRIA) <clement@6pi.fr>
 *
 */
public class DLPProgram {
	public static void main(String[] args) throws ChaseException, IOException, HomomorphismFactoryException, HomomorphismException {
		DlgpWriter writer = new DlgpWriter();
		DlgpParser parser = new DlgpParser(System.in);
		if(args.length > 0)
			 parser = new DlgpParser(new File(args[0]));
		
		InMemoryAtomSet atomSet = AtomSetFactory.instance().createAtomSet();
		RuleSet ruleSet = new LinkedListRuleSet();
		LinkedList<ConjunctiveQuery> querySet = new LinkedList<ConjunctiveQuery>();
		
        for(Object o : parser) {
        	if(o instanceof NegativeConstraint)
        		System.err.println("Constraint not supported");
        	else if(o instanceof Rule)
        		ruleSet.add((Rule)o);
        	else if(o instanceof Atom)
        		atomSet.add((Atom)o);
        	else if(o instanceof ConjunctiveQuery)
        		querySet.add((ConjunctiveQuery)o);
        }
        parser.close();
        
        StaticChase.executeChase(atomSet, ruleSet);
        writer.write("% facts\n");
        writer.write(atomSet);
        
        writer.write("\n\n% queries\n");
        for(ConjunctiveQuery q : querySet) {
        	writer.write(q);
        	for(Substitution s : StaticHomomorphism.executeQuery(q, atomSet)) {
        		writer.write(s.toString());
        		writer.write("\n");
        	}
        }
        writer.flush();

	}
}
