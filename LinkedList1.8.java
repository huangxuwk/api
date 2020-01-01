package java.util;

import java.util.function.Consumer;

/**
	* {@code list}和{@code Deque}的双链表实现
	*接口。实现所有可选的列表操作，并允许所有操作
	*元素(包括{@code null})。
 *
	所有的操作都按照双链的预期执行
	*列表。索引到列表的操作将遍历列表
	*开始或结束，以较接近指定索引的为准。
 *
	*如果多个线程同时访问一个链表，并且至少
	*其中一个线程从结构上修改了列表，它必须被
	*外部同步。结构修改是指任何操作
	*添加或删除一个或多个元素;只是设置的值
	*元素不是结构修改。)这通常是
	*通过自然地同步一些对象来完成
	封装列表。
 *
	*如果不存在此类对象，则应使用
	* {@link Collections#synchronizedList Collections.synchronizedList}
	*方法。这最好在创建时完成，以防止意外
	*对列表的非同步访问:

	* List List = Collections.synchronizedList(newLinkedList (…));
 *
	迭代器返回这个类的{@code迭代器}和
	* {@code listIterator}方法是 failure -fast:如果列表是
	*在迭代器创建后的任何时候，在结构上进行了修改
	*除了通过迭代器自己的{@code remove}或
	* {@code add}方法，迭代器将抛出一个{@link
	* ConcurrentModificationException}。因此，面对并发
	*修改，迭代器失败迅速和干净，而不是
	冒着任意的、不确定的行为的风险
	未来的时间。
 *
	注意，不能保证迭代器的故障-快速行为
	一般来说，我们不可能做出任何严格的保证
	*存在不同步的并发修改。快速失败迭代器
	*尽最大努力抛出{@code ConcurrentModificationException}。
	因此，写一个依赖于此的程序是错误的
	*正确性异常:迭代器的快速失效行为
	*应该只用于检测bug。
 */

