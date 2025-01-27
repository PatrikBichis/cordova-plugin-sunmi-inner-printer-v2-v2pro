package com.sunmi.innerprinter;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.json.JSONArray;
import org.json.JSONException;

import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaWebView;

import woyou.aidlservice.jiuiv5.ICallback;
import woyou.aidlservice.jiuiv5.IWoyouService;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ComponentName;
import android.content.ServiceConnection;

import android.graphics.Bitmap;

import android.os.IBinder;

import android.util.Base64;
import android.util.Log;

import com.sunmi.utils.BitmapUtils;
import com.sunmi.utils.ThreadPoolManager;

public class Printer extends CordovaPlugin {
  private static final String TAG = "SunmiInnerPrinter";

  private BitmapUtils bitMapUtils;
  private IWoyouService woyouService;
  private PrinterStatusReceiver printerStatusReceiver = new PrinterStatusReceiver();
      private ICallback callback;

  private ServiceConnection connService = new ServiceConnection() {
    @Override
    public void onServiceDisconnected(ComponentName name) {
      woyouService = null;
      Log.d(TAG, "Service disconnected");
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
      woyouService = IWoyouService.Stub.asInterface(service);
      Log.d(TAG, "Service connected");
    }
  };

  public final static String OUT_OF_PAPER_ACTION = "woyou.aidlservice.jiuv5.OUT_OF_PAPER_ACTION";
  public final static String ERROR_ACTION = "woyou.aidlservice.jiuv5.ERROR_ACTION";
  public final static String NORMAL_ACTION = "woyou.aidlservice.jiuv5.NORMAL_ACTION";
  public final static String COVER_OPEN_ACTION = "woyou.aidlservice.jiuv5.COVER_OPEN_ACTION";
  public final static String COVER_ERROR_ACTION = "woyou.aidlservice.jiuv5.COVER_ERROR_ACTION";
  public final static String KNIFE_ERROR_1_ACTION = "woyou.aidlservice.jiuv5.KNIFE_ERROR_ACTION_1";
  public final static String KNIFE_ERROR_2_ACTION = "woyou.aidlservice.jiuv5.KNIFE_ERROR_ACTION_2";
  public final static String OVER_HEATING_ACITON = "woyou.aidlservice.jiuv5.OVER_HEATING_ACITON";
  public final static String FIRMWARE_UPDATING_ACITON = "woyou.aidlservice.jiuv5.FIRMWARE_UPDATING_ACITON";

  @Override
  public void initialize(CordovaInterface cordova, CordovaWebView webView) {
    super.initialize(cordova, webView);

    Context applicationContext = this.cordova.getActivity().getApplicationContext();

    bitMapUtils = new BitmapUtils(applicationContext);

    Intent intent = new Intent();
    intent.setPackage("woyou.aidlservice.jiuiv5");
    intent.setAction("woyou.aidlservice.jiuiv5.IWoyouService");

    applicationContext.startService(intent);
    applicationContext.bindService(intent, connService, Context.BIND_AUTO_CREATE);

    IntentFilter mFilter = new IntentFilter();
    mFilter.addAction(OUT_OF_PAPER_ACTION);
    mFilter.addAction(ERROR_ACTION);
    mFilter.addAction(NORMAL_ACTION);
    mFilter.addAction(COVER_OPEN_ACTION);
    mFilter.addAction(COVER_ERROR_ACTION);
    mFilter.addAction(KNIFE_ERROR_1_ACTION);
    mFilter.addAction(KNIFE_ERROR_2_ACTION);
    mFilter.addAction(OVER_HEATING_ACITON);
    mFilter.addAction(FIRMWARE_UPDATING_ACITON);

    applicationContext.registerReceiver(printerStatusReceiver, mFilter);
     callback = new woyou.aidlservice.jiuiv5.ICallback.Stub() {

                @Override
                public void onRunResult(boolean isSuccess) {
                    Log.i(TAG, "onRunResult:" + isSuccess);
                }

                @Override
                public void onReturnString(String result) {
                    Log.i(TAG, "onReturnString:" + result);
                }

                @Override
                public void onRaiseException(int code, String msg) {
                    Log.i(TAG, "onRaiseException:" + code + ":" + msg);
                }

                @Override
                public void onPrintResult(int code, String msg) {
                    Log.i(TAG, "onPrintResult:" + code + ":" + msg);
                }

            };
  }

