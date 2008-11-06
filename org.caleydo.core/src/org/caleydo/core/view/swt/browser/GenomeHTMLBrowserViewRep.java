package org.caleydo.core.view.swt.browser;

import java.util.ArrayList;
import org.caleydo.core.data.IUniqueObject;
import org.caleydo.core.data.mapping.EMappingType;
import org.caleydo.core.data.selection.ESelectionType;
import org.caleydo.core.data.selection.ISelectionDelta;
import org.caleydo.core.data.selection.SelectionItem;
import org.caleydo.core.manager.event.mediator.EMediatorType;
import org.caleydo.core.manager.event.mediator.IMediatorReceiver;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.List;


/**
 * Special HTML browser for genomics use case.
 * 
 * @author Marc Streit
 */
public class GenomeHTMLBrowserViewRep
	extends HTMLBrowserViewRep
	implements IMediatorReceiver
{
	private URLGenerator urlGenerator;
	
	private List list;
	
	private Button buttonKEGG;
	private Button buttonBioCarta;
	private Button buttonPubMed;
	private Button buttonEntrez;
	
	private ArrayList<Integer> iAlDavidID;
	
	private EBrowserQueryType eBrowserQueryType = EBrowserQueryType.EntrezGene;
	
	/**
	 * Constructor.
	 */
	public GenomeHTMLBrowserViewRep(int iParentContainerID, String sLabel)
	{
		super(iParentContainerID, sLabel);
		
		urlGenerator = new URLGenerator();
		
		generalManager.getEventPublisher().registerReceiverToMediatorGroup(
				EMediatorType.DATA_MEDIATOR, this);
		
		iAlDavidID = new ArrayList<Integer>();
	}
	
	@Override
	protected void initViewSwtComposite(Composite parent)
	{
		Composite composite = new Composite(parent, SWT.NONE);
		
		GridLayout layout = new GridLayout(2, false);
		composite.setLayout(layout);
		
		Composite leftColumnComposite = new Composite(composite, SWT.NONE);
		GridLayout leftColumnLayout = new GridLayout(1, false);
		leftColumnComposite.setLayout(leftColumnLayout);
		leftColumnComposite.setLayoutData(new GridData(GridData.FILL_VERTICAL));

		Group groupQueryType = new Group(leftColumnComposite, SWT.SHADOW_ETCHED_IN);
		groupQueryType.setText("Database type");
		buttonKEGG = new Button(groupQueryType, SWT.RADIO);
	    buttonKEGG.setText(EBrowserQueryType.KEGG.toString());
	    buttonBioCarta = new Button(groupQueryType, SWT.RADIO);
	    buttonBioCarta.setText(EBrowserQueryType.BioCarta.toString());
	    buttonEntrez = new Button(groupQueryType, SWT.RADIO);
	    buttonEntrez.setText(EBrowserQueryType.EntrezGene.toString());
	    buttonEntrez.setSelection(true);
	    buttonPubMed = new Button(groupQueryType, SWT.RADIO);
	    buttonPubMed.setText(EBrowserQueryType.PubMed.toString());
	    
	    SelectionListener queryTypeListener = new SelectionAdapter() {
	    	
	    	@Override
	    	public void widgetSelected(SelectionEvent e)
	    	{
	    		eBrowserQueryType = 
	    			EBrowserQueryType.valueOf(((Button)e.widget).getText());
	    		
	    		if (!iAlDavidID.isEmpty())
	    		{
		    		String sURL = urlGenerator.createURL(eBrowserQueryType, 
		    				iAlDavidID.get(list.getSelectionIndex()));    
		    		
		    		browser.setUrl(sURL);
		    		browser.update();
		    		textURL.setText(sURL);
	    		}
	    	}
	    };
	    
	    buttonBioCarta.addSelectionListener(queryTypeListener);
	    buttonKEGG.addSelectionListener(queryTypeListener);
	    buttonPubMed.addSelectionListener(queryTypeListener);
	    buttonEntrez.addSelectionListener(queryTypeListener);
	    
	    GridData data = new GridData();
	    data.widthHint = 200;
		groupQueryType.setLayoutData(data);
		groupQueryType.setLayout(new GridLayout(1, false));
	    
	    list = new List(leftColumnComposite, SWT.SINGLE | SWT.BORDER);
	    data = new GridData(GridData.FILL_VERTICAL);
	    data.grabExcessVerticalSpace = true;
		data.widthHint = 200;
		list.setLayoutData(data);
		
		list.addSelectionListener(new SelectionAdapter() {
		   
	    	@Override
	    	public void widgetSelected(SelectionEvent e)
	    	{
	    		String sURL = urlGenerator.createURL(eBrowserQueryType, 
	    				iAlDavidID.get(list.getSelectionIndex()));    
	    		
	    		browser.setUrl(sURL);
	    		browser.update();
	    		textURL.setText(sURL);	
	    	}
	    });
		
		super.initViewSwtComposite(composite);
	}

	@Override
	public void handleUpdate(IUniqueObject eventTrigger, final ISelectionDelta selectionDelta)
	{		
		parent.getDisplay().asyncExec(new Runnable()
		{
			public void run()
			{		
				if (!checkInternetConnection())
					return;
				
				int iItemsToLoad = 0;
				SelectionItem selectionItem;
				for (int iItemIndex = 0; iItemIndex < selectionDelta.getSelectionData().size(); iItemIndex++)
				{
					selectionItem = selectionDelta.getSelectionData().get(iItemIndex);
					
					if (selectionItem.getSelectionType() == ESelectionType.MOUSE_OVER 
							|| selectionItem.getSelectionType() == ESelectionType.SELECTION)
					{
						if (iItemsToLoad == 0)
						{
				    		String sURL = urlGenerator.createURL(eBrowserQueryType, 
									selectionDelta.getSelectionData().get(iItemIndex).getSelectionID());  
				    		
				    		browser.setUrl(sURL);
				    		browser.update();
				    		textURL.setText(sURL);
				    		
				    		iAlDavidID.clear();
							list.removeAll();
						}
						
						String sRefSeqID = generalManager.getIDMappingManager()
							.getID(EMappingType.DAVID_2_REFSEQ_MRNA, selectionItem.getSelectionID());
						
						String sGeneShortName = generalManager.getIDMappingManager()
							.getID(EMappingType.DAVID_2_GENE_SYMBOL, selectionItem.getSelectionID());
						
						list.add(sRefSeqID + " - " + sGeneShortName);
						iAlDavidID.add(selectionItem.getSelectionID());
						
						iItemsToLoad++;
					}
				}
				
				list.redraw();
				list.setSelection(0);
			}
		});
	}
}
