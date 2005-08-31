/* $RCSfile$
 * $Author$    
 * $Date$    
 * $Revision$
 * 
 * Copyright (C) 1997-2005  The Chemistry Development Kit (CDK) project
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
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA. 
 * 
 */

package org.openscience.cdk.test;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.openscience.cdk.ChemFile;
import org.openscience.cdk.ChemObjectListener;
import org.openscience.cdk.ChemSequence;
import org.openscience.cdk.interfaces.ChemObjectChangeEvent;

/**
 * Checks the funcitonality of the ChemSequence class.
 *
 * @cdk.module test
 *
 * @see org.openscience.cdk.ChemSequence
 */
public class ChemFileTest extends CDKTestCase {

    public ChemFileTest(String name) {
        super(name);
    }

    public void setUp() {}

    public static Test suite() {
        return new TestSuite(ChemFileTest.class);
    }
    
    public void testChemFile() {
        ChemFile cs = new ChemFile();
        assertNotNull(cs);
    }

    public void testAddChemSequence_ChemSequence() {
        ChemFile cs = new ChemFile();
        cs.addChemSequence(new ChemSequence());
        cs.addChemSequence(new ChemSequence());
        cs.addChemSequence(new ChemSequence());
        assertEquals(3, cs.getChemSequenceCount());
    }
    
    public void testGetChemSequence_int() {
        ChemFile cs = new ChemFile();
        cs.addChemSequence(new ChemSequence());
        ChemSequence second = new ChemSequence();
        cs.addChemSequence(second);
        cs.addChemSequence(new ChemSequence());
        assertEquals(second, cs.getChemSequence(1));
    }
    
    public void testGrowChemSequenceArray() {
        ChemFile cs = new ChemFile();
        cs.addChemSequence(new ChemSequence());
        cs.addChemSequence(new ChemSequence());
        cs.addChemSequence(new ChemSequence());
        assertEquals(3, cs.getChemSequenceCount());
        cs.addChemSequence(new ChemSequence());
        cs.addChemSequence(new ChemSequence());
        cs.addChemSequence(new ChemSequence()); // this one should enfore array grow
        assertEquals(6, cs.getChemSequenceCount());
    }

    public void testGetChemSequences() {
        ChemFile cs = new ChemFile();
        cs.addChemSequence(new ChemSequence());
        cs.addChemSequence(new ChemSequence());
        cs.addChemSequence(new ChemSequence());

        assertNotNull(cs.getChemSequences());
        assertEquals(3, cs.getChemSequences().length);
    }

    public void testGetChemSequenceCount() {
        ChemFile cs = new ChemFile();
        cs.addChemSequence(new ChemSequence());
        cs.addChemSequence(new ChemSequence());
        cs.addChemSequence(new ChemSequence());
 
        assertEquals(3, cs.getChemSequenceCount());
    }

    /** Test for RFC #9 */
    public void testToString() {
        ChemFile cs = new ChemFile();
        String description = cs.toString();
        for (int i=0; i< description.length(); i++) {
            assertTrue(description.charAt(i) != '\n');
            assertTrue(description.charAt(i) != '\r');
        }
    }

    public void testStateChanged_ChemObjectChangeEvent() {
        ChemObjectListenerImpl listener = new ChemObjectListenerImpl();
        ChemFile chemObject = new ChemFile();
        chemObject.addListener(listener);
        
        chemObject.addChemSequence(new ChemSequence());
        assertTrue(listener.changed);
    }

    private class ChemObjectListenerImpl implements ChemObjectListener {
        private boolean changed;
        
        private ChemObjectListenerImpl() {
            changed = false;
        }
        
        public void stateChanged(ChemObjectChangeEvent e) {
            changed = true;
        }
        
        public void reset() {
            changed = false;
        }
    }

	public void testClone() {
        ChemFile file = new ChemFile();
        Object clone = file.clone();
        assertTrue(clone instanceof ChemFile);
    }    
        
    public void testClone_ChemSequence() {
		ChemFile file = new ChemFile();
		file.addChemSequence(new ChemSequence()); // 1
		file.addChemSequence(new ChemSequence()); // 2
		file.addChemSequence(new ChemSequence()); // 3
		file.addChemSequence(new ChemSequence()); // 4

		ChemFile clone = (ChemFile)file.clone();
		assertEquals(file.getChemSequenceCount(), clone.getChemSequenceCount());
		for (int f = 0; f < file.getChemSequenceCount(); f++) {
			for (int g = 0; g < clone.getChemSequenceCount(); g++) {
				assertNotNull(file.getChemSequence(f));
				assertNotNull(clone.getChemSequence(g));
				assertNotSame(file.getChemSequence(f), clone.getChemSequence(g));
			}
		}        
    }
}
