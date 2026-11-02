package net.minecraft.src;

import java.io.IOException;

class NetworkWriterThread extends Thread {
	final NetworkManager netManager;

	NetworkWriterThread(NetworkManager var1, String var2) {
		super(var2);
		this.netManager = var1;
	}

	public void run() {
		Object var1 = NetworkManager.threadSyncObject;
		synchronized(var1) {
			++NetworkManager.numWriteThreads;
		}

		while(true) {
			boolean var13 = false;

			try {
				var13 = true;
				if(!NetworkManager.isRunning(this.netManager)) {
					var13 = false;
					break;
				}

				boolean sentAny = false;
				while(NetworkManager.sendNetworkPacket(this.netManager)) {
					sentAny = true;
				}

				try {
					if(sentAny && NetworkManager.func_28140_f(this.netManager) != null) {
						NetworkManager.func_28140_f(this.netManager).flush();
					}
				} catch (IOException var18) {
					if(!NetworkManager.func_28138_e(this.netManager)) {
						NetworkManager.func_30005_a(this.netManager, var18);
					}

					var18.printStackTrace();
				}

				try {
					sleep(15L);
				} catch (InterruptedException var16) {
				}
			} finally {
				if(var13) {
					Object var5 = NetworkManager.threadSyncObject;
					synchronized(var5) {
						--NetworkManager.numWriteThreads;
					}
				}
			}
		}

		var1 = NetworkManager.threadSyncObject;
		synchronized(var1) {
			--NetworkManager.numWriteThreads;
		}
	}
}
