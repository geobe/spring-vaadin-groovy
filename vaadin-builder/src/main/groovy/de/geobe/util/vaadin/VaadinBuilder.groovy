package de.geobe.util.vaadin

import com.vaadin.ui.*
import com.vaadin.ui.MenuBar.MenuItem
import groovy.util.logging.Slf4j

/**
 * Build a Vaadin Component tree including MenuBar menus
 * @author georg beier
 */
@Slf4j
class VaadinBuilder extends BuilderSupport {

    /** add a whole subtree */
    public static final String SUBTREE = 'subtree'
    /** key of the component in the component map */
    public static final String UIKEY = 'uikey'
    /** gridPosition is set in the parent component */
    public static final String GRID_POSITION = 'gridPosition'
    /** alignment is set in the parent component  */
    public static final String ALIGNMENT = 'alignment'

    /** menuitem object are special as they are no vaadin components */
    private static final String MENUITEM = 'menuitem'
    /** inset for printing guitree */
    private static final String PRE = '    '

    private containers = [:]
    private fields = [:]
    private components = [:]
    private menuItems = [:]

    private List<Integer> gridPosition = []
    private Alignment alignment = null

    private Component currentRoot

    /**
     * to identify components of subtrees, unique prefixes can be used
     */
    String keyPrefix = ''

    /**
     * @return a map of all constructed components
     */
    def getUiComponents() {
        components.asImmutable()
    }

    /**
     * @return a map of all constructed menu items
     */
    def getMenuItems() {
        menuItems.asImmutable()
    }

    /**
     * @return the root node of the gui components tree
     */
    def getCurrentRoot() {
        currentRoot
    }

    /**
     * add a custom field component to the builder
     *
     * @param name  identifies the custom component
     * @param fqn   fully qualified class name of the component
     */
    def void addCustomField(String name, String fqn) {
        fields[name] = fqn
    }

    /**
     * add a custom container component to the builder
     *
     * @param name  identifies the custom component
     * @param fqn   fully qualified class name of the component
     */
    def void addCustomContainer(String name, String fqn) {
        containers[name] = fqn
    }

    VaadinBuilder() {
        initContainers()
        initFields()
    }

    @Override
    protected void setParent(Object parent, Object child) {
        if (parent instanceof ComponentContainer) {
            if (child instanceof Component) {
                ComponentContainer p = (ComponentContainer) parent
                Component c = (Component) child
                if (p instanceof GridLayout && gridPosition.size() > 1) {
                    if (gridPosition.size() > 3)
                        p.addComponent(c, gridPosition[0], gridPosition[1], gridPosition[2], gridPosition[3])
                    else
                        p.addComponent(c, gridPosition[0], gridPosition[1])
                } else {
                    p.addComponent(c)
                    if (p instanceof AbstractOrderedLayout && alignment) {
                        log.debug('setting alignment')
                        p.setComponentAlignment(c, alignment)
                    }
                }
            }
        } else if (parent instanceof AbstractSingleComponentContainer) {
            if (child instanceof Component) {
                AbstractSingleComponentContainer p = (AbstractSingleComponentContainer) parent
                Component c = (Component) child
                p.setContent(c)
            }
        } else if (parent instanceof MenuBar || parent instanceof MenuBar.MenuItem) {
            // nothing to do, add was done at construction of component
        } else {
            log.error("VaadinBuilder: $parent is no ComponentContainer")
        }
    }

    @Override
    protected Object createNode(Object name) {
        def n = name.toString()
        if (n == SUBTREE)
            throw new IllegalArgumentException('missing subtree parameter')
        else if (n == MENUITEM)
            throw new IllegalArgumentException('missing MenuItem caption')
        Component component = makeComponent(n)
        if (component) {
            components << [(autoName(n)): (component)]
        }
        component
    }

    @Override
    protected Object createNode(Object name, Object value) {
        def n = name.toString()
        Component component
        if (n == SUBTREE)
            component = (Component) value
        else if (n == MENUITEM)
            return makeMenuItem(value)
        else
            component = makeComponent(n)
        if (component) {
            components << [(autoName(n)): component]
            if (n != SUBTREE) component.setCaption(value.toString())
        }
        component
    }

    @Override
    protected Object createNode(Object name, Map attributes) {
        def n = name.toString()
        if (n == SUBTREE)
            throw new IllegalArgumentException('missing subtree parameter')
        else if (n == MENUITEM)
            throw new IllegalArgumentException('missing MenuItem caption')
        Component component = makeComponent(n)
        if (component) {
            components << [(setAttributes(component, n, attributes)): component]
        }
        component
    }

