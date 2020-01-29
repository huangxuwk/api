package java.util.concurrent;
import java.util.concurrent.locks.*;
import java.util.*;
import java.io.Serializable;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamField;

/**
	*֧�ּ�������ȫ�����Ĺ�ϣ��
	*���µ�Ԥ�ڲ����Կɵ����������ѭ
	*��{@link java.util.��ͬ�Ĺ��ܹ淶��Hashtable},
	*�������ÿ��������Ӧ�ķ����汾
	* < tt > Hashtable < / tt >��Ȼ������ʹ���еĲ�������
	*�̰߳�ȫ������������Ҫ��������Ҫ������
	*�����ж����Ƕ�������������κ�֧��
	*����ֹ���з��ʵķ�ʽ�����ſ�
	*��������Hashtable�ĳ�������Hashtable������
	*
	��������(����get)һ�㲻��Ҫ
	*�飬���Կ�������²����ص�(����
	* put and remove)��������ӳ���
	*���µ���ɵĸ��²�������
	*�����ǿ�ʼ��ʱ�򡣶��ھۺϲ���������putAll
	*������������������ܷ�ӳ�����
	*ֻɾ��һЩ��Ŀ��ͬ��,��������
	*ö�ٷ��ط�ӳ��ϣ��״̬��Ԫ��
	*�ڵ����Ĵ���ʱ��֮���ĳ��ʱ���
 *
	���²���֮������Ĳ���������
	*��ѡ��concurrencyLevel���캯������
	*(Ĭ��16)�������ڲ��ּ�����ʾ����
	*�����ڲ������ģ��Գ�������ָ����
	*û�����õĲ�������������Ϊλ��
	*�ڹ�ϣ���б�����������ģ�ʵ�ʵĲ����Ի�
	*������ͬ����������£���Ӧ��ѡ��һ��ֵ�����ɾ����ܶ��ֵ
	�߳̽���ͬʱ�޸ı�ʹ��һ��
	*������Ҫ�ĸߵö�ļ�ֵ

	����༰����ͼ�͵�����ʵ�����е�
	* {@link Map}��{@link Iterator}��em>��ѡ����
	*�ӿڡ�
 *
	��{@link Hashtable}���ƣ�����{@link HashMap}��ͬ�������
	* ������null��Ϊ����ֵ��
 */
