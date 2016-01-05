package de.fh_zwickau.pti.geobe.util.view

/**
 * distribute Vaadin selection events
 * Created by georg beier on 17.11.2015.
 */
interface VaadinSelectionListener {
    /**
     * is fired when an entry of a selection component was selected
     *
     * @param event id of the selected element, normally containing its id and domain class key
     */
    void onItemSelected(Map<String, Serializable> event)
}