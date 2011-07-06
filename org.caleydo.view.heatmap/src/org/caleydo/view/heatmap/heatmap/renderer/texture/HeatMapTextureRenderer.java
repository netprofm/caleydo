package org.caleydo.view.heatmap.heatmap.renderer.texture;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferFloat;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Date;

import javax.imageio.ImageIO;
import javax.media.opengl.GL2;
import javax.media.opengl.GLProfile;

import org.caleydo.core.data.collection.ISet;
import org.caleydo.core.data.collection.storage.EDataRepresentation;
import org.caleydo.core.data.virtualarray.ContentVirtualArray;
import org.caleydo.core.data.virtualarray.StorageVirtualArray;
import org.caleydo.core.manager.GeneralManager;
import org.caleydo.core.manager.picking.EPickingType;
import org.caleydo.core.manager.picking.PickingManager;
import org.caleydo.core.util.mapping.color.ColorMapper;
import org.caleydo.core.view.opengl.canvas.PixelGLConverter;
import org.caleydo.core.view.opengl.layout.Column;
import org.caleydo.core.view.opengl.layout.ElementLayout;
import org.caleydo.core.view.opengl.layout.LayoutRenderer;
import org.caleydo.core.view.opengl.layout.Row;
import org.caleydo.view.heatmap.HeatMapRenderStyle;
import org.caleydo.view.heatmap.uncertainty.GLUncertaintyHeatMap;

import com.jogamp.opengl.util.awt.ImageUtil;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureCoords;
import com.jogamp.opengl.util.texture.TextureData;
import com.jogamp.opengl.util.texture.TextureIO;
import com.jogamp.opengl.util.texture.awt.AWTTextureIO;

public class HeatMapTextureRenderer extends LayoutRenderer {

	private final static int MAX_SAMPLES_PER_TEXTURE = 2000;

	private int numberOfTextures = 0;

	private int numberOfElements = 0;

	private int samplesPerTexture = 0;

	/** array of textures for holding the data samples */
	private ArrayList<Texture> textures = new ArrayList<Texture>();

	private ArrayList<Integer> numberSamples = new ArrayList<Integer>();

	private GLUncertaintyHeatMap uncertaintyHeatMap;

	private PickingManager pickingManager = GeneralManager.get()
			.getViewGLCanvasManager().getPickingManager();

	private int groupIndex;
	private FloatBuffer[] floatBuffer;

	private int numberOfExpirments;
	private int myFBO = -1;

	private boolean takeScreenShot = false;

	private ArrayList<Float> visualUncertaintyArray;

	public Column heatmapLayout;

	private ContentVirtualArray contentVA;

	private StorageVirtualArray storageVA;

	private ISet set;

	public HeatMapTextureRenderer(GLUncertaintyHeatMap uncertaintyHeatMap,
			Column heatmapLayout) {

		this.uncertaintyHeatMap = uncertaintyHeatMap;
		this.heatmapLayout = heatmapLayout;

	}

