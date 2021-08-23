package mc.thelblack.monitoring;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DockerInfo {

	public static final String FILE = "containerdata";
	private static final Pattern DOCKER_PARSER = Pattern.compile("(?<uuid>[a-z0-9-]+) (?<cpu>\\d+\\.\\d+)% (?<ramused>\\d+\\.\\d+\\w+) / (?<ramlimit>\\d+\\.\\d+\\w+) (?<ramusage>\\d+\\.\\d+)", Pattern.CASE_INSENSITIVE);
	private static final Pattern UNIT_PARSER = Pattern.compile("^(?<amount>\\d+.\\d+)(?<unit>(B|KiB|MiB|GiB|TiB))");
	
	private Path file;
	private DockerInfo.ContainerData last = null;
	
	public DockerInfo(File root) {
		this.file = new File(root, DockerInfo.FILE).toPath();
	}
	
	public ContainerData reloadData() throws IOException {
		this.last = new DockerInfo.ContainerData();
		
		return this.getLastData();
	}
	
	public ContainerData getLastData() {
		return this.last;
	}
	
	public Duration getFileLastModified() throws IOException {
		return Duration.between(Files.getLastModifiedTime(DockerInfo.this.file).toInstant(), Instant.now());
	}

	public class ContainerData {
		private Matcher match;
		
		private ContainerData() throws IOException {
			if (DockerInfo.this.getFileLastModified().toMinutes() > 1) throw new IllegalArgumentException(String.format("File %s is too old.", DockerInfo.FILE));
			this.match = DockerInfo.DOCKER_PARSER.matcher(Files.lines(DockerInfo.this.file).findFirst().get());
			
			if (!this.match.find()) throw new IllegalArgumentException("Match from docker stats file not found.");
		}
		
		private String convertUnit(String number) {
			Matcher m = DockerInfo.UNIT_PARSER.matcher(number);
			
			if (m.find()) {
				double n = Double.valueOf(m.group("amount"));
				String u = m.group("unit");
				
				switch (u) {
					case "KiB": n=n*1024; break;
					case "MiB": n=n*1024*1024; break;
					case "GiB": n=n*1024*1024*1024; break;
					case "TiB": n=n*1024*1024*1024*1024; break;
				}
				
				return BigDecimal.valueOf(n).setScale(2, RoundingMode.DOWN).toPlainString();
			}
			else throw new IllegalArgumentException(String.format("Unknown unit format from %s file. (%s)", DockerInfo.FILE, number));
		}
		
		public String getUUID() {
			return this.match.group("uuid");
		}
		
		public String getCPU() {
			return this.match.group("cpu");
		}
		
		public String getRamUsed() {
			return this.convertUnit(this.match.group("ramused"));
		}
		
		public String getRamLimit() {
			return this.convertUnit(this.match.group("ramlimit"));
		}
		
		public String getRamUsage() {
			return this.match.group("ramusage");
		}
	}
}
