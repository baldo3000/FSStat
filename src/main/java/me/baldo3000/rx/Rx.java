package me.baldo3000.rx;

import me.baldo3000.rx.impl.RxFSStatImpl;

import java.nio.file.Paths;

public class Rx {
    static void main() {
        IO.println("Hello World!");
        var testPath = Paths.get("C:/Users/andre/Documents/PROGRAMMAZIONE");
        var fSStat = new RxFSStatImpl();
        var start = System.currentTimeMillis();
        fSStat.getFSReport(testPath, 100_000L, 10)
                //.takeLast(1)
                .blockingSubscribe(report -> IO.println("Report being generated outside: " + report));
        IO.println("Total time taken: " + (System.currentTimeMillis() - start) / 1000.0 + "s");
    }
}
