package net.minecraft.src;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.IntBuffer;
import javax.imageio.ImageIO;
import org.lwjgl.opengl.GL11;

public class FontRenderer {
	private static final int COLOR_LIST_OFFSET = 352;
	private static final int TOTAL_GLYPHS = 256 + 74;
	private static final int[][] CYRILLIC_GLYPHS = new int[][] {
		// Uppercase: АБВГҐДЕЄЁЖЗИІЇЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯ (37)
		{0x38, 0x6C, 0xC6, 0xC6, 0xFE, 0xC6, 0xC6, 0x00}, // А
		{0xFE, 0xC0, 0xC0, 0xFC, 0xC6, 0xC6, 0xFC, 0x00}, // Б
		{0xFC, 0xC6, 0xC6, 0xFC, 0xC6, 0xC6, 0xFC, 0x00}, // В
		{0xFE, 0xC6, 0xC0, 0xC0, 0xC0, 0xC0, 0xC0, 0x00}, // Г
		{0x06, 0xFE, 0xC0, 0xC0, 0xC0, 0xC0, 0xC0, 0x00}, // Ґ
		{0x3C, 0x66, 0x66, 0x66, 0x66, 0xFF, 0xC3, 0xC3}, // Д
		{0xFE, 0xC0, 0xC0, 0xF8, 0xC0, 0xC0, 0xFE, 0x00}, // Е
		{0x3C, 0x66, 0xC0, 0xFC, 0xC0, 0x66, 0x3C, 0x00}, // Є
		{0x6C, 0x00, 0xFE, 0xC0, 0xF8, 0xC0, 0xFE, 0x00}, // Ё
		{0xD6, 0xD6, 0x7C, 0x38, 0x7C, 0xD6, 0xD6, 0x00}, // Ж
		{0x7C, 0xC6, 0x06, 0x3C, 0x06, 0xC6, 0x7C, 0x00}, // З
		{0xC6, 0xC6, 0xCE, 0xDE, 0xF6, 0xE6, 0xC6, 0x00}, // И
		{0x7E, 0x18, 0x18, 0x18, 0x18, 0x18, 0x7E, 0x00}, // І
		{0x6C, 0x00, 0x7E, 0x18, 0x18, 0x18, 0x7E, 0x00}, // Ї
		{0x38, 0x00, 0xC6, 0xCE, 0xDE, 0xF6, 0xC6, 0x00}, // Й
		{0xC6, 0xCC, 0xD8, 0xF0, 0xD8, 0xCC, 0xC6, 0x00}, // К
		{0x3E, 0x66, 0x66, 0x66, 0x66, 0x66, 0xC6, 0x00}, // Л
		{0xC6, 0xEE, 0xFE, 0xD6, 0xC6, 0xC6, 0xC6, 0x00}, // М
		{0xC6, 0xC6, 0xC6, 0xFE, 0xC6, 0xC6, 0xC6, 0x00}, // Н
		{0x7C, 0xC6, 0xC6, 0xC6, 0xC6, 0xC6, 0x7C, 0x00}, // О
		{0xFE, 0xC6, 0xC6, 0xC6, 0xC6, 0xC6, 0xC6, 0x00}, // П
		{0xFC, 0xC6, 0xC6, 0xFC, 0xC0, 0xC0, 0xC0, 0x00}, // Р
		{0x7C, 0xC6, 0xC0, 0xC0, 0xC0, 0xC6, 0x7C, 0x00}, // С
		{0xFE, 0x38, 0x38, 0x38, 0x38, 0x38, 0x38, 0x00}, // Т
		{0xC6, 0xC6, 0xC6, 0x7E, 0x06, 0xC6, 0x7C, 0x00}, // У
		{0x38, 0x7C, 0xD6, 0xD6, 0x7C, 0x38, 0x38, 0x00}, // Ф
		{0xC6, 0xC6, 0x6C, 0x38, 0x6C, 0xC6, 0xC6, 0x00}, // Х
		{0xCC, 0xCC, 0xCC, 0xCC, 0xCC, 0xCC, 0xFE, 0x06}, // Ц
		{0xC6, 0xC6, 0xC6, 0x7E, 0x06, 0x06, 0x06, 0x00}, // Ч
		{0xD6, 0xD6, 0xD6, 0xD6, 0xD6, 0xD6, 0xFE, 0x00}, // Ш
		{0xD6, 0xD6, 0xD6, 0xD6, 0xD6, 0xD6, 0xFF, 0x03}, // Щ
		{0xE0, 0x60, 0x60, 0x7C, 0x66, 0x66, 0x7C, 0x00}, // Ъ
		{0xC6, 0xC6, 0xC6, 0xF6, 0xCE, 0xCE, 0xF6, 0x00}, // Ы
		{0xC0, 0xC0, 0xC0, 0xFC, 0xC6, 0xC6, 0xFC, 0x00}, // Ь
		{0x7C, 0xC6, 0x06, 0x3E, 0x06, 0xC6, 0x7C, 0x00}, // Э
		{0xCE, 0xDB, 0xDB, 0xFB, 0xDB, 0xDB, 0xCE, 0x00}, // Ю
		{0x7E, 0xC6, 0xC6, 0x7E, 0x36, 0x66, 0xC6, 0x00}, // Я
		// Lowercase: абвгґдеєёжзиіїйклмнопрстуфхцчшщъыьэюя (37)
		{0x00, 0x00, 0x78, 0x0C, 0x7C, 0xCC, 0x76, 0x00}, // а
		{0x3C, 0x60, 0xFC, 0xC6, 0xC6, 0xC6, 0x7C, 0x00}, // б
		{0x00, 0x00, 0xFC, 0xC6, 0xFC, 0xC6, 0xFC, 0x00}, // в
		{0x00, 0x00, 0xFE, 0xC6, 0xC0, 0xC0, 0xC0, 0x00}, // г
		{0x00, 0x06, 0xFE, 0xC0, 0xC0, 0xC0, 0xC0, 0x00}, // ґ
		{0x00, 0x00, 0x3C, 0x66, 0x66, 0xFF, 0xC3, 0x00}, // д
		{0x00, 0x00, 0x7C, 0xC6, 0xFE, 0xC0, 0x7C, 0x00}, // е
		{0x00, 0x00, 0x7C, 0xC0, 0xFC, 0xC0, 0x7C, 0x00}, // є
		{0x6C, 0x00, 0x7C, 0xC6, 0xFE, 0xC0, 0x7C, 0x00}, // ё
		{0x00, 0x00, 0xD6, 0x7C, 0x38, 0x7C, 0xD6, 0x00}, // ж
		{0x00, 0x00, 0x7C, 0x06, 0x3C, 0x06, 0x7C, 0x00}, // з
		{0x00, 0x00, 0xC6, 0xCE, 0xDE, 0xF6, 0xC6, 0x00}, // и
		{0x30, 0x00, 0x70, 0x30, 0x30, 0x30, 0x78, 0x00}, // і
		{0x6C, 0x00, 0x38, 0x18, 0x18, 0x18, 0x3C, 0x00}, // ї
		{0x38, 0x00, 0xC6, 0xCE, 0xDE, 0xF6, 0xC6, 0x00}, // й
		{0x00, 0x00, 0xC6, 0xCC, 0xF8, 0xCC, 0xC6, 0x00}, // к
		{0x00, 0x00, 0x3E, 0x66, 0x66, 0x66, 0xC6, 0x00}, // л
		{0x00, 0x00, 0xC6, 0xEE, 0xFE, 0xD6, 0xC6, 0x00}, // м
		{0x00, 0x00, 0xC6, 0xC6, 0xFE, 0xC6, 0xC6, 0x00}, // н
		{0x00, 0x00, 0x7C, 0xC6, 0xC6, 0xC6, 0x7C, 0x00}, // о
		{0x00, 0x00, 0xFE, 0xC6, 0xC6, 0xC6, 0xC6, 0x00}, // п
		{0x00, 0x00, 0xFC, 0xC6, 0xC6, 0xFC, 0xC0, 0xC0}, // р
		{0x00, 0x00, 0x7C, 0xC6, 0xC0, 0xC6, 0x7C, 0x00}, // с
		{0x00, 0x00, 0xFE, 0x38, 0x38, 0x38, 0x38, 0x00}, // т
		{0x00, 0x00, 0xC6, 0xC6, 0xC6, 0x7E, 0x06, 0x7C}, // у
		{0x00, 0x38, 0x7C, 0xD6, 0xD6, 0x7C, 0x38, 0x38}, // ф
		{0x00, 0x00, 0xC6, 0x6C, 0x38, 0x6C, 0xC6, 0x00}, // х
		{0x00, 0x00, 0xCC, 0xCC, 0xCC, 0xCC, 0xFE, 0x06}, // ц
		{0x00, 0x00, 0xC6, 0xC6, 0x7E, 0x06, 0x06, 0x00}, // ч
		{0x00, 0x00, 0xD6, 0xD6, 0xD6, 0xD6, 0xFE, 0x00}, // ш
		{0x00, 0x00, 0xD6, 0xD6, 0xD6, 0xD6, 0xFF, 0x03}, // щ
		{0x00, 0x00, 0xE0, 0x60, 0x7C, 0x66, 0x7C, 0x00}, // ъ
		{0x00, 0x00, 0xC6, 0xC6, 0xF6, 0xCE, 0xF6, 0x00}, // ы
		{0x00, 0x00, 0xC0, 0xC0, 0xFC, 0xC6, 0xFC, 0x00}, // ь
		{0x00, 0x00, 0x7C, 0x06, 0x3E, 0x06, 0x7C, 0x00}, // э
		{0x00, 0x00, 0xCE, 0xDB, 0xFB, 0xDB, 0xCE, 0x00}, // ю
		{0x00, 0x00, 0x7E, 0xC6, 0x7E, 0x66, 0xC6, 0x00}  // я
	};

