# Cross-process InputStream Test

This is a project that tests various techniques for passing usable
Input/OutputStreams across the process boundary in Android using remote
AIDL services.

The question to be answered is:

   **What is the easiest way to pass a Stream to another Android process?**

We make several assumptions:

* The processes are in distinct apps
    So shared memory or other "same signing key" tricks are disqualified
* No native code helpers

Eventually I'd like to develop a solution to pass files, byte[], and sockets
across a unified interface.


## Test 1: Passing ParcelFileDescriptor in AIDL method signature

TODO: writeup

Preliminary results: input to the service works, but the service writing into a
PFD doesn't, though I suspect there's some wonky thread things happening. The
TransferThread for the output pfd doesn't seem to complete.

## Test 2: Returning ParcelFileDescriptor from an AIDL method

TODO: writeup

Preliminary results: works!

## Test 3:  ContentProvider / ContentResolver

TODO: writeup


# Credits

Credits to the following people:

* [Mark Murphy of CommonsWare][mark] - Service base and advice
* [Flow from SO][flow] - ideas for techniques #1 and #2

Licensed under the Apache 2.0

[mark]: http://commonsware.com/Android/
[flow]: http://stackoverflow.com/questions/18212152/transfer-inputstream-to-another-service-across-process-boundaries-with-parcelf
