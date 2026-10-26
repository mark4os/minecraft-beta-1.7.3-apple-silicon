package net.minecraft.src;

import java.util.Comparator;

public class EntitySorter implements Comparator {
	public EntitySorter(Entity var1) {
	}

	public int sortByDistanceToEntity(WorldRenderer var1, WorldRenderer var2) {
		return Float.compare(var1.sortDistance, var2.sortDistance);
	}

	public int compare(Object var1, Object var2) {
		return this.sortByDistanceToEntity((WorldRenderer)var1, (WorldRenderer)var2);
	}
}