    @Override
    protected Object createNode(Object name, Map attributes, Object value) {
        def n = name.toString()
        Component component
        if (n == SUBTREE)
            component = (Component) value
        else if (n == MENUITEM)
            return makeMenuItem(value, attributes)
        else
            component = makeComponent(n)
        if (component) {
            components << [(setAttributes(component, n, attributes)): component]
            if (n != SUBTREE) component.setCaption(value.toString())
        }
        component
    }

    @Override
    protected void nodeCompleted(Object parent, Object node) {
        if (!parent)
            currentRoot = (Component) node
        super.nodeCompleted(parent, node)
    }

    /**
     * evaluate the attributes map for a component:
     * try to call all addXxx and setXxx methods for a component
     * and caption it by its uikey attribute or by generating a caption from the caption parameter
     * @param component a Vaadin component
     * @param name caption of the pseudo method of the builder that built the component
     * @param attributes map of attributes given to the builder to configure the component
     * @return a unique caption for the component to be used in the components map
     */
    private String setAttributes(Component component, String name, Map attributes) {
        String uikey = attributes[UIKEY] ? keyPrefix + attributes[UIKEY] : autoName(name)
        gridPosition = attributes[GRID_POSITION] ? attributes[GRID_POSITION] : []
        alignment = attributes[ALIGNMENT] ?: null
        def methods0 = component.class.methods.collect { it.name }
        def methods = methods0.findAll { it.startsWith('set') || it.startsWith('add') }.toSet()
        attributes.forEach { String k, v ->
            def prop = k.capitalize()
            if (methods.contains("set$prop".toString())) {
                if (v != null)
                    component."set$prop"(v)
                else
                    component."set$prop"()
            } else if (methods.contains("add$prop".toString())) {
                component."add$prop"(v)
            } else if (!k == UIKEY) {
                log.warn("$k is no property of $component")
            }
        }
        uikey
    }

    /**
     * make a unique caption from caption
     */
    private String autoName(String name) {
        def names = components.keySet().findAll { it.toString() ==~ /$keyPrefix$name\d*/ }
        if (names) {
            def num = names.collect {
                def var = it.toString().replace(keyPrefix + name, '')
                var.isInteger() ? var.toInteger() : 0
            }.sort()[-1]
            keyPrefix + name.toString() + (num + 1)
        } else {
            keyPrefix + name.toString()
        }
    }

    /**
     * create a Vaadin component using classForName
     * @param name selects Class from containers or fields map of Vaadin components
     * @return newly created component
     */
    private Component makeComponent(String name) {
        Component component = null
        if (containers[name]) {
            component = (Component) Class.forName(containers[name].toString()).newInstance()
        } else if (fields[name]) {
            component = (Component) ((Class) fields[name]).newInstance()
        } else {
            log.error("VaadinBuilder: no Component found for $name")
        }
        component
    }

    /**
     * create and configure a MenuBar.MenuItem object.
     * This class has no constructor but is created by the addItem Methods of
     * either MenuBar or MenuItem. The item is added to the menuItems map using
     * uikey entry from the attributes or caption
     * @param caption as shown on the MenuItem
     * @param attributes are used to call setXx or addXx methods on the item
     * @return newly created item
     */
    private MenuItem makeMenuItem(String caption, Map attributes = [:]) {
        def parent = getCurrent()
        MenuItem item = null
        def command = attributes['command'] ?: {}
        attributes.remove('command')
        def key = attributes[UIKEY] ?: caption
        attributes.remove(UIKEY)
        if (parent instanceof MenuBar) {
            item = parent.addItem(caption, command)
        } else if (parent instanceof MenuItem) {
            item = parent.addItem(caption, command)
            parent.command = null
        }
        def methods0 = item.class.methods.collect { it.name }
        def methods = methods0.findAll { it.startsWith('set') || it.startsWith('add') }.toSet()
        attributes.forEach { String k, v ->
            def prop = k.capitalize()
            if (methods.contains("set$prop".toString())) {
                if (v != null)
                    item."set$prop"(v)
                else
                    item."set$prop"()
            } else if (methods.contains("add$prop".toString())) {
                item."add$prop"(v)
            } else {
                log.warn("$k is no property of ${item.text}")
            }
        }
        if (key) menuItems[(key)] = item
        return item
    }
    /**
     * generate a character representation of this component tree
     * for debugging etc.
     */
    String toString() {
        def invertedIndex = [:]
        components.forEach { k, v ->
            invertedIndex << [(v): k]
        }
        if (invertedIndex) {
            makeString(currentRoot, 0, invertedIndex)
        } else {
            ''
        }
    }

