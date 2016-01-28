package de.geobe.util.association;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

/**
 * Implementiert den Zugriff auf die Assoziation und stellt sicher, dass bidirektionale
 * Assoziationen in beiden Richtungen konsistent gehalten werden
 * <p>
 * @author georg beier
 */
public class ToMany<HERE, THERE> implements IToAny<THERE> {
    private IGet<Collection<THERE>> ownSide;
    private HERE self;
    private IGetOther<HERE, THERE> otherSide;

    /**
     * Erzeuge Zugriffsobjekt für zu-N Assoziation
     *
     * @param ownSide
     *         Zugriff auf die Collection der eigenen Seite der Assoziation(Lambda-Ausdruck)
     * @param self
     *         das Objekt, dem das Assoziationsende gehört
     * @param otherSide
     *         Zugriffsfunktion auf anderes Ende der Assoziation (Lambda-Ausdruck)
     */
    public ToMany(IGet<Collection<THERE>> ownSide, HERE self, IGetOther<HERE, THERE> otherSide) {
        this.ownSide = ownSide;
        this.self = self;
        this.otherSide = otherSide;
    }

    /**
     * füge Element zur Assoziation hinzu
     *
     * @param other neues Element
     */
    @Override
    public void add(THERE other) {
        if (!ownSide.get().contains(other)) {
            ownSide.get().add(other);
            if (otherSide != null)
                otherSide.get(other).add(self);
        }
    }

    /**
     * Entferne Element aus Assoziation
     *
     * @param other vorhandenes Element
     */
    @Override
    public void remove(THERE other) {
        if (ownSide.get().contains(other)) {
            ownSide.get().remove(other);
            if (otherSide != null)
                otherSide.get(other).remove(self);
        }

    }

    /**
     * Hole ein Element der Assoziation
     *
     * @return ein Element, wenn vorhanden, sonst null
     */
    @Override
    public THERE getOne() {
        if (ownSide.get().size() > 0)
            return ownSide.get().iterator().next();
        else
            return null;
    }

    /**
     * Hole eine Collection aller Elemente der Assoziation
     *
     * @return alle Elemente
     */
    @Override
    public Collection<THERE> getAll() {
        return Collections.unmodifiableCollection(ownSide.get());
    }

    /**
     * Entferne alle Elemente aus der Assoziation
     */
    @Override
    public void removeAll() {
        Collection<THERE> others = new HashSet<>(ownSide.get());
        ownSide.get().clear();
        if(otherSide != null) {
            for (THERE other : others) {
                otherSide.get(other).remove(self);
            }
        }
    }

}
