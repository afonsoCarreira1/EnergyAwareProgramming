import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap ;

import parser.example_dir.ExampleExternalFile;
import parser.example_dir.more_depth.MoreDepthFile;

import sun.misc.Signal;
import sun.misc.SignalHandler;

public class TestFile {

    private static TestObject t(){ 
        //f();
        //d();
        return new TestObject();
    }

    //private static void f() {}
    //private static double d() {return 0.0;}
}
