/* $Revision: 7844 $ $Author: rajarshi $ $Date: 2007-04-08 14:46:29 -0500 (Thu, 01 Feb 2007) $
 *
 * Copyright (C) 2007  Rajarshi Guha <rajarshi@users.sourceforge.net>
 *
 * Contact: cdk-devel@lists.sourceforge.net
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package org.openscience.cdk.smiles.smarts;

import org.openscience.cdk.CDKConstants;
import org.openscience.cdk.aromaticity.HueckelAromaticityDetector;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.interfaces.IRingSet;
import org.openscience.cdk.isomorphism.UniversalIsomorphismTester;
import org.openscience.cdk.isomorphism.matchers.IQueryAtom;
import org.openscience.cdk.isomorphism.matchers.QueryAtomContainer;
import org.openscience.cdk.isomorphism.matchers.smarts.HydrogenAtom;
import org.openscience.cdk.isomorphism.matchers.smarts.LogicalOperatorAtom;
import org.openscience.cdk.isomorphism.matchers.smarts.RecursiveSmartsAtom;
import org.openscience.cdk.isomorphism.mcss.RMap;
import org.openscience.cdk.ringsearch.AllRingsFinder;
import org.openscience.cdk.ringsearch.SSSRFinder;
import org.openscience.cdk.tools.LoggingTool;

import java.util.*;

/**
 * This class provides a easy to use wrapper around SMARTS matching
 * functionality. <p/> User code that wants to do SMARTS matching should use
 * this rather than using SMARTSParser (and UniversalIsomorphismTester)
 * directly. Example usage would be
 * <p/>
 * <pre>
 * SmilesParser sp = new SmilesParser(DefaultChemObjectBuilder.getInstance());
 * IAtomContainer atomContainer = sp.parseSmiles(&quot;CC(=O)OC(=O)C&quot;);
 * SMARTSQueryTool querytool = new SMARTSQueryTool(&quot;O=CO&quot;);
 * boolean status = querytool.matches(atomContainer);
 * if (status) {
 *    int nmatch = querytool.countMatches();
 *    List mappings = querytool.getMatchingAtoms();
 *    for (int i = 0; i &lt; nmatch; i++) {
 *       List atomIndices = (List) mappings.get(i);
 *    }
 * }
 * </pre>
 * <p/>
 * To use the JJTree based Smarts Parser, use:
 * <p/>
 * <pre>
 * SMARTSQueryTool querytool = new SMARTSQueryTool(&quot;O=CO&quot;, true);
 * </pre>
 *
 * @author Rajarshi Guha
 * @cdk.created 2007-04-08
 * @cdk.module smarts
 * @cdk.keyword SMARTS
 * @cdk.keyword substructure search
 */
public class SMARTSQueryTool {
    private LoggingTool logger;
    private String smarts;
    private IAtomContainer atomContainer = null;
    private QueryAtomContainer query = null;

    private List<List<Integer>> matchingAtoms = null;
    /**
     * Whether to use JJTree based smarts parser
     */
    private boolean useJJTree = false;

    public SMARTSQueryTool(String smarts) throws CDKException {
        this(smarts, false);
    }

    public SMARTSQueryTool(String smarts, boolean useJJTree) throws CDKException {
        logger = new LoggingTool(this);
        this.smarts = smarts;
        this.useJJTree = useJJTree;
        initializeQuery();
    }

    /**
     * Returns the current SMARTS pattern being used.
     *
     * @return The SMARTS pattern
     */
    public String getSmarts() {
        return smarts;
    }

    /**
     * Set a new SMARTS pattern.
     *
     * @param smarts The new SMARTS pattern
     * @throws CDKException if there is an error in parsing the pattern
     */
    public void setSmarts(String smarts) throws CDKException {
        this.smarts = smarts;
        initializeQuery();
    }

