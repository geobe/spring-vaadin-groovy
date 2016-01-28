package de.geobe.util.association;

/**
 * @author georg beier
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
