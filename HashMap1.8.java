package java.util;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import sun.misc.SharedSecrets;

/**
	*���ڹ�ϣ��ʵ�ֵ�Map�ӿڡ���
	*ʵ���ṩ�����п�ѡ��map���������֤
	* nullֵ��null����(< tt > HashMap < / tt >
	class�����൱��Hashtable��ֻ������
	*��ͬ��������Ϊ�ա�)����಻�ܱ�֤
	*��ͼ�Ĵ���;�ر��ǣ������ܱ�֤����
	������ʱ�䱣�ֲ��䡣
 *
	��ʵ��Ϊbasic�ṩ�˳���ʱ������
	*����(get and put)�������ϣ����
	*��Ԫ���ʵ��ط�ɢ������Ͱ�С�����
	*�ռ���ͼ����ʱ���롰������������
	* HashMapʵ��(Ͱ������)�������Ĵ�С(����)
	*��ֵӳ��)����ˣ���Ҫ���ó�ʼֵ�Ƿǳ���Ҫ��
	*����������ܹ���(�������ӹ���)
	*��Ҫ��
 *
	һ��HashMap��ʵ��������Ӱ�����Ĳ���
	*����:��ʼ������ ������������
	* capacity�ǹ�ϣ����Ͱ��������Ϊ��ʼͰ��
	*�������Ǵ�����ϣ��ʱ����������
	* load factor�Ƕ������ϣ������ȵĶ���
	*�����������Զ�����֮ǰ����
	*��ϣ���е�����˸������Ӻ͵ĳ˻�
	*��ǰ��������ϣ����rehashed(��internal)
 *
	��Ϊһ�����Ĭ�ϵĸ���ϵ��(.75)�ṩ��һ�����õ�
	*Ȩ��ʱ��Ϳռ�ɱ���ֵԽ�ߣ�
	*�ռ俪���������Ӳ��ҳɱ�(��ӳ�ڴ����
	* HashMap��Ĳ���������
	* get�� put)��������������
	*��ͼ���为������Ӧ����ʱ
	*�������ʼ������ʹ
	*�ظ������������ʼ��������
	*�����Ŀ�����Ը������ӣ�û���ع�ϣ
 *
	������ӳ��Ҫ�洢��HashMap
	���磬����һ���㹻�������������
	*ӳ�佫������Ч�ش洢������������ִ��
	*������Ҫ�Զ����¹�ϣ��ע��,ʹ��
	*��������ͬ{@code hashCode()}�ļ��϶�������ٶ�
	*�����κι�ϣ������ܡ�����Ӱ�죬���ؼ�
	* are {@link Comparable}����������ʹ��֮��ıȽ�˳��
	*�������ƹ�ϵ��Կ�ס�
 *
	ע�����ʵ�ֲ���ͬ���ġ�
	*�������߳�ͬʱ����һ��ɢ��ӳ�䣬������һ��
	*�߳��ڽṹ���޸�ӳ�䣬������ be
	*�ⲿͬ�����ṹ�޸���ָ�κβ���
	*��ӻ�ɾ��һ������ӳ��;ֻ�Ǹı���ֵ
	*��ʵ���Ѱ����ļ������Ĳ���
	*�ṹ�޸ġ�)��ͨ������
	ͬ��һЩ������Ȼ��װӳ�䡣
 *
	*��������ڴ��������Ӧʹ��
	* {@link Collections#synchronizedMap Collections.synchronizedMap}
	*������������ڴ���ʱ��ɣ��Է�ֹ����
	*��ӳ��ķ�ͬ������:

	* Map m =Collections.synchronizedMap(��HashMap (��));
 *
	���������С�������ͼ���������صĵ�����
	*��fail-fast:�����֮����κ�ʱ���ӳ������˽ṹ�޸�
	������������������ͨ���������Լ��ķ�ʽ
	* ɾ�����������������׳�һ��
	* {@link ConcurrentModificationException}����ˣ���Բ���
	*�޸�ʱ������������ٶ��ɾ���ʧ�ܣ��������з���
	�����⡢��ȷ������Ϊδ����
 *
	ע�⣬���ܱ�֤�������Ĺ���-������Ϊ
	һ����˵�����ǲ����������κ��ϸ�ı�֤
	*���ڲ�ͬ���Ĳ����޸ġ�����ʧ�ܵ�����
	*�����Ŭ���׳�ConcurrentModificationException��
	��ˣ�дһ�������ڴ˵ĳ����Ǵ����
	*��ȷ���쳣:�������Ŀ���ʧЧ��Ϊ
	*Ӧ��ֻ���ڼ��bug��
 */
