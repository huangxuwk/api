package java.util;
import java.io.*;

/**
*基于哈希表实现的Map接口。这
*实现提供了所有可选的map操作和许可证
*null值和null键。(< tt > HashMap < / tt >
*class大致相当于Hashtable，只是它是
*不同步，允许为空。)这个类不能保证
*地图的次序;特别是，它不能保证订单
*会随着时间保持不变。
 *
*此实现为basic提供了常量时间性能
*运算(get and put)，假设哈希函数
*将元素适当地分散到各个桶中。迭代
*收集视图所需时间与“容量”成正比
* HashMap实例(桶的数量)加上它的大小(数量)
*键值映射)。因此，不要设置初始值是非常重要的
*如果迭代性能过高(或负载因子过低)
*重要。
 *
*一个HashMap的实例有两个影响它的参数
*性能:初始容量， 负载因数。的
* capacity是哈希表中桶的数量，为初始桶数
*容量就是创建哈希表时的容量。的
* load factor是对允许哈希表的满度的度量
*在它的容量自动增加之前。当
*哈希表中的项超过了负载因子和的乘积
*当前容量，哈希表为rehashed(即in)
 *
*作为一个通用规则，默认的负载因子(.75)提供了一个很好的权衡
*时间和空间成本。较高的值会减少空间开销
*但增加查找成本(反映在大多数操作的
* HashMap类，包括get和put)。的
*应采用地图中预期的条目数及其负载因子
*在设置其初始容量时考虑，以使
*重哈希操作的数量。如果初始容量更大
*大于最大条目数除以负载因子
*
*如果许多映射要存储在HashMap实例中，
*创建一个足够大的容量将允许映射到
*比让它执行自动重哈希更有效地存储
*需要增加表。
 *
*注意这个实现不是同步的。
*如果多个线程同时访问一个散列映射，且至少一个
*线程在结构上修改映射，它必须 be
*外部同步。结构修改是指任何操作
*添加或删除一个或多个映射;只是改变了值
*与实例已包含的键关联的不是
*结构修改。)这通常是由
*同步一些对象，自然封装映射。
 *
*如果不存在此类对象，则应使用
* {@link Collections#synchronizedMap Collections.synchronizedMap}
*方法。这最好在创建时完成，以防止意外
*对映射的非同步访问:
 *
**这个类的所有“集合视图方法”返回的迭代器
**是fail-fast:如果在之后的任何时间对映射进行了结构修改
*迭代器被创建，除了通过迭代器自己的方式
* 删除方法，迭代器将抛出一个
* {@link ConcurrentModificationException}。因此，面对并发
*修改时，迭代器会快速而干净地失败，而不会有风险
*的任意、不确定的行为
*未来。
 *
*注意，不能保证迭代器的故障-快速行为
*一般来说，我们不可能做出任何严格的保证
*存在不同步的并发修改。快速失败迭代器
*尽最大努力抛出ConcurrentModificationException。
*因此，写一个依赖于此的程序是错误的
*正确性异常:迭代器的快速失效行为
*应该只用于检测bug。
 *
 */

