package mc.thelblack.monitoring.bukkit;

import java.io.File;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import mc.thelblack.monitoring.DockerInfo;
import mc.thelblack.monitoring.ThreadManager;

public class MainBukkit extends JavaPlugin {

	public static MainBukkit INSTANCE;
	
	public static final Logger LOGGER = new Logger("ZMonitoring", null) {
		{
			this.setParent(Bukkit.getLogger());
		}
	};
	
	private ThreadManager thread = new ThreadManager() {

		@Override
		public Logger getLogger() {
			return MainBukkit.LOGGER;
		}

		@Override
		public String getAlternativeName() {
			return String.valueOf(Bukkit.getPort());
		}

		@Override
		public void load(DockerInfo docker) {
			CounterBukkit co = new CounterBukkit(docker);
			
			this.setCounter(co);
			Bukkit.getPluginManager().registerEvents(co, MainBukkit.INSTANCE);
		}

		@Override
		public void unload() {
			Bukkit.getPluginManager().disablePlugin(MainBukkit.INSTANCE);
		}

		@Override
		public void schedule(Runnable r) {
			Bukkit.getScheduler().runTaskLater(MainBukkit.INSTANCE, r, 1200L);
		}

		@Override
		public File getRootFile() {
			return MainBukkit.INSTANCE.getDataFolder().getParentFile().getParentFile();
		}
	};
	
	public MainBukkit() {
		MainBukkit.INSTANCE = this;
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
