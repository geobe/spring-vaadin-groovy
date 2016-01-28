package de.geobe.util.association;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Implementiert den Zugriff auf die Assoziation und stellt sicher, dass bidirektionale
 * Assoziationen in beiden Richtungen konsistent gehalten werden
 * @author georg beier
 */
public class ToOne<HERE, THERE> implements IToAny<THERE> {
    private IGet<THERE> getOwn;
    private ISet<THERE> setOwn;
    private HERE self;
    private IGetOther<HERE, THERE> otherSide;

    /**
     * Erzeuge Zugriffsobjekt für zu-1 Assoziation
     *
     * @param getOwn
     *         lesender Zugriff auf die eigene Seite der Assoziation(Lambda-Ausdruck)
     * @param setOwn
     *         schreibender Zugriff auf die eigene Seite der Assoziation(Lambda-Ausdruck)
     * @param self
     *         das Objekt, dem das Assoziationsende gehört
     * @param otherSide
     *         Zugriffsfunktion auf anderes Ende der Assoziation (Lambda-Ausdruck)
     */
    public ToOne(IGet<THERE> getOwn, ISet<THERE> setOwn, HERE self, IGetOther<HERE, THERE> otherSide) {
        this.getOwn = getOwn;
        this.setOwn = setOwn;
        this.self = self;
        this.otherSide = otherSide;
    }

    /**
     * füge Element zur Assoziation hinzu
     *
     * @param other
     *         neues Element
     */
    @Override
    public void add(THERE other) {
        if (other == getOwn.get())
            return;
        if (otherSide != null) {
            THERE oldref = getOwn.get();
            setOwn.set(other);
            if (oldref != null) {
                otherSide.get(oldref).remove(self);
            }
            if(other != null) {
                otherSide.get(other).add(self);
            }
        } else {
            setOwn.set(other);
        }
    }

    /**
     * Entferne Element aus Assoziation
     *
     * @param other
     *         vorhandenes Element
     */
    @Override
    public void remove(THERE other) {
        if (getOwn.get() == null || other != getOwn.get())
            return;
        if (otherSide != null) {
            otherSide.get(other).remove(self);
        }
        setOwn.set(null);
    }

    /**
     * Hole ein Element der Assoziation
     *
     * @return ein Element, wenn vorhanden, sonst null
     */
    @Override
    public THERE getOne() {
        return getOwn.get();
    }

    /**
     * Hole eine Collection aller Elemente der Assoziation
     *
     * @return das Eine als Alles
     */
    @Override
    public Collection<THERE> getAll() {
        ArrayList<THERE> ret = new ArrayList<>(1);
        if(getOne() != null) {
            ret.add(getOne());
        }
        return ret;
    }

    /**
     * Entferne alle Elemente aus der Assoziation
     */
    @Override
    public void removeAll() {
        if(otherSide != null && getOwn.get() != null) {
            otherSide.get(getOwn.get()).remove(self);
        } else {
            setOwn.set(null);
        }
    }
}
