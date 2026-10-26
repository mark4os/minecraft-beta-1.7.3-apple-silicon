package net.minecraft.src;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import org.lwjgl.opengl.ARBVertexBufferObject;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GLContext;

public class Tessellator {
	private static boolean convertQuadsToTriangles = false;
	private static boolean tryVBO = false;
	private static final boolean IS_LITTLE_ENDIAN = ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN;
	private ByteBuffer byteBuffer;
	private IntBuffer intBuffer;
	private FloatBuffer floatBuffer;
	private int[] rawBuffer;
	private int vertexCount = 0;
	private double textureU;
	private double textureV;
	private int color;
	private boolean hasColor = false;
	private boolean hasTexture = false;
	private boolean hasNormals = false;
	private int rawBufferIndex = 0;
	private int addedVertices = 0;
	private boolean isColorDisabled = false;
	private int drawMode;
	private double xOffset;
	private double yOffset;
	private double zOffset;
	private int normal;
	public static final Tessellator instance = new Tessellator(2097152);
	private boolean isDrawing = false;
	private boolean useVBO = false;
	private IntBuffer vertexBuffers;
	private int vboIndex = 0;
	private int vboCount = 10;
	private int bufferSize;

	private Tessellator(int var1) {
		this.bufferSize = var1;
		this.byteBuffer = GLAllocation.createDirectByteBuffer(var1 * 4);
		this.intBuffer = this.byteBuffer.asIntBuffer();
		this.floatBuffer = this.byteBuffer.asFloatBuffer();
		this.rawBuffer = new int[var1];
		this.useVBO = tryVBO && GLContext.getCapabilities().GL_ARB_vertex_buffer_object;
		if(this.useVBO) {
			this.vertexBuffers = GLAllocation.createDirectIntBuffer(this.vboCount);
			ARBVertexBufferObject.glGenBuffersARB(this.vertexBuffers);
		}

	}

	public void draw() {
		if(!this.isDrawing) {
			throw new IllegalStateException("Not tesselating!");
		} else {
			this.isDrawing = false;
			if(this.vertexCount > 0) {
				this.intBuffer.clear();
				this.intBuffer.put(this.rawBuffer, 0, this.rawBufferIndex);
				this.byteBuffer.position(0);
				this.byteBuffer.limit(this.rawBufferIndex * 4);
				if(this.useVBO) {
					this.vboIndex = (this.vboIndex + 1) % this.vboCount;
					ARBVertexBufferObject.glBindBufferARB(GL15.GL_ARRAY_BUFFER, this.vertexBuffers.get(this.vboIndex));
					ARBVertexBufferObject.glBufferDataARB(GL15.GL_ARRAY_BUFFER, this.byteBuffer, GL15.GL_STREAM_DRAW);
				}

				if(this.hasTexture) {
					if(this.useVBO) {
						GL11.glTexCoordPointer(2, GL11.GL_FLOAT, 32, 12L);
					} else {
						this.floatBuffer.position(3);
						GL11.glTexCoordPointer(2, GL11.GL_FLOAT, 32, (FloatBuffer)this.floatBuffer);
					}

					GL11.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
				}

				if(this.hasColor) {
					if(this.useVBO) {
						GL11.glColorPointer(4, GL11.GL_UNSIGNED_BYTE, 32, 20L);
					} else {
						this.byteBuffer.position(20);
						GL11.glColorPointer(4, GL11.GL_UNSIGNED_BYTE, 32, this.byteBuffer);
					}

					GL11.glEnableClientState(GL11.GL_COLOR_ARRAY);
				}

				if(this.hasNormals) {
					if(this.useVBO) {
						GL11.glNormalPointer(GL11.GL_BYTE, 32, 24L);
					} else {
						this.byteBuffer.position(24);
						GL11.glNormalPointer(GL11.GL_BYTE, 32, (ByteBuffer)this.byteBuffer);
					}

					GL11.glEnableClientState(GL11.GL_NORMAL_ARRAY);
				}

				if(this.useVBO) {
					GL11.glVertexPointer(3, GL11.GL_FLOAT, 32, 0L);
				} else {
					this.floatBuffer.position(0);
					GL11.glVertexPointer(3, GL11.GL_FLOAT, 32, (FloatBuffer)this.floatBuffer);
				}

				GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
				if(this.drawMode == 7 && convertQuadsToTriangles) {
					GL11.glDrawArrays(GL11.GL_TRIANGLES, GL11.GL_POINTS, this.vertexCount);
				} else {
					GL11.glDrawArrays(this.drawMode, GL11.GL_POINTS, this.vertexCount);
				}

				GL11.glDisableClientState(GL11.GL_VERTEX_ARRAY);
				if(this.hasTexture) {
					GL11.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
				}

				if(this.hasColor) {
					GL11.glDisableClientState(GL11.GL_COLOR_ARRAY);
				}

				if(this.hasNormals) {
					GL11.glDisableClientState(GL11.GL_NORMAL_ARRAY);
				}
			}

			this.reset();
		}
	}

