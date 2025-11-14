package net.ankrya.rider_api.reverse;

import net.ankrya.rider_api.reverse.client.ClientHandle;
import net.ankrya.rider_api.reverse.server.ServerHandle;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

public class TMIMain {

	public static ClientHandle clientHandle;
	public static ServerHandle serverHandle;
	
	public static void init() {
		
	}

	@OnlyIn(Dist.CLIENT)
	public static void initClientHandle() {
		clientHandle = new ClientHandle();
		clientHandle.init();
	}
	
	public static void initServerHandle(ServerStartedEvent event) {
		serverHandle = new ServerHandle(event.getServer());
		serverHandle.init();
	}
	
}
