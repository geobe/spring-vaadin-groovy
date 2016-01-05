package de.geobe.util.association;

/**
 * Created by georg beier on 15.10.2015.
 * Funktionales Interface, dass den Zugriff auf das andere Ende der Assoziation mit Lamda-Expression ermöglicht
 */
@FunctionalInterface
public interface IGetOther<HERE, THERE> {
    /**
     * Zugriff auf die andere Seite der Assoziation
     *
     * @return das andere Assoziationsende
     */
    IToAny<HERE> get(THERE t);
}
