package practical.fp;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class JavaMain {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter a file path: ");
        String input = sc.nextLine();
        System.out.print("Enter number of top files by file size: ");
        int n = sc.nextInt();
        List<TopN> topNs = new ArrayList<>();
        topNs.add(new SingleThreadedTopN());
        topNs.add(new MultiThreadedTopN());
        runTopN(input, n, topNs);
        sc.close();
    }

    private static void runTopN(
            String input,
            int n,
            List<TopN> topNs
    ) {
        for (TopN topN : topNs) {
            long start = System.currentTimeMillis();
            for (File file : topN.findTopNFiles(new File(input), n)) {
                System.out.println(file.getAbsolutePath());
            }
            long end = System.currentTimeMillis();
            System.out.println("Time: " + ((double)end - start) / 1000);
            System.out.println("\n");
        }
    }
}