	private int[] charWidth = new int[512];
	public int fontTextureName = 0;
	private int fontDisplayLists;
	private IntBuffer buffer = GLAllocation.createDirectIntBuffer(1024);

	public FontRenderer(GameSettings var1, String var2, RenderEngine var3) {
		BufferedImage origImg;
		try {
			origImg = ImageIO.read(RenderEngine.class.getResourceAsStream(var2));
		} catch (IOException var18) {
			throw new RuntimeException(var18);
		}

		int var5 = origImg.getWidth();
		int origH = origImg.getHeight();
		int var6 = origH * 2;
		BufferedImage var4 = new BufferedImage(var5, var6, BufferedImage.TYPE_INT_ARGB);
		int[] origPixels = new int[var5 * origH];
		origImg.getRGB(0, 0, var5, origH, origPixels, 0, var5);
		var4.setRGB(0, 0, var5, origH, origPixels, 0, var5);

		int cellW = var5 / 16;
		int cellH = origH / 16;
		for (int g = 0; g < CYRILLIC_GLYPHS.length; ++g) {
			int slot = 256 + g;
			int gx = (slot % 16) * cellW;
			int gy = (slot / 16) * cellH;
			int[] rows = CYRILLIC_GLYPHS[g];
			for (int py = 0; py < cellH; ++py) {
				int rowBits = rows[py * 8 / cellH];
				for (int px = 0; px < cellW; ++px) {
					int bitIdx = 7 - (px * 8 / cellW);
					if (((rowBits >> bitIdx) & 1) != 0) {
						var4.setRGB(gx + px, gy + py, 0xFFFFFFFF);
					}
				}
			}
		}

		int[] var7 = new int[var5 * var6];
		var4.getRGB(0, 0, var5, var6, var7, 0, var5);

		int var9;
		int var10;
		int var11;
		int var12;
		int var15;
		int var16;
		for(int var8 = 0; var8 < TOTAL_GLYPHS; ++var8) {
			var9 = var8 % 16;
			var10 = var8 / 16;

			for(var11 = 7; var11 >= 0; --var11) {
				var12 = var9 * 8 + var11;
				boolean var13 = true;

				for(int var14 = 0; var14 < 8 && var13; ++var14) {
					var15 = (var10 * 8 + var14) * var5;
					var16 = var7[var12 + var15] & 255;
					if(var16 > 0) {
						var13 = false;
					}
				}

				if(!var13) {
					break;
				}
			}

			if(var8 == 32) {
				var11 = 2;
			}

			this.charWidth[var8] = var11 + 2;
		}

		this.fontTextureName = var3.allocateAndSetupTexture(var4);
		this.fontDisplayLists = GLAllocation.generateDisplayLists(COLOR_LIST_OFFSET + 32);
		Tessellator var19 = Tessellator.instance;

		for(var9 = 0; var9 < TOTAL_GLYPHS; ++var9) {
			GL11.glNewList(this.fontDisplayLists + var9, GL11.GL_COMPILE);
			var19.startDrawingQuads();
			var10 = var9 % 16 * 8;
			var11 = var9 / 16 * 8;
			float var20 = 7.99F;
			float var21 = 0.0F;
			float var23 = 0.0F;
			var19.addVertexWithUV(0.0D, (double)(0.0F + var20), 0.0D, (double)((float)var10 / 128.0F + var21), (double)(((float)var11 + var20) / 256.0F + var23));
			var19.addVertexWithUV((double)(0.0F + var20), (double)(0.0F + var20), 0.0D, (double)(((float)var10 + var20) / 128.0F + var21), (double)(((float)var11 + var20) / 256.0F + var23));
			var19.addVertexWithUV((double)(0.0F + var20), 0.0D, 0.0D, (double)(((float)var10 + var20) / 128.0F + var21), (double)((float)var11 / 256.0F + var23));
			var19.addVertexWithUV(0.0D, 0.0D, 0.0D, (double)((float)var10 / 128.0F + var21), (double)((float)var11 / 256.0F + var23));
			var19.draw();
			GL11.glTranslatef((float)this.charWidth[var9], 0.0F, 0.0F);
			GL11.glEndList();
		}

		for(var9 = 0; var9 < 32; ++var9) {
			var10 = (var9 >> 3 & 1) * 85;
			var11 = (var9 >> 2 & 1) * 170 + var10;
			var12 = (var9 >> 1 & 1) * 170 + var10;
			int var22 = (var9 >> 0 & 1) * 170 + var10;
			if(var9 == 6) {
				var11 += 85;
			}

			boolean var24 = var9 >= 16;
			if(var1.anaglyph) {
				var15 = (var11 * 30 + var12 * 59 + var22 * 11) / 100;
				var16 = (var11 * 30 + var12 * 70) / 100;
				int var17 = (var11 * 30 + var22 * 70) / 100;
				var11 = var15;
				var12 = var16;
				var22 = var17;
			}

			if(var24) {
				var11 /= 4;
				var12 /= 4;
				var22 /= 4;
			}

			GL11.glNewList(this.fontDisplayLists + COLOR_LIST_OFFSET + var9, GL11.GL_COMPILE);
			GL11.glColor3f((float)var11 / 255.0F, (float)var12 / 255.0F, (float)var22 / 255.0F);
			GL11.glEndList();
		}

	}

