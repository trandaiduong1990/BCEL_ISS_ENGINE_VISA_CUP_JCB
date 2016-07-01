// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   CTFUtils.java

package vn.com.tivn.ctf;


public class CTFUtils
{

    public CTFUtils()
    {
    }

    public static boolean checkLuhn(String cardNumber)
    {
        int m = 2;
        int sum = 0;
        int n = 0;
        for(int i = cardNumber.length() - 1; i >= 0; i--)
            if(cardNumber.charAt(i) >= '0' && cardNumber.charAt(i) <= '9')
            {
                n = cardNumber.charAt(i) - 48;
                if(n * m >= 10)
                    sum += (m * n) / 10 + (m * n) % 10;
                else
                    sum += m * n;
            } else
            {
                return false;
            }

        return (10 - sum % 10) % 10 == cardNumber.charAt(cardNumber.length() - 1) - 48;
    }

    public static String getCheckDigit(String number)
    {
        char digits[] = number.toCharArray();
        int sum = 0;
        int multiplier = 1;
        for(int i = digits.length - 1; i >= 0; i--)
        {
            int digit;
            try
            {
                digit = Integer.parseInt(String.valueOf(digits[i]));
            }
            catch(Exception e)
            {
                String s = "-1";
                return s;
            }
            if(multiplier == 1)
                multiplier = 2;
            else
            if(multiplier == 2)
                multiplier = 1;
            int multiple = digit * multiplier;
            sum = sum + multiple % 10 + multiple / 10;
        }

        int chk = 10 - sum % 10;
        if(chk == 10)
            chk = 0;
        return "" + chk;
    }

    public static String lpad(String src, String filler, int length)
    {
        for(; src.length() < length; src = filler + src);
        return src;
    }

    public static String rpad(String src, String filler, int length)
    {
        for(; src.length() < length; src = src + filler);
        return src;
    }
}
