package practical.fp;

import java.io.File;
import java.util.Collections;
import java.util.Comparator;
import java.util.PriorityQueue;

public class SingleThreadedTopN implements TopN {

    @Override
    public Iterable<File> findTopNFiles(File root, int n) {
        if (root.exists()) {
            if (root.isFile()) {
                return Collections.singleton(root);
            } else { // if directory
                PriorityQueue<File> topNFiles =
                        new PriorityQueue<>(n + 1, Comparator.comparingLong(File::length));
                singleThreadedDoTopN(root, n, topNFiles);
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
}
