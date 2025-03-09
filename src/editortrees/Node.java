package editortrees;

/**
 * A node in a height-balanced binary tree with rank. Except for the NULL_NODE,
 * one node cannot belong to two different trees.
 * 
 * @author Kunaal and Tulsi
 */
public class Node {

	enum Code {
		SAME, LEFT, RIGHT;

		// Used in the displayer and debug string
		public String toString() {
			switch (this) {
			case LEFT:
				return "/";
			case SAME:
				return "=";
			case RIGHT:
				return "\\";
			default:
				throw new IllegalStateException();
			}
		}
	}

	// The fields would normally be private, but for the purposes of this class,
	// we want to be able to test the results of the algorithms in addition to the
	// "publicly visible" effects

	char data;
	Node left, right; // subtrees
	int rank; // inorder position of this node within its own subtree.
	Code balance;
	public DisplayableNodeWrapper displayableNodeWrapper;

	// Feel free to add other fields that you find useful.
	// You probably want a NULL_NODE, but you can comment it out if you decide
	// otherwise.
	// The NULL_NODE uses the "null character", \0, as it's data and null children,
	// but they could be anything since you shouldn't ever actually refer to them in
	// your code.
	static final Node NULL_NODE = new Node('\0', null, null);
	// Node parent; You may want parent, but think twice: keeping it up-to-date
	// takes effort too, maybe more than it's worth.

	public Node() {
		this('\0', NULL_NODE, NULL_NODE);
	}

	public Node(char data, Node left, Node right) {
		// DONE: write this.
		this.data = data;
		this.left = left;
		this.right = right;
		this.balance = Code.SAME;
		this.displayableNodeWrapper = new DisplayableNodeWrapper(this);
	}

	public Node(char data) {
		// Make a leaf
		this(data, NULL_NODE, NULL_NODE);
	}

	// Provided to you to enable testing, please don't change.
	int slowHeight() {
		if (this == NULL_NODE) {
			return -1;
		}
		return Math.max(this.left.slowHeight(), this.right.slowHeight()) + 1;
	}

	// Provided to you to enable testing, please don't change.
	public int slowSize() {
		if (this == NULL_NODE) {
			return 0;
		}
		return this.left.slowSize() + this.right.slowSize() + 1;
	}

	// helper methods for milestone 1:

	public void toString(StringBuilder sb) {
		if (this == NULL_NODE) {
			return;
		}
		// in-order to string
		this.left.toString(sb);
		sb.append(this.data);
		this.right.toString(sb);

	}

	public Node addHelp(char ch, int pos, RotationTracker rotationCount) {
		if (this == NULL_NODE) { // handle null case (simply add at the end)
			return new Node(ch); // return to parent
		} else if (pos > this.rank) {
			// go right
			this.right = right.addHelp(ch, pos - this.rank - 1, rotationCount); // right add case
			if (rotationCount.keepRotating) {
				if (this.balance == Code.LEFT) { // check left case
					this.balance = Code.SAME; // adjust to right balance
					rotationCount.keepRotating = false; // no rotation needed
				} else if (this.balance == Code.SAME) { // no rotation needed, however future rotations are possible
					this.balance = Code.RIGHT;
				} else { // right -> trigger a rotation
					// left rotation
					rotationCount.keepRotating = false;
					if (this.right.balance == Code.LEFT) { // right child is left -> double left rotation
						rotationCount.count += 2; // add 2 rotations
						return this.doubleLeftRotation(this, this.right.left, this.right);
					} else // right child is balance right -> single left rotation
					{
						rotationCount.count++; // add 1 rotation
						return this.singleLeftRotation(this, this.right);
					}
				}
			}
		} else { // left recursion and increment rank
			this.rank++;
			this.left = left.addHelp(ch, pos, rotationCount); // left add case
			if (rotationCount.keepRotating) {
				if (this.balance == Code.RIGHT) { // check right case
					this.balance = Code.SAME;
					rotationCount.keepRotating = false; // no rotation needed
				} else if (this.balance == Code.SAME) { // no rotation needed, however future rotations are possible
					this.balance = Code.LEFT;
				} else { // left -> trigger a rotation
					// right rotation
					rotationCount.keepRotating = false;
					if (this.left.balance == Code.RIGHT) { // left child is right -> double right rotation
						rotationCount.count += 2; // add 2 rotations
						return this.doubleRightRotation(this, this.left.right, this.left);
					} else // left child is left -> single right rotation
					{
						rotationCount.count++; // add 1 rotation
						return this.singleRightRotation(this, this.left);
					}
				}
			}
		}
		// return the node that was added
		return this;
	}


