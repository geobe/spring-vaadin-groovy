package de.geobe.util.association;

import java.util.Collection;

/**
 * @author georg beier
 * <p>
 * Interface zur Implementierung von zu-N und zu-1 Assoziationen
 */
public interface IToAny<THERE> {
    /**
     * füge Element zur Assoziation hinzu
     *
     * @param other neues Element
     */
    void add(THERE other);

    /**
     * Entferne Element aus Assoziation
     *
     * @param other vorhandenes Element
     */
    void remove(THERE other);

    /**
     * Hole ein Element der Assoziation
     *
     * @return ein Element, wenn vorhanden, sonst null
     */
    THERE getOne();

    /**
     * Hole eine Collection aller Elemente der Assoziation
     *
     * @return alle Elemente
     */
    Collection<THERE> getAll();

    /**
     * Entferne alle Elemente aus der Assoziation
     */
    void removeAll();

    @FunctionalInterface
    interface IGet<T> {
        T get();
    }

    @FunctionalInterface
    interface ISet<T> {
        void set(T value);
    }
}
