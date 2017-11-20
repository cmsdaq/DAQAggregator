package rcms.utilities.daqaggregator;

public enum Settings {

	RUN_MODE("run.mode", true),
	RUN_SAMPLING("run.sampling.period"),

	// flashlists
	LAS_URL("flashlist.urls",true),
	FLASHLIST_OPTIONAL("flashlist.optional"),
	STATIC_CATALOG("catalog.static"),
	SUPPRESS_HTTP_FAILED("flashlist.suppress.failed"),


	// settings concerning session definition
	SESSION_L0FILTER1("session.l0filter1"),
	SESSION_L0FILTER2("session.l0filter2"),


	// F3 urls
	F3_ENABLED("f3.enabled", true),
	F3_HLT_URL("f3.htl.url"),
	F3_DISK_URL("f3.disk.url"),
	F3_CRASHES_URL("f3.crashes.url"),
	F3_CPU_LOAD_URL("f3.cpuload.url"),
	F3_CPU_LOAD_TYPE("f3.cpuload.type"),

	// Icinga monitoring
	ICINGA_SM_OCCUPANCY_URL("icinga.sm.occupancy.url"),
	ICINGA_USER("icinga.user"),
	ICINGA_PASSWORD("icinga.password"),
	
	// custom SSL/TLS keystore file
	SSL_TRUSTSTORE("ssl.trustStore"),
	
	// settings for monitoring
	MONITOR_SETUPNAME("monitor.setupName"),

	// settings concerning HWCFG DB
	HWCFGDB_DBURL("hwcfgdb.dburl"),
	HWCFGDB_HOST("hwcfgdb.host"),
	HWCFGDB_PORT("hwcfgdb.port"),
	HWCFGDB_SID("hwcfgdb.sid"),
	HWCFGDB_LOGIN("hwcfgdb.login"),
	HWCFGDB_PWD("hwcfgdb.pwd"),

	// settings concerning SOCKS proxy
	PROXY_ENABLE("socksproxy.enableproxy"),
	PROXY_HOST("socksproy.host"),
	PROXY_PORT("socksproxy.port"),

	// settings concerning persistence
	PERSISTENCE_MODE("persistence.mode", true),
	PERSISTENCE_FLASHLIST_DIR("persistence.flashlist.dir"),
	PERSISTENCE_SNAPSHOT_DIR("persistence.snapshot.dir"),
	PERSISTENCE_FLASHLIST_FORMAT("persistence.flashlist.format"),
	PERSISTENCE_SNAPSHOT_FORMAT("persistence.snapshot.format"),
	PERSISTENCE_LIMIT("persistence.flashlist.explore.start"), ;

	private Settings(String key, boolean required) {
		this.key = key;
		this.required = required;
	}

	private Settings(String key) {
		this(key, false);
	}

	public String getKey() {
		return key;
	}

	public boolean isRequired() {
		return required;
	}

	private final String key;
	private final boolean required;
}
