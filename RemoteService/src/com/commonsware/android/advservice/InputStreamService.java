/***
 Copyright (c) 2008-2012 CommonsWare, LLC
 Licensed under the Apache License, Version 2.0 (the "License"); you may not
 use this file except in compliance with the License. You may obtain a copy
 of the License at http://www.apache.org/licenses/LICENSE-2.0. Unless required
 by applicable law or agreed to in writing, software distributed under the
 License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS
 OF ANY KIND, either express or implied. See the License for the specific
 language governing permissions and limitations under the License.

 From _The Busy Coder's Guide to Android Development_
 http://commonsware.com/Android
 */

package com.commonsware.android.advservice;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.util.Log;

import org.apache.commons.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

/**
 * This class tests various techniques for sending an InputStream across process
 * boundaries in Android
 *
 * @author Abel Luck, Flow, Mark Murphy
 */
public class InputStreamService extends Service {
    private final static String TAG = "InputStreamService";

    @Override
    public IBinder onBind(Intent intent) {
        return (new InputStreamBinder(this));
    }

    private static class InputStreamBinder extends IInputStreamService.Stub {
        private final static String TAG = "InputStreamService.InputStreamBinder";

        private ByteArrayOutputStream mTest2OS;

        InputStreamBinder(Context ctxt) {
        }

        /**
         * Test #1: The client sends us two PFDs that we read and write from
         */
        @Override
        public void sendInputStreams(ParcelFileDescriptor input, ParcelFileDescriptor output) throws RemoteException {
            // read the input
            InputStream is = new ParcelFileDescriptor.AutoCloseInputStream(input);
            OutputStream os = new ByteArrayOutputStream();
            String input_result = "uninitialized";
            try {
                int count = IOUtils.copy(is, os);
                input_result = ((ByteArrayOutputStream) os).toString("UTF-8");
                Log.d(TAG, "Test #1 read input " + count + " str=" + input_result);
                is.close();
                os.close();
            } catch (IOException e) {
                Log.d(TAG, "Test #1 failed");
                e.printStackTrace();
            }

            // write the output
            try {
                is = new ByteArrayInputStream(
                        new StringBuilder(input_result).reverse().toString().getBytes("UTF-8"));
                os = new ParcelFileDescriptor.AutoCloseOutputStream(output);
                int count = IOUtils.copy(is, os);
                Log.d(TAG, "Test #1 wrote result " + count);
                is.close();
                os.close();
            } catch (IOException e) {
                Log.d(TAG, "Test #1 failed");
                e.printStackTrace();
            } finally {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        /**
         * Test #2: returning a PFD that client can write to that we read later
         */
        @Override
        public ParcelFileDescriptor getOutputStream() throws RemoteException {
            ParcelFileDescriptor pfd = null;
            try {
                Log.d(TAG, "Test #2 getOutputStream(): returning PFD");
                mTest2OS = new ByteArrayOutputStream();
                 pfd = ParcelFileDescriptorUtil.pipeTo(mTest2OS, new IThreadListener() {

                    @Override
                    public void onThreadFinished(Thread thread) {
                        Log.d(TAG, "test 2 thread finished");
                        try {
                            byte[] result = mTest2OS.toByteArray();
                            Log.d(TAG, "received " + result.length + " bytes str=" + mTest2OS.toString("UTF-8"));
                            mTest2OS.close();
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                });
            } catch (IOException e) {
                Log.e(TAG, "Test #2 failed getInputStream create PFD failed");
                e.printStackTrace();
            }
            return pfd;
        }

    };
}
