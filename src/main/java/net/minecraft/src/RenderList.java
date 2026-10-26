package net.minecraft.src;

import java.nio.IntBuffer;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;

public class RenderList {
	private int field_1242_a;
	private int field_1241_b;
	private int field_1240_c;
	private float field_1239_d;
	private float field_1238_e;
	private float field_1237_f;
	private IntBuffer field_1236_g = GLAllocation.createDirectIntBuffer(65536);
	private final int[] vboIds = new int[16384];
	private final int[] vertexCounts = new int[16384];
	private int vboCount = 0;
	private boolean field_1235_h = false;
	private boolean field_1234_i = false;

	public void func_861_a(int var1, int var2, int var3, double var4, double var6, double var8) {
		this.field_1235_h = true;
		this.field_1236_g.clear();
		this.vboCount = 0;
		this.field_1242_a = var1;
		this.field_1241_b = var2;
		this.field_1240_c = var3;
		this.field_1239_d = (float)var4;
		this.field_1238_e = (float)var6;
		this.field_1237_f = (float)var8;
	}

	public boolean func_862_a(int var1, int var2, int var3) {
		return !this.field_1235_h ? false : var1 == this.field_1242_a && var2 == this.field_1241_b && var3 == this.field_1240_c;
	}

	public void addChunkVBO(int vboId, int vertexCount) {
		if(vboId >= 0 && vertexCount > 0 && this.vboCount < this.vboIds.length) {
			this.vboIds[this.vboCount] = vboId;
			this.vertexCounts[this.vboCount] = vertexCount;
			++this.vboCount;
		}
	}

	public void func_858_a(int var1) {
		this.field_1236_g.put(var1);
		if(this.field_1236_g.remaining() == 0) {
			this.func_860_a();
		}

	}

	public void func_860_a() {
		if(this.field_1235_h) {
			if(this.vboCount > 0) {
				GL11.glPushMatrix();
				GL11.glTranslatef((float)this.field_1242_a - this.field_1239_d, (float)this.field_1241_b - this.field_1238_e, (float)this.field_1240_c - this.field_1237_f);
				GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
				GL11.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
				GL11.glEnableClientState(GL11.GL_COLOR_ARRAY);
				for(int i = 0; i < this.vboCount; ++i) {
					GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, this.vboIds[i]);
					GL11.glVertexPointer(3, GL11.GL_FLOAT, 24, 0L);
					GL11.glTexCoordPointer(2, GL11.GL_FLOAT, 24, 12L);
					GL11.glColorPointer(4, GL11.GL_UNSIGNED_BYTE, 24, 20L);
					GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, this.vertexCounts[i]);
				}
				GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
				GL11.glDisableClientState(GL11.GL_VERTEX_ARRAY);
				GL11.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
				GL11.glDisableClientState(GL11.GL_COLOR_ARRAY);
				GL11.glPopMatrix();
				return;
			}
			if(!this.field_1234_i) {
				this.field_1236_g.flip();
				this.field_1234_i = true;
			}

			if(this.field_1236_g.remaining() > 0) {
				GL11.glPushMatrix();
				GL11.glTranslatef((float)this.field_1242_a - this.field_1239_d, (float)this.field_1241_b - this.field_1238_e, (float)this.field_1240_c - this.field_1237_f);
				GL11.glCallLists(this.field_1236_g);
				GL11.glPopMatrix();
			}

		}
	}

	public void func_859_b() {
		this.field_1235_h = false;
		this.field_1234_i = false;
		this.vboCount = 0;
	}
}
