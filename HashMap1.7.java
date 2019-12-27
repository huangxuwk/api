package java.util;
import java.io.*;

/**
*���ڹ�ϣ��ʵ�ֵ�Map�ӿڡ���
*ʵ���ṩ�����п�ѡ��map���������֤
*nullֵ��null����(< tt > HashMap < / tt >
*class�����൱��Hashtable��ֻ������
*��ͬ��������Ϊ�ա�)����಻�ܱ�֤
*��ͼ�Ĵ���;�ر��ǣ������ܱ�֤����
*������ʱ�䱣�ֲ��䡣
 *
*��ʵ��Ϊbasic�ṩ�˳���ʱ������
*����(get and put)�������ϣ����
*��Ԫ���ʵ��ط�ɢ������Ͱ�С�����
*�ռ���ͼ����ʱ���롰������������
* HashMapʵ��(Ͱ������)�������Ĵ�С(����)
*��ֵӳ��)����ˣ���Ҫ���ó�ʼֵ�Ƿǳ���Ҫ��
*����������ܹ���(�������ӹ���)
*��Ҫ��
 *
*һ��HashMap��ʵ��������Ӱ�����Ĳ���
*����:��ʼ������ ������������
* capacity�ǹ�ϣ����Ͱ��������Ϊ��ʼͰ��
*�������Ǵ�����ϣ��ʱ����������
* load factor�Ƕ������ϣ������ȵĶ���
*�����������Զ�����֮ǰ����
*��ϣ���е�����˸������Ӻ͵ĳ˻�
*��ǰ��������ϣ��Ϊrehashed(��in)
 *
*��Ϊһ��ͨ�ù���Ĭ�ϵĸ�������(.75)�ṩ��һ���ܺõ�Ȩ��
*ʱ��Ϳռ�ɱ����ϸߵ�ֵ����ٿռ俪��
*�����Ӳ��ҳɱ�(��ӳ�ڴ����������
* HashMap�࣬����get��put)����
*Ӧ���õ�ͼ��Ԥ�ڵ���Ŀ�����为������
*���������ʼ����ʱ���ǣ���ʹ
*�ع�ϣ�����������������ʼ��������
*���������Ŀ�����Ը�������
*
*������ӳ��Ҫ�洢��HashMapʵ���У�
*����һ���㹻�������������ӳ�䵽
*������ִ���Զ��ع�ϣ����Ч�ش洢
*��Ҫ���ӱ�
 *
*ע�����ʵ�ֲ���ͬ���ġ�
*�������߳�ͬʱ����һ��ɢ��ӳ�䣬������һ��
*�߳��ڽṹ���޸�ӳ�䣬������ be
*�ⲿͬ�����ṹ�޸���ָ�κβ���
*��ӻ�ɾ��һ������ӳ��;ֻ�Ǹı���ֵ
*��ʵ���Ѱ����ļ������Ĳ���
*�ṹ�޸ġ�)��ͨ������
*ͬ��һЩ������Ȼ��װӳ�䡣
 *
*��������ڴ��������Ӧʹ��
* {@link Collections#synchronizedMap Collections.synchronizedMap}
*������������ڴ���ʱ��ɣ��Է�ֹ����
*��ӳ��ķ�ͬ������:
 *
**���������С�������ͼ���������صĵ�����
**��fail-fast:�����֮����κ�ʱ���ӳ������˽ṹ�޸�
*������������������ͨ���������Լ��ķ�ʽ
* ɾ�����������������׳�һ��
* {@link ConcurrentModificationException}����ˣ���Բ���
*�޸�ʱ������������ٶ��ɾ���ʧ�ܣ��������з���
*�����⡢��ȷ������Ϊ
*δ����
 *
*ע�⣬���ܱ�֤�������Ĺ���-������Ϊ
*һ����˵�����ǲ����������κ��ϸ�ı�֤
*���ڲ�ͬ���Ĳ����޸ġ�����ʧ�ܵ�����
*�����Ŭ���׳�ConcurrentModificationException��
*��ˣ�дһ�������ڴ˵ĳ����Ǵ����
*��ȷ���쳣:�������Ŀ���ʧЧ��Ϊ
*Ӧ��ֻ���ڼ��bug��
 *
 */