public class LinkedList<E>
    extends AbstractSequentialList<E>
    implements List<E>, Deque<E>, Cloneable, java.io.Serializable
{
		// 结点数；transient关键字：不参与对象的序列化
    transient int size = 0;

    /**
     * *指向第一个节点的指针。
     * 不变量: (first == null && last == null) ||
     *         (first.prev == null && first.item != null)
     */
    transient Node<E> first;

    /**
     * 指向最后一个节点的指针。
     * Invariant: (first == null && last == null) ||
     *            (last.next == null && last.item != null)
     */
    transient Node<E> last;

		// 无参构造
    public LinkedList() {
    }

		// 使用一个集合来构造
    public LinkedList(Collection<? extends E> c) {
    		// 调用无参构造
        this();
        // 后面再介绍
        addAll(c);
    }

		// 链接e作为第一个元素；前插法，增加结点
    private void linkFirst(E e) {
    		// 取得链表的头结点
        final Node<E> f = first;
        // 生成结点，前一结点为null，后一结点为f
        final Node<E> newNode = new Node<>(null, e, f);
        // 将新增的结点作为头结点
        first = newNode;
        // f == null 说明原链表为空，那么新增的结点即为头也是尾
        if (f == null)
            last = newNode;
        // 原链表不为空，则前后结点相连
        else
            f.prev = newNode;
        size++;
        // 修改数+1
        modCount++;
    }

		// 链接e作为最后一个元素，尾加法，增加结点
    void linkLast(E e) {
    		// 取得链表的末结点
        final Node<E> l = last;
        // 新加结点，该结点的前一结点是原链表的末结点
        final Node<E> newNode = new Node<>(l, e, null);
        // 将新增的结点作为末结点
        last = newNode;
        // l == null 说明原链表为空，那么新增的结点即为头也是尾
        if (l == null)
            first = newNode;
        // 原链表不为空，则前后结点相连
        else
            l.next = newNode;
        size++;
        // 修改数+1
        modCount++;
    }

		// 在非空结点succ之前插入元素e
		// 让新增结点指向succ结点的前一结点和后一结点
    void linkBefore(E e, Node<E> succ) {
    		// 取得succ结点的上一结点
        final Node<E> pred = succ.prev;
        // 链表结点的插入
        final Node<E> newNode = new Node<>(pred, e, succ);
        // 在succ结点之前插入，所以succ结点的前一结点是新插入的结点
        succ.prev = newNode;
        // 如果succ没有前一结点，那么它就是头结点，插入的结点会变成新的头结点
        if (pred == null)
            first = newNode;
       	// succ有前一结点，则前后结点相连
        else
            pred.next = newNode;
        size++;
        // 修改数+1
        modCount++;
    }

		// 取消非空的第一个节点f的链接
    private E unlinkFirst(Node<E> f) {
        final E element = f.item;
        // 取得原头结点的后一结点
        final Node<E> next = f.next;
        // 取消引用，gc回收
        f.item = null;
        f.next = null;
				// 删除了头结点，那第二结点就是新的头结点
        first = next;
        // 如果next为null，那么first已经为null，也要将last变为null
        if (next == null)
            last = null;
        // 头结点的前一结点为null
        else
            next.prev = null;
        size--;
        // 修改数+1
        modCount++;
        return element;
    }

		// 取消非空最后一个节点l的链接
    private E unlinkLast(Node<E> l) {
        final E element = l.item;
        // 取得原末结点的前一结点
        final Node<E> prev = l.prev;
        // 取消引用，gc回收
        l.item = null;
        l.prev = null;
        // 删除了末结点，那末结点的前一结点变成新的末结点
        last = prev;
        // 如果prev为null，那么last已经为null，也要将first变为null
        if (prev == null)
            first = null;
        // 末结点的后一结点为null
        else
            prev.next = null;
        size--;
        // 修改数+1
        modCount++;
        return element;
    }

		// 取消非空节点x的链接，指定结点的删除
		// 让删除结点的前一结点与后一结点互相指向
    E unlink(Node<E> x) {
        final E element = x.item;
        // 目标结点的后一结点
        final Node<E> next = x.next;
        // 目标结点的前一结点
        final Node<E> prev = x.prev;

				// 如果前一结点为空，那么后一结点为新的头结点
        if (prev == null) {
            first = next;
        // 不为空，则互相连接
        } else {
            prev.next = next;
            // gc
            x.prev = null;
        }
				// 如果后一结点为空，那么前一结点为新的末结点
        if (next == null) {
            last = prev;
        // 不为空，则相互链接
        } else {
            next.prev = prev;
            // gc
            x.next = null;
        }
				// gc
        x.item = null;
        size--;
        // 操作数+1
        modCount++;
        return element;
    }

		// 返回列表中的第一个元素。
    public E getFirst() {
        final Node<E> f = first;
        if (f == null)
            throw new NoSuchElementException();
        return f.item;
    }

		// 返回列表中的最后一个元素。
    public E getLast() {
        final Node<E> l = last;
        if (l == null)
            throw new NoSuchElementException();
        return l.item;
    }

		// 从列表中删除并返回第一个元素。
    public E removeFirst() {
        final Node<E> f = first;
        if (f == null)
            throw new NoSuchElementException();
        return unlinkFirst(f);
    }

		// 从列表中移除并返回最后一个元素。
    public E removeLast() {
        final Node<E> l = last;
        if (l == null)
            throw new NoSuchElementException();
        return unlinkLast(l);
    }

		// 将指定的元素插入此列表的开头。
    public void addFirst(E e) {
        linkFirst(e);
    }

		// 将指定的元素追加到此列表的末尾。
    public void addLast(E e) {
        linkLast(e);
    }
		
		// 至少包含一个指定的元素，就返回true
    public boolean contains(Object o) {
        return indexOf(o) != -1;
    }

		// 返回列表中元素的数目。
    public int size() {
        return size;
    }

		// 将指定的元素追加到此列表的末尾。
    public boolean add(E e) {
        linkLast(e);
        return true;
    }

		// 删除第一个匹配的元素，从开头遍历链表，找寻目标结点
    public boolean remove(Object o) {
        if (o == null) {
            for (Node<E> x = first; x != null; x = x.next) {
                if (x.item == null) {
                    unlink(x);
                    return true;
                }
            }
        } else {
            for (Node<E> x = first; x != null; x = x.next) {
                if (o.equals(x.item)) {
                    unlink(x);
                    return true;
                }
            }
        }
        // 未找到，返回false
        return false;
    }

		// 将一个集合中的所有元素加到本对象的链表上
    public boolean addAll(Collection<? extends E> c) {
        return addAll(size, c);
    }
		
		// 从指定位置开始插入参数集合中的多个结点
    public boolean addAll(int index, Collection<? extends E> c) {
    		// 检查下标的合法性，0 —> size
        checkPositionIndex(index);

				// 这里的c可能为集合中的某一个，toArray()不确定
				// 所以仅列举LinkedList中的toArray()方法
        Object[] a = c.toArray();
        int numNew = a.length;
        // 集合为空
        if (numNew == 0)
            return false;

        Node<E> pred, succ;
        // 尾加在链表末尾
        if (index == size) {
            succ = null;
            pred = last;
        // 取得插入的目标结点，向目标结点的前一结点的后面增加结点
        } else {
            succ = node(index);
            pred = succ.prev;
        }

        for (Object o : a) {
            @SuppressWarnings("unchecked") E e = (E) o;
            // 生成新结点，以pred为前一结点，后一结点为null
            Node<E> newNode = new Node<>(pred, e, null);
            // 如果前一结点为null，则新增结点为头结点
            if (pred == null)
                first = newNode;
            else
                pred.next = newNode;
            // 将新增结点作为前一结点，尾加法添加结点
            pred = newNode;
        }
				// 如果index == size，则默认加在链表的末尾，那最后添加的结点就是末结点
        if (succ == null) {
            last = pred;
        // 有目标结点，那将原被断开的链表加在最后一个新增结点的后面
        } else {
            pred.next = succ;
            succ.prev = pred;
        }

        size += numNew;
        // 修改数+1
        modCount++;
        return true;
    }

		// 从列表中删除所有元素，调用返回后，列表将为空。
    public void clear() {
				// 遍历链表，取消链表间的指向
				// 虽然不用拆除结点间的引用gc也能回收
				// 但链表中的结点不一定处于gc代，所以利于分代回收
        for (Node<E> x = first; x != null; ) {
            Node<E> next = x.next;
            x.item = null;
            x.next = null;
            x.prev = null;
            // 遍历链表
            x = next;
        }
        // 头、尾结点为null
        first = last = null;
        size = 0;
        modCount++;
    }


    // 位置访问操作

		// 返回列表中指定位置的元素
    public E get(int index) {
    		// 检查下标合法性，0 -> (size-1)
        checkElementIndex(index);
        return node(index).item;
    }

		// 将列表中指定位置的元素替换为指定元素
    public E set(int index, E element) {
    		// 检查下标合法性，0 -> (size-1)
        checkElementIndex(index);
        Node<E> x = node(index);
        // 保存旧元素
        E oldVal = x.item;
 				// 替换新元素
        x.item = element;
        return oldVal;
    }

		// 向指定下标结点前添加元素
    public void add(int index, E element) {
    		// 检查下标
        checkPositionIndex(index);
	
				// 末尾追加
        if (index == size)
            linkLast(element);
        // 指定结点前加
        else
            linkBefore(element, node(index));
    }

		// 删除指定的下标结点
    public E remove(int index) {
        checkElementIndex(index);
        return unlink(node(index));
    }

		// 检查元素的有效性，元素的保存下标：0 -> (size-1)
    // 被get()、set()、remove()等调用
    private boolean isElementIndex(int index) {
        return index >= 0 && index < size;
    }

		// 检查下标的有效性，index可以等于size，相当于向链表末尾增加结点
		// 被add()、addAll()等调用
    private boolean isPositionIndex(int index) {
        return index >= 0 && index <= size;
    }

		// 构造异常的具体信息
    private String outOfBoundsMsg(int index) {
        return "Index: "+index+", Size: "+size;
    }

    private void checkElementIndex(int index) {
        if (!isElementIndex(index))
            throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
    }

    private void checkPositionIndex(int index) {
        if (!isPositionIndex(index))
            throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
    }

		// 返回指定元素索引处的(非空)节点
    Node<E> node(int index) {
    		// 二分法，选择从前向后遍历还是从后向前遍历
    		// size >> 1 = size / 2
        if (index < (size >> 1)) {
            Node<E> x = first;
            for (int i = 0; i < index; i++)
                x = x.next;
            return x;
        } else {
            Node<E> x = last;
            for (int i = size - 1; i > index; i--)
                x = x.prev;
            return x;
        }
    }

    // 搜索操作

		// 返回该元素第一次出现的下标，从前向后遍历
    public int indexOf(Object o) {
        int index = 0;
        if (o == null) {
            for (Node<E> x = first; x != null; x = x.next) {
                if (x.item == null)
                    return index;
                index++;
            }
        } else {
            for (Node<E> x = first; x != null; x = x.next) {
                if (o.equals(x.item))
                    return index;
                index++;
            }
        }
        return -1;
    }

		// 返回该元素最后一次出现的下标，从后向前遍历
    public int lastIndexOf(Object o) {
        int index = size;
        if (o == null) {
            for (Node<E> x = last; x != null; x = x.prev) {
                index--;
                if (x.item == null)
                    return index;
            }
        } else {
            for (Node<E> x = last; x != null; x = x.prev) {
                index--;
                if (o.equals(x.item))
                    return index;
            }
        }
        return -1;
    }

    // 队列的基本操作

		// 查找但不删除此列表的头(第一个元素)
    public E peek() {
        final Node<E> f = first;
        return (f == null) ? null : f.item;
    }

    // 查找但不删除此列表的头(第一个元素)
    public E element() {
        return getFirst();
    }

		// 查找并删除此列表的头(第一个元素)
    public E poll() {
        final Node<E> f = first;
        return (f == null) ? null : unlinkFirst(f);
    }

		// 查找并删除此列表的头(第一个元素)
    public E remove() {
        return removeFirst();
    }

		// 将指定的元素添加为此列表的末尾(最后一个元素)
    public boolean offer(E e) {
        return add(e);
    }

    // 双端队列的操作

		// 将指定的元素插入此列表的前面
    public boolean offerFirst(E e) {
        addFirst(e);
        return true;
    }

		// 在列表末尾插入指定的元素
    public boolean offerLast(E e) {
        addLast(e);
        return true;
    }

		// 查找但不删除此列表的头(第一个元素)
    public E peekFirst() {
        final Node<E> f = first;
        return (f == null) ? null : f.item;
     }

		// 查找但不删除此列表的尾(最后一个元素)
    public E peekLast() {
        final Node<E> l = last;
        return (l == null) ? null : l.item;
    }

		// 查找并删除此列表的头(第一个元素)
    public E pollFirst() {
        final Node<E> f = first;
        return (f == null) ? null : unlinkFirst(f);
    }

		// 查找并删除此列表的尾(最后一个元素)
    public E pollLast() {
        final Node<E> l = last;
        return (l == null) ? null : unlinkLast(l);
    }

		// 将元素推入此列表所表示的堆栈: 将元素插入到列表的前面
    public void push(E e) {
        addFirst(e);
    }

    // 从该列表表示的堆栈中弹出一个元素: 删除并返回此列表的第一个元素
    public E pop() {
        return removeFirst();
    }

		// 删除第一个匹配的元素，从前向后遍历
    public boolean removeFirstOccurrence(Object o) {
        return remove(o);
    }

		// 删除最后一个匹配的元素，从后向前遍历
    public boolean removeLastOccurrence(Object o) {
        if (o == null) {
            for (Node<E> x = last; x != null; x = x.prev) {
                if (x.item == null) {
                    unlink(x);
                    return true;
                }
            }
        } else {
            for (Node<E> x = last; x != null; x = x.prev) {
                if (o.equals(x.item)) {
                    unlink(x);
                    return true;
                }
            }
        }
        return false;
    }

    /**
			返回此列表中元素的列表迭代器(在适当的情况下)
			序列)，从列表中的指定位置开始。
			*遵守{@code List.listIterator(int)}.
			的通用契约
     *
			*列表迭代器是fail-fast:如果列表是结构化的
			*在创建迭代器之后的任何时间修改，除了
			*通过列表迭代器自己的{@code remove}或{@code add}
			*方法，列表迭代器将抛出一个
			* {@code ConcurrentModificationException}。于是，面对
			*并发修改，迭代器失败得更快更干净
			*而不是冒着任意的、不确定的行为的风险
			未来的时间。
     */
     
    // 得到一个从指定下标开始的迭代器
    public ListIterator<E> listIterator(int index) {
    		// 检查下标的合法性，0 -> size
        checkPositionIndex(index);
        return new ListItr(index);
    }

    private class ListItr implements ListIterator<E> {
    		// 上一个返回的元素
        private Node<E> lastReturned;
        // 下一个返回的元素
        private Node<E> next;
        // 下一个返回元素的下标
        private int nextIndex;
        // 在生成ListItr对象时确定的修改数，在遍历迭代器期间
        // 若外部类进行了添加、删除等操作，就会改变modCount
        // 而expectedModCount没有改变，通过判断两个数是否相同
        // 就可以判断集合是否被别的线程修改了，保证线程安全
        private int expectedModCount = modCount;

        ListItr(int index) {
        		// 找到指定下标对应的结点
            next = (index == size) ? null : node(index);
            // 下一个返回元素的下标
            nextIndex = index;
        }

        public boolean hasNext() {
            return nextIndex < size;
        }

        public E next() {
        		// 检查线程安全
            checkForComodification();
            if (!hasNext())
                throw new NoSuchElementException();

						// 将返回的结点作为lastReturned
            lastReturned = next;
            // 得到下一结点
            next = next.next;
            // 下标加1
            nextIndex++;
            return lastReturned.item;
        }
        
        // 若下标大于0，说明下一个返回的结点有前结点
        public boolean hasPrevious() {
            return nextIndex > 0;
        }

        public E previous() {
            checkForComodification();
            if (!hasPrevious())
                throw new NoSuchElementException();

						// 如果next为null，说明下一个返回的结点是末结点的后一结点null
						// 此时：lastReturned == next
            lastReturned = next = (next == null) ? last : next.prev;
            nextIndex--;
            return lastReturned.item;
        }

        public int nextIndex() {
            return nextIndex;
        }

        public int previousIndex() {
            return nextIndex - 1;
        }

				// 迭代器中的删除方法，并不会造成线程不安全
        public void remove() {
        		// 检查线程安全
            checkForComodification();
            if (lastReturned == null)
                throw new IllegalStateException();
						
						// lastNext 可能为 null，当lastReturned为末结点时
            Node<E> lastNext = lastReturned.next;
            // 删除上一个返回的结点，一定不是null，modCount++
            unlink(lastReturned);
            
            // 若调用了previous()方法，则next == lastReturned
            if (next == lastReturned)
            		// 删除了lastReturned结点，下一个结点为删除结点的下一个结点
            		// 下标并未变化，请仔细思考
                next = lastNext;
            // 若调用了next()方法，则next != lastReturned
            else
            		// 下标发生了变化，所以要-1
                nextIndex--;
            lastReturned = null;
            // unlink(lastReturned)会增加modCount，这里保持了同步
            expectedModCount++;
        }

        public void set(E e) {
            if (lastReturned == null)
                throw new IllegalStateException();
            checkForComodification();
            lastReturned.item = e;
        }

				// 线程安全
        public void add(E e) {
            checkForComodification();
            lastReturned = null;
            if (next == null)
                linkLast(e);
            else
                linkBefore(e, next);
            // 略过新增的结点，不参与遍历
            nextIndex++;
            // 同步修改数
            expectedModCount++;
        }

				// 遍历剩下的元素
        public void forEachRemaining(Consumer<? super E> action) {
        		// 判断action是否为null，若是，抛空指针异常
            Objects.requireNonNull(action);
            while (modCount == expectedModCount && nextIndex < size) {
                action.accept(next.item);
                lastReturned = next;
                next = next.next;
                nextIndex++;
            }
            // 上面的循环可能被打断，需要再次判断线程安全
            checkForComodification();
        }

				// 判断修改数是否发生了变化，如果改变了，那么抛出异常
        final void checkForComodification() {
            if (modCount != expectedModCount)
                throw new ConcurrentModificationException();
        }
    }

		// 链表结点，双向链表
    private static class Node<E> {
    		// 结点元素的值
        E item;
        // 结点的下一结点
        Node<E> next;
        // 结点的上一结点
        Node<E> prev;

        Node(Node<E> prev, E element, Node<E> next) {
            this.item = element;
            this.next = next;
            this.prev = prev;
        }
    }

 		// 相反方向的迭代器
    public Iterator<E> descendingIterator() {
        return new DescendingIterator();
    }

    private class DescendingIterator implements Iterator<E> {
    		// 下标定位到链表最后，向前遍历
        private final ListItr itr = new ListItr(size());
        public boolean hasNext() {
            return itr.hasPrevious();
        }
        public E next() {
            return itr.previous();
        }
        public void remove() {
            itr.remove();
        }
    }

    @SuppressWarnings("unchecked")
    private LinkedList<E> superClone() {
        try {
        		// 调用Object.clone()
            return (LinkedList<E>) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InternalError(e);
        }
    }

		// 浅克隆
    public Object clone() {
        LinkedList<E> clone = superClone();

				// 初始化
        clone.first = clone.last = null;
        clone.size = 0;
        clone.modCount = 0;

        // 用我们的元素初始化克隆，复制元素引用，浅克隆
        for (Node<E> x = first; x != null; x = x.next)
            clone.add(x.item);

        return clone;
    }

		// 通过遍历链表生成数组
    public Object[] toArray() {
        Object[] result = new Object[size];
        int i = 0;
        // 遍历链表，依次加到数组里
        for (Node<E> x = first; x != null; x = x.next)
            result[i++] = x.item;
        return result;
    }

    @SuppressWarnings("unchecked")
    public <T> T[] toArray(T[] a) {
    		// 通过反射来保证数组足够容纳链表的所有元素
        if (a.length < size)
            a = (T[])java.lang.reflect.Array.newInstance(
                                a.getClass().getComponentType(), size);
        int i = 0;
        Object[] result = a;
        // 遍历链表，依次加到数组里
        for (Node<E> x = first; x != null; x = x.next)
            result[i++] = x.item;

				// 数组有效元素为：0 -> size-1
        if (a.length > size)
            a[size] = null;

        return a;
    }

    private static final long serialVersionUID = 876323262645176354L;

    private void writeObject(java.io.ObjectOutputStream s)
        throws java.io.IOException {
        // Write out any hidden serialization magic
        s.defaultWriteObject();

        // Write out size
        s.writeInt(size);

        // Write out all elements in the proper order.
        for (Node<E> x = first; x != null; x = x.next)
            s.writeObject(x.item);
    }

    @SuppressWarnings("unchecked")
    private void readObject(java.io.ObjectInputStream s)
        throws java.io.IOException, ClassNotFoundException {
        // Read in any hidden serialization magic
        s.defaultReadObject();

        // Read in size
        int size = s.readInt();

        // Read in all elements in the proper order.
        for (int i = 0; i < size; i++)
            linkLast((E)s.readObject());
    }

    /**
     * Creates a <em><a href="Spliterator.html#binding">late-binding</a></em>
     * and <em>fail-fast</em> {@link Spliterator} over the elements in this
     * list.
     *
     * <p>The {@code Spliterator} reports {@link Spliterator#SIZED} and
     * {@link Spliterator#ORDERED}.  Overriding implementations should document
     * the reporting of additional characteristic values.
     *
     * @implNote
     * The {@code Spliterator} additionally reports {@link Spliterator#SUBSIZED}
     * and implements {@code trySplit} to permit limited parallelism..
     *
     * @return a {@code Spliterator} over the elements in this list
     * @since 1.8
     */
    @Override
    public Spliterator<E> spliterator() {
        return new LLSpliterator<E>(this, -1, 0);
    }

    /** A customized variant of Spliterators.IteratorSpliterator */
    static final class LLSpliterator<E> implements Spliterator<E> {
        static final int BATCH_UNIT = 1 << 10;  // batch array size increment
        static final int MAX_BATCH = 1 << 25;  // max batch array size;
        final LinkedList<E> list; // null OK unless traversed
        Node<E> current;      // current node; null until initialized
        int est;              // size estimate; -1 until first needed
        int expectedModCount; // initialized when est set
        int batch;            // batch size for splits

        LLSpliterator(LinkedList<E> list, int est, int expectedModCount) {
            this.list = list;
            this.est = est;
            this.expectedModCount = expectedModCount;
        }

        final int getEst() {
            int s; // force initialization
            final LinkedList<E> lst;
            if ((s = est) < 0) {
                if ((lst = list) == null)
                    s = est = 0;
                else {
                    expectedModCount = lst.modCount;
                    current = lst.first;
                    s = est = lst.size;
                }
            }
            return s;
        }

        public long estimateSize() { return (long) getEst(); }

        public Spliterator<E> trySplit() {
            Node<E> p;
            int s = getEst();
            if (s > 1 && (p = current) != null) {
                int n = batch + BATCH_UNIT;
                if (n > s)
                    n = s;
                if (n > MAX_BATCH)
                    n = MAX_BATCH;
                Object[] a = new Object[n];
                int j = 0;
                do { a[j++] = p.item; } while ((p = p.next) != null && j < n);
                current = p;
                batch = j;
                est = s - j;
                return Spliterators.spliterator(a, 0, j, Spliterator.ORDERED);
            }
            return null;
        }

        public void forEachRemaining(Consumer<? super E> action) {
            Node<E> p; int n;
            if (action == null) throw new NullPointerException();
            if ((n = getEst()) > 0 && (p = current) != null) {
                current = null;
                est = 0;
                do {
                    E e = p.item;
                    p = p.next;
                    action.accept(e);
                } while (p != null && --n > 0);
            }
            if (list.modCount != expectedModCount)
                throw new ConcurrentModificationException();
        }

        public boolean tryAdvance(Consumer<? super E> action) {
            Node<E> p;
            if (action == null) throw new NullPointerException();
            if (getEst() > 0 && (p = current) != null) {
                --est;
                E e = p.item;
                current = p.next;
                action.accept(e);
                if (list.modCount != expectedModCount)
                    throw new ConcurrentModificationException();
                return true;
            }
            return false;
        }

        public int characteristics() {
            return Spliterator.ORDERED | Spliterator.SIZED | Spliterator.SUBSIZED;
        }
    }

}
