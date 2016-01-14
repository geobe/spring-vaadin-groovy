package de.fh_zwickau.pti.geobe.experimental

import com.vaadin.annotations.Theme
import com.vaadin.data.Item
import com.vaadin.event.ShortcutAction
import com.vaadin.server.VaadinRequest
import com.vaadin.shared.ui.label.ContentMode
import com.vaadin.spring.annotation.SpringUI
import com.vaadin.ui.Alignment
import com.vaadin.ui.Button
import com.vaadin.ui.CheckBox
import com.vaadin.ui.Component
import com.vaadin.ui.CssLayout
import com.vaadin.ui.Field
import com.vaadin.ui.Label
import com.vaadin.ui.Table
import com.vaadin.ui.UI
import com.vaadin.ui.themes.Reindeer
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
@SpringUI(path = "griddemo")
@Theme("valo")
class GridBuilderDemo extends UI {

    @Autowired
    private ResourceLoader resourceLoader;

    def VaadinBuilder vaadin
    def widgets = [:]

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

        Component root = vaadin."$C.gridlayout"(
                [uikey                  : 'topgrid',
                 width                  : '640px',
                 columns                : 1,
                 responsive             : true,
                 hideEmptyRowsAndColumns: true,
                 margins                : true,
                 spacing                : true]) {
            "$C.gridlayout"([uikey     : 'searchgrid', columns: 3, rows: 2,
                             width     : '100%',
                             responsive: true,
                             margins   : true,
                             spacing   : true]) {
                "$F.optiongroup"('Quelle',
                        [uikey       : 'sourceoptions',
                         items       : ['erlebt', 'erfragt', 'gehört'],
                         gridPosition: [0, 0],
                         width       : '180px',
                         alignment   : Alignment.TOP_RIGHT])
                "$C.vlayout"([uikey       : 'countrieslayout',
                              gridPosition: [1, 0],
                              width       : '200px',
                ]) {
                    "$F.combo"('Land 1', [uikey: 'contry1combo',
                                          width: '180px'])
                    "$F.combo"('Land 2', [uikey: 'contry2combo',
                                          width: '180px'])
                }
                "$C.vlayout"([uikey       : 'constellationlayout',
                              gridPosition: [2, 0],
                              width       : '260px',
                              alignment   : Alignment.TOP_CENTER,]) {
                    "$F.combo"('Konstellation',
                            [uikey    : 'constellationcombo',
//                             width    : '250px',
                             alignment: Alignment.TOP_CENTER])
                }
                "$F.button"('Find', [uikey        : 'findbutton',
                                     alignment    : Alignment.MIDDLE_RIGHT,
                                     gridPosition : [2, 1],
                                     clickShortcut: ShortcutAction.KeyCode.ENTER,
                                     styleName    : Reindeer.BUTTON_DEFAULT,
                                     clickListener: { log.info('findbutton pressed') }
                ])
                "$F.checkbox"(' CI anzeigen', [uikey              : 'cishow',
                                               gridPosition       : [0, 1],
                                               valueChangeListener: { hideCi() },
                                               value              : false])
                "$F.checkbox"(' Comments anzeigen', [uikey              : 'commentshow',
                                                     gridPosition       : [1, 1],
                                                     valueChangeListener: { hideComment() },
                                                     value              : false])
            }
            "$F.list"('Suchergebnisse', [uikey    : 'resultlist',
                                         rows     : 12,
                                         width    : '20em',
                                         alignment: Alignment.TOP_CENTER])
            "$F.label"('Critical incident', [uikey  : 'cilabel',
                                             visible: false,
                                             width  : '640px'])
            "$F.table"('Kommentare', [uikey  : 'commenttable',
//                                      newItemsAllowed: true,
                                      visible: false])
        }
        poulate()
        root
    }

    private poulate() {
        Table table = vaadin.uiComponents.'commenttable'
        Label label = vaadin.uiComponents.'cilabel'
        addColumn(table, 'text', CssLayout.class, new Label('no comment'), 'Kommentar', 400)
        addColumn(table, 'votes', Integer.class, 0, 'Votes', 80)
        addColumn(table, 'button', Button.class, new Button('Blub'), 'Abstimmen', 130)
        def comments = [[new Label('wer schreibt denn so etwas')],
                        [new Label('klar, Klingonen schießen gleich, wenn du einen Bug findest')
                         , 5],
                        [new Label('''Jetzt kommt mal ein ganz langer Kommentar, um zu sehen,
was dann passiert. Jetzt kommt mal ein ganz langer Kommentar, um zu sehen,
was dann passiert. Jetzt kommt mal ein ganz langer Kommentar, um zu sehen,
was dann passiert. Jetzt kommt mal ein ganz langer Kommentar, um zu sehen,
was dann passiert. Jetzt kommt mal ein ganz langer Kommentar, um zu sehen,
was dann passiert. Jetzt kommt mal ein ganz langer Kommentar, um zu sehen,
was dann passiert. Jetzt kommt mal ein ganz langer Kommentar, um zu sehen,
was dann passiert. Jetzt kommt mal ein ganz langer Kommentar, um zu sehen,
was dann passiert. Jetzt kommt mal ein ganz langer Kommentar, um zu sehen,
was dann passiert. Jetzt kommt mal ein ganz langer Kommentar, um zu sehen,
was dann passiert. Jetzt kommt mal ein ganz langer Kommentar, um zu sehen,
was dann passiert. Jetzt kommt mal ein ganz langer Kommentar, um zu sehen,
was dann passiert. Jetzt kommt mal ein ganz langer Kommentar, um zu sehen,
was dann passiert. Jetzt kommt mal ein ganz langer Kommentar, um zu sehen,
was dann passiert. '''), 0]
        ]
        comments.each { v ->
            addRow(table, v)
        }
        table.valueChange(new Field.ValueChangeEvent(table))
        label.setContentMode(ContentMode.HTML)
        label.value = '''<h2>Interkulturelle Softwareentwicklung</h2>
<b>Die 10 häufigsten Aussagen, die man von einem klingonischen Softwareentwickler hört</b><br/>
"Spezifikationen sind für die Schwachen und Ängstlichen."<br/>
"Einrückungen im Code?! Ich zeige Dir wie man einrückt wenn ich Deinen Schädel einrücke."<br/>
"Was soll das Gerede mit der `Freigabe´? Klingonen erstellen für ihre Software keine `Freigabe´.
 Wir lassen die Software aus ihrem Käfig, damit sie eine blutige Spur von Designern und
 Qualitätsprüfern hinter sich herzieht."<br/>
"Klingonische Funktionsaufrufe haben keine `Parameter´ - sie haben `Argumente´ - wage
 nicht zu widersprechen."<br/>
"Debugging? Klingonen debuggen nicht. Unsere Software ist nicht dazu gedacht,
 die Schwachen zu verhätscheln."<br/>
"Ich habe die Abteilung vom technischen Qualitätsmanagement in einem Bat-Leth Wettkampf besiegt.
 Sie werden uns nie wieder belästigen."<br/>
"Ein ECHTER klingonischer Programmierer kommentiert seinen Code nicht!"<br/>
"Mit dem Entwurf dieser Anforderungsliste hast Du die Ehre meiner Familie beleidigt.
 Mache Dich bereit zu sterben!"<br/>
"Du stellst den Sinn meines Codes in Frage? Ich sollte Dich auf der Stelle töten,
 gerade so wie Du jetzt dastehst!"<br/>
"Unsere Nutzer werden Furcht und Achtung vor unserer Software haben.<br/>
 Laßt die Software los! Laßt sie los, auf daß die Nutzer wie die Hunde fliehen, die sie sind!"
'''
    }

    private hideCi() {
        Label label = vaadin.uiComponents.'cilabel'
        CheckBox checkBox = vaadin.uiComponents.'cishow'
        label.visible = checkBox.value
        log.info('checkbox changed value')
    }

    private hideComment() {
        Table table = vaadin.uiComponents.'commenttable'
        CheckBox checkBox = vaadin.uiComponents.'commentshow'
        table.visible = checkBox.value
        log.info('checkbox changed value')
    }

    private addColumn(Table table, String pid, Class type,
                      def defaultValue, String header = null, int width = 150, Table.Align align = null) {
        table.addContainerProperty(pid, type, defaultValue, header, null, align)
        table.setColumnWidth(pid, width)
    }

    private addRow(Table table, List cells) {
        def cols = ['text', 'votes', 'button']
        def itemId = table.addItem()
        // to better identify table rows, you could also use the 2nd version of addItem and
        // use the entity ID as itemID
//        table.addItem(newId)
        // Stylable wrapper for the cell content
        CssLayout content = new CssLayout() {
            @Override
            public String getCss(Component c) {
                return "padding-left: 0px;"
            }
        }
        content.addComponent(cells[0])
        Item row = table.getItem(itemId)
        row.getItemProperty(cols[0]).setValue(content)
        row.getItemProperty(cols[1]).setValue(cells[1])
        Button button = new Button('Antwort')
        button.addClickListener { insertBehind(table, itemId, 1) }
        row.getItemProperty(cols[2]).setValue(button)
        itemId
    }

    private insertBehind(Table table, def previousItemId, int indent) {
        def cols = ['text', 'votes', 'button']
        def itemId = table.addItemAfter(previousItemId)
        // to better identify table rows, you could also use the 2nd version of addItemAfter and
        // use the entity ID as itemID
//        table.addItemAfter(previousId, newId)
        Item row = table.getItem(itemId)
        Label label = new Label("Dies ist ein Kommentar der Ebene $indent zu einem Kommentar." +
                'Mit etwas mehr Aufwand kann man ihn auch einrücken.')
        // Stylable wrapper for the cell content
        CssLayout content = new CssLayout() {
            @Override
            public String getCss(Component c) {
                return "padding-left: ${indent * 8}px;"
            }
        }
        content.addComponent(label)
        Button button = new Button('Antwort')
        button.addClickListener { insertBehind(table, itemId, indent + 1) }
        row.getItemProperty(cols[0]).setValue(content)
        row.getItemProperty(cols[1]).setValue(0)
        row.getItemProperty(cols[2]).setValue(button)
    }

}