public class HashMap<K,V>
    extends AbstractMap<K,V>
    implements Map<K,V>, Cloneable, Serializable
{

    /**
     * Ĭ�ϵĳ�ʼ����-������2���ݣ����ٹ�ϣ��ײ����ߴ�������Ч��
     * ���ٹ�ϣ��ײ��Ϊ�������ݾ��ȷֲ������о���hash%length
     * ��ߴ�������Ч�ʣ���hash%length�Ż�Ϊhash&(length - 1)
     * ��Ϊ�����±�ʱʹ�õ��� hash & (length - 1)
     * hash%length == hash&(length-1) ��ǰ����length��2��n�η�
     */
    static final int DEFAULT_INITIAL_CAPACITY = 1 << 4; // aka 16

    /**
		*�����ʽָ���˸��ߵ�ֵ����ʹ���������
		*���κ�һ���������Ĺ��캯����
		*������2����<= 1<<30��
     */
    static final int MAXIMUM_CAPACITY = 1 << 30;

    // �ڹ��캯����û��ָ��ʱʹ�õĸ������ӡ�
    static final float DEFAULT_LOAD_FACTOR = 0.75f;

    // һ���յı�ʵ�����ڱ�����ʱ����
    static final Entry<?,?>[] EMPTY_TABLE = {};

    /**
     * ��������Ҫ������С������һ����2���ݡ�
     */
    transient Entry<K,V>[] table = (Entry<K,V>[]) EMPTY_TABLE;

    // ��ӳ���а����ļ�-ֵӳ�����Ŀ��
    transient int size;

		//���table == EMPTY_TABLE����ô���ǳ�ʼ����
		//��������ʱ������
    int threshold;

    // ��ϣ��ļ������ӡ�
    final float loadFactor;

    /**
			*��HashMap�ڽṹ���޸ĵĴ���
			*�ṹ�޸���ָ�ı�ӳ�������
			* HashMap����������ʽ�޸����ڲ��ṹ(���磬
			*�ظ�)�����ֶ����ڶԼ�����ͼ���ɵ�����
			*HashMapʧ�ܵúܿ졣(��ConcurrentModificationException)��
     */
     // �޸Ĵ����������������ݣ��������޸�value����ɾ�����ݣ��������modCount++
    transient int modCount;

    /**
			*ӳ��������Ĭ����ֵ�����ڸ���ֵ�Ŀ�ѡ��ϣֵ
			*�����ַ���������ѡ�Ĺ�ϣ������
			*��ײ��������ϣ������ַ�������
			*ͨ������ϵͳ���Կ��Ը��Ǵ�ֵ
			* {@code jdk.map.althashing.threshold}������ֵΪ{@code 1}
			*ǿ�����κ�ʱ��ʹ�ÿ�ѡ��ϣ
			* {@code -1}ֵȷ���Ӳ�ʹ�����ɢ�С�
     */
    static final int ALTERNATIVE_HASHING_THRESHOLD_DEFAULT = Integer.MAX_VALUE;

    /**
     * ������VM����֮ǰ�޷���ʼ����ֵ��
     */
    private static class Holder {

        /**
         * ����������������ʱ�����л���ʹ�ÿ�ѡ��ϣ��
         */
        static final int ALTERNATIVE_HASHING_THRESHOLD;

        static {
        	// ���Խ��Ϊnull������ALTERNATIVE_HASHING_THRESHOLD=ALTERNATIVE_HASHING_THRESHOLD_DEFAULT
            String altThreshold = java.security.AccessController.doPrivileged(
                new sun.security.action.GetPropertyAction(
                    "jdk.map.althashing.threshold"));

            int threshold;
            try {
                threshold = (null != altThreshold)
                        ? Integer.parseInt(altThreshold)
                        : ALTERNATIVE_HASHING_THRESHOLD_DEFAULT;

                // disable alternative hashing if -1
                if (threshold == -1) {
                    threshold = Integer.MAX_VALUE;
                }

                if (threshold < 0) {
                    throw new IllegalArgumentException("value must be positive integer.");
                }
            } catch(IllegalArgumentException failed) {
                throw new Error("Illegal value for 'jdk.map.althashing.threshold'", failed);
            }

            ALTERNATIVE_HASHING_THRESHOLD = threshold;
        }
    }

    /**
		*Ӧ���ڴ�ʵ����������ֵ
		*���Ĺ�ϣ�룬ʹ��ϣ��ͻ�����ҵ������0
		*�������ɢ�С�
     */
     
    // �²⣺��hashmap����������int�����ֵʱ��ɢ�ж���Ҫͨ��hashSeed�ı� 
    transient int hashSeed = 0;

    /**
		*ʹ��ָ���ĳ�ʼֵ����һ���յ�HashMap
		*�����͸������ء�
     */
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
        // �ٽ�ֵ��ʱ����ֵΪָ���������������㲢�ı�
        threshold = initialCapacity;
        // ��linkedhashmap��д
        init();
    }

		/**
		*ʹ��ָ���ĳ�ʼֵ����һ���յ�HashMap
		*������Ĭ�ϸ�������(0.75)��
     */
    public HashMap(int initialCapacity) {
        this(initialCapacity, DEFAULT_LOAD_FACTOR);
    }

    /**
		*ʹ��Ĭ�ϳ�ʼ��������һ���յ�HashMap
		*(16)��Ĭ�ϵĸ�������(0.75)��
     */
    public HashMap() {
        this(DEFAULT_INITIAL_CAPACITY, DEFAULT_LOAD_FACTOR);
    }

    /**
		*����ͬӳ�乹��һ���µ�HashMap
		*ָ����ͼ������HashMap
		*Ĭ�ϸ�������(0.75)���㹻�ĳ�ʼ����
		*����ָ��Map�е�ӳ�䡣
     */
    public HashMap(Map<? extends K, ? extends V> m) {
    		// �ж�Capacity�Ƿ�С��Ĭ�ϵ�16�����ǣ���ѡ��Ĭ�ϵ�
        this(Math.max((int) (m.size() / DEFAULT_LOAD_FACTOR) + 1,
                      DEFAULT_INITIAL_CAPACITY), DEFAULT_LOAD_FACTOR);
        // ����Entry[]����
        inflateTable(threshold);
				// �������еļ�ֵ�ԣ����ӵ��µ�map��
        putAllForCreate(m);
    }
		
		
    private static int roundUpToPowerOf2(int number) {
        return number >= MAXIMUM_CAPACITY
                ? MAXIMUM_CAPACITY  // ��number - 1���ٽ������numberΪ2��n�η�
                : (number > 1) ? Integer.highestOneBit((number - 1) << 1) : 1;
    }

    /**
     * ��������
     */
    private void inflateTable(int toSize) {
        // Find a power of 2 >= toSize
        int capacity = roundUpToPowerOf2(toSize);

				// �ٽ�ֵ���ֻ��ȡMAXIMUM_CAPACITY��integer.maxValue
        threshold = (int) Math.min(capacity * loadFactor, MAXIMUM_CAPACITY + 1);
        table = new Entry[capacity];
        // ����hashSeed��������MAXIMUM_CAPACITY�ͻ�һֱ����Ϊ0
        initHashSeedAsNeeded(capacity);
    }

    // internal utilities

    /**
		*����ĳ�ʼ�����ӡ��������������
		*�����й��캯����α���캯��(��¡��readObject)
		* HashMap�ѳ�ʼ���������κ����ѳ�ʼ��֮ǰ
		*�����롣(���û�����������readObject��
		*��Ҫ��ȷ������֪ʶ��)
     */
    void init() {
    }

    /**
			*��ʼ����ϣ����ֵ�����ǽ���ʼ���Ƴٵ�
			**�������Ҫ����
			*ͨ�����ض�Ϊfalse
     */
    final boolean initHashSeedAsNeeded(int capacity) {
    		// false
        boolean currentAltHashing = hashSeed != 0;
        // true && false = false
        boolean useAltHashing = sun.misc.VM.isBooted() &&
                (capacity >= Holder.ALTERNATIVE_HASHING_THRESHOLD);
        // false ^ false = false
        boolean switching = currentAltHashing ^ useAltHashing;
        if (switching) {
            hashSeed = useAltHashing
                ? sun.misc.Hashing.randomHashSeed(this)
                : 0;
        }
        return switching;
    }

    /**
		*���������ϣ�룬����һ�����ӵĹ�ϣ����Ӧ����
		*�����ϣ�������Է�ֹ�������Ĺ�ϣ����������
		*�ؼ�����ΪHashMapʹ�õ���2���ݳ��ȹ�ϣ��
		*��������������ڲ���Ĺ�ϣ���ͻ
		*���͵ı��ء�ע��:�ռ�����ӳ�䵽ɢ��0���������Ϊ0��
     */
    final int hash(Object k) {
        int h = hashSeed;
        if (0 != h && k instanceof String) {
            return sun.misc.Hashing.stringHash32((String) k);
        }

        h ^= k.hashCode();

				//�������ȷ��hashcodeֻ���
				//ÿһλ�ĳ�����������һ���н�
				//��ͻ��(Ĭ�ϸ�������ԼΪ8)��
        h ^= (h >>> 20) ^ (h >>> 12);
        return h ^ (h >>> 7) ^ (h >>> 4);
    }

    static int indexFor(int h, int length) {
        // ����Integer.bitCount(length) == 1:�����ȱ����Ƿ�0��2�η���;
        return h & (length-1);
    }

		// ���ش�ӳ���еļ�ֵӳ�����Ŀ��
    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public V get(Object key) {
    		// �±�Ϊ0�����鱣��keyΪnull������
        if (key == null)
            return getForNullKey();
        // ͨ��key����entry������Ϊͨ��key.hash����
        Entry<K,V> entry = getEntry(key);

        return null == entry ? null : entry.getValue();
    }

    /**
		*ж�ذ汾��get()�����ҿռ����ռ�ӳ��
		*����Ϊ0������������ָ�ɵ����ķ���
		*Ϊ�˱��������������
		*����(get��put)������������ϲ�
		*���ˡ�
     */
    private V getForNullKey() {
        if (size == 0) {
            return null;
        }
        // ��Ϊ��ͨ��e.key == null���Ƚϣ����Դ��±���ౣ��һ��key = null������
        for (Entry<K,V> e = table[0]; e != null; e = e.next) {
            if (e.key == null)
                return e.value;
        }
        return null;
    }

    public boolean containsKey(Object key) {
        return getEntry(key) != null;
    }

    /**
		*��������ָ������������
		* HashMap�����HashMap������ӳ�䣬�򷵻�null
		*Կ�ס�
     */
    final Entry<K,V> getEntry(Object key) {
        if (size == 0) {
            return null;
        }
				// ����hashֵ����λ�����±�
        int hash = (key == null) ? 0 : hash(key);
        for (Entry<K,V> e = table[indexFor(hash, table.length)];
             e != null;
             e = e.next) {
            Object k;
            // key������ͬһ����Ҳ�����ǲ�ͬ�Ķ���ֻҪequals�Ƚϳ�������
            if (e.hash == hash &&
                ((k = e.key) == key || (key != null && key.equals(k))))
                return e;
        }
        return null;
    }

		// ��ָ��ֵ���ӳ���е�ָ�������������֮ǰ��ӳ�����һ������ӳ�䣬��Ϊ�ɵ�ֵ���滻��
    public V put(K key, V value) {
    		// ������ģʽ
        if (table == EMPTY_TABLE) {
            inflateTable(threshold);
        }
        // hashmapֻ�ᱣ��һ��keyΪnull������
        if (key == null)
            return putForNullKey(value);
        int hash = hash(key);
        int i = indexFor(hash, table.length);
        // ����ҵ��о�ֵ�����滻�����ؾ�ֵ
        for (Entry<K,V> e = table[i]; e != null; e = e.next) {
            Object k;
            if (e.hash == hash && ((k = e.key) == key || key.equals(k))) {
                V oldValue = e.value;
                e.value = value;
                e.recordAccess(this);
                return oldValue;
            }
        }
				
        modCount++;
        // ������������һ���µ�����
        addEntry(hash, key, value, i);
        // �޾�ֵ����null
        return null;
    }

    /**
     *ж�ذ汾��putΪ�ռ�
     */
    private V putForNullKey(V value) {
        for (Entry<K,V> e = table[0]; e != null; e = e.next) {
            if (e.key == null) {
                V oldValue = e.value;
                e.value = value;
                e.recordAccess(this);
                return oldValue;
            }
        }
        modCount++;
        // hash : 0 -> index = 0
        addEntry(0, null, value, 0);
        return null;
    }

    /**
		*ʹ�������������put by���캯����
		*α���캯��(��¡��readObject)�������������Ĵ�С��
		*��鲢��֢�ȡ�������createEntry������
		* addEntry��
     */
    private void putForCreate(K key, V value) {
        int hash = null == key ? 0 : hash(key);
        int i = indexFor(hash, table.length);

        /**
				*�����Ѵ��ڵ���Կ��Ŀ������Զ���ᷢ��
				*��¡�����л�����ֻ�ᷢ���ڽ������
				*����ӳ����һ��������ӳ�䣬��������w/ =��һ�¡�
         */
        // �򵥵��滻ֵ
        for (Entry<K,V> e = table[i]; e != null; e = e.next) {
            Object k;
            if (e.hash == hash &&
                ((k = e.key) == key || (key != null && key.equals(k)))) {
                e.value = value;
                return;
            }
        }
				// û�о�ֵ������һ��
        createEntry(hash, key, value, i);
    }

    private void putAllForCreate(Map<? extends K, ? extends V> m) {
        for (Map.Entry<? extends K, ? extends V> e : m.entrySet())
            putForCreate(e.getKey(), e.getValue());
    }

    /**
			*����ӳ�����������ɢ�е�һ����������
			*������������ʱ�Զ����ô˷���
			*��ӳ���еļ����ﵽ����ֵ��
			*
			*�����ǰ����ΪMAXIMUM_CAPACITY����˷�����Ч
			*����ӳ��Ĵ�С������threshold����ΪInteger.MAX_VALUE��
			*�������Է�ֹ�Ժ�ĵ��á�
     */
    void resize(int newCapacity) {
        Entry[] oldTable = table;
        int oldCapacity = oldTable.length;
        if (oldCapacity == MAXIMUM_CAPACITY) {
            threshold = Integer.MAX_VALUE;
            return;
        }
				// ϸ�ڣ����ﲢû�е���inflateTable()��ʹcapacity��Ϊ2��ָ��
				// ��Ϊֻ����ԭ�ǿ�table�����ϣ��Ż����resize��������ԭtableһ������capacity������
        Entry[] newTable = new Entry[newCapacity];
        // �ڶ�������ͨ��Ϊfalse
        transfer(newTable, initHashSeedAsNeeded(newCapacity));
        table = newTable;
        threshold = (int)Math.min(newCapacity * loadFactor, MAXIMUM_CAPACITY + 1);
    }

    /**
     * ��������Ŀ�ӵ�ǰ��ת�Ƶ��±�
     */
    // ���߳̿��ܻᷢ��ѭ������
    void transfer(Entry[] newTable, boolean rehash) {
        int newCapacity = newTable.length;
        for (Entry<K,V> e : table) {
            while(null != e) {
                Entry<K,V> next = e.next;
                // һ��������������¼���hashֵ����������ǰ��
                if (rehash) {
                    e.hash = null == e.key ? 0 : hash(e.key);
                }
                int i = indexFor(e.hash, newCapacity);
                // ����ǰ�巨
                e.next = newTable[i];
                newTable[i] = e;
                e = next;
            }
        }
    }

    /**
		*��ָ��ӳ�������ӳ�临�Ƶ���ӳ�䡣
		*��Щӳ�佫�滻��ӳ�������ӳ��
		*��ǰ��ָ��ӳ���е��κμ���
     */
    public void putAll(Map<? extends K, ? extends V> m) {
        int numKeysToBeAdded = m.size();
        if (numKeysToBeAdded == 0)
            return;

        if (table == EMPTY_TABLE) {
            inflateTable((int) Math.max(numKeysToBeAdded * loadFactor, threshold));
        }

        /*
				*���Ҫ���ӳ�����������չ��ӳ��
				���ڻ������ֵ�����Ǳ��ص�;��
				*����������(m.size() + size) >=��ֵ�������
				*�������ܵ��µ�ͼ���������ʵ�������������
				*���Ҫ��ӵļ����ӳ�����Ѿ����ڵļ��ص���
				ͨ��ʹ�ñ��صļ��㣬���Ƕ��Լ������˼���
				*���һ�ζ��������С��
         */
        if (numKeysToBeAdded > threshold) {
            int targetCapacity = (int)(numKeysToBeAdded / loadFactor + 1);
            if (targetCapacity > MAXIMUM_CAPACITY)
                targetCapacity = MAXIMUM_CAPACITY;
            int newCapacity = table.length;
            while (newCapacity < targetCapacity)
                newCapacity <<= 1;
            if (newCapacity > table.length)
                resize(newCapacity);
        }

        for (Map.Entry<? extends K, ? extends V> e : m.entrySet())
            put(e.getKey(), e.getValue());
    }

    /**
	*		������ڣ���Ӵ�ӳ����ɾ��ָ������ӳ�䡣
     */
    public V remove(Object key) {
        Entry<K,V> e = removeEntryForKey(key);
        return (e == null ? null : e.value);
    }

    /**
	**		ɾ����������ָ������������
	*		��HashMap�С����HashMap������ӳ�䣬�򷵻�null
			*���Կ�ס�
     */
    final Entry<K,V> removeEntryForKey(Object key) {
        if (size == 0) {
            return null;
        }
        int hash = (key == null) ? 0 : hash(key);
        int i = indexFor(hash, table.length);
        Entry<K,V> prev = table[i];
        Entry<K,V> e = prev;

        while (e != null) {
            Entry<K,V> next = e.next;
            Object k;
            if (e.hash == hash &&
                ((k = e.key) == key || (key != null && key.equals(k)))) {
                modCount++;
                size--;
                if (prev == e)
                		// ������Ϊ����ͷ���ʱ
                    table[i] = next;
                else
                		// �����ݲ�Ϊ����ͷ���ʱ���轫ǰһ�ڵ��nextָ��ɾ���ڵ�ĺ�һ���
                    prev.next = next;
                e.recordRemoval(this);
                return e;
            }
            prev = e;
            e = next;
        }

        return e;
    }

    /**
			*ʹ��{@code map . EntrySet .equals()}ɾ��EntrySet������汾
			*ƥ�䡣
	*		ɾ���Ĳ�������ͬһ�ַ�����ͨ�����ɾ��
     */
    final Entry<K,V> removeMapping(Object o) {
        if (size == 0 || !(o instanceof Map.Entry))
            return null;

        Map.Entry<K,V> entry = (Map.Entry<K,V>) o;
        Object key = entry.getKey();
        int hash = (key == null) ? 0 : hash(key);
        int i = indexFor(hash, table.length);
        Entry<K,V> prev = table[i];
        Entry<K,V> e = prev;

        while (e != null) {
            Entry<K,V> next = e.next;
            if (e.hash == hash && e.equals(entry)) {
                modCount++;
                size--;
                if (prev == e)
                    table[i] = next;
                else
                    prev.next = next;
                e.recordRemoval(this);
                return e;
            }
            prev = e;
            e = next;
        }

        return e;
    }

    /**
		*�Ӹ�ӳ����ɾ������ӳ�䡣
		*���÷��غ�ӳ�佫Ϊ�ա�
     */  
    /*
    public static void fill(Object[] a, Object val) {
    	for (int i = 0, len = a.length; i < len; i++) {
    	  a[i] = val;
    	}
    }
    */
    public void clear() {
        modCount++;
        Arrays.fill(table, null);
        size = 0;
    }

    /**
		*�����ӳ�佫һ��������ӳ�䵽���򷵻�true
		*ָ��ֵ��
		*
		* @paramֵ����ֵ�ڴ�ӳ���еĴ��ڽ�������
		* @return true�����ӳ�佫һ��������ӳ�䵽
		*ָ��ֵ
     */
    public boolean containsValue(Object value) {
        if (value == null)
            return containsNullValue();

				// �������ã����Ը���table��Ҳ��Ӱ��������ж�
				// ˫��ѭ���������������Ԫ��������keyһ��equals�Ƚ�
				// �������ÿ��Բ�ͬ����table����һ������value��ͬʱ��������
        Entry[] tab = table;
        for (int i = 0; i < tab.length ; i++)
            for (Entry e = tab[i] ; e != null ; e = e.next)
                if (value.equals(e.value))
                    return true;
        return false;
    }

    /**
     * ���пղ�����containsValue�������������
     */
    private boolean containsNullValue() {
        Entry[] tab = table;
        for (int i = 0; i < tab.length ; i++)
            for (Entry e = tab[i] ; e != null ; e = e.next)
                if (e.value == null)
                    return true;
        return false;
    }

    /**
		*�������HashMapʵ��:����
		*û�п�¡ֵ����
     */
    public Object clone() {
        HashMap<K,V> result = null;
        try {
        		// ʹ�õ���Object��native����
        		// �����¿ռ佫Դ�������ݸ��ƹ�ȥ����˶����ڵ�����Ҳ�Ḵ��
            result = (HashMap<K,V>)super.clone();
        } catch (CloneNotSupportedException e) {
            // assert false;
        }
        // table.length <= hashMap.MAXIMUM_CAPACITY,���Եڶ���ȡmin���ڵ����壿
        if (result.table != EMPTY_TABLE) {
            result.inflateTable(Math.min(
                (int) Math.min(
                		// loadFactor����С��0.25�����loadFactor��С�������capacity��������
                		// Ҳ��һ�ֿռ��Ч�ʵ��ۺ�ѡ���
                		// ��������֮һΪ��size >= threshold
                		// threshold = capacity * loadFactor ==> capacity = size * 1/loadFactor
                    size * Math.min(1 / loadFactor, 4.0f),
                    // we have limits...
                    // ���ܳ���MAXIMUM_CAPACITY
                    HashMap.MAXIMUM_CAPACITY),
               // ���loadFactor��С����ôsize*min < length�����ܹ����˷ѿռ�
               table.length));
        }
        result.entrySet = null;
        result.modCount = 0;
        result.size = 0;
        result.init();
        // �ѱ����������Ԫ�ظ��Ƹ�result
        result.putAllForCreate(this);

        return result;
    }

    static class Entry<K,V> implements Map.Entry<K,V> {
    		// key ���ܱ䣬�ܺ����
        final K key;
        V value;
        // ά������
        Entry<K,V> next;
        int hash;

        Entry(int h, K k, V v, Entry<K,V> n) {
            value = v;
            next = n;
            key = k;
            hash = h;
        }

        public final K getKey() {
            return key;
        }

        public final V getValue() {
            return value;
        }

        public final V setValue(V newValue) {
            V oldValue = value;
            value = newValue;
            return oldValue;
        }

        public final boolean equals(Object o) {
            if (!(o instanceof Map.Entry))
                return false;
            Map.Entry e = (Map.Entry)o;
            // getKey()Ϊ�˷�װ��
            Object k1 = getKey();
            Object k2 = e.getKey();
            if (k1 == k2 || (k1 != null && k1.equals(k2))) {
                Object v1 = getValue();
                Object v2 = e.getValue();
                if (v1 == v2 || (v1 != null && v1.equals(v2)))
                    return true;
            }
            return false;
        }

        public final int hashCode() {
            return Objects.hashCode(getKey()) ^ Objects.hashCode(getValue());
        }

        public final String toString() {
            return getKey() + "=" + getValue();
        }

        /**
				*ÿ����Ŀ�е�ֵΪʱ���ô˷���
				*ͨ�������Ѵ��ڵļ�k��put(k,v)����
				*��HashMap�С�
         */
        void recordAccess(HashMap<K,V> m) {
        }

        /**
				*ÿ����Ŀ������ʱ��������ô˷���
				*�ӱ����Ƴ���
         */
        void recordRemoval(HashMap<K,V> m) {
        }
    }

    /**
			*������ָ������ֵ��ɢ�д����������ӵ�
			*ָ����Ͱ���������ǵ�����
			*�������ʵ��ص�����Ĵ�С��
     *
     * ���า�Ǵ˷����Ը���put��������Ϊ��
     */
    void addEntry(int hash, K key, V value, int bucketIndex) {
        if ((size >= threshold) && (null != table[bucketIndex])) {
        		// tableԭ������2��ָ���η�������ֱ�ӳ�2���ݣ�ָ����һ
            resize(2 * table.length);
            hash = (null != key) ? hash(key) : 0;
            bucketIndex = indexFor(hash, table.length);
        }

        createEntry(hash, key, value, bucketIndex);
    }

    /**
		*������addEntry��ֻ���ڴ�����Ŀʱʹ��������汾
		*��Ϊ��ͼ������α��������һ����(��¡��
		*�����л�)������汾���ص��ĵ�����Ĵ�С��
     *
		*���า�������ı�HashMap(Map)����Ϊ��
		*��¡����readObject��
     */
		// ΪcreateEntry()�ṩ����
		// ͷ�巨
    void createEntry(int hash, K key, V value, int bucketIndex) {
        Entry<K,V> e = table[bucketIndex];
        table[bucketIndex] = new Entry<>(hash, key, value, e);
        // ֻ��ȷ��table��û��key�Ż���е���һ��������һ������ֵ
        size++;
    }

    private abstract class HashIterator<E> implements Iterator<E> {
        Entry<K,V> next;        // next entry to return ��һ���
        int expectedModCount;   // For fast-fail
        int index;              // current slot
        Entry<K,V> current;     // current entry

        HashIterator() {
        		// ��ʼ��iteratorʱ��ȷ����modCount
            expectedModCount = modCount;
            if (size > 0) { // advance to first entry 
                Entry[] t = table;
                // �ҵ������е�һ���ǿյ�Ԫ�أ�nextΪ��һ����ЧԪ��
                while (index < t.length && (next = t[index++]) == null)
                    ;
            }
        }

        public final boolean hasNext() {
            return next != null;
        }

        final Entry<K,V> nextEntry() {
        		// ȷ�ϲ���iteratorʱ�뵱ǰ�޸�״̬�Ƿ�һ��
            if (modCount != expectedModCount)
                throw new ConcurrentModificationException();
            Entry<K,V> e = next;
            if (e == null)
                throw new NoSuchElementException();
						
						// ��hashmap�У���ЧԪ�ص��±겻һ���������ģ���˻���Ҫ�ҵ���һ����Ч��Ԫ��
            if ((next = e.next) == null) {
                Entry[] t = table;
                while (index < t.length && (next = t[index++]) == null)
                    ;
            }
            current = e;
            return e;
        }

        public void remove() {
            if (current == null)
                throw new IllegalStateException();
            if (modCount != expectedModCount)
                throw new ConcurrentModificationException();
            Object k = current.key;
            current = null;
            HashMap.this.removeEntryForKey(k);
            // iterator�е�remove�����modify״̬����������޸��쳣
            expectedModCount = modCount;
        }
    }

    private final class ValueIterator extends HashIterator<V> {
        public V next() {
            return nextEntry().value;
        }
    }

    private final class KeyIterator extends HashIterator<K> {
        public K next() {
            return nextEntry().getKey();
        }
    }

    private final class EntryIterator extends HashIterator<Map.Entry<K,V>> {
        public Map.Entry<K,V> next() {
            return nextEntry();
        }
    }

    // ���า����Щ���ı���ͼ��iterator()��������Ϊ
    Iterator<K> newKeyIterator()   {
        return new KeyIterator();
    }
    Iterator<V> newValueIterator()   {
        return new ValueIterator();
    }
    Iterator<Map.Entry<K,V>> newEntryIterator()   {
        return new EntryIterator();
    }


    // Views

    private transient Set<Map.Entry<K,V>> entrySet = null;

    /**
		*���ذ����ڴ�ӳ���еļ���{@link Set}��ͼ��
		* set����map֧�ֵģ����Զ�map�ĸ�����
		*��ӳ�ڼ����У���֮��Ȼ�������ͼ���޸�
		*�Լ��ϵĵ������ڽ�����(����through)
		*�����������ɾ������)�����Ϊ
		*����û�ж��塣����֧��Ԫ���Ƴ���
		*������ӳ����ɾ����Ӧ��ӳ��
		* < tt >��������ɾ��< / tt >, < tt > Set.remove < / tt >,
     */
    public Set<K> keySet() {
        Set<K> ks = keySet;
        // ������
        return (ks != null ? ks : (keySet = new KeySet()));
    }

    private final class KeySet extends AbstractSet<K> {
        public Iterator<K> iterator() {
            return newKeyIterator();
        }
        public int size() {
            return size;
        }
        public boolean contains(Object o) {
            return containsKey(o);
        }
        public boolean remove(Object o) {
            return HashMap.this.removeEntryForKey(o) != null;
        }
        public void clear() {
            HashMap.this.clear();
        }
    }

    /**
		*���ذ����ڴ�ӳ���е�ֵ��{@link Collection}��ͼ��
		*������ӳ��֧�֣����Զ�ӳ��ĸ�����
		*��ӳ�ڼ����У���֮��Ȼ�������ͼ��
		*�ڶԼ��Ͻ��е���ʱ�����޸�
		*(����ͨ�������������ɾ������)��
		*�����Ľ����δ����ġ�����
		*֧��Ԫ���Ƴ����Ƴ���Ӧ��Ԫ��
		*ͨ��Iterator.remove��
		* < tt >���ϡ�ɾ��< / tt >, < tt > removeAll
     */
    // �� �� Entry ������ͬ�����Ա�����set
    // �� value ������ͬ�����Կ�����collection
    public Collection<V> values() {
        Collection<V> vs = values;
        return (vs != null ? vs : (values = new Values()));
    }

    private final class Values extends AbstractCollection<V> {
        public Iterator<V> iterator() {
            return newValueIterator();
        }
        public int size() {
            return size;
        }
        public boolean contains(Object o) {
            return containsValue(o);
        }
        public void clear() {
            HashMap.this.clear();
        }
    }

    /**
		*���ذ��������ӳ���е�{@link Set}��ͼ��
		* set����map֧�ֵģ����Զ�map�ĸ�����
		*��ӳ�ڼ����У���֮��Ȼ�������ͼ���޸�
		*�Լ��ϵĵ������ڽ�����(����through)
		*�����������ɾ����������ͨ��
		* setValue������һ����ͼ��Ŀ�ķ���
		*�����Ľ����δ����ġ�һ��
		*֧��Ԫ���Ƴ����Ƴ���Ӧ��Ԫ��
		*ͨ��������.rem��ӳ��ӳ��
     */
    public Set<Map.Entry<K,V>> entrySet() {
        return entrySet0();
    }

    private Set<Map.Entry<K,V>> entrySet0() {
        Set<Map.Entry<K,V>> es = entrySet;
        return es != null ? es : (entrySet = new EntrySet());
    }

    private final class EntrySet extends AbstractSet<Map.Entry<K,V>> {
        public Iterator<Map.Entry<K,V>> iterator() {
            return newEntryIterator();
        }
        public boolean contains(Object o) {
            if (!(o instanceof Map.Entry))
                return false;
            Map.Entry<K,V> e = (Map.Entry<K,V>) o;
            Entry<K,V> candidate����ѡ�ˣ� = getEntry(e.getKey());
            return candidate != null && candidate.equals(e);
        }
        public boolean remove(Object o) {
            return removeMapping(o) != null;
        }
        public int size() {
            return size;
        }
        public void clear() {
            HashMap.this.clear();
        }
    }

    /**
     * ��HashMapʵ����״̬���浽����(����
			*���л�)��
     *
     * @serialData HashMap������
			* bucket����)������(int)���������
			* size (an int, the number of key-value
			*ӳ��)��Ȼ���Ǽ�(����)��ֵ(����)
			*����ÿ����ֵӳ�䡣��ֵӳ����
			*���ض�˳�򷢳���
     */
    private void writeObject(java.io.ObjectOutputStream s)
        throws IOException
    {
        // Write out the threshold, loadfactor, and any hidden stuff
        s.defaultWriteObject();

        // Write out number of buckets
        if (table==EMPTY_TABLE) {
            s.writeInt(roundUpToPowerOf2(threshold));
        } else {
           s.writeInt(table.length);
        }

        // Write out size (number of Mappings)
        s.writeInt(size);

        // Write out keys and values (alternating)
        if (size > 0) {
            for(Map.Entry<K,V> e : entrySet0()) {
                s.writeObject(e.getKey());
                s.writeObject(e.getValue());
            }
        }
    }

    private static final long serialVersionUID = 362498820763181265L;

    /**
     * Reconstitute the {@code HashMap} instance from a stream (i.e.,
     * deserialize it).
     */
    private void readObject(java.io.ObjectInputStream s)
         throws IOException, ClassNotFoundException
    {
        // Read in the threshold (ignored), loadfactor, and any hidden stuff
        s.defaultReadObject();
        if (loadFactor <= 0 || Float.isNaN(loadFactor)) {
            throw new InvalidObjectException("Illegal load factor: " +
                                               loadFactor);
        }

        // set other fields that need values
        table = (Entry<K,V>[]) EMPTY_TABLE;

        // Read in number of buckets
        s.readInt(); // ignored.

        // Read number of mappings
        int mappings = s.readInt();
        if (mappings < 0)
            throw new InvalidObjectException("Illegal mappings count: " +
                                               mappings);

        // capacity chosen by number of mappings and desired�������������� load (if >= 0.25)
        int capacity = (int) Math.min(
                    mappings * Math.min(1 / loadFactor, 4.0f),
                    // we have limits...
                    HashMap.MAXIMUM_CAPACITY);

        // allocate the bucket array; ����bucket����
        if (mappings > 0) {
            inflateTable(capacity);
        } else {
        		// threshold = 0��û��ӳ�䣬���Կ���Ϊ��
            threshold = capacity;
        }

        init();  // Give subclass a chance to do its thing.

        // Read the keys and values, and put the mappings in the HashMap
        for (int i = 0; i < mappings; i++) {
            K key = (K) s.readObject();
            V value = (V) s.readObject();
            putForCreate(key, value);
        }
    }

    // ��Щ�����������л���ϣ��
    int   capacity()     { return table.length; }
    float loadFactor()   { return loadFactor;   }
}
