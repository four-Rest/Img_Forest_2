package com.ll.demo.global.util;

public class Ut {
    public static class match {
        public static boolean isTrue(Boolean bool) {
            return bool != null && bool;
        }

        public static boolean isFalse(Boolean bool) {
            return bool != null && !bool;
        }
    }


    public static class str {

        public static String lcfirst(String str) {
            if (str == null || str.isEmpty()) {
                return str;
            }
            return str.substring(0, 1).toLowerCase() + str.substring(1);
        }

        public static boolean hasLength(String string) {
            return string != null && !string.trim().isEmpty();
        }

        public static boolean isBlank(String string) {
            return !hasLength(string);
        }
    }
}
