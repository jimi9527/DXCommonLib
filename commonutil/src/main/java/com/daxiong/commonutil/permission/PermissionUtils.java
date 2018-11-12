package com.sdyx.mall.base.utils;

import android.Manifest;
import android.app.Activity;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import com.hyx.baselibrary.Logger;
import com.hyx.baselibrary.utils.ClientInfoUtils;
import com.hyx.baselibrary.utils.StringUtils;
import com.sdyx.mall.R;
import com.sdyx.mall.base.utils.base.SPUtils;
import com.sdyx.mall.base.widget.dialog.PermissionDialog;

/**
 * @author：xujianye
 * @email：jianyexu@hyx.com
 * @date: 2017/5/6 0006 16:09
 * @description: 6.0权限申请
 */
public class PermissionUtils {

    public static final String READ_CONTACTS_PERMISSION = Manifest.permission.READ_CONTACTS;// 读取联系人权限
    public static final String CAMERA_PERMISSION = Manifest.permission.CAMERA;// 相机
    public static final String WRITE_EXTERNAL_STORAGE = Manifest.permission.WRITE_EXTERNAL_STORAGE;//SD读写权限
    public static final String READ_EXTERNAL_STORAGE = Manifest.permission.READ_EXTERNAL_STORAGE;//SD读写权限
    public static final String READ_PHONE_STATE = Manifest.permission.READ_PHONE_STATE; //获取手机串码  getDeviceId()
    public static final String WRITE_SETTINGS = Manifest.permission.WRITE_SETTINGS;

    public static final String READ_CONTACTS = Manifest.permission.READ_CONTACTS;


    public static final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    public static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    public static final String[] Location_Permissions = new String[]{COARSE_LOCATION, FINE_LOCATION};


    public static final int REQUEST_CODE = 100;
    public static final int REQUEST_CODE_READ_PHONE_STATE = 101;
    public static final int REQUEST_CODE_EXTERNAL_STORAGE = 102;
    public static final int requestPermissions_Type_1 = 1;


    public static final int Location_REQUEST_CODE = 103;

    private static final String TAG = "PermissionUtils";
    public static final String Xiaomi_Flag = "Xiaomi";
    public static final int PerMissionSetting_Request_Code = 100;

    private static final PermissionUtils instance = new PermissionUtils();

    public static PermissionUtils getInstance() {
        return instance;
    }

    private PermissionUtils() {
    }

