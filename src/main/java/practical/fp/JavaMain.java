package practical.fp;

import java.io.File;
import java.util.Scanner;

public class JavaMain {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter a file path: ");
        String input = sc.nextLine();
        System.out.print("Enter number of files: ");
        int n = sc.nextInt();
        for (File file : TopNFilesJava.topN(new File(input), n))
            System.out.println(file.getAbsolutePath());
        System.out.println("\n");
        sc.close();
    }
}