	private Node doubleRightRotation(Node parent, Node grandchild, Node child) {
		// use the same logic as the double left rotation, just reverse directions

		// assign the subtrees of the nodes accordingly
		// update the ranks of the nodes and their balance codes
		child.right = grandchild.left;
		parent.left = grandchild.right;
		grandchild.left = child;
		grandchild.right = parent;

		// ranks
		grandchild.rank += child.rank + 1;
		parent.rank -= grandchild.rank + 1;

		// set balance codes
		if (grandchild.balance == Code.LEFT) { // left balanced grandchild
			child.balance = Code.SAME;
			parent.balance = Code.RIGHT;
		} else if (grandchild.balance == Code.SAME) { // equal balanced grandchild
			child.balance = Code.SAME;
			parent.balance = Code.SAME;
		} else { // right balanced grandchild
			child.balance = Code.LEFT;
			parent.balance = Code.SAME;
		}
		// grandchild now has equal balance
		grandchild.balance = Code.SAME;
		return grandchild;
	}

	private Node doubleLeftRotation(Node parent, Node grandchild, Node child) {
		// assign the subtrees of the nodes accordingly
				// update the ranks of the nodes and their balance codes
				parent.right = grandchild.left;
				child.left = grandchild.right;
				grandchild.left = parent;
				grandchild.right = child;

				// adjust node parameters

				// ranks
				child.rank -= grandchild.rank + 1;
				grandchild.rank += parent.rank + 1;

				// set balance codes
				if (grandchild.balance == Code.LEFT) { // left balanced grandchild
					parent.balance = Code.SAME;
					child.balance = Code.RIGHT;
				} else if (grandchild.balance == Code.SAME) { // equal balanced grandchild
					parent.balance = Code.SAME;
					child.balance = Code.SAME;
				} else { // right balanced grandchild
					parent.balance = Code.LEFT;
					child.balance = Code.SAME;
				}
				// grandchild now has equal balance
				grandchild.balance = Code.SAME;
				return grandchild;
	}

	private Node singleLeftRotation(Node parent, Node child) {
		// make the parent the child, and the child the parent
		parent.right = child.left;
		child.left = parent;

		// adjust node parameters
		parent.balance = Code.SAME;
		child.balance = Code.SAME;
		child.rank += parent.rank + 1; // increment the rank
		return child; // the child is the new parent, so it should be returned
	}

	private Node singleRightRotation(Node parent, Node child) {
		// same implementation as left rotation, just switch directions
		// make the parent the child, and the child the parent
		parent.left = child.right;
		child.right = parent;

		// adjust node parameters
		parent.balance = Code.SAME;
		child.balance = Code.SAME;
		parent.rank -= child.rank + 1; // the rank should be decremented
		return child; // the child is the new parent, so it should be returned
	}

	public void toRankString(StringBuilder sb) {
		// needs to follow : [c0, d0]
		if (this == NULL_NODE) {
			return;
		}
		sb.append(this.data);
		sb.append(this.rank);
		sb.append(", ");
		this.left.toRankString(sb);
		this.right.toRankString(sb);
	}

	public char getHelp(int pos) {
		if (this == NULL_NODE) {
			throw new IllegalStateException();
		}
		if (pos < this.rank) {
			return this.left.getHelp(pos);
		} else if (pos > this.rank) {
			return this.right.getHelp(pos - this.rank - 1);
		} else {
			return this.data;
		}
	}

	public boolean ranksMatchLeftSubtreeSize() {
		if (this == NULL_NODE) {
			return true;
		}
		int leftTree = left.treeSize();
		if (this.rank != leftTree) {
			return false;
		}
		return left.ranksMatchLeftSubtreeSize() && right.ranksMatchLeftSubtreeSize();
	}

	private int treeSize() {
		if (this == NULL_NODE) {
			return 0;
		}
		return 1 + left.treeSize() + right.treeSize();
	}

	// helper methods for milestone 2:

	public void toDebugString(StringBuilder sb) {
		if (this == NULL_NODE) {
			return;
		}
		sb.append(this.data);
		sb.append(this.rank);
		sb.append(this.balance);
		sb.append(", ");
		left.toDebugString(sb);
		right.toDebugString(sb);
	}

	public int fastHeight() {
		if (this == NULL_NODE) {
			return -1;
		}
		if (this.balance == Code.RIGHT) {
			return 1 + this.right.fastHeight();
		}
		return 1 + this.left.fastHeight();
	}

	public boolean balanceCodesAreCorrect() {
		return this.balanceCodes().balance;
	}

	private HeightBalance balanceCodes() {
		if (this == NULL_NODE) {
			return new HeightBalance(true, 0);
		}
		HeightBalance left = this.left.balanceCodes();
		HeightBalance right = this.right.balanceCodes();
		int height = Math.max(left.height, right.height) + 1;
		if (!(left.balance && right.balance)) {
			return new HeightBalance(false, height);
		}
		boolean correct;
		if (left.height > right.height) {
			correct = this.balance == Code.LEFT;
		} else if (left.height < right.height) {
			correct = this.balance == Code.RIGHT;
		} else {
			correct = this.balance == Code.SAME;
		}
		return new HeightBalance(correct, height);
	}
	
	// helper methods for milestone 3:

