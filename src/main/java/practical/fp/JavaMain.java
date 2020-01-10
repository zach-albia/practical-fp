package practical.fp;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.function.BiFunction;

public class JavaMain {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter a file path: ");
        String input = sc.nextLine();
        System.out.print("Enter number of files: ");
        int n = sc.nextInt();
        List<BiFunction<File, Integer, Iterable<File>>> topNs =
                new ArrayList<>();
        topNs.add(TopNFiles::singleThreadedTopN);
        topNs.add(TopNFiles::multiThreadedTopN);
        runTopN(input, n, topNs);
        sc.close();
    }

    private static void runTopN(
            String input,
            int n,
            List<BiFunction<File, Integer, Iterable<File>>> topNs
    ) {
        for (BiFunction<File, Integer, Iterable<File>> topN : topNs) {
            for (File file : topN.apply(new File(input), n)) {
                System.out.println(file.getAbsolutePath());
            }
            System.out.println("\n");
        }
    }
}
