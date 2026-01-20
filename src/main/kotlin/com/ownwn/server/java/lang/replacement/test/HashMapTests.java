package com.ownwn.server.java.lang.replacement.test;

import com.ownwn.server.java.lang.replacement.HashMap;
import com.ownwn.server.java.lang.replacement.Map;

public class HashMapTests {

    static void main() {
        Map<Integer, Integer> map = new HashMap<>();
        java.util.Map<Integer, Integer> map2 = new java.util.HashMap<>();


        for (int i = 0; i < 1000000; i++) {
            int randomKey = (int) (100 * Math.random());
            int randomValue = (int) (100 * Math.random());
            map.put(randomKey, randomValue);
            map2.put(randomKey, randomValue);

            if (map.containsKey(randomKey) != map2.containsKey(randomKey)) throw new Error();
            if (map.containsValue(randomValue) != map2.containsValue(randomValue)) throw new Error();

            var r1 = map.remove(randomKey);
            var r2 = map2.remove(randomKey);
            if (!r1.equals(r2)) {
                throw new Error("remove " + r1 + " " + r2);
            }
//            System.out.println(map.size() + " " + map2.size());
            if (map.size() != map2.size()) {
                throw new Error(map.size() + " " + map2.size() + "\n" + "\n" + map + "\n" + map2);
            }
//            if (map.toString().intern() != map2.toString().intern()) throw new Error("\n" + map + "\n" + map2);
        }
    }
}
