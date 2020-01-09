package practical.fp;

import java.io.File;
import java.util.*;

public class TopN {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter a file path: ");
        String input = sc.nextLine();
        System.out.print("Enter number of files: ");
        int n = sc.nextInt();
        Iterable<File> files = topN(new File(input), n);
        for (File file : files) {
            System.out.println(file.getAbsolutePath());
        }
        sc.close();
    }

    private static Comparator<File> compareLengthDescending =
            Comparator.comparingLong(File::length);

    private static Iterable<File> topN(File path, int n) {
        if (path.exists()) {
            if (path.isFile()) {
                return Collections.singleton(path);
            } else { // if directory
                PriorityQueue<File> topNFiles = new PriorityQueue<>(n + 1, compareLengthDescending);
                doTopN(path, n, topNFiles);
                return topNFiles;
            }
        } else {
            return Collections.emptyList();
        }
    }

    private static void doTopN(File path, int n, PriorityQueue<File> topNFiles) {
        File[] files = path.listFiles();
        for (File path_ : files) {
            if (path_.isFile()) {
                topNFiles.add(path_);
                if (topNFiles.size() > n) topNFiles.remove();
            } else {
                doTopN(path_, n, topNFiles);
            }
        }
    }
}
