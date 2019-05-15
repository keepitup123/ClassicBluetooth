package com.example.bluetoothdemo.bluetooth;


public class EncodeUtil {
    // 将字节数组转化为16进制字符串，确定长度
    public static String bytesToHexString(byte[] bytes, int a) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < a; i++) {
            String hexString = Integer.toHexString(bytes[i] & 0xFF);// 将高24位置0
            if (hexString.length() == 1) {
                hexString = '0' + hexString;
            }
            result.append(hexString.toUpperCase());
        }
        return result.toString();
    }

    // 将字节数组转化为16进制字符串，不确定长度
    public static String Bytes2HexString(byte[] b) {
        StringBuilder ret = new StringBuilder();
        for (byte aB : b) {
            String hex = Integer.toHexString(aB & 0xFF);// 将高24位置0
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            ret.append(hex.toUpperCase());
        }
        return ret.toString();
    }

    // 将16进制字符串转化为字节数组
    public static byte[] hexStr2Bytes(String paramString) {
        int i = paramString.length() / 2;

        byte[] arrayOfByte = new byte[i];
        int j = 0;
        while (true) {
            if (j >= i)
                return arrayOfByte;
            int k = 1 + j * 2;
            int l = k + 1;
            arrayOfByte[j] = (byte) (0xFF & Integer.decode(
                    "0x" + paramString.substring(j * 2, k) + paramString.substring(k, l)));
            ++j;
        }
    }

    /**
     * 转化固定长度的字节数组为ascii表对应的字符
     *
     * @param bytes  初始接收到的字节数组
     * @param length 固定长度
     * @return 返回字符串  可以直接显示
     */
    public static String bytesToCharStr(byte[] bytes, int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            char c = (char) bytes[i];
            sb.append(c);
        }
        return sb.toString();
    }

    /**
     * 将ascii字符转化为十六进制显示
     * 记得最后把它转为String  是为了便于显示，传输之前还是要转化为字节数组的
     *
     * @param charStr ascii字符
     * @return hex字符
     */
    public static String charStr2hexStr(String charStr) {
        String result = "";
        char c;
        for (int i = 0; i < charStr.length(); i++) {
            c = charStr.charAt(i);
            String hex = Integer.toHexString(c);// 将高24位置0
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            result += hex.toUpperCase();
        }
        return result;

    }

}
