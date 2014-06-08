package magic.data;

import javax.imageio.ImageIO;

import magic.ui.utility.GraphicsUtilities;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.Properties;
import java.util.List;
import java.nio.file.Files;

public class FileIO {

    public static final Charset UTF8 = Charset.forName("UTF-8");

    private static String toStr(final BufferedReader input) throws IOException {
        final StringBuilder contents = new StringBuilder();
        try {
            String line = null; //not declared within while loop
            /*
             * readLine is a bit quirky :
             * it returns the content of a line MINUS the newline.
             * it returns null only for the END of the stream.
             * it returns an empty String if two newlines appear in a row.
             */
            while (input != null && (line = input.readLine()) != null) {
                contents.append(line);
                contents.append(System.getProperty("line.separator"));
            }
        } finally {
            close(input);
        }
        return contents.toString();
    }

    public static String toStr(final File aFile) throws IOException {
        return toStr(new FileInputStream(aFile));
    }

    public static List<String> toStrList(final File aFile) throws IOException {
        return Files.readAllLines(aFile.toPath(), UTF8);
    }

    public static String toStr(final InputStream ins) throws IOException {
        return toStr(new BufferedReader(new InputStreamReader(ins, UTF8)));
    }

    public static Properties toProp(final File aFile) {
        Properties properties = new Properties();
        try {
            properties = toProp(new FileInputStream(aFile));
        } catch (final IOException ex) {
            System.err.println("ERROR! Unable to load from " + aFile + ", " + ex.getMessage());
        }
        return properties;
    }

    public static Properties toProp(final InputStream ins) {
        final Properties properties = new Properties();
        try {
            properties.load(new BufferedReader(new InputStreamReader(ins, "utf-8")));
        } catch (final IOException ex) {
            System.err.println("ERROR! Unable to load from input stream, " + ex.getMessage());
        } finally {
            close(ins);
        }
        return properties;
    }

    private static BufferedImage loadImage(final File input) {
        try {
            return ImageIO.read(input);
        } catch (final IOException ex) {
            System.err.println("ERROR! Unable to read from " + input);
            return null;
        } catch (final IllegalArgumentException ex) {
            System.err.println("ERROR! Unable to read from null");
            return null;
        }
    }

    public static BufferedImage toImg(final File input, final BufferedImage def) {
        final BufferedImage img = loadImage(input);
        if (img == null) {
            // no registered ImageReader able to read the file, likely file is corrupted
            input.delete();
            return def;
        } else {
            final BufferedImage optimizedImage =
                    GraphicsUtilities.getCompatibleBufferedImage(img.getWidth(), img.getHeight(), img.getTransparency());
            optimizedImage.getGraphics().drawImage(img, 0, 0 , null);
            return optimizedImage;
        }
    }

    static BufferedImage toImg(final URL input, final BufferedImage def) {
        BufferedImage img = def;
        try {
            img = ImageIO.read(input);
        } catch (final IOException ex) {
            System.err.println("ERROR! Unable to read from " + input);
        } catch (final IllegalArgumentException ex) {
            System.err.println("ERROR! Unable to read from null");
        }
        return img;
    }

    public static BufferedImage toImg(final InputStream input, final BufferedImage def) {
        BufferedImage img = def;
        try {
            img = ImageIO.read(input);
        } catch (final IOException ex) {
            System.err.println("ERROR! Unable to read from input stream");
        } catch (final IllegalArgumentException ex) {
            System.err.println("ERROR! Unable to read from null");
        } finally {
            close(input);
        }
        return img;
    }

    public static void toFile(final File aFile, final String aContents, final boolean append) throws IOException {
        Writer output = null;
        try {
            output = new BufferedWriter(new FileWriter(aFile, append));
            output.write(aContents);
        } finally {
            close(output);
        }
    }

    public static void toFile(
            final File aFile,
            final Properties properties,
            final String name) throws IOException {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(aFile);
            properties.store(fos, name);
        } finally {
            close(fos);
        }
    }

    static void close(final Closeable resource) {
        if (resource == null) {
            return;
        }
        boolean closed = false;
        while (!closed) {
            try {
                resource.close();
                closed = true;
            } catch (final Exception ex) {
                System.err.println(ex.getMessage());
                ex.printStackTrace();
            }
        }
    }

    public static void copyFile(final File sourceFile, final File destFile) throws IOException {
        if (!destFile.exists()) {
            destFile.createNewFile();
        }
        FileInputStream fIn = null;
        FileOutputStream fOut = null;
        FileChannel source = null;
        FileChannel destination = null;
        try {
            fIn = new FileInputStream(sourceFile);
            source = fIn.getChannel();
            fOut = new FileOutputStream(destFile);
            destination = fOut.getChannel();
            long transfered = 0;
            final long bytes = source.size();
            while (transfered < bytes) {
                transfered += destination.transferFrom(source, 0, source.size());
                destination.position(transfered);
            }
        } finally {
            if (source != null) {
                close(source);
            } else if (fIn != null) {
                close(fIn);
            }
            if (destination != null) {
                close(destination);
            } else if (fOut != null) {
                close(fOut);
            }
        }
    }

}
