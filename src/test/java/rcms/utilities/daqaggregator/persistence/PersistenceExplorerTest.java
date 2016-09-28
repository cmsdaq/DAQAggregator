package rcms.utilities.daqaggregator.persistence;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

public class PersistenceExplorerTest {

	private static FileSystemConnectorFake fileSystemConnector;
	private PersistenceExplorer persistenceExplorer;

	private static final Logger logger = Logger.getLogger(PersistenceExplorer.class);

	@BeforeClass
	public static void initFileSystem() {
		fileSystemConnector = new FileSystemConnectorFake();
	}

	@Before
	public void init() {
		persistenceExplorer = new PersistenceExplorer(fileSystemConnector);
	}

	@Test
	public void timeSpanExploreTest() throws IOException {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, 2016);
		cal.set(Calendar.MONTH, 1 - 1);
		cal.set(Calendar.DAY_OF_MONTH, 3);
		cal.set(Calendar.HOUR_OF_DAY, 5);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		long start = cal.getTimeInMillis();
		cal.set(Calendar.MINUTE, 20);
		long end = cal.getTimeInMillis();
		Pair<Long, List<File>> result = persistenceExplorer.explore(start, end, "/base");
		Assert.assertEquals(399, result.getRight().size());
		Assert.assertEquals(1451794797000L, (long) result.getLeft());

		Iterator<File> iterator = result.getRight().iterator();
		Assert.assertEquals("1451793603000.smile", iterator.next().getName());
		Assert.assertEquals("1451793606000.smile", iterator.next().getName());
		Assert.assertEquals("1451793609000.smile", iterator.next().getName());
	}

	/**
	 * There is a problem: undeterministic last timestamp
	 * 
	 * @throws IOException
	 */
	@Test
	public void newerThanExploreTest() throws IOException {

		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, 2016);
		cal.set(Calendar.MONTH, 1 - 1);
		cal.set(Calendar.DAY_OF_MONTH, 1);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		long start = cal.getTimeInMillis();

		Pair<Long, List<File>> a = persistenceExplorer.explore(start, "/base");
		Assert.assertEquals(2000, a.getRight().size());
		Assert.assertEquals("1451602803000.smile", a.getRight().iterator().next().getName());
		Assert.assertEquals(1451608800000L, (long) a.getLeft());
	}

	@Test
	public void interactionsToFileSystemTest() throws IOException {

		FileSystemConnector fileSystemConnectorSpy = Mockito.spy(fileSystemConnector);
		persistenceExplorer = new PersistenceExplorer(fileSystemConnectorSpy);
		persistenceExplorer.explore(1451728800000L, 1451728806000L, "/base");
		Mockito.verify(fileSystemConnectorSpy, Mockito.times(1)).getFiles(Mockito.anyString());
	}

	@Test
	public void testEnterDirectory() {

		// target period inside so should enter
		Assert.assertTrue(persistenceExplorer.enterDirectory(100, 200, 100, 200));
		Assert.assertTrue(persistenceExplorer.enterDirectory(100, 200, 101, 199));

		// target period too early to enter
		Assert.assertFalse(persistenceExplorer.enterDirectory(100, 200, 1, 2));
		Assert.assertFalse(persistenceExplorer.enterDirectory(100, 200, 99, 100));

		// target period too late to enter
		Assert.assertFalse(persistenceExplorer.enterDirectory(100, 200, 201, 202));
		Assert.assertFalse(persistenceExplorer.enterDirectory(100, 200, 200, 201));

		// target period intersects so enter
		Assert.assertTrue(persistenceExplorer.enterDirectory(100, 200, 1, 9999));
		Assert.assertTrue(persistenceExplorer.enterDirectory(100, 200, 100, 9999));
		Assert.assertTrue(persistenceExplorer.enterDirectory(100, 200, 150, 9999));

	}

	@Test
	public void fakeFileSystemConnectorTest() throws IOException {

		Assert.assertEquals(1, fileSystemConnector.getDirs("/base").size());
		Assert.assertEquals(6, fileSystemConnector.getDirs("/base/2016").size());
		Assert.assertEquals(31, fileSystemConnector.getDirs("/base/2016/1").size());
		Assert.assertEquals(31, fileSystemConnector.getDirs("/base/2016/2").size());
		Assert.assertEquals(24, fileSystemConnector.getDirs("/base/2016/1/22").size());
		Assert.assertEquals(24, fileSystemConnector.getDirs("/base/2016/1/1").size());
		Assert.assertEquals(24, fileSystemConnector.getDirs("/base/2016/1/21").size());
		Assert.assertEquals(24, fileSystemConnector.getDirs("/base/2016/1/12").size());
		Assert.assertEquals(24, fileSystemConnector.getDirs("/base/2016/1/31").size());
		Assert.assertTrue(1000 < fileSystemConnector.getFiles("/base/2016/2/22/21").size());
		Assert.assertTrue(1000 < fileSystemConnector.getFiles("/base/2016/1/12/7").size());
		Assert.assertTrue(1000 < fileSystemConnector.getFiles("/base/2016/1/25/13").size());
		Assert.assertTrue(1000 < fileSystemConnector.getFiles("/base/2016/2/2/2").size());
		Assert.assertTrue(1000 < fileSystemConnector.getFiles("/base/2016/1/22/24").size());

		Assert.assertEquals(1, fileSystemConnector.fakeFiles.size());
		Assert.assertEquals("1451602800000",
				fileSystemConnector.fakeFiles.get("base").get("2016").get("1").get("1").get("1").get(0));
		Assert.assertEquals("1451602803000",
				fileSystemConnector.fakeFiles.get("base").get("2016").get("1").get("1").get("1").get(1));

	}

}
