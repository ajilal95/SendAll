package com.aj.sendall.db.converters;

import java.util.AbstractList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * Created by ajilal on 8/5/17.
 */

public class OnDemandConverterList<F, T> extends AbstractList {

    private List<F> baseList;
    private EntityConverter<F, T> entityConverter;

    public OnDemandConverterList(List<F> fromList, EntityConverter<F, T> entityConverter){
        this.baseList = fromList;
        this.entityConverter = entityConverter;
    }


    public boolean add(Object e) {
        baseList.add((F)e);
        return true;
    }

    public T get(int index){
        return entityConverter.convert(baseList.get(index));
    }

    public void add(int index, Object element) {
        baseList.add(index, (F)element);
    }

    public F remove(int index) {
        return baseList.remove(index);
    }

    public int indexOf(Object o) {
        return baseList.indexOf(0);
    }

    public int lastIndexOf(Object o) {
        return baseList.lastIndexOf(o);
    }

    public void clear() {
        baseList.clear();
    }

    public boolean addAll(int index, Collection c) {
        return baseList.addAll(index, c);
    }

    public Iterator<T> iterator() {
        return new Itr();
    }

    public ListIterator<T> listIterator() {
        return listIterator(0);
    }

    private class Itr implements Iterator<T> {
        Iterator<F> baseIterator = baseList.iterator();

        public boolean hasNext() {
            return baseIterator.hasNext();
        }

        public T next() {
            return entityConverter.convert(baseIterator.next());
        }

        public void remove() {
            baseIterator.remove();
        }
    }

    public OnDemandConverterList<F, T> subList(int fromIndex, int toIndex) {
        return new OnDemandConverterList<>(baseList.subList(fromIndex, toIndex), entityConverter);
    }

    public int size(){
        return baseList.size();
    }

    public interface EntityConverter<F, T>{
        T convert(F fromEntity);
    }
}
