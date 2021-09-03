package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.graphics.Bitmap;
import android.util.Base64;
import android.util.Log;
import android.widget.ImageView;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.Bitmap.Config;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private ImageView imageView1, imageView2, imageView3;
    private Bitmap oldBitmap, encryptBitmap, decryptBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 原图
        imageView1 = (ImageView)findViewById(R.id.imageView1);
        oldBitmap = ((BitmapDrawable)imageView1.getDrawable()).getBitmap();  // 显示原Bitmap对象

        double key = keyValue(oldBitmap);          					// 密钥值

        // 加密
        encryptBitmap = processBitmap(oldBitmap, key);
        imageView2 = (ImageView)findViewById(R.id.imageView2);
        imageView2.setImageBitmap(encryptBitmap);                   // 显示加密后的Bitamp对象

//        String base64 = bitmapToBase64(oldBitmap);
        String base64 = bitmapToBase64(encryptBitmap);
        Bitmap change = base64ToBitmap(base64);

        // 解密
        decryptBitmap = processBitmap(change, key);
        imageView3 = (ImageView)findViewById(R.id.imageView3);
        imageView3.setImageBitmap(decryptBitmap);                  // 显示解密后的Bitamp对象
    }

    /*
    base64转字节数组
     */
    public static Bitmap base64ToBitmap(String src) {
        byte[] bytes = Base64.decode(src, Base64.NO_WRAP);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    // 根据位图上随机某个pixel的值求密钥值key
    protected static double keyValue(Bitmap bitmap) {
        int h = bitmap.getHeight();							// 位图高度
        int w = bitmap.getWidth();							// 位图宽度
        int y = new Random().nextInt(h + 1);        // 获得一个[0, h]区间内的随机整数
        int x = new Random().nextInt(w + 1);        // 获得一个[0, w]区间内的随机整数
        int p = Math.abs(bitmap.getPixel(x, y));
        return  (double) p / Math.pow(10, String.valueOf(p).length());
    }

    /**
     * bitmap转base64
     */
    public static String bitmapToBase64(Bitmap bitmap) {
        String result = null;
        ByteArrayOutputStream baos = null;
        try {
            if (bitmap != null) {
                baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);

                baos.flush();
                baos.close();
                Log.i("bitmap",bitmap.toString());
                byte[] bitmapBytes = baos.toByteArray();
                Log.i("bitmapBytes", Arrays.toString(bitmapBytes));
                result = Base64.encodeToString(bitmapBytes, Base64.NO_WRAP);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (baos != null) {
                    baos.flush();
                    baos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    // 加解密程序
    protected static Bitmap processBitmap(Bitmap bitmap, double key) {
        Bitmap newBitmap = bitmap.copy(Config.ARGB_8888, true);
        int h = bitmap.getHeight();							// 位图高度
        int w = bitmap.getWidth();							// 位图宽度
        int mArrayColorLength = h * w;
        int[] s = sequenceGenerator(key, mArrayColorLength);	// 设置Logistic混沌系统初始值和迭代次数
        int[] mArrayColor = new int[mArrayColorLength];
        int[] test = new int[mArrayColorLength];
        // 遍历位图
        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++) {
                int pixel = bitmap.getPixel(j, i);
                test[i*w+j] = pixel;
                mArrayColor[i*w +j] = pixel ^ s[i*w+j];			// 位图像素值与混沌序列值作异或
                newBitmap.setPixel(j, i, mArrayColor[i*w+j]);	// 为新位图赋值
            }
        }
        return newBitmap;
    }

    // 产生logistic混沌序列
    protected static int[] sequenceGenerator(double x0, int timeStep) {
        final double u = 3.9;                        // 控制参数u
        double[] x = new double[timeStep + 1000];

        x[0] = x0;
        // 迭代产生混沌序列，长度为 “timeStep+1000”
        for (int i = 0; i < timeStep + 999; i++) {
            x[i + 1] = u * x[i] * (1 - x[i]);       // 一维Logistic混沌系统
        }

        double[] new_x = Arrays.copyOfRange(x, 1000, timeStep + 1000);    // 去除前1000个混沌值，去除暂态效应
        int[] seq = new int[timeStep];
        // 处理混沌序列值
        for (int i = 0; i < timeStep; i++) {
            new_x[i] = new_x[i] * Math.pow(10, 4) - Math.floor(new_x[i] * Math.pow(10, 4));
            seq[i] = (int) Math.floor(Math.pow(10, 9) * new_x[i]);
        }
        return seq;
    }
}