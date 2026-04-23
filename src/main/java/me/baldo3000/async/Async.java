package me.baldo3000.async;

import me.baldo3000.async.impl.AsyncFSStatImpl;

import java.nio.file.Paths;

public class Async {
    static void main() {
        IO.println("Hello World!");

        var fSStat = new AsyncFSStatImpl();
        var testPath = Paths.get("C:/Users/andre/Documents/PROGRAMMAZIONE/PCD");
        fSStat.getFSReport(testPath).onSuccess(files -> {
                    IO.println("Files found: " + files.size());
                    fSStat.close();
                }
        ).onFailure(System.err::print);
    }
}
