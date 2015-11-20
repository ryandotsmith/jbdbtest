import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.Counter;
import com.codahale.metrics.Timer;
import com.sleepycat.bind.tuple.StringBinding;
import com.sleepycat.je.*;
import com.codahale.metrics.MetricRegistry;

import java.io.File;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class Main {
    static final Integer rounds = 10000000;

    static final MetricRegistry metrics = new MetricRegistry();
    static final Timer writeTimer = metrics.timer("write-time");
    static final Timer readTimer = metrics.timer("read-time");
    static final Counter failureCount = metrics.counter("failures");

    public static void main(String[] args) {
        ConsoleReporter reporter = ConsoleReporter.forRegistry(metrics)
                .convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.MICROSECONDS)
                .build();

        Database db = setupDatabase();

        DatabaseEntry k = new DatabaseEntry();
        Random rng = new Random();
        for (Integer i = 0; i < rounds; i++) {
            Timer.Context context = writeTimer.time();

            UTXO utxo = new UTXO(String.format("t%d", i), 0, rng.nextInt(10000));
            StringBinding.stringToEntry(utxo.getTxid(), k);
            db.put(null, k, utxo.objectToEntry());

            context.stop();
        }

        DatabaseEntry v = new DatabaseEntry();
        for(Integer i = 0; i < rounds; i++) {
            Timer.Context context = readTimer.time();

            UTXO utxo = new UTXO();
            StringBinding.stringToEntry(String.format("t%d", i), k);
            if (db.get(null, k, v, null) == OperationStatus.SUCCESS) {
                utxo.entryToObject(v);
                if (i % 1000 == 0) {
                    //System.out.println(utxo.toString());
                }
            } else {
                failureCount.inc();
            }

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