    /**
     * step down into the tree and insert appropriate spaces
     */
    private String makeString(HasComponents cnt, int indent, Map idx) {
        def pre = PRE * indent
        def res = "$pre${idx[cnt]}: ${cnt.class.canonicalName} {\n"
        for (Component c : cnt) {
            res += makeString(c, indent + 1, idx)
        }
        res + "$pre}\n"
    }

    /**
     * string for a terminal node of the tree
     */
    @SuppressWarnings("GrMethodMayBeStatic")
    private String makeString(Component comp, int indent, Map idx) {
        def pre = PRE * indent
        "$pre${idx[comp]}: ${comp.class.canonicalName}\n"
    }

    /**
     * put all supported Container components into a map
     */
    private void initContainers() {
        containers << [panel: 'com.vaadin.ui.Panel']
        containers << [window: 'com.vaadin.ui.Window']
        containers << [popup: 'com.vaadin.ui.PopupView']
        containers << [tabsheet: 'com.vaadin.ui.TabSheet']
        containers << [accordion: 'com.vaadin.ui.Accordion']
        containers << [custom: 'com.vaadin.ui.CustomComponent']
        containers << [abslayout: 'com.vaadin.ui.AbsoluteLayout']
        containers << [gridlayout: 'com.vaadin.ui.GridLayout']
        containers << [hsplit: 'com.vaadin.ui.HorizontalSplitPanel']
        containers << [vsplit: 'com.vaadin.ui.VerticalSplitPanel']
        containers << [formlayout: 'com.vaadin.ui.FormLayout']
        containers << [hlayout: 'com.vaadin.ui.HorizontalLayout']
        containers << [vlayout: 'com.vaadin.ui.VerticalLayout']
        containers << [csslayout: 'com.vaadin.ui.CssLayout']
//        containers << [customlayout:CustomLayout']
//        containers << [customcomponent:CustomComponent']
    }

    /**
     * put all supported field components into a map
     * also MenuBar.MenuItem's are supported though they strictly are no
     * components
     */
    private void initFields() {
        fields << [button: 'com.vaadin.ui.Button']
        fields << [checkbox: 'com.vaadin.ui.CheckBox']
        fields << [text: 'com.vaadin.ui.TextField']
        fields << [password: 'com.vaadin.ui.PasswordField']
        fields << [textarea: 'com.vaadin.ui.TextArea']
        fields << [richtext: 'com.vaadin.ui.RichTextArea']
        fields << [date: 'com.vaadin.ui.DateField']
        fields << [inlinedate: 'com.vaadin.ui.InlineDateField']
        fields << [popupdate: 'com.vaadin.ui.PopupDateField']
        fields << [combo: 'com.vaadin.ui.ComboBox']
        fields << [list: 'com.vaadin.ui.ListSelect']
        fields << [nativeselect: 'com.vaadin.ui.NativeSelect']
        fields << [twincol: 'com.vaadin.ui.TwinColSelect']
        fields << [optiongroup: 'com.vaadin.ui.OptionGroup']
        fields << [table: 'com.vaadin.ui.Table']
        fields << [tree: 'com.vaadin.ui.Tree']
        fields << [slider: 'com.vaadin.ui.Slider']
        fields << [menubar: 'com.vaadin.ui.MenuBar']
        fields << [menuitem: 'com.vaadin.ui.MenuBar.MenuItem']
        fields << [progress: 'com.vaadin.ui.ProgressBar']
        fields << [grid: 'com.vaadin.ui.Grid']
        fields << [label: 'com.vaadin.ui.Label']
        fields << [colorpicker: 'com.vaadin.ui.ColorPicker']
        fields << [image: 'com.vaadin.ui.Image']
        fields << [flash: 'com.vaadin.ui.Flash']
        fields << [frame: 'com.vaadin.ui.BrowserFrame']
        fields << [embedded: 'com.vaadin.ui.Embedded']
    }

    /**
     * allow code completion when building Vaadin gui trees
     * Here are the containers
     */
    public static enum C {
        panel,
        window,
        popup,
        tabsheet,
        accordion,
        custom,
        abslayout,
        gridlayout,
        hsplit,
        vsplit,
        formlayout,
        hlayout,
        vlayout,
    }

    /**
     * allow code completion when building Vaadin gui trees
     * Here are the fields
     */
    public static enum F {
        button,
        checkbox,
        text,
        password,
        textarea,
        richtext,
        date,
        inlinedate,
        popupdate,
        combo,
        list,
        nativeselect,
        twincol,
        optiongroup,
        table,
        tree,
        slider,
        menubar,
        menuitem,
        progress,
        grid,
        label,
        colorpicker,
        image,
        flash,
        frame,
        embedded,
        subtree
    }
}