public class HashMap<K,V> extends AbstractMap<K,V>
    implements Map<K,V>, Cloneable, Serializable {

    private static final long serialVersionUID = 362498820763181265L;

    /*
     * ʵ��ע�����
     *
		*���ӳ��ͨ���䵱һ������(������)�Ĺ�ϣ������
		*�����ӱ��̫��ʱ�����ǻᱻת����
		*���ڵ㣬ÿ���ڵ�Ľṹ������
		* java.util.TreeMap���������������ʹ����ͨ�����ӣ�����
		*������ʱת����TreeNode����(ֻ����)
		*�ڵ��instanceof)�����Ա�����
		*��������һ��ʹ�ã���������֧�ָ���Ĳ���
		*���˿ڹ�ʣ��Ȼ�������ھ��������������
		*����ʹ�ò����أ�����Ƿ����
     *
		*����(��Ԫ�ض������ڵ������)
		*��Ҫ����hashCode���򣬵������tie����������
		*Ԫ������ͬ�ġ�C��ʵ�ֿɱȵ�<C>����
		*���ͣ�Ȼ�����ǵ�compareTo������������(����
		*ͨ�����䱣�صؼ�鷺�������Խ�����֤
		* this�����μ�����comparableClassFor)�������˸�����
		*�ṩ����O(log n)��ֵ�õ�
		*�������в�ͬ�Ĺ�ϣֵ��Ϊʱ�Ĳ���
		*��������ˣ��������ŵ��½�
     *
		��Ϊ���ڵ�Ĵ�С����ͨ�ڵ������
		*ֻ�е����������㹻�Ľڵ�ʱ��ʹ��
		*(��TREEIFY_THRESHOLD)�������Ǳ��̫С��ʱ��(����
		*ɾ���������С)���Ǳ�ת������ͨ�������䡣��
		*ʹ�÷ֲ����õ��û���ϣ�룬������
		*����ʹ�á���������£��������ϣ����
		bin�еĽڵ���Ӳ��ɷֲ�exp(-0.5) * pow(0.5, k) /factorial(k)
		*Ĭ�ϴ�С������ƽ������ԼΪ0.5
     *
     * 0:    0.60653066
     * 1:    0.30326533
     * 2:    0.07581633
     * 3:    0.01263606
     * 4:    0.00157952
     * 5:    0.00015795
     * 6:    0.00001316
     * 7:    0.00000094
     * 8:    0.00000006
     * ����:����1ǧ���֮һ
     *
		��״�����ĸ�ͨ�������ĵ�һ���ڵ㡣Ȼ��,
		��ʱ(��ǰ�����Iterator.remove)��������
		*�������ط���������ͨ�������ӻָ�
		* (TreeNode.root()����)��
     *
		*�������õ��ڲ����������ܹ�ϣ����Ϊ
		����(ͨ���ɹ��������ṩ)������
		*�ڲ����¼����û���ϣ�������µ��á�
		Ҳ����˵��������ڲ�����Ҳ���ܡ�tab������
		*ͨ���ǵ�ǰ�����������±��ɱ�ʱ
		*������С��ת����
     *
		*��bin�б�treeified��split��untreeifiedʱ�����Ǳ���
		*��������ͬ����Դ�ȡ/��������(�����ֳ�
		(��ͬ)Ϊ�˸��õر���ֲ�����
		*�򻯶Ե��õķָ�ͱ����Ĵ���
		* iterator.remove����ʹ�ñȽ������в���ʱ��Ҫ����a
		*������(�����������Ҫ������)
		* rebalancings�����ǽ����identityHashCodes���бȽϲμӡ�
     *
		*��ͨvs��ģʽ֮���ʹ�ú�ת����
		*����LinkedHashMap����Ĵ��ڶ���ø��ӡ�����
		*�����Ƕ����ڲ���ʱ���õĹ��ӷ�����
		*ɾ���ͷ�������LinkedHashMap�ڲ�
		*���򱣳ֶ�������Щ���ơ�(��Ҳ
		*Ҫ��ӳ��ʵ�����ݸ�һЩʵ�ó��򷽷�
		*���ܻᴴ���½ڵ㡣)
     *
		*����ssa�Ĳ��б�̷����а���
		*��������Ť��ָ������еı�������
     */

		// Ĭ�ϵĳ�ʼ����16
		static final int DEFAULT_INITIAL_CAPACITY = 1 << 4;

		// Ĭ�ϵ��������
		static final int MAXIMUM_CAPACITY = 1 << 30;

		// Ĭ�ϵļ������ӣ�0.75f��ʱ����ռ��ȡƽ����
		static final float DEFAULT_LOAD_FACTOR = 0.75f;

		// ����ת�ɺ��������ֵ���ڴ洢����ʱ���������� >= 8ʱ��������ת���ɺ����
		// jdk���߸��ݲ��ɷֲ���ʵ�ʲ��Գ�����loadFactor=0.75ʱ
		// ����һ��������8���ڵ�ĸ���Ϊ0.00000006����Ϊת������Ǵ��۸߰���
		// �����ڼ����������ת��Ϊ���ṹ��ֵ�õġ�
		static final int TREEIFY_THRESHOLD = 8;

		// �����תΪ�������ֵ����������� <= 6ʱ���������ת��������
		static final int UNTREEIFY_THRESHOLD = 6;

		// ���������� < 64����ʹ�����ȴﵽ������ֵ��Ҳֻ���ݶ�����������
		// ����Ƚ�������⣬�Ͼ�������ʱ��Ϳռ�����Ǹ߰���
		// Ϊ�˱���������ݡ����λ�ѡ��ĳ�ͻ�����ֵ����С�� 4 * TREEIFY_THRESHOLD
		static final int MIN_TREEIFY_CAPACITY = 64;

    /**
		*�����Ĺ�ϣbin�ڵ㣬���ڴ������Ŀ��(������
		* TreeNode���࣬����Ŀ������LinkedHashMap�С�)
     */
    // ��1.7��Entryһ��
    static class Node<K,V> implements Map.Entry<K,V> {
    		// ����ϣֵ
        final int hash;
        // ���key
        final K key;
        // ���value
        V value;
        // ��������һ��㣬��������
        Node<K,V> next;

        	
        // ���췽��
        Node(int hash, K key, V value, Node<K,V> next) {
            this.hash = hash;
            this.key = key;
            this.value = value;
            this.next = next;
        }

        public final K getKey()        { return key; }
        public final V getValue()      { return value; }
        public final String toString() { return key + "=" + value; }

        public final int hashCode() {
            return Objects.hashCode(key) ^ Objects.hashCode(value);
        }
	
				// �滻��ֵ��oldValue������null
        public final V setValue(V newValue) {
            V oldValue = value;
            value = newValue;
            return oldValue;
        }

        public final boolean equals(Object o) {
            if (o == this)
                return true;
            if (o instanceof Map.Entry) {
                Map.Entry<?,?> e = (Map.Entry<?,?>)o;
                if (Objects.equals(key, e.getKey()) &&
                    Objects.equals(value, e.getValue()))
                    return true;
            }
            return false;
        }
    }
*/
    */* ---------------- Static utilities -------------- */

    /**
		*����key.hashCode()����չ(XORs)���ߵ�ɢ��λ����Ϊ��ʹ����2���ݵ��ڱΣ����Լ���
		*���ڵ�ǰ�����ϵ�λ�ϱ仯��ɢ�л�������ײ��(��֪�����Ӱ���һ�鸡���
		*��С����д��������������)��������Ӧ��һ���任����ɢ�߱��ص�Ӱ�졣
		*���ٶȡ�Ч�ú�֮����һ��Ȩ��㲥��������Ϊ�кܶೣ���Ĺ�ϣֵ�Ѿ�����ֲ�
     */
    static final int hash(Object key) {
        int h;
        // ��key��hashCode�ĸ�16λ���16λ����������㣬���ɢ�ж�
        return (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);
    }

		// ����x���࣬������ǡ�C��ʵ�֡�����ʽComparable<C>"������Ϊ�ա�
    static Class<?> comparableClassFor(Object x) {
    		// Comparable��һ���ӿڣ�ֻ��һ��������public int compareTo(T o)
        if (x instanceof Comparable) {
            Class<?> c;
            // Type��Class�������ǣ�Type���Ա�ʾ���з��͵��࣬��Class����
            Type[] ts, as;
            Type t;
            ParameterizedType p;
            if ((c = x.getClass()) == String.class) // �ƹ����
                return c;
            // c.getGenericInterfaces() : ����ʵ�ֽӿ���Ϣ��Type���飬�������ͽӿ�
            if ((ts = c.getGenericInterfaces()) != null) {
                for (int i = 0; i < ts.length; ++i) {
                    if (((t = ts[i]) instanceof ParameterizedType) && // �ýӿ���һ���Ϸ�������
                        ((p = (ParameterizedType)t).getRawType() ==  // ��ȡ�ӿڲ����������ֵ����Ͷ���
                         Comparable.class) &&
                        (as = p.getActualTypeArguments()) != null &&  // ��ȡ�ӿڵķ��Ͳ�������
                        as.length == 1 && as[0] == c) // ֻ��һ�����Ͳ������Ҹ�ʵ�������Ǹ����ͱ���
                        return c;
                }
            }
        }
        return null;
    }

		//kc: key�����ͣ�k: Ŀ���key��x: ��ǰ����key
    @SuppressWarnings({"rawtypes","unchecked"}) // for cast to Comparable
    static int compareComparables(Class<?> kc, Object k, Object x) {
        return (x == null || x.getClass() != kc ? 0 :
                ((Comparable)k).compareTo(x));
    }

		// �õ��Ϸ������������ڵ���cap����С2��ָ������
    static final int tableSizeFor(int cap) {
    		// ��ֹ�ٽ��������capΪ2��ָ������ʱ��������һ�����Ϊ 2 * capacity
        int n = cap - 1;
        n |= n >>> 1;
        n |= n >>> 2;
        n |= n >>> 4;
        n |= n >>> 8;
        n |= n >>> 16;
        // �����������ܳ����涨�������
        return (n < 0) ? 1 : (n >= MAXIMUM_CAPACITY) ? MAXIMUM_CAPACITY : n + 1;
    }

    /* ---------------- Fields -------------- */

		// ����������飬��Ҫʱ���г�ʼ��
    transient Node<K,V>[] table;

		// ���滺���entrySet()����keySet()��values()��ʹ����AbstractMap������
    transient Set<Map.Entry<K,V>> entrySet;

		// �������������+����+����㣩
    transient int size;

    // �޸Ĵ����������������ݣ��������޸�value����ɾ�����ݣ��������ʱmodCount++
    // ��ConcurrentModificationException�쳣�йأ�������Hashmap1.7�жԴ˽����˽���
    transient int modCount;

		// ������ֵ����size > thresholdʱ����������
		// threshold = table.length * loadFactor
    int threshold;
		
		// ��������
    final float loadFactor;

    /* ---------------- Public operations -------------- */
		
		// ˫�ι��죺��������������
    public HashMap(int initialCapacity, float loadFactor) {
        if (initialCapacity < 0)
            throw new IllegalArgumentException("Illegal initial capacity: " +
                                               initialCapacity);
        if (initialCapacity > MAXIMUM_CAPACITY)
            initialCapacity = MAXIMUM_CAPACITY;
        if (loadFactor <= 0 || Float.isNaN(loadFactor))
            throw new IllegalArgumentException("Illegal load factor: " +
                                               loadFactor);
        this.loadFactor = loadFactor;
        // ������1.7������ͬ��1.7��ֱ�ӽ�initialCapacity��ֵ��threshold
        // 1.8ͨ��tableSizeFor()������Ϸ���capacity
        this.threshold = tableSizeFor(initialCapacity);
    }
		// ���ι��죺����������˫�ι��죬ʹ��Ĭ�ϵļ�������
    public HashMap(int initialCapacity) {
        this(initialCapacity, DEFAULT_LOAD_FACTOR);
    }
		
		// �޲ι��죬ֻ��ʼ����������
    public HashMap() {
        this.loadFactor = DEFAULT_LOAD_FACTOR;
    }
		
		// ���ι��죺map��ʹ��Ĭ�ϵļ�������
    public HashMap(Map<? extends K, ? extends V> m) {
        this.loadFactor = DEFAULT_LOAD_FACTOR;
        putMapEntries(m, false);
    }
    
		// һ����table ������ԭ������putAll
    // ���ù�ϵ��public void putAll(Map<? extends K, ? extends V> m)
    // public Object clone() || public HashMap(Map<? extends K, ? extends V> m)
    final void putMapEntries(Map<? extends K, ? extends V> m, boolean evict) {
    		// �������
        int s = m.size();
        if (s > 0) {
        		// ���鲻���ڣ���Ҫ��ʼ��
            if (table == null) {
            		// Ԥ�������Ҫ��capacity��С��+1.0F����ft = 0
            		// +1.0Fֻ�е�s / loadFactor = 2��ָ������ʱ�Ż�����2��
                float ft = ((float)s / loadFactor) + 1.0F;
                int t = ((ft < (float)MAXIMUM_CAPACITY) ?
                         (int)ft : MAXIMUM_CAPACITY);
                         
                // �ڵ��ñ�����֮ǰ��һ���ȵ����˹��췽��
                // 1������(������map�Ĳ���)��˫�ι��죺
                // һ��ִ��this.threshold = tableSizeFor(initialCapacity)�õ��˺Ϸ�����		
                // t > threshold : Ԥ����������ԭ�Ϸ���������Ҫ����Ԥ�������õ��ºϷ�����
                // t < threshold : Ԥ������С��ԭ�Ϸ��������������¹滮����
                // 2���޲ι��죬����Ϊmap�ĵ��ι��죺
                // threshold = 0���� t >= 1����t > threshold���������¹滮����
                if (t > threshold)
                    threshold = tableSizeFor(t);
            }
            // ������鲻Ϊnull����m�Ľ��������������ֵ��������
            else if (s > threshold)
                resize();
          	
          	// ����putVal()��ӽ�㣬�����put()����Ҳ�ǵ������������½��
            for (Map.Entry<? extends K, ? extends V> e : m.entrySet()) {
                K key = e.getKey();
                V value = e.getValue();
                putVal(hash(key), key, value, false, evict);
            }
        }
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public V get(Object key) {
        Node<K,V> e;
        return (e = getNode(hash(key), key)) == null ? null : e.value;
    }

		// ʵ����map.get()����ط���
    final Node<K,V> getNode(int hash, Object key) {
        Node<K,V>[] tab; 
        Node<K,V> first, e; 
        int n;
        K k;
        // table��Ϊnull��table.length != 0�������������Ч
        if ((tab = table) != null && (n = tab.length) > 0 &&
        		// �򻯲�����ֱ�ӵõ��±��ٵõ�node
            (first = tab[(n - 1) & hash]) != null) {
            // ���Ǽ���һ����㣻�������������������ڵ�һ�����ļ�鲢�޲���
            // ��һ���̶������ô�������ȼ�����ܣ����н��Ĳ��Ҵ��۽ϴ�
 						// ��1.7һ�£���ͬ���������equals()�Ϳ���ƥ��
            if (first.hash == hash &&  
                ((k = first.key) == key || (key != null && key.equals(k))))
                return first;
            // ����һ����㲻��Ŀ���㣬����Ҫ���������
            if ((e = first.next) != null) {
            		// ����������ú�����Ĳ��ҷ���
                if (first instanceof TreeNode)
                    return ((TreeNode<K,V>)first).getTreeNode(hash, key);
                // ��do-while���������whileҲ���ԣ�ֻ�������һ���ж�
                // while(e != null) {e = e.next}����һ��ѭ���У�e != nullû�б�Ҫ
                do {
                    if (e.hash == hash &&
                        ((k = e.key) == key || (key != null && key.equals(k))))
                        return e;
                } while ((e = e.next) != null);
            }
        }
        return null;
    }

    public boolean containsKey(Object key) {
        return getNode(hash(key), key) != null;
    }

		// ��������û�д�ӳ�䣬����ӣ��У����滻��ֵ�������ؾ�ֵ
    public V put(K key, V value) {
    		//	����false���ı�����ֵ������true�������ڴ���ģʽ
        return putVal(hash(key), key, value, false, true);
    }

    // @param onlyIfAbsent ���Ϊtrue���򲻸ı�����ֵ�����滻��ֵ��
    // @param evict �˲������ݸ��˿շ���������LinkedHashMap
    // ʵ����map.put()����ط���
    final V putVal(int hash, K key, V value, boolean onlyIfAbsent,
                   boolean evict) {
        Node<K,V>[] tab; 
        Node<K,V> p; 
        int n, i;
        
        // ������ģʽ���ж������Ƿ��ʼ����û�����ȳ�ʼ��
        if ((tab = table) == null || (n = tab.length) == 0)
            n = (tab = resize()).length;
        // ������±���û���κν�㣬��ֱ�Ӵ������
        if ((p = tab[i = (n - 1) & hash]) == null)
        		// null��ʾ��������һ���Ϊ�գ�����㼴Ϊ����ĩ�ڵ�
            tab[i] = newNode(hash, key, value, null);
        // ���±����н�㣬����������Ҳ��������
        else {
            Node<K,V> e;
            K k;
            // �������һ����ԭ����getNode()����һ��
            if (p.hash == hash &&
                ((k = p.key) == key || (key != null && key.equals(k))))
                // ֱ�Ӹ������ã���������oldValue�滻
                e = p;
            // �����
            else if (p instanceof TreeNode)
            		// 1������oldTreeNode��ֱ�ӷ��ظý��
            		// 2��������oldTreeNode�������������
                e = ((TreeNode<K,V>)p).putTreeVal(this, tab, hash, key, value);
            else {
            		// ��������
                for (int binCount = 0; ; ++binCount) {
                		// ��ĩ�ڵ��ж�����ĳ��Ȳ�������
                    if ((e = p.next) == null) {
                    		// p���ڵ����壺�������������������ĩβ�ڵ㣬����β��
                    		// β�ӷ����ӽ��
                        p.next = newNode(hash, key, value, null);
                        // TREEIFY_THRESHOLD = 8�����ڵ���� >= 8ʱתΪ���ṹ
                        if (binCount >= TREEIFY_THRESHOLD - 1)
                            treeifyBin(tab, hash);
                        break;
                    }
                    // �ҵ���key��Ӧ�Ľ��
                    if (e.hash == hash &&
                        ((k = e.key) == key || (key != null && key.equals(k))))
                        break;
                    p = e;
                }
            }
            // ���ھɵ�ӳ���ϵ
            if (e != null) {
                V oldValue = e.value;
                // onlyIfAbsent�����Ƿ�ı�����ֵ������1.7�Ǽ�ǿ
                if (!onlyIfAbsent || oldValue == null)
                    e.value = value;
                // �շ���
                afterNodeAccess(e);
                return oldValue;
            }
        }
        ++modCount;
        // ������������1.7��������
        // 1.7��������(size >= threshold) && (null != table[bucketIndex])
        if (++size > threshold)
            resize();
        // �շ���
        afterNodeInsertion(evict);
        return null;
    }

		// ��ʼ��table���������ݣ���Ȼ��β�ӷ���������
    final Node<K,V>[] resize() {
        Node<K,V>[] oldTab = table;
        int oldCap = (oldTab == null) ? 0 : oldTab.length;
        int oldThr = threshold;
        int newCap, newThr = 0;
				
				// ��ʼ�������
				// 1���û�������capacity����oldThr > 0����
				// 2���û�û������capacity(�޲ι���)��������else����
				// �ǳ�ʼ�������
				// oldCap > 0 ������newCap��Ȼ����һ������threshold��һ�����
        if (oldCap > 0) {
        		// �����������������ٽ�ֵ����Լ�����������������2^31-1 = 2*(2^30)
        		// ���oldCap >= MAXIMUM_CAPACITY����������ôoldCap���ֻ����2^29
            if (oldCap >= MAXIMUM_CAPACITY) {
                threshold = Integer.MAX_VALUE;
                return oldTab;
            }
            // choice 1 ������һ������С�����ֵ && oldCap >= 16����һ������
            else if ((newCap = oldCap << 1) < MAXIMUM_CAPACITY &&
                     oldCap >= DEFAULT_INITIAL_CAPACITY)
                // ���������������Ϊ���Թ�ϵ��ֻ�ý�������ֵ����һ������
                newThr = oldThr << 1;
        }
        else if (oldThr > 0)
         		// choice 2 ��ʹ��˫�λ򵥲ι��췽��(����������Ϊmap�Ĺ��췽��)�Ľ��
         		// ��ʼ��������Ϊ��ֵ����Ϊ��˫�ι��췽���н�������ֵ����threshold
         		// this.threshold = tableSizeFor(initialCapacity);
            newCap = oldThr;
        // oldCap == 0 && oldThr == 0
        else { 
        		// ʹ�õ����޲ι��췽�������Ϊmap�ĵ��ι��췽��
            newCap = DEFAULT_INITIAL_CAPACITY;
            newThr = (int)(DEFAULT_LOAD_FACTOR * DEFAULT_INITIAL_CAPACITY);
        }
        // choice 1 ��������choice 2 ����ʱ��newThr == 0 ����
        if (newThr == 0) {
        		// ft��threshold
            float ft = (float)newCap * loadFactor;
            // �ٽ������newCap > MAXIMUM_CAPACITY && threshold < MAXIMUM_CAPACITY
            // ����newCap < MAXIMUM_CAPACITY������������newCap = MAXIMUM_CAPACITY�س���
            newThr = (newCap < MAXIMUM_CAPACITY && ft < (float)MAXIMUM_CAPACITY ?
                      (int)ft : Integer.MAX_VALUE);
        }
        threshold = newThr;
        @SuppressWarnings({"rawtypes","unchecked"})
        // ����capacity�趨�Ĺ�������newCap���ΪMAXIMUM_CAPACITY�����Բ��ÿ��ǺϷ�����
        // ��ʱ�����ʣ�����ǰ��newCap���Ϸ������ﲻ�����½����飿����Ը�´ο����⻹�ܼǵã�����û�˻᣿��
            Node<K,V>[] newTab = (Node<K,V>[])new Node[newCap];
        table = newTab;
        if (oldTab != null) {
        		//���������±꣬��hash��Ĵ洢�����£����洢���±겢������
            for (int j = 0; j < oldCap; ++j) {
                Node<K,V> e;
                if ((e = oldTab[j]) != null) {
                		// �߳�1��if�ж�֮ǰ�����߳�2����������˶��е�oldTab[j]
                		// �߳�1����ִ�У�����oldTab[j]Ϊ�գ��������±������Ԫ��
                		// ��ôԭtable�����е����ݱ��ֳ��������֣��������߳��и��Ե�newTab
                		// ���Ḳ��table��Ա�����������̸߳���table��ǰ��˳������һ�����ݶ�ʧ
                		// ������������ص����ݶ�ʧ��������صĻ�������ֻ�ᱣ��ԭ����һ������һ����
                		// ���˲��ɵþ������Ǹ�����֮�ʣ�ΪʲôҪ�ÿ�oldTab[j]��
                		// e = oldTab[j]����Ѿ��������������ͷ���������ˣ�gc���ղ�����һ���㼯
                		// ���ڷ������н�����gc���Զ�����ԭ����table���ã������ⲻ�Ƕ��һ����
                		// ������ﲻ�ÿգ���ÿ���߳�ת�ƵĶ�һ������Ȼ�ή�����ܣ�������������²��ᶪʧ
                    oldTab[j] = null;
                    // �ȼ���һ�������Ч��
                    if (e.next == null)
                        newTab[e.hash & (newCap - 1)] = e;
                    else if (e instanceof TreeNode)
                    		// ��ɢԭ���νṹ���γ�����
                        ((TreeNode<K,V>)e).split(this, newTab, j, oldCap);
                    // �˽�㼯������������ܻὫԭ����һ������Ϊ������
                    // ����Ϊʲô������������������߼������¶�
                    else {
                        Node<K,V> loHead = null, loTail = null;
                        Node<K,V> hiHead = null, hiTail = null;
                        Node<K,V> next;
                        // �˴���do-while��getNode()�����е�һ��
                        do {
                            next = e.next;
                            // 0000 0101 & 0000 1111 = 0000 0101
                            // 0000 0101 & 0001 1111 = 0000 0101
                            // oldCap = 0001 0000
                            // ���һ�����ӵĸ�һλ��Ӧ��hashֵ����һλ�Ƿ�Ϊ1
                            // ��Ϊ1����newIndex = oldIndex + oldCap
                            // ��Ϊ0����newIndex = oldIndex
                            // ����һλ�Ƿ�Ϊ1����һ�����п���ÿ������hash����һλ����0
                            // �Ǿ�ֻ�����һ����
                            
                            // ԭ�±��γ�����
                            if ((e.hash & oldCap) == 0) {
                            		// ��һ���ڵ�ʱ��loTail == null
                                if (loTail == null)
                                    loHead = e;
                                else
                                    loTail.next = e;
                                loTail = e;
                            }
                            // ��һ�±��γ�����
                            else {
                                if (hiTail == null)
                                    hiHead = e;
                                else
                                    hiTail.next = e;
                                hiTail = e;
                            }
                        } while ((e = next) != null);
                    		// oldIndex����������������
                        if (loTail != null) {
                            loTail.next = null;
                            newTab[j] = loHead;
                        }
                        // oldIndex + oldCap����������������
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
		
		// ��ת��Ϊ��������������������Ĺ�ϵ��Ϊ�˲�ѯvalue
    final void treeifyBin(Node<K,V>[] tab, int hash) {
        int n, index; Node<K,V> e;
        // �������û�����⣬��ôtab��Զ������null
        // ��������С������ͨ�����ݵķ�ʽ�����ٹ�ϣ��ײ������תΪ��
        if (tab == null || (n = tab.length) < MIN_TREEIFY_CAPACITY)
            resize();
        // �õ�Ŀ���±������׼��ת��
        else if ((e = tab[index = (n - 1) & hash]) != null) {
            TreeNode<K,V> hd = null, tl = null;
            do {
            		// �����ǵ�����Node�Ĺ��췽��������һ���������Ľڵ�
                TreeNode<K,V> p = replacementTreeNode(e, null);
                // ���ｫ�����Ľڵ�������
                if (tl == null)
                    hd = p;
                else {
                    p.prev = tl;
                    // next��Ա����HashMap.Node���̳й�ϵ
                    tl.next = p;
                }
                tl = p;
            } while ((e = e.next) != null);
            if ((tab[index] = hd) != null)
            		// ����תΪ���ķ���
                hd.treeify(tab);
        }
    }
	
    public void putAll(Map<? extends K, ? extends V> m) {
        putMapEntries(m, true);
    }

    public V remove(Object key) {
        Node<K,V> e;
        return (e = removeNode(hash(key), key, null, false, true)) == null ?
            null : e.value;
    }
     
    // @param matchValue ���Ϊtrue�������ֵ���ʱɾ������ֵ�Զ��������
    // @param movable ���Ϊfalse������ɾ��ʱ���ƶ������ڵ�
    // ʵ����map.remove����ط���
    final Node<K,V> removeNode(int hash, Object key, Object value,
                               boolean matchValue, boolean movable) {
        Node<K,V>[] tab; Node<K,V> p; int n, index;
        // table��������Ч, �ҵ�ǰ�±������Ч�ڵ�
        if ((tab = table) != null && (n = tab.length) > 0 &&
            (p = tab[index = (n - 1) & hash]) != null) {
            Node<K,V> node = null, e; K k; V v;
            // ����һ�����
            if (p.hash == hash &&
                ((k = p.key) == key || (key != null && key.equals(k))))
                node = p;
            else if ((e = p.next) != null) {
            		// ���Һ����
                if (p instanceof TreeNode)
                    node = ((TreeNode<K,V>)p).getTreeNode(hash, key);
                else {
                		// �����������㿴������ͻᷢ�ִ����Ѿ���ʼ�ظ���
                    do {
                        if (e.hash == hash &&
                            ((k = e.key) == key ||
                             (key != null && key.equals(k)))) {
                            node = e;
                            break;
                        }
                        // p Ϊ��һ�ڵ�
                        p = e;
                    } while ((e = e.next) != null);
                }
            }
            // matchValue = falseʱ�����ý���value�ĶԱ�
            // �ж�value�Ƿ���ͬ��1.8�����ӵĹ��ܣ����ҿ��Կ���
            if (node != null && (!matchValue || (v = node.value) == value ||
                                 (value != null && value.equals(v)))) {
                // �����
                if (node instanceof TreeNode)
                    ((TreeNode<K,V>)node).removeTreeNode(this, tab, movable);
								// ����ɾ��                
                else if (node == p)
                		// ���� node == p �������ǣ���һ���ڵ㼴ΪĿ��ڵ�
                    tab[index] = node.next;
                else
                		// ��Ϊ��һ���ڵ㣬����һ�ڵ����һ�ڵ�������
                    p.next = node.next;
                // �޸�����һ
                ++modCount;
                --size;
                // �շ���
                afterNodeRemoval(node);
                return node;
            }
        }
        return null;
    }

//    public static void fill(Object[] a, Object val) {
//    	for (int i = 0, len = a.length; i < len; i++) {
//    	  a[i] = val;
//    	}
//    }
		// 1.7�е���Arrays.fill(tab, null); ����ʵ����ͬ��������գ���gc����
    public void clear() {
        Node<K,V>[] tab;
        modCount++;
        if ((tab = table) != null && size > 0) {
            size = 0;
            // �������飬�������±�Ԫ����Ϊnull
            for (int i = 0; i < tab.length; ++i)
                tab[i] = null;
        }
    }

    public boolean containsValue(Object value) {
        Node<K,V>[] tab; V v;
        if ((tab = table) != null && size > 0) {
        		// ���ѭ������������
            for (int i = 0; i < tab.length; ++i) {
            		// �ڲ�ѭ��������������Ϊ�����������ϵ����
            		// ������key��Ϊ���ҵ����ݣ������Ż����ܣ�get()����ʹ�ú�����Դ���ѯ����
            		// ������ֵ���޷��Ż����������ȫ���ڵ㣬���ֱ��ʹ��forѭ��
                for (Node<K,V> e = tab[i]; e != null; e = e.next) {
                    if ((v = e.value) == value ||
                        (value != null && value.equals(v)))
                        return true;
                }
            }
        }
        return false;
    }

		// keySet��values��������AbstractMap������
    public Set<K> keySet() {
        Set<K> ks = keySet;
        if (ks == null) {
            ks = new KeySet();
            keySet = ks;
        }
        return ks;
    }

		// �����1.7���ܷḻ�˺ܶ�
    final class KeySet extends AbstractSet<K> {
        public final int size()                 { return size; }
        public final void clear()               { HashMap.this.clear(); }
        // iterator�Ĵ����ַ�һ��
        public final Iterator<K> iterator()     { return new KeyIterator(); }
        public final boolean contains(Object o) { return containsKey(o); }
        // ��iterator()����֮�󣬵���remove()���������Hashmap��remove()һ��
        // �������ConcurrentModificationException�쳣��new KeyIterator()�Ѿ�modCount�Ѿ�ȷ��
        public final boolean remove(Object key) {
            return removeNode(hash(key), key, null, false, true) != null;
        }
        // ��ֱ���
        public final Spliterator<K> spliterator() {
            return new KeySpliterator<>(HashMap.this, 0, -1, 0, 0);
        }
        
//			new HashMap<String, String>().keySet().forEach(new Consumer<String>() {
//				@Override
//				public void accept(String t) {
//					System.out.println("");
//				}
//			});
				// �ṩ��һ����ʽ��forEach
        public final void forEach(Consumer<? super K> action) {
            Node<K,V>[] tab;
            if (action == null)
                throw new NullPointerException();
            // table���ڽ��
            if (size > 0 && (tab = table) != null) {
            		// �ڱ���ǰȷ���޸�������Ϊ�˱�֤�̰߳�ȫ
                int mc = modCount;
                // �������飬���ڵ��е������ϵ����������
                for (int i = 0; i < tab.length; ++i) {
                    for (Node<K,V> e = tab[i]; e != null; e = e.next)
                    		// ����㴫�ݸ���action
                        action.accept(e.key);
                }
                // �����Ǳ�����ɺ󱨳����쳣����˵��ı���ԭtable��
                // ����ı����������Ҳ��䣬������ͻᱨ�쳣
                if (modCount != mc)
                    throw new ConcurrentModificationException();
            }
        }
    }
    
    public Collection<V> values() {
        Collection<V> vs = values;
        if (vs == null) {
            vs = new Values();
            values = vs;
        }
        return vs;
    }

    final class Values extends AbstractCollection<V> {
        public final int size()                 { return size; }
        public final void clear()               { HashMap.this.clear(); }
        public final Iterator<V> iterator()     { return new ValueIterator(); }
        public final boolean contains(Object o) { return containsValue(o); }
        public final Spliterator<V> spliterator() {
            return new ValueSpliterator<>(HashMap.this, 0, -1, 0, 0);
        }
        public final void forEach(Consumer<? super V> action) {
            Node<K,V>[] tab;
            if (action == null)
                throw new NullPointerException();
            if (size > 0 && (tab = table) != null) {
                int mc = modCount;
                for (int i = 0; i < tab.length; ++i) {
                    for (Node<K,V> e = tab[i]; e != null; e = e.next)
                        action.accept(e.value);
                }
                if (modCount != mc)
                    throw new ConcurrentModificationException();
            }
        }
    }

    public Set<Map.Entry<K,V>> entrySet() {
        Set<Map.Entry<K,V>> es;
        return (es = entrySet) == null ? (entrySet = new EntrySet()) : es;
    }

    final class EntrySet extends AbstractSet<Map.Entry<K,V>> {
        public final int size()                 { return size; }
        public final void clear()               { HashMap.this.clear(); }
        public final Iterator<Map.Entry<K,V>> iterator() {
            return new EntryIterator();
        }
        public final boolean contains(Object o) {
            if (!(o instanceof Map.Entry))
                return false;
            Map.Entry<?,?> e = (Map.Entry<?,?>) o;
            Object key = e.getKey();
            Node<K,V> candidate = getNode(hash(key), key);
            return candidate != null && candidate.equals(e);
        }
        public final boolean remove(Object o) {
            if (o instanceof Map.Entry) {
                Map.Entry<?,?> e = (Map.Entry<?,?>) o;
                Object key = e.getKey();
                Object value = e.getValue();
                return removeNode(hash(key), key, value, true, true) != null;
            }
            return false;
        }
        public final Spliterator<Map.Entry<K,V>> spliterator() {
            return new EntrySpliterator<>(HashMap.this, 0, -1, 0, 0);
        }
        public final void forEach(Consumer<? super Map.Entry<K,V>> action) {
            Node<K,V>[] tab;
            if (action == null)
                throw new NullPointerException();
            if (size > 0 && (tab = table) != null) {
                int mc = modCount;
                for (int i = 0; i < tab.length; ++i) {
                    for (Node<K,V> e = tab[i]; e != null; e = e.next)
                        action.accept(e);
                }
                if (modCount != mc)
                    throw new ConcurrentModificationException();
            }
        }
    }

    // Overrides of JDK8 Map extension methods
    // ����JDK8ӳ����չ����
    @Override
    public V getOrDefault(Object key, V defaultValue) {
        Node<K,V> e;
        // ���ң���û���򷵻ز���defaultValue
        return (e = getNode(hash(key), key)) == null ? defaultValue : e.value;
    }

    @Override
    public V putIfAbsent(K key, V value) {
    		// ������ھ�ֵ�������滻
        return putVal(hash(key), key, value, true, true);
    }

    @Override
    public boolean remove(Object key, Object value) {
    		// �����ڼ�ֵ�Զ���ȵ�ʱ�����ɾ��
        return removeNode(hash(key), key, value, true, true) != null;
    }

    @Override
    public boolean replace(K key, V oldValue, V newValue) {
        Node<K,V> e; V v;
        // �����ļ�ֵƥ��
        if ((e = getNode(hash(key), key)) != null &&
            ((v = e.value) == oldValue || (v != null && v.equals(oldValue)))) {
            e.value = newValue;
            // �շ���
            afterNodeAccess(e);
            return true;
        }
        return false;
    }

    @Override
    public V replace(K key, V value) {
        Node<K,V> e;
        // ��ƥ�伴��
        if ((e = getNode(hash(key), key)) != null) {
            V oldValue = e.value;
            e.value = value;
            afterNodeAccess(e);
            return oldValue;
        }
        return null;
    }

		// Function���û����ݽ����Ķ��������Ǽ�ֵ��
		// V v = mappingFunction.apply(key);
		// 1���н�㣬��value != null�����ؾ�ֵ
		// 2���н�㣬value == null������value
		// 3���޽�㣬�������ϻ�����ͨ������
    @Override
    public V computeIfAbsent(K key,
                             Function<? super K, ? extends V> mappingFunction) {
        if (mappingFunction == null)
            throw new NullPointerException();
        int hash = hash(key);
        Node<K,V>[] tab; Node<K,V> first; int n, i;
        int binCount = 0;
        TreeNode<K,V> t = null;
        Node<K,V> old = null;
        // ��put�������ƣ�put�ڷ�������ж� size > threshold
        // (tab = table) == null����ж�˵����������������Hashmap��ʼ����ֱ��ʹ��
        // ��һ��������жϵ���putVal()
        if (size > threshold || (tab = table) == null ||
            (n = tab.length) == 0)
            n = (tab = resize()).length;
        // ������ң�Ϊʲô����getNode()?
        if ((first = tab[i = (n - 1) & hash]) != null) {
            if (first instanceof TreeNode)
            		// oldΪget����ֵ��t = first
                old = (t = (TreeNode<K,V>)first).getTreeNode(hash, key);
            else {
                Node<K,V> e = first; K k;
                do {
                    if (e.hash == hash &&
                        ((k = e.key) == key || (key != null && key.equals(k)))) {
                        old = e;
                        break;
                    }
                    ++binCount;
                } while ((e = e.next) != null);
            }
            V oldValue;
            // ������ھ�ֵ��ֱ�ӷ��ؾ�ֵ����������
            if (old != null && (oldValue = old.value) != null) {
                afterNodeAccess(old);
                return oldValue;
            }
        }
        // �����ڸýڵ���ߣ��ýڵ��ֵΪnull��ִ����������
        V v = mappingFunction.apply(key);
        if (v == null) {
            return null;
        // old.value = null ���ڽ�㣺1�����ڵ� 2����ͨ���
        } else if (old != null) {
            old.value = v;
            afterNodeAccess(old);
            return v;
        }
        // t != null �����ڸý�㣬�����ڵ㲻��null�����������¼�һ�����
        else if (t != null)
            t.putTreeVal(this, tab, hash, key, v);
        // �������ڵ㣬�����ڸýڵ㣬�������һ����ͨ���
        else {
            tab[i] = newNode(hash, key, v, first);
            // ������ͨ�����ж��Ƿ���Ҫ����
            if (binCount >= TREEIFY_THRESHOLD - 1)
                treeifyBin(tab, hash);
        }
        ++modCount;
        ++size;
        afterNodeInsertion(true);
        return v;
    }

		// V v = remappingFunction.apply(key, oldValue);
		// 1�����ڸýڵ㣬value == null������null
		// 2�����ڸý�㣬value != null��V == null ɾ����V != null �滻
		// ���ڽ��Ų��������Բ��ÿ��ж�
    public V computeIfPresent(K key,
                              BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        if (remappingFunction == null)
            throw new NullPointerException();
        Node<K,V> e; V oldValue;
        int hash = hash(key);
        // û����һ�������Ŀ��жϣ�˵���������������table���ڵ�����²�����
        // value != null ����ԭ����ֵ
        if ((e = getNode(hash, key)) != null &&
            (oldValue = e.value) != null) {
            // ������Ҫ��ֵ�ԣ���һ������Ҫֵ
            V v = remappingFunction.apply(key, oldValue);
            if (v != null) {
                e.value = v;
                afterNodeAccess(e);
                // ����newValue
                return v;
            }
            // v == null��oldValue != null ɾ���ü�ֵ��
            else
        				// false ����������ֵƥ�䣬��ƥ���ɾ��
                removeNode(hash, key, null, false, true);
        }
        return null;
    }

		// V v = remappingFunction.apply(key, oldValue);
		// 1�������ڽ�㣬��Ϊ���������ڵ㣻��Ϊ�����������
		// 2�����ڽ�㣬v == null��ɾ����v != null �滻
    @Override
    public V compute(K key,
                     BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        if (remappingFunction == null)
            throw new NullPointerException();
        int hash = hash(key);
        Node<K,V>[] tab; Node<K,V> first; int n, i;
        int binCount = 0;
        TreeNode<K,V> t = null;
        Node<K,V> old = null;
       	// ���жϣ���ʼ��
        if (size > threshold || (tab = table) == null ||
            (n = tab.length) == 0)
            n = (tab = resize()).length;
        if ((first = tab[i = (n - 1) & hash]) != null) {
            if (first instanceof TreeNode)
                old = (t = (TreeNode<K,V>)first).getTreeNode(hash, key);
            else {
                Node<K,V> e = first; K k;
                do {
                    if (e.hash == hash &&
                        ((k = e.key) == key || (key != null && key.equals(k)))) {
                        old = e;
                        break;
                    }
                    ++binCount;
                } while ((e = e.next) != null);
            }
        }
        // �������������ν
        V oldValue = (old == null) ? null : old.value;
        // ��ֵ��ƥ��
        V v = remappingFunction.apply(key, oldValue);
        // ���ڽ��
        if (old != null) {
        		// v != null �滻
            if (v != null) {
                old.value = v;
                afterNodeAccess(old);
            }
            // v == null ɾ��
            else
                removeNode(hash, key, null, false, true);
        }
        // �����ڽ��
        else if (v != null) {
        		// ���ڵ㲻Ϊ�գ��¼������
            if (t != null)
                t.putTreeVal(this, tab, hash, key, v);
            // �¼���ͨ���
            else {
                tab[i] = newNode(hash, key, v, first);
                // �ж��Ƿ�Ҫת��
                if (binCount >= TREEIFY_THRESHOLD - 1)
                    treeifyBin(tab, hash);
            }
            ++modCount;
            ++size;
            afterNodeInsertion(true);
        }
        return v;
    }

		// v = remappingFunction.apply(old.value, value)
		// 1�������ڽ�㣬��Ϊ���������ڵ㣻��Ϊ�����������
		// 2�����ڽ�㣬v == null(remappingFunction)��ɾ����v != null(value) �滻
    @Override
    public V merge(K key, V value,
                   BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        if (value == null)
            throw new NullPointerException();
        if (remappingFunction == null)
            throw new NullPointerException();
        int hash = hash(key);
        Node<K,V>[] tab; Node<K,V> first; int n, i;
        int binCount = 0;
        TreeNode<K,V> t = null;
        Node<K,V> old = null;
        // ���ж�
        if (size > threshold || (tab = table) == null ||
            (n = tab.length) == 0)
            n = (tab = resize()).length;
        if ((first = tab[i = (n - 1) & hash]) != null) {
            if (first instanceof TreeNode)
                old = (t = (TreeNode<K,V>)first).getTreeNode(hash, key);
            else {
                Node<K,V> e = first; K k;
                do {
                    if (e.hash == hash &&
                        ((k = e.key) == key || (key != null && key.equals(k)))) {
                        old = e;
                        break;
                    }
                    ++binCount;
                } while ((e = e.next) != null);
            }
        }
        // ���ڽ��
        if (old != null) {
            V v;
            if (old.value != null)
                v = remappingFunction.apply(old.value, value);
            // old.value = null
            else
                v = value;
            // �滻
            if (v != null) {
                old.value = v;
                afterNodeAccess(old);
            }
            // v == null ɾ��
            else
                removeNode(hash, key, null, false, true);
            // ������ֵ����null��null����remappingFunction
            return v;
        }
        // �޽�㣬�����쳣����ˣ�valueһ����Ϊnull
        if (value != null) {
            if (t != null)
                t.putTreeVal(this, tab, hash, key, value);
            else {
                tab[i] = newNode(hash, key, value, first);
                if (binCount >= TREEIFY_THRESHOLD - 1)
                    treeifyBin(tab, hash);
            }
            ++modCount;
            ++size;
            afterNodeInsertion(true);
        }
        return value;
    }

		// ��ʽforEach ConcurrentModificationException
    @Override
    public void forEach(BiConsumer<? super K, ? super V> action) {
        Node<K,V>[] tab;
        if (action == null)
            throw new NullPointerException();
        if (size > 0 && (tab = table) != null) {
            int mc = modCount;
            for (int i = 0; i < tab.length; ++i) {
                for (Node<K,V> e = tab[i]; e != null; e = e.next)
                    action.accept(e.key, e.value);
            }
            if (modCount != mc)
                throw new ConcurrentModificationException();
        }
    }
		
		// �̰߳�ȫ�����滻������ֻ�ǲ���
    @Override
    public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
        Node<K,V>[] tab;
        if (function == null)
            throw new NullPointerException();
        if (size > 0 && (tab = table) != null) {
            int mc = modCount;
            for (int i = 0; i < tab.length; ++i) {
                for (Node<K,V> e = tab[i]; e != null; e = e.next) {
                    e.value = function.apply(e.key, e.value);
                }
            }
            if (modCount != mc)
                throw new ConcurrentModificationException();
        }
    }

    /* ------------------------------------------------------------ */
    // Cloning and serialization
		
		// 1.7�еĿ�¡ʮ�ֵĸ���
    @SuppressWarnings("unchecked")
    @Override
    public Object clone() {
        HashMap<K,V> result;
        try {
            result = (HashMap<K,V>)super.clone();
        } catch (CloneNotSupportedException e) {
            // this shouldn't happen, since we are Cloneable
            throw new InternalError(e);
        }
        result.reinitialize();
        // ��this������ӳ�临�Ƹ�result�����¡
        result.putMapEntries(this, false);
        return result;
    }

    // These methods are also used when serializing HashSets
    final float loadFactor() { return loadFactor; }
    final int capacity() {
    		// �չ���ʱ��threshold = 0��������Ĭ������
        return (table != null) ? table.length :
            (threshold > 0) ? threshold :
            DEFAULT_INITIAL_CAPACITY;
    }

    /**
     * Save the state of the <tt>HashMap</tt> instance to a stream (i.e.,
     * serialize it).
     *
     * @serialData The <i>capacity</i> of the HashMap (the length of the
     *             bucket array) is emitted (int), followed by the
     *             <i>size</i> (an int, the number of key-value
     *             mappings), followed by the key (Object) and value (Object)
     *             for each key-value mapping.  The key-value mappings are
     *             emitted in no particular order.
     */
    private void writeObject(java.io.ObjectOutputStream s)
        throws IOException {
        int buckets = capacity();
        // Write out the threshold, loadfactor, and any hidden stuff
        s.defaultWriteObject();
        s.writeInt(buckets);
        s.writeInt(size);
        internalWriteEntries(s);
    }

    /**
     * Reconstitute the {@code HashMap} instance from a stream (i.e.,
     * deserialize it).
     */
    private void readObject(java.io.ObjectInputStream s)
        throws IOException, ClassNotFoundException {
        // Read in the threshold (ignored), loadfactor, and any hidden stuff
        s.defaultReadObject();
        reinitialize();
        if (loadFactor <= 0 || Float.isNaN(loadFactor))
            throw new InvalidObjectException("Illegal load factor: " +
                                             loadFactor);
        s.readInt();                // Read and ignore number of buckets
        int mappings = s.readInt(); // Read number of mappings (size)
        if (mappings < 0)
            throw new InvalidObjectException("Illegal mappings count: " +
                                             mappings);
        else if (mappings > 0) { // (if zero, use defaults)
            // Size the table using given load factor only if within
            // range of 0.25...4.0
            float lf = Math.min(Math.max(0.25f, loadFactor), 4.0f);
            float fc = (float)mappings / lf + 1.0f;
            int cap = ((fc < DEFAULT_INITIAL_CAPACITY) ?
                       DEFAULT_INITIAL_CAPACITY :
                       (fc >= MAXIMUM_CAPACITY) ?
                       MAXIMUM_CAPACITY :
                       tableSizeFor((int)fc));
            float ft = (float)cap * lf;
            threshold = ((cap < MAXIMUM_CAPACITY && ft < MAXIMUM_CAPACITY) ?
                         (int)ft : Integer.MAX_VALUE);

            // Check Map.Entry[].class since it's the nearest public type to
            // what we're actually creating.
            SharedSecrets.getJavaOISAccess().checkArray(s, Map.Entry[].class, cap);
            @SuppressWarnings({"rawtypes","unchecked"})
            Node<K,V>[] tab = (Node<K,V>[])new Node[cap];
            table = tab;

            // Read the keys and values, and put the mappings in the HashMap
            for (int i = 0; i < mappings; i++) {
                @SuppressWarnings("unchecked")
                    K key = (K) s.readObject();
                @SuppressWarnings("unchecked")
                    V value = (V) s.readObject();
                putVal(hash(key), key, value, false, false);
            }
        }
    }

    /* ------------------------------------------------------------ */
    // iterators ������

    abstract class HashIterator {
        Node<K,V> next;        // next entry to return ��һ���
        Node<K,V> current;     // current entry ��ǰ��㣬���Ǽ������ص���һ���
        int expectedModCount;  // for fast-fail
        int index;             // current slot

        HashIterator() {
        		// ȷ��������
            expectedModCount = modCount;
            Node<K,V>[] t = table;
            current = next = null;
            index = 0;
            if (t != null && size > 0) { // advance to first entry
            		// �ҵ������е�һ���ǿյ�Ԫ�أ�nextΪ��һ����ЧԪ��
                do {} while (index < t.length && (next = t[index++]) == null);
            }
        }

        public final boolean hasNext() {
            return next != null;
        }

        final Node<K,V> nextNode() {
            Node<K,V>[] t;
            Node<K,V> e = next;
            if (modCount != expectedModCount)
                throw new ConcurrentModificationException();
            if (e == null)
                throw new NoSuchElementException();
            // ��hashmap�У���ЧԪ�ص��±겻һ���������ģ���˻���Ҫ�ҵ���һ����Ч��Ԫ��
            if ((next = (current = e).next) == null && (t = table) != null) {
                do {} while (index < t.length && (next = t[index++]) == null);
            }
            return e;
        }

        public final void remove() {
            Node<K,V> p = current;
            if (p == null)
                throw new IllegalStateException();
            if (modCount != expectedModCount)
                throw new ConcurrentModificationException();
            current = null;
            K key = p.key;
            removeNode(hash(key), key, null, false, false);
            // iterator�е�remove�����modify״̬����������޸��쳣
            expectedModCount = modCount;
        }
    }

    final class KeyIterator extends HashIterator
        implements Iterator<K> {
        public final K next() { return nextNode().key; }
    }

    final class ValueIterator extends HashIterator
        implements Iterator<V> {
        public final V next() { return nextNode().value; }
    }

    final class EntryIterator extends HashIterator
        implements Iterator<Map.Entry<K,V>> {
        public final Map.Entry<K,V> next() { return nextNode(); }
    }

    /* ------------------------------------------------------------ */
    // spliterators

    static class HashMapSpliterator<K,V> {
        final HashMap<K,V> map;
        Node<K,V> current;          // current node
        int index;                  // current index, modified on advance/split ��ǰ������Ԥ���޸�/�ָ�
        int fence;                  // one past last index ���һ������
        int est;                    // size estimate ��ģ����
        int expectedModCount;       // for comodification checks

        HashMapSpliterator(HashMap<K,V> m, int origin,
                           int fence, int est,
                           int expectedModCount) {
            this.map = m;
            this.index = origin;
            this.fence = fence;
            this.est = est;
            this.expectedModCount = expectedModCount;
        }

        final int getFence() { // �ڵ�һ��ʹ��ʱ��ʼ��դ���ʹ�С
            int hi;
            // �����涨������Χ����Ĭ��Ϊ���鳤��
            if ((hi = fence) < 0) {
                HashMap<K,V> m = map;
                est = m.size;
                // ȷ��������
                expectedModCount = m.modCount;
                Node<K,V>[] tab = m.table;
                // tab.length != size
                hi = fence = (tab == null) ? 0 : tab.length;
            }
            return hi;
        }

        public final long estimateSize() {
            getFence(); // force init
            return (long) est;
        }
    }

    static final class KeySpliterator<K,V>
        extends HashMapSpliterator<K,V>
        implements Spliterator<K> {
        KeySpliterator(HashMap<K,V> m, int origin, int fence, int est,
                       int expectedModCount) {
            super(m, origin, fence, est, expectedModCount);
        }

        public KeySpliterator<K,V> trySplit() {
        		// �ָ�����ȷ�
            int hi = getFence(), lo = index, mid = (lo + hi) >>> 1;
            // ��ǰindex >= mid��current != null˵���ڱ�����;
            return (lo >= mid || current != null) ? null :
                new KeySpliterator<>(map, lo, index = mid, est >>>= 1,
                                        expectedModCount);
        }
				
				// ����ʣ�µ�
				// �ָ�������������±�Ϊ�磬��������size��������Ϊ��
        public void forEachRemaining(Consumer<? super K> action) {
            int i, hi, mc;
            if (action == null)
                throw new NullPointerException();
            HashMap<K,V> m = map;
            Node<K,V>[] tab = m.table;
            // ����Ҳ����getFence()
            if ((hi = fence) < 0) {
                mc = expectedModCount = m.modCount;
                hi = fence = (tab == null) ? 0 : tab.length;
            }
            else
                mc = expectedModCount;
            // i >= 0 && i < hi (i < fence)����index��ʼ����hi����
            if (tab != null && tab.length >= hi &&
                (i = index) >= 0 && (i < (index = hi) || current != null)) {
                Node<K,V> p = current;
                current = null;
                do {
                    if (p == null)
                    		// ���±겻�������ݣ����ȡ��һ�±�
                        p = tab[i++];
                    else {
                    		// ���±�������ݣ���������
                        action.accept(p.key);
                        p = p.next;
                    }
                } while (p != null || i < hi);
                // ��ѭ�����ж��޸�״̬
                if (m.modCount != mc)
                    throw new ConcurrentModificationException();
            }
        }

        public boolean tryAdvance(Consumer<? super K> action) {
            int hi;
            if (action == null)
                throw new NullPointerException();
            Node<K,V>[] tab = map.table;
            // �±����λ(hi / fence)���ô������鳤��
            if (tab != null && tab.length >= (hi = getFence()) && index >= 0) {
                while (current != null || index < hi) {
                    if (current == null)
                        current = tab[index++];
                    else {
                        K k = current.key;
                        current = current.next;
                        action.accept(k);
                        // ��ѭ�����ж��޸�״̬
                        if (map.modCount != expectedModCount)
                            throw new ConcurrentModificationException();
                        return true;
                    }
                }
            }
            return false;
        }
        
				// public static final int DISTINCT   = 0x00000001
				// public static final int SIZED      = 0x00000040;
        public int characteristics() {
            return (fence < 0 || est == map.size ? Spliterator.SIZED : 0) |
                Spliterator.DISTINCT;
        }
    }

    static final class ValueSpliterator<K,V>
        extends HashMapSpliterator<K,V>
        implements Spliterator<V> {
        ValueSpliterator(HashMap<K,V> m, int origin, int fence, int est,
                         int expectedModCount) {
            super(m, origin, fence, est, expectedModCount);
        }

        public ValueSpliterator<K,V> trySplit() {
            int hi = getFence(), lo = index, mid = (lo + hi) >>> 1;
            return (lo >= mid || current != null) ? null :
                new ValueSpliterator<>(map, lo, index = mid, est >>>= 1,
                                          expectedModCount);
        }

        public void forEachRemaining(Consumer<? super V> action) {
            int i, hi, mc;
            if (action == null)
                throw new NullPointerException();
            HashMap<K,V> m = map;
            Node<K,V>[] tab = m.table;
            if ((hi = fence) < 0) {
                mc = expectedModCount = m.modCount;
                hi = fence = (tab == null) ? 0 : tab.length;
            }
            else
                mc = expectedModCount;
            if (tab != null && tab.length >= hi &&
                (i = index) >= 0 && (i < (index = hi) || current != null)) {
                Node<K,V> p = current;
                current = null;
                do {
                    if (p == null)
                        p = tab[i++];
                    else {
                        action.accept(p.value);
                        p = p.next;
                    }
                } while (p != null || i < hi);
                if (m.modCount != mc)
                    throw new ConcurrentModificationException();
            }
        }

        public boolean tryAdvance(Consumer<? super V> action) {
            int hi;
            if (action == null)
                throw new NullPointerException();
            Node<K,V>[] tab = map.table;
            if (tab != null && tab.length >= (hi = getFence()) && index >= 0) {
                while (current != null || index < hi) {
                    if (current == null)
                        current = tab[index++];
                    else {
                        V v = current.value;
                        current = current.next;
                        action.accept(v);
                        if (map.modCount != expectedModCount)
                            throw new ConcurrentModificationException();
                        return true;
                    }
                }
            }
            return false;
        }

				// public static final int SIZED      = 0x00000040;
        public int characteristics() {
            return (fence < 0 || est == map.size ? Spliterator.SIZED : 0);
        }
    }

    static final class EntrySpliterator<K,V>
        extends HashMapSpliterator<K,V>
        implements Spliterator<Map.Entry<K,V>> {
        EntrySpliterator(HashMap<K,V> m, int origin, int fence, int est,
                         int expectedModCount) {
            super(m, origin, fence, est, expectedModCount);
        }

        public EntrySpliterator<K,V> trySplit() {
            int hi = getFence(), lo = index, mid = (lo + hi) >>> 1;
            return (lo >= mid || current != null) ? null :
                new EntrySpliterator<>(map, lo, index = mid, est >>>= 1,
                                          expectedModCount);
        }
				
        public void forEachRemaining(Consumer<? super Map.Entry<K,V>> action) {
            int i, hi, mc;
            if (action == null)
                throw new NullPointerException();
            HashMap<K,V> m = map;
            Node<K,V>[] tab = m.table;
            if ((hi = fence) < 0) {
                mc = expectedModCount = m.modCount;
                hi = fence = (tab == null) ? 0 : tab.length;
            }
            else
                mc = expectedModCount;
            if (tab != null && tab.length >= hi &&
                (i = index) >= 0 && (i < (index = hi) || current != null)) {
                Node<K,V> p = current;
                current = null;
                do {
                    if (p == null)
                        p = tab[i++];
                    else {
                        action.accept(p);
                        p = p.next;
                    }
                } while (p != null || i < hi);
                if (m.modCount != mc)
                    throw new ConcurrentModificationException();
            }
        }

        public boolean tryAdvance(Consumer<? super Map.Entry<K,V>> action) {
            int hi;
            if (action == null)
                throw new NullPointerException();
            Node<K,V>[] tab = map.table;
            if (tab != null && tab.length >= (hi = getFence()) && index >= 0) {
                while (current != null || index < hi) {
                    if (current == null)
                        current = tab[index++];
                    else {
                        Node<K,V> e = current;
                        current = current.next;
                        action.accept(e);
                        if (map.modCount != expectedModCount)
                            throw new ConcurrentModificationException();
                        return true;
                    }
                }
            }
            return false;
        }

        public int characteristics() {
            return (fence < 0 || est == map.size ? Spliterator.SIZED : 0) |
                Spliterator.DISTINCT;
        }
    }

    /* ------------------------------------------------------------ */
    // LinkedHashMap support


    /*
		*���а��������������Ϊ
		*��LinkedHashMap���ǣ��������κ��������า�ǡ�
		*�������������ڲ��������ǰ�������
		*��������Ϊfinal�����Կ��Ա�LinkedHashMap, viewʹ��
		���HashSet��
     */

    // ����һ������(����)�ڵ�
    Node<K,V> newNode(int hash, K key, V value, Node<K,V> next) {
        return new Node<>(hash, key, value, next);
    }

    // ���ڴ����ڵ㵽��ͨ�ڵ��ת��
    Node<K,V> replacementNode(Node<K,V> p, Node<K,V> next) {
        return new Node<>(p.hash, p.key, p.value, next);
    }

    // ����һ����bin�ڵ�
    TreeNode<K,V> newTreeNode(int hash, K key, V value, Node<K,V> next) {
        return new TreeNode<>(hash, key, value, next);
    }

    // For treeifyBin
    TreeNode<K,V> replacementTreeNode(Node<K,V> p, Node<K,V> next) {
        return new TreeNode<>(p.hash, p.key, p.value, next);
    }

    // ����Ϊ��ʼĬ��״̬����clone()��readObject()���á�
    void reinitialize() {
        table = null;
        entrySet = null;
        keySet = null;
        values = null;
        modCount = 0;
        threshold = 0;
        size = 0;
    }

    // ����LinkedHashMap�����Ļص�
    void afterNodeAccess(Node<K,V> p) { }
    void afterNodeInsertion(boolean evict) { }
    void afterNodeRemoval(Node<K,V> p) { }

    // Called only from writeObject, to ensure compatible ordering.
    void internalWriteEntries(java.io.ObjectOutputStream s) throws IOException {
        Node<K,V>[] tab;
        if (size > 0 && (tab = table) != null) {
            for (int i = 0; i < tab.length; ++i) {
                for (Node<K,V> e = tab[i]; e != null; e = e.next) {
                    s.writeObject(e.key);
                    s.writeObject(e.value);
                }
            }
        }
    }

    /* ------------------------------------------------------------ */
    // Tree bins
		
		// �̳й�ϵ��LinkedHashMap.Entry<K,V> extends HashMap.Node<K,V>
    static final class TreeNode<K,V> extends LinkedHashMap.Entry<K,V> {
    		// �����������
        TreeNode<K,V> parent;  
        TreeNode<K,V> left;
        TreeNode<K,V> right;
        // ��Node.next�γ�˫������
        TreeNode<K,V> prev;		
        boolean red;
        TreeNode(int hash, K key, V val, Node<K,V> next) {
        		// ����HashMap.Node�Ĺ��췽��
            super(hash, key, val, next);
        }

				// ���ذ����˽ڵ�����ĸ���
        final TreeNode<K,V> root() {
        		// һֱ�����ң������Ҹ�root
            for (TreeNode<K,V> r = this, p;;) {
                if ((p = r.parent) == null)
                    return r;
                r = p;
            }
        }

				// ȷ������������bin�ĵ�һ���ڵ�
				// ��ɺ�root�����������ͷ��㣬Ҳ�����ĸ���Ҳ�������±�ĵ�һ��Ԫ��
				// ���ù�ϵ��void treeify(Node<K,V>[] tab)
				// void removeTreeNode(HashMap<K,V> map, Node<K,V>[] tab, boolean movable)
				// TreeNode<K,V> putTreeVal(HashMap<K,V> map, Node<K,V>[] tab, int h, K k, V v)
        static <K,V> void moveRootToFront(Node<K,V>[] tab, TreeNode<K,V> root) {
            int n;
            // ���ж�
            if (root != null && tab != null && (n = tab.length) > 0) {
            		// �ҵ���
                int index = (n - 1) & root.hash;
                TreeNode<K,V> first = (TreeNode<K,V>)tab[index];
                // ��root��Ϊ��һ�����
                if (root != first) {
                    Node<K,V> rn;
                    tab[index] = root;
                    // root����ǰһ���
                    // void treeifyBin(Node<K,V>[] tab, int hash)������ԭ�е������ϵ
                    TreeNode<K,V> rp = root.prev;
                    // rnΪroot�ĺ�һ���
                    if ((rn = root.next) != null)
                    		// ��root��һ����ǰ���ָ��ָ��root��ǰһ��㣬�Թ�root
                        ((TreeNode<K,V>)rn).prev = rp;
                    // root�ĺ�һ�����ֵ
                    if (rp != null)
                    		// ��root��ǰһ���ĺ�ָ��ָ��root�ĺ�һ��㣬������ߵ�������
                        rp.next = rn;
                    // ���ݽ�����root�Ҳ�����Ӧ����ʱ��first == null
                    if (first != null)
                        first.prev = root;
                    // ������ܳ���root�ǹ�����㣬ֻ���Լ�
                    root.next = first;
                    root.prev = null;
                }
                // assert [boolean ���ʽ]
								// ���[boolean���ʽ]Ϊtrue����������ִ�С�
								// ���Ϊfalse��������׳�AssertionError������ִֹ�С�
                assert checkInvariants(root);
            }
        }

				// ʹ�ø�����ɢ�кͼ����ҴӸ�p��ʼ�Ľڵ㡣kc�����ڵ�һ��ʹ��ʱ����comparableClassFor(key)�Ƚϼ���
				// ���ù�ϵ��getTreeNode(int h, Object k)  
				// putTreeVal(HashMap<K,V> map, Node<K,V>[] tab, int h, K k, V v)
        final TreeNode<K,V> find(int h, Object k, Class<?> kc) {
            TreeNode<K,V> p = this;
            do {
                int ph, dir; K pk;
                TreeNode<K,V> pl = p.left, pr = p.right, q;
                // ��ǰ���p��hashֵ����Ŀ�����hashֵ������������
                if ((ph = p.hash) > h)
                    p = pl;
                // ��ǰ���p��hashֵС��Ŀ�����hashֵ������������
                else if (ph < h)
                    p = pr;
               	// �����ǰ����key����Ŀ�����key���ҵ�Ŀ�귵��
                else if ((pk = p.key) == k || (k != null && k.equals(pk)))
                    return p;
                // ������˵����p.hash == h����key����ȣ��޷�ȷ�ϸ����ı��������
                else if (pl == null)
                		// ������Ϊ�գ���������
                    p = pr;
                else if (pr == null)
                		// ������Ϊ�գ���������
                    p = pl;
                // �ж��û��Ƿ��ṩ���ҵ����ݣ�comparable��ʵ����
                else if ((kc != null ||
                					// �жϸ�Class�Ƿ�Ϊcomparable��ʵ���࣬�ҷ��Ͳ���Ϊʵ��������
                          (kc = comparableClassFor(k)) != null) &&
                         // ʵ�ʣ�(Comparable)k).compareTo(pk) �����û��Ľӿ�ʵ�ַ�����ȷ������
                         (dir = compareComparables(kc, k, pk)) != 0)
                    p = (dir < 0) ? pl : pr;
                // ������˵�����û�δ�ṩ�������ݻ��ṩ�Ĳ���������Ȼ�޷�ȷ�ϲ��ҷ���
                // �޷��������£�ֻ�����߶��飬�ȵݹ�������������ҵ��򷵻�
                else if ((q = pr.find(h, k, kc)) != null)
                    return q;
                // ������δ�ҵ�����������
                else
                    p = pl;
            } while (p != null);
            return null;
        }

				// ����find()���Ҹ��ڵ㡣
        final TreeNode<K,V> getTreeNode(int h, Object k) {
        		// parent != null ��ǰ���Ǹ��ڵ㣬����Ӹ��ڵ㿪ʼ����
            return ((parent != null) ? root() : this).find(h, k, null);
        }

				// ���ڵ�������ͨ���Ƚ�hashcode
				// ���û���д��hashCode()��������ɢ�У�hashmapΪ�Լ�����һ����·��ʹ��ԭʼhashCode()����
        static int tieBreakOrder(Object a, Object b) {
            int d;
            // ��������Ϊnull����a��b�����Ͳ�һ�£���dΪ��������
            if (a == null || b == null ||
                (d = a.getClass().getName().
                 compareTo(b.getClass().getName())) == 0)
                // a.hashcode <= b.hashcode -> d = -1
                // a.hashcode >  b.hashcode -> d = 1
                d = (System.identityHashCode(a) <= System.identityHashCode(b) ?
                     -1 : 1);
            return d;
        }
	     /**
				*���������������ͬ�Ĺ�ϣ�룬����Ĭ�Ϸ���hashCode()���أ�
				*�����Ƿ񸲸Ǹ����������hashCode()�������õĹ�ϣ��Ϊ�㡣
								
				public static native int identityHashCode(Object x);
	     */

				// ����-->��
        final void treeify(Node<K,V>[] tab) {
            TreeNode<K,V> root = null;
            // thisΪ����ͷ���
            for (TreeNode<K,V> x = this, next; x != null; x = next) {
            		// next�Ǳ�����������ϵ���һ���
                next = (TreeNode<K,V>)x.next;
                x.left = x.right = null;
                // ��һ������ִ�����������
                if (root == null) {
                		// ��ǰ���ĸ�����ÿգ���ǰ��㽫��Ϊroot
                    x.parent = null;
                    // ���ڵ�����Ǻ�ɫ��
                    x.red = false;
                    root = x;
                }
                // ���������ִ��
                else {
                    K k = x.key;
                    int h = x.hash;
                    Class<?> kc = null;
                    // ���������ҵ��ò����λ�ã���find()��������
                    for (TreeNode<K,V> p = root;;) {
                        int dir, ph;
                        K pk = p.key;
                        // p.hash > h : ������
                        if ((ph = p.hash) > h)
                            dir = -1;
                        // p.hash < h : ������
                        else if (ph < h)
                            dir = 1;
                        // �������hashֵ��ͬ����key��ͬ(key�϶���ͬ)
                        else if ((kc == null &&
                        					// kcδʵ��comparable�ӿڻ�δ���淶ʵ��
                                  (kc = comparableClassFor(k)) == null) ||
                                 // pk == null || pk.getClass() != kc (kc = null)
                                 (dir = compareComparables(kc, k, pk)) == 0)
                            // ������ԭʼ��hashCode()�������бȽ�
                            dir = tieBreakOrder(k, pk);

                        TreeNode<K,V> xp = p;
                        // ͨ��dir��ѡ�����Һ��ӣ������ѡ�ӽ��Ϊnull����������һ�����
                        if ((p = (dir <= 0) ? p.left : p.right) == null) {
                            x.parent = xp;
                            if (dir <= 0)
                                xp.left = x;
                            else
                                xp.right = x;
                            // ÿ��һ���µĽ�㶼Ҫ������ƽ�����
                            root = balanceInsertion(root, x);
                            break;
                        }
                    }
                }
            }
            // ��ת����ɺ󣬽��������Ϊ�����±�Ԫ��
            moveRootToFront(tab, root);
        }

				// ��-->����
        final Node<K,V> untreeify(HashMap<K,V> map) {
            Node<K,V> hd = null, tl = null;
            for (Node<K,V> q = this; q != null; q = q.next) {
            		// return new Node<>(p.hash, p.key, p.value, next);
                Node<K,V> p = map.replacementNode(q, null);
                // β׷����������
                if (tl == null)
                    hd = p;
                else
                    tl.next = p;
                tl = p;
            }
            return hd;
        }

				// ���汾��putVal��
        final TreeNode<K,V> putTreeVal(HashMap<K,V> map, Node<K,V>[] tab,
                                       int h, K k, V v) {
            Class<?> kc = null;
            boolean searched = false;
            // �ҵ����ڵ�
            TreeNode<K,V> root = (parent != null) ? root() : this;
            for (TreeNode<K,V> p = root;;) {
                int dir, ph;
                K pk;
                
                //ǰ��������ж���find()һ��
                if ((ph = p.hash) > h)
                    dir = -1;
                else if (ph < h)
                    dir = 1;
                // ������ڸý�㣬ֱ�ӷ��أ��Ƿ��޸�value���ϲ����
                else if ((pk = p.key) == k || (k != null && k.equals(pk)))
                    return p;
                // ������˵����p.hash == h����key����ȣ��޷�ȷ�ϸ����ı��������    
                else if ((kc == null &&
                          (kc = comparableClassFor(k)) == null) ||
                         // pk == null || pk.getClass() != kc (kc = null)
                         (dir = compareComparables(kc, k, pk)) == 0) {
                    // ���أ�ÿ�ε���putTreeVal()��������һ��
                    // �������������˵���û����ǵ�hashCode()�ܲhashmapֻ����һ��
                    if (!searched) {
                        TreeNode<K,V> q, ch;
                        searched = true;
                        // �ֱ������������
                        if (((ch = p.left) != null &&
                             (q = ch.find(h, k, kc)) != null) ||
                            ((ch = p.right) != null &&
                             (q = ch.find(h, k, kc)) != null))
                            return q;
                    }
                    dir = tieBreakOrder(k, pk);
                }

                TreeNode<K,V> xp = p;
                // ���ҵ���ĩ��㣬������
                if ((p = (dir <= 0) ? p.left : p.right) == null) {
                		// xpn �� ��ǰĿ�����ӽ�����һ��
                    Node<K,V> xpn = xp.next;
                    // ��xpn�������½��ĺ��棬�൱�ڲ�����һ�����
                    TreeNode<K,V> x = map.newTreeNode(h, k, v, xpn);
                    if (dir <= 0)
                        xp.left = x;
                    else
                        xp.right = x;
                    // �����½ڵ�
                    xp.next = x;
                    // xpn ��Ŀ����˫������
                    x.parent = x.prev = xp;
                    // xpn ���������˫������
                    if (xpn != null)
                        ((TreeNode<K,V>)xpn).prev = x;
                        
                    // ����ƽ��󣬸��ڵ���ܻ��
                    moveRootToFront(tab, balanceInsertion(root, x));
                    return null;
                }
            }
        }

        /**
					*ɾ��ָ���ڵ㣬�ýڵ�����ڵ���֮ǰ���֡���ȵ��͵ĺ��ɾ����������ң���Ϊ����
					*������Ҷ�ӽ����ڲ��ڵ�����ݱ��ɷ��ʵġ�next��ָ��̶��ĺ��ָ������ر�����
					�������ǽ�������ϵ�������ǰ���ڵ�̫�٣�bin��ת����һ����ͨ��bin��(���Դ���
					�������Ľṹ����2��6���ڵ�֮��)��
         */
        final void removeTreeNode(HashMap<K,V> map, Node<K,V>[] tab,
                                  boolean movable) {
            // section 1��ͨ��prev��nextɾ����ǰ�����н��
            int n;
            // ���ж�
            if (tab == null || (n = tab.length) == 0)
                return;
            // ȡ�±�
            int index = (n - 1) & hash;
            // ȡ�����
            TreeNode<K,V> first = (TreeNode<K,V>)tab[index], root = first, rl;
            // succ: ɾ��������һ����pred��ɾ��������һ��
            TreeNode<K,V> succ = (TreeNode<K,V>)next, pred = prev;
            // û����һ����㣬��ôֱ������ɾ�����
            if (pred == null)
                tab[index] = first = succ;
            // ����һ��㣬����������ɾ���ý��
            else
                pred.next = succ;
            // ����һ��㣬��������  
            if (succ != null)
                succ.prev = pred;
            // 1����ǰ�����±�û���κ�����
            // 2��ֻ��һ����㣺���ڵ㣬����ɾ����㣨�����ܣ��������ֻ��һ����㣬�Ͳ�������
            if (first == null)
                return;
            // section 2�����ڵ�����С��7ʱת�����������ʽ�洢    
            // �ҵ����ڵ㣬ֻҪ�̰߳�ȫ������root.parent != nullһ��������
            if (root.parent != null)
                root = root.root();
            // root.right -> ��ֻ��������㣬��������ת
            // 2-6�������� 
            if (root == null || root.right == null ||
                (rl = root.left) == null || rl.left == null) {
               	// ɾ����������������Ѿ�ɾ���ˣ�����ֱ��ת��Ϊ����һ������
                tab[index] = first.untreeify(map);
                return;
            }
            // section 3���жϵ�ǰ���ڵ����
            TreeNode<K,V> p = this, pl = left, pr = right, replacement;
            if (pl != null && pr != null) {
            		// s ��ɾ���������ӽ��
                TreeNode<K,V> s = pr, sl;
                // �����������е����㣬ֱ�����һ������
                while ((sl = s.left) != null)
                    s = sl;
                boolean c = s.red; s.red = p.red; p.red = c; // swap colors ������ɫ
                TreeNode<K,V> sr = s.right;
                TreeNode<K,V> pp = p.parent;
                if (s == pr) { // p was s's direct parent p��s��ֱϵ��ĸ
                    p.parent = s;
                    s.right = p;
                }
                else {
                    TreeNode<K,V> sp = s.parent;
                    if ((p.parent = sp) != null) {
                        if (s == sp.left)
                            sp.left = p;
                        else
                            sp.right = p;
                    }
                    if ((s.right = pr) != null)
                        pr.parent = s;
                }
                p.left = null;
                if ((p.right = sr) != null)
                    sr.parent = p;
                if ((s.left = pl) != null)
                    pl.parent = s;
                if ((s.parent = pp) == null)
                    root = s;
                else if (p == pp.left)
                    pp.left = s;
                else
                    pp.right = s;
                if (sr != null)
                    replacement = sr;
                else
                    replacement = p;
            }
            else if (pl != null)
                replacement = pl;
            else if (pr != null)
                replacement = pr;
            else
                replacement = p;
            if (replacement != p) {
                TreeNode<K,V> pp = replacement.parent = p.parent;
                if (pp == null)
                    root = replacement;
                else if (p == pp.left)
                    pp.left = replacement;
                else
                    pp.right = replacement;
                p.left = p.right = p.parent = null;
            }

            TreeNode<K,V> r = p.red ? root : balanceDeletion(root, replacement);

            if (replacement == p) {  // detach
                TreeNode<K,V> pp = p.parent;
                p.parent = null;
                if (pp != null) {
                    if (p == pp.left)
                        pp.left = null;
                    else if (p == pp.right)
                        pp.right = null;
                }
            }
            if (movable)
                moveRootToFront(tab, r);
        }

        /**
         * Splits nodes in a tree bin into lower and upper tree bins,
         * or untreeifies if now too small. Called only from resize;
         * see above discussion about split bits and indices.
         *
         * @param map the map
         * @param tab the table for recording bin heads
         * @param index the index of the table being split
         * @param bit the bit of hash to split on
         */
        final void split(HashMap<K,V> map, Node<K,V>[] tab, int index, int bit) {
            TreeNode<K,V> b = this;
            // Relink into lo and hi lists, preserving order
            TreeNode<K,V> loHead = null, loTail = null;
            TreeNode<K,V> hiHead = null, hiTail = null;
            int lc = 0, hc = 0;
            for (TreeNode<K,V> e = b, next; e != null; e = next) {
                next = (TreeNode<K,V>)e.next;
                e.next = null;
                if ((e.hash & bit) == 0) {
                    if ((e.prev = loTail) == null)
                        loHead = e;
                    else
                        loTail.next = e;
                    loTail = e;
                    ++lc;
                }
                else {
                    if ((e.prev = hiTail) == null)
                        hiHead = e;
                    else
                        hiTail.next = e;
                    hiTail = e;
                    ++hc;
                }
            }

            if (loHead != null) {
                if (lc <= UNTREEIFY_THRESHOLD)
                    tab[index] = loHead.untreeify(map);
                else {
                    tab[index] = loHead;
                    if (hiHead != null) // (else is already treeified)
                        loHead.treeify(tab);
                }
            }
            if (hiHead != null) {
                if (hc <= UNTREEIFY_THRESHOLD)
                    tab[index + bit] = hiHead.untreeify(map);
                else {
                    tab[index + bit] = hiHead;
                    if (loHead != null)
                        hiHead.treeify(tab);
                }
            }
        }

        /* ------------------------------------------------------------ */
        // �����������ȫ���ı���CLR
					
				// ����
        static <K,V> TreeNode<K,V> rotateLeft(TreeNode<K,V> root,
                                              TreeNode<K,V> p) {
            TreeNode<K,V> r, pp, rl;
            if (p != null && (r = p.right) != null) {
                if ((rl = p.right = r.left) != null)
                    rl.parent = p;
                if ((pp = r.parent = p.parent) == null)
                    (root = r).red = false;
                else if (pp.left == p)
                    pp.left = r;
                else
                    pp.right = r;
                r.left = p;
                p.parent = r;
            }
            return root;
        }

        static <K,V> TreeNode<K,V> rotateRight(TreeNode<K,V> root,
                                               TreeNode<K,V> p) {
            TreeNode<K,V> l, pp, lr;
            if (p != null && (l = p.left) != null) {
                if ((lr = p.left = l.right) != null)
                    lr.parent = p;
                if ((pp = l.parent = p.parent) == null)
                    (root = l).red = false;
                else if (pp.right == p)
                    pp.right = l;
                else
                    pp.left = l;
                l.right = p;
                p.parent = l;
            }
            return root;
        }

        static <K,V> TreeNode<K,V> balanceInsertion(TreeNode<K,V> root,
                                                    TreeNode<K,V> x) {
            x.red = true;
            for (TreeNode<K,V> xp, xpp, xppl, xppr;;) {
                if ((xp = x.parent) == null) {
                    x.red = false;
                    return x;
                }
                else if (!xp.red || (xpp = xp.parent) == null)
                    return root;
                if (xp == (xppl = xpp.left)) {
                    if ((xppr = xpp.right) != null && xppr.red) {
                        xppr.red = false;
                        xp.red = false;
                        xpp.red = true;
                        x = xpp;
                    }
                    else {
                        if (x == xp.right) {
                            root = rotateLeft(root, x = xp);
                            xpp = (xp = x.parent) == null ? null : xp.parent;
                        }
                        if (xp != null) {
                            xp.red = false;
                            if (xpp != null) {
                                xpp.red = true;
                                root = rotateRight(root, xpp);
                            }
                        }
                    }
                }
                else {
                    if (xppl != null && xppl.red) {
                        xppl.red = false;
                        xp.red = false;
                        xpp.red = true;
                        x = xpp;
                    }
                    else {
                        if (x == xp.left) {
                            root = rotateRight(root, x = xp);
                            xpp = (xp = x.parent) == null ? null : xp.parent;
                        }
                        if (xp != null) {
                            xp.red = false;
                            if (xpp != null) {
                                xpp.red = true;
                                root = rotateLeft(root, xpp);
                            }
                        }
                    }
                }
            }
        }

        static <K,V> TreeNode<K,V> balanceDeletion(TreeNode<K,V> root,
                                                   TreeNode<K,V> x) {
            for (TreeNode<K,V> xp, xpl, xpr;;)  {
                if (x == null || x == root)
                    return root;
                else if ((xp = x.parent) == null) {
                    x.red = false;
                    return x;
                }
                else if (x.red) {
                    x.red = false;
                    return root;
                }
                else if ((xpl = xp.left) == x) {
                    if ((xpr = xp.right) != null && xpr.red) {
                        xpr.red = false;
                        xp.red = true;
                        root = rotateLeft(root, xp);
                        xpr = (xp = x.parent) == null ? null : xp.right;
                    }
                    if (xpr == null)
                        x = xp;
                    else {
                        TreeNode<K,V> sl = xpr.left, sr = xpr.right;
                        if ((sr == null || !sr.red) &&
                            (sl == null || !sl.red)) {
                            xpr.red = true;
                            x = xp;
                        }
                        else {
                            if (sr == null || !sr.red) {
                                if (sl != null)
                                    sl.red = false;
                                xpr.red = true;
                                root = rotateRight(root, xpr);
                                xpr = (xp = x.parent) == null ?
                                    null : xp.right;
                            }
                            if (xpr != null) {
                                xpr.red = (xp == null) ? false : xp.red;
                                if ((sr = xpr.right) != null)
                                    sr.red = false;
                            }
                            if (xp != null) {
                                xp.red = false;
                                root = rotateLeft(root, xp);
                            }
                            x = root;
                        }
                    }
                }
                else { // symmetric
                    if (xpl != null && xpl.red) {
                        xpl.red = false;
                        xp.red = true;
                        root = rotateRight(root, xp);
                        xpl = (xp = x.parent) == null ? null : xp.left;
                    }
                    if (xpl == null)
                        x = xp;
                    else {
                        TreeNode<K,V> sl = xpl.left, sr = xpl.right;
                        if ((sl == null || !sl.red) &&
                            (sr == null || !sr.red)) {
                            xpl.red = true;
                            x = xp;
                        }
                        else {
                            if (sl == null || !sl.red) {
                                if (sr != null)
                                    sr.red = false;
                                xpl.red = true;
                                root = rotateLeft(root, xpl);
                                xpl = (xp = x.parent) == null ?
                                    null : xp.left;
                            }
                            if (xpl != null) {
                                xpl.red = (xp == null) ? false : xp.red;
                                if ((sl = xpl.left) != null)
                                    sl.red = false;
                            }
                            if (xp != null) {
                                xp.red = false;
                                root = rotateRight(root, xp);
                            }
                            x = root;
                        }
                    }
                }
            }
        }

				// �ݹ鲻������飬����t = root
        static <K,V> boolean checkInvariants(TreeNode<K,V> t) {
        		// t = root ʱ��t.parent = null
            TreeNode<K,V> tp = t.parent, tl = t.left, tr = t.right,
                tb = t.prev, tn = (TreeNode<K,V>)t.next;
            // t��ǰһ��㲻Ϊnull��ǰ���ϵ��ͳһ
            if (tb != null && tb.next != t)
                return false;
            // t�ĺ�һ��㲻Ϊnull��ǰ���ϵ��ͳһ
            if (tn != null && tn.prev != t)
                return false;
            // t.parent��Ϊnull��parent�������ӽ�㶼��Ϊt������Ϊ�ӽڵ������һ��
            if (tp != null && t != tp.left && t != tp.right)
                return false;
            // ���Ӳ�Ϊnull�����ӵĸ���㲻��t�������ӵ�hashֵ���ڸ����
            if (tl != null && (tl.parent != t || tl.hash > t.hash))
                return false;
            // �Һ��Ӳ�Ϊnull���Һ��ӵĸ���㲻��t�����Һ��ӵ�hashֵС�ڸ����
            if (tr != null && (tr.parent != t || tr.hash < t.hash))
                return false;
            // tΪ���㣬�����Һ��Ӷ�Ϊ����
            if (t.red && tl != null && tl.red && tr != null && tr.red)
                return false;
            // �������ӣ�ѭ��������ȷ��
            if (tl != null && !checkInvariants(tl))
                return false;
            // �����Һ��ӣ�ѭ��������ȷ��
            if (tr != null && !checkInvariants(tr))
                return false;
            return true;
        }
    }

}