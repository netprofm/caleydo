package org.caleydo.core.application.helper.cacher.kegg;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;
import org.caleydo.core.application.helper.PathwayListGenerator;
import org.caleydo.core.command.system.CmdFetchPathwayData;
import org.caleydo.core.manager.IGeneralManager;
import org.caleydo.core.manager.general.GeneralManager;
import org.caleydo.core.util.exception.CaleydoRuntimeException;
import org.caleydo.core.util.exception.CaleydoRuntimeExceptionType;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.ProgressBar;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import de.phleisch.app.itsucks.constants.ApplicationConstants;
import de.phleisch.app.itsucks.core.Dispatcher;
import de.phleisch.app.itsucks.event.Event;
import de.phleisch.app.itsucks.event.EventObserver;
import de.phleisch.app.itsucks.filter.download.impl.DownloadJobFilter;
import de.phleisch.app.itsucks.filter.download.impl.RegExpJobFilter;
import de.phleisch.app.itsucks.filter.download.impl.RegExpJobFilter.RegExpFilterAction;
import de.phleisch.app.itsucks.filter.download.impl.RegExpJobFilter.RegExpFilterRule;
import de.phleisch.app.itsucks.job.Job;
import de.phleisch.app.itsucks.job.download.impl.DownloadJobFactory;
import de.phleisch.app.itsucks.job.download.impl.UrlDownloadJob;
import de.phleisch.app.itsucks.job.event.JobChangedEvent;

/**
 * Fetch tool for KEGG XML files.
 * 
 * @author Marc Streit
 */
public class KeggPathwayCacher
	extends Thread
{
	private static final int EXPECTED_DOWNLOADS = 213;

	private IGeneralManager generalManager;

	/**
	 * Needed for async access to set progress bar state
	 */
	private Display display;

	private ProgressBar progressBar;

	private CmdFetchPathwayData triggeringCommand;

	int iDownloadCount = 0;

	/**
	 * Constructor.
	 */
	public KeggPathwayCacher(final Display display, final ProgressBar progressBar,
			final CmdFetchPathwayData triggeringCommand)
	{
		this.generalManager = GeneralManager.get();
		this.display = display;
		this.progressBar = progressBar;
		this.triggeringCommand = triggeringCommand;
	}

	@Override
	public void run()
	{
		super.run();

		// load spring application context
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
				ApplicationConstants.CORE_SPRING_CONFIG_FILE);

		// load dispatcher from spring
		final Dispatcher dispatcher = (Dispatcher) context.getBean("Dispatcher");

		// configure an download job filter
		DownloadJobFilter downloadFilter = new DownloadJobFilter();
		downloadFilter.setAllowedHostNames(new String[] { "www.genome.jp.*" });
		downloadFilter.setMaxRecursionDepth(2);
		downloadFilter.setSaveToDisk(new String[] { ".*xml" });

		// add the filter to the dispatcher
		dispatcher.addJobFilter(downloadFilter);

		RegExpJobFilter regExpFilter = new RegExpJobFilter();
		RegExpFilterRule regExpFilterRule = new RegExpJobFilter.RegExpFilterRule(
				".*KGMLViewer.*|.*PathwayViewer.*|.*xmlview.*|.*dbget.*|.*html"
						+ "|.*atlas|.*css|.*menu.*|.*feedback.*|.*docs.|.*menu.*");

		RegExpFilterAction regExpFilterAction = new RegExpJobFilter.RegExpFilterAction();
		regExpFilterAction.setAccept(false);

		regExpFilterRule.setMatchAction(regExpFilterAction);

		regExpFilter.addFilterRule(regExpFilterRule);

		dispatcher.addJobFilter(regExpFilter);

		// create an job factory
		DownloadJobFactory jobFactory = (DownloadJobFactory) context.getBean("JobFactory");

		String sOutputFileName = System.getProperty("user.home")
				+ System.getProperty("file.separator") + "/.caleydo";

		// create an initial job
		UrlDownloadJob job = jobFactory.createDownloadJob();

		try
		{
			job.setUrl(new URL("http://www.genome.jp/kegg/xml/hsa/index.html"));
		}
		catch (MalformedURLException e)
		{
			e.printStackTrace();
		}
		// "http://www.genome.jp/kegg/pathway/hsa/hsa00380.gif
		job.setSavePath(new File(sOutputFileName));
		job.setIgnoreFilter(true);
		dispatcher.addJob(job);

		dispatcher.getEventManager().registerObserver(new EventObserver()
		{
			/*
			 * (non-Javadoc)
			 * @see
			 * de.phleisch.app.itsucks.event.EventObserver#processEvent(de.phleisch
			 * .app.itsucks.event.Event)
			 */
			@Override
			public void processEvent(Event arg0)
			{
				if (arg0 instanceof JobChangedEvent
						&& ((JobChangedEvent) arg0).getJob().getState() == Job.STATE_FINISHED)
				{
					iDownloadCount++;

					display.asyncExec(new Runnable()
					{
						public void run()
						{
							if (progressBar.isDisposed())
								return;
							progressBar
									.setSelection((iDownloadCount * 100 / EXPECTED_DOWNLOADS));

							// System.out.println("Download count: "
							// +iDownloadCount);
							// System.out.println("Percentage: "
							// +(int)(iDownloadCount * 100 /
							// EXPECTED_DOWNLOADS));
						}
					});
				}
			}
		});

		// start the dispatcher
		dispatcher.processJobs();

		if (triggeringCommand != null)
			triggeringCommand.setFinishedKeggCacher();
	}


}