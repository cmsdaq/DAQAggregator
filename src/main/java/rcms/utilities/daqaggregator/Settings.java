package rcms.utilities.daqaggregator;

public enum Settings {

	RUN_MODE("run.mode"),

	// settings concerning session definition
	SESSION_LASURL_GE("session.lasURLgeneral"),
	SESSION_L0FILTER1("session.l0filter1"),
	SESSION_L0FILTER2("session.l0filter2"),

	// settings for monitoring
	MONITOR_SETUPNAME("monitor.setupName"),
	MONITOR_URLS("monitor.lasURLs"),

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
	PERSISTENCE_FLASHLIST_DIR("persistence.flashlist.dir"),
	PERSISTENCE_SNAPSHOT_DIR("persistence.snapshot.dir"),
	PERSISTENCE_FLASHLIST_FORMAT("persistence.flashlist.format"),
	PERSISTENCE_SNAPSHOT_FORMAT("persistence.snapshot.format"),
	PERSISTENCE_MODE("persistence.mode"),
	PERSISTENCE_LIMIT("persistence.flashlist.explore.start");

	private Settings(String key) {
		this.key = key;
	}

	public String getKey() {
		return key;
	}

	private final String key;
}
