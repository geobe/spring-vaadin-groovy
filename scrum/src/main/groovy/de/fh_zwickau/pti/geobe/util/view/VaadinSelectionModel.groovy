package de.fh_zwickau.pti.geobe.util.view

/**
 * Default implementation and delegate for VaadinSelectionModels Listeners.<br>
 *     Supports different kinds of objects within one selection component, e.g. a Tree.
 *     We suppose that the relevant item ids consíst of a Map with a key (e.g. a class name)
 *     and a value (e.g. an object id from the persistamt storage).
 *     ListenersForKey subscribe for a certain key and are notified with the value when an
 *     item with "their" key is selected. KeyListeners get notified with the key when any
 *     item with a Map-id is selected.
 * Created by georg beier on 17.11.2015.
 */
class VaadinSelectionModel {
    private Map<String, Set<VaadinSelectionListener>> listeners = new LinkedHashMap<>()
    private Set<VaadinSelectionKeyListener> keyListeners = new LinkedHashSet<>()
    private Set<VaadinTreeRootChangeListener> rootChangeListeners = new LinkedHashSet<>()

    public void addListenerForKey(VaadinSelectionListener l, String key) {
        if (!listeners.containsKey(key)) {
            listeners[key] = new LinkedHashSet<VaadinSelectionListener>()
        }
        listeners[key].add(l)
    }

    public void removeListenerForKey(VaadinSelectionListener l, String key) {
        listeners[key]?.remove(l)
    }

    public void addKeyListener(VaadinSelectionKeyListener keyListener) {
        keyListeners.add(keyListener)
    }

    public void removeKeyListener(VaadinSelectionKeyListener keyListener) {
        keyListeners.remove(keyListener)
    }

    public void addRootChangeListener(VaadinTreeRootChangeListener changeListener) {
        rootChangeListeners.add(changeListener)
    }

    public void removeRootChangeListener(VaadinTreeRootChangeListener changeListener) {
        rootChangeListeners.remove(changeListener)
    }

    public void notifyChange(Map<String, Serializable> rawEvent) {
            listeners[rawEvent['type']].each { it.onItemSelected(rawEvent) }
            keyListeners.each { it.onItemKeySelected(rawEvent) }
    }

    public void notifyRootChange(Map<String, Serializable> rawEvent) {
        rootChangeListeners.each { it.onRootChanged(rawEvent) }
    }
}
