/**
 * 
 */
package fr.lirmm.graphik.graal.transformation;

import fr.lirmm.graphik.graal.core.ConjunctiveQuery;
import fr.lirmm.graphik.graal.core.Query;
import fr.lirmm.graphik.graal.core.atomset.ReadOnlyAtomSet;
import fr.lirmm.graphik.graal.solver.Solver;
import fr.lirmm.graphik.graal.solver.checker.AbstractSolverChecker;

/**
 * @author Clément Sipieter (INRIA) {@literal <clement@6pi.fr>}
 *
 */
public class TransformatorSolverChecker extends AbstractSolverChecker {

	@Override
	public boolean check(Query query, ReadOnlyAtomSet atomset) {
		return query instanceof ConjunctiveQuery && atomset instanceof ReadOnlyTransformStore;
	}

	@Override
	public Solver getSolver(Query query, ReadOnlyAtomSet atomset) {
		return new TransformatorSolver((ConjunctiveQuery) query, atomset);
	}

	@Override
	public int getDefaultPriority() {
		return 101;
	}

}