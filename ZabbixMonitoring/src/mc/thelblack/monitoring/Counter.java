package mc.thelblack.monitoring;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Supplier;

public class Counter {

	private final List<Information> informations = new CopyOnWriteArrayList<>();
	
	private final DockerInfo dockerstats;
	private final Runtime runtime = Runtime.getRuntime();
	
	protected Counter(DockerInfo docker) {
    	this.dockerstats = docker;
    	
    	if (this.hasDockerInfo()) {
        	new Information("jbwm.docker.cpu", () -> this.dockerstats.getLastData().getCPU());
        	new Information("jbwm.docker.ramused", () -> this.dockerstats.getLastData().getRamUsed());
        	new Information("jbwm.docker.ramlimit", () -> this.dockerstats.getLastData().getRamLimit());
        	new Information("jbwm.docker.ramusage", () -> this.dockerstats.getLastData().getRamUsage());
    	}

    	new Information("jbwm.jvm.freememory", () -> String.valueOf(this.runtime.freeMemory()));
    	new Information("jbwm.jvm.maxmemory", () -> String.valueOf(this.runtime.maxMemory()));
    	new Information("jbwm.jvm.totalmemory", () -> String.valueOf(this.runtime.totalMemory()));
    }
    
    public List<Information> getInformations() {
    	return this.informations;
    }
    
    public DockerInfo getDockerInfo() {
    	return this.dockerstats;
    }
    
    public boolean hasDockerInfo() {
    	return this.dockerstats != null;
    }
    
    public class Information {

    	private String key;
    	private Supplier<String> value;
    	
    	public Information(String key, Supplier<String> value) {
    		this.key = String.format("%s", key);
    		this.value = value;
    		
    		Counter.this.informations.add(this);
    	}

		public String getKey() {
    		return this.key;
    	}
    	
    	public String supply() {
    		return this.value.get();
    	}
    }
}
