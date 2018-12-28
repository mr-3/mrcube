/**
 * TreeAlgorithm.java
 *  
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package org.mrcube.layout;

import org.mrcube.models.MR3Constants.GraphType;

import java.awt.geom.Point2D;
import java.util.Collection;

public class TreeAlgorithm {

    private char rootOrient_;
    private int depth_;
    private GraphLayoutData[] prevNodeAtLevel_;
    private double[] levelHeight_;
    private double[] levelPosition_;

    private double levelSeparation_;
    private double subtreeSeparation_;
    private double siblingSeparation_;

    public TreeAlgorithm(char orientation) {
        rootOrient_ = orientation;
    }

    public void setOrientation(char orientation) {
        rootOrient_ = orientation;
    }

    public void applyTreeAlgorithm(Collection<GraphLayoutData> graphNodes, GraphLayoutData selectedNode, GraphType type) {
        GraphLayoutData root = selectedNode;
        if (root == null) {
            for (GraphLayoutData data : graphNodes) {
                if (!data.hasParent()) {
                    root = data;
                    compute(graphNodes, root, type);
                }
            }
        }
    }

    public boolean compute(Collection<GraphLayoutData> graphNodes, GraphLayoutData root, GraphType type) {
        if (type == GraphType.RDF) {
            levelSeparation_ = GraphLayoutUtilities.RDF_HORIZONTAL_SPACE;
            subtreeSeparation_ = siblingSeparation_ = GraphLayoutUtilities.RDF_VERTICAL_SPACE;
        } else if (type == GraphType.CLASS) {
            levelSeparation_ = GraphLayoutUtilities.CLASS_HORIZONTAL_SPACE;
            subtreeSeparation_ = siblingSeparation_ = GraphLayoutUtilities.CLASS_VERTICAL_SPACE;
        } else if (type == GraphType.PROPERTY) {
            levelSeparation_ = GraphLayoutUtilities.PROPERTY_HORIZONTAL_SPACE;
            subtreeSeparation_ = siblingSeparation_ = GraphLayoutUtilities.PROPERTY_VERTICAL_SPACE;
        }
        Point2D.Double rootPos_ = root.getPosition();

        // Construct the Node data fields.
        for (GraphLayoutData tmpnode : graphNodes) {
            tmpnode.data = new TreeAlgorithmData();
        }

        // Initialize the data fields.
        depth_ = 0;
        initializeData_(root, 0);

        // Build and initialize array for connecting neighboring non-sibling
        // nodes.
        prevNodeAtLevel_ = new GraphLayoutData[depth_ + 1];

        for (int i = 0; i < depth_ + 1; i++)
            prevNodeAtLevel_[i] = null;

        setInitialPositions_(root);
        levelHeight_ = new double[depth_ + 1];
        for (int i = 0; i < depth_ + 1; i++)
            levelHeight_[i] = 0.0;
        setLevelHeight_(root);
        levelPosition_ = new double[depth_ + 1];
        levelPosition_[0] = 0;
        for (int i = 1; i < depth_ + 1; i++)
            levelPosition_[i] = levelPosition_[i - 1] + (levelHeight_[i - 1] + levelHeight_[i]) / 2.0;
        setFinalPositions_(root, 0.0);

        // Shift root to original position
        double xoffs = rootPos_.x - root.getPosition().x;
        double yoffs = rootPos_.y - root.getPosition().y;

        for (GraphLayoutData tmpnode : graphNodes) {
            if (tmpnode.data.level != -1) {
                Point2D.Double pos = tmpnode.getPosition();
                tmpnode.setPosition(pos.x + xoffs, pos.y + yoffs);
            }
        }

        return true;
    }

    private void initializeData_(GraphLayoutData node, int level) {
        node.data.level = level;

        if (level > depth_) depth_ = level;

        GraphLayoutData[] children = new GraphLayoutData[node.getChildCount()];
        int num_children = 0;

        // Put all the unvisited children in the list.
        // for (Iterator i = node.getChildren().iterator(); i.hasNext();) {
        // GraphLayoutData child = (GraphLayoutData) i.next();
        for (GraphLayoutData child : node.getChildren()) {
            if (child.data.level == -1) {
                children[num_children++] = child;
            }
        }

        if (num_children == 0) {
            node.data.isLeaf = true;
            return;
        }

        // Sort the children by "left" coordinate;
        GraphLayoutData tmpnode;
        for (int i = 0; i < num_children - 1; i++)
            for (int j = i + 1; j < num_children; j++)
                if (((rootOrient_ == 'd' || rootOrient_ == 'u') && children[j].getPosition().x < children[i]
                        .getPosition().x)
                        || ((rootOrient_ == 'l' || rootOrient_ == 'r') && children[j].getPosition().y < children[i]
                                .getPosition().y)) {
                    tmpnode = children[i];
                    children[i] = children[j];
                    children[j] = tmpnode;
                }

        // Initialize links.
        node.data.leftChild = children[0];
        node.data.rightChild = children[num_children - 1];
        for (int i = 0; i < num_children; i++)
            children[i].data.parent = node;
        for (int i = 0; i < num_children - 1; i++)
            children[i].data.rightSibling = children[i + 1];
        for (int i = 1; i < num_children; i++)
            children[i].data.leftSibling = children[i - 1];
        for (int i = 0; i < num_children; i++)
            children[i].data.level = level + 1;

        // Initialize data for the children.
        for (int i = 0; i < num_children; i++)
            initializeData_(children[i], level + 1);
    }

    private void setInitialPositions_(GraphLayoutData node) {
        TreeAlgorithmData data = node.data;

        // Initialize neighbor links.
        data.leftNeighbor = prevNodeAtLevel_[data.level];
        if (data.leftNeighbor != null) data.leftNeighbor.data.rightNeighbor = node;
        prevNodeAtLevel_[data.level] = node;

        GraphLayoutData tmpnode;
        if (data.leftSibling != null) {
            if (rootOrient_ == 'd' || rootOrient_ == 'u') {
                data.prelim = data.leftSibling.data.prelim + siblingSeparation_
                        + (node.getBoundingBox().width + data.leftSibling.getBoundingBox().width) / 2.0;
            } else {
                data.prelim = data.leftSibling.data.prelim + siblingSeparation_
                        + (node.getBoundingBox().height + data.leftSibling.getBoundingBox().height) / 2.0;
            }
        }

        if (!data.isLeaf)
        // Bottom-up, left-to-right recurse.
        {
            for (tmpnode = data.leftChild; tmpnode != null; tmpnode = tmpnode.data.rightSibling)
                setInitialPositions_(tmpnode);

            double mid_point = data.leftChild.data.prelim + (data.rightChild.data.prelim) / 2.0;

            if (data.leftSibling != null) {
                data.modifier = data.prelim - mid_point;
                evenOut(node);
            } else data.prelim = mid_point;
        }

    }

    /**
     * Called when two subtree are moved apart. It evens out the position of all
     * the other subtrees between them.
     */
    private void evenOut(GraphLayoutData node) {
        TreeAlgorithmData data = node.data;

        GraphLayoutData left_most = data.leftChild;
        GraphLayoutData neighbor = left_most.data.leftNeighbor;
        int compare_depth = 1;
        while (left_most != null && neighbor != null) {
            double left_mod_sum = 0.0;
            double right_mod_sum = 0.0;

            GraphLayoutData ancestor_leftmost = left_most;
            GraphLayoutData ancestor_neighbor = neighbor;

            for (int i = 0; i < compare_depth; i++) {
                ancestor_leftmost = ancestor_leftmost.data.parent;
                ancestor_neighbor = ancestor_neighbor.data.parent;

                right_mod_sum += ancestor_leftmost.data.modifier;
                left_mod_sum += ancestor_neighbor.data.modifier;
            }

            double move_distance;
            if (rootOrient_ == 'd' || rootOrient_ == 'u') {
                move_distance = neighbor.data.prelim + left_mod_sum + subtreeSeparation_
                        + (left_most.getBoundingBox().width + neighbor.getBoundingBox().width) / 2.0
                        - left_most.data.prelim - right_mod_sum;
            } else {
                move_distance = neighbor.data.prelim + left_mod_sum + subtreeSeparation_
                        + (left_most.getBoundingBox().height + neighbor.getBoundingBox().height) / 2.0
                        - left_most.data.prelim - right_mod_sum;
            }

            if (move_distance > 0.0) {
                GraphLayoutData tmpnode;
                int left_sibling;
                for (tmpnode = node, left_sibling = 0; tmpnode != null && tmpnode != ancestor_neighbor; tmpnode = tmpnode.data.leftSibling)
                    left_sibling++;

                if (tmpnode != null) {
                    double slide_value = move_distance / left_sibling;
                    for (tmpnode = node; tmpnode != ancestor_neighbor; tmpnode = tmpnode.data.leftSibling) {
                        tmpnode.data.prelim += move_distance;
                        tmpnode.data.modifier += move_distance;
                        move_distance -= slide_value;
                    }
                } else {
                    return;
                }
            }

            compare_depth++;
            if (left_most.data.leftChild == null) {
                left_most = leftMost_(node, 0, compare_depth);
            } else {
                left_most = left_most.data.leftChild;
            }
            if (left_most != null) {
                neighbor = left_most.data.leftNeighbor;
            }
        }
    }

    private GraphLayoutData leftMost_(GraphLayoutData node, int level, int depth) {
        if (level >= depth) return node;
        if (node.data.leftChild == null) return null;

        GraphLayoutData left_most, right_most;
        right_most = node.data.leftChild;
        left_most = leftMost_(right_most, level + 1, depth);
        while (left_most == null && right_most.data.rightSibling != null) {
            right_most = right_most.data.rightSibling;
            left_most = leftMost_(right_most, level + 1, depth);
        }
        return left_most;
    }

    private void setFinalPositions_(GraphLayoutData node, double mod_sum) {
        double x = 0.0, y = 0.0;
        // GraphLayoutData tmpnode;
        TreeAlgorithmData data = node.data;

        x = data.prelim + mod_sum;
        y = -data.level * levelSeparation_ - levelPosition_[data.level];

        if (data.leftChild != null) {
            setFinalPositions_(data.leftChild, mod_sum + data.modifier);
        }

        if (data.rightSibling != null) {
            setFinalPositions_(data.rightSibling, mod_sum);
        }

        if (rootOrient_ == 'd') {
            node.setPosition(x, y);
        } else if (rootOrient_ == 'u') {
            node.setPosition(x, -y);
        } else if (rootOrient_ == 'l') {
            node.setPosition(y, x);
        } else if (rootOrient_ == 'r') {
            node.setPosition(-y, x);
        }
    }

    private void setLevelHeight_(GraphLayoutData node) {
        TreeAlgorithmData data = node.data;

        if (rootOrient_ == 'd' || rootOrient_ == 'u') {
            if (node.getBoundingBox().height > levelHeight_[data.level])
                levelHeight_[data.level] = node.getBoundingBox().height;
        } else {
            if (node.getBoundingBox().width > levelHeight_[data.level])
                levelHeight_[data.level] = node.getBoundingBox().width;
        }

        if (data.leftChild != null) setLevelHeight_(data.leftChild);
        if (data.rightSibling != null) setLevelHeight_(data.rightSibling);
    }

    public class TreeAlgorithmData {

        public int level;
        public GraphLayoutData parent;
        public GraphLayoutData leftChild;
        public GraphLayoutData rightChild;
        public GraphLayoutData leftSibling;
        public GraphLayoutData rightSibling;
        public GraphLayoutData leftNeighbor;
        public GraphLayoutData rightNeighbor;
        public boolean isLeaf;
        public double modifier;
        public double prelim;
        public GraphLayoutData group;

        public TreeAlgorithmData() {
            level = -1;
            parent = leftChild = rightChild = leftSibling = rightSibling = leftNeighbor = rightNeighbor = null;
            isLeaf = false;
            modifier = prelim = 0;
            group = null;
        }
    }
}
