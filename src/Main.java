import lib.twio.ITwioWriter;
import lib.twio.TwioFileWriter;
import lib.twio.TwioReader;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class Main {
    public static void main(String[] args) throws FileNotFoundException {
        TwioReader reader = new TwioReader(System.in);
        ITwioWriter writer = new TwioFileWriter(new FileOutputStream("output.txt"));

        while (reader.hasNext()) {
            writer.write(reader.read());
            if (reader.getLineNo() == 3) {
                System.out.println("Yes");
            }
        }
    }
}