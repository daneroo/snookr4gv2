/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.snookr.transcode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author daniel
 *  Comparator orders List of T
 *  Mapper T -> G
 *  Expect the natural ordering on G to be rescected by Mapping
 */
public class Partitioner<T, G> {

    public final String name;
    final Comparator<T> comparator;
    final Grouper<T, G> grouper;

    public interface Grouper<T, G> {

        G key(T o);
    }

    public Partitioner(String name, Comparator<T> comparator, Grouper<T, G> grouper) {
        this.name = name;
        this.comparator = comparator;
        this.grouper = grouper;
    }

    public Map<G, List<T>> toMap(List<T> list) {
        //Copy the list, then sort, then Map
        List<T> listCopy = new ArrayList<T>(list);
        Collections.sort(listCopy, comparator);

        // LinkedHashMap preserves insertion order in iteration
        Map<G, List<T>> map = new LinkedHashMap<G, List<T>>();
        for (T t : listCopy) {
            G key = grouper.key(t);
            if (map.containsKey(key)) {
                map.get(key).add(t);
            } else {
                List<T> newgroup = new ArrayList<T>();
                newgroup.add(t);
                map.put(key, newgroup);
            }
        }
        return map;
    }

    public List<T> toList(Map<G, List<T>> map) {
        List list = new ArrayList<T>();
        for(List<T> parts:map.values()){
            list.addAll(parts);
        }
        return list;
    }

}
