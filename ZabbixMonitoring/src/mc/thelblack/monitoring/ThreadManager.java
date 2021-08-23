package mc.thelblack.monitoring;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import mc.thelblack.monitoring.DockerInfo.ContainerData;

public abstract class ThreadManager {
	
	private ScheduledExecutorService thread = Executors.newSingleThreadScheduledExecutor(a -> {
		return new Thread(a, "Zabbix Thread");
	});
	
	private Zabbix server;
	private Counter counter;
	
	public ThreadManager() {}
	
	public void setCounter(Counter c) {
		this.counter = c;
	}
	
	public void establishConnection() {
		this.establish(true);
	}
	
	public void shutdown() {
		this.thread.shutdown();
	}
	
	private void establish(boolean firstTime) {
		try {
			new Zabbix("test");
		} catch (IOException e) {
			this.getLogger().warning(String.format("Test connection to the zabbix server failed. To prevent further errors plugin will be disabled. (%s)", e.getMessage()));
			
			this.unload();
		}

		DockerInfo docker = new DockerInfo(this.getRootFile());
		
		try {
			ContainerData data = docker.reloadData();
			this.launch(docker, data.getUUID());
		}
		catch (Exception e) {
			if (firstTime) {
				this.getLogger().warning(String.format("Could not get server uuid from %s file. There will be one more retry in 60 seconds. (%s)", DockerInfo.FILE, e.getMessage()));

				this.schedule(() -> this.establish(false));
			}
			else {
				this.getLogger().warning(String.format("Could not get server uuid from %s file. Using alternative name instead. (%s)", DockerInfo.FILE, e.getMessage()));
				
				this.launch(null, this.getAlternativeName());
			}
		}
	}
	
	private void launch(DockerInfo docker, String hostname) {
		boolean dock = docker != null;
		this.getLogger().info(String.format("Launching monitoring. Picked server hostname: %s docker: %b", hostname, dock));
		
		try {
			this.server = new Zabbix(hostname);
			this.load(docker);
			
			this.thread.scheduleWithFixedDelay(() -> {
				try {
					if (this.counter.hasDockerInfo()) this.counter.getDockerInfo().reloadData();
					
					List<String> data = new ArrayList<>();
					this.counter.getInformations().forEach(a -> {
						try {
							data.add(a.getKey());
							data.add(a.supply());
						}
						catch (Exception e) {
							this.getLogger().warning(String.format("Collecting of data failed for %s key! (%s)", a.getKey(), e.getMessage()));
						}
					});
					
					this.server.send(data.toArray(String[]::new));
				}
				catch (Exception e) {
					this.getLogger().warning(String.format("Collecting of data failed globally! (%s)", e.getMessage()));
				}
			}, !dock ? 1 : docker.getFileLastModified().toSeconds() >= 29 && docker.getFileLastModified().toSeconds() <= 30 ? 3 : 0, 30, TimeUnit.SECONDS);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public abstract Logger getLogger();
	public abstract String getAlternativeName();
	public abstract File getRootFile();
	public abstract void load(DockerInfo docker);
	public abstract void unload();
	public abstract void schedule(Runnable r);
}