    /**
     * Perform a SMARTS match and check whether the query is present in the
     * target molecule. <p/> This function simply checks whether the query
     * pattern matches the specified molecule. However the function will also,
     * internally, save the mapping of query atoms to the target molecule
     *
     * @param atomContainer The target moleculoe
     * @return true if the pattern is found in the target molecule, false
     *         otherwise
     * @throws CDKException if there is an error in ring, aromaticity or isomorphism
     *                      perception
     * @see #getMatchingAtoms()
     * @see #countMatches()
     */
    public boolean matches(IAtomContainer atomContainer) throws CDKException {
        // TODO: we should consider some sort of caching?
        this.atomContainer = atomContainer;
        initializeMolecule();
        
    	// First calculate the recursive smarts
    	initializeRecursiveSmarts(this.atomContainer);        

        // lets see if we have a single atom query
        if (query.getAtomCount() == 1) {
            // lets get the query atom
            IQueryAtom queryAtom = (IQueryAtom) query.getAtom(0);

            matchingAtoms = new ArrayList<List<Integer>>();
            Iterator<IAtom> atoms = this.atomContainer.atoms();
            while (atoms.hasNext()) {
                IAtom atom = atoms.next();
                if (queryAtom.matches(atom)) {
                    List<Integer> tmp = new ArrayList<Integer>();
                    tmp.add(this.atomContainer.getAtomNumber(atom));
                    matchingAtoms.add(tmp);
                }
            }
        } else {
            List bondMapping = UniversalIsomorphismTester.getSubgraphMaps(this.atomContainer, query);
            matchingAtoms = getAtomMappings(bondMapping, this.atomContainer);
        }

        return matchingAtoms.size() != 0;
    }

    /**
     * Returns the number of times the pattern was found in the target molecule.
     * <p/> This function should be called after
     * {@link #matches(org.openscience.cdk.interfaces.IAtomContainer)}. If not,
     * the results may be undefined.
     *
     * @return The number of times the pattern was found in the target molecule
     */
    public int countMatches() {
        return matchingAtoms.size();
    }

    /**
     * Get the atoms in the target molecule that match the query pattern. <p/>
     * Since there may be multiple matches, the return value is a List of List
     * objects. Each List object contains the indices of the atoms in the target
     * molecule, that match the query pattern
     *
     * @return A List of List of atom indices in the target molecule
     */
    public List<List<Integer>> getMatchingAtoms() {
        return matchingAtoms;
    }

    /**
     * Get the atoms in the target molecule that match the query pattern. <p/>
     * Since there may be multiple matches, the return value is a List of List
     * objects. Each List object contains the unique set of indices of the atoms in the target
     * molecule, that match the query pattern
     *
     * @return A List of List of atom indices in the target molecule
     */

    public List<List<Integer>> getUniqueMatchingAtoms() {
        List<List<Integer>> ret = new ArrayList<List<Integer>>();
        for (List<Integer> atomMapping : matchingAtoms) {
            Collections.sort(atomMapping);

            // see if this sequence of atom indices is present
            // in the return container
            boolean present = false;
            for (List<Integer> r : ret) {
                if (r.size() != atomMapping.size()) continue;
                Collections.sort(r);
                boolean matches = true;
                for (int i = 0; i < atomMapping.size(); i++) {
                    int index1 = atomMapping.get(i);
                    int index2 = r.get(i);
                    if (index1 != index2) {
                        matches = false;
                        break;
                    }
                }
                if (matches) {
                    present = true;
                    break;
                }
            }
            if (!present) ret.add(atomMapping);
        }
        return ret;
    }

