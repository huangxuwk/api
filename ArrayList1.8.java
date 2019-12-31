package java.util;

import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import sun.misc.SharedSecrets;

/**
	*可调整大小的数组实现List接口。实现了
	*所有可选的列表操作，并允许所有元素，包括
	* < tt >空< / tt >。除了实现List接口外，
	这个类提供了操作数组大小的方法
	*用于内部存储列表。这个类大致相当于
	* Vector，但它是不同步的。)
 *
 * <p>The <tt>size</tt>, <tt>isEmpty</tt>, <tt>get</tt>, <tt>set</tt>,
 * <tt>iterator</tt>, and <tt>listIterator</tt> operations run in constant
	* 迭代器， listIterator操作恒定运行
	*时间。加法运算在平摊常数时间，
	也就是说，添加n个元素需要O(n)的时间。所有其他的操作
	*在线性时间内运行(粗略地说)。常数因子比较低
	*用于LinkedList实现。
 *
	每个ArrayList实例有一个容量。的能力是
	用于在列表中存储元素的数组的大小。它总是
	*至少与列表大小相同。当元素被添加到ArrayList中时，
	*容量自动增长。增长政策的细节没有
	*超出了添加一个元素已经摊销的事实
	*时间成本。
 *
	一个应用程序可以增加一个ArrayList实例的容量
	*在使用ensureCapacity添加大量元素之前
	*操作。这可能会减少增量重新分配的数量。
 *
	注意这个实现不是同步的。
	*如果多个线程同时访问一个ArrayList实例，
	并且至少有一个线程从结构上修改了列表
	* 必须在外部同步。(结构修改是
	任何添加或删除一个或多个元素的操作，或显式操作
	*调整背景阵列的大小;仅仅设置元素的值是不行的
	*结构调整。)这通常是由
	同步的一些对象，自然封装的列表。
 *
	*如果不存在此类对象，则应使用
	* {@link Collections#synchronizedList Collections.synchronizedList}
	*方法。这最好在创建时完成，以防止意外
	*对列表的非同步访问:
	* List List =Collections.synchronizedList(newArrayList (…));
 *
	* < p > < name = "快速失败" >
	*这个类的{@link #iterator() iterator}和返回的迭代器
	* {@link #listIterator(int) listIterator}的方法是fail-fast:
	*如果在迭代器之后的任何时候对列表进行了结构上的修改
	*以任何方式创建，除了通过迭代器自己创建
	* {@link ListIterator#remove()删除}或
	* {@link ListIterator#add(Object) add}方法，迭代器将抛出一个
	* {@link ConcurrentModificationException}。于是，面对
	*并发修改，迭代器失败得更快更干净
	*而不是冒着arbit的风险
	 *
	注意，不能保证迭代器的故障-快速行为
	一般来说，我们不可能做出任何严格的保证
	*存在不同步的并发修改。快速失败迭代器
	*尽最大努力抛出{@code ConcurrentModificationException}。
	因此，写一个依赖于此的程序是错误的
	*正确性异常:迭代器的快速失效行为
	*应该只用于检测bug。
 */

