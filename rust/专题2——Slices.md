## Slices
另一个没有所有权的数据类型是 slice。slice 允许你引用集合中一段连续的元素序列，而不用引用整个集合。

### 字符串 slice

```
let s = String::from("hello world");

let hello = &s[0..5];
let world = &s[6..11];
```
![引用部分 String 的字符串 slice](https://rust-lang.budshome.com/img/trpl04-06.svg)

> 注意：字符串 slice range 的索引必须位于有效的 UTF-8 字符边界内，如果尝试从一个多字节字符的中间位置创建字符串 slice，则程序将会因错误而退出。


### 字符串字面值

字符串字面值就是 slice，其类型是 &str：它是一个指向二进制程序特定位置的 slice。

字面值和 String 的 slice 作为参数的区别：
```
fn first_word(s: &String) -> &str {
```
与
```
fn first_word(s: &str) -> &str {
```
如果有一个字符串 slice，可以直接传递它。如果有一个 String，则可以传递整个 String 的 slice。定义一个获取字符串 slice 而不是 String 引用的函数使得我们的 API 更加通用并且不会丢失任何功能。


### 其它类型的 slice

```
let a = [1, 2, 3, 4, 5];

let slice = &a[1..3];
```