	private void reset() {
		this.vertexCount = 0;
		this.byteBuffer.clear();
		this.rawBufferIndex = 0;
		this.addedVertices = 0;
	}

	public void abort() {
		this.isDrawing = false;
		this.reset();
	}

	public int drawToChunkVBO(int vboId) {
		if(!this.isDrawing) {
			throw new IllegalStateException("Not tesselating!");
		} else {
			this.isDrawing = false;
			int triVerts = 0;
			if(this.vertexCount > 0) {
				int numQuads = this.vertexCount >> 2;
				triVerts = numQuads * 6;
				int totalInts = numQuads * 36;
				int[] raw = this.rawBuffer;
				for(int q = numQuads - 1; q >= 0; --q) {
					int src = q << 5;
					int dst = q * 36;
					int v0x = raw[src];
					int v0y = raw[src + 1];
					int v0z = raw[src + 2];
					int v0u = raw[src + 3];
					int v0v = raw[src + 4];
					int v0c = raw[src + 5];

					int v1x = raw[src + 8];
					int v1y = raw[src + 9];
					int v1z = raw[src + 10];
					int v1u = raw[src + 11];
					int v1v = raw[src + 12];
					int v1c = raw[src + 13];

					int v2x = raw[src + 16];
					int v2y = raw[src + 17];
					int v2z = raw[src + 18];
					int v2u = raw[src + 19];
					int v2v = raw[src + 20];
					int v2c = raw[src + 21];

					int v3x = raw[src + 24];
					int v3y = raw[src + 25];
					int v3z = raw[src + 26];
					int v3u = raw[src + 27];
					int v3v = raw[src + 28];
					int v3c = raw[src + 29];

					raw[dst] = v0x;
					raw[dst + 1] = v0y;
					raw[dst + 2] = v0z;
					raw[dst + 3] = v0u;
					raw[dst + 4] = v0v;
					raw[dst + 5] = v0c;

					raw[dst + 6] = v1x;
					raw[dst + 7] = v1y;
					raw[dst + 8] = v1z;
					raw[dst + 9] = v1u;
					raw[dst + 10] = v1v;
					raw[dst + 11] = v1c;

					raw[dst + 12] = v2x;
					raw[dst + 13] = v2y;
					raw[dst + 14] = v2z;
					raw[dst + 15] = v2u;
					raw[dst + 16] = v2v;
					raw[dst + 17] = v2c;

					raw[dst + 18] = v0x;
					raw[dst + 19] = v0y;
					raw[dst + 20] = v0z;
					raw[dst + 21] = v0u;
					raw[dst + 22] = v0v;
					raw[dst + 23] = v0c;

					raw[dst + 24] = v2x;
					raw[dst + 25] = v2y;
					raw[dst + 26] = v2z;
					raw[dst + 27] = v2u;
					raw[dst + 28] = v2v;
					raw[dst + 29] = v2c;

					raw[dst + 30] = v3x;
					raw[dst + 31] = v3y;
					raw[dst + 32] = v3z;
					raw[dst + 33] = v3u;
					raw[dst + 34] = v3v;
					raw[dst + 35] = v3c;
				}
				this.intBuffer.clear();
				this.intBuffer.put(raw, 0, totalInts);
				this.byteBuffer.position(0);
				this.byteBuffer.limit(totalInts << 2);
				GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboId);
				GL15.glBufferData(GL15.GL_ARRAY_BUFFER, this.byteBuffer, GL15.GL_STATIC_DRAW);
				GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
			}
			this.reset();
			return triVerts;
		}
	}

	public void startDrawingQuads() {
		this.startDrawing(7);
	}

	public void startDrawing(int var1) {
		if(this.isDrawing) {
			throw new IllegalStateException("Already tesselating!");
		} else {
			this.isDrawing = true;
			this.reset();
			this.drawMode = var1;
			this.hasNormals = false;
			this.hasColor = false;
			this.hasTexture = false;
			this.isColorDisabled = false;
		}
	}

	public void setTextureUV(double var1, double var3) {
		this.hasTexture = true;
		this.textureU = var1;
		this.textureV = var3;
	}

	public void setColorOpaque_F(float var1, float var2, float var3) {
		this.setColorOpaque((int)(var1 * 255.0F), (int)(var2 * 255.0F), (int)(var3 * 255.0F));
	}

	public void setColorRGBA_F(float var1, float var2, float var3, float var4) {
		this.setColorRGBA((int)(var1 * 255.0F), (int)(var2 * 255.0F), (int)(var3 * 255.0F), (int)(var4 * 255.0F));
	}

	public void setColorOpaque(int var1, int var2, int var3) {
		this.setColorRGBA(var1, var2, var3, 255);
	}

	public void setColorRGBA(int var1, int var2, int var3, int var4) {
		if(!this.isColorDisabled) {
			if(var1 > 255) {
				var1 = 255;
			}

			if(var2 > 255) {
				var2 = 255;
			}

			if(var3 > 255) {
				var3 = 255;
			}

			if(var4 > 255) {
				var4 = 255;
			}

			if(var1 < 0) {
				var1 = 0;
			}

			if(var2 < 0) {
				var2 = 0;
			}

			if(var3 < 0) {
				var3 = 0;
			}

			if(var4 < 0) {
				var4 = 0;
			}

			this.hasColor = true;
			if(IS_LITTLE_ENDIAN) {
				this.color = var4 << 24 | var3 << 16 | var2 << 8 | var1;
			} else {
				this.color = var1 << 24 | var2 << 16 | var3 << 8 | var4;
			}

		}
	}

	public void addVertexWithUV(double var1, double var3, double var5, double var7, double var9) {
		this.setTextureUV(var7, var9);
		this.addVertex(var1, var3, var5);
	}

	public void addVertex(double var1, double var3, double var5) {
		++this.addedVertices;
		if(this.drawMode == 7 && convertQuadsToTriangles && this.addedVertices % 4 == 0) {
			for(int var7 = 0; var7 < 2; ++var7) {
				int var8 = 8 * (3 - var7);
				if(this.hasTexture) {
					this.rawBuffer[this.rawBufferIndex + 3] = this.rawBuffer[this.rawBufferIndex - var8 + 3];
					this.rawBuffer[this.rawBufferIndex + 4] = this.rawBuffer[this.rawBufferIndex - var8 + 4];
				}

				if(this.hasColor) {
					this.rawBuffer[this.rawBufferIndex + 5] = this.rawBuffer[this.rawBufferIndex - var8 + 5];
				}

				this.rawBuffer[this.rawBufferIndex + 0] = this.rawBuffer[this.rawBufferIndex - var8 + 0];
				this.rawBuffer[this.rawBufferIndex + 1] = this.rawBuffer[this.rawBufferIndex - var8 + 1];
				this.rawBuffer[this.rawBufferIndex + 2] = this.rawBuffer[this.rawBufferIndex - var8 + 2];
				++this.vertexCount;
				this.rawBufferIndex += 8;
			}
		}

		if(this.hasTexture) {
			this.rawBuffer[this.rawBufferIndex + 3] = Float.floatToRawIntBits((float)this.textureU);
			this.rawBuffer[this.rawBufferIndex + 4] = Float.floatToRawIntBits((float)this.textureV);
		}

		if(this.hasColor) {
			this.rawBuffer[this.rawBufferIndex + 5] = this.color;
		}

		if(this.hasNormals) {
			this.rawBuffer[this.rawBufferIndex + 6] = this.normal;
		}

		this.rawBuffer[this.rawBufferIndex + 0] = Float.floatToRawIntBits((float)(var1 + this.xOffset));
		this.rawBuffer[this.rawBufferIndex + 1] = Float.floatToRawIntBits((float)(var3 + this.yOffset));
		this.rawBuffer[this.rawBufferIndex + 2] = Float.floatToRawIntBits((float)(var5 + this.zOffset));
		this.rawBufferIndex += 8;
		++this.vertexCount;
		if(this.rawBufferIndex >= this.bufferSize - 32 && (this.vertexCount & 3) == 0) {
			this.draw();
			this.isDrawing = true;
		}

	}

	public void setColorOpaque_I(int var1) {
		int var2 = var1 >> 16 & 255;
		int var3 = var1 >> 8 & 255;
		int var4 = var1 & 255;
		this.setColorOpaque(var2, var3, var4);
	}

	public void setColorRGBA_I(int var1, int var2) {
		int var3 = var1 >> 16 & 255;
		int var4 = var1 >> 8 & 255;
		int var5 = var1 & 255;
		this.setColorRGBA(var3, var4, var5, var2);
	}

	public void disableColor() {
		this.isColorDisabled = true;
	}

	public void setNormal(float var1, float var2, float var3) {
		if(!this.isDrawing) {
			System.out.println("But..");
		}

		this.hasNormals = true;
		byte var4 = (byte)((int)(var1 * 128.0F));
		byte var5 = (byte)((int)(var2 * 127.0F));
		byte var6 = (byte)((int)(var3 * 127.0F));
		this.normal = var4 | var5 << 8 | var6 << 16;
	}

	public void setTranslationD(double var1, double var3, double var5) {
		this.xOffset = var1;
		this.yOffset = var3;
		this.zOffset = var5;
	}

	public void setTranslationF(float var1, float var2, float var3) {
		this.xOffset += (double)var1;
		this.yOffset += (double)var2;
		this.zOffset += (double)var3;
	}
}
