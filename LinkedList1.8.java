package java.util;

import java.util.function.Consumer;

/**
	* {@code list}��{@code Deque}��˫����ʵ��
	*�ӿڡ�ʵ�����п�ѡ���б���������������в���
	*Ԫ��(����{@code null})��
 *
	���еĲ���������˫����Ԥ��ִ��
	*�б��������б�Ĳ����������б�
	*��ʼ��������ԽϽӽ�ָ��������Ϊ׼��
 *
	*�������߳�ͬʱ����һ��������������
	*����һ���̴߳ӽṹ���޸����б������뱻
	*�ⲿͬ�����ṹ�޸���ָ�κβ���
	*��ӻ�ɾ��һ������Ԫ��;ֻ�����õ�ֵ
	*Ԫ�ز��ǽṹ�޸ġ�)��ͨ����
	*ͨ����Ȼ��ͬ��һЩ���������
	��װ�б�
 *
	*��������ڴ��������Ӧʹ��
	* {@link Collections#synchronizedList Collections.synchronizedList}
	*������������ڴ���ʱ��ɣ��Է�ֹ����
	*���б�ķ�ͬ������:

	* List List = Collections.synchronizedList(newLinkedList (��));
 *
	����������������{@code������}��
	* {@code listIterator}������ failure -fast:����б���
	*�ڵ�������������κ�ʱ���ڽṹ�Ͻ������޸�
	*����ͨ���������Լ���{@code remove}��
	* {@code add}���������������׳�һ��{@link
	* ConcurrentModificationException}����ˣ���Բ���
	*�޸ģ�������ʧ��Ѹ�ٺ͸ɾ���������
	ð������ġ���ȷ������Ϊ�ķ���
	δ����ʱ�䡣
 *
	ע�⣬���ܱ�֤�������Ĺ���-������Ϊ
	һ����˵�����ǲ����������κ��ϸ�ı�֤
	*���ڲ�ͬ���Ĳ����޸ġ�����ʧ�ܵ�����
	*�����Ŭ���׳�{@code ConcurrentModificationException}��
	��ˣ�дһ�������ڴ˵ĳ����Ǵ����
	*��ȷ���쳣:�������Ŀ���ʧЧ��Ϊ
	*Ӧ��ֻ���ڼ��bug��
 */

