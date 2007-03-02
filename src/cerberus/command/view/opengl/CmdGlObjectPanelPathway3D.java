/**
 * 
 */
package cerberus.command.view.opengl;

import cerberus.command.CommandQueueSaxType;
import cerberus.command.ICommand;
import cerberus.command.base.ACmdCreate_GlCanvasUser;
import cerberus.manager.ICommandManager;
import cerberus.manager.IGeneralManager;
import cerberus.util.exception.CerberusRuntimeException;
import cerberus.util.system.StringConversionTool;
import cerberus.view.gui.opengl.canvas.pathway.GLCanvasPanelPathway3D;
import cerberus.xml.parser.parameter.IParameterHandler;

/**
 * @author Michael Kalkusch
 * @author Marc Streit
 *
 */
public class CmdGlObjectPanelPathway3D 
extends ACmdCreate_GlCanvasUser
implements ICommand {
	
	protected int iPathwaySetId = 0;
	
	/**
	 * Constructor.
	 * 
	 */
	public CmdGlObjectPanelPathway3D(
			final IGeneralManager refGeneralManager,
			final ICommandManager refCommandManager,
			final CommandQueueSaxType refCommandQueueSaxType)
	{
		super(refGeneralManager, 
				refCommandManager,
				refCommandQueueSaxType);
				
		localManagerObjectType = CommandQueueSaxType.CREATE_GL_PANEL_PATHWAY_3D;
	}

	public void setParameterHandler( final IParameterHandler refParameterHandler ) {
	
		super.setParameterHandler(refParameterHandler);

		iPathwaySetId = StringConversionTool.convertStringToInt(sDetail, -1);
	}

	@Override
	public void doCommandPart() throws CerberusRuntimeException {
		
		GLCanvasPanelPathway3D canvas = 
			(GLCanvasPanelPathway3D) openGLCanvasUser;		
		
		canvas.setOriginRotation(vec3fOrigin, vec4fRotation);
		//canvas.setPathwaySet(iPathwaySetId);
		//canvas.setTargetPathwayId(iTargetPathwayId);
	}

	@Override
	public void undoCommandPart() throws CerberusRuntimeException {

	}
}
