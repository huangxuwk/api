package java.util;

import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import sun.misc.SharedSecrets;

/**
	*�ɵ�����С������ʵ��List�ӿڡ�ʵ����
	*���п�ѡ���б����������������Ԫ�أ�����
	* < tt >��< / tt >������ʵ��List�ӿ��⣬
	������ṩ�˲��������С�ķ���
	*�����ڲ��洢�б����������൱��
	* Vector�������ǲ�ͬ���ġ�)
 *
 * <p>The <tt>size</tt>, <tt>isEmpty</tt>, <tt>get</tt>, <tt>set</tt>,
 * <tt>iterator</tt>, and <tt>listIterator</tt> operations run in constant
	* �������� listIterator�����㶨����
	*ʱ�䡣�ӷ�������ƽ̯����ʱ�䣬
	Ҳ����˵�����n��Ԫ����ҪO(n)��ʱ�䡣���������Ĳ���
	*������ʱ��������(���Ե�˵)���������ӱȽϵ�
	*����LinkedListʵ�֡�
 *
	ÿ��ArrayListʵ����һ����������������
	�������б��д洢Ԫ�ص�����Ĵ�С��������
	*�������б��С��ͬ����Ԫ�ر���ӵ�ArrayList��ʱ��
	*�����Զ��������������ߵ�ϸ��û��
	*���������һ��Ԫ���Ѿ�̯������ʵ
	*ʱ��ɱ���
 *
	һ��Ӧ�ó����������һ��ArrayListʵ��������
	*��ʹ��ensureCapacity��Ӵ���Ԫ��֮ǰ
	*����������ܻ�����������·����������
 *
	ע�����ʵ�ֲ���ͬ���ġ�
	*�������߳�ͬʱ����һ��ArrayListʵ����
	����������һ���̴߳ӽṹ���޸����б�
	* �������ⲿͬ����(�ṹ�޸���
	�κ���ӻ�ɾ��һ������Ԫ�صĲ���������ʽ����
	*�����������еĴ�С;��������Ԫ�ص�ֵ�ǲ��е�
	*�ṹ������)��ͨ������
	ͬ����һЩ������Ȼ��װ���б�
 *
	*��������ڴ��������Ӧʹ��
	* {@link Collections#synchronizedList Collections.synchronizedList}
	*������������ڴ���ʱ��ɣ��Է�ֹ����
	*���б�ķ�ͬ������:
	* List List =Collections.synchronizedList(newArrayList (��));
 *
	* < p > < name = "����ʧ��" >
	*������{@link #iterator() iterator}�ͷ��صĵ�����
	* {@link #listIterator(int) listIterator}�ķ�����fail-fast:
	*����ڵ�����֮����κ�ʱ����б�����˽ṹ�ϵ��޸�
	*���κη�ʽ����������ͨ���������Լ�����
	* {@link ListIterator#remove()ɾ��}��
	* {@link ListIterator#add(Object) add}���������������׳�һ��
	* {@link ConcurrentModificationException}�����ǣ����
	*�����޸ģ�������ʧ�ܵø�����ɾ�
	*������ð��arbit�ķ���
	 *
	ע�⣬���ܱ�֤�������Ĺ���-������Ϊ
	һ����˵�����ǲ����������κ��ϸ�ı�֤
	*���ڲ�ͬ���Ĳ����޸ġ�����ʧ�ܵ�����
	*�����Ŭ���׳�{@code ConcurrentModificationException}��
	��ˣ�дһ�������ڴ˵ĳ����Ǵ����
	*��ȷ���쳣:�������Ŀ���ʧЧ��Ϊ
	*Ӧ��ֻ���ڼ��bug��
 */