public class HashMap<K,V>
    extends AbstractMap<K,V>
    implements Map<K,V>, Cloneable, Serializable
{

    /**
     * 默认的初始容量-必须是2的幂：减少哈希碰撞和提高代码运行效率
     * 减少哈希碰撞：为了让数据均匀分布，所有就用hash%length
     * 提高代码运行效率：将hash%length优化为hash&(length - 1)
     * 因为计算下标时使用的是 hash & (length - 1)
     * hash%length == hash&(length-1) 的前提是length是2的n次方
     */
    static final int DEFAULT_INITIAL_CAPACITY = 1 << 4; // aka 16

    /**
		*如果隐式指定了更高的值，则使用最大容量
		*由任何一个带参数的构造函数。
		*必须是2的幂<= 1<<30。
     */
    static final int MAXIMUM_CAPACITY = 1 << 30;

    // 在构造函数中没有指定时使用的负载因子。
    static final float DEFAULT_LOAD_FACTOR = 0.75f;

    // 一个空的表实例，在表不膨胀时共享。
    static final Entry<?,?>[] EMPTY_TABLE = {};

    /**
     * 表，根据需要调整大小。长度一定是2的幂。
     */
    transient Entry<K,V>[] table = (Entry<K,V>[]) EMPTY_TABLE;

    // 此映射中包含的键-值映射的数目。
    transient int size;

		//如果table == EMPTY_TABLE，那么这是初始容量
		//表将在膨胀时创建。
    int threshold;

    // 哈希表的加载因子。
    final float loadFactor;

    /**
			*此HashMap在结构上修改的次数
			*结构修改是指改变映射的数量
			* HashMap或以其他方式修改其内部结构(例如，
			*重复)。此字段用于对集合视图生成迭代器
			*HashMap失败得很快。(见ConcurrentModificationException)。
     */
     // 修改次数：当增加新数据（不代表修改value），删除数据，或者清空modCount++
    transient int modCount;

    /**
			*映射容量的默认阈值，高于该阈值的可选哈希值
			*用于字符串键。可选的哈希降低了
			*碰撞由于弱哈希码计算字符串键。
			*通过定义系统属性可以覆盖此值
			* {@code jdk.map.althashing.threshold}。属性值为{@code 1}
			*强制在任何时候都使用可选哈希
			* {@code -1}值确保从不使用替代散列。
     */
    static final int ALTERNATIVE_HASHING_THRESHOLD_DEFAULT = Integer.MAX_VALUE;

    /**
     * 保存在VM启动之前无法初始化的值。
     */
    private static class Holder {

        /**
         * 表容量超过该容量时，可切换到使用可选哈希表。
         */
        static final int ALTERNATIVE_HASHING_THRESHOLD;

        static {
        	// 测试结果为null，所以ALTERNATIVE_HASHING_THRESHOLD=ALTERNATIVE_HASHING_THRESHOLD_DEFAULT
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
		*应用于此实例的相关随机值
		*键的哈希码，使哈希冲突更难找到。如果0
		*禁用替代散列。
     */
     
    // 猜测：当hashmap的容量大于int的最大值时，散列度需要通过hashSeed改变 
    transient int hashSeed = 0;

    /**
		*使用指定的初始值构造一个空的HashMap
		*容量和负载因素。
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
        // 临界值临时被赋值为指定容量，后面会计算并改变
        threshold = initialCapacity;
        // 被linkedhashmap重写
        init();
    }

		/**
		*使用指定的初始值构造一个空的HashMap
		*容量和默认负载因子(0.75)。
     */
    public HashMap(int initialCapacity) {
        this(initialCapacity, DEFAULT_LOAD_FACTOR);
    }

    /**
		*使用默认初始容量构造一个空的HashMap
		*(16)和默认的负载因子(0.75)。
     */
    public HashMap() {
        this(DEFAULT_INITIAL_CAPACITY, DEFAULT_LOAD_FACTOR);
    }

    /**
		*的相同映射构造一个新的HashMap
		*指定地图。创建HashMap
		*默认负载因数(0.75)和足够的初始容量
		*保存指定Map中的映射。
     */
    public HashMap(Map<? extends K, ? extends V> m) {
    		// 判读Capacity是否小于默认的16，若是，则选择默认的
        this(Math.max((int) (m.size() / DEFAULT_LOAD_FACTOR) + 1,
                      DEFAULT_INITIAL_CAPACITY), DEFAULT_LOAD_FACTOR);
        // 创建Entry[]数组
        inflateTable(threshold);
				// 遍历所有的键值对，并加到新的map中
        putAllForCreate(m);
    }
		
		
    private static int roundUpToPowerOf2(int number) {
        return number >= MAXIMUM_CAPACITY
                ? MAXIMUM_CAPACITY  // （number - 1）临界情况，number为2的n次方
                : (number > 1) ? Integer.highestOneBit((number - 1) << 1) : 1;
    }

    /**
     * 扩容数组
     */
    private void inflateTable(int toSize) {
        // Find a power of 2 >= toSize
        int capacity = roundUpToPowerOf2(toSize);

				// 临界值最大只能取MAXIMUM_CAPACITY，integer.maxValue
        threshold = (int) Math.min(capacity * loadFactor, MAXIMUM_CAPACITY + 1);
        table = new Entry[capacity];
        // 计算hashSeed，不超过MAXIMUM_CAPACITY就会一直保持为0
        initHashSeedAsNeeded(capacity);
    }

    // internal utilities

    /**
		*子类的初始化钩子。这个方法被调用
		*在所有构造函数和伪构造函数(克隆，readObject)
		* HashMap已初始化，但在任何项已初始化之前
		*被插入。(如果没有这个方法，readObject会
		*需要明确的子类知识。)
     */
    void init() {
    }

    /**
			*初始化哈希掩码值。我们将初始化推迟到
			**我真的需要它。
			*通常返回都为false
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
		*检索对象哈希码，并将一个附加的哈希函数应用于
		*结果哈希，它可以防止低质量的哈希函数。这是
		*关键，因为HashMap使用的是2的幂长度哈希表
		*否则会遇到不存在差异的哈希码冲突
		*更低的比特。注意:空键总是映射到散列0，因此索引为0。
     */
    final int hash(Object k) {
        int h = hashSeed;
        if (0 != h && k instanceof String) {
            return sun.misc.Hashing.stringHash32((String) k);
        }

        h ^= k.hashCode();

				//这个函数确保hashcode只相差
				//每一位的常数倍数都有一个有界
				//冲突数(默认负载因数约为8)。
        h ^= (h >>> 20) ^ (h >>> 12);
        return h ^ (h >>> 7) ^ (h >>> 4);
    }

    static int indexFor(int h, int length) {
        // 断言Integer.bitCount(length) == 1:“长度必须是非0的2次方”;
        return h & (length-1);
    }

		// 返回此映射中的键值映射的数目。
    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public V get(Object key) {
    		// 下标为0的数组保存key为null的数据
        if (key == null)
            return getForNullKey();
        // 通过key来找entry，本质为通过key.hash来找
        Entry<K,V> entry = getEntry(key);

        return null == entry ? null : entry.getValue();
    }

    /**
		*卸载版本的get()来查找空键。空键映射
		*索引为0。这个空例被分割成单独的方法
		*为了表现在两方面最常用
		*操作(get和put)，但与条件句合并
		*别人。
     */
    private V getForNullKey() {
        if (size == 0) {
            return null;
        }
        // 因为是通过e.key == null来比较，所以此下标最多保存一个key = null的数据
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
		*属性中与指定键关联的项
		* HashMap。如果HashMap不包含映射，则返回null
		*钥匙。
     */
    final Entry<K,V> getEntry(Object key) {
        if (size == 0) {
            return null;
        }
				// 计算hash值，定位数组下标
        int hash = (key == null) ? 0 : hash(key);
        for (Entry<K,V> e = table[indexFor(hash, table.length)];
             e != null;
             e = e.next) {
            Object k;
            // key可以是同一对象，也可以是不同的对象，只要equals比较成立即可
            if (e.hash == hash &&
                ((k = e.key) == key || (key != null && key.equals(k))))
                return e;
        }
        return null;
    }

		// 将指定值与此映射中的指定键关联。如果之前的映射包含一个键的映射，则为旧的值被替换。
    public V put(K key, V value) {
    		// 懒加载模式
        if (table == EMPTY_TABLE) {
            inflateTable(threshold);
        }
        // hashmap只会保存一对key为null的数据
        if (key == null)
            return putForNullKey(value);
        int hash = hash(key);
        int i = indexFor(hash, table.length);
        // 如果找到有旧值，则替换并返回旧值
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
        // 在数组中增加一个新的数据
        addEntry(hash, key, value, i);
        // 无旧值返回null
        return null;
    }

    /**
     *卸载版本的put为空键
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
		*使用这个方法代替put by构造函数和
		*伪构造函数(克隆，readObject)。它不会调整表的大小，
		*检查并发症等。它调用createEntry而不是
		* addEntry。
     */
    private void putForCreate(K key, V value) {
        int hash = null == key ? 0 : hash(key);
        int i = indexFor(hash, table.length);

        /**
				*查找已存在的密钥条目。这永远不会发生
				*克隆或反序列化。它只会发生在建设如果
				*输入映射是一个排序后的映射，其排序与w/ =不一致。
         */
        // 简单的替换值
        for (Entry<K,V> e = table[i]; e != null; e = e.next) {
            Object k;
            if (e.hash == hash &&
                ((k = e.key) == key || (key != null && key.equals(k)))) {
                e.value = value;
                return;
            }
        }
				// 没有旧值就增加一个
        createEntry(hash, key, value, i);
    }

    private void putAllForCreate(Map<? extends K, ? extends V> m) {
        for (Map.Entry<? extends K, ? extends V> e : m.entrySet())
            putForCreate(e.getKey(), e.getValue());
    }

    /**
			*将此映射的内容重新散列到一个新数组中
			*大容量。方法时自动调用此方法
			*此映射中的键数达到其阈值。
			*
			*如果当前容量为MAXIMUM_CAPACITY，则此方法无效
			*调整映射的大小，但将threshold设置为Integer.MAX_VALUE。
			*这样可以防止以后的调用。
     */
    void resize(int newCapacity) {
        Entry[] oldTable = table;
        int oldCapacity = oldTable.length;
        if (oldCapacity == MAXIMUM_CAPACITY) {
            threshold = Integer.MAX_VALUE;
            return;
        }
				// 细节：这里并没有调用inflateTable()来使capacity变为2的指数
				// 因为只有在原非空table基础上，才会进行resize操作，而原table一定符合capacity的条件
        Entry[] newTable = new Entry[newCapacity];
        // 第二个参数通常为false
        transfer(newTable, initHashSeedAsNeeded(newCapacity));
        table = newTable;
        threshold = (int)Math.min(newCapacity * loadFactor, MAXIMUM_CAPACITY + 1);
    }

    /**
     * 将所有条目从当前表转移到新表。
     */
    // 多线程可能会发生循环链表
    void transfer(Entry[] newTable, boolean rehash) {
        int newCapacity = newTable.length;
        for (Entry<K,V> e : table) {
            while(null != e) {
                Entry<K,V> next = e.next;
                // 一般情况都不用重新计算hash值，而是用以前的
                if (rehash) {
                    e.hash = null == e.key ? 0 : hash(e.key);
                }
                int i = indexFor(e.hash, newCapacity);
                // 链表前插法
                e.next = newTable[i];
                newTable[i] = e;
                e = next;
            }
        }
    }

    /**
		*将指定映射的所有映射复制到此映射。
		*这些映射将替换此映射的所有映射
		*当前在指定映射中的任何键。
     */
    public void putAll(Map<? extends K, ? extends V> m) {
        int numKeysToBeAdded = m.size();
        if (numKeysToBeAdded == 0)
            return;

        if (table == EMPTY_TABLE) {
            inflateTable((int) Math.max(numKeysToBeAdded * loadFactor, threshold));
        }

        /*
				*如果要添加映射的数量，则展开映射
				大于或等于阈值。这是保守的;的
				*明显条件是(m.size() + size) >=阈值，但这个
				*条件可能导致地图的容量是适当容量的两倍，
				*如果要添加的键与此映射中已经存在的键重叠。
				通过使用保守的计算，我们对自己进行了检验
				*最多一次额外调整大小。
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
	*		如果存在，则从此映射中删除指定键的映射。
     */
    public V remove(Object key) {
        Entry<K,V> e = removeEntryForKey(key);
        return (e == null ? null : e.value);
    }

    /**
	**		删除并返回与指定键关联的项
	*		在HashMap中。如果HashMap不包含映射，则返回null
			*这把钥匙。
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
                		// 当数据为链表头结点时
                    table[i] = next;
                else
                		// 当数据不为链表头结点时，需将前一节点的next指向删除节点的后一结点
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
			*使用{@code map . EntrySet .equals()}删除EntrySet的特殊版本
			*匹配。
	*		删除的操作都是同一手法，普通链表的删除
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
		*从该映射中删除所有映射。
		*调用返回后映射将为空。
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
		*如果此映射将一个或多个键映射到。则返回true
		*指定值。
		*
		* @param值，该值在此映射中的存在将被测试
		* @return true如果此映射将一个或多个键映射到
		*指定值
     */
    public boolean containsValue(Object value) {
        if (value == null)
            return containsNullValue();

				// 复制引用，所以更改table，也会影响这里的判断
				// 双重循环遍历整个数组和元素链表，与key一样equals比较
				// 所以引用可以不同，若table中有一个或多个value相同时，返回真
        Entry[] tab = table;
        for (int i = 0; i < tab.length ; i++)
            for (Entry e = tab[i] ; e != null ; e = e.next)
                if (value.equals(e.value))
                    return true;
        return false;
    }

    /**
     * 带有空参数的containsValue的特殊情况代码
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
		*返回这个HashMap实例:键和
		*没有克隆值本身。
     */
    public Object clone() {
        HashMap<K,V> result = null;
        try {
        		// 使用的是Object的native方法
        		// 申请新空间将源对象内容复制过去，因此对象内的引用也会复制
            result = (HashMap<K,V>)super.clone();
        } catch (CloneNotSupportedException e) {
            // assert false;
        }
        // table.length <= hashMap.MAXIMUM_CAPACITY,所以第二层取min存在的意义？
        if (result.table != EMPTY_TABLE) {
            result.inflateTable(Math.min(
                (int) Math.min(
                		// loadFactor不能小于0.25，如果loadFactor过小，会出现capacity过大的情况
                		// 也是一种空间和效率的综合选择吧
                		// 扩容条件之一为：size >= threshold
                		// threshold = capacity * loadFactor ==> capacity = size * 1/loadFactor
                    size * Math.min(1 / loadFactor, 4.0f),
                    // we have limits...
                    // 不能超过MAXIMUM_CAPACITY
                    HashMap.MAXIMUM_CAPACITY),
               // 如果loadFactor过小，那么size*min < length，不能过于浪费空间
               table.length));
        }
        result.entrySet = null;
        result.modCount = 0;
        result.size = 0;
        result.init();
        // 把本对象的数组元素复制给result
        result.putAllForCreate(this);

        return result;
    }

    static class Entry<K,V> implements Map.Entry<K,V> {
    		// key 不能变，很好理解
        final K key;
        V value;
        // 维护链表
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
            // getKey()为了封装？
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
				*每当条目中的值为时调用此方法
				*通过调用已存在的键k的put(k,v)覆盖
				*在HashMap中。
         */
        void recordAccess(HashMap<K,V> m) {
        }

        /**
				*每当条目被调用时，都会调用此方法
				*从表中移除。
         */
        void recordRemoval(HashMap<K,V> m) {
        }
    }

    /**
			*将具有指定键、值和散列代码的新项添加到
			*指定的桶。这是我们的责任
			*方法来适当地调整表的大小。
     *
     * 子类覆盖此方法以更改put方法的行为。
     */
    void addEntry(int hash, K key, V value, int bucketIndex) {
        if ((size >= threshold) && (null != table[bucketIndex])) {
        		// table原本就是2的指数次方，所以直接乘2扩容，指数加一
            resize(2 * table.length);
            hash = (null != key) ? hash(key) : 0;
            bucketIndex = indexFor(hash, table.length);
        }

        createEntry(hash, key, value, bucketIndex);
    }

    /**
		*类似于addEntry，只是在创建条目时使用了这个版本
		*作为地图构建或“伪构建”的一部分(克隆、
		*反序列化)。这个版本不必担心调整表的大小。
     *
		*子类覆盖它来改变HashMap(Map)的行为，
		*克隆，和readObject。
     */
		// 为createEntry()提供服务
		// 头插法
    void createEntry(int hash, K key, V value, int bucketIndex) {
        Entry<K,V> e = table[bucketIndex];
        table[bucketIndex] = new Entry<>(hash, key, value, e);
        // 只有确定table中没有key才会进行到这一步，所以一定是新值
        size++;
    }

    private abstract class HashIterator<E> implements Iterator<E> {
        Entry<K,V> next;        // next entry to return 下一项返回
        int expectedModCount;   // For fast-fail
        int index;              // current slot
        Entry<K,V> current;     // current entry

        HashIterator() {
        		// 初始化iterator时就确定了modCount
            expectedModCount = modCount;
            if (size > 0) { // advance to first entry 
                Entry[] t = table;
                // 找到数组中第一个非空的元素，next为第一个有效元素
                while (index < t.length && (next = t[index++]) == null)
                    ;
            }
        }

        public final boolean hasNext() {
            return next != null;
        }

        final Entry<K,V> nextEntry() {
        		// 确认产生iterator时与当前修改状态是否一致
            if (modCount != expectedModCount)
                throw new ConcurrentModificationException();
            Entry<K,V> e = next;
            if (e == null)
                throw new NoSuchElementException();
						
						// 在hashmap中，有效元素的下标不一定是连续的，因此还需要找到下一个有效的元素
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
            // iterator中的remove会更新modify状态，不会产生修改异常
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

    // 子类覆盖这些来改变视图的iterator()方法的行为
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
		*返回包含在此映射中的键的{@link Set}视图。
		* set是由map支持的，所以对map的更改是
		*反映在集合中，反之亦然。如果地图被修改
		*对集合的迭代正在进行中(除了through)
		*迭代器自身的删除操作)，结果为
		*迭代没有定义。集合支持元素移除，
		*方法从映射中删除对应的映射
		* < tt >迭代器。删除< / tt >, < tt > Set.remove < / tt >,
     */
    public Set<K> keySet() {
        Set<K> ks = keySet;
        // 懒加载
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
		*返回包含在此映射中的值的{@link Collection}视图。
		*集合由映射支持，所以对映射的更改是
		*反映在集合中，反之亦然。如果地图是
		*在对集合进行迭代时进行修改
		*(除了通过迭代器自身的删除操作)，
		*迭代的结果是未定义的。集合
		*支持元素移除，移除对应的元素
		*通过Iterator.remove，
		* < tt >集合。删除< / tt >, < tt > removeAll
     */
    // 键 和 Entry 不能相同，所以必须用set
    // 而 value 可以相同，所以可以用collection
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
		*返回包含在这个映射中的{@link Set}视图。
		* set是由map支持的，所以对map的更改是
		*反映在集合中，反之亦然。如果地图被修改
		*对集合的迭代正在进行中(除了through)
		*迭代器自身的删除操作，或通过
		* setValue操作对一个地图条目的返回
		*迭代的结果是未定义的。一组
		*支持元素移除，移除对应的元素
		*通过迭代器.rem从映射映射
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
            Entry<K,V> candidate（候选人） = getEntry(e.getKey());
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
     * 将HashMap实例的状态保存到流中(即，
			*序列化)。
     *
     * @serialData HashMap的容量
			* bucket数组)被发送(int)，后面跟着
			* size (an int, the number of key-value
			*映射)，然后是键(对象)和值(对象)
			*用于每个键值映射。键值映射是
			*无特定顺序发出。
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

        // capacity chosen by number of mappings and desired（渴望，期望） load (if >= 0.25)
        int capacity = (int) Math.min(
                    mappings * Math.min(1 / loadFactor, 4.0f),
                    // we have limits...
                    HashMap.MAXIMUM_CAPACITY);

        // allocate the bucket array; 分配bucket数组
        if (mappings > 0) {
            inflateTable(capacity);
        } else {
        		// threshold = 0，没有映射，所以可以为空
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

    // 这些方法用于序列化哈希集
    int   capacity()     { return table.length; }
    float loadFactor()   { return loadFactor;   }
}