	/*
	 * Init textures, build array of textures used for holding the whole samples
	 */
	public void init(GLUncertaintyHeatMap uncertaintyHeatMap, ISet set,
			ContentVirtualArray contentVA, StorageVirtualArray storageVA,
			int groupIndex) {

		this.contentVA = contentVA;
		this.storageVA = storageVA;
		this.set = set;

		this.uncertaintyHeatMap = uncertaintyHeatMap;
		ColorMapper colorMapper = uncertaintyHeatMap.getColorMapper();
		this.groupIndex = groupIndex;

		int textureHeight = numberOfElements = contentVA.size();
		int textureWidth = numberOfExpirments = storageVA.size();

		numberOfTextures = (int) Math.ceil((double) numberOfElements
				/ MAX_SAMPLES_PER_TEXTURE);

		if (numberOfTextures <= 1)
			samplesPerTexture = numberOfElements;
		else
			samplesPerTexture = MAX_SAMPLES_PER_TEXTURE;

		textures.clear();
		numberSamples.clear();

		Texture tempTexture;

		samplesPerTexture = (int) Math.ceil((double) textureHeight
				/ numberOfTextures);

		float fLookupValue = 0;

		floatBuffer = new FloatBuffer[numberOfTextures];

		for (int itextures = 0; itextures < numberOfTextures; itextures++) {

			if (itextures == numberOfTextures - 1) {
				numberSamples
						.add(textureHeight - samplesPerTexture * itextures);
				floatBuffer[itextures] = FloatBuffer
						.allocate((textureHeight - samplesPerTexture
								* itextures)
								* textureWidth * 4);
			} else {
				numberSamples.add(samplesPerTexture);
				floatBuffer[itextures] = FloatBuffer.allocate(samplesPerTexture
						* textureWidth * 4);
			}
		}

		int contentCount = 0;
		int textureCounter = 0;
		float opacity = 1;

		for (Integer contentIndex : contentVA) {
			contentCount++;
			for (Integer storageIndex : storageVA) {
				// if
				// (contentSelectionManager.checkStatus(SelectionType.DESELECTED,
				// iContentIndex)) {
				// fOpacity = 0.3f;
				// } else {
				// fOpacity = 1.0f;
				// }

				fLookupValue = set.get(storageIndex).getFloat(
						EDataRepresentation.NORMALIZED, contentIndex);

				float[] mappingColor = colorMapper.getColor(fLookupValue);

				float[] rgba = { mappingColor[0], mappingColor[1],
						mappingColor[2], opacity };

				floatBuffer[textureCounter].put(rgba);
			}
			if (contentCount >= numberSamples.get(textureCounter)) {
				floatBuffer[textureCounter].rewind();

				TextureData texData = new TextureData(GLProfile.getDefault(),
						GL2.GL_RGBA /* internalFormat */,
						textureWidth /* height */,
						numberSamples.get(textureCounter) /* width */,
						0 /* border */, GL2.GL_RGBA /* pixelFormat */,
						GL2.GL_FLOAT /* pixelType */, false /* mipmap */,
						false /* dataIsCompressed */,
						false /* mustFlipVertically */,
						floatBuffer[textureCounter], null);

				tempTexture = TextureIO.newTexture(0);
				tempTexture.updateImage(texData);

				textures.add(tempTexture);

				textureCounter++;
				contentCount = 0;
			}
		}
	}

	@Override
	public void render(GL2 gl) {

		float yOffset = 0.0f;

		// fHeight = viewFrustum.getHeight();
		// fWidth = renderStyle.getWidthLevel1();

		float elementHeight = y / numberOfElements;
		float step = 0;

		gl.glColor4f(1f, 1f, 0f, 1f);

		for (int i = 0; i < numberOfTextures; i++) {

			step = elementHeight * numberSamples.get(numberOfTextures - i - 1);
			renderTexture(gl, textures.get(numberOfTextures - i - 1), 0,
					yOffset, x, yOffset + step);
			yOffset += step;

			/*
			 * step = elementHeight * numberSamples.get(numberOfTextures - i -
			 * 1);
			 * 
			 * textures.get(numberOfTextures - i - 1).enable();
			 * textures.get(numberOfTextures - i - 1).bind();
			 * gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_WRAP_S,
			 * GL2.GL_CLAMP); gl.glTexParameteri(GL2.GL_TEXTURE_2D,
			 * GL2.GL_TEXTURE_WRAP_T, GL2.GL_CLAMP);
			 * gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MAG_FILTER,
			 * GL2.GL_NEAREST); gl.glTexParameteri(GL2.GL_TEXTURE_2D,
			 * GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_NEAREST); TextureCoords
			 * texCoords = textures.get(numberOfTextures - i - 1)
			 * .getImageTexCoords();
			 * 
			 * gl.glPushName(pickingManager.getPickingID(
			 * uncertaintyHeatMap.getID(), EPickingType.HEAT_MAP_CLUSTER_GROUP,
			 * groupIndex)); gl.glBegin(GL2.GL_QUADS);
			 * gl.glTexCoord2d(texCoords.left(), texCoords.top());
			 * gl.glVertex3f(0, yOffset, 0); gl.glTexCoord2d(texCoords.left(),
			 * texCoords.bottom()); gl.glVertex3f(0, yOffset + step, 0);
			 * gl.glTexCoord2d(texCoords.right(), texCoords.bottom());
			 * gl.glVertex3f(x, yOffset + step, 0);
			 * gl.glTexCoord2d(texCoords.right(), texCoords.top());
			 * gl.glVertex3f(x, yOffset, 0); gl.glEnd(); gl.glPopName();
			 * 
			 * yOffset += step; textures.get(numberOfTextures - i -
			 * 1).disable();
			 */
		}
	}

