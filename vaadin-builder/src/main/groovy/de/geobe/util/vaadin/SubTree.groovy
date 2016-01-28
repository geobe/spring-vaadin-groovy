package de.geobe.util.vaadin

import com.vaadin.ui.Component
import com.vaadin.ui.UI

/**
 * A base class for building Vaadin component subtrees with VaadinBuilder
 * @author georg beier
 */
abstract class SubTree {
    /**
     * builder is configured here and used in subclasses
     */
    protected VaadinBuilder vaadin
    /**
     * make prefix available for accessing components in the subclasses
     */
    protected String subkeyPrefix
    protected def uiComponents

    /**
     * set component prefix in builder, delegate building subtree to subclass
     * and reset prefix afterwards.
     * @param builder   VaadinBuilder instance that builds the whole GUI
     * @param componentPrefix   name prefix for components in this subtree
     * @return  topmost component (i.e. root) of this subtree
     */
    Component buildSubtree(VaadinBuilder builder, String componentPrefix) {
        this.vaadin = builder
        def oldKeyPrefix = builder.getKeyPrefix()
        subkeyPrefix = oldKeyPrefix + componentPrefix
        builder.setKeyPrefix subkeyPrefix
        Component component = build()
        builder.setKeyPrefix oldKeyPrefix
        component
    }

    /**
     * build component subtree.
     * @return  topmost component (i.e. root) of subtree
     */
    abstract Component build()

    /**
     * initialize subtree components. should be called after whole component tree is built.
     * call sequence of different subtrees may be important.
     * @param value various parameters needed for initialization
     */
    void init(Object... value) {}

    protected setComponentValue(String id, Object value) {
        uiComponents."${subkeyPrefix + id}".value = value.toString()
    }

    protected setComponentValue(String id, Boolean value) {
        uiComponents."${subkeyPrefix + id}".value = value
    }

    protected UI getVaadinUi(Component c) {
        Component parent = c?.parent
        if(parent instanceof UI) {
            parent
        } else {
            getVaadinUi(parent)
        }
    }

    protected Long longFrom(String val) {
        try {
            new Long(val)
        } catch(NumberFormatException e) {
            0L
        }
    }
}