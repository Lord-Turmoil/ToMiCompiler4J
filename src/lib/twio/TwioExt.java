package lib.twio;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class TwioExt {
    private TwioExt() {}

    public static ITwioReader buildReader(String input) {
        if (input == null || input.equals("null")) {
            throw new IllegalArgumentException("input cannot be null");
        }

        if (input.equals("stdin")) {
            return new TwioReader(System.in);
        }

        try {
            return new TwioReader(new FileInputStream(input));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static ITwioWriter buildWriter(String output) {
        if (output == null || output.equals("null")) {
            return new TwioBufferWriter();
        }

        if (output.equals("stdout")) {
            return new TwioFileWriter(System.out);
        } else if (output.equals("stderr")) {
            return new TwioFileWriter(System.err);
        }

        try {
            return new TwioFileWriter(new FileOutputStream(output));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