	public void drawStringWithShadow(String var1, int var2, int var3, int var4) {
		this.renderString(var1, var2 + 1, var3 + 1, var4, true);
		this.drawString(var1, var2, var3, var4);
	}

	public void drawString(String var1, int var2, int var3, int var4) {
		this.renderString(var1, var2, var3, var4, false);
	}

	public void renderString(String var1, int var2, int var3, int var4, boolean var5) {
		if(var1 != null) {
			int var6;
			if(var5) {
				var6 = var4 & -16777216;
				var4 = (var4 & 16579836) >> 2;
				var4 += var6;
			}

			GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.fontTextureName);
			float var10 = (float)(var4 >> 16 & 255) / 255.0F;
			float var7 = (float)(var4 >> 8 & 255) / 255.0F;
			float var8 = (float)(var4 & 255) / 255.0F;
			float var9 = (float)(var4 >> 24 & 255) / 255.0F;
			if(var9 == 0.0F) {
				var9 = 1.0F;
			}

			GL11.glColor4f(var10, var7, var8, var9);
			this.buffer.clear();
			GL11.glPushMatrix();
			GL11.glTranslatef((float)var2, (float)var3, 0.0F);

			for(var6 = 0; var6 < var1.length(); ++var6) {
				int var11;
				for(; var1.length() > var6 + 1 && var1.charAt(var6) == 167; var6 += 2) {
					var11 = "0123456789abcdef".indexOf(Character.toLowerCase(var1.charAt(var6 + 1)));
					if(var11 < 0 || var11 > 15) {
						var11 = 15;
					}

					this.buffer.put(this.fontDisplayLists + COLOR_LIST_OFFSET + var11 + (var5 ? 16 : 0));
					if(this.buffer.remaining() == 0) {
						this.buffer.flip();
						GL11.glCallLists(this.buffer);
						this.buffer.clear();
					}
				}

				if(var6 < var1.length()) {
					char ch = var1.charAt(var6);
					int cyrIdx = ChatAllowedCharacters.CYRILLIC_CHARS.indexOf(ch);
					if(cyrIdx >= 0) {
						this.buffer.put(this.fontDisplayLists + 256 + cyrIdx);
					} else {
						var11 = ChatAllowedCharacters.allowedCharacters.indexOf(ch);
						if(var11 >= 0 && var11 + 32 < 256) {
							this.buffer.put(this.fontDisplayLists + var11 + 32);
						}
					}
				}

				if(this.buffer.remaining() == 0) {
					this.buffer.flip();
					GL11.glCallLists(this.buffer);
					this.buffer.clear();
				}
			}

			this.buffer.flip();
			GL11.glCallLists(this.buffer);
			GL11.glPopMatrix();
		}
	}

	public int getStringWidth(String var1) {
		if(var1 == null) {
			return 0;
		} else {
			int var2 = 0;

			for(int var3 = 0; var3 < var1.length(); ++var3) {
				if(var1.charAt(var3) == 167) {
					++var3;
				} else {
					char ch = var1.charAt(var3);
					int cyrIdx = ChatAllowedCharacters.CYRILLIC_CHARS.indexOf(ch);
					if(cyrIdx >= 0) {
						var2 += this.charWidth[256 + cyrIdx];
					} else {
						int var4 = ChatAllowedCharacters.allowedCharacters.indexOf(ch);
						if(var4 >= 0 && var4 + 32 < 256) {
							var2 += this.charWidth[var4 + 32];
						}
					}
				}
			}

			return var2;
		}
	}

	public void func_27278_a(String var1, int var2, int var3, int var4, int var5) {
		String[] var6 = var1.split("\n");
		if(var6.length > 1) {
			for(int var11 = 0; var11 < var6.length; ++var11) {
				this.func_27278_a(var6[var11], var2, var3, var4, var5);
				var3 += this.func_27277_a(var6[var11], var4);
			}

		} else {
			String[] var7 = var1.split(" ");
			int var8 = 0;

			while(var8 < var7.length) {
				String var9;
				for(var9 = var7[var8++] + " "; var8 < var7.length && this.getStringWidth(var9 + var7[var8]) < var4; var9 = var9 + var7[var8++] + " ") {
				}

				int var10;
				for(; this.getStringWidth(var9) > var4; var9 = var9.substring(var10)) {
					for(var10 = 0; this.getStringWidth(var9.substring(0, var10 + 1)) <= var4; ++var10) {
					}

					if(var9.substring(0, var10).trim().length() > 0) {
						this.drawString(var9.substring(0, var10), var2, var3, var5);
						var3 += 8;
					}
				}

				if(var9.trim().length() > 0) {
					this.drawString(var9, var2, var3, var5);
					var3 += 8;
				}
			}

		}
	}

	public int func_27277_a(String var1, int var2) {
		String[] var3 = var1.split("\n");
		int var5;
		if(var3.length > 1) {
			int var9 = 0;

			for(var5 = 0; var5 < var3.length; ++var5) {
				var9 += this.func_27277_a(var3[var5], var2);
			}

			return var9;
		} else {
			String[] var4 = var1.split(" ");
			var5 = 0;
			int var6 = 0;

			while(var5 < var4.length) {
				String var7;
				for(var7 = var4[var5++] + " "; var5 < var4.length && this.getStringWidth(var7 + var4[var5]) < var2; var7 = var7 + var4[var5++] + " ") {
				}

				int var8;
				for(; this.getStringWidth(var7) > var2; var7 = var7.substring(var8)) {
					for(var8 = 0; this.getStringWidth(var7.substring(0, var8 + 1)) <= var2; ++var8) {
					}

					if(var7.substring(0, var8).trim().length() > 0) {
						var6 += 8;
					}
				}

				if(var7.trim().length() > 0) {
					var6 += 8;
				}
			}

			if(var6 < 8) {
				var6 += 8;
			}

			return var6;
		}
	}
}