    /**
     * Prepare the target molecule for analysis. <p/> We perform ring perception
     * and aromaticity detection and set up the appropriate properties. Right
     * now, this function is called each time we need to do a query and this is
     * inefficient.
     *
     * @throws CDKException if there is a problem in ring perception or aromaticity
     *                      detection, which is usually related to a timeout in the ring
     *                      finding code.
     */
    private void initializeMolecule() throws CDKException {
        // Code copied from 
        // org.openscience.cdk.qsar.descriptors.atomic.AtomValenceDescriptor;
        Map<String, Integer> valencesTable = new HashMap<String, Integer>();
        valencesTable.put("H", 1);
        valencesTable.put("Li", 1);
        valencesTable.put("Be", 2);
        valencesTable.put("B", 3);
        valencesTable.put("C", 4);
        valencesTable.put("N", 5);
        valencesTable.put("O", 6);
        valencesTable.put("F", 7);
        valencesTable.put("Na", 1);
        valencesTable.put("Mg", 2);
        valencesTable.put("Al", 3);
        valencesTable.put("Si", 4);
        valencesTable.put("P", 5);
        valencesTable.put("S", 6);
        valencesTable.put("Cl", 7);
        valencesTable.put("K", 1);
        valencesTable.put("Ca", 2);
        valencesTable.put("Ga", 3);
        valencesTable.put("Ge", 4);
        valencesTable.put("As", 5);
        valencesTable.put("Se", 6);
        valencesTable.put("Br", 7);
        valencesTable.put("Rb", 1);
        valencesTable.put("Sr", 2);
        valencesTable.put("In", 3);
        valencesTable.put("Sn", 4);
        valencesTable.put("Sb", 5);
        valencesTable.put("Te", 6);
        valencesTable.put("I", 7);
        valencesTable.put("Cs", 1);
        valencesTable.put("Ba", 2);
        valencesTable.put("Tl", 3);
        valencesTable.put("Pb", 4);
        valencesTable.put("Bi", 5);
        valencesTable.put("Po", 6);
        valencesTable.put("At", 7);
        valencesTable.put("Fr", 1);
        valencesTable.put("Ra", 2);
        valencesTable.put("Cu", 2);
        valencesTable.put("Mn", 2);
        valencesTable.put("Co", 2);

        // do all ring perception
        AllRingsFinder arf = new AllRingsFinder();
        IRingSet allRings;
        try {
            allRings = arf.findAllRings(atomContainer);
        } catch (CDKException e) {
            logger.debug(e.toString());
            throw new CDKException(e.toString());
        }

        // sets SSSR information
        SSSRFinder finder = new SSSRFinder(atomContainer);
        IRingSet sssr = finder.findEssentialRings();

        Iterator<IAtom> atoms = atomContainer.atoms();
        while (atoms.hasNext()) {
            IAtom atom = atoms.next();

            // add a property to each ring atom that will be an array of
            // Integers, indicating what size ring the given atom belongs to
            // Add SSSR ring counts
            if (allRings.contains(atom)) { // it's in a ring
                atom.setFlag(CDKConstants.ISINRING, true);
                // lets find which ring sets it is a part of
                List<Integer> ringsizes = new ArrayList<Integer>();
                IRingSet currentRings = allRings.getRings(atom);
                int min = 0;
                for (int i = 0; i < currentRings.getAtomContainerCount(); i++) {
                    int size = currentRings.getAtomContainer(i).getAtomCount();
                    if (min > size) min = size;
                    ringsizes.add(size);
                }
                atom.setProperty(CDKConstants.RING_SIZES, ringsizes);
                atom.setProperty(CDKConstants.SMALLEST_RINGS, sssr.getRings(atom));
            }

            // determine how many rings bonds each atom is a part of
            int hCount;
            if (atom.getHydrogenCount() == CDKConstants.UNSET) hCount = 0;
            else hCount = atom.getHydrogenCount();

            List<IAtom> connectedAtoms = atomContainer.getConnectedAtomsList(atom);
            int total = hCount + connectedAtoms.size();
            for (IAtom connectedAtom : connectedAtoms) {
                if (connectedAtom.getSymbol().equals("H")) {                    
                    hCount++;
                }
            }
            atom.setProperty(CDKConstants.TOTAL_CONNECTIONS, total);
            atom.setProperty(CDKConstants.TOTAL_H_COUNT, hCount);

            if (valencesTable.get(atom.getSymbol()) != null) {
                atom.setValency(valencesTable.get(atom.getSymbol()) -
                        atom.getFormalCharge());
            }
        }

        Iterator<IBond> bonds = atomContainer.bonds();
        while (bonds.hasNext()) {
            IBond bond = bonds.next();
            if (allRings.getRings(bond).size() > 0) {
                bond.setFlag(CDKConstants.ISINRING, true);
            }
        }

        atoms = atomContainer.atoms();
        while (atoms.hasNext()) {
            IAtom atom = atoms.next();
            List<IAtom> connectedAtoms = atomContainer.getConnectedAtomsList(atom);

            int counter = 0;
            IAtom any;
            for (IAtom connectedAtom : connectedAtoms) {
                any = connectedAtom;
                if (any.getFlag(CDKConstants.ISINRING)) {
                    counter++;
                }
            }
            atom.setProperty(CDKConstants.RING_CONNECTIONS, counter);
        }

        // check for atomaticity
        try {
            HueckelAromaticityDetector.detectAromaticity(atomContainer, true, arf);
        } catch (CDKException e) {
            logger.debug(e.toString());
            throw new CDKException(e.toString());
        }

    }
    