  @Override
  public boolean execute(String action, JSONArray data, CallbackContext callbackContext) throws JSONException {
    if (action.equals("printerInit")) {
      printerInit(callbackContext, callback);
      return true;
    } else if (action.equals("cutPaper")) {
      cutPaper(callbackContext, callback);
      return true;
    } else if (action.equals("printerSelfChecking")) {
      printerSelfChecking(callbackContext, callback);
      return true;
    } else if (action.equals("getPrinterSerialNo")) {
      getPrinterSerialNo(callbackContext);
      return true;
    } else if (action.equals("getPrinterVersion")) {
      getPrinterVersion(callbackContext);
      return true;
    } else if (action.equals("hasPrinter")) {
      hasPrinter(callbackContext);
      return true;
    } else if (action.equals("getPrintedLength")) {
      getPrintedLength(callbackContext, callback);
      return true;
    } else if (action.equals("lineWrap")) {
      lineWrap(data.getInt(0), callbackContext, callback);
      return true;
    } else if (action.equals("sendRAWData")) {
      sendRAWData(data.getString(0), callbackContext, callback);
      return true;
    } else if (action.equals("setAlignment")) {
      setAlignment(data.getInt(0), callbackContext, callback);
      return true;
    } else if (action.equals("setFontName")) {
      setFontName(data.getString(0), callbackContext, callback);
      return true;
    } else if (action.equals("setFontSize")) {
      setFontSize((float) data.getDouble(0), callbackContext, callback);
      return true;
    } else if (action.equals("printTextWithFont")) {
      printTextWithFont(data.getString(0), data.getString(1), (float) data.getDouble(2), callbackContext, callback);
      return true;
    } else if (action.equals("printColumnsText")) {
      printColumnsText(data.getJSONArray(0), data.getJSONArray(1), data.getJSONArray(2), callbackContext, callback);
      return true;
    } else if (action.equals("printBitmap")) {
      printBitmap(data.getString(0), data.getInt(1), data.getInt(2), callbackContext, callback);
      return true;
    } else if (action.equals("printBarCode")) {
      printBarCode(data.getString(0), data.getInt(1), data.getInt(2), data.getInt(1), data.getInt(2), callbackContext, callback);
      return true;
    } else if (action.equals("printQRCode")) {
      printQRCode(data.getString(0), data.getInt(1), data.getInt(2), callbackContext, callback);
      return true;
    } else if (action.equals("printOriginalText")) {
      printOriginalText(data.getString(0), callbackContext, callback);
      return true;
    } else if (action.equals("printString")) {
      printString(data.getString(0), callbackContext, callback);
      return true;
    } else if (action.equals("printerStatusStartListener")) {
      printerStatusStartListener(callbackContext);
      return true;
    } else if (action.equals("printerStatusStopListener")) {
      printerStatusStopListener();
      return true;
    } else if (action.equals("enterPrinterBuffer")) {
           enterPrinterBuffer(data.getBoolean(0));
           return true;
     }else if (action.equals("exitPrinterBuffer")) {
         exitPrinterBuffer(data.getBoolean(0));
         return true;
       }

    return false;
  }

  public void printerInit(final CallbackContext callbackContext, final ICallback callback) {
    final IWoyouService printerService = woyouService;
    ThreadPoolManager.getInstance().executeTask(new Runnable() {
      @Override
      public void run() {
        try {
          printerService.printerInit(callback);
        } catch (Exception e) {
          e.printStackTrace();
          Log.i(TAG, "ERROR: " + e.getMessage());
          callbackContext.error(e.getMessage());
        }
      }
    });
  }

   public void cutPaper(final CallbackContext callbackContext, final ICallback callback) {
    final IWoyouService printerService = woyouService;
    ThreadPoolManager.getInstance().executeTask(new Runnable() {
      @Override
      public void run() {
        try {
          printerService.cutPaper(callback);
        } catch (Exception e) {
          e.printStackTrace();
          Log.i(TAG, "ERROR: " + e.getMessage());
          callbackContext.error(e.getMessage());
        }
      }
    });
  }

