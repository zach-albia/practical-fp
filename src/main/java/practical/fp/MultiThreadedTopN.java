package practical.fp;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.RecursiveTask;

public class MultiThreadedTopN extends RecursiveTask<Integer> implements TopN {

    private File root;
    private int n;
    private PriorityBlockingQueue<File> topNFiles;

    public MultiThreadedTopN() {
    }

    private MultiThreadedTopN(
            File root,
            int n,
            PriorityBlockingQueue<File> topNFiles
    ) {
        this.root = root;
        this.n = n;
        this.topNFiles = topNFiles;
    }

    @Override
    public Iterable<File> findTopNFiles(File root, int n) {
        this.root = root;
        this.n = n;
        compute();
        ArrayList<File> copy = new ArrayList<>(topNFiles.size());
        topNFiles.drainTo(copy);
        topNFiles.clear();
        return copy;
    }

    @Override
    protected Integer compute() {
        if (root.exists()) {
            if (root.isFile()) {
                topNFiles.add(root);
            } else if (root.isDirectory()) {
                File[] files = root.listFiles();
                for (File subPath: files) {
                    if (subPath.isFile()) {
                        topNFiles.add(subPath);
                    } else {
                        MultiThreadedTopN topN = new MultiThreadedTopN(subPath, n, topNFiles);
                        topN.fork().join();
                    }
                }
            }
        }
        return 0; // bogus
    }
}
