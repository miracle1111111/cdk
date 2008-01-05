/* $Revision: 7691 $ $Author: egonw $ $Date: 2007-01-11 12:47:48 +0100 (Thu, 11 Jan 2007) $
 * 
 * Copyright (C) 2007  Egon Willighagen <egonw@users.sf.net>
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
package org.openscience.cdk.test.qsar;

import org.junit.Assert;
import org.junit.Test;
import org.openscience.cdk.qsar.DescriptorSpecification;
import org.openscience.cdk.qsar.DescriptorValue;
import org.openscience.cdk.qsar.result.DoubleResult;
import org.openscience.cdk.test.NewCDKTestCase;

/**
 * @cdk.module test-standard
 */
public class DescriptorValueTest extends NewCDKTestCase {
    
    public DescriptorValueTest() {
        super();
    }

	private final static String DESC_REF = "bla"; 
	private final static String DESC_IMPL_TITLE = "bla2"; 
	private final static String DESC_IMPL_VENDOR = "bla3"; 
	private final static String DESC_IMPL_ID = "bla4"; 

    @Test
    public void testDescriptorValue_DescriptorSpecification_arrayString_arrayObject_IDescriptorResult_arrayString() {
		DescriptorSpecification spec = new DescriptorSpecification(
	        DESC_REF, DESC_IMPL_TITLE, DESC_IMPL_ID, DESC_IMPL_VENDOR
		);
		DescriptorValue value = new DescriptorValue(
			spec, new String[0], new Object[0], 
			new DoubleResult(0.7), 
			new String[]{ "bla" }
		);
		Assert.assertNotNull(value);
	}

    @Test
    public void testDescriptorValue_DescriptorSpecification_arrayString_arrayObject_IDescriptorResult() {
		DescriptorSpecification spec = new DescriptorSpecification(
	        DESC_REF, DESC_IMPL_TITLE, DESC_IMPL_ID, DESC_IMPL_VENDOR
		);
		DescriptorValue value = new DescriptorValue(
			spec, new String[0], new Object[0], 
			new DoubleResult(0.7)
		);
		Assert.assertNotNull(value);
	}

    @Test
    public void testGetValue() {
		DescriptorSpecification spec = new DescriptorSpecification(
	        DESC_REF, DESC_IMPL_TITLE, DESC_IMPL_ID, DESC_IMPL_VENDOR
		);
		DoubleResult doubleVal = new DoubleResult(0.7); 
		DescriptorValue value = new DescriptorValue(
			spec, new String[0], new Object[0], 
			doubleVal, 
			new String[]{ "bla" }
		);
		Assert.assertEquals(doubleVal, value.getValue());
	}

    @Test
    public void testGetSpecification() {
		DescriptorSpecification spec = new DescriptorSpecification(
	        DESC_REF, DESC_IMPL_TITLE, DESC_IMPL_ID, DESC_IMPL_VENDOR
		);
		DoubleResult doubleVal = new DoubleResult(0.7); 
		DescriptorValue value = new DescriptorValue(
			spec, new String[0], new Object[0], 
			doubleVal, 
			new String[]{ "bla" }
		);
		Assert.assertEquals(spec, value.getSpecification());
	}

    @Test
    public void testGetParameters() {
		DescriptorSpecification spec = new DescriptorSpecification(
	        DESC_REF, DESC_IMPL_TITLE, DESC_IMPL_ID, DESC_IMPL_VENDOR
		);
		DoubleResult doubleVal = new DoubleResult(0.7); 
		DescriptorValue value = new DescriptorValue(
			spec, new String[0], new Object[0], 
			doubleVal, 
			new String[]{ "bla" }
		);
		Assert.assertEquals(0, value.getParameters().length);
	}

    @Test
    public void testGetParameterNames() {
		DescriptorSpecification spec = new DescriptorSpecification(
	        DESC_REF, DESC_IMPL_TITLE, DESC_IMPL_ID, DESC_IMPL_VENDOR
		);
		DoubleResult doubleVal = new DoubleResult(0.7); 
		DescriptorValue value = new DescriptorValue(
			spec, new String[0], new Object[0], 
			doubleVal, 
			new String[]{ "bla" }
		);
		Assert.assertEquals(0, value.getParameterNames().length);
	}

    @Test
    public void testGetNames() {
		DescriptorSpecification spec = new DescriptorSpecification(
	        DESC_REF, DESC_IMPL_TITLE, DESC_IMPL_ID, DESC_IMPL_VENDOR
		);
		DoubleResult doubleVal = new DoubleResult(0.7); 
		DescriptorValue value = new DescriptorValue(
			spec, new String[0], new Object[0], 
			doubleVal, 
			new String[]{ "bla" }
		);
		Assert.assertEquals(1, value.getNames().length);
	}
}


