package de.fh_zwickau.pti.geobe.experimental

import com.vaadin.annotations.Theme
import com.vaadin.server.VaadinRequest
import com.vaadin.spring.annotation.SpringUI
import com.vaadin.ui.Component
import com.vaadin.ui.MenuBar.MenuItem
import com.vaadin.ui.Notification
import com.vaadin.ui.UI
import com.vaadin.ui.Window
import de.geobe.util.vaadin.VaadinBuilder
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.ResourceLoader

import static VaadinBuilder.C
import static VaadinBuilder.F

/**
 * Created by georg beier on 12.11.2015.
 * Beispiel 1
 */
@Slf4j
@SpringUI(path = "builddemo2")
@Theme("valo")
class HelloBuilder2 extends UI {

    @Autowired
    private ResourceLoader resourceLoader;

    def VaadinBuilder vaadin, subwin
    def widgets = [:], winwid = [:]
    Window subwindow

    @Override
    protected void init(VaadinRequest request) {
        setContent(initBuilder())
    }


    /**
     * Aufbau des Vaadin Komponentenbaums<br>
     *     "Äste" müssen vor dem "Stamm" angelegt und mit subtree hinzugefügt werden
     * @return
     */
    Component initBuilder() {
        vaadin = new VaadinBuilder()
        subwin = new VaadinBuilder()

        Component root = vaadin."$C.hlayout" ('toplayout') {
            "$F.menubar"('menu', [uikey: 'menu']) {

                "$F.menuitem"('item1', [uikey: 'i1', command: {show(it)}])
                "$F.menuitem"('item2', [uikey: 'i2', command: {show(it)}]) {
"$F.menuitem"('item2.1', [uikey: 'i2.1', command: {show(it)}])
"$F.menuitem"('item2.2', [uikey: 'i2.2', command: {show(it)}])
}
}
}
root
}

def show(MenuItem item) {
    Notification.show("clicked on ${item.text}", Notification.Type.HUMANIZED_MESSAGE)
}
}
