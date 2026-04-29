import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.*;
import java.util.concurrent.*;

public class ParallelFileCopy {

    private static final int BUFFER_SIZE = 8192;

    public static void copyFileParallel(String src, String dst, int numThreads) throws IOException, InterruptedException {
        Path srcPath = Paths.get(src);
        Path dstPath = Paths.get(dst);

        try (FileChannel srcChannel = FileChannel.open(srcPath, StandardOpenOption.READ);
             FileChannel dstChannel = FileChannel.open(dstPath,
                     StandardOpenOption.CREATE,
                     StandardOpenOption.WRITE,
                     StandardOpenOption.TRUNCATE_EXISTING)) {

            long fileSize = srcChannel.size();
            long chunkSize = (fileSize + numThreads - 1) / numThreads;

            ExecutorService executor = Executors.newFixedThreadPool(numThreads);

            for (int i = 0; i < numThreads; i++) {
                final long start = i * chunkSize;
                final long end = Math.min(start + chunkSize, fileSize);

                if (start >= end) break;

                executor.submit(() -> {
                    ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
                    long position = start;

                    try {
                        while (position < end) {
                            buffer.clear();

                            int bytesToRead = (int) Math.min(BUFFER_SIZE, end - position);

                            buffer.limit(bytesToRead);

                            int bytesRead = srcChannel.read(buffer, position);
                            if (bytesRead == -1) break;

                            buffer.flip();

                            dstChannel.write(buffer, position);

                            position += bytesRead;
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            }

            executor.shutdown();
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        }
    }

    public static void main(String[] args) {
        try {
            copyFileParallel("source.txt", "destination.txt", 4);
            System.out.println("Parallel copy complete.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}