package com.ownwn.server.java.lang.replacement.test;

import com.ownwn.server.java.lang.replacement.*;

public class Tests {

    static void main() {
        listTest();
        hashMapTest();
        hashSetTest();
    }

    static void hashSetTest() {
        Set<Integer> set = new HashSet<>();
        java.util.Set<Integer> set2 = new java.util.HashSet<>();


        for (int i = 0; i < 1000000; i++) {
            int random = (int) (100 * Math.random());

            set.add(random);
            set2.add(random);

            if (set.size() != set2.size()) {
                throw new Error("size");
            }

            if (!set.equals(set2)) {
                    throw new Error("equals\n" + set + " " + set2);
            }

            if (Math.random() < 0.5) {
                set.remove(random+1);
                set2.remove(random+1);
            }

        }
    }

    static void listTest() {
        List<Integer> l = new ArrayList<>();
        java.util.List<Integer> l2 = new java.util.ArrayList<>();

        for (int i = 0; i < 10000; i++) {
            int random = (int) (100 * Math.random());

            l.add(random);
            l2.add(random);

            if (l.size() != l2.size()) {
                throw new Error("size");
            }

            if (!l.equals(l2)) {
                throw new Error("equals");
            }

            if (Math.random() < 0.5) {
                int index = (int) (Math.random() * l.size());
                l.remove(index);
                l2.remove(index);
            }
        }

    }

    static void hashMapTest() {
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
