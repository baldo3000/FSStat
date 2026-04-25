package me.baldo3000.async;

import io.vertx.core.Vertx;
import me.baldo3000.async.impl.AsyncFSStatImpl;

import java.nio.file.Paths;

public class Async {
    static void main() {
        IO.println("Hello World!");
        var vertx = Vertx.vertx();
        var fSStat = new AsyncFSStatImpl(vertx);
        var testPath = Paths.get("C:/Users/andre/Documents/PROGRAMMAZIONE/Tesi");
        var start = System.currentTimeMillis();

        fSStat.getFSReport(testPath, 100_000L, 10).onSuccess(r -> {
            IO.println("Directory: " + r.getDirectory());
            IO.println("Total files found: " + r.getTotalFiles());
            IO.println("Files distribution: " + r.getFilesDistribution());
            System.out.println("Total time taken: " + (System.currentTimeMillis() - start) / 1000.0 + "s");
        }).onFailure(System.err::print).onComplete(_ -> vertx.close());

//        Future.all(
//                fSStat.getFSReport(testPath).onSuccess(f -> IO.println("Files found: " + f)),
//                fSStat.getFSReport(testPath).onSuccess(f -> IO.println("Files found: " + f))
//        ).onSuccess(_ ->
//                System.out.println("Total time taken: " + (System.currentTimeMillis() - start) / 1000.0 + "s")
//        ).onFailure(System.err::print).onComplete(_ -> vertx.close());
    }
}
