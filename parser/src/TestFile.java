import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap ;

import parser.example_dir.ExampleExternalFile;
import parser.example_dir.more_depth.MoreDepthFile;

import sun.misc.Signal;
import sun.misc.SignalHandler;

public class TestFile {

    private static void t(){ 
        int x = 1+1;
        x +=2;
        Boolean d = false;
        for (int i = 0; i < 10; i++) {
            ++x;
        }
    }
}
