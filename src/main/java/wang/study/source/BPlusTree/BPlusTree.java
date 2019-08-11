package wang.study.source.BPlusTree;


public class BPlusTree<T, V extends Comparable<? super V>> {

    //B+树的阶
    private Integer bTreeOrder;

    // B+树的非叶子节点至少拥有的节点数量
    private Integer minNumber;

    //B+树的非叶子节点最大拥有的节点数量
    private Integer maxNumber;

    private Node<T, V> root;

    private LeafNode<T, V> left;

    /**
     * 无参构造函数
     */
    public BPlusTree() {
        this(3);
    }

    /**
     * 有参构造函数，设定B+树的阶
     *
     * @param bTreeOrder
     */
    public BPlusTree(Integer bTreeOrder) {
        this.bTreeOrder = bTreeOrder;
        this.minNumber =(int) Math.ceil(1.0*bTreeOrder/2.0);
        // 因为插入节点过程中可能出现超过上限的情况，所以加1，主要是为了避免OutOfIndexError
        this.maxNumber = bTreeOrder + 1;
        this.root = new LeafNode<T, V>();
        this.left = null;
    }

    /**
     * 查询
     *
     * @param key
     * @return
     */
    public T find(V key) {
        T t = this.root.find(key);
        if (t == null) {
            System.out.println("不存在");
        }
        return t;
    }

    public void insert(T value, V key) {
        if (key == null) {
            return;
        }
        Node<T, V> t = this.root.insert(value, key);
        if (t != null)
            this.root = t;
        this.left =  this.root.refreshLeft();
    }

    /**
     * 节点父类，因为B+树中，非叶子节点不用存储具体数据，只需要把索引作为键就可以了
     *
     * @param <T>
     * @param <V>
     */
    abstract class Node<T, V extends Comparable<? super V>> {
        // 父节点
        protected Node<T, V> parent;

        // 子节点
        protected Node<T, V>[] childs;

        // 键(子节点)数量
        protected Integer number;

        // 键
        protected Object keys[];

        // 构造方法
        public Node() {
            this.keys = new Object[maxNumber];
            this.childs = new Node[maxNumber];
            this.number = 0;
            this.parent = null;
        }

        // 查找
        abstract T find(V key);

        // 插入
        abstract Node<T, V> insert(T value, V key);

        abstract LeafNode<T, V> refreshLeft();
    }

    /**
     * 非叶子节点类
     *
     * @param <T>
     * @param <V>
     */
    class BPlusNode<T, V extends Comparable<? super V>> extends Node<T, V> {
        /**
         * 无参构造函数
         */
        public BPlusNode() {
            super();
        }

        /**
         * 递归查找，确定值在哪里
         *
         * @param key
         * @return
         */
        @Override
        T find(V key) {
            int i = 0;
            while (i < this.number) {
                if (key.compareTo((V) this.keys[i]) <= 0)
                    break;
                i++;
            }
            if (this.number == i)
                return null;
            return this.childs[i].find(key);
        }

        /**
         * 递归插入，先把值插入到对应叶子节点，最终调用叶子节点的插入类
         *
         * @param value
         * @param key
         * @return
         */
        @Override
        Node<T, V> insert(T value, V key) {
            int i = 0;
            while (i < this.number) {
                if (key.compareTo((V) this.keys[i]) < 0)
                    break;
                i++;
            }

            if (key.compareTo((V) this.keys[this.number - 1]) >= 0) {
                i--;
            }
            return this.childs[i].insert(value, key);
        }

        @Override
        LeafNode<T, V> refreshLeft() {
            return this.childs[0].refreshLeft();
        }

        /**
         * 当叶子节点插入成功完成分解，递归地向父类节点插入新的节点以保持平衡
         *
         * @param node1 新父节点
         * @param node2 新父节点
         * @param key 原来父节点
         * @return
         */
        Node<T, V> insertNode(Node<T, V> node1, Node<T, V> node2, V key) {
            V oldKey = null;
            if (this.number > 0)
                oldKey = (V) this.keys[this.number - 1];
            // 如果原有key为null，说明这个非节点是空的，直接放入二个节点即可
            if (key == null || this.number <= 0) {
                this.keys[0] = node1.keys[node1.number - 1];
                this.keys[1] = node2.keys[node2.number - 1];
                this.childs[0] = node1;
                this.childs[1] = node2;
                this.number += 2;
                return this;
            }

            // 原有节点不为空则先寻找原有节点位置，然后将新的节点插入到原有节点
            int i = 0;
            while (key.compareTo((V) this.keys[i]) != 0) {
                i++;
            }
            // 左边节点的最大值可以直接插入，右边的则要挪一挪在进行插入
            // 左边节点复用原来父节点
            this.keys[i] = node1.keys[node1.number - 1];
            this.childs[i] = node1;

            Object tempKeys[] = new Object[maxNumber];
            Object tempChilds[] = new Node[maxNumber];

            System.arraycopy(this.keys, 0, tempKeys, 0, i + 1);
            System.arraycopy(this.childs, 0, tempChilds, 0, i + 1);
            System.arraycopy(this.keys, i + 1, tempKeys, i + 2, this.number - i - 1);
            System.arraycopy(this.childs, i + 1, tempChilds, i + 2, this.number - i - 1);
            tempKeys[i + 1] = node2.keys[node2.number - 1];
            tempChilds[i + 1] = node2;

            this.number++;

            // 判断是否需要拆分
            // 如果不需要拆分，把数组复制回去，直接返回
            if (this.number <= bTreeOrder) {
                System.arraycopy(tempKeys, 0, this.keys, 0, this.number);
                System.arraycopy(tempChilds, 0, this.childs, 0, this.number);
                return null;
            }

            // 如果需要拆分，和拆叶子节点时类似，从中间拆开
            Integer middle = this.number / 2;

            //新建非叶子节点，作为拆分的右半部分
            BPlusNode<T,V> tempNode = new BPlusNode<>();
            tempNode.number = this.number - middle;
            tempNode.parent = this.parent;

            // 如果父节点为空，则新建一个非叶子节点作为父节点，并且让拆分的成功的二个非叶子节点的指针指向父节点
            if(this.parent == null){
                BPlusNode<T,V> tempBPlusNode = new BPlusNode<>();
                tempNode.parent = tempBPlusNode;
                this.parent = tempBPlusNode;
                oldKey = null;
            }
            System.arraycopy(tempKeys,middle,tempNode.keys,0,tempNode.number);
            System.arraycopy(tempChilds,middle,tempNode.childs,0,tempNode.number);
            for(int j = 0;j < tempNode.number;j++){
                tempNode.childs[j].parent = tempNode;
            }

            // 让原有非叶子节点作为左边节点
            this.number = middle;
            this.keys = new Object[maxNumber];
            this.childs = new Node[maxNumber];
            System.arraycopy(tempKeys,0,this.keys,0,middle);
            System.arraycopy(tempChilds,0,this.childs,0,middle);

            // 叶子节点拆分成功后，需要把新生的节点插入父节点
            BPlusNode<T,V> parentNode = (BPlusNode<T,V>) this.parent;
            return parentNode.insertNode(this,tempNode,oldKey);
        }
    }

