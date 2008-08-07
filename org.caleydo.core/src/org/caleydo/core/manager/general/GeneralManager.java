package org.caleydo.core.manager.general;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.caleydo.core.manager.ICommandManager;
import org.caleydo.core.manager.IEventPublisher;
import org.caleydo.core.manager.IGeneralManager;
import org.caleydo.core.manager.IManager;
import org.caleydo.core.manager.IMementoManager;
import org.caleydo.core.manager.ISWTGUIManager;
import org.caleydo.core.manager.IViewGLCanvasManager;
import org.caleydo.core.manager.IXmlParserManager;
import org.caleydo.core.manager.command.CommandManager;
import org.caleydo.core.manager.data.ISelectionManager;
import org.caleydo.core.manager.data.ISetManager;
import org.caleydo.core.manager.data.IStorageManager;
import org.caleydo.core.manager.data.selection.SelectionManager;
import org.caleydo.core.manager.data.set.SetManager;
import org.caleydo.core.manager.data.storage.StorageManager;
import org.caleydo.core.manager.event.EventPublisher;
import org.caleydo.core.manager.gui.SWTGUIManager;
import org.caleydo.core.manager.memento.MementoManager;
import org.caleydo.core.manager.parser.XmlParserManager;
import org.caleydo.core.manager.specialized.genome.IGenomeIdManager;
import org.caleydo.core.manager.specialized.genome.IPathwayItemManager;
import org.caleydo.core.manager.specialized.genome.IPathwayManager;
import org.caleydo.core.manager.specialized.genome.id.GenomeIdManager;
import org.caleydo.core.manager.specialized.genome.pathway.PathwayItemManager;
import org.caleydo.core.manager.specialized.genome.pathway.PathwayManager;
import org.caleydo.core.manager.specialized.glyph.GlyphManager;
import org.caleydo.core.manager.specialized.glyph.IGlyphManager;
import org.caleydo.core.manager.view.ViewGLCanvasManager;
import org.caleydo.core.util.exception.CaleydoRuntimeException;
import org.caleydo.core.util.exception.CaleydoRuntimeExceptionType;
import org.eclipse.core.internal.resources.Folder;
import org.eclipse.jface.preference.PreferenceStore;

/**
 * General manager that contains all module managers.
 * 
 * @author Michael Kalkusch
 * @author Marc Streit
 */
