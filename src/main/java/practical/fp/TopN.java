package practical.fp;

import java.io.File;
import java.util.Scanner;

public class TopN {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter a file path: ");
        String input = sc.nextLine();
        System.out.print("Enter number of files: ");
        int n = sc.nextInt();
        File[] files = topN(new File(input), n);
        for (File file : files) {
            System.out.println(file.getName());
        }
        sc.close();
    }

    private static File[] topN(File file, int n) {
        return null;
    }
}
