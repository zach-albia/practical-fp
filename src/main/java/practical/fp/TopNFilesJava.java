package practical.fp;

import java.io.File;
import java.util.*;

class TopNFilesJava {

    static Iterable<File> topN(File path, int n) {
        if (path.exists()) {
            if (path.isFile()) {
                return Collections.singleton(path);
            } else if (path.isDirectory()) { // if directory
                PriorityQueue<File> topNFiles =
                        new PriorityQueue<>(n + 1,
                                Comparator.comparingLong(File::length));
                doTopN(path, n, topNFiles);
                return topNFiles;
            } else return Collections.emptyList();
        } else return Collections.emptyList();
    }

    private static void doTopN(File path, int n, PriorityQueue<File> topNFiles) {
        File[] files = path.listFiles();
        if (null != files) {
            for (File fileInPath : files) {
                if (fileInPath.isFile()) {
                    topNFiles.add(fileInPath);
                    if (topNFiles.size() > n) topNFiles.remove();
                } else if (fileInPath.isDirectory()) {
                    doTopN(fileInPath, n, topNFiles);
                }
            }
        }
    }
}
