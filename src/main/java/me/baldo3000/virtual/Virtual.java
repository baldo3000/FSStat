package me.baldo3000.virtual;

import me.baldo3000.virtual.impl.VirtualFSStatImpl;

import java.nio.file.Paths;
import java.util.concurrent.ExecutionException;

public class Virtual {
    static void main() throws ExecutionException, InterruptedException {
        IO.println("Hello World!");
        var testPath2 = Paths.get("C:/Users/andre/AppData");
        var fSStat = new VirtualFSStatImpl();
        var start = System.currentTimeMillis();

        var report = fSStat.getFSReport(testPath2, 100_000L, 10).get();
        System.out.println("Total time taken: " + (System.currentTimeMillis() - start) / 1000.0 + "s");
        IO.println(report);
    }
}
