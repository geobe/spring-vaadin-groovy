package de.fh_zwickau.pti.geobe.util.view

/**
 * In working with multi rooted tree views, it might be interesting to be notified
 * when a branch with a different root element is selected
 *
 * @author georg beier
 */
interface VaadinTreeRootChangeListener {
    /**
     * is fired when the newly selected component is in a differently rooted
     * branch of the tree
     *
     * @param event id of the new root element, normally containing its id and domain class key
     */
    void onRootChanged(Map<String, Serializable> event)
}