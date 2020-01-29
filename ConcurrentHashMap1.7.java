package java.util.concurrent;
import java.util.concurrent.locks.*;
import java.util.*;
import java.io.Serializable;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamField;

/**
	*支持检索和完全并发的哈希表
	*更新的预期并发性可调。这个类遵循
	*与{@link java.util.相同的功能规范。Hashtable},
	*包含与的每个方法对应的方法版本
	* < tt > Hashtable < / tt >。然而，即使所有的操作都是
	*线程安全，检索操作需要而不是需要锁定，
	*并且有而不是对锁定整个表的任何支持
	*以阻止所有访问的方式。这门课
	*在依赖于Hashtable的程序中与Hashtable互操作
	*
	检索操作(包括get)一般不需要
	*块，所以可能与更新操作重叠(包括
	* put and remove)。检索反映结果
	*最新的完成的更新操作持有
	*在他们开始的时候。对于聚合操作，例如putAll
	*和清除，并发检索可能反映插入或
	*只删除一些条目。同样,迭代器和
	*枚举返回反映哈希表状态的元素
	*在迭代的创建时或之后的某个时间点
 *
	更新操作之间允许的并发性是由
	*可选的concurrencyLevel构造函数参数
	*(默认16)，用于内部分级的提示。的
	*表是内部分区的，以尝试允许指定的
	*没有争用的并发更新数。因为位置
	*在哈希表中本质上是随机的，实际的并发性会
	*有所不同。理想情况下，您应该选择一个值来容纳尽可能多的值
	线程将会同时修改表。使用一个
	*比你需要的高得多的价值

	这个类及其视图和迭代器实现所有的
	* {@link Map}和{@link Iterator}的em>可选方法
	*接口。
 *
	与{@link Hashtable}类似，但与{@link HashMap}不同，这个类
	* 不允许null作为键或值。
 */
