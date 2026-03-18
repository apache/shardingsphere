+++
title = "Fuzzy query for CipherColumn | ShardingSphere 5.3.0 Deep Dive"
weight = 84
chapter = true 

+++

![img](https://shardingsphere.apache.org/blog/img/2022_12_28_Fuzzy_query_for_CipherColumn__ShardingSphere_5.3.0_Deep_Dive1.png)

# 1. Background

[Apache ShardingSphere](https://shardingsphere.apache.org/) supports data encryption. By parsing users' SQL input and rewriting the SQL according to the users' encryption rules, the original data is encrypted and stored with ciphertext data in the underlying database at the same time.

When a user queries the data, it only fetches the ciphertext data from the database, decrypts it, and finally returns the decrypted original data to the user. However, because the encryption algorithm encrypts the whole string, fuzzy queries cannot be achieved.

Nevertheless, many businesses still need fuzzy queries after the data is encrypted. [In version 5.3.0](https://medium.com/faun/shardingsphere-5-3-0-is-released-new-features-and-improvements-bf4d1c43b09b?source=your_stories_page-------------------------------------), Apache ShardingSphere provides users with a default fuzzy query algorithm, supporting the fuzzy query for encrypted fields. The algorithm also supports hot plugging, which can be customized by users, and the fuzzy query can be achieved through configuration.

# **2. How to achieve fuzzy query in encrypted scenarios?**

## 2.1 Load data to the in-memory database (IMDB)

Load all the data into the IMDB to decrypt it; then it'll be like querying the original data. This method can achieve fuzzy queries. If the amount of data is small, this method will prove to be simple and cost-effective, while on the other hand, if the amount of data is large, it'll turn out to be a disaster.

## 2.2 Implement encryption & decryption functions consistent with database programs

The second method is to modify fuzzy query conditions and use the database decryption function to decrypt data first and then implement fuzzy query. This method's advantage is the low implementation & development cost, as well as use cost.

Users only need to slightly modify the previous fuzzy query conditions. However, the ciphertext and encryption functions are stored together in the database, which cannot cope with the problem of account data leaks.

```sql
Native SQL: select * from user where name like "%xxx%" 
After implementing the decryption function: ѕеlесt * frоm uѕеr whеrе dесоdе(namе) lіkе "%ххх%"
```

## 2.3 Store after data masking

Implement data masking on ciphertext and then store it in a fuzzy query column. This method could lack in terms of precision.

```markdown
For example, mobile number 13012345678 becomes 130****5678 after the masking algorithm is performed.
```

## 2.4 Perform encrypted storage after tokenization and combination

This method performs tokenization and combination on ciphertext data and then encrypts the resultset by grouping characters with fixed length and splitting a field into multiple ones. For example, we take four English characters and two Chinese characters as a query condition:

`ningyu1` uses the 4-character as a group to encrypt, so the first group is `ning`, the second group `ingy`, the third group `ngyu`, the fourth group `gyu1`, and so on. All the characters are encrypted and stored in the fuzzy query column. If you want to retrieve all data that contains four characters, such as `ingy`, encrypt the characters and use a key `like"%partial%"` to query.

**Shortcomings:**

1. Increased storage costs: free grouping will increase the amount of data and the data length will increase after being encrypted.
2. Limited length in fuzzy query: due to security issues, the length of free grouping cannot be too short, otherwise it will be easily cracked by the [rainbow table](https://www.techtarget.com/whatis/definition/rainbow-table). Like the example I mentioned above, the length of fuzzy query characters must be greater than or equal to 4 letters/digits, or 2 Chinese characters.

## 2.5 Single-character digest algorithm (default fuzzy query algorithm provided in ShardingSphere [version 5.3.0](https://medium.com/faun/shardingsphere-5-3-0-is-released-new-features-and-improvements-bf4d1c43b09b?source=your_stories_page-------------------------------------))

Although the above methods are all viable, it's only natural to wonder if there's a better alternative out there. In our community, we find that single-character encryption and storage can balance both performance and query, but fails to meet security requirements.

Then what's the ideal solution? Inspired by masking algorithms and cryptographic hash functions, we find that data loss and one-way functions can be used.

The cryptographic hash function should have the following four features:

1. For any given message, it should be easy to calculate the hash value.
2. It should be difficult to infer the original message from a known hash value.
3. It should not be feasible to modify the message without changing the hash value.
4. There should only be a very low chance that two different messages produce the same hash value.

**Security:** because of the one-way function, it's not possible to infer the original message. In order to improve the accuracy of the fuzzy query, we want to encrypt a single character, but it will be cracked by the rainbow table.

So we take a one-way function (to make sure every character is the same after encryption) and increase the frequency of collisions (to make sure every string is 1: N backward), which greatly enhances security.

# 3. Fuzzy query algorithm

Apache ShardingSphere implements a universal fuzzy query algorithm by using the below single-character digest algorithm `org.apache.shardingsphere.encrypt.algorithm.like.CharDigestLikeEncryptAlgorithm`.

```java
public final class CharDigestLikeEncryptAlgorithm implements LikeEncryptAlgorithm<Object, String> {
  
    private static final String DELTA = "delta";
  
    private static final String MASK = "mask";
  
    private static final String START = "start";
  
    private static final String DICT = "dict";
  
    private static final int DEFAULT_DELTA = 1;
  
    private static final int DEFAULT_MASK = 0b1111_0111_1101;
  
    private static final int DEFAULT_START = 0x4e00;
  
    private static final int MAX_NUMERIC_LETTER_CHAR = 255;
  
    @Getter
    private Properties props;
  
    private int delta;
  
    private int mask;
  
    private int start;
  
    private Map<Character, Integer> charIndexes;
  
    @Override
    public void init(final Properties props) {
        this.props = props;
        delta = createDelta(props);
        mask = createMask(props);
        start = createStart(props);
        charIndexes = createCharIndexes(props);
    }
  
    private int createDelta(final Properties props) {
        if (props.containsKey(DELTA)) {
            String delta = props.getProperty(DELTA);
            try {
                return Integer.parseInt(delta);
            } catch (NumberFormatException ex) {
                throw new EncryptAlgorithmInitializationException("CHAR_DIGEST_LIKE", "delta can only be a decimal number");
            }
        }
        return DEFAULT_DELTA;
    }
  
    private int createMask(final Properties props) {
        if (props.containsKey(MASK)) {
            String mask = props.getProperty(MASK);
            try {
                return Integer.parseInt(mask);
            } catch (NumberFormatException ex) {
                throw new EncryptAlgorithmInitializationException("CHAR_DIGEST_LIKE", "mask can only be a decimal number");
            }
        }
        return DEFAULT_MASK;
    }
  
    private int createStart(final Properties props) {
        if (props.containsKey(START)) {
            String start = props.getProperty(START);
            try {
                return Integer.parseInt(start);
            } catch (NumberFormatException ex) {
                throw new EncryptAlgorithmInitializationException("CHAR_DIGEST_LIKE", "start can only be a decimal number");
            }
        }
        return DEFAULT_START;
    }
  
    private Map<Character, Integer> createCharIndexes(final Properties props) {
        String dictContent = props.containsKey(DICT) && !Strings.isNullOrEmpty(props.getProperty(DICT)) ? props.getProperty(DICT) : initDefaultDict();
        Map<Character, Integer> result = new HashMap<>(dictContent.length(), 1);
        for (int index = 0; index < dictContent.length(); index++) {
            result.put(dictContent.charAt(index), index);
        }
        return result;
    }
  
    @SneakyThrows
    private String initDefaultDict() {
        InputStream inputStream = CharDigestLikeEncryptAlgorithm.class.getClassLoader().getResourceAsStream("algorithm/like/common_chinese_character.dict");
        LineProcessor<String> lineProcessor = new LineProcessor<String>() {
  
            private final StringBuilder builder = new StringBuilder();
  
            @Override
            public boolean processLine(final String line) {
                if (line.startsWith("#") || line.isEmpty()) {
                    return true;
                } else {
                    builder.append(line);
                    return false;
                }
            }
  
            @Override
            public String getResult() {
                return builder.toString();
            }
        };
        return CharStreams.readLines(new InputStreamReader(inputStream, Charsets.UTF_8), lineProcessor);
    }
  
    @Override
    public String encrypt(final Object plainValue, final EncryptContext encryptContext) {
        return null == plainValue ? null : digest(String.valueOf(plainValue));
    }
  
    private String digest(final String plainValue) {
        StringBuilder result = new StringBuilder(plainValue.length());
        for (char each : plainValue.toCharArray()) {
            char maskedChar = getMaskedChar(each);
            if ('%' == maskedChar) {
                result.append(each);
            } else {
                result.append(maskedChar);
            }
        }
        return result.toString();
    }
  
    private char getMaskedChar(final char originalChar) {
        if ('%' == originalChar) {
            return originalChar;
        }
        if (originalChar <= MAX_NUMERIC_LETTER_CHAR) {
            return (char) ((originalChar + delta) & mask);
        }
        if (charIndexes.containsKey(originalChar)) {
            return (char) (((charIndexes.get(originalChar) + delta) & mask) + start);
        }
        return (char) (((originalChar + delta) & mask) + start);
    }
  
    @Override
    public String getType() {
        return "CHAR_DIGEST_LIKE";
    }
}
```

- Define the binary `mask` code to lose precision `0b1111_0111_1101` (mask).
- Save common Chinese characters with disrupted order like a `map` dictionary.
- Obtain a single string of `Unicode` for digits, English, and Latin.
- Obtain `index` for a Chinese character belonging to a dictionary.
- Other characters fetch the `Unicode` of a single string.
- Add `1 (delta)` to the digits obtained by different types above to prevent any original text from appearing in the database.
- Then convert the offset `Unicode` into binary and perform the `AND` operation with `mask`, and carry out a 2-bit digit loss.
- Directly output digits, English, and Latin after the loss of precision.
- The remaining characters are converted to decimal and output with the common character `start` code after the loss of precision.

# **4. The fuzzy algorithm development progress**

## 4.1 The first edition

Simply use `Unicode` and `mask` code of common characters to perform the `AND` operation.

```yaml
Mask: 0b11111111111001111101
The original character: 0b1000101110101111讯
After encryption: 0b1000101000101101設
Assuming we know the key and encryption algorithm, the original string after a backward pass is:1.0b1000101100101101 謭
2.0b1000101100101111 謯
3.0b1000101110101101 训
4.0b1000101110101111 讯
5.0b1000101010101101 読
6.0b1000101010101111 誯
7.0b1000101000101111 訯
8.0b1000101000101101 設
```

We find that based on the missing bits, each string can be derived `2^n` Chinese characters backward. When the `Unicode` of common Chinese characters is decimal, their intervals are very large. Notice that the Chinese characters inferred backward are not common characters, and it's more likely to infer the original characters.

![img](https://shardingsphere.apache.org/blog/img/2022_12_28_Fuzzy_query_for_CipherColumn__ShardingSphere_5.3.0_Deep_Dive2.png)

## 4.2 The second edition

Since the interval of common Chinese characters `Unicode` is irregular, we planned to leave the last few bits of Chinese characters `Unicode` and convert them into decimal as `index` to fetch some common Chinese characters. This way, when the algorithm is known, uncommon characters won't appear after a backward pass, and distractors are no longer easy to eliminate.

If we leave the last few bits of Chinese characters `Unicode`, it has something to do with the relationship between the accuracy of fuzzy query and anti-decryption complexity. The higher the accuracy, the lower the decryption difficulty.

Let's take a look at the collision degree of common Chinese characters under our algorithm:

1. When `mask`=0b0011_1111_1111:

![img](https://shardingsphere.apache.org/blog/img/2022_12_28_Fuzzy_query_for_CipherColumn__ShardingSphere_5.3.0_Deep_Dive3.png)

2. When `mask`=0b0001_1111_1111:

![img](https://shardingsphere.apache.org/blog/img/2022_12_28_Fuzzy_query_for_CipherColumn__ShardingSphere_5.3.0_Deep_Dive4.png)

For the mantissa of Chinese characters, leave 10 and 9 digits. The 10-digit query is more accurate because its collision is much weaker. Nevertheless, if the algorithm and the key are known, the original text of the 1:1 character can be derived backward.

The 9-digit query is less accurate because 9-digit collisions are relatively stronger, but there are fewer 1:1 characters. We find that although we change the collisions regardless of whether we leave 10 or 9 digits, the distribution is very unbalanced due to the irregular Unicode of Chinese characters, and the overall collision probability cannot be controlled.

## 4.3 The third edition

In response to the unevenly distributed problem found in the second edition, we take common characters with disrupted order as the dictionary table.

1. The encrypted text first looks up the `index` in the out-of-order dictionary table. We use the `index` and subscript to replace the `Unicode` without rules.

Use `Unicode` in case of uncommon characters. (Note: evenly distribute the code to be calculated as far as possible.)

2. The next step is to perform the `AND` operation with `mask` and lose 2-bit precision to increase the frequency of collisions.

Let's take a look at the collision degree of common Chinese characters under our algorithm:

1. When `mask`=0b1111_1011_1101:

![img](https://shardingsphere.apache.org/blog/img/2022_12_28_Fuzzy_query_for_CipherColumn__ShardingSphere_5.3.0_Deep_Dive5.png)

2. When `mask`=0b0111_1011_1101:

![img](https://shardingsphere.apache.org/blog/img/2022_12_28_Fuzzy_query_for_CipherColumn__ShardingSphere_5.3.0_Deep_Dive6.png)

When the `mask` leaves 11 bits, you can see that the collision distribution is concentrated at 1:4. When `mask` leaves 10 bits, the number becomes 1:8. At this time, we only need to adjust the number of precision losses to control whether the collision is 1:2, 1:4 or 1:8.

If `mask` is selected as 1, and the algorithm and key are known, there will be a 1:1 Chinese character, because what we calculate at this time is the collision degree of common characters. If we add the missing 4 bits before the 16-bit binary of Chinese characters, the situation becomes `2^5=32` cases.

Since we encrypted the whole text, even if the individual character is inferred backwards, there will be little impact on overall security, and it will not cause mass data leaks. At the same time, the premise of backward pass is to know the algorithm, key, `delta` and dictionary, so it's impossible to achieve from the data in the database.

# **5. How to use fuzzy query**

Fuzzy query requires the configuration of `encryptors`(encryption algorithm configuration), `likeQueryColumn` (fuzzy query column name), and `likeQueryEncryptorName`(encryption algorithm name of fuzzy query column ) in the encryption configuration.

Please refer to the following configuration. Add your own sharding algorithm and data source.

```yaml
dataSources:
  ds_0:
    dataSourceClassName: com.zaxxer.hikari.HikariDataSource
    driverClassName: com.mysql.jdbc.Driver
    standardJdbcUrl: jdbc:mysql://127.0.0.1:3306/test?allowPublicKeyRetrieval=true
    username: root
    password: root
    
rules:
- !ENCRYPT
  encryptors:
    like_encryptor:
      type: CHAR_DIGEST_LIKE
    aes_encryptor:
      type: AES
      props:
        aes-key-value: 123456abc
  tables:
    user:
      columns:
        name:
          cipherColumn: name
          encryptorName: aes_encryptor
          assistedQueryColumn: name_ext
          assistedQueryEncryptorName: aes_encryptor
          likeQueryColumn: name_like
          likeQueryEncryptorName: like_encryptor
        phone:
          cipherColumn: phone
          encryptorName: aes_encryptor
          likeQueryColumn: phone_like
          likeQueryEncryptorName: like_encryptor

props:
  sql-show: true
```

Insert

```sql
Logic SQL: insert into user ( id, name, phone, sex) values ( 1, '熊高祥', '13012345678', '男')
Actual SQL: ds_0 ::: insert into user ( id, name, name_ext, name_like, phone, phone_like, sex) values (1, 'gyVPLyhIzDIZaWDwTl3n4g==', 'gyVPLyhIzDIZaWDwTl3n4g==', '佹堝偀', 'qEmE7xRzW0d7EotlOAt6ww==', '04101454589', '男')
```

Update

```sql
Logic SQL: update user set name = '熊高祥123', sex = '男1' where sex ='男' and phone like '130%'
Actual SQL: ds_0 ::: update user set name = 'K22HjufsPPy4rrf4PD046A==', name_ext = 'K22HjufsPPy4rrf4PD046A==', name_like = '佹堝偀014', sex = '男1' where sex ='男' and phone_like like '041%'
```

Select

```sql
Logic SQL: select * from user where (id = 1 or phone = '13012345678') and name like '熊%'
Actual SQL: ds_0 ::: select `user`.`id`, `user`.`name` AS `name`, `user`.`sex`, `user`.`phone` AS `phone`, `user`.`create_time` from user where (id = 1 or phone = 'qEmE7xRzW0d7EotlOAt6ww==') and name_like like '佹%'
```

Select: federated table sub-query

```sql
Logic SQL: select * from user LEFT JOIN user_ext on user.id=user_ext.id where user.id in (select id from user where sex = '男' and name like '熊%')
Actual SQL: ds_0 ::: select `user`.`id`, `user`.`name` AS `name`, `user`.`sex`, `user`.`phone` AS `phone`, `user`.`create_time`, `user_ext`.`id`, `user_ext`.`address` from user LEFT JOIN user_ext on user.id=user_ext.id where user.id in (select id from user where sex = '男' and name_like like '佹%')
```

Delete

```sql
Logic SQL: delete from user where sex = '男' and name like '熊%'
Actual SQL: ds_0 ::: delete from user where sex = '男' and name_like like '佹%'
```

The above example demonstrates how fuzzy query columns rewrite SQL in different SQL syntaxes to support fuzzy queries.

This blog post introduced you to the working principles of fuzzy query and used specific examples to demonstrate how to use it. We hope that through this article, you will have gained a basic understanding of fuzzy queries.

# Links

🔗 [Download Link](https://shardingsphere.apache.org/document/current/en/downloads/)

🔗 [Project Address](https://shardingsphere.apache.org/)

🔗 [ShardingSphere-on-Cloud](https://github.com/apache/shardingsphere-on-cloud)

# Author

Xiong Gaoxiang, an engineer at [Iflytek](https://global.iflytek.com/) and a ShardingSphere Contributor, is responsible for the data encryption and data masking R&D.
