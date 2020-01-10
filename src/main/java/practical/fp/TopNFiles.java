package practical.fp;

import java.io.File;
import java.util.*;
import java.util.concurrent.*;

class TopNFiles {

    private static Comparator<File> compareLengthDescending =
            Comparator.comparingLong(File::length);

    static Iterable<File> singleThreadedTopN(File path, int n) {
        if (path.exists()) {
            if (path.isFile()) {
                return Collections.singleton(path);
            } else { // if directory
                PriorityQueue<File> topNFiles =
                        new PriorityQueue<>(n + 1, compareLengthDescending);
                singleThreadedDoTopN(path, n, topNFiles);
                return topNFiles;
            }
        } else {
            throw new IllegalArgumentException("Invalid path provided");
        }
    }

    private static void singleThreadedDoTopN(File path, int n, PriorityQueue<File> topNFiles) {
        for (File fileInPath : path.listFiles()) {
            if (fileInPath.isFile()) {
                topNFiles.add(fileInPath);
                if (topNFiles.size() > n) topNFiles.remove();
            } else {
                singleThreadedDoTopN(fileInPath, n, topNFiles);
            }
        }
    }

    static Iterable<File> multiThreadedTopN(File root, int n) {
        if (root.exists()) {
            if (root.isFile()) {
                return Collections.singleton(root);
            } else { // if directory
                ExecutorService es = Executors.newWorkStealingPool();
                List<CompletableFuture<Void>> futures = new ArrayList<>();
                PriorityBlockingQueue<File> topNFiles = new PriorityBlockingQueue<>(
                        n + 1,
                        compareLengthDescending
                );
                multiThreadedDoTopN(root, topNFiles, futures, es);
                CompletableFuture[] cfs = futures.toArray(new CompletableFuture[futures.size()]);
                try {
                    CompletableFuture.allOf(cfs).get();
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
                while (topNFiles.size() > n)
                    topNFiles.remove();
                return topNFiles;
            }
        } else {
            throw new IllegalArgumentException("Invalid path provided");
        }
    }

    private static void multiThreadedDoTopN(
            File path,
            PriorityBlockingQueue<File> topNFiles,
            List<CompletableFuture<Void>> futures,
            ExecutorService es) {
        for (File subPath : path.listFiles()) {
            if (subPath.isFile()) {
                topNFiles.add(subPath);
            } else {
                futures.add(CompletableFuture.runAsync(
                        () -> multiThreadedDoTopN(subPath, topNFiles, futures, es), es)
                );
            }
        }
    }
}
