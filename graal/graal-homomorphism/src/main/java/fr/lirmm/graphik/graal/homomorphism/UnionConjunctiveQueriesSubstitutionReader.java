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
package fr.lirmm.graphik.graal.homomorphism;


import fr.lirmm.graphik.graal.api.core.AtomSet;
import fr.lirmm.graphik.graal.api.core.ConjunctiveQuery;
import fr.lirmm.graphik.graal.api.core.Query;
import fr.lirmm.graphik.graal.api.core.Substitution;
import fr.lirmm.graphik.graal.api.homomorphism.Homomorphism;
import fr.lirmm.graphik.graal.api.homomorphism.HomomorphismException;
import fr.lirmm.graphik.graal.core.UnionConjunctiveQueries;
import fr.lirmm.graphik.util.stream.CloseableIterator;
import fr.lirmm.graphik.util.stream.GIterator;

/**
 * @author Clément Sipieter (INRIA) <clement@6pi.fr>
 *
 */
public class UnionConjunctiveQueriesSubstitutionReader implements CloseableIterator<Substitution> {

    private AtomSet atomSet;
	private GIterator<ConjunctiveQuery> cqueryIterator;
	private CloseableIterator<Substitution> tmpReader;
    private boolean hasNextCallDone = false;
    
    /**
     * @param queries
     * @param atomSet
     */
    public UnionConjunctiveQueriesSubstitutionReader(UnionConjunctiveQueries queries,
            AtomSet atomSet) {
        this.cqueryIterator = queries.iterator();
        this.atomSet = atomSet;
        this.tmpReader = null;
    }

    @Override
    public boolean hasNext() {
        if (!this.hasNextCallDone) {
            this.hasNextCallDone = true;

            while ((this.tmpReader == null || !this.tmpReader.hasNext())
                    && this.cqueryIterator.hasNext()) {
				if (!this.tmpReader.hasNext()) {
					this.tmpReader.close();
				}
                Query q = this.cqueryIterator.next();
                Homomorphism solver;
                try {
                    solver = DefaultHomomorphismFactory.instance().getSolver(q, this.atomSet);
                    if(solver == null) {
                    	return false;
                    } else {
                    	this.tmpReader = solver.execute(q, this.atomSet);
                    }
                } catch (HomomorphismException e) {
                    return false;
                }
            }
        }
        return this.tmpReader != null && this.tmpReader.hasNext();
    }

    @Override
    public Substitution next() {
        if (!this.hasNextCallDone)
            this.hasNext();

        this.hasNextCallDone = false;

        return this.tmpReader.next();
    }

    @Override
    public void close() {
		this.tmpReader.close();
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
    
    

}
