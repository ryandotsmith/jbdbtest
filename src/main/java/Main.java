import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.Timer;
import com.sleepycat.bind.tuple.StringBinding;
import com.sleepycat.je.*;
import com.codahale.metrics.MetricRegistry;

import java.io.File;
import java.util.concurrent.TimeUnit;

public class Main {
    static final MetricRegistry metrics = new MetricRegistry();
    static final Timer writeTimer = metrics.timer("write-time");

    public static void main(String[] args) {
        ConsoleReporter reporter = ConsoleReporter.forRegistry(metrics)
                .convertRatesTo(TimeUnit.MILLISECONDS)
                .convertDurationsTo(TimeUnit.MICROSECONDS)
                .build();

        Database db = setupDatabase();

        DatabaseEntry k = new DatabaseEntry();
        for (Integer i = 0; i < 10000; i++) {
            Timer.Context context = writeTimer.time();

            UTXO utxo = new UTXO(String.format("t%d", i), 0, 1000);
            StringBinding.stringToEntry(utxo.getTxid(), k);
            db.put(null, k, utxo.objectToEntry());

            context.stop();
        }

        teardownDatabase(db);
        reporter.report();
    }

    public static Database setupDatabase() {
        EnvironmentConfig environmentConfig = new EnvironmentConfig();
        environmentConfig.setTransactional(true);
        environmentConfig.setAllowCreate(true);
        Environment dbenv = new Environment(new File("/tmp"), environmentConfig);

        DatabaseConfig databaseConfig = new DatabaseConfig();
        databaseConfig.setAllowCreate(true);

        return dbenv.openDatabase(null, "testdb", databaseConfig);
    }

    public static void teardownDatabase(Database db) {
        db.close();
        db.getEnvironment().close();
    }
}