	private void renderTexture(GL2 gl, Texture texture, float x, float y,
			float width, float height) {

		texture.enable();
		texture.bind();
		gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_WRAP_S,
				GL2.GL_CLAMP);
		gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_WRAP_T,
				GL2.GL_CLAMP);
		gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MAG_FILTER,
				GL2.GL_NEAREST);
		gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MIN_FILTER,
				GL2.GL_NEAREST);
		TextureCoords texCoords = texture.getImageTexCoords();

		gl.glPushName(pickingManager.getPickingID(uncertaintyHeatMap.getID(),
				EPickingType.HEAT_MAP_CLUSTER_GROUP, groupIndex));
		gl.glBegin(GL2.GL_QUADS);
		gl.glTexCoord2d(texCoords.left(), texCoords.top());
		gl.glVertex3f(x, y, 0);
		gl.glTexCoord2d(texCoords.left(), texCoords.bottom());
		gl.glVertex3f(x, y + height, 0);
		gl.glTexCoord2d(texCoords.right(), texCoords.bottom());
		gl.glVertex3f(x + width, y + height, 0);
		gl.glTexCoord2d(texCoords.right(), texCoords.top());
		gl.glVertex3f(x + width, y, 0);
		gl.glEnd();
		gl.glPopName();

		texture.disable();

	}

	public ArrayList<Float> getVisualUncertainty() {
		return visualUncertaintyArray;
	}

	private int genFBO(GL2 gl) {
		int[] array = new int[1];
		IntBuffer ib = IntBuffer.wrap(array);
		gl.glGenFramebuffers(1, ib);
		return ib.get(0);
	}

	public void getScreenAreaShot(GL2 gl, int x, int y, int width, int height) {

		/*
		 * if (myFBO == -1) { myFBO = genFBO(gl);}
		 * gl.glBindFramebuffer(GL2.GL_DRAW_BUFFER, this.myFBO);
		 * gl.glReadBuffer(GL2.GL_FRONT); gl.glDrawBuffer(myFBO);
		 * 
		 * gl.glBlitFramebuffer(x, y, x + width - 1, y + height - 1, 0, 0, 600,
		 * 600, GL2.GL_COLOR_BUFFER_BIT, GL2.GL_LINEAR);
		 * //gl.glBlitFramebuffer(x, y, x+width, y+height, 0, 0,
		 * numberOfExpirments, numberOfElements, GL2.GL_COLOR_BUFFER_BIT, //
		 * GL2.GL_LINEAR);
		 */
		// gl.glReadBuffer(GL2.GL_AUX1);
		// gl.glDrawBuffer(GL2.GL_BACK);

		gl.glReadBuffer(GL2.GL_FRONT);
		ByteBuffer screenShotByteBuffer = null;
		BufferedImage screenShotImage = null;
		screenShotImage = new BufferedImage(width, height,
				BufferedImage.TYPE_4BYTE_ABGR);

		screenShotByteBuffer = ByteBuffer
				.wrap(((DataBufferByte) screenShotImage.getRaster()
						.getDataBuffer()).getData());

		gl.glReadPixels(x, y, width, height, GL2.GL_ABGR_EXT,
				GL2.GL_UNSIGNED_BYTE, screenShotByteBuffer);

		ImageUtil.flipImageVertically(screenShotImage);

		visualUncertaintyArray = new ArrayList<Float>();
		Date now = new Date();

		try {
			ImageIO.write(screenShotImage, "png", new File(
					"C:\\Documents and Settings\\Clemens\\bild" + now.getTime()
							+ ".png"));
		} catch (IOException e) { // TODO Auto-generated catch block
			e.printStackTrace();
		}

		// resizeImageByFactor();

		// float xScale = width / numberOfExpirments;
		// float yScale = height / numberOfElements;

		// Fgl.glBindFramebuffer(GL2.GL_DRAW_BUFFER, 0);
		// gl.glReadBuffer(GL2.GL_NONE);
		// gl.glDrawBuffer(GL2.GL_BACK);

	}

	// public float getVisUncForContent

	public float getValueFromBytes(byte[] abgr) {

		float val = -((abgr[2] + 128) / 255f) + ((abgr[3] + 128) / 255f);
		return val;

	}

	public void getScreenAreaShot2(GL2 gl, int x, int y, int width, int height) {

		// readScreenShot
		{
			ByteBuffer screenShotByteBuffer = null;
			BufferedImage screenShotImage = null;
			screenShotImage = new BufferedImage(width, height,
					BufferedImage.TYPE_4BYTE_ABGR);

			screenShotByteBuffer = ByteBuffer
					.wrap(((DataBufferByte) screenShotImage.getRaster()
							.getDataBuffer()).getData());

			gl.glReadBuffer(GL2.GL_FRONT);
			gl.glReadPixels(x, y, width, height, GL2.GL_ABGR_EXT,
					GL2.GL_UNSIGNED_BYTE, screenShotByteBuffer);

			Texture awtTexture = AWTTextureIO.newTexture(
					GLProfile.getDefault(), screenShotImage, false);
			// create new Texture from ScreenShot
			TextureData texData = new TextureData(GLProfile.getDefault(),
					GL2.GL_RGBA /* internalFormat */, height /* height */,
					width /* width */, 0 /* border */,
					GL2.GL_RGBA /* pixelFormat */,
					GL2.GL_UNSIGNED_BYTE /* pixelType */, false /* mipmap */,
					false /* dataIsCompressed */, false /* mustFlipVertically */,
					screenShotByteBuffer, null);
			Texture tex = TextureIO.newTexture(0);
			tex.updateImage(texData);

			try {
				ImageIO.write(screenShotImage, "png", new File(
						"C:\\Documents and Settings\\Clemens\\bild.png"));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		/*
		 * // Draw ScreenShot as new Texture in BackBuffer
		 * gl.glDrawBuffer(GL2.GL_BACK); // gl.glDrawBuffer(GL2.GL_FRONT);
		 * PixelGLConverter pixelGLConverter = this.uncertaintyHeatMap
		 * .getParentGLCanvas().getPixelGLConverter(); float glHeight =
		 * pixelGLConverter .getGLHeightForPixelHeight(numberOfElements); float
		 * glWidth = pixelGLConverter
		 * .getGLWidthForPixelWidth(numberOfExpirments); renderTexture(gl,
		 * awtTexture, 0, 0, glWidth, glHeight);
		 * 
		 * 
		 * 
		 * 
		 * // getNewScreenShots { gl.glReadBuffer(GL2.GL_BACK); ByteBuffer
		 * screenShotByteBuffer = null; BufferedImage screenShotImage = null;
		 * screenShotImage = new BufferedImage(width, height,
		 * BufferedImage.TYPE_4BYTE_ABGR);
		 * 
		 * screenShotByteBuffer = ByteBuffer .wrap(((DataBufferByte)
		 * screenShotImage.getRaster() .getDataBuffer()).getData());
		 * 
		 * gl.glReadBuffer(GL2.GL_BACK); gl.glReadPixels(x, y, width, height,
		 * GL2.GL_ABGR_EXT, GL2.GL_UNSIGNED_BYTE, screenShotByteBuffer);
		 * 
		 * try { ImageIO.write(screenShotImage, "png", new File(
		 * "C:\\Documents and Settings\\Clemens\\bild2.png")); } catch
		 * (IOException e) { // TODO Auto-generated catch block
		 * e.printStackTrace(); }
		 * 
		 * }
		 */
		gl.glReadBuffer(GL2.GL_FRONT);
		gl.glDrawBuffer(GL2.GL_FRONT);

	}

	public final static BufferedImage resizeImageByFactor(BufferedImage image,
			double factor) {
		int width = (int) (image.getWidth() * factor);
		int height = (int) (image.getHeight() * factor);
		BufferedImage newimage = new BufferedImage(width, height,
				BufferedImage.TYPE_INT_RGB);
		newimage.createGraphics().drawImage(image, 0, 0, width, height, null);

		return newimage;
	}

	public float getUncertaintyForLine(int imageLine, int width, int height) {

		float maxUncertainty = 0;
		float val = 0;
		float uncertainty = 0;

		float ratio = (float) numberOfElements / (float) height;
		int startGene = (int) ((ratio * imageLine) - (Math.round(ratio / 2f)));
		int endGene = (int) ((ratio * imageLine) + (Math.round(ratio / 2f)));
		startGene = startGene < 0 ? 0 : startGene;
		endGene = endGene > numberOfElements - 1 ? numberOfElements - 1
				: endGene;

		for (int exps = 0; exps < numberOfExpirments; exps++) {
			val = 0;

			for (int i = startGene; i < endGene; i++) {
				byte[] abgr = new byte[4];

				val = val+ ((set.get(storageVA.get(exps)).getFloat(
						EDataRepresentation.NORMALIZED, contentVA.get(i)) - 0.5f));
			}
			// buffer.get(abgr, i * numberOfExpirments * 4 + exps * 4, 4);

			// getting avr over genes
			val = val / (float) (endGene - startGene);
			// unc = difference
			uncertainty = 0;
			for (int i = startGene; i < endGene; i++) {
				float tempVal = set.get(storageVA.get(exps)).getFloat(
						EDataRepresentation.NORMALIZED, contentVA.get(i)) - 0.5f;
				uncertainty = Math.abs(val - tempVal);
				if (uncertainty > maxUncertainty) {
					maxUncertainty = uncertainty;
				}
			}
			//uncertainty = uncertainty / (float) (endGene - startGene);;
			//System.out.println(ratio);
		}

		return 1-maxUncertainty*2;
	}

}
