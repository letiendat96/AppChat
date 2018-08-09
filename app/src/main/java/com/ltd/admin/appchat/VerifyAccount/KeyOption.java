package com.ltd.admin.appchat.VerifyAccount;

import android.util.Log;

import java.security.MessageDigest;
import java.util.Random;

import static java.lang.Integer.toHexString;

/**
 * Created by Admin on 4/27/2018.
 */

public class KeyOption {

    public long mod(long a,long  b){
        if(a>=0) return (a%b);
        else return(b-(Math.abs(a)%b));
    }

    public long chebyshev1(int beta, long x,long p){
        long t0 = 1;
        long t1 = x%p;
        long tn = 0;
        int i;
        for( i=2 ;i <= beta; i++) {
            tn = mod((2 * (x % p) * t1 - t0), p);
            t0 = t1;
            t1 = tn;
        }
        if(beta==0) return t0;
        else if(beta==1) return t1;
        else return tn;
    }

    // SHA 256 da test
    public static String SHA256(String base){
        try{
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(base.getBytes("UTF-8"));
            StringBuilder hexString = new StringBuilder();

            for (int i = 0; i < hash.length; i++) {
                String hex = toHexString(0xff & hash[i]);
                if(hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }
    // a - privateKey
    // b - publicKey khoi tao
    // index - so nguyen to chon
    public String ShowKey(){
        Random randomGenarator = new Random();
        int a = 12000 + randomGenarator.nextInt(6001);
        long b = 1996;
        long index = 9241;
        for (int i = 1; i <= a; i ++){
            b = chebyshev1(3, b, index);
        }
        StringBuilder bufferKey = new StringBuilder();
        bufferKey.append(a).append(1996).append(b);
        return  bufferKey.toString();
    }

    // Dung de tinh K1, K2 da test
    // aa - khóa bí mật
    // Tab - khóa công khai
    // a - random number
    // x - Key[1]
    // p - 9241
    // k mang 2 phan tu
    public void ComputeK( long aa, long Tab, int a, long x, long p, long [] k ){
        long s = aa - a;
        //long long k[2];
        int i;
        long  z = Tab;
        for(i=1;i<=s;i++)
            z = chebyshev1(3,z,p);
        k[0] = z%877;
        k[1] = z - k[0];
//        String cs = Long.toString(k[0]) + " " + Long.toString(k[1]);
//        Log.d("cKey", cs);

    }

    // Random da test
    public int Random( int p){
        int j = 0;
        // long -> int
        int [] a = new int [p];
        for (int i = 1; i < p-1 ; i++){
            if ( (p-1)%i != 0 ){
                a[j] = i;
                j = j+1;
            }
        }
        Random random = new Random();
        int k = random.nextInt(j);
        //String s = Integer.toString(a[k]);
        //Log.d("random", s);
        return a[k];
    }



}
