package org.caleydo.testing.command.data.filter;

import java.util.ArrayList;

import junit.framework.TestCase;

import org.caleydo.core.data.collection.ICContainer;
import org.caleydo.core.data.collection.ccontainer.NominalStringCContainer;
import org.caleydo.core.data.collection.ccontainer.PrimitiveFloatCContainer;

public class NominalStringCCollectionTest extends TestCase 
{
	NominalStringCContainer sCollection;
	ArrayList<String> sAlTestWords;

	protected void setUp() 
		throws Exception
	{
		super.setUp();
		sAlTestWords = new ArrayList<String>();
		sAlTestWords.add("Flu");
		sAlTestWords.add("Cancer");
		sAlTestWords.add("Flu");
		sAlTestWords.add("Gastritis");
		
		sCollection = new NominalStringCContainer(sAlTestWords);
	}
	
	public void testGet() {
		assertEquals(sCollection.get(0), sAlTestWords.get(0));
	
	}

	public void testNormalize() 
	{
		ICContainer normalizedStorage = sCollection.normalize();
		if(!(normalizedStorage instanceof PrimitiveFloatCContainer))
			fail("Should be primitive float");
		
		PrimitiveFloatCContainer normalizedFloatStorage = (PrimitiveFloatCContainer)normalizedStorage;
		assertEquals(sCollection.getDiscreteForNominalValue("Flu"), normalizedFloatStorage.get(0), 0.01);
		assertEquals(sCollection.getDiscreteForNominalValue("Cancer"), normalizedFloatStorage.get(1), 0.01);
		assertEquals(sCollection.getDiscreteForNominalValue("Flu"), normalizedFloatStorage.get(2), 0.01);
		assertEquals(sCollection.getDiscreteForNominalValue("Gastritis"), normalizedFloatStorage.get(3), 0.01);
	}

	public void testSize() {
		assertEquals(4, sCollection.size());
	}

	public void testGetNominalForDiscreteValue() {
	
		assertEquals("Flu",	sCollection.getNominalForDiscreteValue(sCollection.getDiscreteForNominalValue("Flu")));
		assertEquals("Cancer",	sCollection.getNominalForDiscreteValue(sCollection.getDiscreteForNominalValue("Cancer")));
		assertEquals("Gastritis",	sCollection.getNominalForDiscreteValue(sCollection.getDiscreteForNominalValue("Gastritis")));
		
		
		
	}

}
