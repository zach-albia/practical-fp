package practical.fp;

import java.io.File;

public interface TopN {

    Iterable<File> findTopNFiles(File root, int n);
}