    /**
     * Initializes recursive smarts atoms in the query
     * 
     * @param atomContainer
     * @throws CDKException
     */
    private void initializeRecursiveSmarts(IAtomContainer atomContainer) throws CDKException {
    	for (Iterator<IAtom> it = this.query.atoms(); it.hasNext(); ) {
			IAtom atom = it.next();
			initializeRecursiveSmartsAtom(atom, atomContainer);
		}
    }
    
    /**
     * Recursively initializes recursive smarts atoms
     * 
     * @param atom
     * @param atomContainer
     * @throws CDKException
     */
    private void initializeRecursiveSmartsAtom(IAtom atom, IAtomContainer atomContainer) throws CDKException {
    	if (atom instanceof LogicalOperatorAtom) {
    		initializeRecursiveSmartsAtom(((LogicalOperatorAtom)atom).getLeft(), atomContainer);
    		if (((LogicalOperatorAtom)atom).getRight() != null) {
    			initializeRecursiveSmartsAtom(((LogicalOperatorAtom)atom).getRight(), atomContainer);	
    		}
    	} else if (atom instanceof RecursiveSmartsAtom) {
            ((RecursiveSmartsAtom)atom).setAtomContainer(atomContainer);
    	} else if (atom instanceof HydrogenAtom) {
    		((HydrogenAtom)atom).setAtomContainer(atomContainer);
    	}
    }    

    private void initializeQuery() throws CDKException {
        matchingAtoms = null;
        if (useJJTree) {
            query = org.openscience.cdk.smiles.smarts.parser.SMARTSParser.parse(smarts);
        } else {
            query = SMARTSParser.parse(smarts);
        }
    }


    private List<List<Integer>> getAtomMappings(List bondMapping, IAtomContainer atomContainer) {
        List<List<Integer>> atomMapping = new ArrayList<List<Integer>>();

        // loop over each mapping
        for (Object aBondMapping : bondMapping) {
            List list = (List) aBondMapping;

            List<Integer> tmp = new ArrayList<Integer>();
            IAtom atom1 = null;
            IAtom atom2 = null;
            // loop over this mapping
            for (Object aList : list) {
                RMap map = (RMap) aList;
                int bondID = map.getId1();

                // get the atoms in this bond
                IBond bond = atomContainer.getBond(bondID);
                atom1 = bond.getAtom(0);
                atom2 = bond.getAtom(1);

                Integer idx1 = atomContainer.getAtomNumber(atom1);
                Integer idx2 = atomContainer.getAtomNumber(atom2);

                if (!tmp.contains(idx1)) tmp.add(idx1);
                if (!tmp.contains(idx2)) tmp.add(idx2);
            }
            if (tmp.size() > 0) atomMapping.add(tmp);
            
            // If there is only one bond, check if it matches both ways.
            if (list.size() == 1 && atom1.getAtomicNumber() == atom2.getAtomicNumber()) {
            	List<Integer> tmp2 = new ArrayList<Integer>();
            	tmp2.add(tmp.get(0));
            	tmp2.add(tmp.get(1));
            	atomMapping.add(tmp2);
            }
        }
        
        
        return atomMapping;
    }
}
