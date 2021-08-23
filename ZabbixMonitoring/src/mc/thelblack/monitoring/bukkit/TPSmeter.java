package mc.thelblack.monitoring.bukkit;

import org.bukkit.Bukkit;

public class TPSmeter {

	private static final int SECONDS_SAVED = 64;

	private int secCount = 0;
	private long[] ticks = new long[TPSmeter.SECONDS_SAVED];

	public TPSmeter() {
		Bukkit.getScheduler().runTaskTimer(MainBukkit.INSTANCE, () -> {
			this.ticks[(this.secCount++ & (TPSmeter.SECONDS_SAVED - 1))] = System.currentTimeMillis();
		}, 0L, 20L);
	}

	public double getTPS(int secs) {
		if (this.secCount < secs) return 20d;

		int target = (this.secCount - 1 - secs) & (TPSmeter.SECONDS_SAVED - 1);
		long elapsed = System.currentTimeMillis() - this.ticks[target];

		return (secs * 20) / (elapsed / 1000d);
	}
}
