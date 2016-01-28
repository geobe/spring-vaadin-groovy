package de.fh_zwickau.pti.geobe.experimental

import com.vaadin.annotations.Theme
import com.vaadin.server.VaadinRequest
import com.vaadin.spring.annotation.SpringUI
import com.vaadin.ui.*

@SpringUI(path = "groove")
@Theme("valo")
/**
 * @author georg beier
 */
class GroovyHello extends UI {
    @Override
    protected void init(VaadinRequest request) {
        VerticalLayout layout = new VerticalLayout()
//        Button button = new Button("Click me")
//        button.addClickListener({e->Notification.show("Hello from Groovy!")})
        layout.addComponent(new Button("Click me",
                (Button.ClickListener){Button.ClickEvent e->
                    Notification.show(
                            "Hello Closure!\n x: $e.relativeX y: $e.relativeY  ")}))
//        layout.addComponent(button)
        TextField tf = new TextField("huhu")
        tf.value = 'hurz'
        layout.addComponent(tf)
        setContent(layout)

    }
}
