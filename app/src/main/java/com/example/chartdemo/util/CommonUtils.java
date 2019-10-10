package com.example.chartdemo.util;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.text.TextUtils;
import android.text.format.Time;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ViewConfiguration;

import com.example.chartdemo.MyApp;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Created by cuihaiyang1 on 2017/8/17.
 */

public class CommonUtils {
    private static int mWidth;
    private static int mHeight;
    /**
     * 设备分辨率宽度
     * @return
     */
    public static int getScreenWidth(){
        if(mWidth==0){
            Resources resources = MyApp.getAppContext().getResources();
            DisplayMetrics dm = resources.getDisplayMetrics();
            mWidth=dm.widthPixels;
        }
        return mWidth;
    }

    /**
     * 设备分辨率高度,加虚拟键高度
     * @return
     */
    public static int getScreenHeight(){
        if(mHeight==0) {
            Resources resources = MyApp.getAppContext().getResources();
            DisplayMetrics dm = resources.getDisplayMetrics();
            mHeight = dm.heightPixels+getNavigationBarHeight(MyApp.getAppContext());
        }
        return mHeight;
    }
    /**
     * 设备分辨率高度,无虚拟键高度
     * @return
     */
    public static int getScreenHeightNoBar(){
        if(mHeight==0) {
            Resources resources = MyApp.getAppContext().getResources();
            DisplayMetrics dm = resources.getDisplayMetrics();
            mHeight = dm.heightPixels+getNavigationBarHeight(MyApp.getAppContext());
        }
        return mHeight;
    }
    //获取虚拟按键的高度
    public static int getNavigationBarHeight(Context context) {
        int result = 0;
        if (hasNavBar(context)) {
            Resources res = context.getResources();
            int resourceId = res.getIdentifier("navigation_bar_height", "dimen", "android");
            if (resourceId > 0) {
                result = res.getDimensionPixelSize(resourceId);
            }
        }
        return result;
    }

    /**
     * 检查是否存在虚拟按键栏
     *
     * @param context
     * @return
     */
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public static boolean hasNavBar(Context context) {
        Resources res = context.getResources();
        int resourceId = res.getIdentifier("config_showNavigationBar", "bool", "android");
        if (resourceId != 0) {
            boolean hasNav = res.getBoolean(resourceId);
            // check override flag
            String sNavBarOverride = getNavBarOverride();
            if ("1".equals(sNavBarOverride)) {
                hasNav = false;
            } else if ("0".equals(sNavBarOverride)) {
                hasNav = true;
            }
            return hasNav;
        } else { // fallback
            return !ViewConfiguration.get(context).hasPermanentMenuKey();
        }
    }