  public void printerSelfChecking(final CallbackContext callbackContext, final ICallback callback) {
    final IWoyouService printerService = woyouService;
    ThreadPoolManager.getInstance().executeTask(new Runnable() {
      @Override
      public void run() {
        try {
          printerService.printerSelfChecking(callback);
        } catch (Exception e) {
          e.printStackTrace();
          Log.i(TAG, "ERROR: " + e.getMessage());
          callbackContext.error(e.getMessage());
        }
      }
    });
  }

  public void getPrinterSerialNo(final CallbackContext callbackContext) {
    try {
      callbackContext.success(getPrinterSerialNo());
    } catch (Exception e) {
      Log.i(TAG, "ERROR: " + e.getMessage());
      callbackContext.error(e.getMessage());
    }
  }

  private String getPrinterSerialNo() throws Exception {
    final IWoyouService printerService = woyouService;
    return printerService.getPrinterSerialNo();
  }

  public void getPrinterVersion(final CallbackContext callbackContext) {
    try {
      callbackContext.success(getPrinterVersion());
    } catch (Exception e) {
      Log.i(TAG, "ERROR: " + e.getMessage());
      callbackContext.error(e.getMessage());
    }
  }

  private String getPrinterVersion() throws Exception {
    final IWoyouService printerService = woyouService;
    return printerService.getPrinterVersion();
  }

  public void getPrinterModal(final CallbackContext callbackContext) {
    try {
      callbackContext.success(getPrinterModal());
    } catch (Exception e) {
      Log.i(TAG, "ERROR: " + e.getMessage());
      callbackContext.error(e.getMessage());
    }
  }

  private String getPrinterModal() throws Exception {
    // Caution: This method is not fully test -- Januslo 2018-08-11
    final IWoyouService printerService = woyouService;
    return printerService.getPrinterModal();
  }

  public void hasPrinter(final CallbackContext callbackContext) {
    try {
      callbackContext.success(hasPrinter());
    } catch (Exception e) {
      Log.i(TAG, "ERROR: " + e.getMessage());
      callbackContext.error(e.getMessage());
    }
  }

  private int hasPrinter() {
    final IWoyouService printerService = woyouService;
    final boolean hasPrinterService = printerService != null;
    return hasPrinterService ? 1 : 0;
  }

  public void getPrintedLength(final CallbackContext callbackContext, final ICallback callback) {
    final IWoyouService printerService = woyouService;
    ThreadPoolManager.getInstance().executeTask(new Runnable() {
      @Override
      public void run() {
        try {
          printerService.getPrintedLength(callback);
        } catch (Exception e) {
          e.printStackTrace();
          Log.i(TAG, "ERROR: " + e.getMessage());
          callbackContext.error(e.getMessage());
        }
      }
    });
  }

  public void lineWrap(int n, final CallbackContext callbackContext, final ICallback callback) {
    final IWoyouService printerService = woyouService;
    final int count = n;
    ThreadPoolManager.getInstance().executeTask(new Runnable() {
      @Override
      public void run() {
        try {
          printerService.lineWrap(count, callback);
        } catch (Exception e) {
          e.printStackTrace();
          Log.i(TAG, "ERROR: " + e.getMessage());
          callbackContext.error(e.getMessage());
        }
      }
    });
  }

  public void sendRAWData(String base64EncriptedData, final CallbackContext callbackContext, final ICallback callback) {
    final IWoyouService printerService = woyouService;
    final byte[] d = Base64.decode(base64EncriptedData, Base64.DEFAULT);
    ThreadPoolManager.getInstance().executeTask(new Runnable() {
      @Override
      public void run() {
        try {
          printerService.sendRAWData(d, callback);
        } catch (Exception e) {
          e.printStackTrace();
          Log.i(TAG, "ERROR: " + e.getMessage());
          callbackContext.error(e.getMessage());
        }
      }
    });
  }