public class GeneralManager
	implements IGeneralManager
{	
	public static final String PREFERENCE_FILE_NAME = "caleydo.prefs";
	public static final String USER_HOME = "user.home";
	public static final String CALEYDO_HOME = "/.caleydo/";
	
	/**
	 * Location where Caleydo stores preferences and caching data.
	 */
	private String sCaleydoHomePath;
	
	/**
	 * Preferences store enables storing and restoring of application specific preference data.
	 */
	private PreferenceStore preferenceStore;
	
	private boolean bAllManagersInitialized = false;

//	private ArrayList<IManager> alManagers;

	private IStorageManager storageManager;

	private IMementoManager mementoManager;

	// protected IVirtualArrayManager virtualArrayManager;

	private ISetManager setManager;

	private ISelectionManager selectionManager;

	private ICommandManager commandManager;

	private ISWTGUIManager sWTGUIManager;

	private IViewGLCanvasManager viewGLCanvasManager;

	private IPathwayManager pathwayManager;

	private IPathwayItemManager pathwayItemManager;

	private IEventPublisher eventPublisher;

	private IXmlParserManager xmlParserManager;

	private IGenomeIdManager genomeIdManager;

	private IGlyphManager glyphManager;

	private Logger logger;

	/**
	 * Constructor.
	 */
	public GeneralManager()
	{
//		alManagers = new ArrayList<IManager>();

		// Retrieve platform independent home directory
		sCaleydoHomePath = System.getProperty(USER_HOME);
		sCaleydoHomePath +=  CALEYDO_HOME;
		
		initLogger();
		initPreferences();
		initManager();
	}

	private void initPreferences() 
	{				
		preferenceStore = new PreferenceStore(sCaleydoHomePath + PREFERENCE_FILE_NAME);
		
		try
		{
			preferenceStore.load();
		}
		catch (IOException e)
		{
			logger.log(Level.INFO, "Create new preference store at "
					+sCaleydoHomePath + PREFERENCE_FILE_NAME);
			
			// Create .caleydo folder
			if (!(new File(sCaleydoHomePath).exists()))
			{
				if(!(new File(sCaleydoHomePath).mkdir()))
					throw new CaleydoRuntimeException("Unable to create home folder .caleydo. Check user permissions!", 
							CaleydoRuntimeExceptionType.DATAHANDLING);
			}
				
			try
			{				
				preferenceStore.setValue("firstStart", true);	
				preferenceStore.save();
			}
			catch (IOException e1)
			{
				throw new CaleydoRuntimeException("Unable to save preference file.", 
						CaleydoRuntimeExceptionType.DATAHANDLING);
			}
		}
		
		// Create log folder in .caleydo
		new File(sCaleydoHomePath + "logs").mkdirs();
	}
	
	/**
	 * Must be called right after the constructor before using this class.
	 * Initializes all manager objects.
	 */
	public void initManager()
	{
		if (bAllManagersInitialized)
		{
			throw new CaleydoRuntimeException("Tried to initialize managers multiple times. Abort.");
		}

		bAllManagersInitialized = true;

		storageManager = new StorageManager(this);
		// virtualArrayManager = new VirtualArrayManager(this, 4);
		setManager = new SetManager(this);
		selectionManager = new SelectionManager(this);
		mementoManager = new MementoManager(this);
		commandManager = new CommandManager(this);
		viewGLCanvasManager = new ViewGLCanvasManager(this);
		sWTGUIManager = new SWTGUIManager(this);
		eventPublisher = new EventPublisher(this);
		genomeIdManager = new GenomeIdManager(this);
		pathwayManager = new PathwayManager(this);
		// serializationInputTest();
		pathwayItemManager = new PathwayItemManager(this);
		xmlParserManager = new XmlParserManager(this);
		glyphManager = new GlyphManager(this);

//		alManagers.add(setManager);
//		// alManagers.add(virtualArrayManager);
//		alManagers.add(storageManager);
//		alManagers.add(selectionManager);
//		alManagers.add(pathwayManager);
//		alManagers.add(pathwayItemManager);
//		alManagers.add(genomeIdManager);
//		alManagers.add(eventPublisher);
//		alManagers.add(viewGLCanvasManager);
//		alManagers.add(sWTGUIManager);
//		alManagers.add(commandManager);
//		alManagers.add(mementoManager);
//		alManagers.add(glyphManager);
	}

	/**
	 * Initialize the Java internal logger
	 */
	private void initLogger()
	{
		logger = Logger.getLogger("Caleydo Log");
	}

	/*
	 * (non-Javadoc)
	 * @see org.caleydo.core.manager.IGeneralManager#getLogger()
	 */
	public final Logger getLogger()
	{
		return logger;
	}

	/*
	 * (non-Javadoc)
	 * @see org.caleydo.core.data.manager.GeneralManager#hasItem(int)
	 */
//	public boolean hasItem(final int iItemId)
//	{
//		Iterator<IManager> iter = alManagers.iterator();
//
//		while (iter.hasNext())
//		{
//			if (iter.next().hasItem(iItemId))
//				return true;
//		}
//
//		return false;
//	}

	/**
	 * @see org.caleydo.core.manager.IGeneralManager#hasItem(int)
	 * @param iItemId unique Id used for lookup
	 * @return Object bound to Id or null, if id was not found.
	 */
//	public Object getItem(final int iItemId)
//	{
//		Iterator<IManager> iter = alManagers.iterator();
//
//		while (iter.hasNext())
//		{
//			IManager buffer = iter.next();
//
//			if (buffer.hasItem(iItemId))
//			{
//				return buffer.getItem(iItemId);
//			}
//		}
//
//		return null;
//	}

	/*
	 * (non-Javadoc)
	 * @see org.caleydo.core.manager.IGeneralManager#getMementoManager()
	 */
	public IMementoManager getMementoManager()
	{
		return mementoManager;
	}

	/*
	 * (non-Javadoc)
	 * @see org.caleydo.core.manager.IGeneralManager#getStorageManager()
	 */
	public IStorageManager getStorageManager()
	{
		return storageManager;
	}

	/*
	 * (non-Javadoc)
	 * @see org.caleydo.core.manager.IGeneralManager#getVirtualArrayManager()
	 */
	// public IVirtualArrayManager getVirtualArrayManager() {
	// return virtualArrayManager;
	// }
	public ISelectionManager getSelectionManager()
	{
		return selectionManager;
	}

	/*
	 * (non-Javadoc)
	 * @see org.caleydo.core.manager.IGeneralManager#getSetManager()
	 */
	public ISetManager getSetManager()
	{
		return setManager;
	}

	/*
	 * (non-Javadoc)
	 * @see org.caleydo.core.manager.IGeneralManager#getViewGLCanvasManager()
	 */
	public IViewGLCanvasManager getViewGLCanvasManager()
	{
		return viewGLCanvasManager;
	}

	/*
	 * (non-Javadoc)
	 * @see org.caleydo.core.manager.IGeneralManager#getPathwayManager()
	 */
	public IPathwayManager getPathwayManager()
	{
		return pathwayManager;
	}

	/*
	 * (non-Javadoc)
	 * @see org.caleydo.core.manager.IGeneralManager#getPathwayItemManager()
	 */
	public IPathwayItemManager getPathwayItemManager()
	{
		return pathwayItemManager;
	}

	/*
	 * (non-Javadoc)
	 * @see org.caleydo.core.manager.IGeneralManager#getSWTGUIManager()
	 */
	public ISWTGUIManager getSWTGUIManager()
	{
		return sWTGUIManager;
	}

	/*
	 * (non-Javadoc)
	 * @see org.caleydo.core.manager.IGeneralManager#getEventPublisher()
	 */
	public IEventPublisher getEventPublisher()
	{
		return eventPublisher;
	}

	/*
	 * (non-Javadoc)
	 * @see org.caleydo.core.manager.IGeneralManager#getXmlParserManager()
	 */
	public IXmlParserManager getXmlParserManager()
	{
		return this.xmlParserManager;
	}

	/*
	 * (non-Javadoc)
	 * @see org.caleydo.core.manager.IGeneralManager#getGenomeIdManager()
	 */
	public IGenomeIdManager getGenomeIdManager()
	{
		return this.genomeIdManager;
	}

	/*
	 * (non-Javadoc)
	 * @see org.caleydo.core.manager.IGeneralManager#getCommandManager()
	 */
	public ICommandManager getCommandManager()
	{
		return commandManager;
	}

	/*
	 * (non-Javadoc)
	 * @see org.caleydo.core.manager.IGeneralManager#getCommandManager()
	 */
	public IGlyphManager getGlyphManager()
	{
		return glyphManager;
	}

	/*
	 * (non-Javadoc)
	 * @see org.caleydo.core.manager.IGeneralManager#getPreferenceStore()
	 */
	public PreferenceStore getPreferenceStore() 
	{
		return preferenceStore;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.caleydo.core.manager.IGeneralManager#getCaleydoHomePath()
	 */
	public String getCaleydoHomePath() 
	{
		return sCaleydoHomePath;
	}
	
	// public void serializationOutputTest() {
	//		
	// try
	// {
	// ObjectOutputStream out = new ObjectOutputStream(
	// new FileOutputStream("data/serialize_test.out"));
	//			
	// out.writeObject(pathwayManager);
	// out.close();
	//			
	// } catch (FileNotFoundException e)
	// {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// } catch (IOException e)
	// {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	// }
	//	
	// public void serializationInputTest() {
	//		
	// try
	// {
	// ObjectInputStream in = new ObjectInputStream(
	// new FileInputStream("data/serialize_test.out"));
	//			
	// pathwayManager = (PathwayManager) in.readObject();
	// in.close();
	//			
	// } catch (FileNotFoundException e)
	// {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// } catch (IOException e)
	// {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// } catch (ClassNotFoundException e)
	// {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	// }
}
