import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class FileCopy {

    public static void copyFile(String src, String dst) throws IOException {
        Path srcPath = Paths.get(src);
        Path dstPath = Paths.get(dst);

        // Open source and destination channels
        try (FileChannel srcChannel = FileChannel.open(srcPath, StandardOpenOption.READ);
             FileChannel dstChannel = FileChannel.open(dstPath,
                     StandardOpenOption.CREATE,
                     StandardOpenOption.WRITE,
                     StandardOpenOption.TRUNCATE_EXISTING)) {

            long fileSize = srcChannel.size();
            long position = 0;

            int bufferSize = 8192; // 8 KB buffer
            ByteBuffer buffer = ByteBuffer.allocate(bufferSize);

            while (position < fileSize) {
                buffer.clear();

                // Read from source at specific position (like pread)
                int bytesRead = srcChannel.read(buffer, position);
                if (bytesRead == -1) break;

                buffer.flip();

                // Write to destination at specific position (like pwrite)
                dstChannel.write(buffer, position);

                position += bytesRead;
            }
        }
        // try-with-resources ensures channels are closed automatically
    }

    public static void main(String[] args) {
        try {
            copyFile("source.txt", "destination.txt");
            System.out.println("File copied successfully.");
        } catch (IOException e) {
            System.err.println("Error copying file: " + e.getMessage());
        }
    }
}