package practical.fp;

import java.io.File;
import java.util.Scanner;
import java.util.function.BiFunction;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter a file path: ");
        String input = sc.nextLine();
        System.out.print("Enter number of files: ");
        int n = sc.nextInt();
        BiFunction<File, Integer, Iterable<File>> topN = Util::topN;
        runTopN(topN, input, n);
        sc.close();
    }

    private static void runTopN(
            BiFunction<File, Integer, Iterable<File>> topN,
            String input, int n
    ) {
        for (File file : topN.apply(new File(input), n)) {
            System.out.println(file.getAbsolutePath());
        }
    }
}
