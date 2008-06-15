package org.caleydo.core.view.opengl.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.EnumMap;

import javax.media.opengl.GL;
import javax.media.opengl.GLException;

import com.sun.opengl.util.texture.Texture;
import com.sun.opengl.util.texture.TextureIO;

/**
 * TODO move this to manager to use it as a singleton
 * 
 * @author Alexander Lex
 *
 */

public class GLIconTextureManager 
{
	
	EnumMap<EIconTextures, Texture> mapIconTextures;
	
	/**
	 * Constructor.
	 * 
	 * @param gl
	 */
	public GLIconTextureManager(final GL gl)
	{
		mapIconTextures = new EnumMap<EIconTextures, Texture>(EIconTextures.class);
		for(EIconTextures eIconTextures : EIconTextures.values())
		{
			try
			{
				Texture tmpTexture;
				String sFileName = eIconTextures.getFileName();			
				
			    if (this.getClass().getClassLoader().getResourceAsStream(sFileName) != null)
			    {
			    	tmpTexture = TextureIO.newTexture(TextureIO.newTextureData(
			    			this.getClass().getClassLoader().getResourceAsStream(sFileName), true, "PNG"));
			    }
			    else
			    {
			    	tmpTexture = TextureIO.newTexture(TextureIO.newTextureData(
							new File(eIconTextures.getFileName()), true, "PNG"));
			    }
				
				mapIconTextures.put(eIconTextures, tmpTexture);
				
			} catch (GLException e)
			{
				e.printStackTrace();
			} catch (IllegalArgumentException e)
			{
				e.printStackTrace();
			} catch (IOException e)
			{
				e.printStackTrace();
			}	
		}
	}
	
	public Texture getIconTexture(final EIconTextures eIconTextures)
	{
		return mapIconTextures.get(eIconTextures);
	}
}