public class ConcurrentHashMap<K, V> extends AbstractMap<K, V>
        implements ConcurrentMap<K, V>, Serializable {
    private static final long serialVersionUID = 7249069246763182397L;

    /*
			*���������ǽ����ϸ�ֵ���ͬ�Ĳ��֣�
			*ÿ����ϣ�����ǲ����ɶ��Ĺ�ϣ����
			*����ռ�ÿռ䣬��һ�����⣬����ζ�ֻ����
			*��һ����Ҫʱ(��ensureSegment)�����ֿɼ���
			*�ڴ��ڶ��Խṹʱ���Զεķ���Ϊ
			*���ڶα��Ԫ�ر���ʹ��volatile���ʣ�
			*����ͨ������ȫ�ķ����ֶε�
			�����*����Щ�ṩ��ԭ�Ӳο����ߵĹ���
			*���Ǽ��ټ�ӵĲ�Ρ�Additio

			*��ʷ��¼:�������ǰ�İ汾����
			*����ʹ�á�final���ֶΣ�������һЩvolatile��ȡ
			*����ռ�������ķ��á�һЩ������
			*�����������(����ǿ�ƽ���0��)
			*ȷ�����л������ԡ�
     */

    /* ---------------- Constants -------------- */

    static final int DEFAULT_INITIAL_CAPACITY = 16;
    static final float DEFAULT_LOAD_FACTOR = 0.75f;
		// �˱��Ĭ�ϲ�������
    static final int DEFAULT_CONCURRENCY_LEVEL = 16;
    static final int MAXIMUM_CAPACITY = 1 << 30;
    // ÿ���α����С����
    static final int MIN_SEGMENT_TABLE_CAPACITY = 2;
		// �����������Ա���
    static final int MAX_SEGMENTS = 1 << 16;
    // ����ǰ����Դ�������modCount�����
    static final int RETRIES_BEFORE_LOCK = 2;

    /* ---------------- Fields -------------- */

    /**
     * ������VM����֮ǰ�޷���ʼ����ֵ��
     */
    private static class Holder {

        /**
        * �����ַ������Ŀ�ѡ��ϣ?
        *
				������ɢ��ӳ��ʵ�ֲ�ͬ������û��ʵ��a
				*�����Ƿ�ʹ�������ϣ����ֵ
				*�ַ�������������ʵ���������˱�ѡɢ��
				*�������ʵ�����á�
        */
        static final boolean ALTERNATIVE_HASHING;

        static {
						//ʹ�á�threshold��ϵͳ���ԣ���ʹ���ǵķ�ֵ��Ϊ�ǡ������򡰹ء���
            String altThreshold = java.security.AccessController.doPrivileged(
            		// ��ȡProperty�ļ����ԣ�û��Ȩ����ʹ��hashseed
                new sun.security.action.GetPropertyAction(
                    "jdk.map.althashing.threshold"));

            int threshold;
            try {
                threshold = (null != altThreshold)
                				// ���ַ���ת��������
                        ? Integer.parseInt(altThreshold)
                        : Integer.MAX_VALUE;

                // ���-1�����ÿ�ѡ��ϣ
                if (threshold == -1) {
                    threshold = Integer.MAX_VALUE;
                }

                if (threshold < 0) {
                    throw new IllegalArgumentException("value must be positive integer.");
                }
            } catch(IllegalArgumentException failed) {
                throw new Error("Illegal value for 'jdk.map.althashing.threshold'", failed);
            }
            ALTERNATIVE_HASHING = threshold <= MAXIMUM_CAPACITY;
        }
    }

    /**
		*Ӧ���ڴ�ʵ����������ֵ
		*���Ĺ�ϣ�룬ʹ��ϣ��ͻ�����ҵ���
     */
    private transient final int hashSeed = randomHashSeed(this);

    private static int randomHashSeed(ConcurrentHashMap instance) {
        if (sun.misc.VM.isBooted() && Holder.ALTERNATIVE_HASHING) {
        		// �����Ȩ�޶�ȡ��ʹ������ȷ��threshold����ô�Ż�ʹ��hashseed
            return sun.misc.Hashing.randomHashSeed(instance);
        }

        return 0;
    }

		// �ε�����ֵ��ͨ������������������λ���±�
    final int segmentMask;
		// ����ȷ����ϣֵ�в���ζ�λ�ĸ�λ��λ��
    final int segmentShift;
		// �Σ�ÿ���ζ���һ��ר�ŵĹ�ϣ��
    final Segment<K,V>[] segments;

    transient Set<K> keySet;
    transient Set<Map.Entry<K,V>> entrySet;
    // ֵ�����ظ�
    transient Collection<V> values;

    // ConcurrentHashMap�б���Ŀ����ע�⣬����Զ���ᵼ��
		// ��Ϊ�û��ɼ���Map.Entry��
    static final class HashEntry<K,V> {
        final int hash;
        final K key;
        // ֵ�ͽ��ָ����Ըı�
        volatile V value;
        volatile HashEntry<K,V> next;

        HashEntry(int hash, K key, V value, HashEntry<K,V> next) {
            this.hash = hash;
            this.key = key;
            this.value = value;
            this.next = next;
        }

				// �ڱ������next��Ա��ƫ�Ƶ�ַ������n
        final void setNext(HashEntry<K,V> n) {
            UNSAFE.putOrderedObject(this, nextOffset, n);
        }

        static final sun.misc.Unsafe UNSAFE;
        static final long nextOffset;
        static {
            try {
                UNSAFE = sun.misc.Unsafe.getUnsafe();
                Class k = HashEntry.class;
                // �õ�next��Ա�ڶ����е�ƫ�����������������Ӳ���
                // ����UnSafe�����ͨ��ֱ�Ӳ����ڴ�������ٶ�
                nextOffset = UNSAFE.objectFieldOffset
                    (k.getDeclaredField("next"));
            } catch (Exception e) {
                throw new Error(e);
            }
        }
    }

		// ʹ��volatile��ȡ������ĵ�i��Ԫ�أ�ֱ��ͨ��ƫ�Ƶ�ַ��ȡ
    @SuppressWarnings("unchecked")
    static final <K,V> HashEntry<K,V> entryAt(HashEntry<K,V>[] tab, int i) {
        return (tab == null) ? null :
        		// getObjectVolatile()����tab�����ƫ�Ƴ��Ȼ�ö�Ӧ������
        		// ������ǻ������ƫ�Ƴ��ȵ�Ԫ�أ�Volatile��֤�ɼ��Ժ�������
            (HashEntry<K,V>) UNSAFE.getObjectVolatile
            // TSHIFT������ÿ��Ԫ�ص�ƫ�Ƴ��ȣ�TBASE�������׵�ַ��ƫ����
            // ���������������Եõ���Ԫ�ص������ַ
            (tab, ((long)i << TSHIFT) + TBASE);
    }

		// ʹ��volatileд���ø�����ĵ�i��Ԫ�ء�
    static final <K,V> void setEntryAt(HashEntry<K,V>[] tab, int i,
                                       HashEntry<K,V> e) {
        // ͨ��ƫ�Ƶ�ַ��e�ŵ�tab��ָ��λ��
        UNSAFE.putOrderedObject(tab, ((long)i << TSHIFT) + TBASE, e);
    }

    /**
			*�Ը�����hashCodeӦ��һ�����ӵ�ɢ�к���
			*��ֹ�������Ĺ�ϣ����������������Ҫ��
			��ΪConcurrentHashMapʹ��2���ݳ��ȹ�ϣ��
			��������������ڵĹ�ϣ���ͻ
			*����λ��ͬ��
     */
    private int hash(Object k) {
        int h = hashSeed;

        if ((0 != h) && (k instanceof String)) {
            return sun.misc.Hashing.stringHash32((String) k);
        }
				// ��֤key����null
        h ^= k.hashCode();
        
        h += (h <<  15) ^ 0xffffcd7d;
        h ^= (h >>> 10);
        h += (h <<   3);
        h ^= (h >>>  6);
        h += (h <<   2) + (h << 14);
        return h ^ (h >>> 16);
    }

    /**
		*���ǹ�ϣ�������汾����
		* ReentrantLock�����ֻ࣬��Ϊ��
		��һЩ�������ⵥ�����졣
     */
    static final class Segment<K,V> extends ReentrantLock implements Serializable {
        /*
					��ά��һ����Ŀ�б��������
					*����һ�µ�״̬�����Կ��Զ�ȡ(ͨ��volatile)
					*��ȡ�κͱ�)û����������
					*��Ҫ�ڱ��и��ƽڵ�
					*������С���Ա���߿��Ա������б�
					*��Ȼʹ�þɰ汾�ı��
         *
					�����ֻ������Ҫ���Ŀɱ䷽����
					��ķ���ִ��
					* ConcurrentHashMap������ÿ���ΰ汾��(����
					*����ֱ�Ӽ��ɵ�ConcurrentHashMap��
					*������)��Щͻ�䷽��ʹ��һ���ܿ���ʽ
					*ͨ������scanAndLock��
					* scanAndLockForPut����Щ��׺��
					*�����Զ�λ�ڵ㡣��Ҫ�ĺô�������
					����δ����(���ڹ�ϣ���кܳ���
         */

        private static final long serialVersionUID = 2249069246763182397L;

        /**
					*��ǰһ��Ԥ�������г���������������
					*�����ڻ�ȡʱ������Ϊ������׼��
					*�β������ڶദ�����ϣ�ʹ���н��
					*���Դ���ά���ڶ�λʱ��ȡ�Ļ���
					*�ڵ㡣
         */
        // ��ǿ�Ƽ���ǰ������Դ���
        // availableProcessors()����java������Ŀ��ô�������
        static final int MAX_SCAN_RETRIES =     		
            Runtime.getRuntime().availableProcessors() > 1 ? 64 : 1;

				// ÿ���ֶ�������һ������
        transient volatile HashEntry<K,V>[] table;
        // ��������
        transient int count;

        /**
					*��һ�ε�ͻ�����������
					*��Ȼ����ܻ����32λ�������ṩ��
					* CHM isEmpty()���ȶ��Լ���׼ȷ��
					*��size()������ֻ��������
					*�������ֿɼ��Ե�volatile��������
         */
        transient int modCount;
        // ������ֵ
        transient int threshold;
        // ��������
        final float loadFactor;

        Segment(float lf, int threshold, HashEntry<K,V>[] tab) {
            this.loadFactor = lf;
            this.threshold = threshold;
            this.table = tab;
        }
        
        final V put(K key, int hash, V value, boolean onlyIfAbsent) {
        		// tryLock()�������з���ֵ�ģ�����ʾ�������Ի�ȡ���������ȡ�ɹ���
        		// �򷵻�true�������ȡʧ�ܣ������ѱ������̻߳�ȡ�����򷵻�false��
        		// Ҳ��˵�������������ζ����������ء����ò�����ʱ����һֱ���ǵȴ���
            HashEntry<K,V> node = tryLock() ? null :
                scanAndLockForPut(key, hash, value);
            V oldValue;
            try {
                HashEntry<K,V>[] tab = table;
                // ȡ�������±�
                int index = (tab.length - 1) & hash;
                // �õ������׽��
                HashEntry<K,V> first = entryAt(tab, index);
                // �����Ƿ�����ͬ��key�����У������onlyIfAbsent��ȷ���Ƿ�����滻
                for (HashEntry<K,V> e = first;;) {
                    if (e != null) {
                        K k;
                        // key��ͬ������equals����
                        if ((k = e.key) == key ||
                            (e.hash == hash && key.equals(k))) {
                            oldValue = e.value;
                            // onlyIfAbsent: ���key�����ڲ�����
                            if (!onlyIfAbsent) {
                                e.value = value;
                                // �޸Ĵ�����1
                                ++modCount;
                            }
                            break;
                        }
                        // �����ǰ��㲻Ϊ�գ���ѯ��һ���
                        e = e.next;
                    }
                    // ����Ϊ�գ�����ĩ��㲢δ�ҵ�Ŀ��key
                    else {
                    		// ��㲻Ϊ�գ�˵��scanAndLockForPut()�з���ֵ
                        if (node != null)
                        		// ���ǰ�巨��hashmap1.7Ҳ�õ�ǰ�巨
                            node.setNext(first);
                        // ��һ����tryLock()�ɹ�
                        else
                        		// ǰ����ӽ�㣬��first��Ϊnode����һ���
                            node = new HashEntry<K,V>(hash, key, value, first);
                        int c = count + 1;
                        // �ﵽ������ֵ��δ�������������������
                        if (c > threshold && tab.length < MAXIMUM_CAPACITY)
                            rehash(node);
                        // ���µ�ͷ�����ɵ�����ŵ�������
                        else
                            setEntryAt(tab, index, node);
                        ++modCount;
                        count = c;
                        oldValue = null;
                        break;
                    }
                }
            // ��֤���۳����κ������Ҫ����
            } finally {
                unlock();
            }
            return oldValue;
        }

				// ���ݣ���Ϊ�����������н��У������̰߳�ȫ��������ѭ��
        @SuppressWarnings("unchecked")
        private void rehash(HashEntry<K,V> node) {
            HashEntry<K,V>[] oldTable = table;
            int oldCapacity = oldTable.length;
            // ��ԭ���������������£�����1��
            int newCapacity = oldCapacity << 1;
            // �µ�������ֵ
            threshold = (int)(newCapacity * loadFactor);
            // �����µ�����
            HashEntry<K,V>[] newTable =
                (HashEntry<K,V>[]) new HashEntry[newCapacity];
            int sizeMask = newCapacity - 1;
            // �������飬ÿһ����������ת��
            for (int i = 0; i < oldCapacity ; i++) {
            		// �õ�ԭ�����±���׽��
                HashEntry<K,V> e = oldTable[i];
                // �������Ϊ�գ���ת��
                if (e != null) {
                    HashEntry<K,V> next = e.next;
                    // ���¶�λ���������е��±�
                    int idx = e.hash & sizeMask;
                    // ����ֻ��һ�����
                    if (next == null)
                        newTable[idx] = e;
                    // ����ж����㣬�����еĽ�㶼����ת��
                    else {
                    		// ������һ�����
                        HashEntry<K,V> lastRun = e;
                        // ������һ�������±�
                        int lastIdx = idx;
                        // ��������        
                        for (HashEntry<K,V> last = next;
                             last != null;
                             last = last.next) {
           									// ÿ����㶼��Ҫ�ض�λ
                            int k = last.hash & sizeMask;
                            // ��������±����˱Ƚϣ���ζ����Ȼ�ж����㵫lastRunһֱû��
                            if (k != lastIdx) {
                                lastIdx = k;
                                lastRun = last;
                            }
                        }
                        newTable[lastIdx] = lastRun;
                        // ������±����˱�ǣ������������ת����һ����
                        for (HashEntry<K,V> p = e; p != lastRun; p = p.next) {
                            V v = p.value;
                            int h = p.hash;
                            // �ض�λ
                            int k = h & sizeMask;
                            // ǰ�巨
                            HashEntry<K,V> n = newTable[k];
                            newTable[k] = new HashEntry<K,V>(h, p.key, v, n);
                        }
                    }
                }
            }
            // ���½��ӵ�������
            int nodeIndex = node.hash & sizeMask;
            node.setNext(newTable[nodeIndex]);
            newTable[nodeIndex] = node;
            table = newTable;
        }

        // �������ã������½����ҵ�key��ͬ�Ľ�㲢������Ϊ�����׼��
        private HashEntry<K,V> scanAndLockForPut(K key, int hash, V value) {
        		// �ҵ���hashcode��Ӧ�������ͷ���
            HashEntry<K,V> first = entryForHash(this, hash);
            HashEntry<K,V> e = first;
            HashEntry<K,V> node = null;
            int retries = -1; // ��λ�ڵ�Ϊ��
            // ѭ����ȡ�����̰߳�ȫ
            while (!tryLock()) {
                HashEntry<K,V> f; // �����������¼��
                if (retries < 0) {
                		// ����Ϊ�ջ����������δ�ҵ�key��ͬ�Ľ��
                    if (e == null) {
                    		// ���������else if�п��ܻὫretries��Ϊ-1
                    		// ���Ի������ٴν��������Ҫ�ж�node�Ƿ�Ϊ��
                    		// ������Ȼ�������µĽ�㣬���ǲ�û�����������ϣ�e��ȻΪ��             		
                        if (node == null) // �����½��
                            node = new HashEntry<K,V>(hash, key, value, null);
                        retries = 0;
                    }
                    // �������ͬ���Ͳ���new�½��
                    else if (key.equals(e.key))
                        retries = 0;
                    // δ��ĩ��㣬�Ҳ�Ϊ��ǰ��㣬���ѯ��һ��
                    else
                        e = e.next;
                }
                // ����Ĵ�����������1��new���½�� 2���ҵ�key��ͬ�Ľ��
                
                // ���ɨ�����������ֵ����ǿ�ƻ�ȡ��
                else if (++retries > MAX_SCAN_RETRIES) {
                		// ����������ȡ���������������
                    lock();
                    break;
                }
                // (retries & 1) == 0�������λ��Ϊ1ʱ����			
								// (retries & 1) == 0��û����һ�佫���ܻ������ѭ��
								// ��ѭ������������if���ǽ�retries��Ϊ0����MAX_SCAN_RETRIES >= 1
								// ��û��(retries & 1) == 0���ƣ����������ǽ�������ɨ�裬����ѭ��
                else if ((retries & 1) == 0 &&
                				 // ����������仯���������ڵ�ǰ�̵߳��̶߳�����������޸�
                				 // һ�������ϵ�����仯�����±�����ѯ�����Ǳ�Ȼ��
                         (f = entryForHash(this, hash)) != first) {
                    e = first = f;
                    retries = -1;
                }
            }
            return node;
        }

        private void scanAndLock(Object key, int hash) {
        		// ��λĿ���±���׽��
            HashEntry<K,V> first = entryForHash(this, hash);
            HashEntry<K,V> e = first;
            int retries = -1;
            while (!tryLock()) {
                HashEntry<K,V> f;
                if (retries < 0) {
                    if (e == null || key.equals(e.key))
                        retries = 0;
                    else
                        e = e.next;
                }
                else if (++retries > MAX_SCAN_RETRIES) {
                    lock();
                    break;
                }
                else if ((retries & 1) == 0 &&
                         (f = entryForHash(this, hash)) != first) {
                    e = first = f;
                    retries = -1;
                }
            }
        }

				// ����ɾ���Ļ�������
        final V remove(Object key, int hash, Object value) {
            if (!tryLock())
            		// ����
                scanAndLock(key, hash);
            V oldValue = null;
            try {
                HashEntry<K,V>[] tab = table;
                int index = (tab.length - 1) & hash;
                // �ҵ�Ŀ���±���׽��
                HashEntry<K,V> e = entryAt(tab, index);
                // ǰһ���
                HashEntry<K,V> pred = null;
                while (e != null) {
                    K k;
                    HashEntry<K,V> next = e.next;
                    if ((k = e.key) == key ||
                        (e.hash == hash && key.equals(k))) {
                        V v = e.value;
                        // ���ֵΪnull��ֵ���
                        if (value == null || value == v || value.equals(v)) {
                        		// ��ǰ���Ϊͷ���
                            if (pred == null)
                                setEntryAt(tab, index, next);                     
                            else
                                pred.setNext(next);
                            ++modCount;
                            --count;
                            oldValue = v;
                        }
                        break;
                    }
                    pred = e;
                    e = next;
                }
            } finally {
                unlock();
            }
            return oldValue;
        }

        final boolean replace(K key, int hash, V oldValue, V newValue) {
            if (!tryLock())
            		// ����
                scanAndLock(key, hash);
            boolean replaced = false;
            try {
                HashEntry<K,V> e;
                for (e = entryForHash(this, hash); e != null; e = e.next) {
                    K k;
                    if ((k = e.key) == key ||
                        (e.hash == hash && key.equals(k))) {
                        if (oldValue.equals(e.value)) {
                            e.value = newValue;
                            ++modCount;
                            replaced = true;
                        }
                        break;
                    }
                }
            } finally {
                unlock();
            }
            return replaced;
        }

        final V replace(K key, int hash, V value) {
            if (!tryLock())
                scanAndLock(key, hash);
            V oldValue = null;
            try {
                HashEntry<K,V> e;
                for (e = entryForHash(this, hash); e != null; e = e.next) {
                    K k;
                    if ((k = e.key) == key ||
                        (e.hash == hash && key.equals(k))) {
                        oldValue = e.value;
                        e.value = value;
                        ++modCount;
                        break;
                    }
                }
            } finally {
                unlock();
            }
            return oldValue;
        }

        final void clear() {
            lock();
            try {
                HashEntry<K,V>[] tab = table;
                for (int i = 0; i < tab.length ; i++)
                    setEntryAt(tab, i, null);
                ++modCount;
                count = 0;
            } finally {
                unlock();
            }
        }
    }

    // Accessing segments

    /**
			*��ȡ����������(����ǿ�)�ĵ�j��Ԫ��
			* volatileԪ��ͨ������ȫ�������塣(����
			*ֻ���ڷ����л��������޺��ش�����)ע��:
			*��Ϊ�������ÿ��Ԫ��ֻ����һ��(ʹ��
			*��ȫ����д)��һЩ�������еķ�������
			*���ڶ�ȡ��ֵʱ���¼��˷�����
     */
    // ��ȡ���±�
    @SuppressWarnings("unchecked")
    static final <K,V> Segment<K,V> segmentAt(Segment<K,V>[] ss, int j) {
        long u = (j << SSHIFT) + SBASE;
        return ss == null ? null :
            (Segment<K,V>) UNSAFE.getObjectVolatile(ss, u);
    }

		// ȷ�϶Σ���û�иöΣ��򴴽�
    @SuppressWarnings("unchecked")
    private Segment<K,V> ensureSegment(int k) {
        final Segment<K,V>[] ss = this.segments;
        // ��������ַ��SSHIFT��SegmentԪ�ص�ƫ������SBASE��ss�ĵ�һ��Ԫ�صĵ�ַ
        long u = (k << SSHIFT) + SBASE;
        Segment<K,V> seg;
        // ͨ�������ַ�õ��Ķ���Ϊ��ʱ����������
        if ((seg = (Segment<K,V>)UNSAFE.getObjectVolatile(ss, u)) == null) {
        		// �ڹ��캯���ж�segments����ĵ�һ��Ԫ�ؽ����˳�ʼ��
        		// ��˵�һ��Ԫ�ؿ�����Ϊģ��������Ŀնν��г�ʼ��
            Segment<K,V> proto = ss[0];
            int cap = proto.table.length;
            float lf = proto.loadFactor;
            int threshold = (int)(cap * lf);
            HashEntry<K,V>[] tab = (HashEntry<K,V>[])new HashEntry[cap];
            // �ٴμ��ö��Ƿ�Ϊ��
            if ((seg = (Segment<K,V>)UNSAFE.getObjectVolatile(ss, u))
                == null) {
                Segment<K,V> s = new Segment<K,V>(lf, threshold, tab);
                while ((seg = (Segment<K,V>)UNSAFE.getObjectVolatile(ss, u))
                       == null) {
                    // cas��������Ŀ���Ϊ��ʱ���Ž����滻�������滻
                    // ����жϿ��Է�ֹ�����߳�ͬʱ����������滻���ε����
                    // ��һ���߳�������滻������һ���߳�����һ��getʱ�����������˳�ѭ��
                    if (UNSAFE.compareAndSwapObject(ss, u, null, seg = s))
                        break;
                }
            }
        }
        return seg;
    }

    // ���ڹ�ϣ�Ķκ���Ŀ����

    @SuppressWarnings("unchecked")
    private Segment<K,V> segmentForHash(int h) {
        long u = (((h >>> segmentShift) & segmentMask) << SSHIFT) + SBASE;
        return (Segment<K,V>) UNSAFE.getObjectVolatile(segments, u);
    }

		// ��ȡ�����κ�ɢ�еı���
    @SuppressWarnings("unchecked")
    static final <K,V> HashEntry<K,V> entryForHash(Segment<K,V> seg, int h) {
        HashEntry<K,V>[] tab;
        return (seg == null || (tab = seg.table) == null) ? null :
            (HashEntry<K,V>) UNSAFE.getObjectVolatile
            // (tab.length - 1) & h �������±�
            (tab, ((long)(((tab.length - 1) & h)) << TSHIFT) + TBASE);
    }

    /* ---------------- Public operations -------------- */

    @SuppressWarnings("unchecked")
    public ConcurrentHashMap(int initialCapacity,
                             float loadFactor, int concurrencyLevel) {
        if (!(loadFactor > 0) || initialCapacity < 0 || concurrencyLevel <= 0)
            throw new IllegalArgumentException();
        // concurrencyLevel������ˮƽ������Segment�ֶ��������ܳ���������
        if (concurrencyLevel > MAX_SEGMENTS)
            concurrencyLevel = MAX_SEGMENTS;
        int sshift = 0;
        int ssize = 1;
        while (ssize < concurrencyLevel) {
            ++sshift;
            ssize <<= 1;
        }
        // segmentShift��hashֵ�ж���ռ��λ��
        // ��concurrencyLevel = 16����segmentShift = 27
        this.segmentShift = 32 - sshift;
        // �ε�����-1������ͨ��hashֵ��Ŀ��ε��±�
        this.segmentMask = ssize - 1;
        // �����С���ܳ����������
        if (initialCapacity > MAXIMUM_CAPACITY)
            initialCapacity = MAXIMUM_CAPACITY;
        // c : ÿ������Ҫ�Ĺ�ϣ��Ĵ�С
        int c = initialCapacity / ssize;
        // ���initialCapacity / ssizeΪ����������Ҫ������չ
        if (c * ssize < initialCapacity)
            ++c;
        // ÿ���α��������С����Ϊ2
        int cap = MIN_SEGMENT_TABLE_CAPACITY;
        // ����õ�����Ҫ���cap
        while (cap < c)
            cap <<= 1;

				// ��Ȼ�����˶ε����飬����ֻʵ�����˵�һ��Ԫ�أ�������������
        Segment<K,V> s0 =
            new Segment<K,V>(loadFactor, (int)(cap * loadFactor),
                             (HashEntry<K,V>[])new HashEntry[cap]);
        Segment<K,V>[] ss = (Segment<K,V>[])new Segment[ssize];
        // ��s0�ŵ�ss����ĵ�һ��Ԫ��λ��
        UNSAFE.putOrderedObject(ss, SBASE, s0);
        this.segments = ss;
    }

    public ConcurrentHashMap(int initialCapacity, float loadFactor) {
        this(initialCapacity, loadFactor, DEFAULT_CONCURRENCY_LEVEL);
    }

    public ConcurrentHashMap(int initialCapacity) {
        this(initialCapacity, DEFAULT_LOAD_FACTOR, DEFAULT_CONCURRENCY_LEVEL);
    }

    public ConcurrentHashMap() {
        this(DEFAULT_INITIAL_CAPACITY, DEFAULT_LOAD_FACTOR, DEFAULT_CONCURRENCY_LEVEL);
    }

		// ��Ĭ�ϵļ������Ӻ�ͬ��ˮƽ���г�ʼ��
    public ConcurrentHashMap(Map<? extends K, ? extends V> m) {
    		// threshold = capacity * loadFactor
    		// -> capacity = m.size / loadFactor
        this(Math.max((int) (m.size() / DEFAULT_LOAD_FACTOR) + 1,
                      DEFAULT_INITIAL_CAPACITY),
             DEFAULT_LOAD_FACTOR, DEFAULT_CONCURRENCY_LEVEL);
        putAll(m);
    }

    public boolean isEmpty() {
        /*
         * Sum per-segment modCounts to avoid mis-reporting when
         * elements are concurrently added and removed in one segment
         * while checking another, in which case the table was never
         * actually empty at any point. (The sum ensures accuracy up
         * through at least 1<<31 per-segment modifications before
         * recheck.)  Methods size() and containsValue() use similar
         * constructions for stability checks.
         */
        long sum = 0L;
        final Segment<K,V>[] segments = this.segments;
        for (int j = 0; j < segments.length; ++j) {
        		// �õ���Ӧ�±�Ķ�
            Segment<K,V> seg = segmentAt(segments, j);
            // ����κ͸�������Ϊ�գ�����ֵ
            if (seg != null) {
                if (seg.count != 0)
                    return false;
                sum += seg.modCount;
            }
        }
        // TODO
        if (sum != 0L) { // ���¼�飬����û���޸�
            for (int j = 0; j < segments.length; ++j) {
                Segment<K,V> seg = segmentAt(segments, j);
                if (seg != null) {
                    if (seg.count != 0)
                        return false;
                    sum -= seg.modCount;
                }
            }
            // �޸����ı��ˣ�������ԭ��ϣ������˲���
            // �����ϣ��Ϊ��
            if (sum != 0L)
                return false;
        }
        return true;
    }

    public int size() {
        final Segment<K,V>[] segments = this.segments;
        int size;
        boolean overflow; // �����С���32λ����Ϊ��
        long sum;         // modCounts֮��
        long last = 0L;   // ֮ǰ���ܺ�
        int retries = -1; // ��һ�ε�����������
        try {
            for (;;) {
            		// ����ǰ���Դ�����RETRIES_BEFORE_LOCK = 2������3��
            		// ���������ò�������Ľ��ʱ��ǿ�Ƽ�������size��ͳ��
                if (retries++ == RETRIES_BEFORE_LOCK) {
                		// ��ÿһ���ζ����м���
                    for (int j = 0; j < segments.length; ++j)
                        ensureSegment(j).lock();
                }
                sum = 0L;
                size = 0;
                overflow = false;
                for (int j = 0; j < segments.length; ++j) {
                    Segment<K,V> seg = segmentAt(segments, j);
                    if (seg != null) {
                        sum += seg.modCount;
                        int c = seg.count;
                        // ���
                        if (c < 0 || (size += c) < 0)
                            overflow = true;
                    }
                }
                // ���ԣ�һֱ��ͳ���ڼ�û�б���̶߳Թ�ϣ����в���
                // ���������ε�sum��ͬ��˵��û�б���߳̽��и��棬���Է���ֵ
                if (sum == last)
                    break;
                last = sum;
            }
        } finally {
        		// ֻ�м����˲��б�Ҫ���н���
            if (retries > RETRIES_BEFORE_LOCK) {
                for (int j = 0; j < segments.length; ++j)
                    segmentAt(segments, j).unlock();
            }
        }
        return overflow ? Integer.MAX_VALUE : size;
    }

    public V get(Object key) {
        Segment<K,V> s;
        HashEntry<K,V>[] tab;
        // �õ�key��ɢ��ֵ
        int h = hash(key);
        // h >>> segmentShift : �������ƣ��൱��hashֵ�ĸ�nλ�μ��˶εĶ�λ
        long u = (((h >>> segmentShift) & segmentMask) << SSHIFT) + SBASE;
        // ������±�Ķβ�Ϊ���Ҷ��е����鲻Ϊ������������
        if ((s = (Segment<K,V>)UNSAFE.getObjectVolatile(segments, u)) != null &&
            (tab = s.table) != null) {
            // UNSAFE.getObjectVolatile()�õ������������ַ�µö���
            for (HashEntry<K,V> e = (HashEntry<K,V>) UNSAFE.getObjectVolatile
            				 // (tab.length - 1) & h : ʹ��ȫ��ɢ��ֵ����Ԫ�ص��±궨λ
            				 // TSHIFT ����ϣ��ÿ��Ԫ�ص�ƫ������TBASE ����ϣ���һ��Ԫ�ص�ƫ����
                     (tab, ((long)(((tab.length - 1) & h)) << TSHIFT) + TBASE);
                 e != null; e = e.next) {
                K k;
                if ((k = e.key) == key || (e.hash == h && key.equals(k)))
                    return e.value;
            }
        }
        return null;
    }

		// ��get()��������һ��
    @SuppressWarnings("unchecked")
    public boolean containsKey(Object key) {
        Segment<K,V> s;
        HashEntry<K,V>[] tab;
        int h = hash(key);
        long u = (((h >>> segmentShift) & segmentMask) << SSHIFT) + SBASE;
        if ((s = (Segment<K,V>)UNSAFE.getObjectVolatile(segments, u)) != null &&
            (tab = s.table) != null) {
            for (HashEntry<K,V> e = (HashEntry<K,V>) UNSAFE.getObjectVolatile
                     (tab, ((long)(((tab.length - 1) & h)) << TSHIFT) + TBASE);
                 e != null; e = e.next) {
                K k;
                if ((k = e.key) == key || (e.hash == h && key.equals(k)))
                    return true;
            }
        }
        return false;
    }

    public boolean containsValue(Object value) {
        if (value == null)
            throw new NullPointerException();
        final Segment<K,V>[] segments = this.segments;
        boolean found = false;
        long last = 0;
        int retries = -1;
        try {
            outer: for (;;) {
            		// ԭ����size()����һ��
                if (retries++ == RETRIES_BEFORE_LOCK) {
                    for (int j = 0; j < segments.length; ++j)
                        ensureSegment(j).lock();
                }
                long hashSum = 0L;
                int sum = 0;
                for (int j = 0; j < segments.length; ++j) {
                    HashEntry<K,V>[] tab;
                    Segment<K,V> seg = segmentAt(segments, j);
                    if (seg != null && (tab = seg.table) != null) {
                        for (int i = 0 ; i < tab.length; i++) {
                            HashEntry<K,V> e;
                            for (e = entryAt(tab, i); e != null; e = e.next) {
                                V v = e.value;
                                if (v != null && value.equals(v)) {
                                		// ����ҵ������ж��Ƿ�ͬ����ֱ������ѭ��
                                    found = true;
                                    // ��ͨbreakֻ������һ��ѭ�������������������
                                    break outer;
                                }
                            }
                        }
                        sum += seg.modCount;
                    }
                }
                // ���û���ҵ����Ż��ж��Ƿ�ͬ��������ͬ����������
                if (retries > 0 && sum == last)
                    break;
                last = sum;
            }
        } finally {
            if (retries > RETRIES_BEFORE_LOCK) {
                for (int j = 0; j < segments.length; ++j)
                    segmentAt(segments, j).unlock();
            }
        }
        return found;
    }

    public boolean contains(Object value) {
        return containsValue(value);
    }

		// put()������������ԭʼ�ļ�ֵ�ԣ����滻ֵ��������ֱ�����
		// key��value������Ϊnull
    @SuppressWarnings("unchecked")
    public V put(K key, V value) {
        Segment<K,V> s;
        // ֵ����Ϊnull
        if (value == null)
            throw new NullPointerException();
        int hash = hash(key);
        // ͨ��ɢ��ֵ�ĸ�nλ��ȷ�����±�
        int j = (hash >>> segmentShift) & segmentMask;
        // ���������ַ��ȡ��Ŀ���
				// SSHIFT ��Segment����ÿ��Ԫ�ص�ƫ������SBASE ��Segment�����һ��Ԫ�ص�ƫ����
				// ����������ƫ�����������Ŀ���±�Ԫ�ص������ַ
        if ((s = (Segment<K,V>)UNSAFE.getObject          
             (segments, (j << SSHIFT) + SBASE)) == null)
            // ȷ�ϸöδ��ڣ��������ڣ��÷�������г�ʼ��
            s = ensureSegment(j);
        // false : boolean onlyIfAbsent
        return s.put(key, hash, value, false);
    }

		// ������ԭʼ�ļ�ֵ�ԣ����滻ֵ��������ֱ�����
    @SuppressWarnings("unchecked")
    public V putIfAbsent(K key, V value) {
        Segment<K,V> s;
        if (value == null)
            throw new NullPointerException();
        int hash = hash(key);
        int j = (hash >>> segmentShift) & segmentMask;
        if ((s = (Segment<K,V>)UNSAFE.getObject
             (segments, (j << SSHIFT) + SBASE)) == null)
            s = ensureSegment(j);
        // true : boolean onlyIfAbsent
        return s.put(key, hash, value, true);
    }

    // ��Ŀ��map������ӳ����ӵ���������
    public void putAll(Map<? extends K, ? extends V> m) {
        for (Map.Entry<? extends K, ? extends V> e : m.entrySet())
            put(e.getKey(), e.getValue());
    }

    public V remove(Object key) {
        int hash = hash(key);
        // ����ɢ��ֵȡ��Ŀ���
        Segment<K,V> s = segmentForHash(hash);
        return s == null ? null : s.remove(key, hash, null);
    }

    public boolean remove(Object key, Object value) {
        int hash = hash(key);
        Segment<K,V> s;
        return value != null && (s = segmentForHash(hash)) != null &&
            s.remove(key, hash, value) != null;
    }

    public boolean replace(K key, V oldValue, V newValue) {
        int hash = hash(key);
        if (oldValue == null || newValue == null)
            throw new NullPointerException();
        Segment<K,V> s = segmentForHash(hash);
        return s != null && s.replace(key, hash, oldValue, newValue);
    }

    public V replace(K key, V value) {
        int hash = hash(key);
        if (value == null)
            throw new NullPointerException();
        Segment<K,V> s = segmentForHash(hash);
        return s == null ? null : s.replace(key, hash, value);
    }

    public void clear() {
        final Segment<K,V>[] segments = this.segments;
        for (int j = 0; j < segments.length; ++j) {
            Segment<K,V> s = segmentAt(segments, j);
            if (s != null)
                s.clear();
        }
    }

    public Set<K> keySet() {
        Set<K> ks = keySet;
        return (ks != null) ? ks : (keySet = new KeySet());
    }

    public Collection<V> values() {
        Collection<V> vs = values;
        return (vs != null) ? vs : (values = new Values());
    }

    public Set<Map.Entry<K,V>> entrySet() {
        Set<Map.Entry<K,V>> es = entrySet;
        return (es != null) ? es : (entrySet = new EntrySet());
    }

    public Enumeration<K> keys() {
        return new KeyIterator();
    }

    public Enumeration<V> elements() {
        return new ValueIterator();
    }

    /* ---------------- Iterator Support -------------- */

    abstract class HashIterator {
        int nextSegmentIndex;
        int nextTableIndex;
        HashEntry<K,V>[] currentTable;
        HashEntry<K, V> nextEntry;
        HashEntry<K, V> lastReturned;

        HashIterator() {
            nextSegmentIndex = segments.length - 1;
            nextTableIndex = -1;
            advance();
        }

        /**
					*��nextEntry����Ϊ��һ���ǿձ�ĵ�һ���ڵ�
					*(Ϊ�˼򻯼�飬����������)��
         */
        final void advance() {
            for (;;) {
                if (nextTableIndex >= 0) {
                    if ((nextEntry = entryAt(currentTable,
                                             nextTableIndex--)) != null)
                        break;
                }
                else if (nextSegmentIndex >= 0) {
                    Segment<K,V> seg = segmentAt(segments, nextSegmentIndex--);
                    if (seg != null && (currentTable = seg.table) != null)
                        nextTableIndex = currentTable.length - 1;
                }
                else
                    break;
            }
        }

        final HashEntry<K,V> nextEntry() {
            HashEntry<K,V> e = nextEntry;
            if (e == null)
                throw new NoSuchElementException();
            lastReturned = e; // ��ִ�пռ��֮ǰ���ܸ�ֵ
            // �����ǰ������һ���Ϊ�գ�����Ҫ�ٴ��ҵ��ǿս��
            if ((nextEntry = e.next) == null)
                advance();
            return e;
        }

        public final boolean hasNext() { return nextEntry != null; }
        public final boolean hasMoreElements() { return nextEntry != null; }

        public final void remove() {
            if (lastReturned == null)
                throw new IllegalStateException();
            ConcurrentHashMap.this.remove(lastReturned.key);
            lastReturned = null;
        }
    }

    final class KeyIterator
        extends HashIterator
        implements Iterator<K>, Enumeration<K>
    {
        public final K next()        { return super.nextEntry().key; }
        public final K nextElement() { return super.nextEntry().key; }
    }

    final class ValueIterator
        extends HashIterator
        implements Iterator<V>, Enumeration<V>
    {
        public final V next()        { return super.nextEntry().value; }
        public final V nextElement() { return super.nextEntry().value; }
    }

    /**
			��EntryIterator.next()ʹ�õ��Զ��������
			*���Ļ���ӳ���setValue��
     */
    final class WriteThroughEntry
        extends AbstractMap.SimpleEntry<K,V>
    {
        WriteThroughEntry(K k, V v) {
            super(k,v);
        }

        /**
					*�������ǵ���Ŀ��ֵ��д�뵽��ͼ����
					*���ص�ֵ������������ġ���
					* WriteThroughEntry��һ�������첽
					*���ģ�����ġ���ǰ��ֵ������
					�����������صĲ�ͬ(���������ǲ�ͬ��)
					*���Ƴ�����ʱput�����½���)�����ǲ�
					���ܱ�֤���ࡣ
         */
        public V setValue(V value) {
            if (value == null) throw new NullPointerException();
            V v = super.setValue(value);
            ConcurrentHashMap.this.put(getKey(), value);
            return v;
        }
    }

    final class EntryIterator
        extends HashIterator
        implements Iterator<Entry<K,V>>
    {
        public Map.Entry<K,V> next() {
            HashEntry<K,V> e = super.nextEntry();
            return new WriteThroughEntry(e.key, e.value);
        }
    }

    final class KeySet extends AbstractSet<K> {
        public Iterator<K> iterator() {
            return new KeyIterator();
        }
        public int size() {
            return ConcurrentHashMap.this.size();
        }
        public boolean isEmpty() {
            return ConcurrentHashMap.this.isEmpty();
        }
        public boolean contains(Object o) {
            return ConcurrentHashMap.this.containsKey(o);
        }
        public boolean remove(Object o) {
            return ConcurrentHashMap.this.remove(o) != null;
        }
        public void clear() {
            ConcurrentHashMap.this.clear();
        }
    }

    final class Values extends AbstractCollection<V> {
        public Iterator<V> iterator() {
            return new ValueIterator();
        }
        public int size() {
            return ConcurrentHashMap.this.size();
        }
        public boolean isEmpty() {
            return ConcurrentHashMap.this.isEmpty();
        }
        public boolean contains(Object o) {
            return ConcurrentHashMap.this.containsValue(o);
        }
        public void clear() {
            ConcurrentHashMap.this.clear();
        }
    }

    final class EntrySet extends AbstractSet<Map.Entry<K,V>> {
        public Iterator<Map.Entry<K,V>> iterator() {
            return new EntryIterator();
        }
        public boolean contains(Object o) {
            if (!(o instanceof Map.Entry))
                return false;
            Map.Entry<?,?> e = (Map.Entry<?,?>)o;
            V v = ConcurrentHashMap.this.get(e.getKey());
            return v != null && v.equals(e.getValue());
        }
        public boolean remove(Object o) {
            if (!(o instanceof Map.Entry))
                return false;
            Map.Entry<?,?> e = (Map.Entry<?,?>)o;
            return ConcurrentHashMap.this.remove(e.getKey(), e.getValue());
        }
        public int size() {
            return ConcurrentHashMap.this.size();
        }
        public boolean isEmpty() {
            return ConcurrentHashMap.this.isEmpty();
        }
        public void clear() {
            ConcurrentHashMap.this.clear();
        }
    }

    /* ---------------- Serialization Support -------------- */

    /**
     * Save the state of the <tt>ConcurrentHashMap</tt> instance to a
     * stream (i.e., serialize it).
     * @param s the stream
     * @serialData
     * the key (Object) and value (Object)
     * for each key-value mapping, followed by a null pair.
     * The key-value mappings are emitted in no particular order.
     */
    private void writeObject(java.io.ObjectOutputStream s) throws IOException {
        // force all segments for serialization compatibility
        for (int k = 0; k < segments.length; ++k)
            ensureSegment(k);
        s.defaultWriteObject();

        final Segment<K,V>[] segments = this.segments;
        for (int k = 0; k < segments.length; ++k) {
            Segment<K,V> seg = segmentAt(segments, k);
            seg.lock();
            try {
                HashEntry<K,V>[] tab = seg.table;
                for (int i = 0; i < tab.length; ++i) {
                    HashEntry<K,V> e;
                    for (e = entryAt(tab, i); e != null; e = e.next) {
                        s.writeObject(e.key);
                        s.writeObject(e.value);
                    }
                }
            } finally {
                seg.unlock();
            }
        }
        s.writeObject(null);
        s.writeObject(null);
    }

    /**
     * Reconstitute the <tt>ConcurrentHashMap</tt> instance from a
     * stream (i.e., deserialize it).
     * @param s the stream
     */
    @SuppressWarnings("unchecked")
    private void readObject(java.io.ObjectInputStream s)
        throws IOException, ClassNotFoundException {
        // Don't call defaultReadObject()
        ObjectInputStream.GetField oisFields = s.readFields();
        final Segment<K,V>[] oisSegments = (Segment<K,V>[])oisFields.get("segments", null);

        final int ssize = oisSegments.length;
        if (ssize < 1 || ssize > MAX_SEGMENTS
            || (ssize & (ssize-1)) != 0 )  // ssize not power of two
            throw new java.io.InvalidObjectException("Bad number of segments:"
                                                     + ssize);
        int sshift = 0, ssizeTmp = ssize;
        while (ssizeTmp > 1) {
            ++sshift;
            ssizeTmp >>>= 1;
        }
        UNSAFE.putIntVolatile(this, SEGSHIFT_OFFSET, 32 - sshift);
        UNSAFE.putIntVolatile(this, SEGMASK_OFFSET, ssize - 1);
        UNSAFE.putObjectVolatile(this, SEGMENTS_OFFSET, oisSegments);

        // set hashMask
        UNSAFE.putIntVolatile(this, HASHSEED_OFFSET, randomHashSeed(this));

        // Re-initialize segments to be minimally sized, and let grow.
        int cap = MIN_SEGMENT_TABLE_CAPACITY;
        final Segment<K,V>[] segments = this.segments;
        for (int k = 0; k < segments.length; ++k) {
            Segment<K,V> seg = segments[k];
            if (seg != null) {
                seg.threshold = (int)(cap * seg.loadFactor);
                seg.table = (HashEntry<K,V>[]) new HashEntry[cap];
            }
        }

        // Read the keys and values, and put the mappings in the table
        for (;;) {
            K key = (K) s.readObject();
            V value = (V) s.readObject();
            if (key == null)
                break;
            put(key, value);
        }
    }

    // Unsafe mechanics
    private static final sun.misc.Unsafe UNSAFE;
    private static final long SBASE;
    private static final int SSHIFT;
    private static final long TBASE;
    private static final int TSHIFT;
    private static final long HASHSEED_OFFSET;
    private static final long SEGSHIFT_OFFSET;
    private static final long SEGMASK_OFFSET;
    private static final long SEGMENTS_OFFSET;

    static {
        int ss, ts;
        try {
            UNSAFE = sun.misc.Unsafe.getUnsafe();
            Class tc = HashEntry[].class;
            Class sc = Segment[].class;
            // �����һ��Ԫ�ص�ƫ�����������׵�ַ
            TBASE = UNSAFE.arrayBaseOffset(tc);
            SBASE = UNSAFE.arrayBaseOffset(sc);
            // ������ÿ��Ԫ�صĴ�С������ָ�룩��С
            ts = UNSAFE.arrayIndexScale(tc);
            ss = UNSAFE.arrayIndexScale(sc);
            // ��������ڶ����е�ƫ�Ƶ�ַ
            HASHSEED_OFFSET = UNSAFE.objectFieldOffset(
                ConcurrentHashMap.class.getDeclaredField("hashSeed"));
            SEGSHIFT_OFFSET = UNSAFE.objectFieldOffset(
                ConcurrentHashMap.class.getDeclaredField("segmentShift"));
            SEGMASK_OFFSET = UNSAFE.objectFieldOffset(
                ConcurrentHashMap.class.getDeclaredField("segmentMask"));
            SEGMENTS_OFFSET = UNSAFE.objectFieldOffset(
                ConcurrentHashMap.class.getDeclaredField("segments"));
        } catch (Exception e) {
            throw new Error(e);
        }
        // Ԫ�س��ȱ�����2��ָ���ݣ�����Ԫ���Ƕ���ָ�룩
        // (ss & (ss-1)) != 0 ��֤������λ��ֻ��һ��1������ȫΪ0
        if ((ss & (ss-1)) != 0 || (ts & (ts-1)) != 0)
            throw new Error("data type scale not a power of two");
        
        // numberOfLeadingZeros(ss)���Ӹ�λ����λ��һ��1��0�ĸ���
        // 0000 0100 ���Ϊ5
        SSHIFT = 31 - Integer.numberOfLeadingZeros(ss);
        TSHIFT = 31 - Integer.numberOfLeadingZeros(ts);
    }

}
