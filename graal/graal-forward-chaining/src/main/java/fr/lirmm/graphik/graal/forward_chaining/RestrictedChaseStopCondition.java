/**
 * 
 */
package fr.lirmm.graphik.graal.forward_chaining;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.lirmm.graphik.graal.core.Query;
import fr.lirmm.graphik.graal.core.Term;
import fr.lirmm.graphik.graal.core.atomset.ReadOnlyAtomSet;
import fr.lirmm.graphik.graal.homomorphism.HomomorphismException;
import fr.lirmm.graphik.graal.homomorphism.HomomorphismFactoryException;
import fr.lirmm.graphik.graal.homomorphism.StaticHomomorphism;

/**
 * @author Clément Sipieter (INRIA) <clement@6pi.fr>
 *
 */
public class RestrictedChaseStopCondition implements ChaseStopCondition {

	private static final Logger logger = LoggerFactory
			.getLogger(RestrictedChaseStopCondition.class);
	
	/* (non-Javadoc)
	 * @see fr.lirmm.graphik.alaska.chase.ChaseStopCondition#canIAdd(fr.lirmm.graphik.kb.core.AtomSet)
	 */
	@Override
	public boolean canIAdd(ReadOnlyAtomSet atomSet, Set<Term> fixedTerms, ReadOnlyAtomSet base) throws HomomorphismFactoryException, HomomorphismException {
		
		Query query = new ConjunctiveQueryWithFixedVariables(atomSet, fixedTerms);
		if(logger.isDebugEnabled()) {
			logger.debug("Fixed Query:" + query);
		}
		if (StaticHomomorphism.executeQuery(query, base).hasNext()) {
			return false;
		}
		return true;
	}

}