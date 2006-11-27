/*
 * Project: GenView
 * 
 * Author: Michael Kalkusch
 * 
 *  creation date: 18-05-2005
 *  
 */
package cerberus.net.dwt.swing.collection;

import java.awt.LayoutManager;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Color;
import java.util.LinkedList;
import java.util.Vector;
import java.util.Iterator;
import java.awt.Rectangle;
//import java.io.IOException;

import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.border.LineBorder;
import javax.swing.ListSelectionModel;
import javax.swing.Box;
import javax.swing.BoxLayout;

import cerberus.manager.IDistComponentManager;
import cerberus.manager.IGeneralManager;
import cerberus.manager.data.IVirtualArrayManager;
import cerberus.manager.data.ISetManager;
import cerberus.manager.data.IStorageManager;
import cerberus.manager.singelton.IGeneralManagerSingelton;
import cerberus.manager.type.ManagerObjectType;

//import org.xml.sax.InputSource;
//import org.xml.sax.SAXException;
//import org.xml.sax.XMLReader;
//import org.xml.sax.helpers.XMLReaderFactory;

import cerberus.data.collection.ISet;
import cerberus.data.collection.IStorage;
import cerberus.data.collection.view.IViewCanvas;
import cerberus.command.ICommandListener;
import cerberus.net.dwt.DNetEvent;
import cerberus.net.dwt.DNetEventComponentInterface;
import cerberus.net.dwt.DNetEventListener;
import cerberus.net.dwt.swing.collection.DSwingStorageTabbedPane;
import cerberus.xml.parser.ISaxParserHandler;
import cerberus.net.dwt.swing.parser.DSwingHistogramCanvasHandler;
import cerberus.net.protocol.interaction.SuperMouseEvent;

import cerberus.util.exception.CerberusRuntimeException;



/**
 * GUI for handling ISet, Selections and IStorage objects.
 * Visual link to SetManger, SelectionManger and IStorageManager.
 * 
 * @author Michael Kalkusch
 *
 */