    public boolean requestSystemPermission(Activity activity, int requestCode, String... permissions) {
        if (permissions == null || permissions.length <= 0) {
            return false;
        }
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Logger.i(TAG, "requestRunTimePermission  : " + permissions[0] + "    " + ActivityCompat.checkSelfPermission(activity, permissions[0]));
                if (ActivityCompat.checkSelfPermission(activity, permissions[0]) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(activity, permissions, requestCode);
                    return true;
                }
            }
        } catch (Exception e) {
            Logger.e(TAG, "requestRunTimePermission  : " + e.getMessage());
        }
        return false;
    }

    public boolean requestRunTimePermission(Activity activity, int requestCode, OnRequestPermissionListener onClickListener, String... permissions) {
        if (permissions == null || permissions.length <= 0) {
            return false;
        }
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Logger.i(TAG, "requestRunTimePermission  : " + permissions[0] + "    " + ActivityCompat.checkSelfPermission(activity, permissions[0]));
                if (ActivityCompat.checkSelfPermission(activity, permissions[0]) != PackageManager.PERMISSION_GRANTED) {
                    //ActivityCompat.requestPermissions(activity, permissions, requestCode);
                    requestPermissionDialog(activity, permissions, requestCode, onClickListener);
                    return true;
                }
            }
        } catch (Exception e) {
            Logger.e(TAG, "requestRunTimePermission  : " + e.getMessage());
        }
        return false;
    }


    //  定位权限 兼容主流系统 权限拒绝后  检查权限异常
    public boolean requestLocationPermission_NoCountLimit(final Activity activity, final int requestCode, final OnRequestPermissionListener onClickListener) {
        return requestLocationPermission_NoCountLimit(activity, requestCode, onClickListener, true);
    }

    public boolean requestLocationPermission_NoCountLimit(final Activity activity, final int requestCode, final OnRequestPermissionListener onClickListener,
                                                          boolean isShowDialog) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                int checkPermission = ActivityCompat.checkSelfPermission(activity, Location_Permissions[0]);
                Logger.i(TAG, "requestLocationPermission_NoCountLimit  : " + checkPermission);
                if (checkPermission != PackageManager.PERMISSION_GRANTED) {
                    PermissionDialog permissionDialog = null;
                    if (Xiaomi_Flag.equals(Build.MANUFACTURER)) {
                        permissionDialog = getDialog(activity, null, null, requestCode, onClickListener, Location_Permissions);
                    } else {
                        if (!ActivityCompat.shouldShowRequestPermissionRationale(activity,
                                Location_Permissions[0])) {
                            permissionDialog = getDialog(activity, null, null, requestCode, onClickListener, requestPermissions_Type_1, Location_Permissions);
                        } else {
                            permissionDialog = getDialog(activity, null, null, requestCode, onClickListener, Location_Permissions);
                        }
                    }

                    if (permissionDialog != null && isShowDialog) {
                        permissionDialog.show();
                    }
                    return true;
                } else {
                    //兼容 小米  拒绝后权限检测返回成功
                    AppOpsManager appOpsManager = (AppOpsManager) activity.getSystemService(Context.APP_OPS_SERVICE);
                    int checkOp = appOpsManager.checkOp(AppOpsManager.OPSTR_FINE_LOCATION, Binder.getCallingUid(), activity.getPackageName());
                    Logger.i(TAG, "requestLocationPermission_NoCountLimit  : " + checkOp);
                    if (checkOp == AppOpsManager.MODE_IGNORED) {
                        PermissionDialog permissionDialog = getDialog(activity, null, null, requestCode, onClickListener, requestPermissions_Type_1, Location_Permissions);
                        if (permissionDialog != null && isShowDialog) {
                            permissionDialog.show();
                        }
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            Logger.e(TAG, "requestLocationPermission_NoCountLimit  : " + e.getMessage());
        }
        return false;
    }


    public static boolean checkPermission(String permission, Activity context) {
        boolean flag = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // 检查该权限是否已经获取
            int i = ContextCompat.checkSelfPermission(context, permission);
            // 权限是否已经 授权 GRANTED---授权  DINIED---拒绝
            if (i != PackageManager.PERMISSION_GRANTED) {
                flag = false;
            }
        }
        Logger.d(TAG, "permission = " + permission + " " + flag);
        return flag;
    }


    /**
     * 请求权限检查
     *
     * @param activity
     * @param permissions
     * @param onClickListener
     * @return
     */
    public static PermissionDialog requestPermissionDialog(Activity activity,
                                                           String[] permissions,
                                                           int requestCode,
                                                           OnRequestPermissionListener onClickListener) {
        return showDialog(activity, null, null, requestCode, onClickListener, permissions);
    }

    public static PermissionDialog requestPermissionDialog(Activity activity,
                                                           String title, String msg,
                                                           String[] permissions,
                                                           OnRequestPermissionListener onClickListener) {
        return showDialog(activity, title, msg, REQUEST_CODE, onClickListener, permissions);
    }

    public static PermissionDialog requestPermissionDialog(final Activity activity, final String permission, final OnRequestPermissionListener onClickListener) {
        return showDialog(activity, null, null, REQUEST_CODE, onClickListener, permission);
    }


    public static PermissionDialog showDialog(final Activity activity,
                                              String title, String msg,
                                              final int requestCode,
                                              final OnRequestPermissionListener onClickListener,
                                              final String... permissions) {
        if (permissions == null || permissions.length <= 0) {
            return null;
        }
        final String key = permissions[0];
        final SPUtils spUtils = new SPUtils(activity);
        final int count = spUtils.getInt(key, 0);
        Logger.d(TAG, "key = " + key + ", count = " + count);
        if (count >= 3) {
            return null;
        }
        PermissionDialog permissionDialog = getDialog(activity, title, msg, requestCode, new OnRequestPermissionListener() {
            @Override
            public void requested(boolean isRequested) {

                if (onClickListener != null) {
                    onClickListener.requested(isRequested);
                }
            }

            @Override
            public void canceled() {
                spUtils.putInt(key, count + 1);
                spUtils.commit();
                if (onClickListener != null) {
                    onClickListener.canceled();
                }
            }
        }, permissions);

        if (permissionDialog != null) {
            permissionDialog.show();
        }
        return permissionDialog;
    }


    /**
     * @param
     * @return
     * @ actionType 0  正常请求权限弹窗
     * 1  跳转权限页面
     * @autuor statham
     * @date 2018/1/9 下午6:48
     */
    private static PermissionDialog getDialog(final Activity activity,
                                              String title, String msg,
                                              final int requestCode,
                                              final OnRequestPermissionListener onClickListener,
                                              final String... permissions) {
        return getDialog(activity, title, msg, requestCode, onClickListener, 0, permissions);
    }

    private static PermissionDialog getDialog(final Activity activity,
                                              String title, String msg,
                                              final int requestCode,
                                              final OnRequestPermissionListener onClickListener,
                                              final int actionType,
                                              final String... permissions) {
        PermissionDialog permissionDialog = new PermissionDialog(activity);
        permissionDialog.setTitle(StringUtils.isEmpty(title) ? getPermissionTitle(permissions[0]) : title);
        permissionDialog.setMessage(StringUtils.isEmpty(msg) ? getPermissionMessage(permissions[0]) : msg);
        permissionDialog.setPermissionIcon(getPermissionIcon(permissions[0]));
        permissionDialog.setCancelable(false);
        // 取消
        permissionDialog.setNegativeButton("暂不", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                Logger.i(TAG, "onClick  :  cannel");
                if (onClickListener != null) {
                    onClickListener.canceled();
                }
            }
        });
        // 授权
        permissionDialog.setPositiveButton("授权", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                Logger.i(TAG, "onClick  :  allow");
                if (requestPermissions_Type_1 == actionType) {
                    Logger.i(TAG, "onClick  :  ti settings");
                    ToAppSettings(activity);
                    if (onClickListener != null) {
                        onClickListener.requested(true);
                    }
                } else {
                    boolean requested = requestPermissions(activity, requestCode, permissions);
                    Logger.i(TAG, "onClick  : " + requested);
                    if (onClickListener != null) {
                        onClickListener.requested(requested);
                    }
                }

            }
        });
        return permissionDialog;
    }


    public static void ToAppSettings(Activity context) {
        try {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts("package", context.getPackageName(), null);
            intent.setData(uri);
            //context.startActivity(intent);
            context.startActivityForResult(intent, PerMissionSetting_Request_Code);
        } catch (Exception e) {

        }
    }

    public static boolean requestPermissions(Activity activity, int requesetCode, String... permissions) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { // Android6.0+
            ActivityCompat.requestPermissions(activity, permissions, requesetCode);
            return true;
        }
        return false;
    }


    public static String getPermissionTitle(String permission) {
        if (READ_CONTACTS_PERMISSION.equals(permission)) {
            return "通讯录";
        } else if (CAMERA_PERMISSION.equals(permission)) {
            return "照相机";
        } else if (WRITE_EXTERNAL_STORAGE.equals(permission)) {
            return "读写文件";
        } else if (COARSE_LOCATION.equals(permission) || FINE_LOCATION.equals(permission)) {
            return "获取地址权限";
        }
        return null;
    }

    public static String getPermissionMessage(String permission) {
        if (READ_CONTACTS_PERMISSION.equals(permission)) {
            return "允许获取通讯录权限后，可以直接从通讯录选取联系人号码";
        } else if (CAMERA_PERMISSION.equals(permission)) {
            return "扫描二维码或拍摄照片需获取相机拍照权限";
        } else if (WRITE_EXTERNAL_STORAGE.equals(permission)) {
            return "允许应用程序读写文件";
        } else if (COARSE_LOCATION.equals(permission) || FINE_LOCATION.equals(permission)) {
            return "授权使用你的地理位置信息，可以更好地发现身边优质商品";
        }
        return null;
    }

    private static int getPermissionIcon(String permission) {
        if (READ_CONTACTS_PERMISSION.equals(permission)) {
            return R.drawable.ic_permission_contacts;
        } else if (CAMERA_PERMISSION.equals(permission)) {
            return R.drawable.ic_camera;
        } else if (COARSE_LOCATION.equals(permission) || FINE_LOCATION.equals(permission)) {
            return R.drawable.iv_location_permiss;
        }
        return -1;
    }

    public interface OnRequestPermissionListener {

        /**
         * 确认授权
         *
         * @param isRequested Android6.0+ isRequested = true，否则isRequested = false
         */
        void requested(boolean isRequested);

        /**
         * 取消授权
         */
        void canceled();
    }
}
