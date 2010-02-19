package org.caleydo.util.r;

import java.util.ArrayList;

import org.caleydo.core.data.collection.ISet;
import org.caleydo.core.data.collection.ccontainer.FloatCContainer;
import org.caleydo.core.data.collection.storage.EDataRepresentation;
import org.caleydo.core.data.selection.IVirtualArray;
import org.caleydo.core.manager.event.AEvent;
import org.caleydo.core.manager.event.AEventListener;
import org.caleydo.core.manager.event.IListenerOwner;
import org.caleydo.core.manager.event.view.grouper.CompareGroupsEvent;
import org.caleydo.core.manager.general.GeneralManager;
import org.caleydo.core.util.statistics.IStatisticsPerformer;
import org.caleydo.util.r.listener.CompareGroupsEventListener;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.rosuda.JRI.REXP;
import org.rosuda.JRI.Rengine;

public class RStatisticsPerformer implements IStatisticsPerformer, IListenerOwner {

	private Rengine engine;
	
	private CompareGroupsEventListener compareGroupsEventListener = null;
	
	public RStatisticsPerformer() {
		init();
		
		registerEventListeners();
	}
	
	@Override
	public void init() {
		// just making sure we have the right version of everything
		if (!Rengine.versionCheck()) {
			System.err
					.println("** Version mismatch - Java files don't match library version.");
			System.exit(1);
		}
		System.out.println("Creating Rengine (with arguments)");
		String[] args = new String[1];
		args[0] = "--no-save";
		engine = new Rengine(args, false, new RConsole());
		System.out.println("Rengine created, waiting for R");
		// the engine creates R is a new thread, so we should wait until it's
		// ready
		if (!engine.waitForR()) {
			System.out.println("Cannot load R");
			return;
		}
	}
	
	private void registerEventListeners() {
		
		compareGroupsEventListener = new CompareGroupsEventListener();
		compareGroupsEventListener.setHandler(this);
		GeneralManager.get().getEventPublisher().addListener(CompareGroupsEvent.class,
				compareGroupsEventListener);
	}
	
	//TODO: never called!
	public void unregisterEventListeners() {

		if (compareGroupsEventListener != null) {
			GeneralManager.get().getEventPublisher().removeListener(compareGroupsEventListener);
			compareGroupsEventListener = null;
		}
	}
	
	@Override
	public void performTest() {

		try {
			REXP test;
			int[] array = new int[]{223 ,259,248,220,287,191,229,270,245,201};//5, 6, 7};
			int[] array_2 = new int[]{220,244,243,211,299,170,210,276,252,189};//1, 2, 3};
			engine.assign("my_array", array);
			engine.assign("my_array_2", array_2);

			System.out.println("Array: " + engine.eval("my_array"));
			System.out.println("Array 2: " + engine.eval("my_array_2"));
			test = engine.eval("t.test(my_array,my_array_2)");
			System.out.println("T-Test result: " + test);
		} catch (Exception e) {
			System.out.println("EX:" + e);
			e.printStackTrace();
		}
	}
	
	public void compareSets(ArrayList<ISet> setsToCompare) {
		
		ISet set1 = setsToCompare.get(0);
		ISet set2 = setsToCompare.get(1);
	
		for (int contentIndex = 0; contentIndex < set1.get(0).size(); contentIndex++) {
			
			IVirtualArray storageVA1 = set1.createCompleteStorageVA();
			IVirtualArray storageVA2 = set2.createCompleteStorageVA();
		
			double[] compareVec1 = new double[storageVA1.size()];
			double[] compareVec2 = new double[storageVA2.size()];
			
			int storageCount = 0;
			for (Integer storageIndex : storageVA1) {
				compareVec1[storageCount++] = set1.get(storageIndex).getFloat(EDataRepresentation.NORMALIZED, contentIndex);				
			}
			
			storageCount = 0;
			for (Integer storageIndex : storageVA2) {
				compareVec2[storageCount++] = set2.get(storageIndex).getFloat(EDataRepresentation.NORMALIZED, contentIndex);				
			}
			
			engine.assign("set_1", compareVec1);
			engine.assign("set_2", compareVec2);
			
			REXP compareResult = engine.eval("t.test(set_1,set_2)");
			
			//System.out.println("T-Test result: " + compareResult);
			System.out.println("P-value: "+compareResult.asVector().get(2));
		}
		
		System.out.println("Finished");
	}

	@Override
	public synchronized void queueEvent(AEventListener<? extends IListenerOwner> listener,
			AEvent event) {

		compareGroupsEventListener.handleEvent(event);
	}
}