  public void setAlignment(int alignment, final CallbackContext callbackContext, final ICallback callback) {
    final IWoyouService printerService = woyouService;
    final int align = alignment;
    ThreadPoolManager.getInstance().executeTask(new Runnable() {
      @Override
      public void run() {
        try {
          printerService.setAlignment(align, callback);
        } catch (Exception e) {
          e.printStackTrace();
          Log.i(TAG, "ERROR: " + e.getMessage());
          callbackContext.error(e.getMessage());
        }
      }
    });
  }

  public void setFontName(String typeface, final CallbackContext callbackContext, final ICallback callback) {
    final IWoyouService printerService = woyouService;
    final String tf = typeface;
    ThreadPoolManager.getInstance().executeTask(new Runnable() {
      @Override
      public void run() {
        try {
          printerService.setFontName(tf, callback);
        } catch (Exception e) {
          e.printStackTrace();
          Log.i(TAG, "ERROR: " + e.getMessage());
          callbackContext.error(e.getMessage());
        }
      }
    });
  }

  public void setFontSize(float fontsize, final CallbackContext callbackContext, final ICallback callback) {
    final IWoyouService printerService = woyouService;
    final float fs = fontsize;
    ThreadPoolManager.getInstance().executeTask(new Runnable() {
      @Override
      public void run() {
        try {
          printerService.setFontSize(fs, callback);
        } catch (Exception e) {
          e.printStackTrace();
          Log.i(TAG, "ERROR: " + e.getMessage());
          callbackContext.error(e.getMessage());
        }
      }
    });
  }

  public void printTextWithFont(String text, String typeface, float fontsize, final CallbackContext callbackContext, final ICallback callback) {
    final IWoyouService printerService = woyouService;
    final String txt = text;
    final String tf = typeface;
    final float fs = fontsize;
    ThreadPoolManager.getInstance().executeTask(new Runnable() {
      @Override
      public void run() {
        try {
          printerService.printTextWithFont(txt, tf, fs, callback);
        } catch (Exception e) {
          e.printStackTrace();
          Log.i(TAG, "ERROR: " + e.getMessage());
          callbackContext.error(e.getMessage());
        }
      }
    });
  }

  public void printColumnsText(JSONArray colsTextArr, JSONArray colsWidthArr, JSONArray colsAlign,
      final CallbackContext callbackContext, final ICallback callback) {
    final IWoyouService printerService = woyouService;
    final String[] clst = new String[colsTextArr.length()];
    for (int i = 0; i < colsTextArr.length(); i++) {
      try {
        clst[i] = colsTextArr.getString(i);
      } catch (JSONException e) {
        clst[i] = "-";
        Log.i(TAG, "ERROR TEXT: " + e.getMessage());
      }
    }
    final int[] clsw = new int[colsWidthArr.length()];
    for (int i = 0; i < colsWidthArr.length(); i++) {
      try {
        clsw[i] = colsWidthArr.getInt(i);
      } catch (JSONException e) {
        clsw[i] = 1;
        Log.i(TAG, "ERROR WIDTH: " + e.getMessage());
      }
    }
    final int[] clsa = new int[colsAlign.length()];
    for (int i = 0; i < colsAlign.length(); i++) {
      try {
        clsa[i] = colsAlign.getInt(i);
      } catch (JSONException e) {
        clsa[i] = 0;
        Log.i(TAG, "ERROR ALIGN: " + e.getMessage());
      }
    }
    ThreadPoolManager.getInstance().executeTask(new Runnable() {
      @Override
      public void run() {
        try {
          printerService.printColumnsText(clst, clsw, clsa, callback);
        } catch (Exception e) {
          e.printStackTrace();
          Log.i(TAG, "ERROR: " + e.getMessage());
          callbackContext.error(e.getMessage());
        }
      }
    });
  }

  public void printBitmap(String data, int width, int height, final CallbackContext callbackContext, final ICallback callback) {
    try {
      final IWoyouService printerService = woyouService;
      byte[] decoded = Base64.decode(data, Base64.DEFAULT);
      final Bitmap bitMap = bitMapUtils.decodeBitmap(decoded, width, height);
      ThreadPoolManager.getInstance().executeTask(new Runnable() {
        @Override
        public void run() {
          try {
            printerService.printBitmap(bitMap, callback);
          } catch (Exception e) {
            e.printStackTrace();
            Log.i(TAG, "ERROR: " + e.getMessage());
            callbackContext.error(e.getMessage());
          }
        }
      });
    } catch (Exception e) {
      e.printStackTrace();
      Log.i(TAG, "ERROR: " + e.getMessage());
    }
  }