public class LinkedList<E>
    extends AbstractSequentialList<E>
    implements List<E>, Deque<E>, Cloneable, java.io.Serializable
{
		// �������transient�ؼ��֣��������������л�
    transient int size = 0;

    /**
     * *ָ���һ���ڵ��ָ�롣
     * ������: (first == null && last == null) ||
     *         (first.prev == null && first.item != null)
     */
    transient Node<E> first;

    /**
     * ָ�����һ���ڵ��ָ�롣
     * Invariant: (first == null && last == null) ||
     *            (last.next == null && last.item != null)
     */
    transient Node<E> last;

		// �޲ι���
    public LinkedList() {
    }

		// ʹ��һ������������
    public LinkedList(Collection<? extends E> c) {
    		// �����޲ι���
        this();
        // �����ٽ���
        addAll(c);
    }

		// ����e��Ϊ��һ��Ԫ�أ�ǰ�巨�����ӽ��
    private void linkFirst(E e) {
    		// ȡ�������ͷ���
        final Node<E> f = first;
        // ���ɽ�㣬ǰһ���Ϊnull����һ���Ϊf
        final Node<E> newNode = new Node<>(null, e, f);
        // �������Ľ����Ϊͷ���
        first = newNode;
        // f == null ˵��ԭ����Ϊ�գ���ô�����Ľ�㼴ΪͷҲ��β
        if (f == null)
            last = newNode;
        // ԭ����Ϊ�գ���ǰ��������
        else
            f.prev = newNode;
        size++;
        // �޸���+1
        modCount++;
    }

		// ����e��Ϊ���һ��Ԫ�أ�β�ӷ������ӽ��
    void linkLast(E e) {
    		// ȡ�������ĩ���
        final Node<E> l = last;
        // �¼ӽ�㣬�ý���ǰһ�����ԭ�����ĩ���
        final Node<E> newNode = new Node<>(l, e, null);
        // �������Ľ����Ϊĩ���
        last = newNode;
        // l == null ˵��ԭ����Ϊ�գ���ô�����Ľ�㼴ΪͷҲ��β
        if (l == null)
            first = newNode;
        // ԭ����Ϊ�գ���ǰ��������
        else
            l.next = newNode;
        size++;
        // �޸���+1
        modCount++;
    }

		// �ڷǿս��succ֮ǰ����Ԫ��e
		// ���������ָ��succ����ǰһ���ͺ�һ���
    void linkBefore(E e, Node<E> succ) {
    		// ȡ��succ������һ���
        final Node<E> pred = succ.prev;
        // ������Ĳ���
        final Node<E> newNode = new Node<>(pred, e, succ);
        // ��succ���֮ǰ���룬����succ����ǰһ������²���Ľ��
        succ.prev = newNode;
        // ���succû��ǰһ��㣬��ô������ͷ��㣬����Ľ������µ�ͷ���
        if (pred == null)
            first = newNode;
       	// succ��ǰһ��㣬��ǰ��������
        else
            pred.next = newNode;
        size++;
        // �޸���+1
        modCount++;
    }

		// ȡ���ǿյĵ�һ���ڵ�f������
    private E unlinkFirst(Node<E> f) {
        final E element = f.item;
        // ȡ��ԭͷ���ĺ�һ���
        final Node<E> next = f.next;
        // ȡ�����ã�gc����
        f.item = null;
        f.next = null;
				// ɾ����ͷ��㣬�ǵڶ��������µ�ͷ���
        first = next;
        // ���nextΪnull����ôfirst�Ѿ�Ϊnull��ҲҪ��last��Ϊnull
        if (next == null)
            last = null;
        // ͷ����ǰһ���Ϊnull
        else
            next.prev = null;
        size--;
        // �޸���+1
        modCount++;
        return element;
    }

		// ȡ���ǿ����һ���ڵ�l������
    private E unlinkLast(Node<E> l) {
        final E element = l.item;
        // ȡ��ԭĩ����ǰһ���
        final Node<E> prev = l.prev;
        // ȡ�����ã�gc����
        l.item = null;
        l.prev = null;
        // ɾ����ĩ��㣬��ĩ����ǰһ������µ�ĩ���
        last = prev;
        // ���prevΪnull����ôlast�Ѿ�Ϊnull��ҲҪ��first��Ϊnull
        if (prev == null)
            first = null;
        // ĩ���ĺ�һ���Ϊnull
        else
            prev.next = null;
        size--;
        // �޸���+1
        modCount++;
        return element;
    }

		// ȡ���ǿսڵ�x�����ӣ�ָ������ɾ��
		// ��ɾ������ǰһ������һ��㻥��ָ��
    E unlink(Node<E> x) {
        final E element = x.item;
        // Ŀ����ĺ�һ���
        final Node<E> next = x.next;
        // Ŀ�����ǰһ���
        final Node<E> prev = x.prev;

				// ���ǰһ���Ϊ�գ���ô��һ���Ϊ�µ�ͷ���
        if (prev == null) {
            first = next;
        // ��Ϊ�գ���������
        } else {
            prev.next = next;
            // gc
            x.prev = null;
        }
				// �����һ���Ϊ�գ���ôǰһ���Ϊ�µ�ĩ���
        if (next == null) {
            last = prev;
        // ��Ϊ�գ����໥����
        } else {
            next.prev = prev;
            // gc
            x.next = null;
        }
				// gc
        x.item = null;
        size--;
        // ������+1
        modCount++;
        return element;
    }

		// �����б��еĵ�һ��Ԫ�ء�
    public E getFirst() {
        final Node<E> f = first;
        if (f == null)
            throw new NoSuchElementException();
        return f.item;
    }

		// �����б��е����һ��Ԫ�ء�
    public E getLast() {
        final Node<E> l = last;
        if (l == null)
            throw new NoSuchElementException();
        return l.item;
    }

		// ���б���ɾ�������ص�һ��Ԫ�ء�
    public E removeFirst() {
        final Node<E> f = first;
        if (f == null)
            throw new NoSuchElementException();
        return unlinkFirst(f);
    }

		// ���б����Ƴ����������һ��Ԫ�ء�
    public E removeLast() {
        final Node<E> l = last;
        if (l == null)
            throw new NoSuchElementException();
        return unlinkLast(l);
    }

		// ��ָ����Ԫ�ز�����б�Ŀ�ͷ��
    public void addFirst(E e) {
        linkFirst(e);
    }

		// ��ָ����Ԫ��׷�ӵ����б��ĩβ��
    public void addLast(E e) {
        linkLast(e);
    }
		
		// ���ٰ���һ��ָ����Ԫ�أ��ͷ���true
    public boolean contains(Object o) {
        return indexOf(o) != -1;
    }

		// �����б���Ԫ�ص���Ŀ��
    public int size() {
        return size;
    }

		// ��ָ����Ԫ��׷�ӵ����б��ĩβ��
    public boolean add(E e) {
        linkLast(e);
        return true;
    }

		// ɾ����һ��ƥ���Ԫ�أ��ӿ�ͷ����������ѰĿ����
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
        // δ�ҵ�������false
        return false;
    }

		// ��һ�������е�����Ԫ�ؼӵ��������������
    public boolean addAll(Collection<? extends E> c) {
        return addAll(size, c);
    }
		
		// ��ָ��λ�ÿ�ʼ������������еĶ�����
    public boolean addAll(int index, Collection<? extends E> c) {
    		// ����±�ĺϷ��ԣ�0 ��> size
        checkPositionIndex(index);

				// �����c����Ϊ�����е�ĳһ����toArray()��ȷ��
				// ���Խ��о�LinkedList�е�toArray()����
        Object[] a = c.toArray();
        int numNew = a.length;
        // ����Ϊ��
        if (numNew == 0)
            return false;

        Node<E> pred, succ;
        // β��������ĩβ
        if (index == size) {
            succ = null;
            pred = last;
        // ȡ�ò����Ŀ���㣬��Ŀ�����ǰһ���ĺ������ӽ��
        } else {
            succ = node(index);
            pred = succ.prev;
        }

        for (Object o : a) {
            @SuppressWarnings("unchecked") E e = (E) o;
            // �����½�㣬��predΪǰһ��㣬��һ���Ϊnull
            Node<E> newNode = new Node<>(pred, e, null);
            // ���ǰһ���Ϊnull�����������Ϊͷ���
            if (pred == null)
                first = newNode;
            else
                pred.next = newNode;
            // �����������Ϊǰһ��㣬β�ӷ���ӽ��
            pred = newNode;
        }
				// ���index == size����Ĭ�ϼ��������ĩβ���������ӵĽ�����ĩ���
        if (succ == null) {
            last = pred;
        // ��Ŀ���㣬�ǽ�ԭ���Ͽ�������������һ���������ĺ���
        } else {
            pred.next = succ;
            succ.prev = pred;
        }

        size += numNew;
        // �޸���+1
        modCount++;
        return true;
    }

		// ���б���ɾ������Ԫ�أ����÷��غ��б�Ϊ�ա�
    public void clear() {
				// ��������ȡ��������ָ��
				// ��Ȼ���ò�����������gcҲ�ܻ���
				// �������еĽ�㲻һ������gc�����������ڷִ�����
        for (Node<E> x = first; x != null; ) {
            Node<E> next = x.next;
            x.item = null;
            x.next = null;
            x.prev = null;
            // ��������
            x = next;
        }
        // ͷ��β���Ϊnull
        first = last = null;
        size = 0;
        modCount++;
    }


    // λ�÷��ʲ���

		// �����б���ָ��λ�õ�Ԫ��
    public E get(int index) {
    		// ����±�Ϸ��ԣ�0 -> (size-1)
        checkElementIndex(index);
        return node(index).item;
    }

		// ���б���ָ��λ�õ�Ԫ���滻Ϊָ��Ԫ��
    public E set(int index, E element) {
    		// ����±�Ϸ��ԣ�0 -> (size-1)
        checkElementIndex(index);
        Node<E> x = node(index);
        // �����Ԫ��
        E oldVal = x.item;
 				// �滻��Ԫ��
        x.item = element;
        return oldVal;
    }

		// ��ָ���±���ǰ���Ԫ��
    public void add(int index, E element) {
    		// ����±�
        checkPositionIndex(index);
	
				// ĩβ׷��
        if (index == size)
            linkLast(element);
        // ָ�����ǰ��
        else
            linkBefore(element, node(index));
    }

		// ɾ��ָ�����±���
    public E remove(int index) {
        checkElementIndex(index);
        return unlink(node(index));
    }

		// ���Ԫ�ص���Ч�ԣ�Ԫ�صı����±꣺0 -> (size-1)
    // ��get()��set()��remove()�ȵ���
    private boolean isElementIndex(int index) {
        return index >= 0 && index < size;
    }

		// ����±����Ч�ԣ�index���Ե���size���൱��������ĩβ���ӽ��
		// ��add()��addAll()�ȵ���
    private boolean isPositionIndex(int index) {
        return index >= 0 && index <= size;
    }

		// �����쳣�ľ�����Ϣ
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

		// ����ָ��Ԫ����������(�ǿ�)�ڵ�
    Node<E> node(int index) {
    		// ���ַ���ѡ���ǰ���������ǴӺ���ǰ����
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

    // ��������

		// ���ظ�Ԫ�ص�һ�γ��ֵ��±꣬��ǰ������
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

		// ���ظ�Ԫ�����һ�γ��ֵ��±꣬�Ӻ���ǰ����
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

    // ���еĻ�������

		// ���ҵ���ɾ�����б��ͷ(��һ��Ԫ��)
    public E peek() {
        final Node<E> f = first;
        return (f == null) ? null : f.item;
    }

    // ���ҵ���ɾ�����б��ͷ(��һ��Ԫ��)
    public E element() {
        return getFirst();
    }

		// ���Ҳ�ɾ�����б��ͷ(��һ��Ԫ��)
    public E poll() {
        final Node<E> f = first;
        return (f == null) ? null : unlinkFirst(f);
    }

		// ���Ҳ�ɾ�����б��ͷ(��һ��Ԫ��)
    public E remove() {
        return removeFirst();
    }

		// ��ָ����Ԫ�����Ϊ���б��ĩβ(���һ��Ԫ��)
    public boolean offer(E e) {
        return add(e);
    }

    // ˫�˶��еĲ���

		// ��ָ����Ԫ�ز�����б��ǰ��
    public boolean offerFirst(E e) {
        addFirst(e);
        return true;
    }

		// ���б�ĩβ����ָ����Ԫ��
    public boolean offerLast(E e) {
        addLast(e);
        return true;
    }

		// ���ҵ���ɾ�����б��ͷ(��һ��Ԫ��)
    public E peekFirst() {
        final Node<E> f = first;
        return (f == null) ? null : f.item;
     }

		// ���ҵ���ɾ�����б��β(���һ��Ԫ��)
    public E peekLast() {
        final Node<E> l = last;
        return (l == null) ? null : l.item;
    }

		// ���Ҳ�ɾ�����б��ͷ(��һ��Ԫ��)
    public E pollFirst() {
        final Node<E> f = first;
        return (f == null) ? null : unlinkFirst(f);
    }

		// ���Ҳ�ɾ�����б��β(���һ��Ԫ��)
    public E pollLast() {
        final Node<E> l = last;
        return (l == null) ? null : unlinkLast(l);
    }

		// ��Ԫ��������б�����ʾ�Ķ�ջ: ��Ԫ�ز��뵽�б��ǰ��
    public void push(E e) {
        addFirst(e);
    }

    // �Ӹ��б��ʾ�Ķ�ջ�е���һ��Ԫ��: ɾ�������ش��б�ĵ�һ��Ԫ��
    public E pop() {
        return removeFirst();
    }

		// ɾ����һ��ƥ���Ԫ�أ���ǰ������
    public boolean removeFirstOccurrence(Object o) {
        return remove(o);
    }

		// ɾ�����һ��ƥ���Ԫ�أ��Ӻ���ǰ����
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
			���ش��б���Ԫ�ص��б������(���ʵ��������)
			����)�����б��е�ָ��λ�ÿ�ʼ��
			*����{@code List.listIterator(int)}.
			��ͨ����Լ
     *
			*�б��������fail-fast:����б��ǽṹ����
			*�ڴ���������֮����κ�ʱ���޸ģ�����
			*ͨ���б�������Լ���{@code remove}��{@code add}
			*�������б���������׳�һ��
			* {@code ConcurrentModificationException}�����ǣ����
			*�����޸ģ�������ʧ�ܵø�����ɾ�
			*������ð������ġ���ȷ������Ϊ�ķ���
			δ����ʱ�䡣
     */
     
    // �õ�һ����ָ���±꿪ʼ�ĵ�����
    public ListIterator<E> listIterator(int index) {
    		// ����±�ĺϷ��ԣ�0 -> size
        checkPositionIndex(index);
        return new ListItr(index);
    }

    private class ListItr implements ListIterator<E> {
    		// ��һ�����ص�Ԫ��
        private Node<E> lastReturned;
        // ��һ�����ص�Ԫ��
        private Node<E> next;
        // ��һ������Ԫ�ص��±�
        private int nextIndex;
        // ������ListItr����ʱȷ�����޸������ڱ����������ڼ�
        // ���ⲿ���������ӡ�ɾ���Ȳ������ͻ�ı�modCount
        // ��expectedModCountû�иı䣬ͨ���ж��������Ƿ���ͬ
        // �Ϳ����жϼ����Ƿ񱻱���߳��޸��ˣ���֤�̰߳�ȫ
        private int expectedModCount = modCount;

        ListItr(int index) {
        		// �ҵ�ָ���±��Ӧ�Ľ��
            next = (index == size) ? null : node(index);
            // ��һ������Ԫ�ص��±�
            nextIndex = index;
        }

        public boolean hasNext() {
            return nextIndex < size;
        }

        public E next() {
        		// ����̰߳�ȫ
            checkForComodification();
            if (!hasNext())
                throw new NoSuchElementException();

						// �����صĽ����ΪlastReturned
            lastReturned = next;
            // �õ���һ���
            next = next.next;
            // �±��1
            nextIndex++;
            return lastReturned.item;
        }
        
        // ���±����0��˵����һ�����صĽ����ǰ���
        public boolean hasPrevious() {
            return nextIndex > 0;
        }

        public E previous() {
            checkForComodification();
            if (!hasPrevious())
                throw new NoSuchElementException();

						// ���nextΪnull��˵����һ�����صĽ����ĩ���ĺ�һ���null
						// ��ʱ��lastReturned == next
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

				// �������е�ɾ������������������̲߳���ȫ
        public void remove() {
        		// ����̰߳�ȫ
            checkForComodification();
            if (lastReturned == null)
                throw new IllegalStateException();
						
						// lastNext ����Ϊ null����lastReturnedΪĩ���ʱ
            Node<E> lastNext = lastReturned.next;
            // ɾ����һ�����صĽ�㣬һ������null��modCount++
            unlink(lastReturned);
            
            // ��������previous()��������next == lastReturned
            if (next == lastReturned)
            		// ɾ����lastReturned��㣬��һ�����Ϊɾ��������һ�����
            		// �±겢δ�仯������ϸ˼��
                next = lastNext;
            // ��������next()��������next != lastReturned
            else
            		// �±귢���˱仯������Ҫ-1
                nextIndex--;
            lastReturned = null;
            // unlink(lastReturned)������modCount�����ﱣ����ͬ��
            expectedModCount++;
        }

        public void set(E e) {
            if (lastReturned == null)
                throw new IllegalStateException();
            checkForComodification();
            lastReturned.item = e;
        }

				// �̰߳�ȫ
        public void add(E e) {
            checkForComodification();
            lastReturned = null;
            if (next == null)
                linkLast(e);
            else
                linkBefore(e, next);
            // �Թ������Ľ�㣬���������
            nextIndex++;
            // ͬ���޸���
            expectedModCount++;
        }

				// ����ʣ�µ�Ԫ��
        public void forEachRemaining(Consumer<? super E> action) {
        		// �ж�action�Ƿ�Ϊnull�����ǣ��׿�ָ���쳣
            Objects.requireNonNull(action);
            while (modCount == expectedModCount && nextIndex < size) {
                action.accept(next.item);
                lastReturned = next;
                next = next.next;
                nextIndex++;
            }
            // �����ѭ�����ܱ���ϣ���Ҫ�ٴ��ж��̰߳�ȫ
            checkForComodification();
        }

				// �ж��޸����Ƿ����˱仯������ı��ˣ���ô�׳��쳣
        final void checkForComodification() {
            if (modCount != expectedModCount)
                throw new ConcurrentModificationException();
        }
    }

		// �����㣬˫������
    private static class Node<E> {
    		// ���Ԫ�ص�ֵ
        E item;
        // ������һ���
        Node<E> next;
        // ������һ���
        Node<E> prev;

        Node(Node<E> prev, E element, Node<E> next) {
            this.item = element;
            this.next = next;
            this.prev = prev;
        }
    }

 		// �෴����ĵ�����
    public Iterator<E> descendingIterator() {
        return new DescendingIterator();
    }

    private class DescendingIterator implements Iterator<E> {
    		// �±궨λ�����������ǰ����
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
        		// ����Object.clone()
            return (LinkedList<E>) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InternalError(e);
        }
    }

		// ǳ��¡
    public Object clone() {
        LinkedList<E> clone = superClone();

				// ��ʼ��
        clone.first = clone.last = null;
        clone.size = 0;
        clone.modCount = 0;

        // �����ǵ�Ԫ�س�ʼ����¡������Ԫ�����ã�ǳ��¡
        for (Node<E> x = first; x != null; x = x.next)
            clone.add(x.item);

        return clone;
    }

		// ͨ������������������
    public Object[] toArray() {
        Object[] result = new Object[size];
        int i = 0;
        // �����������μӵ�������
        for (Node<E> x = first; x != null; x = x.next)
            result[i++] = x.item;
        return result;
    }

    @SuppressWarnings("unchecked")
    public <T> T[] toArray(T[] a) {
    		// ͨ����������֤�����㹻�������������Ԫ��
        if (a.length < size)
            a = (T[])java.lang.reflect.Array.newInstance(
                                a.getClass().getComponentType(), size);
        int i = 0;
        Object[] result = a;
        // �����������μӵ�������
        for (Node<E> x = first; x != null; x = x.next)
            result[i++] = x.item;

				// ������ЧԪ��Ϊ��0 -> size-1
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
