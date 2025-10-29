/*
 * Copyright 2025 PixelsDB.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package io.pixelsdb.pixels.storage.http;


import io.pixelsdb.pixels.common.physical.*;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.atomic.AtomicLong;


public class TestHttpStreamPerf
{
    private final int sendLimit = 8 * 1024 * 1024; // 8MB per block
    private final int sendNum = 200;               // send 200 blocks
    private final int rounds = 3;                  // run multiple rounds for averaging

    @Test
    public void testPhysicalReaderAndWriterPerf() throws IOException
    {
        double[] throughputList = new double[rounds];

        for (int round = 0; round < rounds; round++)
        {
            final AtomicLong totalBytesReader = new AtomicLong(0);
            final AtomicLong totalBytesWriter = new AtomicLong(0);
            final Storage httpStream = StorageFactory.Instance().getStorage(Storage.Scheme.httpstream);
            final String endpoint = "httpstream://localhost:" + (29920 + round);

            int finalRound = round;
            Thread readerThread = new Thread(() -> {
                try (PhysicalReader fsReader = PhysicalReaderUtil.newPhysicalReader(httpStream, endpoint))
                {
                    byte[] buffer = new byte[sendLimit];
                    long start = System.nanoTime();
                    for (int i = 0; i < sendNum; i++)
                    {
                        fsReader.readFully(buffer);
                        totalBytesReader.addAndGet(buffer.length);
                    }
                    long end = System.nanoTime();
                    double seconds = (end - start) / 1e9;
                    double throughputMB = totalBytesReader.get() / 1024.0 / 1024.0 / seconds;
                    throughputList[finalRound] = throughputMB;
                    System.out.printf("[Round %d][Reader] Total: %.2f MB, Time: %.2fs, Throughput: %.2f MB/s%n",
                            finalRound + 1, totalBytesReader.get() / 1024.0 / 1024.0, seconds, throughputMB);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });

            int finalRound1 = round;
            Thread writerThread = new Thread(() -> {
                try (PhysicalWriter fsWriter = PhysicalWriterUtil.newPhysicalWriter(httpStream, endpoint))
                {
                    byte[] buffer = new byte[sendLimit];
                    for (int j = 0; j < sendLimit; j++) {
                        buffer[j] = (byte) ('a' + j % 10);
                    }

                    long start = System.nanoTime();
                    for (int i = 0; i < sendNum; i++)
                    {
                        fsWriter.append(buffer, 0, sendLimit);
                        fsWriter.flush();
                        totalBytesWriter.addAndGet(buffer.length);
                    }
                    long end = System.nanoTime();
                    double seconds = (end - start) / 1e9;
                    double throughputMB = totalBytesWriter.get() / 1024.0 / 1024.0 / seconds;
                    System.out.printf("[Round %d][Writer] Total: %.2f MB, Time: %.2fs, Throughput: %.2f MB/s%n",
                            finalRound1 + 1, totalBytesWriter.get() / 1024.0 / 1024.0, seconds, throughputMB);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });

            readerThread.start();
            writerThread.start();
            try {
                readerThread.join();
                writerThread.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        double sum = 0;
        for (double v : throughputList) {
            sum += v;
        }
        double avg = sum / rounds;

        System.out.println("\n========== [Perf Summary] ==========");
        for (int i = 0; i < rounds; i++) {
            System.out.printf("Round %d Throughput: %.2f MB/s%n", i + 1, throughputList[i]);
        }
        System.out.printf("Average Throughput: %.2f MB/s%n", avg);
        System.out.println("====================================\n");

        System.out.printf("[SCORE] %.1f%n", getGrade(avg));
    }


    private double getGrade(double throughputMB)
    {
        double base = 600.0;
        double max = 1200.0;

        if (throughputMB <= base)
        {
            return 50.0;
        }
        else if (throughputMB >= max)
        {
            return 100.0;
        }
        else
        {
            return 50.0 + (throughputMB - base) / (max - base) * 50.0;
        }
    }
}
