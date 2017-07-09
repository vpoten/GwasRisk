/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.clados.gwasrisk.metaheuristic;

import java.util.Comparator;

/**
 *
 * @author victor
 */
public class SolutionComparator implements Comparator<IMetaheuristicSolution> {

    /**
     * Returns a negative integer, zero, or a positive integer as the first
     * argument is less than, equal to, or greater than the second
     * @param o1
     * @param o2
     * @return
     */
    public int compare(IMetaheuristicSolution o1, IMetaheuristicSolution o2) {

        if( o1==null && o2==null)
            return 0;

        if( o1!=null && o2==null)
            return 1;

        if( o1==null && o2!=null)
            return -1;

        if( o1.isBetter(o2) )
            return 1;

        return -1;
    }


}
