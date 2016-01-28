package de.fh_zwickau.pti.geobe.util.view

import com.vaadin.ui.Tree

/**
 * A set of utility methods to make work with Vaadin Tree component a bit easier
 * <br>
 * @author georg beier
 */
class VaadinTreeHelper {
    private Tree tree

    /**
     * A TreeHelper instance is bound to a Vaadin Tree
     * @param aTree the Tree object that is supported by this instance
     */
    public VaadinTreeHelper(Tree aTree) {
        tree = aTree
    }

    /**
     *  add a new node to the tree
     * @param id id of a new node. if null, id will begenerated
     * @param parentId id of parent node. if null, there is no parent
     * @param caption caption of new node as displayed
     * @param childrenAllowed can node have children?
     * @return id , either given or generated
     */
    public addNode(Object id, Object parentId, String caption, Boolean childrenAllowed) {
        if (id)
            tree.addItem(id)
        else
            id = tree.addItem()
        tree.setItemCaption(id, caption)
        if (parentId)
            tree.setParent(id, parentId)
        tree.setChildrenAllowed(id, childrenAllowed)
        id
    }

    /**
     * build a vaadin tree for a given tree data structure. To ease the work with tree selections,
     * id's for the vaadin tree node consist of single element maps with a key describing the kind
     * of object represented by the node and a value, typically a database key or other unique
     * value used to lookup the domain instance represented by this tree node
     *
     * @param node an object (e.g. a dto) that should be represented by the Vaadin tree node
     * @param parentId id of the Vaadin tree parent node
     * @param idPrefix identifies kind of node, e.g. domain class name of underlying object
     * @param idField name of the field in the node object that holds the key value
     * @param captionField name of the field in the node object that holds the caption
     * @param childrenField name of the field in the node object that holds the list of child objects
     * @return
     */
    public descend(Object node, Object parentId, String idPrefix, String idField, String captionField, String childrenField) {
        def nodeId = [type: idPrefix, id: node."$idField"]
        addNode(nodeId, parentId, node."$captionField", !node."$childrenField".isEmpty())
        node."$childrenField".each { subnode ->
            descend(subnode, nodeId, idPrefix, idField, captionField, childrenField)
        }
    }

    /**
     * find the node id of the topmost node for a given node
     * @param id th id of the node where we start
     * @return topmost parent node
     */
    public topParentForId(def id) {
        def pid = tree.getParent(id)
        if (pid)
            topParentForId(pid)
        else
            id
    }

    /**
     * get a List that contains ids of all expanded nodes.
     *
     * @return the expanded node list
     */
    public getAllExpanded() {
        tree.itemIds.findAll {
            tree.isExpanded(it)
        }
    }

    /**
     * try to identify previously expanded nodes from List and reexpand them after a tree reload.
     * As node id objects differ after reload, this needs a comparison in groovy
     *
     * @param exp the list generated before tree reload
     */
    public void reexpand(Collection exp) {
        tree.itemIds.findAll { itemId ->
            exp.find { it == itemId }
        }.each {
            tree.expandItem(it)
        }
    }

    /**
     * find matching itemId in the tree by comparing each id of type Map with the match parameter. This is necessary
     * because Tree uses Identity and not Equality to compare ids. So you cannot directly look for an equal but
     * not identical key
     * @param
     *      match a map that looks like the id we are looking for
     * @return
     *      the found tree id or null, if none
     */
    public findMatchingId(Map match) {
        tree.itemIds.find{ id ->
            id instanceof Map && match.keySet().every { key ->
                id[key] == match[key]
            }
        }
    }
}
