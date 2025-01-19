import java.util.ArrayList;
import java.util.HashMap ;

import parser.example_dir.ExampleExternalFile;
import parser.example_dir.more_depth.MoreDepthFile;

public class TestFile {

    @SuppressWarnings("unused")
    private static void t(){
        //int sum = 0;
        //Integer d = 15;
        TestObject obj = new TestObject();
        obj.yes();
        TestObject2 obj2 = new TestObject2();
        obj2.yes();
        ExampleExternalFile f = new ExampleExternalFile();
        f.negative();
        //MoreDepthFile mdf = new MoreDepthFile();
    }
    
}
