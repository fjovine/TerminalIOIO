package it.hilab.android.terminalioio;

import ioio.lib.api.DigitalOutput;
import ioio.lib.api.IOIO;
import ioio.lib.api.IOIO.VersionType;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.api.Uart;
import ioio.lib.util.BaseIOIOLooper;
import ioio.lib.util.IOIOLooper;
import ioio.lib.util.android.IOIOActivity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.EditText;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * This is the main activity of the TerminalIOIO example application directly
 * derived HelloIOIO example application.
 *
 * It displays a toggle button on the screen, which enables control of the
 * on-board LED. This example shows a very simple usage of the IOIO, by using
 * the {@link IOIOActivity} class. For a more advanced use case, see the
 * HelloIOIOPower example.
 */
public class TerminalIOIO extends IOIOActivity {
    private EditText lineToSend_;
    private EditText arrived_;
    private String arrivedContent_ = "";
    private EditText sent_;
    private String sentContent_ = "";
    private StringBuffer arrivedLine_ = new StringBuffer();

    private boolean wasClicked = false;

    /**
     * Called when the activity is first created. Here we normally initialize
     * our GUI.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        lineToSend_ = (EditText) findViewById(R.id.lineToSend);
        arrived_ = (EditText) findViewById(R.id.arrived);
        sent_ = (EditText) findViewById(R.id.sent);
        lineToSend_.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    wasClicked = true;
                    return true;
                }
                return false;
            }
        });
    }

    /**
     * This is the thread on which all the IOIO activity happens. It will be run
     * every time the application is resumed and aborted when it is paused. The
     * method setup() will be called right after a connection with the IOIO has
     * been established (which might happen several times!). Then, loop() will
     * be called repetitively until the IOIO gets disconnected.
     */
    class Looper extends BaseIOIOLooper {
        /** The on-board LED. */
        private DigitalOutput led_;
        private Uart uart_ = null;
        private OutputStream uartOut_ = null;
        private InputStream uartIn_ = null;

        /**
         * Called every time a connection with IOIO has been established.
         * Typically used to open pins.
         *
         * @throws ConnectionLostException
         *             When IOIO connection is lost.
         *
         * @see ioio.lib.util.IOIOLooper#setup()
         */
        @Override
        protected void setup() throws ConnectionLostException {
            showVersions(ioio_, "IOIO connected!");
            led_ = ioio_.openDigitalOutput(0, true);
            uart_ = ioio_.openUart(6, 7, 9600, Uart.Parity.NONE, Uart.StopBits.ONE);
            uartOut_ = uart_.getOutputStream();
            uartIn_ = uart_.getInputStream();
            arrivedContent_="";
            enableUi(true);
        }

        /**
         * Called repetitively while the IOIO is connected.
         *
         * @throws ConnectionLostException
         *             When IOIO connection is lost.
         * @throws InterruptedException
         * 				When the IOIO thread has been interrupted.
         *
         * @see ioio.lib.util.IOIOLooper#loop()
         */
        @Override
        public void loop() throws ConnectionLostException, InterruptedException {
            if (wasClicked) {
                try {
                    led_.write(true);
                    String s = lineToSend_.getText().toString()+"\n";
                    Log.e("TerminalIOIO", "["+s+"]");
                    sentContent_ += s;
                    uartOut_.write(s.getBytes());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            sent_.setText(sentContent_);
                            sent_.setSelection(sent_.length());
                            lineToSend_.setText("");
                        }
                    });
                } catch (IOException ioe) {
                    Log.e("TerminalIOIO","Impossible to write to serial line");
                }
            }
            try {
                int availableCount = this.uartIn_.available();
                if (availableCount > 0) {
                    byte [] buffer = new byte[availableCount];
                    this.uartIn_.read(buffer);
                    for (byte b : buffer) {
                        switch (b) {
                            case 0xd:
                                arrivedContent_ += arrivedLine_.toString();
                                arrivedContent_ += "\n";
                                arrivedLine_.setLength(0);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        arrived_.setText(arrivedContent_);
                                        arrived_.setSelection(arrived_.length());
                                    }
                                });
                                break;
                            case 0xa:
                                break;
                            case 0x8:
                                if (arrivedLine_.length() > 0) {
                                    arrivedLine_.setLength(arrivedLine_.length() - 1);
                                }
                                break;
                            default:
                                arrivedLine_.append((char)b);
                                break;
                        }
                    }
                }
            } catch (IOException ioe) {
                Log.e("TerminalIOIO","An IOException happened");
            }
            synchronized (this) {
                wasClicked = false;
            }
            Thread.sleep(100);
            led_.write(false);
        }

        /**
         * Called when the IOIO is disconnected.
         *
         * @see ioio.lib.util.IOIOLooper#disconnected()
         */
        @Override
        public void disconnected() {
            enableUi(false);
            toast("IOIO disconnected");
        }

        /**
         * Called when the IOIO is connected, but has an incompatible firmware version.
         *
         * @see ioio.lib.util.IOIOLooper#incompatible(IOIO)
         */
        @Override
        public void incompatible() {
            showVersions(ioio_, "Incompatible firmware version!");
        }
    }

    /**
     * A method to create our IOIO thread.
     *
     * @see ioio.lib.util.AbstractIOIOActivity#createIOIOThread()
     */
    @Override
    protected IOIOLooper createIOIOLooper() {
        return new Looper();
    }

    private void showVersions(IOIO ioio, String title) {
        toast(String.format("%s\n" +
                        "IOIOLib: %s\n" +
                        "Application firmware: %s\n" +
                        "Bootloader firmware: %s\n" +
                        "Hardware: %s",
                title,
                ioio.getImplVersion(VersionType.IOIOLIB_VER),
                ioio.getImplVersion(VersionType.APP_FIRMWARE_VER),
                ioio.getImplVersion(VersionType.BOOTLOADER_VER),
                ioio.getImplVersion(VersionType.HARDWARE_VER)));
    }

    private void toast(final String message) {
        final Context context = this;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private int numConnected_ = 0;

    private void enableUi(final boolean enable) {
        // This is slightly trickier than expected to support a multi-IOIO use-case.
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (enable) {
                    if (numConnected_++ == 0) {
                        lineToSend_.setEnabled(true);
                    }
                } else {
                    if (--numConnected_ == 0) {
                        lineToSend_.setEnabled(false);
                    }
                }
            }
        });
    }
}