  public void printBarCode(String data, int symbology, int width, int height, int textPosition,
      final CallbackContext callbackContext, final ICallback callback) {
    final IWoyouService printerService = woyouService;
    final String d = data;
    final int s = symbology;
    final int h = height;
    final int w = width;
    final int tp = textPosition;

    ThreadPoolManager.getInstance().executeTask(new Runnable() {
      @Override
      public void run() {
        try {
          printerService.printBarCode(d, s, h, w, tp, callback);
        } catch (Exception e) {
          e.printStackTrace();
          Log.i(TAG, "ERROR: " + e.getMessage());
          callbackContext.error(e.getMessage());
        }
      }
    });
  }

  public void printQRCode(String data, int moduleSize, int errorLevel, final CallbackContext callbackContext, final ICallback callback) {
    final IWoyouService printerService = woyouService;
    final String d = data;
    final int size = moduleSize;
    final int level = errorLevel;
    ThreadPoolManager.getInstance().executeTask(new Runnable() {
      @Override
      public void run() {
        try {
          printerService.printQRCode(d, size, level, callback);
        } catch (Exception e) {
          e.printStackTrace();
          Log.i(TAG, "ERROR: " + e.getMessage());
          callbackContext.error(e.getMessage());
        }
      }
    });
  }

  public void printOriginalText(String text, final CallbackContext callbackContext, final ICallback callback) {
    final IWoyouService printerService = woyouService;
    final String txt = text;
    ThreadPoolManager.getInstance().executeTask(new Runnable() {
      @Override
      public void run() {
        try {
          printerService.printOriginalText(txt, callback);
        } catch (Exception e) {
          e.printStackTrace();
          Log.i(TAG, "ERROR: " + e.getMessage());
          callbackContext.error(e.getMessage());
        }
      }
    });
  }

  public void commitPrinterBuffer() {
    final IWoyouService printerService = woyouService;
    ThreadPoolManager.getInstance().executeTask(new Runnable() {
      @Override
      public void run() {
        try {
          printerService.commitPrinterBuffer();
        } catch (Exception e) {
          e.printStackTrace();
          Log.i(TAG, "ERROR: " + e.getMessage());
        }
      }
    });
  }

  public void enterPrinterBuffer(boolean clean) {
    final IWoyouService printerService = woyouService;
    final boolean c = clean;
    ThreadPoolManager.getInstance().executeTask(new Runnable() {
      @Override
      public void run() {
        try {
          printerService.enterPrinterBuffer(c);
        } catch (Exception e) {
          e.printStackTrace();
          Log.i(TAG, "ERROR: " + e.getMessage());
        }
      }
    });
  }

  public void exitPrinterBuffer(boolean commit) {
    final IWoyouService printerService = woyouService;
    final boolean com = commit;
    ThreadPoolManager.getInstance().executeTask(new Runnable() {
      @Override
      public void run() {
        try {
          printerService.exitPrinterBuffer(com);
        } catch (Exception e) {
          e.printStackTrace();
          Log.i(TAG, "ERROR: " + e.getMessage());
        }
      }
    });
  }

  public void printString(String message, final CallbackContext callbackContext, final ICallback callback) {
    final IWoyouService printerService = woyouService;
    final String msgs = message;
    ThreadPoolManager.getInstance().executeTask(new Runnable() {
      @Override
      public void run() {
        try {
          printerService.printText(msgs, callback);
        } catch (Exception e) {
          e.printStackTrace();
          Log.i(TAG, "ERROR: " + e.getMessage());
          callbackContext.error(e.getMessage());
        }
      }
    });
  }

  public void printerStatusStartListener(final CallbackContext callbackContext) {
    final PrinterStatusReceiver receiver = printerStatusReceiver;
    receiver.startReceiving(callbackContext);
  }

  public void printerStatusStopListener() {
    final PrinterStatusReceiver receiver = printerStatusReceiver;
    receiver.stopReceiving();
  }

}
