import lib.twio.IWriter;
import lib.twio.TwioFileWriter;
import lib.twio.TwioReader;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class Main {
    public static void main(String[] args) throws FileNotFoundException {
        TwioReader reader = new TwioReader(System.in);
        IWriter writer = new TwioFileWriter(new FileOutputStream("output.txt"));

        while (reader.hasNext()) {
            writer.write(reader.read());
            if (reader.getLineNo() == 3) {
                System.out.println("Yes");
            }
        }
    }
}