package mc.thelblack.monitoring;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.github.hengyunabc.zabbix.sender.DataObject;
import io.github.hengyunabc.zabbix.sender.ZabbixSender;

public class Zabbix extends ZabbixSender {

	private static final String Z_IP = "xxx";
	private static final int Z_PORT = 10051;

	private String hostname;
	
	public Zabbix(String hostname) throws IOException {
		super(Zabbix.Z_IP, Zabbix.Z_PORT);
		this.hostname = hostname;
		
		this.testConnection();
	}
	
	public void send(String... keyvalue) throws IOException {
		if (keyvalue.length == 0 || keyvalue.length % 2 != 0) throw new IllegalArgumentException("Bad argument for zabbix key-value pair.");

		List<DataObject> data = new ArrayList<>();
		for (int i = 0; i < keyvalue.length; i+=2) {
			DataObject d = new DataObject();

			d.setHost(this.hostname);
			d.setClock(System.currentTimeMillis()/1000);
			d.setKey(keyvalue[i]);
			d.setValue(keyvalue[i+1]);
			
			data.add(d);
		}

		super.send(data);
	}
	
	private void testConnection() throws IOException {
		this.send("testkey", "testvalue");
	}
}
