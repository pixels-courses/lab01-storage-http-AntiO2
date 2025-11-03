## Lab #1

The first lab is to optimize `mini-storage-http`. This module can be used to transfer data from one process to the other, and it will be used by the
shuffle framework in PixelsDB (the cloud lakehouse engine based on Pixels).

The efficiency of `mini-storage-http` significantly impacts the performance of data transfer and data shuffle.
The current implementation of `mini-storage-http` needs to be optimized in terms of performance, resource efficiency, and comment+code qualify.
For example, it attempts to use Netty (a popular async-network library used in many big-data systems such as Spark) to implement asynchronous data transfer.
However, it actually transfers data serially.

Please help improve the performance (and also the coding quality) of this component (e.g., by enabling real async transfer, reducing memory copy, etc.) and reduce the CPU and memory consumptions.

You can test the performance of this component by running the unit test in `mini-storage-http/src/test/java/io/pixelsdb/pixels/storage/http/TestHttpStream`.
Ensure you have 8GB free memory on your laptop to run this test.

You can also conduct performance tests in `mini-storage-http/src/test/java/io/pixelsdb/pixels/storage/http/TestHttpStreamPerf`. We will score based on the performance tool of GitHub Action's standard worker. You can run some local tests to observe if there's any performance improvement.

**NOTICE:** To prevent hacking, please do not modify the `TestHttpStream` and `TestHttpStreamPerf` classes in your commits. If you genuinely need to make modifications, you can duplicate these classes and then make your changes.