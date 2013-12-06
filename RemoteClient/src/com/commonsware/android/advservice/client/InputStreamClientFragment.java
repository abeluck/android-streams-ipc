
package com.commonsware.android.advservice.client;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

import com.commonsware.android.advservice.IInputStreamService;
import com.commonsware.android.advservice.IThreadListener;
import com.commonsware.android.advservice.ParcelFileDescriptorUtil;

import org.apache.commons.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

public class InputStreamClientFragment extends Fragment implements
        ServiceConnection {
    private final static String TAG = "InputStreamClientFragment";
    private IInputStreamService service = null;
    private Button btn = null;
    private Button btn2 = null;

    @Override
    public View onCreateView(LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {
        View result = inflater.inflate(R.layout.main, container, false);

        btn = (Button) result.findViewById(R.id.test1);
        btn.setOnClickListener(test1Listener);
        btn.setEnabled((service != null));

        btn2 = (Button) result.findViewById(R.id.test2);
        btn2.setOnClickListener(test2Listener);
        btn2.setEnabled((service != null));

        return (result);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setRetainInstance(true);
        getActivity().getApplicationContext()
                .bindService(new Intent(
                        "com.commonsware.android.advservice.IInputStreamService"),
                        this, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onDestroy() {
        getActivity().getApplicationContext().unbindService(this);

        super.onDestroy();
    }

    @Override
    public void onServiceConnected(ComponentName className, IBinder binder) {
        service = IInputStreamService.Stub.asInterface(binder);
        btn.setEnabled(true);
        btn2.setEnabled(true);
    }

    @Override
    public void onServiceDisconnected(ComponentName className) {
        service = null;
    }

    OnClickListener test1Listener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            doTest1();
        }
    };
    OnClickListener test2Listener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            doTest2();
        }
    };

    private void doTest1() {
        try {
            // send the input and output pfds

            InputStream is = new ByteArrayInputStream(
                    "Colorless green ideas sleep furiously".getBytes("UTF-8"));
            ParcelFileDescriptor input = ParcelFileDescriptorUtil.pipeFrom(is,
                    new IThreadListener() {

                        @Override
                        public void onThreadFinished(Thread thread) {
                            Log.d(TAG, "Test #1: copy to service finished");
                        }
                    });

            final ByteArrayOutputStream os = new ByteArrayOutputStream();
            ParcelFileDescriptor output = ParcelFileDescriptorUtil.pipeTo(os,
                    new IThreadListener() {

                        @Override
                        public void onThreadFinished(Thread thread) {
                            // service finished writing
                            try {
                                Log.d(TAG, "Test #1 read result: " + os.toByteArray().length
                                        + " str=" + os.toString("UTF-8"));
                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            }

                        }
                    });

            // blocks until result is ready
            service.sendInputStream(input, output);
            output.close(); // <-- this is required to halt the TransferThread

        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void doTest2() {
        Log.d(TAG, "Test2: fetching OutputStream");
        // Step 1: get OutputStream from service and write to it
        try {
            InputStream is = new ByteArrayInputStream(
                    "Colorless green ideas sleep furiously".getBytes("UTF-8"));
            ParcelFileDescriptor pfd = service.getOutputStream();
            if (pfd == null)
                Log.e(TAG, "sadface PFD NULL");
            OutputStream os = new ParcelFileDescriptor.AutoCloseOutputStream(pfd);

            try {
                int count = IOUtils.copy(is, os);
                Log.d(TAG, "test #2: wrote " + count);
                os.flush(); // just in case?
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    is.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                try {
                    os.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }

            }
        } catch (RemoteException e2) {
            e2.printStackTrace();
        } catch (UnsupportedEncodingException e2) {
            e2.printStackTrace();
        }
    }

}
