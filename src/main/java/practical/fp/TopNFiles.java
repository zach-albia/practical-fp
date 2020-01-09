package practical.fp;

import java.io.File;
import java.util.*;

class TopNFiles {

    private static Comparator<File> compareLengthDescending =
            Comparator.comparingLong(File::length);

    static Iterable<File> singleThreadedTopN(File path, int n) {
        if (path.exists()) {
            if (path.isFile()) {
                return Collections.singleton(path);
            } else { // if directory
                PriorityQueue<File> topNFiles = new PriorityQueue<>(n + 1, compareLengthDescending);
                singleThreadedDoTopN(path, n, topNFiles);
                return topNFiles;
            }
        } else {
            return Collections.emptyList();
        }
    }

    private static void singleThreadedDoTopN(File path, int n, PriorityQueue<File> topNFiles) {
        for (File path_ : path.listFiles()) {
            if (path_.isFile()) {
                topNFiles.add(path_);
                if (topNFiles.size() > n) topNFiles.remove();
            } else {
                singleThreadedDoTopN(path_, n, topNFiles);
            }
        }
    }
}
