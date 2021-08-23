package mc.thelblack.monitoring.bungee;

import java.io.File;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import mc.thelblack.monitoring.DockerInfo;
import mc.thelblack.monitoring.ThreadManager;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;

public class MainBungee extends Plugin {

	public static MainBungee INSTANCE;
	
	public static final Logger LOGGER = new Logger("ZMonitoring", null) {
		{
			this.setParent(ProxyServer.getInstance().getLogger());
		}
	};
	
	private ThreadManager thread = new ThreadManager() {

		@Override
		public Logger getLogger() {
			return MainBungee.LOGGER;
		}

		@Override
		public String getAlternativeName() {
			return ProxyServer.getInstance().getVersion();
		}

		@Override
		public void load(DockerInfo docker) {
			CounterBungee co = new CounterBungee(docker);
			
			this.setCounter(co);
			ProxyServer.getInstance().getPluginManager().registerListener(MainBungee.INSTANCE, co);
		}

		@Override
		public void unload() {}

		@Override
		public void schedule(Runnable r) {
			ProxyServer.getInstance().getScheduler().schedule(MainBungee.INSTANCE, r, 60L, TimeUnit.SECONDS);
		}

		@Override
		public File getRootFile() {
			return MainBungee.INSTANCE.getDataFolder().getParentFile().getParentFile();
		}
	};
	
	public MainBungee() {
		MainBungee.INSTANCE = this;
	}
	
	@Override
	public void onEnable() {
		this.thread.establishConnection();
	}
	
	@Override
	public void onDisable() {
		this.thread.shutdown();
	}
}
