package utils;

public class CalUtils {
    public static int calFormatSpecifiers(String formatStr){
        int count = 0;
        for (int i = 0; i < formatStr.length(); i++) {
            if (formatStr.charAt(i) == '%' && i + 1 < formatStr.length()) {
                char nextChar = formatStr.charAt(i + 1);
                // 处理常见的格式化符
                if (nextChar == 'd' || nextChar == 'c') {
                    count++;
                    i++; // 跳过格式字符
                }
            }
        }
        return count;
    }
}
