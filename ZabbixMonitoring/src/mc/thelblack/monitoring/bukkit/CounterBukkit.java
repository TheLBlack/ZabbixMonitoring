package mc.thelblack.monitoring.bukkit;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkPopulateEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

import mc.thelblack.monitoring.Counter;
import mc.thelblack.monitoring.DockerInfo;

public class CounterBukkit extends Counter implements Listener {

	private static final int WAIT_FOR_BUKKIT_REPONSE_SEC = 1;
	
	private final TPSmeter tps = new TPSmeter();
	
    private final AtomicInteger chunkLoad = new AtomicInteger(Bukkit.getWorlds().stream().mapToInt(a -> a.getLoadedChunks().length).sum());
    private final AtomicInteger chunkUnload = new AtomicInteger(0);
    private final AtomicInteger chunkGenerate = new AtomicInteger(0);
    
    private final AtomicInteger playerJoined = new AtomicInteger(Bukkit.getOnlinePlayers().size());
    private final AtomicInteger playerQuitted = new AtomicInteger(0);
    private final AtomicInteger playerFirstjoints = new AtomicInteger(0);
    private final AtomicInteger playerOnline = new AtomicInteger(0);
	
	public CounterBukkit(DockerInfo docker) {
		super(docker);
		
    	new Information("jbwm.mc.chunkload", () -> this.chunkLoad.toString());
    	new Information("jbwm.mc.chunkunload", () -> this.chunkUnload.toString());
    	new Information("jbwm.mc.chunkgenerate", () -> this.chunkGenerate.toString());
    	
    	new Information("jbwm.mc.playerjoin", () -> this.playerJoined.toString());
    	new Information("jbwm.mc.playerquit", () -> this.playerQuitted.toString());
    	new Information("jbwm.mc.playerfirst", () -> this.playerFirstjoints.toString());
    	new Information("jbwm.mc.playeronline", () -> this.playerOnline.toString());
    	
    	new Information("jbwm.mc.entitytotal", () -> {
    		FutureTask<Integer> task = new FutureTask<Integer>(() -> {
    			return Bukkit.getWorlds().stream().mapToInt(a -> a.getEntities().size()).sum();
    		});
    		Bukkit.getScheduler().runTask(MainBukkit.INSTANCE, task);

    		try {
    			return String.valueOf(task.get(CounterBukkit.WAIT_FOR_BUKKIT_REPONSE_SEC, TimeUnit.SECONDS));
			} catch (InterruptedException | ExecutionException | TimeoutException e) {
				throw new RuntimeException(e.getMessage());
			}
    	});
    	
    	new Information("jbwm.mc.tps[15]", () -> {
    		FutureTask<Double> task = new FutureTask<Double>(() -> {
    			return this.tps.getTPS(15);
    		});
    		Bukkit.getScheduler().runTask(MainBukkit.INSTANCE, task);

    		try {
    			return BigDecimal.valueOf(task.get(CounterBukkit.WAIT_FOR_BUKKIT_REPONSE_SEC, TimeUnit.SECONDS)).setScale(1, RoundingMode.UP).toPlainString();
			} catch (InterruptedException | ExecutionException | TimeoutException e) {
				throw new RuntimeException(e.getMessage());
			}
    	});
    	
    	new Information("jbwm.mc.tps[30]", () -> {
    		FutureTask<Double> task = new FutureTask<Double>(() -> {
    			return this.tps.getTPS(30);
    		});
    		Bukkit.getScheduler().runTask(MainBukkit.INSTANCE, task);

    		try {
    			return BigDecimal.valueOf(task.get(CounterBukkit.WAIT_FOR_BUKKIT_REPONSE_SEC, TimeUnit.SECONDS)).setScale(1, RoundingMode.UP).toPlainString();
			} catch (InterruptedException | ExecutionException | TimeoutException e) {
				throw new RuntimeException(e.getMessage());
			}
    	});
    	
    	new Information("jbwm.mc.tps[60]", () -> {
    		FutureTask<Double> task = new FutureTask<Double>(() -> {
    			return this.tps.getTPS(60);
    		});
    		Bukkit.getScheduler().runTask(MainBukkit.INSTANCE, task);

    		try {
    			return BigDecimal.valueOf(task.get(CounterBukkit.WAIT_FOR_BUKKIT_REPONSE_SEC, TimeUnit.SECONDS)).setScale(1, RoundingMode.UP).toPlainString();
			} catch (InterruptedException | ExecutionException | TimeoutException e) {
				throw new RuntimeException(e.getMessage());
			}
    	});
	}
	
    @EventHandler
    public void chunkLoad(ChunkLoadEvent e) {
    	this.chunkLoad.incrementAndGet();
    }
    
    @EventHandler
    public void chunkUnload(ChunkUnloadEvent e) {
    	this.chunkUnload.incrementAndGet();
    }
    
    @EventHandler
    public void chunkGenerate(ChunkPopulateEvent e) {
    	this.chunkGenerate.incrementAndGet();
    }
    
    @EventHandler
    public void playerJoin(PlayerJoinEvent e) {
    	this.playerJoined.incrementAndGet();
    	this.playerOnline.set(Bukkit.getOnlinePlayers().size());
    	if (!e.getPlayer().hasPlayedBefore()) this.playerFirstjoints.incrementAndGet();
    }
    
    @EventHandler
    public void playerQuit(PlayerQuitEvent e) {
    	this.playerQuitted.incrementAndGet();
    	this.playerOnline.set(Bukkit.getOnlinePlayers().size());
    }
}
