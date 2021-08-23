package mc.thelblack.monitoring.bungee;

import java.util.concurrent.atomic.AtomicInteger;

import mc.thelblack.monitoring.Counter;
import mc.thelblack.monitoring.DockerInfo;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.ProxyPingEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class CounterBungee extends Counter implements Listener {

	private final AtomicInteger serverPings = new AtomicInteger(0);
	
    private final AtomicInteger playerJoined = new AtomicInteger(ProxyServer.getInstance().getOnlineCount());
    private final AtomicInteger playerQuitted = new AtomicInteger(0);
    private final AtomicInteger playerOnline = new AtomicInteger(0);
	
	public CounterBungee(DockerInfo docker) {
		super(docker);
	
		new Information("jbwm.mc.playerping", () -> this.serverPings.toString());
		
		new Information("jbwm.mc.playerjoin", () -> this.playerJoined.toString());
		new Information("jbwm.mc.playerquit", () -> this.playerQuitted.toString());
		new Information("jbwm.mc.playeronline", () -> this.playerOnline.toString());
	}
	
	@EventHandler
	public void ping(ProxyPingEvent e) {
		this.serverPings.incrementAndGet();
	}
	
	@EventHandler
	public void join(PostLoginEvent e) {
		this.playerJoined.incrementAndGet();
		this.playerOnline.set(ProxyServer.getInstance().getOnlineCount());
	}
	
	@EventHandler
	public void quit(PlayerDisconnectEvent e) {
		this.playerQuitted.incrementAndGet();
		this.playerOnline.set(ProxyServer.getInstance().getOnlineCount());
	}
}
