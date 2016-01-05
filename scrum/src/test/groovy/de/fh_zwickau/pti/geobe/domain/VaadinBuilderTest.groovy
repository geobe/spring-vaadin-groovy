package de.fh_zwickau.pti.geobe.domain

import com.vaadin.ui.Component
import com.vaadin.ui.Field
import de.fh_zwickau.pti.geobe.GroovaaApplication
import de.geobe.util.vaadin.VaadinBuilder
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.boot.test.SpringApplicationConfiguration
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner

/**
 * Created by georg beier on 10.11.2015.
 */
@RunWith(SpringJUnit4ClassRunner)
@SpringApplicationConfiguration(classes = GroovaaApplication)
class VaadinBuilderTest {

    @Test
    public void createBuilder() {
        println "running test createBuilder"

        boolean eventSent = false

        def tcl = { Field.ValueChangeEvent event ->
            println "valueChangeEvent from ${event.component.class.name} $event.property.value"
            eventSent = true
        }

        def vaadin = new VaadinBuilder()
        Component ui = vaadin.hlayout {
            text("Textfeld1")
            text("Textfeld2", [uikey: 'tf2', value: 'hurz'])
            text("Textfeld3", [uikey              : 'tf3',
                               value              : 'burz',
                               valueChangeListener: tcl])
        }
        def uic = vaadin.uiComponents
        assert uic['tf2'].parent == uic['hlayout']
        assert uic['text'].parent == uic['hlayout']
        assert uic['tf2'].value == 'hurz'
        assert vaadin.currentRoot == uic['hlayout']

        uic['tf3'].value = 'burz is not enough!'

        assert eventSent

        println(vaadin.uiComponents)
    }

    @Test
    public void addSubTree() {
        println 'running test addSubTree'

        def vaadin = new VaadinBuilder()

        Component root, st1, st2, one

        vaadin.keyPrefix = 'st1.'
        st1 = vaadin.panel {
            text('tf1', [uikex: 'tf1', value: 'tf1'])
        }

        println(vaadin.uiComponents)
        println vaadin

        vaadin.keyPrefix = 'st1.'
        st2=vaadin.vsplit{
            text([value:'st2-1'])
            text([value:'st2-2'])
        }

        println(vaadin.uiComponents)
        println vaadin

        vaadin.keyPrefix = ''

        one = vaadin.text("auf top level")

        root = vaadin.hlayout{
            subtree(st1, [uikey: 'st1'] )
            subtree(st2, [uikey: 'st2'] )
            subtree one, [uikey: 'textone']
        }

        println(vaadin.uiComponents)
        println vaadin

    }
}