public class DSwingStorageCanvas 
extends JPanel 
implements DNetEventComponentInterface, IViewCanvas
{

	static final long serialVersionUID = 80008070;
	
	static final private int iTabOffsetXML = 2;
	
	protected int iDNetEventComponentId;
	
	private Vector<DSwingStorageTabbedPane> vecJListRows;
	
	//private JList j_mainBox;
	//private JPanel j_mainBox;
	
	private Box j_mainBox;
	
	private int iActiveJListItems = 0;
	
	private String[] sHeaderListLabel = { "type", 
			"Id" ,
			"label", 
			"offset", 
			"len",
			"rep",
			"inc", 
			"random" };
	
	/**
	 * Reference to parent and/or creator of this class.
	 * Used to check, if id was changed by creator.
	 * 
	 * TODO: remove this from stable code!
	 */
	private IDistComponentManager refParentCreator;
	
	/**
	 * reference to parent object.
	 */
	private DNetEventComponentInterface setParentComponent = this;
	
	
	protected SuperMouseEvent refMouseNetEvent;
	
	/**
	 * stores references to Command listener objects.
	 */
	private Vector<ICommandListener> vecRefCommandListener;
	
	private Vector<DNetEventComponentInterface> vecRefComponentCildren;
	
	private Vector<DNetEventListener> verRefDNetEventListener;
		
	private boolean bHistogramIsValid = false;
	
	private boolean b_Gui_UpdateEnabled = false;
	
	protected IGeneralManager refGeneralManager;
	
	private IGeneralManagerSingelton refGeneralManagerSingelton;
	private IVirtualArrayManager refSelectionManager;
	private ISetManager refSetManager;
	private IStorageManager refStorageManager;
	
	private JButton j_button_update;
	private JButton j_button_new;
	
	protected ISet refCurrentSet = null;
	

//	/**
//	 * @param arg0
//	 */
//	public DSwingSelectionCanvas(LayoutManager arg0) {
//		super(arg0);
//		initDPanel();
//	}



	/**
	 * 
	 */
	public DSwingStorageCanvas( IGeneralManager refGeneralManager ) {
		super();
		
		this.refGeneralManager= refGeneralManager;
		
		initDPanel();
	}

	/**
	 * Get the singelton
	 * 
	 * @see cerberus.data.IUniqueManagedObject#getManager()
	 */
	public IGeneralManager getManager() {
		return refGeneralManager;
	}
	
	
	private void initDPanel() {
		vecRefComponentCildren = new Vector<DNetEventComponentInterface>();
		
		verRefDNetEventListener = new  Vector<DNetEventListener>();
		
		vecRefCommandListener = new Vector<ICommandListener>(); 
		
		vecJListRows = new Vector<DSwingStorageTabbedPane> (8);
		
		
//		GridLayout rowGridLayout = new GridLayout( );
//		rowGridLayout.setRows( sHeaderListLabel.length );
		j_button_new 	= new JButton("new");
		j_button_update = new JButton("Update");
		
		j_button_update.setEnabled( b_Gui_UpdateEnabled );
		
		DSwingSelectionUpdateButtonHandler updateButtonHandler = 
			new DSwingSelectionUpdateButtonHandler();
		
		DSwingSelectionNewSelectionButtonHandler newSelectionButtonHandler =
			new DSwingSelectionNewSelectionButtonHandler();
		
		j_button_update.addActionListener( updateButtonHandler );
		j_button_new.addActionListener( newSelectionButtonHandler );
			
		j_mainBox = Box.createVerticalBox();
		
		//Box headerBox = Box.createHorizontalBox();
		
		JPanel header_panel = new JPanel( new FlowLayout(FlowLayout.CENTER) );
		
		header_panel.add( j_button_update );
		for ( int i=0; i< sHeaderListLabel.length ; i++) {
			header_panel.add( new JLabel( sHeaderListLabel[i] ));
		}
		header_panel.add( j_button_new );
		
		j_mainBox.add( header_panel );
		
		
		
		//this.add( new JLabel("SET - VIRTUAL_ARRAY - STORAGE") );
		
		this.add( new JScrollPane( j_mainBox ) );
		this.setAutoscrolls( true );
		
		try {
			refGeneralManagerSingelton = 
				(IGeneralManagerSingelton) this.refGeneralManager;
			
			refSelectionManager = (IVirtualArrayManager)
				refGeneralManagerSingelton.getManagerByBaseType(
						ManagerObjectType.VIRTUAL_ARRAY );
			refSetManager = (ISetManager)
				refGeneralManagerSingelton.getManagerByBaseType(
						ManagerObjectType.SET );
			refStorageManager = (IStorageManager)
				refGeneralManagerSingelton.getManagerByBaseType(
						ManagerObjectType.STORAGE );
		}
		catch (NullPointerException npe) {
			
			refSelectionManager = null;
			refSetManager = null;
			refStorageManager = null;
			
			throw new CerberusRuntimeException("can not get all required references to managers. " +
					npe.toString() );
		}
		
	}
	
	
	/* (non-Javadoc)
	 * @see cerberus.net.dwt.DNetEventComponentInterface#addNetActionListener(cerberus.net.dwt.DNetEventListener)
	 */
	public void addNetActionListener(DNetEventListener addListener) {
		
		//TODO remove call in release version
		if ( verRefDNetEventListener.contains(addListener)) {
			assert false: "addNetActionListener() try to add existing listener";
			return;
		}
		
		verRefDNetEventListener.add( addListener );
	}

	/* (non-Javadoc)
	 * @see cerberus.net.dwt.DNetEventComponentInterface#handleNetEvent(cerberus.net.dwt.DNetEvent)
	 */
	public void handleNetEvent(DNetEvent event) {
		// TODO Auto-generated method stub
		
		/**
		 * promote event to children...
		 */
		Iterator<DNetEventListener> iter = verRefDNetEventListener.iterator();
		
		while ( iter.hasNext() ) {
			iter.next().netActionPerformed( event );
		}
	}

	/* (non-Javadoc)
	 * @see cerberus.net.dwt.DNetEventComponentInterface#addCommandListener(cerberus.command.ICommandListener)
	 */
	synchronized public boolean addCommandListener(ICommandListener setCommandListener) {
		
		if ( vecRefCommandListener.contains(setCommandListener)) {
			return false;
		}
		
		vecRefCommandListener.add( setCommandListener );
		return true;
	}

	/* (non-Javadoc)
	 * @see cerberus.net.dwt.DNetEventComponentInterface#containsNetEvent(cerberus.net.dwt.DNetEvent)
	 */
	public boolean containsNetEvent(DNetEvent event) {
		return this.getBounds().contains( 
				event.getSuperMouseEvent().getX(),
				event.getSuperMouseEvent().getY() );
	}

	/* (non-Javadoc)
	 * @see cerberus.net.dwt.DNetEventComponentInterface#getNetEventComponent(cerberus.net.dwt.DNetEvent)
	 */
	public DNetEventComponentInterface getNetEventComponent(DNetEvent event) {
		if ( this.containsNetEvent( event ) ) {
			
			Iterator<DNetEventComponentInterface> iter = vecRefComponentCildren.iterator();
			
			for (int i=0; iter.hasNext(); i++ ) {
				DNetEventComponentInterface child = iter.next();
				if ( child.containsNetEvent( event ) ) {
					return child.getNetEventComponent( event );
				}
				
			} // end for
			return this;
			
		} // end if
		return null;
	}

	/* (non-Javadoc)
	 * @see cerberus.net.dwt.DNetEventMementoXML#getDNetEventId()
	 */
	public int getId() {
		return iDNetEventComponentId;
	}

	/* (non-Javadoc)
	 * @see cerberus.net.dwt.DNetEventMementoXML#setDNetEventId(java.lang.Object, int)
	 */
	public void setId(int iSetDNetEventId) {
		
		//refParentCreator = (IDistComponentManager) creator;
		//FIXME check...	
		
		iDNetEventComponentId = iSetDNetEventId;
	}
	
	public final void setParentCreator( final IDistComponentManager creator) {
		refParentCreator = creator; 
	}
	
	public final void setParentComponent( final DNetEventComponentInterface parentComponent) {
		setParentComponent = parentComponent; 
	}


	/*
	 *  (non-Javadoc)
	 * @see cerberus.data.xml.IMementoNetEventXML#setMementoXML_usingHandler(cerberus.net.dwt.swing.parser.DParseSaxHandler)
	 */
	public synchronized boolean setMementoXML_usingHandler( final ISaxParserHandler refSaxHandler ) {
		
		try {
			/**
			 * TRy to cast refSaxHandler ...
			 */
			final DSwingHistogramCanvasHandler refHistogramSaxHandler = 
				(DSwingHistogramCanvasHandler) refSaxHandler;
			
			/**
			 * Test if GUI component does already exist...
			 */			
			if ( iDNetEventComponentId != refHistogramSaxHandler.getXML_dNetEvent_Id() ) {
				getManager().unregisterItem( iDNetEventComponentId, 
						ManagerObjectType.VIEW_HISTOGRAM2D );
				
				setId( refHistogramSaxHandler.getXML_dNetEvent_Id() );
			}
			
			getManager().registerItem( this, 
					iDNetEventComponentId, 
					ManagerObjectType.VIEW_HISTOGRAM2D );
			
			this.setVisible( refHistogramSaxHandler.getXML_state_visible() );
			this.setEnabled( refHistogramSaxHandler.getXML_state_enabled() );
			this.setToolTipText( refHistogramSaxHandler.getXML_state_tooltip() );
			this.setName( refHistogramSaxHandler.getXML_state_label() );
			this.setBounds( refHistogramSaxHandler.getXML_position_x(),
					refHistogramSaxHandler.getXML_position_y(),
					refHistogramSaxHandler.getXML_position_width(),
					refHistogramSaxHandler.getXML_position_height() );
			
			this.setBorder( new LineBorder( Color.RED ) );
			
			
			
			/**
			 * Register Buttons and subcomponents...
			 */
//			Iterator<Integer> iter = refHistogramSaxHandler.getXML_Iterator_NetEventCildrenComponentId();
//			
//			while ( iter.hasNext() ) {
//				Integer itemId = iter.next();
//				DNetEventComponentInterface itemRef = refParentCreator.getItemSet( itemId );
//				
//				if ( itemRef == null ) {
//					throw new CerberusRuntimeException("DPanel.setMementoXML_usingHandler() ERROR during iterator due to not existing itemID= [" +
//							itemId + "]");
//				}
//				this.add( (JComponent) itemRef );
//			}
			
			/**
			 * memento is applied now!
			 */
			return true;
						
		}
		catch (ClassCastException ce) {
			System.out.println("ERROR: DPanel.setMementoXML_usingHandler() wrong cast! " + ce.toString() );
			return false;
		}
		catch (NullPointerException ne) {
			System.out.println("ERROR: DPanel.setMementoXML_usingHandler() " + ne.toString() );
			return false;
		}
		
	}
	
	/**
	 * formating XML output.
	 * Talking locla tab offset into account. Thus  getTab(0) also is an important call.
	 * 
	 * @param iCountTabs number of tabs to be set starting with 0 tabs
	 * @return number of tabs created
	 */
	static private String getTab( final int iCountTabs ) {
		final String sTab ="  ";
		String tabResult = "";
		
		for ( int i=0; i<iCountTabs+iTabOffsetXML ; i++) {
			tabResult += sTab;
		}
		return tabResult;
	}
	
	/* (non-Javadoc)
	 * @see cerberus.net.dwt.DNetEventMementoXML#createMementoXML()
	 */
	public String createMementoXMLperObject() {
		/**
		 * XML Header
		 */
		String XML_MementoString = getTab(0) + "<DNetEventComponent dNetEvent_Id=\"" +
			this.iDNetEventComponentId + "\" label=\"DPanel Swing\">\n";
		XML_MementoString += getTab(1) + "<DNetEvent_type type=\"DPanel\"/>\n";
		XML_MementoString += getTab(1) + "<DNetEvent_details>\n";
		
		/**
		 * position of component
		 */
		final Rectangle rec = this.getBounds();
		XML_MementoString += getTab(2) + "<position x=\"" + rec.x + 
			"\" y=\"" + rec.y +
			"\" width=\"" + rec.width + 
			"\" height=\"" + rec.height + "\" />\n";
		
		/**
		 * State of component
		 */
		XML_MementoString += getTab(2) + "<state enabled=\"" + this.isEnabled() +
			"\" visible=\"" + this.isVisible() + 
			"\" label=\"" + this.getName() + 
			"\" tooltip=\"" + this.getToolTipText() + "\" />\n";	
		
		/**
		 * Layout manager
		 */
		XML_MementoString += getTab(2) + "<PanelLayout style=\"" +
			this.getLayout().getClass() + "\" />\n";
		
		/**
		 * XML footer
		 */
		XML_MementoString += getTab(1) + "</DNetEvent_details>\n<";	
		
		/**
		 * cildren...
		 */
		XML_MementoString += getTab(1) + "\n<SubComponents>\n";
		
		Iterator<DNetEventComponentInterface> iter = vecRefComponentCildren.iterator();
		
		for (int i=0; iter.hasNext(); i++ ) {
			DNetEventComponentInterface child = iter.next();
			XML_MementoString += getTab(2) + "<item  dNetEvent_Id=\"" +
				child.getId() + "\" >\n";
			XML_MementoString += getTab(3) + "<item_details></item_details>\n";
			XML_MementoString += getTab(2) + "</item>\n";
		}			
		
		XML_MementoString += getTab(1) + "</SubComponents>\n";	
		
		/**
		 * Link to NetEventListener ...
		 */
		XML_MementoString += getTab(1) + "<SubNetEventListener>\n";	
		
		Iterator<DNetEventListener> iterNetEvent = verRefDNetEventListener.iterator();
		
		while ( iterNetEvent.hasNext() ) {
			XML_MementoString += getTab(2) + "<NetListener  Id=\"" +			
				iterNetEvent.next().getId() + "\"></NetListener>\n";
		}
		XML_MementoString += getTab(1) + "</SubNetEventListener>\n";
		
		/**
		 * Link to ICommandListener ...
		 */
		XML_MementoString += getTab(1) + "<SubCommandListener>\n";	
		
//		Iterator<ICommandListener> iterCommand = vecRefCommandListener.iterator();
//		
//		while ( iterCommand.hasNext() ) {
//			XML_MementoString += getTab(2) + "<CmdListener  Id=\"" +			
//			iterCommand.next().getDNetEventId() + "\"></CmdListener>\n";
//		}
		XML_MementoString += getTab(1) + "</SubCommandListener>\n";
		
		
		XML_MementoString += getTab(0) + "</DNetEventComponent>\n\n";
		
		return XML_MementoString;		
	}

	/* (non-Javadoc)
	 * @see cerberus.net.dwt.DNetEventMementoXML#createMementoXML()
	 */
	public String createMementoXML() {
		String XML_MementoString = createMementoXMLperObject();
		
		Iterator<DNetEventComponentInterface> iter = vecRefComponentCildren.iterator();
				
		for (;iter.hasNext();) {
			XML_MementoString +=
				((DNetEventComponentInterface)iter.next()).createMementoXML();
		}
		
		return XML_MementoString;
	}
	
	/*
	 *  (non-Javadoc)
	 * @see cerberus.data.xml.IMementoNetEventXML#callbackForParser(java.lang.String)
	 */
	public void callbackForParser(  final ManagerObjectType type,
			final String tag_causes_callback,
			final String details,
			final ISaxParserHandler refSaxHandler ) {
		
		//FIXME test type...
		
		System.out.println(" DPanel.callbackForParser() ");
	
		Graphics g = ((JComponent) refParentCreator).getGraphics();
		this.paint( g );
		
		g.setColor( Color.RED );
		g.fillRect( this.getX(), this.getY(), this.getWidth(), this.getWidth() );
	}
	
	/**
	 * Get the type of this object.
	 * 
	 * @return type of this object
	 */
	public ManagerObjectType getBaseType() {
		return ManagerObjectType.VIEW;
	}

	public void paintDComponent( Graphics g ) {
		
		super.paintComponent(g);
		
//		if ( ! bHistogramIsValid ) {
//			
//		
//		}
//		
//		//final int iCounterRows = refHistogramCreator.getRowWidth();
//		
//		final int[] iCounterPerRow = refHistogramCreator.getCounterPerRow();
//		
//		boolean bAlternateColor = true;
//		
//		final int iHistogramCell_height_Y = 1;
//		final int iHistogramCell_width_X = 3;
//		
//		final  int iHistogramCell_offsetX = 10;
//		final  int iHistogramCell_offsetY = 10
//			+ refHistogramCreator.getMaxCountPerRow()*iHistogramCell_height_Y;
//		
//		final  int iHistogramCell_incX = 5;			
//		
//		int iHistogramCell_PosX = iHistogramCell_offsetX;
//		int iHistogramCell_PosY = iHistogramCell_offsetY;
//		
//		for ( int i=0; i<iCounterPerRow.length; i++ ) {
//			
//			if ( bAlternateColor ) {
//				g.setColor( Color.GREEN );
//				bAlternateColor = false;
//			}
//			else {
//				g.setColor( Color.YELLOW );
//				 bAlternateColor = true;
//			}
//			
//			int iX1 = iHistogramCell_PosX;
//			int iX2 = iHistogramCell_width_X;			
//			int iY2= iHistogramCell_height_Y * (iCounterPerRow[i]);
//			int iY1= iHistogramCell_PosY - iY2;
//			
//			
//			//iHistogramCell_PosY += iHistogramCell_incY;
//			
//			g.fillRect( iX1,iY1,iX2,iY2 );
//			
//			g.setColor( Color.BLACK );
//			g.drawRect( iX1,iY1,iX2,iY2 );
//			
//		
//			
//			iHistogramCell_PosX += iHistogramCell_incX;
//			
//		} // end for...
//		
//		g.drawLine( iHistogramCell_offsetX,
//				iHistogramCell_offsetY,
//				iHistogramCell_offsetX + 
//					((int) (iCounterPerRow.length+1)*iHistogramCell_incX ) + 
//					iHistogramCell_width_X,
//				iHistogramCell_offsetY );
		
	}
	
	public void notifySelectionHasChangedInGui() {
		if ( ! b_Gui_UpdateEnabled ){
			b_Gui_UpdateEnabled = true;
			j_button_update.setEnabled( true );
		}
	}
	
	public void updateAllSelectionsFromGui() {
		
		LinkedList<IStorage> refVecCurrentSelections = 
			refStorageManager.getAllStorageItemsVector();
		
		Iterator<IStorage> iterSel = refVecCurrentSelections.iterator();
		
		Iterator<DSwingStorageTabbedPane> iterRow = vecJListRows.iterator();
		
		for ( int iIndex=0; iterRow.hasNext(); iIndex++ ) {
			DSwingStorageTabbedPane buffer = iterRow.next();
			if (( buffer != null )&&( buffer.hasSelectionChanged() )) 
			{
				if (iterSel.hasNext() ) {
					IStorage bufferSel = iterSel.next();
					buffer.updateStorageFromGui( bufferSel );
				}
				else {
					/*
					 * Selections are done, but Gui-elements still exist.
					 */
					throw new CerberusRuntimeException("updateAllSelectionsFromGui() failed becaus selection does not exist any more.");
				}
			}
		}
	}
	
	/*
	 *  (non-Javadoc)
	 * @see cerberus.data.collection.ViewCanvas#updateState()
	 */
	public void updateState() {
		
		LinkedList<IStorage> refVecCurrentSelections = 
			refStorageManager.getAllStorageItemsVector();
		
		final int iSizeVectorSelections = refVecCurrentSelections.size();
		
		if ( vecJListRows.size() < iSizeVectorSelections ) {
			vecJListRows.ensureCapacity( iSizeVectorSelections );
		}
		
		Iterator<IStorage> iterFromManager = refVecCurrentSelections.iterator();
		Iterator<DSwingStorageTabbedPane> iterFromGui = vecJListRows.iterator();
		
		// index for both vectors...
		int iIndex;
		int iIndexGuiRow = 0;
		
		for ( iIndex= 0 ; iterFromManager.hasNext(); iIndex++ ) {
			IStorage bufferSelection = iterFromManager.next();
			DSwingStorageTabbedPane buffer = null;
			
			if ( iIndexGuiRow < iActiveJListItems ) {
				if ( iterFromGui.hasNext() ) {
					buffer = iterFromGui.next();
				}
			}
//			else {
//				if ( iIndexGuiRow <= refVecCurrentSelections.size() ) {
//					buffer = iterFromGui.next();
//					iActiveJListItems = iIndexGuiRow;
//				}
//			}
			
			assert bufferSelection != null :
				"null-pointer in IVirtualArray list from ISelectionManager.";
			
			if ( buffer == null ) {
				DSwingStorageTabbedPane newListItem = 
				 new DSwingStorageTabbedPane( this );
				
				//j_mainBox.add( newListItem.getJRow(), iIndex+1 );
				
				j_mainBox.add( newListItem.getJRow() );
				
				newListItem.updateFromStorageToGui( bufferSelection );
				
				vecJListRows.add( iIndex, newListItem );
				
				//iActiveJListItems
				iActiveJListItems = iIndex + 1;
				
			}
			else {
				
				buffer.updateFromStorageToGui( bufferSelection );
				//FIXME optimize by chaching states of selections!
				
//				if ( buffer.getCurrentSelection() == bufferSelection ) {
//					//TODO optimize by chaching states of selections!
//					buffer.updateFromSelection( bufferSelection );
//				}
//				else {
//					buffer.updateFromSelection( bufferSelection );
//				}
			}
			
			iIndexGuiRow++;
		}
		
		/*
		 * If previouse lsit was longer...
		 */
		if ( iIndex < iActiveJListItems ) {
			while ( iIndex < iActiveJListItems ) {
				vecJListRows.get( iIndex ).setVisibel( false );
				iIndex++;
			}
		}
		
		//FIXME test this mehtode !!
		
	}
	
	private class DSwingSelectionUpdateButtonHandler implements ActionListener
	{
		
		public void actionPerformed( ActionEvent event ) {
			
			if ( event.getSource() == j_button_update ) {
				
				updateAllSelectionsFromGui();
				
				b_Gui_UpdateEnabled = false;
				j_button_update.setEnabled( false );
			}
		}
		
	} // end class DSwingSelectionRowTextFieldHandler
	
	private class DSwingSelectionNewSelectionButtonHandler implements ActionListener
	{
		
		public void actionPerformed( ActionEvent event ) {
			
			if ( event.getSource() == j_button_new ) {
				
				updateAllSelectionsFromGui();
				
				b_Gui_UpdateEnabled = false;
				j_button_update.setEnabled( false );
				
				IStorage newStorage =
					(IStorage) refGeneralManagerSingelton.createNewItem( 
							ManagerObjectType.STORAGE_FLAT, "" );
				
				refGeneralManagerSingelton.registerItem( newStorage, 
						newStorage.getId(), 
						ManagerObjectType.STORAGE_FLAT );
				
				updateState();
				
				System.out.println("NEW");
				
				DSwingStorageCanvas.this.updateUI();
				
				
			}
		}
		
	} // end class DSwingSelectionNewSelectionButtonHandler
	
	
}
