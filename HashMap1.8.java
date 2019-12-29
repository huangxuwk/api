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
	*基于哈希表实现的Map接口。这
	*实现提供了所有可选的map操作和许可证
	* null值和null键。(< tt > HashMap < / tt >
	class大致相当于Hashtable，只是它是
	*不同步，允许为空。)这个类不能保证
	*地图的次序;特别是，它不能保证订单
	会随着时间保持不变。
 *
	此实现为basic提供了常量时间性能
	*运算(get and put)，假设哈希函数
	*将元素适当地分散到各个桶中。迭代
	*收集视图所需时间与“容量”成正比
	* HashMap实例(桶的数量)加上它的大小(数量)
	*键值映射)。因此，不要设置初始值是非常重要的
	*如果迭代性能过高(或负载因子过低)
	*重要。
 *
	一个HashMap的实例有两个影响它的参数
	*性能:初始容量， 负载因数。的
	* capacity是哈希表中桶的数量，为初始桶数
	*容量就是创建哈希表时的容量。的
	* load factor是对允许哈希表的满度的度量
	*在它的容量自动增加之前。当
	*哈希表中的项超过了负载因子和的乘积
	*当前容量，哈希表是rehashed(即internal)
 *
	作为一般规则，默认的负载系数(.75)提供了一个良好的
	*权衡时间和空间成本。值越高，
	*空间开销，但增加查找成本(反映在大多数
	* HashMap类的操作，包括
	* get， put)。所期望的项数
	*地图及其负载因素应考虑时
	*设置其初始容量，使
	*重复操作。如果初始容量大于
	*最大条目数除以负载因子，没有重哈希
 *
	如果许多映射要存储在HashMap
	例如，创建一个足够大的容量将允许
	*映射将被更有效地存储，而不是让它执行
	*根据需要自动重新哈希表。注意,使用
	*许多带有相同{@code hashCode()}的键肯定会减慢速度
	*降低任何哈希表的性能。改善影响，当关键
	* are {@link Comparable}，这个类可以使用之间的比较顺序
	*帮助打破关系的钥匙。
 *
	注意这个实现不是同步的。
	*如果多个线程同时访问一个散列映射，且至少一个
	*线程在结构上修改映射，它必须 be
	*外部同步。结构修改是指任何操作
	*添加或删除一个或多个映射;只是改变了值
	*与实例已包含的键关联的不是
	*结构修改。)这通常是由
	同步一些对象，自然封装映射。
 *
	*如果不存在此类对象，则应使用
	* {@link Collections#synchronizedMap Collections.synchronizedMap}
	*方法。这最好在创建时完成，以防止意外
	*对映射的非同步访问:

	* Map m =Collections.synchronizedMap(新HashMap (…));
 *
	这个类的所有“集合视图方法”返回的迭代器
	*是fail-fast:如果在之后的任何时间对映射进行了结构修改
	迭代器被创建，除了通过迭代器自己的方式
	* 删除方法，迭代器将抛出一个
	* {@link ConcurrentModificationException}。因此，面对并发
	*修改时，迭代器会快速而干净地失败，而不会有风险
	的任意、不确定的行为未来。
 *
	注意，不能保证迭代器的故障-快速行为
	一般来说，我们不可能做出任何严格的保证
	*存在不同步的并发修改。快速失败迭代器
	*尽最大努力抛出ConcurrentModificationException。
	因此，写一个依赖于此的程序是错误的
	*正确性异常:迭代器的快速失效行为
	*应该只用于检测bug。
 */
public class HashMap<K,V> extends AbstractMap<K,V>
    implements Map<K,V>, Cloneable, Serializable {

    private static final long serialVersionUID = 362498820763181265L;

    /*
     * 实现注意事项。
     *
		*这个映射通常充当一个被绑定(被屏蔽)的哈希表，但是
		*当箱子变得太大时，它们会被转换成
		*树节点，每个节点的结构类似于
		* java.util.TreeMap。大多数方法尝试使用普通的箱子，但是
		*在适用时转发到TreeNode方法(只需检查)
		*节点的instanceof)。可以遍历和
		*像其他的一样使用，但是另外支持更快的查找
		*当人口过剩。然而，由于绝大多数的垃圾箱
		*正常使用不超载，检查是否存在
     *
		*树箱(即元素都是树节点的箱子)
		*主要根据hashCode排序，但如果是tie，则是两个
		*元素是相同的“C类实现可比的<C>”，
		*类型，然后他们的compareTo方法用于排序。(我们
		*通过反射保守地检查泛型类型以进行验证
		* this——参见方法comparableClassFor)。增加了复杂性
		*提供最坏情况O(log n)是值得的
		*当键具有不同的哈希值或为时的操作
		*可排序，因此，性能优雅地下降
     *
		因为树节点的大小是普通节点的两倍
		*只有当箱子中有足够的节点时才使用
		*(见TREEIFY_THRESHOLD)。当它们变得太小的时候(由于
		*删除或调整大小)它们被转换回普通的垃圾箱。在
		*使用分布良好的用户哈希码，树箱是
		*很少使用。理想情况下，在随机哈希码下
		bin中的节点服从泊松分布exp(-0.5) * pow(0.5, k) /factorial(k)
		*默认大小调整的平均参数约为0.5
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
     * 更多:少于1千万分之一
     *
		树状容器的根通常是它的第一个节点。然而,
		有时(当前仅针对Iterator.remove)，根可能
		*在其他地方，但可以通过父链接恢复
		* (TreeNode.root()方法)。
     *
		*所有适用的内部方法都接受哈希码作为
		参数(通常由公共方法提供)，允许
		*在不重新计算用户哈希码的情况下调用。
		也就是说，大多数内部方法也接受“tab”参数
		*通常是当前表，但可能是新表或旧表时
		*调整大小或转换。
     *
		*当bin列表被treeified、split或untreeified时，我们保留
		*它们以相同的相对存取/遍历次序(即、现场
		(下同)为了更好地保存局部，对
		*简化对调用的分割和遍历的处理
		* iterator.remove。当使用比较器进行插入时，要保持a
		*总排序(或相近的是需要在这里)
		* rebalancings，我们将类和identityHashCodes进行比较参加。
     *
		*普通vs树模式之间的使用和转换是
		*由于LinkedHashMap子类的存在而变得复杂。看到
		*下面是定义在插入时调用的钩子方法，
		*删除和访问允许LinkedHashMap内部
		*否则保持独立于这些机制。(这也
		*要求将映射实例传递给一些实用程序方法
		*可能会创建新节点。)
     *
		*基于ssa的并行编程风格很有帮助
		*避免所有扭曲指针操作中的别名错误。
     */

		// 默认的初始容量16
		static final int DEFAULT_INITIAL_CAPACITY = 1 << 4;

		// 默认的最大容量
		static final int MAXIMUM_CAPACITY = 1 << 30;

		// 默认的加载因子，0.75f是时间与空间的取平衡结果
		static final float DEFAULT_LOAD_FACTOR = 0.75f;

		// 链表转成红黑树的阈值，在存储数据时，当链表长度 >= 8时，将链表转换成红黑树
		// jdk作者根据泊松分布，实际测试出：在loadFactor=0.75时
		// 出现一条链上有8个节点的概率为0.00000006；因为转红黑树是代价高昂的
		// 所以在极端情况下再转化为树结构是值得的。
		static final int TREEIFY_THRESHOLD = 8;

		// 红黑树转为链表的阈值，当树结点数 <= 6时，将红黑树转换成链表
		static final int UNTREEIFY_THRESHOLD = 6;

		// 若数组容量 < 64，即使链表长度达到树化阈值，也只扩容而非树化链表
		// 这个比较容易理解，毕竟树化的时间和空间代价是高昂的
		// 为了避免进行扩容、树形化选择的冲突，这个值不能小于 4 * TREEIFY_THRESHOLD
		static final int MIN_TREEIFY_CAPACITY = 64;

    /**
		*基本的哈希bin节点，用于大多数条目。(见下文
		* TreeNode子类，其条目子类在LinkedHashMap中。)
     */
    // 与1.7的Entry一致
    static class Node<K,V> implements Map.Entry<K,V> {
    		// 结点哈希值
        final int hash;
        // 结点key
        final K key;
        // 结点value
        V value;
        // 本结点的下一结点，单向链表
        Node<K,V> next;

        	
        // 构造方法
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
	
				// 替换旧值，oldValue可能是null
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
		*计算key.hashCode()并扩展(XORs)更高的散列位。因为表使用了2的幂的掩蔽，所以集合
		*仅在当前掩码上的位上变化的散列会总是碰撞。(已知的例子包括一组浮点键
		*在小表格中存放连续的整数。)所以我们应用一个变换来分散高比特的影响。
		*在速度、效用和之间有一个权衡点播质量。因为有很多常见的哈希值已经合理分布
     */
    static final int hash(Object key) {
        int h;
        // 将key的hashCode的高16位与低16位进行异或运算，提高散列度
        return (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);
    }

		// 返回x的类，如果它是“C类实现”的形式Comparable<C>"，否则为空。
    static Class<?> comparableClassFor(Object x) {
    		// Comparable是一个接口，只有一个方法：public int compareTo(T o)
        if (x instanceof Comparable) {
            Class<?> c;
            // Type与Class的区别是：Type可以表示带有泛型的类，而Class不能
            Type[] ts, as;
            Type t;
            ParameterizedType p;
            if ((c = x.getClass()) == String.class) // 绕过检查
                return c;
            // c.getGenericInterfaces() : 返回实现接口信息的Type数组，包含泛型接口
            if ((ts = c.getGenericInterfaces()) != null) {
                for (int i = 0; i < ts.length; ++i) {
                    if (((t = ts[i]) instanceof ParameterizedType) && // 该接口是一个合法的类型
                        ((p = (ParameterizedType)t).getRawType() ==  // 获取接口不带参数部分的类型对象
                         Comparable.class) &&
                        (as = p.getActualTypeArguments()) != null &&  // 获取接口的泛型参数数组
                        as.length == 1 && as[0] == c) // 只有一个泛型参数，且该实现类型是该类型本身
                        return c;
                }
            }
        }
        return null;
    }

		//kc: key的类型；k: 目标的key；x: 当前结点的key
    @SuppressWarnings({"rawtypes","unchecked"}) // for cast to Comparable
    static int compareComparables(Class<?> kc, Object k, Object x) {
        return (x == null || x.getClass() != kc ? 0 :
                ((Comparable)k).compareTo(x));
    }

		// 得到合法的容量：大于等于cap的最小2的指数次幂
    static final int tableSizeFor(int cap) {
    		// 防止临界情况，当cap为2的指数次幂时，若不减一，结果为 2 * capacity
        int n = cap - 1;
        n |= n >>> 1;
        n |= n >>> 2;
        n |= n >>> 4;
        n |= n >>> 8;
        n |= n >>> 16;
        // 数组容量不能超过规定最大容量
        return (n < 0) ? 1 : (n >= MAXIMUM_CAPACITY) ? MAXIMUM_CAPACITY : n + 1;
    }

    /* ---------------- Fields -------------- */

		// 保存结点的数组，需要时进行初始化
    transient Node<K,V>[] table;

		// 保存缓存的entrySet()，而keySet()和values()则使用了AbstractMap的属性
    transient Set<Map.Entry<K,V>> entrySet;

		// 结点总数（数组+链表+树结点）
    transient int size;

    // 修改次数：当增加新数据（不代表修改value），删除数据，或者清空时modCount++
    // 与ConcurrentModificationException异常有关，博主在Hashmap1.7中对此进行了讲解
    transient int modCount;

		// 扩容阈值：当size > threshold时，进行扩容
		// threshold = table.length * loadFactor
    int threshold;
		
		// 加载因子
    final float loadFactor;

    /* ---------------- Public operations -------------- */
		
		// 双参构造：容量，加载因子
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
        // 这里与1.7有所不同，1.7是直接将initialCapacity赋值给threshold
        // 1.8通过tableSizeFor()计算出合法的capacity
        this.threshold = tableSizeFor(initialCapacity);
    }
		// 单参构造：容量；调用双参构造，使用默认的加载因子
    public HashMap(int initialCapacity) {
        this(initialCapacity, DEFAULT_LOAD_FACTOR);
    }
		
		// 无参构造，只初始化加载因子
    public HashMap() {
        this.loadFactor = DEFAULT_LOAD_FACTOR;
    }
		
		// 单参构造：map；使用默认的加载因子
    public HashMap(Map<? extends K, ? extends V> m) {
        this.loadFactor = DEFAULT_LOAD_FACTOR;
        putMapEntries(m, false);
    }
    
		// 一、空table 二、在原基础上putAll
    // 调用关系：public void putAll(Map<? extends K, ? extends V> m)
    // public Object clone() || public HashMap(Map<? extends K, ? extends V> m)
    final void putMapEntries(Map<? extends K, ? extends V> m, boolean evict) {
    		// 结点数量
        int s = m.size();
        if (s > 0) {
        		// 数组不存在，需要初始化
            if (table == null) {
            		// 预测大致需要的capacity大小，+1.0F避免ft = 0
            		// +1.0F只有当s / loadFactor = 2的指数次幂时才会扩大2倍
                float ft = ((float)s / loadFactor) + 1.0F;
                int t = ((ft < (float)MAXIMUM_CAPACITY) ?
                         (int)ft : MAXIMUM_CAPACITY);
                         
                // 在调用本方法之前，一定先调用了构造方法
                // 1、单参(参数是map的不算)、双参构造：
                // 一定执行this.threshold = tableSizeFor(initialCapacity)得到了合法容量		
                // t > threshold : 预测容量大于原合法容量，需要根据预测容量得到新合法容量
                // t < threshold : 预测容量小于原合法容量，不用重新规划容量
                // 2、无参构造，参数为map的单参构造：
                // threshold = 0，而 t >= 1，则t > threshold成立，重新规划容量
                if (t > threshold)
                    threshold = tableSizeFor(t);
            }
            // 如果数组不为null，且m的结点数大于扩容阈值，则扩容
            else if (s > threshold)
                resize();
          	
          	// 调用putVal()添加结点，对外的put()方法也是调用它来增加新结点
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

		// 实现了map.get()和相关方法
    final Node<K,V> getNode(int hash, Object key) {
        Node<K,V>[] tab; 
        Node<K,V> first, e; 
        int n;
        K k;
        // table不为null，table.length != 0，数组存在且有效
        if ((tab = table) != null && (n = tab.length) > 0 &&
        		// 简化操作：直接得到下标再得到node
            (first = tab[(n - 1) & hash]) != null) {
            // 总是检查第一个结点；无论是链表还是树，对于第一个结点的检查并无差异
            // 在一定程度上是用代码量挽救检查性能，树中结点的查找代价较大
 						// 与1.7一致，相同对象或满足equals()就可以匹配
            if (first.hash == hash &&  
                ((k = first.key) == key || (key != null && key.equals(k))))
                return first;
            // 若第一个结点不是目标结点，就需要分情况查找
            if ((e = first.next) != null) {
            		// 红黑树：调用红黑树的查找方法
                if (first instanceof TreeNode)
                    return ((TreeNode<K,V>)first).getTreeNode(hash, key);
                // 用do-while隔离操作，while也可以，只不过会多一次判断
                // while(e != null) {e = e.next}，第一次循环中，e != null没有必要
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

		// 若容器中没有此映射，则添加；有，则替换旧值，并返回旧值
    public V put(K key, V value) {
    		//	参数false：改变现有值；参数true：不处于创建模式
        return putVal(hash(key), key, value, false, true);
    }

    // @param onlyIfAbsent 如果为true，则不改变现有值（不替换旧值）
    // @param evict 此参数传递给了空方法，用于LinkedHashMap
    // 实现了map.put()及相关方法
    final V putVal(int hash, K key, V value, boolean onlyIfAbsent,
                   boolean evict) {
        Node<K,V>[] tab; 
        Node<K,V> p; 
        int n, i;
        
        // 懒加载模式，判断数组是否初始化，没有则先初始化
        if ((tab = table) == null || (n = tab.length) == 0)
            n = (tab = resize()).length;
        // 如果该下标中没有任何结点，则直接创建结点
        if ((p = tab[i = (n - 1) & hash]) == null)
        		// null表示本结点的下一结点为空，本结点即为链表末节点
            tab[i] = newNode(hash, key, value, null);
        // 该下标中有结点，可能是链表，也可能是树
        else {
            Node<K,V> e;
            K k;
            // 常规检查第一个，原因与getNode()方法一致
            if (p.hash == hash &&
                ((k = p.key) == key || (key != null && key.equals(k))))
                // 直接复制引用，在最后进行oldValue替换
                e = p;
            // 红黑树
            else if (p instanceof TreeNode)
            		// 1、存在oldTreeNode，直接返回该结点
            		// 2、不存在oldTreeNode，将其加在树上
                e = ((TreeNode<K,V>)p).putTreeVal(this, tab, hash, key, value);
            else {
            		// 遍历链表
                for (int binCount = 0; ; ++binCount) {
                		// 在末节点判断链表的长度才有意义
                    if ((e = p.next) == null) {
                    		// p存在的意义：链表基本操作，保存最末尾节点，用来尾加
                    		// 尾加法增加结点
                        p.next = newNode(hash, key, value, null);
                        // TREEIFY_THRESHOLD = 8，当节点个数 >= 8时转为树结构
                        if (binCount >= TREEIFY_THRESHOLD - 1)
                            treeifyBin(tab, hash);
                        break;
                    }
                    // 找到了key对应的结点
                    if (e.hash == hash &&
                        ((k = e.key) == key || (key != null && key.equals(k))))
                        break;
                    p = e;
                }
            }
            // 存在旧的映射关系
            if (e != null) {
                V oldValue = e.value;
                // onlyIfAbsent控制是否改变现有值，对于1.7是加强
                if (!onlyIfAbsent || oldValue == null)
                    e.value = value;
                // 空方法
                afterNodeAccess(e);
                return oldValue;
            }
        }
        ++modCount;
        // 扩容条件对于1.7有所降低
        // 1.7的条件：(size >= threshold) && (null != table[bucketIndex])
        if (++size > threshold)
            resize();
        // 空方法
        afterNodeInsertion(evict);
        return null;
    }

		// 初始化table，或者扩容，依然是尾加法增加链表
    final Node<K,V>[] resize() {
        Node<K,V>[] oldTab = table;
        int oldCap = (oldTab == null) ? 0 : oldTab.length;
        int oldThr = threshold;
        int newCap, newThr = 0;
				
				// 初始化情况：
				// 1、用户设置了capacity，则oldThr > 0成立
				// 2、用户没有设置capacity(无参构造)，则最后的else成立
				// 非初始化情况：
				// oldCap > 0 成立，newCap必然扩容一倍，而threshold不一定会变
        if (oldCap > 0) {
        		// 超过最大容量则更改临界值，大约是最大容量的两倍，2^31-1 = 2*(2^30)
        		// 如果oldCap >= MAXIMUM_CAPACITY不成立，那么oldCap最大只能是2^29
            if (oldCap >= MAXIMUM_CAPACITY) {
                threshold = Integer.MAX_VALUE;
                return oldTab;
            }
            // choice 1 ：扩容一倍后仍小于最大值 && oldCap >= 16，不一定成立
            else if ((newCap = oldCap << 1) < MAXIMUM_CAPACITY &&
                     oldCap >= DEFAULT_INITIAL_CAPACITY)
                // 如果条件成立，因为线性关系，只用将扩容阈值扩大一倍即可
                newThr = oldThr << 1;
        }
        else if (oldThr > 0)
         		// choice 2 ：使用双参或单参构造方法(不包括参数为map的构造方法)的结果
         		// 初始容量设置为阈值，因为在双参构造方法中将容量赋值给了threshold
         		// this.threshold = tableSizeFor(initialCapacity);
            newCap = oldThr;
        // oldCap == 0 && oldThr == 0
        else { 
        		// 使用的是无参构造方法或参数为map的单参构造方法
            newCap = DEFAULT_INITIAL_CAPACITY;
            newThr = (int)(DEFAULT_LOAD_FACTOR * DEFAULT_INITIAL_CAPACITY);
        }
        // choice 1 不成立或choice 2 成立时，newThr == 0 成立
        if (newThr == 0) {
        		// ft是threshold
            float ft = (float)newCap * loadFactor;
            // 临界情况：newCap > MAXIMUM_CAPACITY && threshold < MAXIMUM_CAPACITY
            // 这里newCap < MAXIMUM_CAPACITY若不成立，那newCap = MAXIMUM_CAPACITY必成立
            newThr = (newCap < MAXIMUM_CAPACITY && ft < (float)MAXIMUM_CAPACITY ?
                      (int)ft : Integer.MAX_VALUE);
        }
        threshold = newThr;
        @SuppressWarnings({"rawtypes","unchecked"})
        // 由于capacity设定的规则，这里newCap最大为MAXIMUM_CAPACITY，所以不用考虑合法问题
        // 当时的疑问：这里前面newCap不合法，这里不检查就新建数组？（但愿下次看到这还能记得，网上没人会？）
            Node<K,V>[] newTab = (Node<K,V>[])new Node[newCap];
        table = newTab;
        if (oldTab != null) {
        		//遍历数组下标，在hash表的存储规则下，结点存储的下标并不连续
            for (int j = 0; j < oldCap; ++j) {
                Node<K,V> e;
                if ((e = oldTab[j]) != null) {
                		// 线程1在if判断之前挂起，线程2到这里，保留了独有的oldTab[j]
                		// 线程1继续执行，发现oldTab[j]为空，跳过此下标的所有元素
                		// 那么原table数组中的数据被分成了两部分，而两个线程有各自的newTab
                		// 都会覆盖table成员，无论两个线程覆盖table的前后顺序，总有一组数据丢失
                		// 这里造成了严重的数据丢失，情况严重的话，可能只会保留原来的一条链或一棵树
                		// 让人不由得觉得这是个神来之笔，为什么要置空oldTab[j]？
                		// e = oldTab[j]语句已经保留链表或树的头结点的引用了，gc回收不了这一块结点集
                		// 而在方法运行结束后，gc会自动回收原来的table引用，所以这不是多此一举吗？
                		// 如果这里不置空，那每个线程转移的都一样，虽然会降低性能，但在这种情况下不会丢失
                    oldTab[j] = null;
                    // 先检查第一个，提高效率
                    if (e.next == null)
                        newTab[e.hash & (newCap - 1)] = e;
                    else if (e instanceof TreeNode)
                    		// 拆散原树形结构，形成新树
                        ((TreeNode<K,V>)e).split(this, newTab, j, oldCap);
                    // 此结点集是链表，下面可能会将原来的一条链变为两条链
                    // 至于为什么可能是两条链，请读者继续向下读
                    else {
                        Node<K,V> loHead = null, loTail = null;
                        Node<K,V> hiHead = null, hiTail = null;
                        Node<K,V> next;
                        // 此处的do-while与getNode()方法中的一致
                        do {
                            next = e.next;
                            // 0000 0101 & 0000 1111 = 0000 0101
                            // 0000 0101 & 0001 1111 = 0000 0101
                            // oldCap = 0001 0000
                            // 检查一下增加的高一位对应的hash值的那一位是否为1
                            // 若为1，则newIndex = oldIndex + oldCap
                            // 若为0，则newIndex = oldIndex
                            // 而这一位是否为1并不一定，有可能每个结点的hash的这一位都是0
                            // 那就只会产生一条链
                            
                            // 原下标形成新链
                            if ((e.hash & oldCap) == 0) {
                            		// 第一个节点时，loTail == null
                                if (loTail == null)
                                    loHead = e;
                                else
                                    loTail.next = e;
                                loTail = e;
                            }
                            // 另一下标形成新链
                            else {
                                if (hiTail == null)
                                    hiHead = e;
                                else
                                    hiTail.next = e;
                                hiTail = e;
                            }
                        } while ((e = next) != null);
                    		// oldIndex下有链，交给数组
                        if (loTail != null) {
                            loTail.next = null;
                            newTab[j] = loHead;
                        }
                        // oldIndex + oldCap下有链，交给数组
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
		
		// 虽转化为了树，但还保留了链表的关系，为了查询value
    final void treeifyBin(Node<K,V>[] tab, int hash) {
        int n, index; Node<K,V> e;
        // 如果程序没有问题，那么tab永远不等于null
        // 如果数组较小，可以通过扩容的方式来减少哈希碰撞，不必转为树
        if (tab == null || (n = tab.length) < MIN_TREEIFY_CAPACITY)
            resize();
        // 得到目标下标的链表，准备转树
        else if ((e = tab[index = (n - 1) & hash]) != null) {
            TreeNode<K,V> hd = null, tl = null;
            do {
            		// 仅仅是调用了Node的构造方法，生成一个个独立的节点
                TreeNode<K,V> p = replacementTreeNode(e, null);
                // 这里将独立的节点变成链表
                if (tl == null)
                    hd = p;
                else {
                    p.prev = tl;
                    // next成员来自HashMap.Node，继承关系
                    tl.next = p;
                }
                tl = p;
            } while ((e = e.next) != null);
            if ((tab[index] = hd) != null)
            		// 真正转为树的方法
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
     
    // @param matchValue 如果为true，则仅在值相等时删除，键值对都必须相等
    // @param movable 如果为false，则在删除时不移动其他节点
    // 实现了map.remove和相关方法
    final Node<K,V> removeNode(int hash, Object key, Object value,
                               boolean matchValue, boolean movable) {
        Node<K,V>[] tab; Node<K,V> p; int n, index;
        // table存在且有效, 且当前下标存有有效节点
        if ((tab = table) != null && (n = tab.length) > 0 &&
            (p = tab[index = (n - 1) & hash]) != null) {
            Node<K,V> node = null, e; K k; V v;
            // 检查第一个结点
            if (p.hash == hash &&
                ((k = p.key) == key || (key != null && key.equals(k))))
                node = p;
            else if ((e = p.next) != null) {
            		// 查找红黑树
                if (p instanceof TreeNode)
                    node = ((TreeNode<K,V>)p).getTreeNode(hash, key);
                else {
                		// 遍历链表，当你看到这你就会发现代码已经开始重复了
                    do {
                        if (e.hash == hash &&
                            ((k = e.key) == key ||
                             (key != null && key.equals(k)))) {
                            node = e;
                            break;
                        }
                        // p 为上一节点
                        p = e;
                    } while ((e = e.next) != null);
                }
            }
            // matchValue = false时，不用进行value的对比
            // 判断value是否相同是1.8新增加的功能，而且可以开闭
            if (node != null && (!matchValue || (v = node.value) == value ||
                                 (value != null && value.equals(v)))) {
                // 树结点
                if (node instanceof TreeNode)
                    ((TreeNode<K,V>)node).removeTreeNode(this, tab, movable);
								// 链表删除                
                else if (node == p)
                		// 满足 node == p 的条件是：第一个节点即为目标节点
                    tab[index] = node.next;
                else
                		// 不为第一个节点，则将上一节点与后一节点相链接
                    p.next = node.next;
                // 修改数加一
                ++modCount;
                --size;
                // 空方法
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
		// 1.7中调用Arrays.fill(tab, null); 两个实质相同，暴力清空，靠gc回收
    public void clear() {
        Node<K,V>[] tab;
        modCount++;
        if ((tab = table) != null && size > 0) {
            size = 0;
            // 遍历数组，将所有下标元素置为null
            for (int i = 0; i < tab.length; ++i)
                tab[i] = null;
        }
    }

    public boolean containsValue(Object value) {
        Node<K,V>[] tab; V v;
        if ((tab = table) != null && size > 0) {
        		// 外层循环，遍历数组
            for (int i = 0; i < tab.length; ++i) {
            		// 内层循环，遍历链表，虽为树，但链表关系还在
            		// 树是以key作为查找的依据，可以优化性能，get()方法使用红黑树自带查询方法
            		// 但查找值，无法优化，必须遍历全部节点，因此直接使用for循环
                for (Node<K,V> e = tab[i]; e != null; e = e.next) {
                    if ((v = e.value) == value ||
                        (value != null && value.equals(v)))
                        return true;
                }
            }
        }
        return false;
    }

		// keySet、values依旧来自AbstractMap抽象类
    public Set<K> keySet() {
        Set<K> ks = keySet;
        if (ks == null) {
            ks = new KeySet();
            keySet = ks;
        }
        return ks;
    }

		// 相比于1.7功能丰富了很多
    final class KeySet extends AbstractSet<K> {
        public final int size()                 { return size; }
        public final void clear()               { HashMap.this.clear(); }
        // iterator的处理手法一致
        public final Iterator<K> iterator()     { return new KeyIterator(); }
        public final boolean contains(Object o) { return containsKey(o); }
        // 在iterator()调用之后，调用remove()方法与调用Hashmap的remove()一样
        // 都会产生ConcurrentModificationException异常，new KeyIterator()已经modCount已经确定
        public final boolean remove(Object key) {
            return removeNode(hash(key), key, null, false, true) != null;
        }
        // 拆分遍历
        public final Spliterator<K> spliterator() {
            return new KeySpliterator<>(HashMap.this, 0, -1, 0, 0);
        }
        
//			new HashMap<String, String>().keySet().forEach(new Consumer<String>() {
//				@Override
//				public void accept(String t) {
//					System.out.println("");
//				}
//			});
				// 提供了一个显式的forEach
        public final void forEach(Consumer<? super K> action) {
            Node<K,V>[] tab;
            if (action == null)
                throw new NullPointerException();
            // table存在结点
            if (size > 0 && (tab = table) != null) {
            		// 在遍历前确定修改数，是为了保证线程安全
                int mc = modCount;
                // 遍历数组，树节点中的链表关系又起到了作用
                for (int i = 0; i < tab.length; ++i) {
                    for (Node<K,V> e = tab[i]; e != null; e = e.next)
                    		// 将结点传递给了action
                        action.accept(e.key);
                }
                // 这里是遍历完成后报出的异常，因此当改变了原table后
                // 上面的遍历结果可能也会变，但下面就会报异常
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
    // 覆盖JDK8映射扩展方法
    @Override
    public V getOrDefault(Object key, V defaultValue) {
        Node<K,V> e;
        // 查找，若没有则返回参数defaultValue
        return (e = getNode(hash(key), key)) == null ? defaultValue : e.value;
    }

    @Override
    public V putIfAbsent(K key, V value) {
    		// 如果存在旧值，不能替换
        return putVal(hash(key), key, value, true, true);
    }

    @Override
    public boolean remove(Object key, Object value) {
    		// 必须在键值对都相等的时候才能删除
        return removeNode(hash(key), key, value, true, true) != null;
    }

    @Override
    public boolean replace(K key, V oldValue, V newValue) {
        Node<K,V> e; V v;
        // 基本的键值匹配
        if ((e = getNode(hash(key), key)) != null &&
            ((v = e.value) == oldValue || (v != null && v.equals(oldValue)))) {
            e.value = newValue;
            // 空方法
            afterNodeAccess(e);
            return true;
        }
        return false;
    }

    @Override
    public V replace(K key, V value) {
        Node<K,V> e;
        // 键匹配即可
        if ((e = getNode(hash(key), key)) != null) {
            V oldValue = e.value;
            e.value = value;
            afterNodeAccess(e);
            return oldValue;
        }
        return null;
    }

		// Function是用户传递进来的动作，就是键值对
		// V v = mappingFunction.apply(key);
		// 1、有结点，且value != null，返回旧值
		// 2、有结点，value == null，覆盖value
		// 3、无结点，加在树上或者普通链表上
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
        // 与put方法神似，put在方法最后判断 size > threshold
        // (tab = table) == null这个判断说明，本方法可以在Hashmap初始化后直接使用
        // 上一个有这个判断的是putVal()
        if (size > threshold || (tab = table) == null ||
            (n = tab.length) == 0)
            n = (tab = resize()).length;
        // 这里查找，为什么不用getNode()?
        if ((first = tab[i = (n - 1) & hash]) != null) {
            if (first instanceof TreeNode)
            		// old为get到的值，t = first
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
            // 如果存在旧值，直接返回旧值，不作操作
            if (old != null && (oldValue = old.value) != null) {
                afterNodeAccess(old);
                return oldValue;
            }
        }
        // 不存在该节点或者，该节点的值为null会执行下面的语句
        V v = mappingFunction.apply(key);
        if (v == null) {
            return null;
        // old.value = null 存在结点：1、树节点 2、普通结点
        } else if (old != null) {
            old.value = v;
            afterNodeAccess(old);
            return v;
        }
        // t != null 不存在该结点，若根节点不是null，则在树上新加一个结点
        else if (t != null)
            t.putTreeVal(this, tab, hash, key, v);
        // 不是树节点，不存在该节点，给链表加一个普通结点
        else {
            tab[i] = newNode(hash, key, v, first);
            // 加上普通结点后，判断是否需要树化
            if (binCount >= TREEIFY_THRESHOLD - 1)
                treeifyBin(tab, hash);
        }
        ++modCount;
        ++size;
        afterNodeInsertion(true);
        return v;
    }

		// V v = remappingFunction.apply(key, oldValue);
		// 1、存在该节点，value == null，返回null
		// 2、存在该结点，value != null，V == null 删除；V != null 替换
		// 存在结点才操作，所以不用空判断
    public V computeIfPresent(K key,
                              BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        if (remappingFunction == null)
            throw new NullPointerException();
        Node<K,V> e; V oldValue;
        int hash = hash(key);
        // 没有上一个方法的空判断，说明这个方法必须在table存在的情况下才能用
        // value != null 覆盖原来的值
        if ((e = getNode(hash, key)) != null &&
            (oldValue = e.value) != null) {
            // 这里需要键值对，上一个仅需要值
            V v = remappingFunction.apply(key, oldValue);
            if (v != null) {
                e.value = v;
                afterNodeAccess(e);
                // 返回newValue
                return v;
            }
            // v == null，oldValue != null 删除该键值对
            else
        				// false 参数：不用值匹配，键匹配就删除
                removeNode(hash, key, null, false, true);
        }
        return null;
    }

		// V v = remappingFunction.apply(key, oldValue);
		// 1、不存在结点，若为树，加树节点；不为树，加链结点
		// 2、存在结点，v == null，删除；v != null 替换
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
       	// 空判断，初始化
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
        // 结点存在与否无所谓
        V oldValue = (old == null) ? null : old.value;
        // 键值对匹配
        V v = remappingFunction.apply(key, oldValue);
        // 存在结点
        if (old != null) {
        		// v != null 替换
            if (v != null) {
                old.value = v;
                afterNodeAccess(old);
            }
            // v == null 删除
            else
                removeNode(hash, key, null, false, true);
        }
        // 不存在结点
        else if (v != null) {
        		// 根节点不为空，新加树结点
            if (t != null)
                t.putTreeVal(this, tab, hash, key, v);
            // 新加普通结点
            else {
                tab[i] = newNode(hash, key, v, first);
                // 判断是否要转树
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
		// 1、不存在结点，若为树，加树节点；不为树，加链结点
		// 2、存在结点，v == null(remappingFunction)，删除；v != null(value) 替换
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
        // 空判断
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
        // 存在结点
        if (old != null) {
            V v;
            if (old.value != null)
                v = remappingFunction.apply(old.value, value);
            // old.value = null
            else
                v = value;
            // 替换
            if (v != null) {
                old.value = v;
                afterNodeAccess(old);
            }
            // v == null 删除
            else
                removeNode(hash, key, null, false, true);
            // 返回新值，或null，null来自remappingFunction
            return v;
        }
        // 无结点，上面异常检测了，value一定不为null
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

		// 显式forEach ConcurrentModificationException
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
		
		// 线程安全检测的替换，可能只是部分
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
		
		// 1.7中的克隆十分的复杂
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
        // 将this的所有映射复制给result，深克隆
        result.putMapEntries(this, false);
        return result;
    }

    // These methods are also used when serializing HashSets
    final float loadFactor() { return loadFactor; }
    final int capacity() {
    		// 空构造时，threshold = 0，所有是默认容量
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
    // iterators 迭代器

    abstract class HashIterator {
        Node<K,V> next;        // next entry to return 下一项返回
        Node<K,V> current;     // current entry 当前结点，就是即将返回的上一结点
        int expectedModCount;  // for fast-fail
        int index;             // current slot

        HashIterator() {
        		// 确定操作数
            expectedModCount = modCount;
            Node<K,V>[] t = table;
            current = next = null;
            index = 0;
            if (t != null && size > 0) { // advance to first entry
            		// 找到数组中第一个非空的元素，next为第一个有效元素
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
            // 在hashmap中，有效元素的下标不一定是连续的，因此还需要找到下一个有效的元素
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
            // iterator中的remove会更新modify状态，不会产生修改异常
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
        int index;                  // current index, modified on advance/split 当前索引，预先修改/分割
        int fence;                  // one past last index 最后一次索引
        int est;                    // size estimate 规模估算
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

        final int getFence() { // 在第一次使用时初始化栅栏和大小
            int hi;
            // 若不规定索引范围，则默认为数组长度
            if ((hi = fence) < 0) {
                HashMap<K,V> m = map;
                est = m.size;
                // 确定操作数
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
        		// 分割成两等份
            int hi = getFence(), lo = index, mid = (lo + hi) >>> 1;
            // 当前index >= mid，current != null说明在遍历中途
            return (lo >= mid || current != null) ? null :
                new KeySpliterator<>(map, lo, index = mid, est >>>= 1,
                                        expectedModCount);
        }
				
				// 遍历剩下的
				// 分割遍历：以数组下标为界，而不是以size（个数）为界
        public void forEachRemaining(Consumer<? super K> action) {
            int i, hi, mc;
            if (action == null)
                throw new NullPointerException();
            HashMap<K,V> m = map;
            Node<K,V>[] tab = m.table;
            // 这里也可以getFence()
            if ((hi = fence) < 0) {
                mc = expectedModCount = m.modCount;
                hi = fence = (tab == null) ? 0 : tab.length;
            }
            else
                mc = expectedModCount;
            // i >= 0 && i < hi (i < fence)，从index开始，到hi结束
            if (tab != null && tab.length >= hi &&
                (i = index) >= 0 && (i < (index = hi) || current != null)) {
                Node<K,V> p = current;
                current = null;
                do {
                    if (p == null)
                    		// 该下标不存在数据，则读取下一下标
                        p = tab[i++];
                    else {
                    		// 该下标存在数据，遍历链表
                        action.accept(p.key);
                        p = p.next;
                    }
                } while (p != null || i < hi);
                // 在循环后判断修改状态
                if (m.modCount != mc)
                    throw new ConcurrentModificationException();
            }
        }

        public boolean tryAdvance(Consumer<? super K> action) {
            int hi;
            if (action == null)
                throw new NullPointerException();
            Node<K,V>[] tab = map.table;
            // 下标最高位(hi / fence)不得大于数组长度
            if (tab != null && tab.length >= (hi = getFence()) && index >= 0) {
                while (current != null || index < hi) {
                    if (current == null)
                        current = tab[index++];
                    else {
                        K k = current.key;
                        current = current.next;
                        action.accept(k);
                        // 在循环内判断修改状态
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
		*下列包保护方法被设计为
		*被LinkedHashMap覆盖，但不被任何其他子类覆盖。
		*几乎所有其他内部方法都是包保护的
		*但被声明为final，所以可以被LinkedHashMap, view使用
		类和HashSet。
     */

    // 创建一个常规(非树)节点
    Node<K,V> newNode(int hash, K key, V value, Node<K,V> next) {
        return new Node<>(hash, key, value, next);
    }

    // 用于从树节点到普通节点的转换
    Node<K,V> replacementNode(Node<K,V> p, Node<K,V> next) {
        return new Node<>(p.hash, p.key, p.value, next);
    }

    // 创建一个树bin节点
    TreeNode<K,V> newTreeNode(int hash, K key, V value, Node<K,V> next) {
        return new TreeNode<>(hash, key, value, next);
    }

    // For treeifyBin
    TreeNode<K,V> replacementTreeNode(Node<K,V> p, Node<K,V> next) {
        return new TreeNode<>(p.hash, p.key, p.value, next);
    }

    // 重置为初始默认状态。被clone()和readObject()调用。
    void reinitialize() {
        table = null;
        entrySet = null;
        keySet = null;
        values = null;
        modCount = 0;
        threshold = 0;
        size = 0;
    }

    // 允许LinkedHashMap后动作的回调
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
		
		// 继承关系：LinkedHashMap.Entry<K,V> extends HashMap.Node<K,V>
    static final class TreeNode<K,V> extends LinkedHashMap.Entry<K,V> {
    		// 红黑树的链接
        TreeNode<K,V> parent;  
        TreeNode<K,V> left;
        TreeNode<K,V> right;
        // 与Node.next形成双向链表
        TreeNode<K,V> prev;		
        boolean red;
        TreeNode(int hash, K key, V val, Node<K,V> next) {
        		// 调用HashMap.Node的构造方法
            super(hash, key, val, next);
        }

				// 返回包含此节点的树的根。
        final TreeNode<K,V> root() {
        		// 一直向上找，总能找个root
            for (TreeNode<K,V> r = this, p;;) {
                if ((p = r.parent) == null)
                    return r;
                r = p;
            }
        }

				// 确保给定根是其bin的第一个节点
				// 完成后，root不仅是链表的头结点，也是树的根，也是数组下标的第一个元素
				// 调用关系：void treeify(Node<K,V>[] tab)
				// void removeTreeNode(HashMap<K,V> map, Node<K,V>[] tab, boolean movable)
				// TreeNode<K,V> putTreeVal(HashMap<K,V> map, Node<K,V>[] tab, int h, K k, V v)
        static <K,V> void moveRootToFront(Node<K,V>[] tab, TreeNode<K,V> root) {
            int n;
            // 空判断
            if (root != null && tab != null && (n = tab.length) > 0) {
            		// 找到树
                int index = (n - 1) & root.hash;
                TreeNode<K,V> first = (TreeNode<K,V>)tab[index];
                // 将root变为第一个结点
                if (root != first) {
                    Node<K,V> rn;
                    tab[index] = root;
                    // root结点的前一结点
                    // void treeifyBin(Node<K,V>[] tab, int hash)保留了原有的链表关系
                    TreeNode<K,V> rp = root.prev;
                    // rn为root的后一结点
                    if ((rn = root.next) != null)
                    		// 将root后一结点的前结点指针指向root的前一结点，略过root
                        ((TreeNode<K,V>)rn).prev = rp;
                    // root的后一结点有值
                    if (rp != null)
                    		// 将root的前一结点的后指针指向root的后一结点，与上面颠倒，互链
                        rp.next = rn;
                    // 传递进来的root找不到对应的树时，first == null
                    if (first != null)
                        first.prev = root;
                    // 这里可能出现root是孤立结点，只有自己
                    root.next = first;
                    root.prev = null;
                }
                // assert [boolean 表达式]
								// 如果[boolean表达式]为true，则程序继续执行。
								// 如果为false，则程序抛出AssertionError，并终止执行。
                assert checkInvariants(root);
            }
        }

				// 使用给定的散列和键查找从根p开始的节点。kc参数在第一次使用时缓存comparableClassFor(key)比较键。
				// 调用关系：getTreeNode(int h, Object k)  
				// putTreeVal(HashMap<K,V> map, Node<K,V>[] tab, int h, K k, V v)
        final TreeNode<K,V> find(int h, Object k, Class<?> kc) {
            TreeNode<K,V> p = this;
            do {
                int ph, dir; K pk;
                TreeNode<K,V> pl = p.left, pr = p.right, q;
                // 当前结点p的hash值大于目标结点的hash值，查找左子树
                if ((ph = p.hash) > h)
                    p = pl;
                // 当前结点p的hash值小于目标结点的hash值，查找右子树
                else if (ph < h)
                    p = pr;
               	// 如果当前结点的key等于目标结点的key，找到目标返回
                else if ((pk = p.key) == k || (k != null && k.equals(pk)))
                    return p;
                // 到这里说明：p.hash == h，但key不相等，无法确认该向哪边深入查找
                else if (pl == null)
                		// 左子树为空，找右子树
                    p = pr;
                else if (pr == null)
                		// 右子树为空，找左子树
                    p = pl;
                // 判断用户是否提供查找的依据：comparable的实现类
                else if ((kc != null ||
                					// 判断该Class是否为comparable的实现类，且泛型参数为实现类自身
                          (kc = comparableClassFor(k)) != null) &&
                         // 实质：(Comparable)k).compareTo(pk) 调用用户的接口实现方法，确定方向
                         (dir = compareComparables(kc, k, pk)) != 0)
                    p = (dir < 0) ? pl : pr;
                // 到这里说明：用户未提供查找依据或提供的查找依据依然无法确认查找方法
                // 无方向的情况下，只能两边都查，先递归查找右子树，找到则返回
                else if ((q = pr.find(h, k, kc)) != null)
                    return q;
                // 右子树未找到，找左子树
                else
                    p = pl;
            } while (p != null);
            return null;
        }

				// 调用find()查找根节点。
        final TreeNode<K,V> getTreeNode(int h, Object k) {
        		// parent != null 当前结点非根节点，必须从根节点开始查找
            return ((parent != null) ? root() : this).find(h, k, null);
        }

				// 树节点插入规则，通过比较hashcode
				// 若用户重写的hashCode()方法不够散列，hashmap为自己留了一条退路：使用原始hashCode()计算
        static int tieBreakOrder(Object a, Object b) {
            int d;
            // 两个结点可为null，若a、b的类型不一致，则d为垃圾数据
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
				*返回与给定对象相同的哈希码，将由默认方法hashCode()返回，
				*无论是否覆盖给定对象的类hashCode()。空引用的哈希码为零。
								
				public static native int identityHashCode(Object x);
	     */

				// 链表-->树
        final void treeify(Node<K,V>[] tab) {
            TreeNode<K,V> root = null;
            // this为链表头结点
            for (TreeNode<K,V> x = this, next; x != null; x = next) {
            		// next是本结点在链表上的下一结点
                next = (TreeNode<K,V>)x.next;
                x.left = x.right = null;
                // 第一个结点会执行这个方法块
                if (root == null) {
                		// 当前结点的父结点置空，当前结点将变为root
                    x.parent = null;
                    // 根节点必须是黑色的
                    x.red = false;
                    root = x;
                }
                // 除根结点外执行
                else {
                    K k = x.key;
                    int h = x.hash;
                    Class<?> kc = null;
                    // 遍历树，找到该插入的位置，与find()方法类似
                    for (TreeNode<K,V> p = root;;) {
                        int dir, ph;
                        K pk = p.key;
                        // p.hash > h : 左子树
                        if ((ph = p.hash) > h)
                            dir = -1;
                        // p.hash < h : 右子树
                        else if (ph < h)
                            dir = 1;
                        // 两个结点hash值相同，但key不同(key肯定不同)
                        else if ((kc == null &&
                        					// kc未实现comparable接口或未按规范实现
                                  (kc = comparableClassFor(k)) == null) ||
                                 // pk == null || pk.getClass() != kc (kc = null)
                                 (dir = compareComparables(kc, k, pk)) == 0)
                            // 尝试用原始的hashCode()方法进行比较
                            dir = tieBreakOrder(k, pk);

                        TreeNode<K,V> xp = p;
                        // 通过dir来选择左右孩子，如果所选子结点为null，则链在上一结点上
                        if ((p = (dir <= 0) ? p.left : p.right) == null) {
                            x.parent = xp;
                            if (dir <= 0)
                                xp.left = x;
                            else
                                xp.right = x;
                            // 每加一个新的结点都要进行自平衡调整
                            root = balanceInsertion(root, x);
                            break;
                        }
                    }
                }
            }
            // 树转换完成后，将根结点作为数组下标元素
            moveRootToFront(tab, root);
        }

				// 树-->链表
        final Node<K,V> untreeify(HashMap<K,V> map) {
            Node<K,V> hd = null, tl = null;
            for (Node<K,V> q = this; q != null; q = q.next) {
            		// return new Node<>(p.hash, p.key, p.value, next);
                Node<K,V> p = map.replacementNode(q, null);
                // 尾追法创建链表
                if (tl == null)
                    hd = p;
                else
                    tl.next = p;
                tl = p;
            }
            return hd;
        }

				// 树版本的putVal。
        final TreeNode<K,V> putTreeVal(HashMap<K,V> map, Node<K,V>[] tab,
                                       int h, K k, V v) {
            Class<?> kc = null;
            boolean searched = false;
            // 找到根节点
            TreeNode<K,V> root = (parent != null) ? root() : this;
            for (TreeNode<K,V> p = root;;) {
                int dir, ph;
                K pk;
                
                //前面的两个判断与find()一致
                if ((ph = p.hash) > h)
                    dir = -1;
                else if (ph < h)
                    dir = 1;
                // 如果存在该结点，直接返回，是否修改value由上层决定
                else if ((pk = p.key) == k || (k != null && k.equals(pk)))
                    return p;
                // 到这里说明：p.hash == h，但key不相等，无法确认该向哪边深入查找    
                else if ((kc == null &&
                          (kc = comparableClassFor(k)) == null) ||
                         // pk == null || pk.getClass() != kc (kc = null)
                         (dir = compareComparables(kc, k, pk)) == 0) {
                    // 开关，每次调用putTreeVal()方法最多查一次
                    // 出现这种情况，说明用户覆盖的hashCode()很差，hashmap只帮你一次
                    if (!searched) {
                        TreeNode<K,V> q, ch;
                        searched = true;
                        // 分别查找左右子树
                        if (((ch = p.left) != null &&
                             (q = ch.find(h, k, kc)) != null) ||
                            ((ch = p.right) != null &&
                             (q = ch.find(h, k, kc)) != null))
                            return q;
                    }
                    dir = tieBreakOrder(k, pk);
                }

                TreeNode<K,V> xp = p;
                // 若找到了末结点，则链上
                if ((p = (dir <= 0) ? p.left : p.right) == null) {
                		// xpn 是 当前目标链接结点的下一个
                    Node<K,V> xpn = xp.next;
                    // 把xpn加在了新结点的后面，相当于插入了一个结点
                    TreeNode<K,V> x = map.newTreeNode(h, k, v, xpn);
                    if (dir <= 0)
                        xp.left = x;
                    else
                        xp.right = x;
                    // 插入新节点
                    xp.next = x;
                    // xpn 与目标结点双向链表
                    x.parent = x.prev = xp;
                    // xpn 与新增结点双向链接
                    if (xpn != null)
                        ((TreeNode<K,V>)xpn).prev = x;
                        
                    // 树自平衡后，根节点可能会变
                    moveRootToFront(tab, balanceInsertion(root, x));
                    return null;
                }
            }
        }

        /**
					*删除指定节点，该节点必须在调用之前出现。这比典型的红黑删除代码更混乱，因为我们
					*不能用叶子交换内部节点的内容被可访问的“next”指针固定的后继指针独立地遍历。
					所以我们交换树联系。如果当前树节点太少，bin被转换回一个普通的bin。(测试触发
					根据树的结构，在2到6个节点之间)。
         */
        final void removeTreeNode(HashMap<K,V> map, Node<K,V>[] tab,
                                  boolean movable) {
            // section 1：通过prev和next删除当前链表中结点
            int n;
            // 空判断
            if (tab == null || (n = tab.length) == 0)
                return;
            // 取下标
            int index = (n - 1) & hash;
            // 取根结点
            TreeNode<K,V> first = (TreeNode<K,V>)tab[index], root = first, rl;
            // succ: 删除结点的下一个，pred：删除结点的上一个
            TreeNode<K,V> succ = (TreeNode<K,V>)next, pred = prev;
            // 没有上一个结点，那么直接跳过删除结点
            if (pred == null)
                tab[index] = first = succ;
            // 有上一结点，则在链表中删除该结点
            else
                pred.next = succ;
            // 有下一结点，则反向链接  
            if (succ != null)
                succ.prev = pred;
            // 1、当前数组下标没有任何数据
            // 2、只有一个结点：根节点，且是删除结点（不可能），如果树只有一个结点，就不会是树
            if (first == null)
                return;
            // section 2：当节点数量小于7时转换成链表的形式存储    
            // 找到根节点，只要线程安全，这里root.parent != null一定不成立
            if (root.parent != null)
                root = root.root();
            // root.right -> 树只有两个结点，三个会旋转
            // 2-6个会变回链 
            if (root == null || root.right == null ||
                (rl = root.left) == null || rl.left == null) {
               	// 删除结点在树链表中已经删除了，可以直接转化为另外一条链表
                tab[index] = first.untreeify(map);
                return;
            }
            // section 3：判断当前树节点情况
            TreeNode<K,V> p = this, pl = left, pr = right, replacement;
            if (pl != null && pr != null) {
            		// s 是删除结点的右子结点
                TreeNode<K,V> s = pr, sl;
                // 遍历右子树中的左结点，直到最后一个左结点
                while ((sl = s.left) != null)
                    s = sl;
                boolean c = s.red; s.red = p.red; p.red = c; // swap colors 交换颜色
                TreeNode<K,V> sr = s.right;
                TreeNode<K,V> pp = p.parent;
                if (s == pr) { // p was s's direct parent p是s的直系父母
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
        // 红黑树方法，全部改编自CLR
					
				// 左旋
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

				// 递归不变量检查，初次t = root
        static <K,V> boolean checkInvariants(TreeNode<K,V> t) {
        		// t = root 时，t.parent = null
            TreeNode<K,V> tp = t.parent, tl = t.left, tr = t.right,
                tb = t.prev, tn = (TreeNode<K,V>)t.next;
            // t的前一结点不为null且前后关系不统一
            if (tb != null && tb.next != t)
                return false;
            // t的后一结点不为null且前后关系不统一
            if (tn != null && tn.prev != t)
                return false;
            // t.parent不为null且parent的左右子结点都不为t，必须为子节点的其中一个
            if (tp != null && t != tp.left && t != tp.right)
                return false;
            // 左孩子不为null且左孩子的父结点不是t或者左孩子的hash值大于父结点
            if (tl != null && (tl.parent != t || tl.hash > t.hash))
                return false;
            // 右孩子不为null且右孩子的父结点不是t或者右孩子的hash值小于父结点
            if (tr != null && (tr.parent != t || tr.hash < t.hash))
                return false;
            // t为红结点，且左右孩子都为红结点
            if (t.red && tl != null && tl.red && tr != null && tr.red)
                return false;
            // 存在左孩子，循环遍历正确性
            if (tl != null && !checkInvariants(tl))
                return false;
            // 存在右孩子，循环遍历正确性
            if (tr != null && !checkInvariants(tr))
                return false;
            return true;
        }
    }

}