public class ArrayList<E> extends AbstractList<E>
        implements List<E>, RandomAccess, Cloneable, java.io.Serializable
{
    private static final long serialVersionUID = 8683452581122892189L;

		// 默认初始容量
    private static final int DEFAULT_CAPACITY = 10;

		// 当指定数组的容量为0的时候使用这个变量赋值，空对象数组 
    private static final Object[] EMPTY_ELEMENTDATA = {};

		// 默认实例化的时候使用此变量赋值，空对象数组 
		// 用来区别EMPTY_ELEMENTDATA，后面将会详细解释
    private static final Object[] DEFAULTCAPACITY_EMPTY_ELEMENTDATA = {};

		// 用来存储数据的数组，非私有以简化嵌套类的访问
    transient Object[] elementData;

		// 已存储元素的个数
    private int size;

		// 单参构造
    public ArrayList(int initialCapacity) {
    		// 未限制initialCapacity的大小，所以最大容量为Integer.MAX_VALUE
        if (initialCapacity > 0) {
            this.elementData = new Object[initialCapacity];
        } else if (initialCapacity == 0) {
        		// 容量为0，将第一个空数组赋值给它
            this.elementData = EMPTY_ELEMENTDATA;
        } else {
            throw new IllegalArgumentException("Illegal Capacity: "+
                                               initialCapacity);
        }
    }

		// 构造一个初始容量为10的空列表。
    public ArrayList() {
    		// 默认情况下，赋值第二个空数组，表示容量为默认初始容量
    		// 这也就是为什么给了两个空数组对象，就为了将两种情况区别开
        this.elementData = DEFAULTCAPACITY_EMPTY_ELEMENTDATA;
    }

		// 用另一个集合来初始化本对象
    public ArrayList(Collection<? extends E> c) {
    		// 得到缓存数组，赋值给成员elementData
        elementData = c.toArray();
        if ((size = elementData.length) != 0) {
            // c.toArray可能(不正确地)不返回Object[]
            // 当泛型是String时，返回的是String[]
            if (elementData.getClass() != Object[].class)
            		// 此方法将详细讲解
                elementData = Arrays.copyOf(elementData, size, Object[].class);
        } else {
            // 用空数组替换，容量为0
            this.elementData = EMPTY_ELEMENTDATA;
        }
    }
  	// original: 原数组，newLength: 复制长度，newType: 返回数组的类型
	//public static <T,U> T[] copyOf(U[] original, int newLength, Class<? extends T[]> newType) {
	//    @SuppressWarnings("unchecked")
				// 如果是Object[].class，则new Object[newLength]
				// 如果不是，则Array.newInstance(newType.getComponentType(), newLength);
	//    T[] copy = ((Object)newType == (Object)Object[].class)
	//        ? (T[]) new Object[newLength]
						// newType.getComponentType()可以得到数组类型
	//        : (T[]) Array.newInstance(newType.getComponentType(), newLength);
				// 复制的长度不能超过原数组的最大值，否则可能会出现下标越界的问题
	//    System.arraycopy(original, 0, copy, 0,
	//                     Math.min(original.length, newLength));
	//    return copy;
	//}

    // 将elementData长度变为size
    public void trimToSize() {
    		// 修改数+1
        modCount++;
        if (size < elementData.length) {
            elementData = (size == 0)
              ? EMPTY_ELEMENTDATA
              : Arrays.copyOf(elementData, size);
        }
    }
	//public static <T> T[] copyOf(T[] original, int newLength) {
	//    return (T[]) copyOf(original, newLength, original.getClass());
	//}

    /**
		增加这个ArrayList实例的容量，如果必要的，以确保它可以容纳至少数量的元素
		*由最小容量参数指定。
     */
    public void ensureCapacity(int minCapacity) {
    		// 在构造方法中产生了差异，无参构造elementData = DEFAULTCAPACITY_EMPTY_ELEMENTDATA
    		// 而这个的默认大小为DEFAULT_CAPACITY
        int minExpand = (elementData != DEFAULTCAPACITY_EMPTY_ELEMENTDATA)
            ? 0
            : DEFAULT_CAPACITY;
				
				// 
        if (minCapacity > minExpand) {
            ensureExplicitCapacity(minCapacity);
        }
    }

		// calculateCapacity : 计算容量
    private static int calculateCapacity(Object[] elementData, int minCapacity) {
    		// 无参构造规定容量不能小于默认容量10
        if (elementData == DEFAULTCAPACITY_EMPTY_ELEMENTDATA) {
            return Math.max(DEFAULT_CAPACITY, minCapacity);
        }
        return minCapacity;
    }

    private void ensureCapacityInternal(int minCapacity) {
        ensureExplicitCapacity(calculateCapacity(elementData, minCapacity));
    }
	
		// ensureExplicitCapacity : 确定明确的容量
    private void ensureExplicitCapacity(int minCapacity) {
    		// 修改数+1，调用此方法的方法不用再写这一步
        modCount++;

				// 如果需要的最小容量大于数组长度，则扩容
        if (minCapacity - elementData.length > 0)
            grow(minCapacity);
    }

    /**
		*要分配的数组的最大大小。
		*有些虚拟机在一个数组中保留一些标题词。
		*尝试分配更大的数组可能会导致
		* OutOfMemoryError:请求的数组大小超过VM限制
     */
    // 由于VM的限制，一般容量不要超过MAX_ARRAY_SIZE，但最大值可以是Integer.MAX_VALUE
    private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;

		//增加容量，以确保至少可以容纳由minCapacity指定的元素数量
    private void grow(int minCapacity) {
        int oldCapacity = elementData.length;
        
        // oldCapacity >> 1 = oldCapacity * 1/2
        // oldCapacity + oldCapacity / 2 = oldCapacity * 3/2
        int newCapacity = oldCapacity + (oldCapacity >> 1);
        
        // 计算的新容量小于要求的最小容量(minCapacity)，则选择最小容量
        // 如果大于，使用计算的容量
        if (newCapacity - minCapacity < 0)
            newCapacity = minCapacity;
            
        // 如果newCapacity大于MAX_ARRAY_SIZE，就尝试用最小容量
       	// 若minCapacity也超过了MAX_ARRAY_SIZE，则用Integer.MAX_VALUE
        if (newCapacity - MAX_ARRAY_SIZE > 0)
            newCapacity = hugeCapacity(minCapacity);
            
        // minCapacity通常接近于size，所以复制效率较高
        elementData = Arrays.copyOf(elementData, newCapacity);
    }

    private static int hugeCapacity(int minCapacity) {
        if (minCapacity < 0) // 溢出
            throw new OutOfMemoryError();
        // 最大值可以是 Integer.MAX_VALUE
        return (minCapacity > MAX_ARRAY_SIZE) ?
            Integer.MAX_VALUE :
            MAX_ARRAY_SIZE;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

		// 包含至少一个就行
    public boolean contains(Object o) {
        return indexOf(o) >= 0;
    }

		// 返回第一个目标元素的下标（可能存在多个），没有返回-1
    public int indexOf(Object o) {
        if (o == null) {
        		// 证实：ArrayList可以保存多个null，因为数组中元素可以是null
            for (int i = 0; i < size; i++)
                if (elementData[i]==null)
                    return i;
        } else {
            for (int i = 0; i < size; i++)
            		// 没有 o == elementData[i]
                if (o.equals(elementData[i]))
                    return i;
        }
        return -1;
    }

		// 从后遍历，返回最后一个目标元素的下标，没有返回-1
    public int lastIndexOf(Object o) {
        if (o == null) {
        		// 从后遍历
            for (int i = size-1; i >= 0; i--)
                if (elementData[i]==null)
                    return i;
        } else {
            for (int i = size-1; i >= 0; i--)
                if (o.equals(elementData[i]))
                    return i;
        }
        return -1;
    }

    public Object clone() {
        try {
        		// 调用Object.clone()：浅克隆
            ArrayList<?> v = (ArrayList<?>) super.clone();
            v.elementData = Arrays.copyOf(elementData, size);
            v.modCount = 0;
            return v;
        } catch (CloneNotSupportedException e) {
            // 这不应该发生，因为我们是可克隆的
            throw new InternalError(e);
        }
    }

		// elementData的浅克隆（测试得出）
    public Object[] toArray() {    	
        return Arrays.copyOf(elementData, size);
    }
  //public static <T> T[] copyOf(T[] original, int newLength) {
	//    return (T[]) copyOf(original, newLength, original.getClass());
	//}

    @SuppressWarnings("unchecked")
    public <T> T[] toArray(T[] a) {
    		// 数据可能会丢失，根据用户给的数组确定，a.length
        if (a.length < size)
						// 返回的数组的长度为size，但a的长度不够保存
						// 所以会有部分数据丢失，且a数组被填满
            return (T[]) Arrays.copyOf(elementData, size, a.getClass());
        
        // 返回的数组的长度为a.length
        System.arraycopy(elementData, 0, a, 0, size);
        if (a.length > size)
        		// 若a.length = size，不会执行这一步
        		// 数组下标从 0 到 size - 1，共size个
            a[size] = null;
        return a;
    }

    @SuppressWarnings("unchecked")
    E elementData(int index) {
        return (E) elementData[index];
    }

    public E get(int index) {
        rangeCheck(index);

        return elementData(index);
    }
		
    public E set(int index, E element) {
        rangeCheck(index);

        E oldValue = elementData(index);
        elementData[index] = element;
    		// oldValue 可能为 null    
        return oldValue;
    }

    public boolean add(E e) {
    		// 添加前判断容量够不够，不够扩容；最重要的是增量modCount
        ensureCapacityInternal(size + 1);
        elementData[size++] = e;
        return true;
    }

    public void add(int index, E element) {
        rangeCheckForAdd(index);

				// 添加前判断容量够不够，不够扩容；最重要的是增量modCount
        ensureCapacityInternal(size + 1);
        // 数组元素后移
        // 把elementData从index开始的元素复制到elementData数组从index + 1开始的size - index个
        System.arraycopy(elementData, index, elementData, index + 1,
                         size - index);
        elementData[index] = element;
        size++;
    }

		// 删除指定下标元素
    public E remove(int index) {
        rangeCheck(index);

				// 修改数+1
        modCount++;
        // 取得要删除的数据
        E oldValue = elementData(index);

				// 需要移动的个数
        int numMoved = size - index - 1;
        if (numMoved > 0)
        		// 这里直接就覆盖了该元素，不用再进行删除的那一步
            System.arraycopy(elementData, index+1, elementData, index,
                             numMoved);
       	// 清除，让GC做它的工作
        elementData[--size] = null;

        return oldValue;
    }

		// 删除第一个匹配的元素（从头遍历），没有则返回false
    public boolean remove(Object o) {
        if (o == null) {
        		// 从头开始遍历，找到第一个匹配的元素null
            for (int index = 0; index < size; index++)
                if (elementData[index] == null) {
                		// 快速删除
                    fastRemove(index);
                    return true;
                }
        } else {
        		// 同上
            for (int index = 0; index < size; index++)
                if (o.equals(elementData[index])) {
                    fastRemove(index);
                    return true;
                }
        }
        return false;
    }

		// 工具内部的删除，一定是对象匹配才能删，所以不用rangeCheck(index)
    private void fastRemove(int index) {
        modCount++;
        int numMoved = size - index - 1;
        if (numMoved > 0)
        		// 用覆盖代替删除
            System.arraycopy(elementData, index+1, elementData, index,
                             numMoved);
        elementData[--size] = null;
    }

		// 暴力删除，让gc回收
    public void clear() {
        modCount++;
				
        for (int i = 0; i < size; i++)
            elementData[i] = null;

        size = 0;
    }

		// 所以元素尾加到elementData中
    public boolean addAll(Collection<? extends E> c) {
        Object[] a = c.toArray();
        int numNew = a.length;
        // 确保容量足够
        ensureCapacityInternal(size + numNew);  // 增量modCount
        System.arraycopy(a, 0, elementData, size, numNew);
        size += numNew;
        return numNew != 0;
    }
		
		// 所以元素插入到elementData数组的指定index中
    public boolean addAll(int index, Collection<? extends E> c) {
    		// 检查下标合法性
        rangeCheckForAdd(index);

				// 将List变为Object数组
        Object[] a = c.toArray();
        int numNew = a.length;
        // 确保容量足够
        ensureCapacityInternal(size + numNew);  // 增量modCount

        int numMoved = size - index;
        if (numMoved > 0)
        		// 先将位置空出来，为插入作准备
            System.arraycopy(elementData, index, elementData, index + numNew,
                             numMoved);
				// 这里省去了for循环的赋值
        System.arraycopy(a, 0, elementData, index, numNew);
        size += numNew;
        return numNew != 0;
    }

		// 指定区间删除
    protected void removeRange(int fromIndex, int toIndex) {
        modCount++;
        int numMoved = size - toIndex;
        // 定量元素前移，toIndex后的移动到fromIndex开始的numMoved个位置上
        System.arraycopy(elementData, toIndex, elementData, fromIndex,
                         numMoved);
				
				// newSize为剩余元素个数
        int newSize = size - (toIndex-fromIndex);
        for (int i = newSize; i < size; i++) {
            elementData[i] = null;
        }
        size = newSize;
    }
		
		// 封装的下标越界检查，被get()、remove()使用
    private void rangeCheck(int index) {
    		// 并没有检查下标是否小于0，因为在elementData[index]操作中，
    		// jvm会帮我们检查越界问题
        if (index >= size)
            throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
    }

		// 由add()和addAll()使用的版本
		// add()和addAll()中调用了native的System.copyOf()方法
		// 所以需要判断 index < 0，与rangeCheck(int index)不同
    private void rangeCheckForAdd(int index) {
        if (index > size || index < 0)
            throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
    }

		// 构造异常的具体信息
    private String outOfBoundsMsg(int index) {
        return "Index: "+index+", Size: "+size;
    }

    /**
    public static <T> T requireNonNull(T obj) {
        if (obj == null)
            throw new NullPointerException();
        return obj;
    }
     */
    public boolean removeAll(Collection<?> c) {
    		// 判断是否为空引用
        Objects.requireNonNull(c);
        return batchRemove(c, false);
    }

		// 删除
    public boolean retainAll(Collection<?> c) {
        Objects.requireNonNull(c);
        return batchRemove(c, true);
    }

		// complement : 是否保留elementData中与集合c中相同的元素
    private boolean batchRemove(Collection<?> c, boolean complement) {
        final Object[] elementData = this.elementData;
        int r = 0, w = 0;
        boolean modified = false;
        try {
            for (; r < size; r++)
            		// 如果complement为true，则保留
                if (c.contains(elementData[r]) == complement)
                    elementData[w++] = elementData[r];
        } finally {
            // 保持与AbstractCollection的行为兼容性，即使c.contains()抛出异常。
           	// r != size 说明没有执行完循环，将后面没有读到的全部保留，修改也是失败的
            if (r != size) {
                System.arraycopy(elementData, r,
                                 elementData, w,
                                 size - r);
                w += size - r;
            }
            // 删除了部分元素，需要清空后面的
            if (w != size) {
                // clear to let GC do its work
                for (int i = w; i < size; i++)
                    elementData[i] = null;
                modCount += size - w;
                size = w;
                modified = true;
            }
        }
        return modified;
    }

		// 序列化
    private void writeObject(java.io.ObjectOutputStream s)
        throws java.io.IOException{
        // Write out element count, and any hidden stuff
        int expectedModCount = modCount;
        s.defaultWriteObject();

        // Write out size as capacity for behavioural compatibility with clone()
        s.writeInt(size);

        // Write out all elements in the proper order.
        for (int i=0; i<size; i++) {
            s.writeObject(elementData[i]);
        }

        if (modCount != expectedModCount) {
            throw new ConcurrentModificationException();
        }
    }

		// 反序列化
    private void readObject(java.io.ObjectInputStream s)
        throws java.io.IOException, ClassNotFoundException {
        elementData = EMPTY_ELEMENTDATA;

        // Read in size, and any hidden stuff
        s.defaultReadObject();

        // Read in capacity
        s.readInt(); // ignored

        if (size > 0) {
            // be like clone(), allocate array based upon size not capacity
            int capacity = calculateCapacity(elementData, size);
            SharedSecrets.getJavaOISAccess().checkArray(s, Object[].class, capacity);
            ensureCapacityInternal(size);

            Object[] a = elementData;
            // Read in all elements in the proper order.
            for (int i=0; i<size; i++) {
                a[i] = s.readObject();
            }
        }
    }
    
    // 返回从index开始列表信息
    public ListIterator<E> listIterator(int index) {
        if (index < 0 || index > size)
            throw new IndexOutOfBoundsException("Index: "+index);
        return new ListItr(index);
    }

		// 返回从下标0开始的列表信息，即，全部信息
    public ListIterator<E> listIterator() {
        return new ListItr(0);
    }

		// 按适当的顺序对列表中的元素返回一个迭代器。
    public Iterator<E> iterator() {
        return new Itr();
    }

    private class Itr implements Iterator<E> {
        int cursor;       // 下一个要返回的元素的索引
        int lastRet = -1; // 最后一个返回元素的索引(上一个返回的元素);如没有返回-1
        int expectedModCount = modCount;  // 确定修改数，保证线程安全

        Itr() {}

        public boolean hasNext() {
            return cursor != size;
        }

        @SuppressWarnings("unchecked")
        public E next() {
        		// 检查修改数是否相同，判断线程的安全性
            checkForComodification();
            // 第一次为0，因为int成员初始值为0
            int i = cursor;
            if (i >= size)
                throw new NoSuchElementException();
            Object[] elementData = ArrayList.this.elementData;
            // 上面已经判断i < size成立，而size <= elementData.length
            // 所以下面条件成立则说明进行了容量收缩，线程不安全
            if (i >= elementData.length)
                throw new ConcurrentModificationException();
            cursor = i + 1;
            return (E) elementData[lastRet = i];
        }

        public void remove() {
            if (lastRet < 0)
                throw new IllegalStateException();
            checkForComodification();

            try {
                ArrayList.this.remove(lastRet);
                cursor = lastRet;
                lastRet = -1;
                // 保证修改数同步，不会造成线程不安全
                expectedModCount = modCount;
            } catch (IndexOutOfBoundsException ex) {
            		// 如果下标越界了，那么肯定是对elementData进行了删除等操作
                throw new ConcurrentModificationException();
            }
        }
	
				// 显式的forEach调用，遍历剩下的所有元素
        @Override
        @SuppressWarnings("unchecked")
        public void forEachRemaining(Consumer<? super E> consumer) {
        		// 判断consumer是否为 null
            Objects.requireNonNull(consumer);
            // final关键字修饰，不可更改
            final int size = ArrayList.this.size;
            // 接着之前的继续遍历
            int i = cursor;
            if (i >= size) {
                return;
            }
            final Object[] elementData = ArrayList.this.elementData;
            if (i >= elementData.length) {
                throw new ConcurrentModificationException();
            }
            // 遍历后面的元素，并判断修改数是否相同，如果不同立即停止循环
            while (i != size && modCount == expectedModCount) {
                consumer.accept((E) elementData[i++]);
            }
            // 确定下一个返回值的下标和上一个返回值的下标
            cursor = i;
            lastRet = i - 1;
            // 检查线程同步
            checkForComodification();
        }
	
				// 通过修改数的对比，判断是否对原数组进行了修改
        // 在产生Itr对象时，就确定了expectedModCount的大小
        // 如果在外部类进行了修改操作，就会被认为是线程不安全的行为
        final void checkForComodification() {
            if (modCount != expectedModCount)
                throw new ConcurrentModificationException();
        }
    }

		// 强化版的Itr，可以返回下标
    private class ListItr extends Itr implements ListIterator<E> {
        ListItr(int index) {
            super();
            cursor = index;
        }

				// cursor == 0说明当前处于第一个数组下标
        public boolean hasPrevious() {
            return cursor != 0;
        }

        public int nextIndex() {
            return cursor;
        }

        public int previousIndex() {
            return cursor - 1;
        }

        @SuppressWarnings("unchecked")
        public E previous() {
            checkForComodification();
            int i = cursor - 1;
            if (i < 0)
                throw new NoSuchElementException();
            Object[] elementData = ArrayList.this.elementData;
            if (i >= elementData.length)
                throw new ConcurrentModificationException();
            cursor = i;
            return (E) elementData[lastRet = i];
        }

				// 将元素放在上一个返回元素的下标下，不存在修改数的改变，线程安全
        public void set(E e) {
            if (lastRet < 0)
                throw new IllegalStateException();
            checkForComodification();

            try {
                ArrayList.this.set(lastRet, e);
            } catch (IndexOutOfBoundsException ex) {
                throw new ConcurrentModificationException();
            }
        }

				// 尾加到末尾，同步修改数，线程安全
        public void add(E e) {
            checkForComodification();

            try {
                int i = cursor;
                ArrayList.this.add(i, e);
                cursor = i + 1;
                lastRet = -1;
                expectedModCount = modCount;
            } catch (IndexOutOfBoundsException ex) {
                throw new ConcurrentModificationException();
            }
        }
    }

		// 返回一个指定范围的List
    public List<E> subList(int fromIndex, int toIndex) {
        subListRangeCheck(fromIndex, toIndex, size);
        return new SubList(this, 0, fromIndex, toIndex);
    }
	
		// 判断下标合法性
    static void subListRangeCheck(int fromIndex, int toIndex, int size) {
        if (fromIndex < 0)
            throw new IndexOutOfBoundsException("fromIndex = " + fromIndex);
        if (toIndex > size)
            throw new IndexOutOfBoundsException("toIndex = " + toIndex);
        if (fromIndex > toIndex)
            throw new IllegalArgumentException("fromIndex(" + fromIndex +
                                               ") > toIndex(" + toIndex + ")");
    }

    private class SubList extends AbstractList<E> implements RandomAccess {
    		// 被复制的List
        private final AbstractList<E> parent;
        // 被复制数组的偏移量
        private final int parentOffset;
        // 表示本对象的第一个元素在parent中的偏移量
        // new SubList(this, offset, fromIndex, toIndex)相当于得到一个起始下标为2*offset的list
        // 在subList上不管递归分割多少次，实质都是对parent进行操作，只是offset变了而已
        private final int offset;
        // 复制元素个数
        int size;

        SubList(AbstractList<E> parent,
                int offset, int fromIndex, int toIndex) {
            this.parent = parent;
            this.parentOffset = fromIndex;
            this.offset = offset + fromIndex;
            this.size = toIndex - fromIndex;
            // 同步修改数
            this.modCount = ArrayList.this.modCount;
        }

        public E set(int index, E e) {
            rangeCheck(index);
            checkForComodification();
            // 替换旧值
            E oldValue = ArrayList.this.elementData(offset + index);
            ArrayList.this.elementData[offset + index] = e;
            return oldValue;
        }

        public E get(int index) {
            rangeCheck(index);
            checkForComodification();
            return ArrayList.this.elementData(offset + index);
        }

        public int size() {
            checkForComodification();
            return this.size;
        }

        public void add(int index, E e) {
            rangeCheckForAdd(index);
            checkForComodification();
            parent.add(parentOffset + index, e);
            // 内部添加，保持修改数同步
            this.modCount = parent.modCount;
            this.size++;
        }

        public E remove(int index) {
            rangeCheck(index);
            checkForComodification();
            E result = parent.remove(parentOffset + index);
            this.modCount = parent.modCount;
            this.size--;
            return result;
        }

        protected void removeRange(int fromIndex, int toIndex) {
            checkForComodification();
            parent.removeRange(parentOffset + fromIndex,
                               parentOffset + toIndex);
            this.modCount = parent.modCount;
            this.size -= toIndex - fromIndex;
        }

        public boolean addAll(Collection<? extends E> c) {
            return addAll(this.size, c);
        }

        public boolean addAll(int index, Collection<? extends E> c) {
            rangeCheckForAdd(index);
            int cSize = c.size();
            if (cSize==0)
                return false;

            checkForComodification();
            parent.addAll(parentOffset + index, c);
            this.modCount = parent.modCount;
            this.size += cSize;
            return true;
        }

        public Iterator<E> iterator() {
            return listIterator();
        }

        public ListIterator<E> listIterator(final int index) {
            checkForComodification();
            rangeCheckForAdd(index);
            final int offset = this.offset;

            return new ListIterator<E>() {
                int cursor = index;
                int lastRet = -1;
                int expectedModCount = ArrayList.this.modCount;

                public boolean hasNext() {
                    return cursor != SubList.this.size;
                }

                @SuppressWarnings("unchecked")
                public E next() {
                    checkForComodification();
                    int i = cursor;
                    if (i >= SubList.this.size)
                        throw new NoSuchElementException();
                    Object[] elementData = ArrayList.this.elementData;
                    if (offset + i >= elementData.length)
                        throw new ConcurrentModificationException();
                    cursor = i + 1;
                    return (E) elementData[offset + (lastRet = i)];
                }

                public boolean hasPrevious() {
                    return cursor != 0;
                }

                @SuppressWarnings("unchecked")
                public E previous() {
                    checkForComodification();
                    int i = cursor - 1;
                    if (i < 0)
                        throw new NoSuchElementException();
                    Object[] elementData = ArrayList.this.elementData;
                    if (offset + i >= elementData.length)
                        throw new ConcurrentModificationException();
                    cursor = i;
                    return (E) elementData[offset + (lastRet = i)];
                }

                @SuppressWarnings("unchecked")
                public void forEachRemaining(Consumer<? super E> consumer) {
                    Objects.requireNonNull(consumer);
                    final int size = SubList.this.size;
                    int i = cursor;
                    if (i >= size) {
                        return;
                    }
                    final Object[] elementData = ArrayList.this.elementData;
                    if (offset + i >= elementData.length) {
                        throw new ConcurrentModificationException();
                    }
                    while (i != size && modCount == expectedModCount) {
                        consumer.accept((E) elementData[offset + (i++)]);
                    }

                    lastRet = cursor = i;
                    checkForComodification();
                }

                public int nextIndex() {
                    return cursor;
                }

                public int previousIndex() {
                    return cursor - 1;
                }

                public void remove() {
                    if (lastRet < 0)
                        throw new IllegalStateException();
                    checkForComodification();

                    try {
                        SubList.this.remove(lastRet);
                        cursor = lastRet;
                        lastRet = -1;
                        expectedModCount = ArrayList.this.modCount;
                    } catch (IndexOutOfBoundsException ex) {
                        throw new ConcurrentModificationException();
                    }
                }

                public void set(E e) {
                    if (lastRet < 0)
                        throw new IllegalStateException();
                    checkForComodification();

                    try {
                        ArrayList.this.set(offset + lastRet, e);
                    } catch (IndexOutOfBoundsException ex) {
                        throw new ConcurrentModificationException();
                    }
                }

                public void add(E e) {
                    checkForComodification();

                    try {
                        int i = cursor;
                        SubList.this.add(i, e);
                        cursor = i + 1;
                        lastRet = -1;
                        expectedModCount = ArrayList.this.modCount;
                    } catch (IndexOutOfBoundsException ex) {
                        throw new ConcurrentModificationException();
                    }
                }

                final void checkForComodification() {
                    if (expectedModCount != ArrayList.this.modCount)
                        throw new ConcurrentModificationException();
                }
            };
        }

        public List<E> subList(int fromIndex, int toIndex) {
            subListRangeCheck(fromIndex, toIndex, size);
            return new SubList(this, offset, fromIndex, toIndex);
        }

        private void rangeCheck(int index) {
            if (index < 0 || index >= this.size)
                throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
        }

        private void rangeCheckForAdd(int index) {
            if (index < 0 || index > this.size)
                throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
        }

        private String outOfBoundsMsg(int index) {
            return "Index: "+index+", Size: "+this.size;
        }

        private void checkForComodification() {
            if (ArrayList.this.modCount != this.modCount)
                throw new ConcurrentModificationException();
        }

        public Spliterator<E> spliterator() {
            checkForComodification();
            return new ArrayListSpliterator<E>(ArrayList.this, offset,
                                               offset + this.size, this.modCount);
        }
    }

		// 显示的forEach
    @Override
    public void forEach(Consumer<? super E> action) {
        Objects.requireNonNull(action);
        final int expectedModCount = modCount;
        @SuppressWarnings("unchecked")
        final E[] elementData = (E[]) this.elementData;
        final int size = this.size;
        // modCount == expectedModCount相比hashmap是加强,hashmap不会打断
        for (int i=0; modCount == expectedModCount && i < size; i++) {
            action.accept(elementData[i]);
        }
        if (modCount != expectedModCount) {
            throw new ConcurrentModificationException();
        }
    }

    @Override
    public Spliterator<E> spliterator() {
        return new ArrayListSpliterator<>(this, 0, -1, 0);
    }

    static final class ArrayListSpliterator<E> implements Spliterator<E> {

        /*
				*如果arraylist是不可变的，或者在结构上是不可变的(没有添加、删除等)，
				我们可以用Arrays.spliterator实现它们的spliterator。相反，
				我们在遍历过程中尽可能多地检测干扰，同时又不会牺牲太多性能。
				我们主要依靠modCounts。这些方法并不能保证检测到并发性违规，
				而且有时对于线程内的干扰过于保守，但是在实践中可以检测到足够多的问题。
				为了实现这一点，我们
				(1)延迟初始化fence并等待modcount，直到需要提交到我们正在检查的状态的最新点;从而提高精度。(这不适用于创建具有当前惰性值的spliterators的子列表)。
				(2)我们只在forEach结尾执行一个 ConcurrentModificationException检查(最敏感的性能方法)。
				当使用forEach (与迭代器相反)时，我们通常只能在操作之后检测干扰，而不是之前。进一步的
				cmtrigger检查适用于所有其他可能的违反假设的情况，例如null或太小的elementData数组，
				因为它的size()，而这只会因为干扰而发生。这使得forEach的内部循环无需进一步检查就可以运行，
				并且简化了lambda-resolution。虽然这确实需要数量的检查，但请注意，在 list.stream().forEach(a)的常见情况下，除了在forEach内部，
				其他任何地方都不会发生检查或其他计算。其他不太常用的方法不能利用这些流线。
         */

        private final ArrayList<E> list;
        private int index; // 当前索引，预先修改/分割
        private int fence; // -1直到使用;然后是最后一个索引
        private int expectedModCount; // 设置栅栏时初始化

        /** 创建覆盖给定范围的新spliterator */
        ArrayListSpliterator(ArrayList<E> list, int origin, int fence,
                             int expectedModCount) {
            this.list = list; // OK if null unless traversed
            this.index = origin;
            this.fence = fence;
            this.expectedModCount = expectedModCount;
        }

        private int getFence() { // 在第一次使用时将栅栏初始化为大小
            int hi; // (在方法forEach中出现一个专门的变体)
            ArrayList<E> lst;
            if ((hi = fence) < 0) {
                if ((lst = list) == null)
                    hi = fence = 0;
                else {
                    expectedModCount = lst.modCount;
                    hi = fence = lst.size;
                }
            }
            return hi;
        }

        public ArrayListSpliterator<E> trySplit() {
        		// (lo + hi) >>> 1 分成两份
            int hi = getFence(), lo = index, mid = (lo + hi) >>> 1;
            return (lo >= mid) ? null : // 把范围分成两半，除非太小
                new ArrayListSpliterator<E>(list, lo, index = mid,
                                            expectedModCount);
        }

				// 读取下一个
        public boolean tryAdvance(Consumer<? super E> action) {
            if (action == null)
                throw new NullPointerException();
            int hi = getFence(), i = index;
            if (i < hi) {
                index = i + 1;
                @SuppressWarnings("unchecked") 
                E e = (E)list.elementData[i];
                action.accept(e);
                if (list.modCount != expectedModCount)
                    throw new ConcurrentModificationException();
                return true;
            }
            return false;
        }

        public void forEachRemaining(Consumer<? super E> action) {
            int i, hi, mc; // 从循环中提升入口和检查
            ArrayList<E> lst; Object[] a;
            if (action == null)
                throw new NullPointerException();
            if ((lst = list) != null && (a = lst.elementData) != null) {
            		// 未初始化范围，则遍历全部
                if ((hi = fence) < 0) {
                    mc = lst.modCount;
                    hi = lst.size;
                }
                else
                		// 保存list的修改数，防止外部修改
                    mc = expectedModCount;
                // 保证遍历范围
                if ((i = index) >= 0 && (index = hi) <= a.length) {
                    for (; i < hi; ++i) {
                        @SuppressWarnings("unchecked") 
                        E e = (E) a[i];
                        action.accept(e);
                    }
                    if (lst.modCount == mc)
                        return;
                }
            }
            throw new ConcurrentModificationException();
        }
				// 当前估计范围
        public long estimateSize() {
            return (long) (getFence() - index);
        }
				
        public int characteristics() {
            return Spliterator.ORDERED | Spliterator.SIZED | Spliterator.SUBSIZED;
        }
    }

    @Override
    public boolean removeIf(Predicate<? super E> filter) {
        Objects.requireNonNull(filter);
				//找出需要删除的元素，在此阶段从筛选器中抛出的任何异常，将不修改集合
        int removeCount = 0;
        final BitSet removeSet = new BitSet(size);
        final int expectedModCount = modCount;
        final int size = this.size;
        for (int i=0; modCount == expectedModCount && i < size; i++) {
            @SuppressWarnings("unchecked")
            final E element = (E) elementData[i];
            // 过滤器的作用，如果fileter有这个元素，则将下标保存
            if (filter.test(element)) {
                removeSet.set(i);
                removeCount++;
            }
        }
        if (modCount != expectedModCount) {
            throw new ConcurrentModificationException();
        }

        // 将剩余的元素移动到被移除元素所留下的空间上
        final boolean anyToRemove = removeCount > 0;
        if (anyToRemove) {
        		// 不该删除的元素的个数
            final int newSize = size - removeCount;
            for (int i=0, j=0; (i < size) && (j < newSize); i++, j++) {
                i = removeSet.nextClearBit(i);
                elementData[j] = elementData[i];
            }
            for (int k=newSize; k < size; k++) {
                elementData[k] = null;  // Let gc do its work
            }
            this.size = newSize;
            if (modCount != expectedModCount) {
                throw new ConcurrentModificationException();
            }
            modCount++;
        }

        return anyToRemove;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void replaceAll(UnaryOperator<E> operator) {
        Objects.requireNonNull(operator);
        final int expectedModCount = modCount;
        final int size = this.size;
        // 替换值
        for (int i=0; modCount == expectedModCount && i < size; i++) {
            elementData[i] = operator.apply((E) elementData[i]);
        }
        if (modCount != expectedModCount) {
            throw new ConcurrentModificationException();
        }
        modCount++;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void sort(Comparator<? super E> c) {
        final int expectedModCount = modCount;
        // 全排序
        Arrays.sort((E[]) elementData, 0, size, c);
        if (modCount != expectedModCount) {
            throw new ConcurrentModificationException();
        }
        modCount++;
    }
}
