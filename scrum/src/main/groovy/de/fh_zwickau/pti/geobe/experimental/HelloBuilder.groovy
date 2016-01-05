package de.fh_zwickau.pti.geobe.experimental

import com.vaadin.annotations.Theme
import com.vaadin.event.ItemClickEvent
import com.vaadin.server.VaadinRequest
import com.vaadin.spring.annotation.SpringUI
import com.vaadin.ui.Component
import com.vaadin.ui.UI
import de.geobe.util.vaadin.VaadinBuilder

/**
 * Created by georg beier on 12.11.2015.
 * Beispiel 1
 */
@SpringUI(path = "builddemo")
@Theme("valo")
class HelloBuilder extends UI {
    def VaadinBuilder vaadin
    def widgets = [:]

    @Override
    protected void init(VaadinRequest request) {
        setContent(initBuilder())
    }

    // Variablen müssen vor der Verwendung deklariert sein, daher müssen listener closures
    // vor initBuilder definiert werden

    def treeClick = {ItemClickEvent e ->
        Component c = (Component) widgets[e.itemId]
        widgets['label'].value = e.itemId
        c.focus()
    }

    Component initBuilder() {
        vaadin = new VaadinBuilder()

        Component root = vaadin.hsplit (splitPosition: 25.0 ){
            panel([uikey: 'treepanel']) {
                tree('Komponentenbaum', [itemClickListener: treeClick])
            }
            vlayout {
                label('ein Label')
                text('Ein Textfeld', [uikey: 'exampleText', value: 'Beispieltext'])
                textarea('der Komponentenbaum', [uikey: 'comptree', value: 'tree', width: '44em', rows: 10])
            }
        }
        widgets = vaadin.uiComponents
        def wtree = vaadin.toString()
        def wtextarea = widgets.comptree
        wtextarea.setValue(wtree)
        widgets.tree.addItems(widgets.keySet())
        root
    }
}