	// You will probably want to add more constructors and many other
	// recursive methods here. I added 47 of them - most were tiny helper methods
	// to make the rest of the code easy to understand. My longest method was
	// delete(): 20 lines of code other than } lines. Other than delete() and one of
	// its helpers, the others were less than 10 lines long. Well-named helper
	// methods are more effective than comments in writing clean code.

	public Node CreateEditTree(String s) {
		Node n = NULL_NODE;
		if (s.length() == 1) {
			n = new Node(s.charAt(0));
		} else if (s.length() > 0) {
			n = new Node(s.charAt(s.length()/2));
			n.rank = s.length()/2;
			if (Math.floor(Math.log(s.length() - s.length()/2 - 1)/Math.log(2)) < Math.floor(Math.log(s.length()/2)/Math.log(2))) {
				n.balance = Code.LEFT;
			}
			n.left = n.left.CreateEditTree(s.substring(0, s.length()/2));
			n.right = n.right.CreateEditTree(s.substring(s.length()/2 + 1, s.length()));
		}
		return n;
	}
	
	public Node delete(int pos, RotationTracker rotationCount) {
		if (pos < this.rank) {
			this.left = this.left.delete(pos, rotationCount);
			this.rank--;
			if (rotationCount.keepRotating) {
				if (this.balance == Code.LEFT) { // check left case
					this.balance = Code.SAME; // adjust to right balance
					rotationCount.keepRotating = false; // no rotation needed
				} else if (this.balance == Code.SAME) { // no rotation needed, however future rotations are possible
					this.balance = Code.RIGHT;
				} else { // right -> trigger a rotation
					// left rotation
					rotationCount.keepRotating = false;
					if (this.right.balance == Code.LEFT) { // right child is left -> double left rotation
						rotationCount.count += 2; // add 2 rotations
						return this.doubleLeftRotation(this, this.right.left, this.right);
					} else // right child is balance right -> single left rotation
					{
						rotationCount.count++; // add 1 rotation
						return this.singleLeftRotation(this, this.right);
					}
				}
			}
		} else if (pos > this.rank) {
			this.right = this.right.delete(pos - this.rank - 1, rotationCount);
			if (rotationCount.keepRotating) {
				if (this.balance == Code.RIGHT) { // check right case
					this.balance = Code.SAME;
					rotationCount.keepRotating = false; // no rotation needed
				} else if (this.balance == Code.SAME) { // no rotation needed, however future rotations are possible
					this.balance = Code.LEFT;
				} else { // left -> trigger a rotation
					// right rotation
					rotationCount.keepRotating = false;
					if (this.left.balance == Code.RIGHT) { // left child is right -> double right rotation
						rotationCount.count += 2; // add 2 rotations
						return this.doubleRightRotation(this, this.left.right, this.left);
					} else // left child is left -> single right rotation
					{
						rotationCount.count++; // add 1 rotation
						return this.singleRightRotation(this, this.left);
					}
				}
			}
		} else {
			if (!this.hasLeft() && !this.hasRight()) {
				return NULL_NODE;
			} else if (!this.hasLeft()) {
				return this.right;
			} else if (!this.hasRight()) {
				return this.left;
			} else {
				Node n = this.left;
				while (n.right != NULL_NODE) {
					n = n.right;
				}
				Node temp = new Node(n.data);
				this.left = this.left.delete(pos - 1, rotationCount);
				rotationCount.keepRotating = true;
				temp.right = this.right;
				temp.left = this.left;
				if (this.left == NULL_NODE) {
					temp.rank = 0;
				} else {
					temp.rank = this.left.rank + 1;
				}
				if (this.balance == Code.SAME || this.balance == Code.RIGHT) {
					temp.balance = Code.RIGHT;
				}
				if (rotationCount.keepRotating) {
					if (temp.balance == Code.LEFT) { // check left case
						temp.balance = Code.SAME; // adjust to right balance
						rotationCount.keepRotating = false; // no rotation needed
					} else if (temp.balance == Code.SAME) { // no rotation needed, however future rotations are possible
						temp.balance = Code.RIGHT;
					} else { // right -> trigger a rotation
						// left rotation
						rotationCount.keepRotating = false;
						if (temp.right.balance == Code.LEFT) { // right child is left -> double left rotation
							rotationCount.count += 2; // add 2 rotations
							return temp.doubleLeftRotation(temp, temp.right.left, temp.right);
						} else // right child is balance right -> single left rotation
						{
							rotationCount.count++; // add 1 rotation
							return temp.singleLeftRotation(temp, temp.right);
						}
					}
				}
				return temp;
			}
		}
		
		return this;
	}
	
	// DONE: By the end of milestone 1, consider if you want to use the graphical
	// debugger. See
	// the unit test throwing an error and the README.txt file.
	
	public boolean hasRight() {
		return this.right != NULL_NODE;
	}

	public boolean hasLeft() {
		return this.left != NULL_NODE;
	}

	public boolean hasParent() {
		return false;
	}

	public Node getParent() {
		return NULL_NODE;
	}
}