public class ConcurrentHashMap<K, V> extends AbstractMap<K, V>
        implements ConcurrentMap<K, V>, Serializable {
    private static final long serialVersionUID = 7249069246763182397L;

    /*
			*基本策略是将表格细分到不同的部分，
			*每个哈希表本身都是并发可读的哈希表。来
			*减少占用空间，除一个段外，其余段都只构造
			*第一次需要时(见ensureSegment)。保持可见性
			*在存在惰性结构时，对段的访问为
			*由于段表的元素必须使用volatile访问，
			*这是通过不安全的方法分段等
			下面的*。这些提供了原子参考射线的功能
			*但是减少间接的层次。Additio

			*历史记录:这个类以前的版本依赖
			*大量使用“final”字段，避免了一些volatile读取
			*初期占地面积大的费用。一些残留的
			*存在这种设计(包括强制建造0段)
			*确保序列化兼容性。
     */

    /* ---------------- Constants -------------- */

    static final int DEFAULT_INITIAL_CAPACITY = 16;
    static final float DEFAULT_LOAD_FACTOR = 0.75f;
		// 此表的默认并发级别
    static final int DEFAULT_CONCURRENCY_LEVEL = 16;
    static final int MAXIMUM_CAPACITY = 1 << 30;
    // 每个段表的最小容量
    static final int MIN_SEGMENT_TABLE_CAPACITY = 2;
		// 最大段数，略显保守
    static final int MAX_SEGMENTS = 1 << 16;
    // 加锁前最大尝试次数，与modCount相配合
    static final int RETRIES_BEFORE_LOCK = 2;

    /* ---------------- Fields -------------- */

    /**
     * 保存在VM启动之前无法初始化的值。
     */
    private static class Holder {

        /**
        * 启用字符串键的可选哈希?
        *
				与其他散列映射实现不同，我们没有实现a
				*调节是否使用替代哈希的阈值
				*字符串键。对所有实例都启用了备选散列
				*或对所有实例禁用。
        */
        static final boolean ALTERNATIVE_HASHING;

        static {
						//使用“threshold”系统属性，即使我们的阀值行为是“开”或“关”。
            String altThreshold = java.security.AccessController.doPrivileged(
            		// 读取Property文件属性，没有权限则不使用hashseed
                new sun.security.action.GetPropertyAction(
                    "jdk.map.althashing.threshold"));

            int threshold;
            try {
                threshold = (null != altThreshold)
                				// 将字符串转换成整数
                        ? Integer.parseInt(altThreshold)
                        : Integer.MAX_VALUE;

                // 如果-1，禁用可选哈希
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
		*应用于此实例的相关随机值
		*键的哈希码，使哈希冲突更难找到。
     */
    private transient final int hashSeed = randomHashSeed(this);

    private static int randomHashSeed(ConcurrentHashMap instance) {
        if (sun.misc.VM.isBooted() && Holder.ALTERNATIVE_HASHING) {
        		// 如果有权限读取并使用了正确的threshold，那么才会使用hashseed
            return sun.misc.Hashing.randomHashSeed(instance);
        }

        return 0;
    }

		// 段的掩码值，通过它进行与运算来定位段下标
    final int segmentMask;
		// 用来确定哈希值中参与段定位的高位的位数
    final int segmentShift;
		// 段，每个段都是一个专门的哈希表
    final Segment<K,V>[] segments;

    transient Set<K> keySet;
    transient Set<Map.Entry<K,V>> entrySet;
    // 值可以重复
    transient Collection<V> values;

    // ConcurrentHashMap列表条目。请注意，这永远不会导出
		// 作为用户可见的Map.Entry。
    static final class HashEntry<K,V> {
        final int hash;
        final K key;
        // 值和结点指向可以改变
        volatile V value;
        volatile HashEntry<K,V> next;

        HashEntry(int hash, K key, V value, HashEntry<K,V> next) {
            this.hash = hash;
            this.key = key;
            this.value = value;
            this.next = next;
        }

				// 在本对象的next成员的偏移地址处放入n
        final void setNext(HashEntry<K,V> n) {
            UNSAFE.putOrderedObject(this, nextOffset, n);
        }

        static final sun.misc.Unsafe UNSAFE;
        static final long nextOffset;
        static {
            try {
                UNSAFE = sun.misc.Unsafe.getUnsafe();
                Class k = HashEntry.class;
                // 得到next成员在对象中的偏移量，用来进行链接操作
                // 利用UnSafe类可以通过直接操作内存来提高速度
                nextOffset = UNSAFE.objectFieldOffset
                    (k.getDeclaredField("next"));
            } catch (Exception e) {
                throw new Error(e);
            }
        }
    }

		// 使用volatile获取给定表的第i个元素，直接通过偏移地址读取
    @SuppressWarnings("unchecked")
    static final <K,V> HashEntry<K,V> entryAt(HashEntry<K,V>[] tab, int i) {
        return (tab == null) ? null :
        		// getObjectVolatile()根据tab对象和偏移长度获得对应的属性
        		// 这里就是获得数组偏移长度的元素，Volatile保证可见性和有序性
            (HashEntry<K,V>) UNSAFE.getObjectVolatile
            // TSHIFT：数组每个元素的偏移长度，TBASE：数组首地址的偏移量
            // 由这两个参数可以得到该元素的物理地址
            (tab, ((long)i << TSHIFT) + TBASE);
    }

		// 使用volatile写设置给定表的第i个元素。
    static final <K,V> void setEntryAt(HashEntry<K,V>[] tab, int i,
                                       HashEntry<K,V> e) {
        // 通过偏移地址将e放到tab的指定位置
        UNSAFE.putOrderedObject(tab, ((long)i << TSHIFT) + TBASE, e);
    }

    /**
			*对给定的hashCode应用一个附加的散列函数
			*防止低质量的哈希函数。这是至关重要的
			因为ConcurrentHashMap使用2的幂长度哈希表，
			否则会遇到不存在的哈希码冲突
			*上下位不同。
     */
    private int hash(Object k) {
        int h = hashSeed;

        if ((0 != h) && (k instanceof String)) {
            return sun.misc.Hashing.stringHash32((String) k);
        }
				// 保证key不是null
        h ^= k.hashCode();
        
        h += (h <<  15) ^ 0xffffcd7d;
        h ^= (h >>> 10);
        h += (h <<   3);
        h ^= (h >>>  6);
        h += (h <<   2) + (h << 14);
        return h ^ (h >>> 16);
    }

    /**
		*段是哈希表的特殊版本。这
		* ReentrantLock的子类，只是为了
		简化一些锁，避免单独构造。
     */
    static final class Segment<K,V> extends ReentrantLock implements Serializable {
        /*
					段维护一个条目列表表，它总是
					*保持一致的状态，所以可以读取(通过volatile)
					*读取段和表)没有锁定。这
					*需要在表中复制节点
					*调整大小，以便读者可以遍历旧列表
					*仍然使用旧版本的表格。
         *
					这个类只定义需要锁的可变方法。
					类的方法执行
					* ConcurrentHashMap方法的每个段版本。(其他
					*方法直接集成到ConcurrentHashMap中
					*方法。)这些突变方法使用一种受控形式
					*通过方法scanAndLock和
					* scanAndLockForPut。这些点缀着
					*遍历以定位节点。主要的好处是吸收
					缓存未命中(这在哈希表中很常见
         */

        private static final long serialVersionUID = 2249069246763182397L;

        /**
					*在前一个预编译器中尝试锁定的最大次数
					*可能在获取时阻塞，为锁定做准备
					*段操作。在多处理器上，使用有界的
					*重试次数维护在定位时获取的缓存
					*节点。
         */
        // 在强制加锁前的最大尝试次数
        // availableProcessors()返回java虚拟机的可用处理器数
        static final int MAX_SCAN_RETRIES =     		
            Runtime.getRuntime().availableProcessors() > 1 ? 64 : 1;

				// 每个分段锁里有一个数组
        transient volatile HashEntry<K,V>[] table;
        // 数组结点数
        transient int count;

        /**
					*这一段的突变操作总数。
					*虽然这可能会溢出32位，但它提供了
					* CHM isEmpty()中稳定性检查的准确性
					*和size()方法。只能在锁或
					*其他保持可见性的volatile读操作。
         */
        transient int modCount;
        // 扩容阈值
        transient int threshold;
        // 加载因子
        final float loadFactor;

        Segment(float lf, int threshold, HashEntry<K,V>[] tab) {
            this.loadFactor = lf;
            this.threshold = threshold;
            this.table = tab;
        }
        
        final V put(K key, int hash, V value, boolean onlyIfAbsent) {
        		// tryLock()方法是有返回值的，它表示用来尝试获取锁，如果获取成功，
        		// 则返回true，如果获取失败（即锁已被其他线程获取），则返回false，
        		// 也就说这个方法无论如何都会立即返回。在拿不到锁时不会一直在那等待。
            HashEntry<K,V> node = tryLock() ? null :
                scanAndLockForPut(key, hash, value);
            V oldValue;
            try {
                HashEntry<K,V>[] tab = table;
                // 取得数组下标
                int index = (tab.length - 1) & hash;
                // 得到链表首结点
                HashEntry<K,V> first = entryAt(tab, index);
                // 查找是否有相同的key，若有，则根据onlyIfAbsent来确定是否进行替换
                for (HashEntry<K,V> e = first;;) {
                    if (e != null) {
                        K k;
                        // key相同或满足equals条件
                        if ((k = e.key) == key ||
                            (e.hash == hash && key.equals(k))) {
                            oldValue = e.value;
                            // onlyIfAbsent: 如果key不存在才增加
                            if (!onlyIfAbsent) {
                                e.value = value;
                                // 修改次数加1
                                ++modCount;
                            }
                            break;
                        }
                        // 如果当前结点不为空，查询下一结点
                        e = e.next;
                    }
                    // 链表为空，或到了末结点并未找到目标key
                    else {
                    		// 结点不为空，说明scanAndLockForPut()有返回值
                        if (node != null)
                        		// 结点前插法，hashmap1.7也用的前插法
                            node.setNext(first);
                        // 第一句中tryLock()成功
                        else
                        		// 前插添加结点，将first作为node的下一结点
                            node = new HashEntry<K,V>(hash, key, value, first);
                        int c = count + 1;
                        // 达到扩容阈值且未到最大容量，进行扩容
                        if (c > threshold && tab.length < MAXIMUM_CAPACITY)
                            rehash(node);
                        // 将新的头结点组成的链表放到数组中
                        else
                            setEntryAt(tab, index, node);
                        ++modCount;
                        count = c;
                        oldValue = null;
                        break;
                    }
                }
            // 保证无论出现任何情况都要解锁
            } finally {
                unlock();
            }
            return oldValue;
        }

				// 扩容，因为本方法在锁中进行，所以线程安全，避免死循环
        @SuppressWarnings("unchecked")
        private void rehash(HashEntry<K,V> node) {
            HashEntry<K,V>[] oldTable = table;
            int oldCapacity = oldTable.length;
            // 在原有容量条件限制下，扩容1倍
            int newCapacity = oldCapacity << 1;
            // 新的扩容阈值
            threshold = (int)(newCapacity * loadFactor);
            // 创建新的数组
            HashEntry<K,V>[] newTable =
                (HashEntry<K,V>[]) new HashEntry[newCapacity];
            int sizeMask = newCapacity - 1;
            // 遍历数组，每一条链都进行转移
            for (int i = 0; i < oldCapacity ; i++) {
            		// 得到原数组下标的首结点
                HashEntry<K,V> e = oldTable[i];
                // 如果链表不为空，则转移
                if (e != null) {
                    HashEntry<K,V> next = e.next;
                    // 重新定位在新数组中的下标
                    int idx = e.hash & sizeMask;
                    // 链表只有一个结点
                    if (next == null)
                        newTable[idx] = e;
                    // 如果有多个结点，则将所有的结点都进行转移
                    else {
                    		// 保存上一个结点
                        HashEntry<K,V> lastRun = e;
                        // 保存上一个结点的下标
                        int lastIdx = idx;
                        // 遍历链表        
                        for (HashEntry<K,V> last = next;
                             last != null;
                             last = last.next) {
           									// 每个结点都需要重定位
                            int k = last.hash & sizeMask;
                            // 这里仅对下标做了比较，意味着虽然有多个结点但lastRun一直没变
                            if (k != lastIdx) {
                                lastIdx = k;
                                lastRun = last;
                            }
                        }
                        newTable[lastIdx] = lastRun;
                        // 上面对下标做了标记，所以这里仅仅转移了一条链
                        for (HashEntry<K,V> p = e; p != lastRun; p = p.next) {
                            V v = p.value;
                            int h = p.hash;
                            // 重定位
                            int k = h & sizeMask;
                            // 前插法
                            HashEntry<K,V> n = newTable[k];
                            newTable[k] = new HashEntry<K,V>(h, p.key, v, n);
                        }
                    }
                }
            }
            // 将新结点加到链表上
            int nodeIndex = node.hash & sizeMask;
            node.setNext(newTable[nodeIndex]);
            newTable[nodeIndex] = node;
            table = newTable;
        }

        // 方法作用：创建新结点或找到key相同的结点并加锁，为添加做准备
        private HashEntry<K,V> scanAndLockForPut(K key, int hash, V value) {
        		// 找到该hashcode对应的链表的头结点
            HashEntry<K,V> first = entryForHash(this, hash);
            HashEntry<K,V> e = first;
            HashEntry<K,V> node = null;
            int retries = -1; // 定位节点为负
            // 循环获取锁，线程安全
            while (!tryLock()) {
                HashEntry<K,V> f; // 请在下面重新检查
                if (retries < 0) {
                		// 链表为空或遍历完链表未找到key相同的结点
                    if (e == null) {
                    		// 在最下面的else if中可能会将retries置为-1
                    		// 所以还可能再次进入这里，需要判断node是否为空
                    		// 这里虽然创建了新的结点，但是并没有链在链表上，e依然为空             		
                        if (node == null) // 创建新结点
                            node = new HashEntry<K,V>(hash, key, value, null);
                        retries = 0;
                    }
                    // 如果键相同，就不用new新结点
                    else if (key.equals(e.key))
                        retries = 0;
                    // 未到末结点，且不为当前结点，则查询下一个
                    else
                        e = e.next;
                }
                // 下面的代码出现情况：1、new出新结点 2、找到key相同的结点
                
                // 如果扫描次数大于阈值，则强制获取锁
                else if (++retries > MAX_SCAN_RETRIES) {
                		// 加锁，若获取不了锁，则会阻塞
                    lock();
                    break;
                }
                // (retries & 1) == 0，当最低位不为1时成立			
								// (retries & 1) == 0，没有这一句将可能会造成死循环
								// 死循环：如果上面的if总是将retries置为0，而MAX_SCAN_RETRIES >= 1
								// 若没有(retries & 1) == 0限制，那下面总是进行重置扫描，即死循环
                else if ((retries & 1) == 0 &&
                				 // 如果链表发生变化，代表先于当前线程的线程对链表进行了修改
                				 // 一旦链表关系发生变化，重新遍历查询加锁是必然的
                         (f = entryForHash(this, hash)) != first) {
                    e = first = f;
                    retries = -1;
                }
            }
            return node;
        }

        private void scanAndLock(Object key, int hash) {
        		// 定位目标下标的首结点
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

				// 链表删除的基本操作
        final V remove(Object key, int hash, Object value) {
            if (!tryLock())
            		// 加锁
                scanAndLock(key, hash);
            V oldValue = null;
            try {
                HashEntry<K,V>[] tab = table;
                int index = (tab.length - 1) & hash;
                // 找到目标下标的首结点
                HashEntry<K,V> e = entryAt(tab, index);
                // 前一结点
                HashEntry<K,V> pred = null;
                while (e != null) {
                    K k;
                    HashEntry<K,V> next = e.next;
                    if ((k = e.key) == key ||
                        (e.hash == hash && key.equals(k))) {
                        V v = e.value;
                        // 如果值为null或值相等
                        if (value == null || value == v || value.equals(v)) {
                        		// 当前结点为头结点
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
            		// 加锁
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
			*获取给定段数组(如果非空)的第j个元素
			* volatile元素通过不安全访问语义。(零检查
			*只会在反序列化过程中无害地触发。)注意:
			*因为段数组的每个元素只设置一次(使用
			*完全有序写)，一些性能敏感的方法依赖
			*仅在读取空值时重新检查此方法。
     */
    // 获取段下标
    @SuppressWarnings("unchecked")
    static final <K,V> Segment<K,V> segmentAt(Segment<K,V>[] ss, int j) {
        long u = (j << SSHIFT) + SBASE;
        return ss == null ? null :
            (Segment<K,V>) UNSAFE.getObjectVolatile(ss, u);
    }

		// 确认段，若没有该段，则创建
    @SuppressWarnings("unchecked")
    private Segment<K,V> ensureSegment(int k) {
        final Segment<K,V>[] ss = this.segments;
        // 获得物理地址，SSHIFT：Segment元素的偏移量，SBASE：ss的第一个元素的地址
        long u = (k << SSHIFT) + SBASE;
        Segment<K,V> seg;
        // 通过物理地址得到的对象为空时，条件成立
        if ((seg = (Segment<K,V>)UNSAFE.getObjectVolatile(ss, u)) == null) {
        		// 在构造函数中对segments数组的第一个元素进行了初始化
        		// 因此第一个元素可以作为模板对其它的空段进行初始化
            Segment<K,V> proto = ss[0];
            int cap = proto.table.length;
            float lf = proto.loadFactor;
            int threshold = (int)(cap * lf);
            HashEntry<K,V>[] tab = (HashEntry<K,V>[])new HashEntry[cap];
            // 再次检查该段是否为空
            if ((seg = (Segment<K,V>)UNSAFE.getObjectVolatile(ss, u))
                == null) {
                Segment<K,V> s = new Segment<K,V>(lf, threshold, tab);
                while ((seg = (Segment<K,V>)UNSAFE.getObjectVolatile(ss, u))
                       == null) {
                    // cas操作，当目标段为空时，才进行替换，否则不替换
                    // 这个判断可以防止两个线程同时到这里出现替换两次的情况
                    // 若一个线程完成了替换，则另一个线程在下一次get时条件不成立退出循环
                    if (UNSAFE.compareAndSwapObject(ss, u, null, seg = s))
                        break;
                }
            }
        }
        return seg;
    }

    // 基于哈希的段和条目访问

    @SuppressWarnings("unchecked")
    private Segment<K,V> segmentForHash(int h) {
        long u = (((h >>> segmentShift) & segmentMask) << SSHIFT) + SBASE;
        return (Segment<K,V>) UNSAFE.getObjectVolatile(segments, u);
    }

		// 获取给定段和散列的表项
    @SuppressWarnings("unchecked")
    static final <K,V> HashEntry<K,V> entryForHash(Segment<K,V> seg, int h) {
        HashEntry<K,V>[] tab;
        return (seg == null || (tab = seg.table) == null) ? null :
            (HashEntry<K,V>) UNSAFE.getObjectVolatile
            // (tab.length - 1) & h 求数组下标
            (tab, ((long)(((tab.length - 1) & h)) << TSHIFT) + TBASE);
    }

    /* ---------------- Public operations -------------- */

    @SuppressWarnings("unchecked")
    public ConcurrentHashMap(int initialCapacity,
                             float loadFactor, int concurrencyLevel) {
        if (!(loadFactor > 0) || initialCapacity < 0 || concurrencyLevel <= 0)
            throw new IllegalArgumentException();
        // concurrencyLevel：并发水平，即，Segment分段数，不能超过最大段数
        if (concurrencyLevel > MAX_SEGMENTS)
            concurrencyLevel = MAX_SEGMENTS;
        int sshift = 0;
        int ssize = 1;
        while (ssize < concurrencyLevel) {
            ++sshift;
            ssize <<= 1;
        }
        // segmentShift：hash值中段所占的位数
        // 若concurrencyLevel = 16，则segmentShift = 27
        this.segmentShift = 32 - sshift;
        // 段的数量-1，用来通过hash值算目标段的下标
        this.segmentMask = ssize - 1;
        // 数组大小不能超过最大容量
        if (initialCapacity > MAXIMUM_CAPACITY)
            initialCapacity = MAXIMUM_CAPACITY;
        // c : 每个段需要的哈希表的大小
        int c = initialCapacity / ssize;
        // 如果initialCapacity / ssize为浮点数，需要向上扩展
        if (c * ssize < initialCapacity)
            ++c;
        // 每个段表的数组最小容量为2
        int cap = MIN_SEGMENT_TABLE_CAPACITY;
        // 扩大得到满足要求的cap
        while (cap < c)
            cap <<= 1;

				// 虽然创建了段的数组，但是只实例化了第一个元素，类似于懒加载
        Segment<K,V> s0 =
            new Segment<K,V>(loadFactor, (int)(cap * loadFactor),
                             (HashEntry<K,V>[])new HashEntry[cap]);
        Segment<K,V>[] ss = (Segment<K,V>[])new Segment[ssize];
        // 将s0放到ss数组的第一个元素位置
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

		// 以默认的加载因子和同步水平进行初始化
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
        		// 得到对应下标的段
            Segment<K,V> seg = segmentAt(segments, j);
            // 如果段和个数都不为空，则有值
            if (seg != null) {
                if (seg.count != 0)
                    return false;
                sum += seg.modCount;
            }
        }
        // TODO
        if (sum != 0L) { // 重新检查，除非没有修改
            for (int j = 0; j < segments.length; ++j) {
                Segment<K,V> seg = segmentAt(segments, j);
                if (seg != null) {
                    if (seg.count != 0)
                        return false;
                    sum -= seg.modCount;
                }
            }
            // 修改数改变了，表明对原哈希表进行了操作
            // 代表哈希表不为空
            if (sum != 0L)
                return false;
        }
        return true;
    }

    public int size() {
        final Segment<K,V>[] segments = this.segments;
        int size;
        boolean overflow; // 如果大小溢出32位，则为真
        long sum;         // modCounts之和
        long last = 0L;   // 之前的总和
        int retries = -1; // 第一次迭代不是重试
        try {
            for (;;) {
            		// 加锁前重试次数，RETRIES_BEFORE_LOCK = 2，重试3次
            		// 当不加锁得不到理想的结果时，强制加锁进行size的统计
                if (retries++ == RETRIES_BEFORE_LOCK) {
                		// 对每一个段都进行加锁
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
                        // 溢出
                        if (c < 0 || (size += c) < 0)
                            overflow = true;
                    }
                }
                // 重试，一直到统计期间没有别的线程对哈希表进行操作
                // 当遍历两次的sum相同，说明没有别的线程进行干涉，可以返回值
                if (sum == last)
                    break;
                last = sum;
            }
        } finally {
        		// 只有加锁了才有必要进行解锁
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
        // 得到key的散列值
        int h = hash(key);
        // h >>> segmentShift : 绝对右移，相当于hash值的高n位参加了段的定位
        long u = (((h >>> segmentShift) & segmentMask) << SSHIFT) + SBASE;
        // 如果该下标的段不为空且段中的数组不为空则条件成立
        if ((s = (Segment<K,V>)UNSAFE.getObjectVolatile(segments, u)) != null &&
            (tab = s.table) != null) {
            // UNSAFE.getObjectVolatile()得到该数组物理地址下得对象
            for (HashEntry<K,V> e = (HashEntry<K,V>) UNSAFE.getObjectVolatile
            				 // (tab.length - 1) & h : 使用全部散列值进行元素的下标定位
            				 // TSHIFT ：哈希表每个元素的偏移量，TBASE ：哈希表第一个元素的偏移量
                     (tab, ((long)(((tab.length - 1) & h)) << TSHIFT) + TBASE);
                 e != null; e = e.next) {
                K k;
                if ((k = e.key) == key || (e.hash == h && key.equals(k)))
                    return e.value;
            }
        }
        return null;
    }

		// 与get()方法几乎一致
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
            		// 原理与size()方法一致
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
                                		// 如果找到，不判断是否同步，直接跳出循环
                                    found = true;
                                    // 普通break只能跳出一层循环，这里可以跳出两层
                                    break outer;
                                }
                            }
                        }
                        sum += seg.modCount;
                    }
                }
                // 如果没有找到，才会判断是否同步，若不同步，则重试
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

		// put()方法，若存在原始的键值对，则替换值，不存在直接添加
		// key、value都不能为null
    @SuppressWarnings("unchecked")
    public V put(K key, V value) {
        Segment<K,V> s;
        // 值不能为null
        if (value == null)
            throw new NullPointerException();
        int hash = hash(key);
        // 通过散列值的高n位来确定段下标
        int j = (hash >>> segmentShift) & segmentMask;
        // 根据物理地址来取得目标段
				// SSHIFT ：Segment数组每个元素的偏移量，SBASE ：Segment数组第一个元素的偏移量
				// 由上面两个偏移量可以算出目标下标元素的物理地址
        if ((s = (Segment<K,V>)UNSAFE.getObject          
             (segments, (j << SSHIFT) + SBASE)) == null)
            // 确认该段存在，若不存在，该方法会进行初始化
            s = ensureSegment(j);
        // false : boolean onlyIfAbsent
        return s.put(key, hash, value, false);
    }

		// 若存在原始的键值对，则不替换值，不存在直接添加
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

    // 将目标map的所有映射添加到本对象中
    public void putAll(Map<? extends K, ? extends V> m) {
        for (Map.Entry<? extends K, ? extends V> e : m.entrySet())
            put(e.getKey(), e.getValue());
    }

    public V remove(Object key) {
        int hash = hash(key);
        // 根据散列值取得目标段
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
					*将nextEntry设置为下一个非空表的第一个节点
					*(为了简化检查，按倒序排列)。
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
            lastReturned = e; // 在执行空检查之前不能赋值
            // 如果当前结点的下一结点为空，则需要再次找到非空结点
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
			由EntryIterator.next()使用的自定义入口类
			*更改基础映射的setValue。
     */
    final class WriteThroughEntry
        extends AbstractMap.SimpleEntry<K,V>
    {
        WriteThroughEntry(K k, V v) {
            super(k,v);
        }

        /**
					*设置我们的条目的值并写入到地图。的
					*返回的值在这里是任意的。自
					* WriteThroughEntry不一定跟踪异步
					*更改，最近的“以前”值可以是
					与我们所返回的不同(甚至可能是不同的)
					*已移除，此时put将重新建立)。我们不
					不能保证更多。
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
            // 数组第一个元素的偏移量，就是首地址
            TBASE = UNSAFE.arrayBaseOffset(tc);
            SBASE = UNSAFE.arrayBaseOffset(sc);
            // 数组中每个元素的大小，对象（指针）大小
            ts = UNSAFE.arrayIndexScale(tc);
            ss = UNSAFE.arrayIndexScale(sc);
            // 获得属性在对象中的偏移地址
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
        // 元素长度必须是2的指数幂，数组元素是对象（指针）
        // (ss & (ss-1)) != 0 保证二进制位中只有一个1，其余全为0
        if ((ss & (ss-1)) != 0 || (ts & (ts-1)) != 0)
            throw new Error("data type scale not a power of two");
        
        // numberOfLeadingZeros(ss)，从高位到低位第一个1的0的个数
        // 0000 0100 结果为5
        SSHIFT = 31 - Integer.numberOfLeadingZeros(ss);
        TSHIFT = 31 - Integer.numberOfLeadingZeros(ts);
    }

}
