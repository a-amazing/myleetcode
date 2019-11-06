---

#### ThreadLocal源码

 一个生命使用周期很长的map 存放了很多实际使用生命周期短的对象。 

(eg.ThreadLocal)



```
To help deal with very large and long-lived usages, the hash table entries use WeakReferences for keys. However, since reference queues are not used, stale entries are guaranteed to be removed only when the table starts running out of space.
```

```text
为了应对较大容量和较长生命周期的用途,哈希表的键值对使用了WewakReferences作为键.然而,因为没有使用应用队列,所以旧键只会在哈希表需要扩容时被移除
```

---

#### HashMap源码

```java
//HashMap的put元素的方法    
final V putVal(int hash, K key, V value, boolean onlyIfAbsent, boolean evict) {
    //局部变量
    Node<K,V>[] tab; Node<K,V> p; int n, i;
    //当哈希表内部数组未创建或者为空的情况下
    if ((tab = table) == null || (n = tab.length) == 0)
        n = (tab = resize()).length;	//默认长度16
    //如果未发生hash碰撞,直接放入新节点
    if ((p = tab[i = (n - 1) & hash]) == null)
        tab[i] = newNode(hash, key, value, null);
    else {
        //p现在为数组中的头结点
        Node<K,V> e; K k;
        //同一个key
        if (p.hash == hash &&
            ((k = p.key) == key || (key != null && key.equals(k))))
            e = p;
        //p为红黑树的根节点
        else if (p instanceof TreeNode)
            e = ((TreeNode<K,V>)p).putTreeVal(this, tab, hash, key, value);
        else {
            //以链表形式存在,其实以0开始未计算p,节点数>=8时转化未红黑树
            for (int binCount = 0; ; ++binCount) {
                if ((e = p.next) == null) {
                    p.next = newNode(hash, key, value, null);
                    if (binCount >= TREEIFY_THRESHOLD - 1) // -1 for 1st(8-1 = 7)
                        treeifyBin(tab, hash);
                    break;
                }
                if (e.hash == hash &&
                    ((k = e.key) == key || (key != null && key.equals(k))))
                    break;
                p = e;
            }
        }
        if (e != null) { // existing mapping for key
            V oldValue = e.value;
            if (!onlyIfAbsent || oldValue == null)
                e.value = value;
            afterNodeAccess(e);
            return oldValue;
        }
    }
    ++modCount;
    //在put结束之后才能知道增加了元素还是替换了value值,再进行扩容操作
    if (++size > threshold)
        resize();
    afterNodeInsertion(evict);
    return null;
}
```



```java
/**
* Initializes or doubles table size.  If null, allocates in
* accord with initial capacity target held in field threshold.
* Otherwise, because we are using power-of-two expansion, the
* elements from each bin must either stay at same index, or move
* with a power of two offset in the new table.
* 初始化或者双倍扩容数组,并将元素移入新的数组
* @return the table
*/
final Node<K,V>[] resize() {
    Node<K,V>[] oldTab = table;
    int oldCap = (oldTab == null) ? 0 : oldTab.length; //原数组长度
    int oldThr = threshold;							//待扩容长度,默认为16
    int newCap, newThr = 0;
    if (oldCap > 0) {
        //已经超过最大扩容容量,不作扩容行为
        if (oldCap >= MAXIMUM_CAPACITY) {
            threshold = Integer.MAX_VALUE;
            return oldTab;
        }
        //如果翻倍后长度小于最大容量,并且原长度比默认容量大,将阈值翻倍
        else if ((newCap = oldCap << 1) < MAXIMUM_CAPACITY &&
                 oldCap >= DEFAULT_INITIAL_CAPACITY)
            newThr = oldThr << 1; // double threshold
    }
    //原长度小于等于0,原阈值大于0,以阈值为准
    else if (oldThr > 0) // initial capacity was placed in threshold
        newCap = oldThr;
    //如果原长度和阈值俊小于0,设置有误,以默认值为准
    else {               // zero initial threshold signifies using defaults
        newCap = DEFAULT_INITIAL_CAPACITY;
        newThr = (int)(DEFAULT_LOAD_FACTOR * DEFAULT_INITIAL_CAPACITY);
    }
    //如果新阈值==0,用新长度*负载因子重新计算阈值
    if (newThr == 0) {
        float ft = (float)newCap * loadFactor;
        newThr = (newCap < MAXIMUM_CAPACITY && ft < (float)MAXIMUM_CAPACITY ?
                  (int)ft : Integer.MAX_VALUE);
    }
    threshold = newThr;
    @SuppressWarnings({"rawtypes","unchecked"})
    //创建一个新长度的数组
    Node<K,V>[] newTab = (Node<K,V>[])new Node[newCap];
    table = newTab;
    //如果原数组不为空,需要进行迁移
    if (oldTab != null) {
        for (int j = 0; j < oldCap; ++j) {
            Node<K,V> e;
            //取当前节点node
            if ((e = oldTab[j]) != null) {
                //置空,待垃圾回收
                oldTab[j] = null;
                //只有单独节点,直接将该节点放入新数组
                if (e.next == null)
                    newTab[e.hash & (newCap - 1)] = e;
                //是红黑树根节点
                else if (e instanceof TreeNode)
                    ((TreeNode<K,V>)e).split(this, newTab, j, oldCap);
                else { // preserve order(维持顺序?)
                    /**
                    * 所谓的loHead和hiHead是指原节点的hashCode&oldCap会分化为0/1
                    * 分别对应新数组的同一个位置和翻倍位置
                    * 将原来的一条链按顺序分解成了两条链
                    * */
                    Node<K,V> loHead = null, loTail = null;
                    Node<K,V> hiHead = null, hiTail = null;
                    Node<K,V> next;
                    do {
                        next = e.next;
                        if ((e.hash & oldCap) == 0) {
                            if (loTail == null)
                                loHead = e;
                            else
                                loTail.next = e;
                            loTail = e;
                        }
                        else {
                            if (hiTail == null)
                                hiHead = e;
                            else
                                hiTail.next = e;
                            hiTail = e;
                        }
                    } while ((e = next) != null);
                    //最后将链首节点放入当前位置和翻倍后位置
                    if (loTail != null) {
                        loTail.next = null;
                        newTab[j] = loHead;
                    }
                    if (hiTail != null) {
                        hiTail.next = null;
                        newTab[j + oldCap] = hiHead;
                    }
                }
            }
        }
    }
    return newTab;
}
```

---

#### HashMap可能发生线程不安全的场景