    /**
     * 叶子节点
     *
     * @param <T>
     * @param <V>
     */
    class LeafNode<T, V extends Comparable<? super V>> extends Node<T, V> {

        protected Object values[];
        protected LeafNode left;
        protected LeafNode right;

        /**
         * 无参构造函数
         */
        public LeafNode() {
            super();
            this.values = new Object[maxNumber];
            this.left = null;
            this.right = null;
        }

        /**
         * 二分查找
         *
         * @param key
         * @return
         */
        @Override
        T find(V key) {
            if (this.number < 0)
                return null;

            Integer left = 0;
            Integer right = this.number;

            Integer middle = (left + right) / 2;
            while (left < right) {
                V middleKey = (V) this.keys[middle];
                if (key.compareTo(middleKey) == 0)
                    return (T) this.values[middle];
                else if (key.compareTo(middleKey) < 0) {
                    right = middle;
                } else
                    left = middle;
                middle = (left + right) / 2;
            }
            return null;
        }

        @Override
        Node<T, V> insert(T value, V key) {

            // 保存原始存在父节点key值
            V oldKey = null;
            if (this.number > 0)
                oldKey = (V) this.keys[this.number - 1];

            // 先插入数据
            int i = 0;
            while (i < this.number) {
                if (key.compareTo((V) this.keys[i]) < 0)
                    break;
                i++;
            }

            Object[] tempKeys = new Object[maxNumber];
            Object[] tempValues = new Object[maxNumber];
            System.arraycopy(this.keys, 0, tempKeys, 0, i);
            System.arraycopy(this.values, 0, tempValues, 0, i);
            System.arraycopy(this.keys, i, tempKeys, i + 1, this.number - i);
            System.arraycopy(this.values, i, tempValues, i + 1, this.number - i);
            tempKeys[i] = key;
            tempValues[i] = value;

            this.number++;

            // 判断是否需要拆分
            // 如果不需要拆分，则直接返回
            if (this.number < bTreeOrder) {
                System.arraycopy(tempKeys, 0, this.keys, 0, this.number);
                System.arraycopy(tempValues, 0, this.values, 0, this.number);

                // 虽然节点没有分裂，但是插入值可能是边界值，所以对父节点边界值进行更新
                Node node = this;
                while (node.parent != null) {
                    V tempKey = (V) node.keys[node.number - 1];
                    if (tempKey.compareTo((V) node.parent.keys[node.parent.number - 1]) > 0) {
                        node.parent.keys[node.parent.number - 1] = tempKey;
                        node = node.parent;
                    } else {
                        break;
                    }
                }
                return null;
            }

            // 如果需要拆分，则从中间把节点拆分差不多的二份
            Integer middle = this.number / 2;

            //新建叶子节点，作为拆分的右半部分
            LeafNode<T,V> tempNode = new LeafNode<>();
            tempNode.number = this.number - middle;
            tempNode.parent = this.parent;

            // 如果父节点为空，则新建一个非叶子节点作为父节点，并且让拆分成功的二个叶子节点的指针指向父节点
            if(this.parent == null){
                BPlusNode<T,V> tempBPlusNode = new BPlusNode<>();
                tempNode.parent = tempBPlusNode;
                this.parent = tempBPlusNode;
                oldKey = null;
            }
            System.arraycopy(tempKeys,middle,tempNode.keys,0,tempNode.number);
            System.arraycopy(tempValues,middle,tempNode.values,0,tempNode.number);

            // 让原有叶子节点成为拆分的左半部分
            this.number = middle;
            this.keys = new Object[maxNumber];
            this.values = new Object[maxNumber];
            System.arraycopy(tempKeys,0,this.keys,0,middle);
            System.arraycopy(tempValues,0,this.values,0,middle);
            this.right = tempNode;
            tempNode.left = this;

            // 叶子节点拆分成功后，需要把叶子节点插入到父节点
            BPlusNode<T,V> parentNode = (BPlusNode<T,V>) this.parent;
            return parentNode.insertNode(this,tempNode,oldKey);
        }

        @Override
        LeafNode<T, V> refreshLeft() {
            if(this.number <= 0){
                return null;
            }
            return this;
        }
    }

}