    /**
     * 判断虚拟按键栏是否重写
     *
     * @return
     */
    private static String getNavBarOverride() {
        String sNavBarOverride = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            try {
                Class c = Class.forName("android.os.SystemProperties");
                Method m = c.getDeclaredMethod("get", String.class);
                m.setAccessible(true);
                sNavBarOverride = (String) m.invoke(null, "qemu.hw.mainkeys");
            } catch (Throwable e) {
            }
        }
        return sNavBarOverride;
    }

    public static float getDimen(int d){
        Resources resources = MyApp.getAppContext().getResources();
        return resources.getDimension(d);
    }

    public static int getColor(int d) {
        Resources resources = MyApp.getAppContext().getResources();
        return resources.getColor(d);
    }
    public static String getString(int d) {
        Resources resources = MyApp.getAppContext().getResources();
        return resources.getString(d);
    }
    /**
     *  从assets目录中复制整个文件夹内容
     *  @param  oldPath  String  原文件路径  如：/aa
     *  @param  newPath  String  复制后路径  如：xx:/bb/cc
     */
    public static boolean copyFilesFassets(String oldPath, String newPath) {
        boolean isScuess = true;
        InputStream is = null;
        FileOutputStream fos = null;
        try {
            String fileNames[] = MyApp.getAppContext().getAssets().list(oldPath);//获取assets目录下的所有文件及目录名
            if (fileNames.length > 0) {//如果是目录
                File file = new File(newPath);
                file.mkdirs();//如果文件夹不存在，则递归
                for (String fileName : fileNames) {
                    isScuess = isScuess&&copyFilesFassets(oldPath + "/" + fileName,newPath+"/"+fileName);
                }
            } else if(!new File(newPath).exists()){//如果是文件
                is = MyApp.getAppContext().getAssets().open(oldPath);
                fos = new FileOutputStream(new File(newPath));
                byte[] buffer = new byte[1024];
                int byteCount=0;
                while((byteCount=is.read(buffer))!=-1) {//循环从输入流读取 buffer字节
                    fos.write(buffer, 0, byteCount);//将读取的输入流写入到输出流
                }
                fos.flush();//刷新缓冲区
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            //如果捕捉到错误则通知UI线程
            isScuess =false;
        }finally {
            try {
                if(is!=null) {
                    is.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if(fos!=null){
                    fos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return isScuess;
    }

    //检查网络是否连接
    public static boolean isNetworkConnected() {
        if (MyApp.getAppContext() != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) MyApp.getAppContext()
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            if(mConnectivityManager!=null) {
                NetworkInfo mNetworkInfo = mConnectivityManager
                        .getActiveNetworkInfo();
                if (mNetworkInfo != null) {
                    return mNetworkInfo.isAvailable();
                }
            }
        }
        return false;
    }

    public static int getStatusBarHeight() {
        int result = 0;
        if (MyApp.getAppContext() != null) {
            int resourceId = MyApp.getAppContext().getResources().getIdentifier("status_bar_height", "dimen", "android");
            if (resourceId > 0) {
                result = MyApp.getAppContext().getResources().getDimensionPixelSize(resourceId);
            }
        }
        return result;
    }

    public static boolean isSdk16High() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN;
    }

    private static String IMEI = "";

    /**
     * 获取ip地址
     * @return
     */
    public static String getHostIP() {

        String hostIp = null;
        try {
            Enumeration nis = NetworkInterface.getNetworkInterfaces();
            InetAddress ia = null;
            while (nis.hasMoreElements()) {
                NetworkInterface ni = (NetworkInterface) nis.nextElement();
                Enumeration<InetAddress> ias = ni.getInetAddresses();
                while (ias.hasMoreElements()) {
                    ia = ias.nextElement();
                    if (ia instanceof Inet6Address) {
                        continue;// skip ipv6
                    }
                    String ip = ia.getHostAddress();
                    if (!"127.0.0.1".equals(ip)) {
                        hostIp = ia.getHostAddress();
                        break;
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return hostIp;

    }

    /**
     * 判断当前系统时间是否在指定时间的范围内
     *
     * @param beginHour
     * 开始小时，例如22
     * @param beginMin
     * 开始小时的分钟数，例如30
     * @param endHour
     * 结束小时，例如 8
     * @param endMin
     * 结束小时的分钟数，例如0
     * @return true表示在范围内，否则false
     */
    public static boolean isCurrentInTimeScope(int beginHour, int beginMin, int endHour, int endMin) {
        boolean result = false;
        final long aDayInMillis = 1000 * 60 * 60 * 24;
        final long currentTimeMillis = System.currentTimeMillis();

        Time now = new Time();
        now.set(currentTimeMillis);

        Time startTime = new Time();
        startTime.set(currentTimeMillis);
        startTime.hour = beginHour;
        startTime.minute = beginMin;

        Time endTime = new Time();
        endTime.set(currentTimeMillis);
        endTime.hour = endHour;
        endTime.minute = endMin;

        if (!startTime.before(endTime)) {
// 跨天的特殊情况（比如22:00-8:00）
            startTime.set(startTime.toMillis(true) - aDayInMillis);
            result = !now.before(startTime) && !now.after(endTime); // startTime <= now <= endTime
            Time startTimeInThisDay = new Time();
            startTimeInThisDay.set(startTime.toMillis(true) + aDayInMillis);
            if (!now.before(startTimeInThisDay)) {
                result = true;
            }
        } else {
// 普通情况(比如 8:00 - 14:00)
            result = !now.before(startTime) && !now.after(endTime); // startTime <= now <= endTime
        }
        return result;
    }

    public static boolean isRightIP(String ip){
        String num = "(25[0-5]|2[0-4]\\d|[0-1]\\d{2}|[1-9]?\\d)";
        String regex = "^" + num + "\\." + num + "\\." + num + "\\." + num;
        return !TextUtils.isEmpty(ip)&&(match(regex, ip));
    }
    public static boolean isRightIPAndPort(String ip){
        String num = "(25[0-5]|2[0-4]\\d|[0-1]\\d{2}|[1-9]?\\d)";
        String port = "([0-9]|[1-9]\\d|[1-9]\\d{2}|[1-9]\\d{3}|[1-5]\\d{4}|6[0-4]\\d{3}|65[0-4]\\d{2}|655[0-2]\\d|6553[0-5])";
        String regex = "^" + num + "\\." + num + "\\." + num + "\\." + num + "\\:"+port+"$";
        String http = "^([hH][tT]{2}[pP]://|[hH][tT]{2}[pP][sS]://)(([A-Za-z0-9-~]+).)+([A-Za-z0-9-~\\/])+$";
        return !TextUtils.isEmpty(ip)&&(match(regex, ip)||match(http,ip));
    }

    /**
     * @param regex
     * 正则表达式字符串
     * @param str
     * 要匹配的字符串
     * @return 如果str 符合 regex的正则表达式格式,返回true, 否则返回 false;
     */
    private static boolean match(String regex, String str) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(str);
        return matcher.matches();
    }

    /**
     * 利用正则表达式判断字符串是否是数字
     * @param str
     * @return
     */
    public static boolean isNumeric(String str){
        Pattern pattern = Pattern.compile("[0-9]*");
        Matcher isNum = pattern.matcher(str);
        return isNum.matches();
    }


    /**
     * 判断2个对象是否相等
     *
     * @param a Object a
     * @param b Object b
     * @return isEqual
     */
    public static boolean isEquals(Object a, Object b) {
        return (a == null) ? (b == null) : a.equals(b);
    }

    public static String replaceBlank(String str) {
        if (!TextUtils.isEmpty(str)) {
            Pattern p = Pattern.compile("\\s*|\t|\r|\n");
            Matcher m = p.matcher(str);
            return m.replaceAll("");
        }else {
            return str;
        }
    }
    public static String getFormatPrice(String price){
        if (!TextUtils.isEmpty(price)&&price.contains(".")) {
            price = price.replaceAll("0+?$", "");//去掉多余的0
            price = price.replaceAll("[.]$", "");//如最后一位是.则去掉
        }
        return price;
    }

    public static int getRealSizeWithWidth(float size){
        return (int) (size/750f*getScreenWidth());
    }
    public static int getRealSizeWithHeight(float size){
        return (int) (size/1334*getScreenHeight());
    }

    public static int px2dip(float pxValue) {
        final float scale = MyApp.getAppContext().getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    public static int dip2px(float dipValue) {
        final float scale = MyApp.getAppContext().getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

    /**
     * 获取应用包名
     * @return
     */
    public static String getPackageName() {
        try {
            PackageManager packageManager = MyApp.getAppContext().getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(
                    MyApp.getAppContext().getPackageName(), 0);
            return packageInfo.packageName;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 获取AppcitionId
     * @return
     */
    public static String getAppcitionId() {
        String name = MyApp.getAppContext().getPackageName();
        if(TextUtils.isEmpty(name)){
            return getPackageName();
        }else{
            return name;
        }
    }

    public static String getJson(Context context, String fileName) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            AssetManager assetManager = context.getAssets();
            InputStream inputStream = assetManager.open(fileName);
            BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
            byte[] buffer = new byte[1024];
            int len;
            while ((len = bufferedInputStream.read(buffer)) != -1) {
                baos.write(buffer, 0, len);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                baos.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return baos.toString();
    }

}