public class ArrayList<E> extends AbstractList<E>
        implements List<E>, RandomAccess, Cloneable, java.io.Serializable
{
    private static final long serialVersionUID = 8683452581122892189L;

		// Ĭ�ϳ�ʼ����
    private static final int DEFAULT_CAPACITY = 10;

		// ��ָ�����������Ϊ0��ʱ��ʹ�����������ֵ���ն������� 
    private static final Object[] EMPTY_ELEMENTDATA = {};

		// Ĭ��ʵ������ʱ��ʹ�ô˱�����ֵ���ն������� 
		// ��������EMPTY_ELEMENTDATA�����潫����ϸ����
    private static final Object[] DEFAULTCAPACITY_EMPTY_ELEMENTDATA = {};

		// �����洢���ݵ����飬��˽���Լ�Ƕ����ķ���
    transient Object[] elementData;

		// �Ѵ洢Ԫ�صĸ���
    private int size;

		// ���ι���
    public ArrayList(int initialCapacity) {
    		// δ����initialCapacity�Ĵ�С�������������ΪInteger.MAX_VALUE
        if (initialCapacity > 0) {
            this.elementData = new Object[initialCapacity];
        } else if (initialCapacity == 0) {
        		// ����Ϊ0������һ�������鸳ֵ����
            this.elementData = EMPTY_ELEMENTDATA;
        } else {
            throw new IllegalArgumentException("Illegal Capacity: "+
                                               initialCapacity);
        }
    }

		// ����һ����ʼ����Ϊ10�Ŀ��б�
    public ArrayList() {
    		// Ĭ������£���ֵ�ڶ��������飬��ʾ����ΪĬ�ϳ�ʼ����
    		// ��Ҳ����Ϊʲô����������������󣬾�Ϊ�˽������������
        this.elementData = DEFAULTCAPACITY_EMPTY_ELEMENTDATA;
    }

		// ����һ����������ʼ��������
    public ArrayList(Collection<? extends E> c) {
    		// �õ��������飬��ֵ����ԱelementData
        elementData = c.toArray();
        if ((size = elementData.length) != 0) {
            // c.toArray����(����ȷ��)������Object[]
            // ��������Stringʱ�����ص���String[]
            if (elementData.getClass() != Object[].class)
            		// �˷�������ϸ����
                elementData = Arrays.copyOf(elementData, size, Object[].class);
        } else {
            // �ÿ������滻������Ϊ0
            this.elementData = EMPTY_ELEMENTDATA;
        }
    }
  	// original: ԭ���飬newLength: ���Ƴ��ȣ�newType: �������������
	//public static <T,U> T[] copyOf(U[] original, int newLength, Class<? extends T[]> newType) {
	//    @SuppressWarnings("unchecked")
				// �����Object[].class����new Object[newLength]
				// ������ǣ���Array.newInstance(newType.getComponentType(), newLength);
	//    T[] copy = ((Object)newType == (Object)Object[].class)
	//        ? (T[]) new Object[newLength]
						// newType.getComponentType()���Եõ���������
	//        : (T[]) Array.newInstance(newType.getComponentType(), newLength);
				// ���Ƶĳ��Ȳ��ܳ���ԭ��������ֵ��������ܻ�����±�Խ�������
	//    System.arraycopy(original, 0, copy, 0,
	//                     Math.min(original.length, newLength));
	//    return copy;
	//}

    // ��elementData���ȱ�Ϊsize
    public void trimToSize() {
    		// �޸���+1
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
		�������ArrayListʵ���������������Ҫ�ģ���ȷ����������������������Ԫ��
		*����С��������ָ����
     */
    public void ensureCapacity(int minCapacity) {
    		// �ڹ��췽���в����˲��죬�޲ι���elementData = DEFAULTCAPACITY_EMPTY_ELEMENTDATA
    		// �������Ĭ�ϴ�СΪDEFAULT_CAPACITY
        int minExpand = (elementData != DEFAULTCAPACITY_EMPTY_ELEMENTDATA)
            ? 0
            : DEFAULT_CAPACITY;
				
				// 
        if (minCapacity > minExpand) {
            ensureExplicitCapacity(minCapacity);
        }
    }

		// calculateCapacity : ��������
    private static int calculateCapacity(Object[] elementData, int minCapacity) {
    		// �޲ι���涨��������С��Ĭ������10
        if (elementData == DEFAULTCAPACITY_EMPTY_ELEMENTDATA) {
            return Math.max(DEFAULT_CAPACITY, minCapacity);
        }
        return minCapacity;
    }

    private void ensureCapacityInternal(int minCapacity) {
        ensureExplicitCapacity(calculateCapacity(elementData, minCapacity));
    }
	
		// ensureExplicitCapacity : ȷ����ȷ������
    private void ensureExplicitCapacity(int minCapacity) {
    		// �޸���+1�����ô˷����ķ���������д��һ��
        modCount++;

				// �����Ҫ����С�����������鳤�ȣ�������
        if (minCapacity - elementData.length > 0)
            grow(minCapacity);
    }

    /**
		*Ҫ��������������С��
		*��Щ�������һ�������б���һЩ����ʡ�
		*���Է�������������ܻᵼ��
		* OutOfMemoryError:����������С����VM����
     */
    // ����VM�����ƣ�һ��������Ҫ����MAX_ARRAY_SIZE�������ֵ������Integer.MAX_VALUE
    private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;

		//������������ȷ�����ٿ���������minCapacityָ����Ԫ������
    private void grow(int minCapacity) {
        int oldCapacity = elementData.length;
        
        // oldCapacity >> 1 = oldCapacity * 1/2
        // oldCapacity + oldCapacity / 2 = oldCapacity * 3/2
        int newCapacity = oldCapacity + (oldCapacity >> 1);
        
        // �����������С��Ҫ�����С����(minCapacity)����ѡ����С����
        // ������ڣ�ʹ�ü��������
        if (newCapacity - minCapacity < 0)
            newCapacity = minCapacity;
            
        // ���newCapacity����MAX_ARRAY_SIZE���ͳ�������С����
       	// ��minCapacityҲ������MAX_ARRAY_SIZE������Integer.MAX_VALUE
        if (newCapacity - MAX_ARRAY_SIZE > 0)
            newCapacity = hugeCapacity(minCapacity);
            
        // minCapacityͨ���ӽ���size�����Ը���Ч�ʽϸ�
        elementData = Arrays.copyOf(elementData, newCapacity);
    }

    private static int hugeCapacity(int minCapacity) {
        if (minCapacity < 0) // ���
            throw new OutOfMemoryError();
        // ���ֵ������ Integer.MAX_VALUE
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

		// ��������һ������
    public boolean contains(Object o) {
        return indexOf(o) >= 0;
    }

		// ���ص�һ��Ŀ��Ԫ�ص��±꣨���ܴ��ڶ������û�з���-1
    public int indexOf(Object o) {
        if (o == null) {
        		// ֤ʵ��ArrayList���Ա�����null����Ϊ������Ԫ�ؿ�����null
            for (int i = 0; i < size; i++)
                if (elementData[i]==null)
                    return i;
        } else {
            for (int i = 0; i < size; i++)
            		// û�� o == elementData[i]
                if (o.equals(elementData[i]))
                    return i;
        }
        return -1;
    }

		// �Ӻ�������������һ��Ŀ��Ԫ�ص��±꣬û�з���-1
    public int lastIndexOf(Object o) {
        if (o == null) {
        		// �Ӻ����
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
        		// ����Object.clone()��ǳ��¡
            ArrayList<?> v = (ArrayList<?>) super.clone();
            v.elementData = Arrays.copyOf(elementData, size);
            v.modCount = 0;
            return v;
        } catch (CloneNotSupportedException e) {
            // �ⲻӦ�÷�������Ϊ�����ǿɿ�¡��
            throw new InternalError(e);
        }
    }

		// elementData��ǳ��¡�����Եó���
    public Object[] toArray() {    	
        return Arrays.copyOf(elementData, size);
    }
  //public static <T> T[] copyOf(T[] original, int newLength) {
	//    return (T[]) copyOf(original, newLength, original.getClass());
	//}

    @SuppressWarnings("unchecked")
    public <T> T[] toArray(T[] a) {
    		// ���ݿ��ܻᶪʧ�������û���������ȷ����a.length
        if (a.length < size)
						// ���ص�����ĳ���Ϊsize����a�ĳ��Ȳ�������
						// ���Ի��в������ݶ�ʧ����a���鱻����
            return (T[]) Arrays.copyOf(elementData, size, a.getClass());
        
        // ���ص�����ĳ���Ϊa.length
        System.arraycopy(elementData, 0, a, 0, size);
        if (a.length > size)
        		// ��a.length = size������ִ����һ��
        		// �����±�� 0 �� size - 1����size��
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
    		// oldValue ����Ϊ null    
        return oldValue;
    }

    public boolean add(E e) {
    		// ���ǰ�ж��������������������ݣ�����Ҫ��������modCount
        ensureCapacityInternal(size + 1);
        elementData[size++] = e;
        return true;
    }

    public void add(int index, E element) {
        rangeCheckForAdd(index);

				// ���ǰ�ж��������������������ݣ�����Ҫ��������modCount
        ensureCapacityInternal(size + 1);
        // ����Ԫ�غ���
        // ��elementData��index��ʼ��Ԫ�ظ��Ƶ�elementData�����index + 1��ʼ��size - index��
        System.arraycopy(elementData, index, elementData, index + 1,
                         size - index);
        elementData[index] = element;
        size++;
    }

		// ɾ��ָ���±�Ԫ��
    public E remove(int index) {
        rangeCheck(index);

				// �޸���+1
        modCount++;
        // ȡ��Ҫɾ��������
        E oldValue = elementData(index);

				// ��Ҫ�ƶ��ĸ���
        int numMoved = size - index - 1;
        if (numMoved > 0)
        		// ����ֱ�Ӿ͸����˸�Ԫ�أ������ٽ���ɾ������һ��
            System.arraycopy(elementData, index+1, elementData, index,
                             numMoved);
       	// �������GC�����Ĺ���
        elementData[--size] = null;

        return oldValue;
    }

		// ɾ����һ��ƥ���Ԫ�أ���ͷ��������û���򷵻�false
    public boolean remove(Object o) {
        if (o == null) {
        		// ��ͷ��ʼ�������ҵ���һ��ƥ���Ԫ��null
            for (int index = 0; index < size; index++)
                if (elementData[index] == null) {
                		// ����ɾ��
                    fastRemove(index);
                    return true;
                }
        } else {
        		// ͬ��
            for (int index = 0; index < size; index++)
                if (o.equals(elementData[index])) {
                    fastRemove(index);
                    return true;
                }
        }
        return false;
    }

		// �����ڲ���ɾ����һ���Ƕ���ƥ�����ɾ�����Բ���rangeCheck(index)
    private void fastRemove(int index) {
        modCount++;
        int numMoved = size - index - 1;
        if (numMoved > 0)
        		// �ø��Ǵ���ɾ��
            System.arraycopy(elementData, index+1, elementData, index,
                             numMoved);
        elementData[--size] = null;
    }

		// ����ɾ������gc����
    public void clear() {
        modCount++;
				
        for (int i = 0; i < size; i++)
            elementData[i] = null;

        size = 0;
    }

		// ����Ԫ��β�ӵ�elementData��
    public boolean addAll(Collection<? extends E> c) {
        Object[] a = c.toArray();
        int numNew = a.length;
        // ȷ�������㹻
        ensureCapacityInternal(size + numNew);  // ����modCount
        System.arraycopy(a, 0, elementData, size, numNew);
        size += numNew;
        return numNew != 0;
    }
		
		// ����Ԫ�ز��뵽elementData�����ָ��index��
    public boolean addAll(int index, Collection<? extends E> c) {
    		// ����±�Ϸ���
        rangeCheckForAdd(index);

				// ��List��ΪObject����
        Object[] a = c.toArray();
        int numNew = a.length;
        // ȷ�������㹻
        ensureCapacityInternal(size + numNew);  // ����modCount

        int numMoved = size - index;
        if (numMoved > 0)
        		// �Ƚ�λ�ÿճ�����Ϊ������׼��
            System.arraycopy(elementData, index, elementData, index + numNew,
                             numMoved);
				// ����ʡȥ��forѭ���ĸ�ֵ
        System.arraycopy(a, 0, elementData, index, numNew);
        size += numNew;
        return numNew != 0;
    }

		// ָ������ɾ��
    protected void removeRange(int fromIndex, int toIndex) {
        modCount++;
        int numMoved = size - toIndex;
        // ����Ԫ��ǰ�ƣ�toIndex����ƶ���fromIndex��ʼ��numMoved��λ����
        System.arraycopy(elementData, toIndex, elementData, fromIndex,
                         numMoved);
				
				// newSizeΪʣ��Ԫ�ظ���
        int newSize = size - (toIndex-fromIndex);
        for (int i = newSize; i < size; i++) {
            elementData[i] = null;
        }
        size = newSize;
    }
		
		// ��װ���±�Խ���飬��get()��remove()ʹ��
    private void rangeCheck(int index) {
    		// ��û�м���±��Ƿ�С��0����Ϊ��elementData[index]�����У�
    		// jvm������Ǽ��Խ������
        if (index >= size)
            throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
    }

		// ��add()��addAll()ʹ�õİ汾
		// add()��addAll()�е�����native��System.copyOf()����
		// ������Ҫ�ж� index < 0����rangeCheck(int index)��ͬ
    private void rangeCheckForAdd(int index) {
        if (index > size || index < 0)
            throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
    }

		// �����쳣�ľ�����Ϣ
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
    		// �ж��Ƿ�Ϊ������
        Objects.requireNonNull(c);
        return batchRemove(c, false);
    }

		// ɾ��
    public boolean retainAll(Collection<?> c) {
        Objects.requireNonNull(c);
        return batchRemove(c, true);
    }

		// complement : �Ƿ���elementData���뼯��c����ͬ��Ԫ��
    private boolean batchRemove(Collection<?> c, boolean complement) {
        final Object[] elementData = this.elementData;
        int r = 0, w = 0;
        boolean modified = false;
        try {
            for (; r < size; r++)
            		// ���complementΪtrue������
                if (c.contains(elementData[r]) == complement)
                    elementData[w++] = elementData[r];
        } finally {
            // ������AbstractCollection����Ϊ�����ԣ���ʹc.contains()�׳��쳣��
           	// r != size ˵��û��ִ����ѭ����������û�ж�����ȫ���������޸�Ҳ��ʧ�ܵ�
            if (r != size) {
                System.arraycopy(elementData, r,
                                 elementData, w,
                                 size - r);
                w += size - r;
            }
            // ɾ���˲���Ԫ�أ���Ҫ��պ����
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

		// ���л�
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

		// �����л�
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
    
    // ���ش�index��ʼ�б���Ϣ
    public ListIterator<E> listIterator(int index) {
        if (index < 0 || index > size)
            throw new IndexOutOfBoundsException("Index: "+index);
        return new ListItr(index);
    }

		// ���ش��±�0��ʼ���б���Ϣ������ȫ����Ϣ
    public ListIterator<E> listIterator() {
        return new ListItr(0);
    }

		// ���ʵ���˳����б��е�Ԫ�ط���һ����������
    public Iterator<E> iterator() {
        return new Itr();
    }

    private class Itr implements Iterator<E> {
        int cursor;       // ��һ��Ҫ���ص�Ԫ�ص�����
        int lastRet = -1; // ���һ������Ԫ�ص�����(��һ�����ص�Ԫ��);��û�з���-1
        int expectedModCount = modCount;  // ȷ���޸�������֤�̰߳�ȫ

        Itr() {}

        public boolean hasNext() {
            return cursor != size;
        }

        @SuppressWarnings("unchecked")
        public E next() {
        		// ����޸����Ƿ���ͬ���ж��̵߳İ�ȫ��
            checkForComodification();
            // ��һ��Ϊ0����Ϊint��Ա��ʼֵΪ0
            int i = cursor;
            if (i >= size)
                throw new NoSuchElementException();
            Object[] elementData = ArrayList.this.elementData;
            // �����Ѿ��ж�i < size��������size <= elementData.length
            // ������������������˵�������������������̲߳���ȫ
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
                // ��֤�޸���ͬ������������̲߳���ȫ
                expectedModCount = modCount;
            } catch (IndexOutOfBoundsException ex) {
            		// ����±�Խ���ˣ���ô�϶��Ƕ�elementData������ɾ���Ȳ���
                throw new ConcurrentModificationException();
            }
        }
	
				// ��ʽ��forEach���ã�����ʣ�µ�����Ԫ��
        @Override
        @SuppressWarnings("unchecked")
        public void forEachRemaining(Consumer<? super E> consumer) {
        		// �ж�consumer�Ƿ�Ϊ null
            Objects.requireNonNull(consumer);
            // final�ؼ������Σ����ɸ���
            final int size = ArrayList.this.size;
            // ����֮ǰ�ļ�������
            int i = cursor;
            if (i >= size) {
                return;
            }
            final Object[] elementData = ArrayList.this.elementData;
            if (i >= elementData.length) {
                throw new ConcurrentModificationException();
            }
            // ���������Ԫ�أ����ж��޸����Ƿ���ͬ�������ͬ����ֹͣѭ��
            while (i != size && modCount == expectedModCount) {
                consumer.accept((E) elementData[i++]);
            }
            // ȷ����һ������ֵ���±����һ������ֵ���±�
            cursor = i;
            lastRet = i - 1;
            // ����߳�ͬ��
            checkForComodification();
        }
	
				// ͨ���޸����ĶԱȣ��ж��Ƿ��ԭ����������޸�
        // �ڲ���Itr����ʱ����ȷ����expectedModCount�Ĵ�С
        // ������ⲿ��������޸Ĳ������ͻᱻ��Ϊ���̲߳���ȫ����Ϊ
        final void checkForComodification() {
            if (modCount != expectedModCount)
                throw new ConcurrentModificationException();
        }
    }

		// ǿ�����Itr�����Է����±�
    private class ListItr extends Itr implements ListIterator<E> {
        ListItr(int index) {
            super();
            cursor = index;
        }

				// cursor == 0˵����ǰ���ڵ�һ�������±�
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

				// ��Ԫ�ط�����һ������Ԫ�ص��±��£��������޸����ĸı䣬�̰߳�ȫ
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

				// β�ӵ�ĩβ��ͬ���޸������̰߳�ȫ
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

		// ����һ��ָ����Χ��List
    public List<E> subList(int fromIndex, int toIndex) {
        subListRangeCheck(fromIndex, toIndex, size);
        return new SubList(this, 0, fromIndex, toIndex);
    }
	
		// �ж��±�Ϸ���
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
    		// �����Ƶ�List
        private final AbstractList<E> parent;
        // �����������ƫ����
        private final int parentOffset;
        // ��ʾ������ĵ�һ��Ԫ����parent�е�ƫ����
        // new SubList(this, offset, fromIndex, toIndex)�൱�ڵõ�һ����ʼ�±�Ϊ2*offset��list
        // ��subList�ϲ��ܵݹ�ָ���ٴΣ�ʵ�ʶ��Ƕ�parent���в�����ֻ��offset���˶���
        private final int offset;
        // ����Ԫ�ظ���
        int size;

        SubList(AbstractList<E> parent,
                int offset, int fromIndex, int toIndex) {
            this.parent = parent;
            this.parentOffset = fromIndex;
            this.offset = offset + fromIndex;
            this.size = toIndex - fromIndex;
            // ͬ���޸���
            this.modCount = ArrayList.this.modCount;
        }

        public E set(int index, E e) {
            rangeCheck(index);
            checkForComodification();
            // �滻��ֵ
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
            // �ڲ���ӣ������޸���ͬ��
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

		// ��ʾ��forEach
    @Override
    public void forEach(Consumer<? super E> action) {
        Objects.requireNonNull(action);
        final int expectedModCount = modCount;
        @SuppressWarnings("unchecked")
        final E[] elementData = (E[]) this.elementData;
        final int size = this.size;
        // modCount == expectedModCount���hashmap�Ǽ�ǿ,hashmap������
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
				*���arraylist�ǲ��ɱ�ģ������ڽṹ���ǲ��ɱ��(û����ӡ�ɾ����)��
				���ǿ�����Arrays.spliteratorʵ�����ǵ�spliterator���෴��
				�����ڱ��������о����ܶ�ؼ����ţ�ͬʱ�ֲ�������̫�����ܡ�
				������Ҫ����modCounts����Щ���������ܱ�֤��⵽������Υ�棬
				������ʱ�����߳��ڵĸ��Ź��ڱ��أ�������ʵ���п��Լ�⵽�㹻������⡣
				Ϊ��ʵ����һ�㣬����
				(1)�ӳٳ�ʼ��fence���ȴ�modcount��ֱ����Ҫ�ύ���������ڼ���״̬�����µ�;�Ӷ���߾��ȡ�(�ⲻ�����ڴ������е�ǰ����ֵ��spliterators�����б�)��
				(2)����ֻ��forEach��βִ��һ�� ConcurrentModificationException���(�����е����ܷ���)��
				��ʹ��forEach (��������෴)ʱ������ͨ��ֻ���ڲ���֮������ţ�������֮ǰ����һ����
				cmtrigger��������������������ܵ�Υ����������������null��̫С��elementData���飬
				��Ϊ����size()������ֻ����Ϊ���Ŷ���������ʹ��forEach���ڲ�ѭ�������һ�����Ϳ������У�
				���Ҽ���lambda-resolution����Ȼ��ȷʵ��Ҫ�����ļ�飬����ע�⣬�� list.stream().forEach(a)�ĳ�������£�������forEach�ڲ���
				�����κεط������ᷢ�������������㡣������̫���õķ�������������Щ���ߡ�
         */

        private final ArrayList<E> list;
        private int index; // ��ǰ������Ԥ���޸�/�ָ�
        private int fence; // -1ֱ��ʹ��;Ȼ�������һ������
        private int expectedModCount; // ����դ��ʱ��ʼ��

        /** �������Ǹ�����Χ����spliterator */
        ArrayListSpliterator(ArrayList<E> list, int origin, int fence,
                             int expectedModCount) {
            this.list = list; // OK if null unless traversed
            this.index = origin;
            this.fence = fence;
            this.expectedModCount = expectedModCount;
        }

        private int getFence() { // �ڵ�һ��ʹ��ʱ��դ����ʼ��Ϊ��С
            int hi; // (�ڷ���forEach�г���һ��ר�ŵı���)
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
        		// (lo + hi) >>> 1 �ֳ�����
            int hi = getFence(), lo = index, mid = (lo + hi) >>> 1;
            return (lo >= mid) ? null : // �ѷ�Χ�ֳ����룬����̫С
                new ArrayListSpliterator<E>(list, lo, index = mid,
                                            expectedModCount);
        }

				// ��ȡ��һ��
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
            int i, hi, mc; // ��ѭ����������ںͼ��
            ArrayList<E> lst; Object[] a;
            if (action == null)
                throw new NullPointerException();
            if ((lst = list) != null && (a = lst.elementData) != null) {
            		// δ��ʼ����Χ�������ȫ��
                if ((hi = fence) < 0) {
                    mc = lst.modCount;
                    hi = lst.size;
                }
                else
                		// ����list���޸�������ֹ�ⲿ�޸�
                    mc = expectedModCount;
                // ��֤������Χ
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
				// ��ǰ���Ʒ�Χ
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
				//�ҳ���Ҫɾ����Ԫ�أ��ڴ˽׶δ�ɸѡ�����׳����κ��쳣�������޸ļ���
        int removeCount = 0;
        final BitSet removeSet = new BitSet(size);
        final int expectedModCount = modCount;
        final int size = this.size;
        for (int i=0; modCount == expectedModCount && i < size; i++) {
            @SuppressWarnings("unchecked")
            final E element = (E) elementData[i];
            // �����������ã����fileter�����Ԫ�أ����±걣��
            if (filter.test(element)) {
                removeSet.set(i);
                removeCount++;
            }
        }
        if (modCount != expectedModCount) {
            throw new ConcurrentModificationException();
        }

        // ��ʣ���Ԫ���ƶ������Ƴ�Ԫ�������µĿռ���
        final boolean anyToRemove = removeCount > 0;
        if (anyToRemove) {
        		// ����ɾ����Ԫ�صĸ���
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
        // �滻ֵ
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
        // ȫ����
        Arrays.sort((E[]) elementData, 0, size, c);
        if (modCount != expectedModCount) {
            throw new ConcurrentModificationException();
        }
        modCount++;
    }
}
