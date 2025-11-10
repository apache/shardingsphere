.. _sec:conf_database:

Конфигурация ``database-sharding.yaml``
=======================================

Файл ``database-sharding.yaml`` в ShardingSphere-Proxy предназначен для настройки правил шардирования 
и конфигурации логических баз данных. Его основная цель — описать, как SQL-запросы, приходящие 
в прокси, должны распределяться между физическими базами (шардами) и таблицами.

В этом файле настраиваются следующие параметры:

- Имя логической базы данных (``databaseName``);
- Источники данных (``dataSources``);
- Правила шардирования (``rules``);

Имя логической базы данных (``databaseName``)
--------------------------------------------------

При работе с ShardingSphere-Proxy клиенты не подключаются напрямую к физическим базам данных (шардам).
Вместо этого в конфигурации задаётся логическая база данных:

.. compound::

  .. code-block:: yaml

      databaseName: sharding_db 


  где ``sharding_db`` — это имя логической базы данных в ShardingSphere.

При подключении клиента к Proxy указывается это имя базы данных, а ShardingSphere 
самостоятельно маршрутизирует запросы на соответствующие физические источники данных (шарды).

Таким образом, ``databaseName`` — это «виртуальная» база данных, которая отображается на одну или несколько реальных баз данных 
(указанных в ``dataSources``).

Настройка источников данных (``dataSources``)
-----------------------------------------------------------

В ShardingSphere-Proxy каждый физический шард настраивается через блок ``dataSources``. Допустимо указывать несколько шардов.

ShardingSphere-Proxy может работать с разными пулами соединений к базам данных:

 - ``HikariCP`` — по умолчанию, современный и высокопроизводительный пул.
 - ``C3P0`` — устаревающий, менее производительный. Требует дополнительного плагина.
 - ``DBCP`` — Apache Commons DBCP, также устаревший. Требует отдельного плагина.

Если используется ``C3P0`` или ``DBCP``, необходимо скачать соответствующий плагин из репозитория ``shardingsphere-plugins``.

Параметр ``dataSourceClassName`` позволяет явно указать класс пула соединений. 
Если он не задан, по умолчанию используется ``HikariCP``.

.. code-block:: yaml

    dataSources:              # Настройка источников данных (шардов)
      <data_source_name_1>:   # Имя источника данных
        dataSourceClassName:  # Полное имя класса пула соединений
        url:                  # URL адрес базы данных
        username:             # имя пользователя базы данных
        password:             # пароль пользователя базы данных
        ...                   # Свойства пула соединений
      <data_source_name_2>:   # Имя другого источника данных
        ...                   # Аналогичные настройки

.. rubric:: Пример конфигурации

.. code-block:: yaml

    dataSources:
      ds_1:
        url: jdbc:firebirdsql://localhost:3050//home/databases/shard1.fdb
        username: sysdba
        password: masterkey
        connectionTimeoutMilliseconds: 30000
        idleTimeoutMilliseconds: 60000
        maxLifetimeMilliseconds: 1800000
        maxPoolSize: 1000
        minPoolSize: 1
      ds_2:
        dataSourceClassName: com.mchange.v2.c3p0.ComboPooledDataSource
        url: jdbc:firebirdsql://localhost:3050//home/databases/shard2.fdb
        username: sysdba
        password: masterkey
      ds_3:
        dataSourceClassName: org.apache.commons.dbcp2.BasicDataSource
        url: jdbc:firebirdsql://localhost:3050//home/databases/shard3.fdb
        username: sysdba
        password: masterkey

Полный список параметров ``HikariCP`` можно найти здесь: https://github.com/brettwooldridge/HikariCP#configuration .

Свойства для ``C3P0`` или ``DBCP`` можно найти в их официальной документации.


Настройка правил шардирования (``rules``)
-----------------------------------------------------------

Блок ``rules`` в файле ``database-sharding.yaml`` определяет правила распределения данных, 
маршрутизации SQL-запросов и дополнительные функции, связанные с безопасностью и обработкой данных.

Каждый тип правила задаётся отдельным YAML-тегом:

- ``!SHARDING`` — правила шардирования таблиц и баз данных;
- ``!BROADCAST`` — конфигурация Broadcast-таблиц;
- ``!SINGLE`` — настройка одиночных таблиц;
- ``!READWRITE_SPLITTING`` — разделение операций чтения и записи;
- ``!ENCRYPT`` — правила шифрования данных;
- ``!MASK`` — правила маскирования данных;
- ``!SHADOW`` — правила теневых баз данных.


Тег ``- !SHARDING``
~~~~~~~~~~~~~~~~~~~~

Основной модуль. Описывает, как логические таблицы распределяются по физическим базам 
и таблицам, какие алгоритмы используются, какие таблицы связаны и другое.

Общая структура правила шардирования выглядит так:

.. code-block:: yaml

  rules:
  - !SHARDING
    tables:                         
      <logic_table_name>:           # Имя логической таблицы
        actualDataNodes:            # Описывает узлы данных
        databaseStrategy:           # Стратегия шардинга на уровне базы данных. 
          <стратегия шардирования>  # см. ниже
        tableStrategy:              # Стратегия шардинга для таблицы
          <стратегия шардирования>  # см. ниже
        keyGenerateStrategy:        # Стратегия распределенного первичного ключа 
          column:                   # Имя столбца для автоматической генерации ключей
          keyGeneratorName:         # Имя генератора распределённых первичных ключей
        auditStrategy:              # Стратегия аудитора
          auditorNames:             # Произвольные имена SQL-аудитора 
            - <auditor_name>
            - <auditor_name>
          allowHintDisable: true    # Позволяет отключить аудит через SQL Hint
    autoTables:                     # Автоматическое распределение данных по таблицам
      <logic_table_name>:           # Логическое имя таблицы
        actualDataSources:          # Имена источников данных (шардов)
        shardingStrategy:           # Стратегия шардинта
          standard:                 # если шардинг с одним ключом шардирования
            shardingColumn:         # имя столбца - ключа шардинга
            shardingAlgorithmName:  # Имя алгоритма для автоматического шардинга
    bindingTables:                  # Связанные таблицы
      - <logic_table_name_1, logic_table_name_2, ...>
      - <logic_table_name_1, logic_table_name_2, ...>
    defaultDatabaseStrategy:        # Стратегия шардинга по умолчанию на уровне БД
      <стратегия шардирования>      # см. ниже
    defaultTableStrategy:           # Стратегия шардинга по умолчанию для таблиц
      <стратегия шардирования>      # см. ниже
    defaultKeyGenerateStrategy:     # Стратегия по умолчанию для генератора распред. ПК
      column:                       # Имя столбца для автоматической генерации ключей
      keyGeneratorName:             # Имя генератора распределённых первичных ключей
    defaultShardingColumn:          # имя столбца - ключа шардинга по умолчанию

    # Настройка алгоритмов шардинга
    shardingAlgorithms:
      <sharding_algorithm_name>:    # Имя алгоритма шардинга, используемого в стратегии
        type:                       # Тип алгоритма
        props:                      # Свойства алгоритма
        ...

    # Настройка алоритмов генерации ключей
    keyGenerators:
      <key_generate_algorithm_name>: # Имя генератора распределённых первичных ключей
        type:                        # Тип алгоритма
        props:                       # Свойства алгоритма
        ...

    # Настройка аудиторов
    auditors:
      <auditor_name>:     # Имя аудитора
        type:             # Тип аудитора, выполняющего определенную проверку
        props:            # Свойства определенного типа аудитора
        ...

Основные элементы конфигурации
""""""""""""""""""""""""""""""""""""

``<logic_table_name>``
  *Имя логической таблицы.* 
  
  Клиенты работают именно с логической таблицей, подключаясь через Proxy, не имея 
  представления о физическом распределении данных. 
  
  Определение логической таблицы можно найти в :numref:`подразделе %s<subsubsec:logic_table>` .

  .. code-block:: yaml

    rules:
    - !SHARDING
      tables:
        t_order:              # Имя логической таблицы
          actualDataNodes: ...
          ...

``actualDataNodes``
  *Узлы данных.* Для описания узлов применяется синтаксис ``GROOVY``.

  Определение узла данных можно найти в :numref:`подразделе %s<subsec:data_nodes>` .

  Определение строковых выражений можно найти в :numref:`подразделе %s<subsec:row_expressions>` .

  .. code-block:: yaml

    rules:
    - !SHARDING
      tables:
        t_order:
          actualDataNodes: ds_${0..1}.t_order_${0..9}  # Узлы данных
          databaseStrategy: ...
          ...

``databaseStrategy`` 
  *Стратегия шардинга на уровне базы данных*, которая определяет, как данные распределяются между базами.  

  Это комбинация ключа шардинга (``shardingColumn``) и алгоритма шардинга (``shardingAlgorithmName``).

  Определение стратегии можно найти в :numref:`подразделе %s<subsec:shard_strategy>`.

  .. code-block:: yaml

    <стратегия шардирования> ::=
          standard:                 # стандартная стратегия
            shardingColumn:         # имя столбца - ключа шардинга
            shardingAlgorithmName:  # Имя алгоритма <sharding_algorithm_name>
          complex:                  # составная стратегия
            shardingColumns:        # Имена столбцов - ключей шардинга через запятую
            shardingAlgorithmName:  # Имя алгоритма <sharding_algorithm_name>
          hint:                     # стратегия с подсказками
            shardingAlgorithmName:  # Имя алгоритма <sharding_algorithm_name>
          none:                     # Не шардировать
 
  Если стратегия не задана, применяется стратегия по умолчанию, указанная в параметре ``defaultDatabaseStrategy``.

  В зависимости от используемых алгоритмов, стратегии могут быть следующих типов:

  - *стандартная* - если шардинг с одним ключом шардирования (см. :ref:`subsubsec:standart_algor`) 
  - *составная* - если шардинг с несколькими ключами шардирования (см. :ref:`subsubsec:complex_algor`) 
  - *с подсказками* - если нужен hint - шардинг(см. :ref:`subsubsec:hint_algor`) 

  Из доступных вариантов стратегий шардинга можно выбрать *только одну* для каждой логической таблицы.

  Описание встроенных алгоритмов для каждой стратегии можно найти в следующих параграфах.

  Если имя ключа шардинга ``shardingColumn`` не указано, применяется ключ шардина по умолчанию - ``defaultShardingColumn``. 

  .. code-block:: yaml

    rules:
    - !SHARDING
      tables:
        t_order:
          actualDataNodes: ds_${0..1}.t_order_${0..9}  
          databaseStrategy:            # Стратегия шардинга для базы данных
            standard:                  # Выбрана стандартная стратегия 
              shardingColumn: USER_ID  # Ключ шардинга
              shardingAlgorithmName: database_inline # Имя станд. алгоритма(произв.)
          ...
      defaultDatabaseStrategy: # Стратегия шардинга для базы данных по умолчанию
        standard:                      # Выбрана стандартная стратегия
          shardingColumn: USER_ID      # Ключ шардинга
          shardingAlgorithmName: def_inline # Имя стандарт. алгоритма(произв.)
      
      shardingAlgorithms:
        database_inline:
          type: INLINE     # Встроенный Inline алгоритм
          props:
            algorithm-expression: ds_${USER_ID % 2}  

        def_inline:
          type: INLINE    # Встроенный Inline алгоритм
          props:
            algorithm-expression: ds_$->{USER_ID % 2}

``tableStrategy``              
  *Стратегия шардинга на уровне таблицы*, которая отвечает за выбор физической таблицы в рамках выбранной базы данных.

  Если стратегия отсутствует, применяется стратегия по умолчанию, указанная в параметре ``defaultTableStrategy``.

  В остальном определение параметра ``tableStrategy`` полностью соответствует ``databaseStrategy`` (см. предыдущий пункт).

  .. code-block:: yaml

    rules:
    - !SHARDING
      tables:
        t_order:
          actualDataNodes: ds_${0..1}.t_order_${0..9}  
          tableStrategy:            # Стратегия шардинга для таблицы
            standard:                  # Выбрана стандартная стратегия 
              shardingColumn: ORDER_ID  # Ключ шардинга
              shardingAlgorithmName: T_ORDER_inline # Имя станд. алгоритма(произв.)
          ...
      defaultTableStrategy: # Стратегия шардинга для таблицы по умолчанию
        standard:                      # Выбрана стандартная стратегия
          shardingColumn: ORDER_ID      # Ключ шардинга
          shardingAlgorithmName: defT_inline # Имя стандарт. алгоритма(произв.)
      
      shardingAlgorithms:
        T_ORDER_inline:
          type: INLINE     # Встроенный Inline алгоритм
          props:
            algorithm-expression: T_ORDER_$->{ORDER_ID % 2}  

        defT_inline:
          type: INLINE    # Встроенный Inline алгоритм
          props:
            algorithm-expression: T_ORDER_$->{ORDER_ID % 2}

``keyGenerateStrategy``
  *Стратегия автоматической генерации распределенных первичных ключей.* 

  Описание распределенных первичных ключей
  можно найти в :numref:`подразделе %s<subsec:distributedPK>` .

  Можно задать стратегию по умолчанию: ``defaultKeyGenerateStrategy``.

  Подробнее об алгоритмах генерации распределённых (глобально уникальных) первичных ключей 
  описано в :numref:`подразделе %s<subsec:dist_pk>`.

  .. code-block:: yaml

    rules:
    - !SHARDING
      tables:
        t_order:
          actualDataNodes: ds_$->{0..2}.t_order
          keyGenerateStrategy:
            column: order_id
            keyGeneratorName: sflake
      ...
      keyGenerators:
        sflake:
          type: SNOWFLAKE

``auditStrategy``
  *Включение SQL аудитора*, который выполняет проверку SQL-запросов перед их выполнением.

  Описание аудиторов приведено в :numref:`подразделе %s<subsec:auditors>`.

  Параметр ``allowHintDisable`` управляет возможностью отключения аудита с помощью SQL Hint:

  - ``true`` - аудит можно отключить аудит через SQL Hint;
  - ``false`` - аудит нельзя отключить вручную.

  .. code-block:: yaml

    rules:
    - !SHARDING
      tables:
        T_ORDER:
          actualDataNodes: ds_$->{0..1}.T_ORDER_$->{0..1}
          auditStrategy:              # Стратегия аудитора
            auditorNames:             
              - sharding_key_required_auditor # Имя SQL-аудитора 
            allowHintDisable: true    # Позволяет отключить аудит через SQL Hint
      auditors:
        sharding_key_required_auditor:
          type: DML_SHARDING_CONDITIONS

``autoTables``
  *Настройка автоматически шардируемых таблиц*. 

  Описание можно найти в :numref:`подразделе %s<subsubsec:auto_algor>`.

  При использовании ``autoTables`` необходимо указать только список источников данных (шардов) через параметр 
  ``actualDataSources`` без указания узлов, а ShardingSphere автоматически вычислит и создаст необходимые таблицы в каждом шарде.  

  .. code-block:: yaml

    rules:
    - !SHARDING
      autoTables:
        t_order_auto:                     # логическая таблица
          actualDataSources: ds_0, ds_1   # список источников данных (шардов)
          shardingStrategy:
            standard:
              shardingColumn: order_id    # поле, по которому выполняется шардирование
              shardingAlgorithmName: auto_mod
      shardingAlgorithms:
        auto_mod:
          type: MOD                      # тип алгоритма (деление по остатку)
          props:
            sharding-count: 2            # количество шардов

  Подробнее о встроенных автоматических алгоритмах шардирования см. в разделе :ref:`subsec:buildin_alg_autotables`.
  
``bindingTables``
  *Настройка связанных таблиц*. 

  Подробно о связанных таблицах можно найти в :numref:`подразделе %s<subsubsec:binding_table>`.

  .. code-block:: yaml

    rules:
    - !SHARDING
      tables:
        t_order:
          actualDataNodes: ds_${0..1}.t_order_${0..1}
          tableStrategy: ...
        t_order_item:
          actualDataNodes: ds_${0..1}.t_order_item_${0..1}
          tableStrategy: ...
      bindingTables:
        - t_order, t_order_item


.. _subsec:buildin_alg_autotables:

Встроенные алгоритмы для autoTables
""""""""""""""""""""""""""""""""""""

.. rubric:: ``MOD``

Данные распределеются по таблицам по результатам остатков от деления значений столбца ``shardingColumn``. 
Такой тип алгоритма подходит для числовых, последовательных ключей.

.. tabularcolumns:: |>{\ttfamily\arraybackslash}\X{4}{16}|>{\ttfamily\arraybackslash}\X{2}{16}|>{\arraybackslash}\X{10}{16}|
.. list-table:: Свойства алгоритма ``MOD``
   :class: longtable
   :name: table:mod
   :header-rows: 1

   * - Название свойства
     - Тип данных
     - Описание
   * - sharding-count 
     - int 
     - На сколько частей разбить таблицу
 
.. rubric:: ``HASH_MOD``

``HASH_MOD`` добавляет дополнительный шаг хеширования перед вычислением модуля. 
Данные распределяются по результату хеш-функции, примененной к значениям столбца ``shardingColumn``. 
Такой тип алгоритма подходит для строковых, UUID, случайных или неравномерных ключей.

.. tabularcolumns:: |>{\ttfamily\arraybackslash}\X{4}{16}|>{\ttfamily\arraybackslash}\X{2}{16}|>{\arraybackslash}\X{10}{16}|
.. list-table:: Свойства алгоритма ``HASH_MOD``
   :class: longtable
   :name: table:hashmod
   :header-rows: 1

   * - Название свойства
     - Тип данных
     - Описание
   * - sharding-count 
     - int 
     - На сколько частей разбить таблицу

.. rubric:: ``VOLUME_RANGE``

Алгоритм автоматического шардинга, который делит данные по диапазонам значений.
``VOLUME_RANGE`` используется, когда необходимо, чтобы каждая таблица (или шард) содержала определённый диапазон значений шард-ключа.

Он автоматически создаёт физические таблицы по мере увеличения значения шард-ключа,
основываясь на объёме (``sharding-volume``) или диапазонах (``range-*``), указанных в свойствах.


.. tabularcolumns:: |>{\ttfamily\arraybackslash}\X{4}{16}|>{\ttfamily\arraybackslash}\X{2}{16}|>{\arraybackslash}\X{10}{16}|
.. list-table:: Свойства алгоритма ``VOLUME_RANGE``
   :class: longtable
   :name: table:volume_range
   :header-rows: 1

   * - Название свойства
     - Тип данных
     - Описание
   * - range-lower 
     - long 
     - Нижняя граница диапазона. Выдается исключение, если ниже границы.
   * - range-upper 
     - long 
     - Верхняя граница диапазона. Выдается исключение, если выше границы.
   * - sharding-volume  
     - long 
     - Сколько значений содержит каждая таблица

.. rubric:: ``BOUNDARY_RANGE``

Это алгоритм шардинга по заданным граничным значениям. Граничные значения указываются через запятую.
``BOUNDARY_RANGE`` распределяет данные по таблицам (или базам) в зависимости от того, в какой диапазон 
граничных значений попадает значение шард-ключа. Количество таблиц всегда равно числу граничных значений + 1.

.. tabularcolumns:: |>{\ttfamily\arraybackslash}\X{4}{16}|>{\ttfamily\arraybackslash}\X{2}{16}|>{\arraybackslash}\X{10}{16}|
.. list-table:: Свойства алгоритма ``BOUNDARY_RANGE``
   :class: longtable
   :name: table:boundary_range
   :header-rows: 1

   * - Название свойства
     - Тип данных
     - Описание
   * - sharding-ranges
     - String 
     - Список граничных значений, разделенные запятой


.. rubric:: ``AUTO_INTERVAL``

Такой алгоритм используется, если ключ шардинга имеет временной диапазон в формате ``timestamp`` с точностю до секунд.
Более высокая точность (миллисекунды, микросекунды) будет автоматически отброшена.


.. tabularcolumns:: |>{\ttfamily\arraybackslash}\X{4}{16}|>{\ttfamily\arraybackslash}\X{2}{16}|>{\arraybackslash}\X{10}{16}|
.. list-table:: Свойства алгоритма ``AUTO_INTERVAL``
   :class: longtable
   :name: table:auto_interval
   :header-rows: 1

   * - Название свойства
     - Тип данных
     - Описание
   * - datetime-lower
     - String 
     - Нижняя граница временной метки в формате ``yyyy-MM-dd HH:mm:ss``
   * - datetime-upper
     - String 
     - Верхняя граница временной метки в формате ``yyyy-MM-dd HH:mm:ss``
   * - sharding-seconds
     - long 
     - Временной шаг в секундах, для разделения данных по таблицам.

.. _subsec:buildin_alg_standart:

Встроенные алгоритмы для standard-стратегии
""""""""""""""""""""""""""""""""""""""""""""

.. rubric:: ``INLINE``

Это алгоритм шардинга на основе выражения, в котором логика распределения данных задаётся в 
виде текстового шаблона (``algorithm-expression``) прямо в конфигурации YAML.

Полный синтаксис строковых выражений в Apache ShardingSphere основан на Groovy — встроенном в Java скриптовом языке.
По умолчанию все выражения обрабатываются парсером GROOVY (``InlineExpressionParser``).

Например, ``t_user_$->{u_id % 8}`` означает, что таблица ``t_user`` разделена на 8 таблиц
в соответствии с ``u_id``, с именами таблиц от ``t_user_0`` до ``t_user_7``. 

.. code-block:: yaml

  shardingAlgorithms:
    user-inline:
      type: INLINE
      props:
        algorithm-expression: t_user_$->{u_id % 8}

.. tabularcolumns:: |>{\ttfamily\arraybackslash}\X{4}{16}|>{\ttfamily\arraybackslash}\X{2}{16}|>{\arraybackslash}\X{10}{16}|
.. list-table:: Свойства алгоритма ``INLINE``
   :class: longtable
   :name: table:inline
   :header-rows: 1

   * - Название свойства
     - Тип данных
     - Описание
   * - algorithm-expression
     - String 
     - Выражение-формула, основанная на Groovy, по которой ShardingSphere вычисляет, в какую таблицу попадут данные.
   * - allow-range-query-
       with-inline-sharding
     - boolean 
     - Этот параметр управляет поведением Inline-алгоритма (``INLINE``) при диапазонных запросах — то есть запросах, 
       где используется оператор ``BETWEEN, >, <, >=, <=``. 
       По умолчанию Inline-шардинг подходит только для точечных значений (``=`` или ``IN``).
       Когда в запросе встречается диапазон (``BETWEEN 1 AND 100``), ShardingSphere не может заранее вычислить,
       какие таблицы или базы нужно опрашивать — ведь Inline-выражение не умеет считать диапазоны.
       Поэтому, если этот параметр выключен (``false``, по умолчанию), при диапазонном запросе с Inline-алгоритмом ShardingSphere выдаст ошибку.
       Если включить (``true``) ShardingSphere разрешает выполнение диапазонных запросов, даже если используется Inline-алгоритм. 
       В этом случае он принудительно отправит запрос во все возможные шарды (т.е. сделает full routing)
       Это гарантирует, что результат будет корректным, но производительность может быть хуже, потому что опрашиваются все таблицы/БД.


.. rubric:: ``INTERVAL``


Алгоритм ``INTERVAL`` в Apache ShardingSphere используется для шардинга данных по 
временным диапазонам, когда ключ шардинга — это ``timestamp``.
Суть этого алгоритма заключается в том, что данные распределяются по таблицам или 
базам данных с определённым интервалом времени значений шардинга.
Для каждой таблицы/шарда задаётся размер интервала, и все значения, попадающие в этот интервал, отправляются в соответствующий шард.


.. tabularcolumns:: |>{\ttfamily\arraybackslash}\X{5}{16}|>{\ttfamily\arraybackslash}\X{2}{16}|>{\arraybackslash}\X{9}{16}|
.. list-table:: Свойства алгоритма ``INTERVAL``
   :class: longtable
   :name: table:inline
   :header-rows: 1

   * - Название свойства
     - Тип данных
     - Описание
   * - datetime-pattern
     - String 
     - Определяет формат даты/времени, по которому ShardingSphere будет анализировать и сравнивать значения шардинг-ключа.
       Если формат даты в данных не соответствует ``datetime-pattern``,
       алгоритм не сможет корректно вычислить, к какому интервалу относится запись,
       и шардинг может сработать некорректно (ошибка или не туда вставит данные).
   * - datetime-lower 
     - String 
     - Нижняя граница (начало диапазона) в формате ``datetime-pattern``
   * - datetime-upper 
     - String 
     - Верхняя граница (конец диапазона) в формате ``datetime-pattern``. Значение по умолчанию - ``Now``.
   * - sharding-suffix-pattern
     - String
     - Формат для суффикса имени шарда или таблиц.
       Например, ``'yyyyMM' - t_order_202407``.
       Этот шаблон должен быть совместим с Java ``LocalDateTime``, т.е. по нему можно восстановить дату и время.
       Шаблон должен соответствовать единице интервала (``datetime-interval-unit``). 
   * - datetime-interval-amount
     - int
     - Продолжительность одного временного сегмента (шаг). По умолчанию - 1.
   * - datetime-interval-unit
     - String
     - Единица измерения временного сегмента. Должен соответствовать перечислению ChronoUnit (Java). По умолчанию - ``DAYS``.
    

Этот алгоритм намеренно игнорирует информацию о часовой зоне в ``datetime-pattern``.
Это означает, что если ``datetime-lower``, datetime-upper и передаваемый ключ шардинга 
содержат информацию о часовом поясе, конвертация часового пояса не будет выполняться, 
чтобы избежать несоответствий.

При особом случае, если ключ шардинга представлен как ``java.time.Instant`` или ``java.util.Date``, 
он подхватывает часовой пояс системы и преобразуется в строковый формат, соответствующий ``datetime-pattern``, 
перед выполнением следующего шага шардинга.

.. code-block:: yaml

  shardingAlgorithms:
    t_order_interval:
      type: INTERVAL
      props:
        datetime-pattern: 'yyyy-MM-dd HH:mm:ss'
        datetime-lower: '2023-01-01 00:00:00'
        datetime-upper: '2025-01-01 00:00:00'
        datetime-interval-amount: 1
        datetime-interval-unit: MONTHS
        sharding-suffix-pattern: 'yyyyMM'

.. _subsec:buildin_alg_complex:

Встроенные алгоритмы для complex-стратегии
""""""""""""""""""""""""""""""""""""""""""""""


.. rubric:: ``COMPLEX_INLINE``

Алгоритм ``COMPLEX_INLINE`` в Apache ShardingSphere — это расширенный вариант обычного ``INLINE``, 
который используется для шардирования по нескольким ключам одновременно.
Он также вычисляет целевой шард (базу данных или таблицу) с помощью инлайн-выражения (Groovy).

.. code-block:: yaml

  shardingAlgorithms:
    complex-inline-algorithm:
      type: COMPLEX_INLINE
      props:
        algorithm-expression: "t_order_${user_id % 2}_${order_id % 4}"
        sharding-columns: "user_id, order_id"


.. tabularcolumns:: |>{\ttfamily\arraybackslash}\X{4}{16}|>{\ttfamily\arraybackslash}\X{2}{16}|>{\arraybackslash}\X{10}{16}|
.. list-table:: Свойства алгоритма ``COMPLEX_INLINE``
   :class: longtable
   :name: table:complex_inline
   :header-rows: 1

   * - Название свойства
     - Тип данных
     - Описание
   * - sharding-columns
     - String 
     - Имена столбцов - ключей шардинга. 
   * - algorithm-expression
     - String 
     - Выражение-формула, основанная на Groovy, по которой ShardingSphere вычисляет, в какую таблицу попадут данные.
   * - allow-range-query-
       with-inline-sharding
     - boolean 
     - Этот параметр управляет поведением Inline-алгоритма (``COMPLEX_INLINE``) при диапазонных запросах — то есть запросах, 
       где используется оператор ``BETWEEN, >, <, >=, <=``. 
       По умолчанию Inline-шардинг подходит только для точечных значений (``=`` или ``IN``).
       Когда в запросе встречается диапазон (``BETWEEN 1 AND 100``), ShardingSphere не может заранее вычислить,
       какие таблицы или базы нужно опрашивать — ведь Inline-выражение не умеет считать диапазоны.
       Поэтому, если этот параметр выключен (``false``, по умолчанию), при диапазонном запросе с Inline-алгоритмом ShardingSphere выдаст ошибку.
       Если включить (``true``) ShardingSphere разрешает выполнение диапазонных запросов, даже если используется Inline-алгоритм. 
       В этом случае он принудительно отправит запрос во все возможные шарды (т.е. сделает full routing)
       Это гарантирует, что результат будет корректным, но производительность может быть хуже, потому что опрашиваются все таблицы/БД.

.. _subsec:buildin_alg_hint:

Встроенные алгоритмы для Hint-стратегии
""""""""""""""""""""""""""""""""""""""""""""""""""""

.. rubric:: ``HINT_INLINE``

Алгоритм ``HINT_INLINE`` — это особый тип inline-шардирования в Apache ShardingSphere,
который используется не на основе ключей, а на основе hint-подсказок, передаваемых приложением во время выполнения запроса.

В обычных inline-алгоритмах (``INLINE, COMPLEX_INLINE``) ShardingSphere анализирует SQL-запрос, ищет в нём sharding key,
и по формуле из ``algorithm-expression`` определяет, в какую таблицу или БД направить запрос.

Но бывают случаи, когда:

- SQL не содержит явно ключ шардирования (например, запрос объединяет данные разных таблиц);
- запрос формируется динамически, и вычислить ключ невозможно;
- приложение само знает, куда направить запрос.

Тогда используется Hint-стратегия, где приложение передаёт ShardingSphere "подсказку" —
какой шард или таблицу использовать.

Он позволяет задать выражение для вычисления имени таблицы/шарда,
но входное значение берётся не из SQL, а используется переменная ``${value}``,
значение которой задаётся из приложения.
``HINT_INLINE`` не зависит от SQL, поэтому он не требует наличия ключа шардирования в запросе.


.. tabularcolumns:: |>{\ttfamily\arraybackslash}\X{4}{16}|>{\ttfamily\arraybackslash}\X{2}{16}|>{\arraybackslash}\X{10}{16}|
.. list-table:: Свойства алгоритма ``HINT_INLINE``
   :class: longtable
   :name: table:hint_inline
   :header-rows: 1

   * - Название свойства
     - Тип данных
     - Описание
   * - algorithm-expression
     - String 
     - Выражение-формула, основанная на Groovy, по которой ShardingSphere вычисляет, в какую таблицу попадут данные.
       Значение по умолчанию ``${value}``. Значение переменной ``${value}`` — это число или строка, переданное 
       приложением через HintManager (в Java API).

.. code-block:: yaml

    shardingAlgorithms:
      db-hint-inline:
        type: HINT_INLINE
        props:
          algorithm-expression: ds_${value % 2}

.. _subsec:custom_alg:

Универсальный (пользовательский) алгоритм
""""""""""""""""""""""""""""""""""""""""""""""

.. rubric:: ``CLASS_BASED``

Если для ваших целей не подходят встроенные алгоритмы, вы можете реализовать
собственное расширение (Java-реализацию алгоритма шардирования), указав тип 
стратегии шардирования (``STANDARD, COMPLEX, HINT``) и имя класса алгоритма.

Тип ``CLASS_BASED`` позволяет передавать дополнительные пользовательские параметры (из секции ``props:``) в класс алгоритма.

Переданные параметры можно получить через объект ``java.util.Properties``, доступный в классе под именем ``props``.

Смотри пример реализации в репозитории ShardingSphere — 
класс ``org.apache.shardingsphere.example.extension.sharding.algorithm.classbased.fixture.ClassBasedStandardShardingAlgorithmFixture``.


.. tabularcolumns:: |>{\ttfamily\arraybackslash}\X{4}{16}|>{\ttfamily\arraybackslash}\X{2}{16}|>{\arraybackslash}\X{10}{16}|
.. list-table:: Свойства алгоритма ``CLASS_BASED``
   :class: longtable
   :name: table:hint_inline
   :header-rows: 1

   * - Название свойства
     - Тип данных
     - Описание
   * - strategy
     - String 
     - Тип стратегии шардирования. Поддерживаются ``STANDARD, COMPLEX`` или ``HINT`` (нечувствительны к регистру). 
   * - algorithmClassName
     - String 
     - Полное имя Java-класса, реализующего алгоритм шардирования
   * - <свойства алгоритма>
     - любой
     - Дополнительные параметры, используемые в классе.


Тестовый пример реализации кастомного алгоритма шардирования:


.. code-block:: java

  package org.example.test;

  import org.apache.shardingsphere.sharding.api.sharding.standard.PreciseShardingValue;
  import org.apache.shardingsphere.sharding.api.sharding.standard.RangeShardingValue;
  import org.apache.shardingsphere.sharding.api.sharding.standard.StandardShardingAlgorithm;
  import java.util.Collection;

  public final class TestShardingAlgorithmFixture implements StandardShardingAlgorithm <Integer> {
    @Override
    public String doSharding(final Collection<String> availableTargetNames, final PreciseShardingValue<Integer> shardingValue) {
        String resultDatabaseName = "ds_" + shardingValue.getValue() % 2;
        for (String each : availableTargetNames) {
           if (each.equals(resultDatabaseName)) {
               return each;
           }
        }
        return null;
    }

    @Override
    public Collection<String> doSharding(final Collection<String> availableTargetNames, final RangeShardingValue<Integer> shardingValue) {
        throw new RuntimeException("This algorithm class does not support range queries.");
    }
  }

Файл конфигурации:

.. code-block:: yaml

    rules:
    - !SHARDING
      defaultDatabaseStrategy:
        standard:
          shardingColumn: user_id
          shardingAlgorithmName: inline
      shardingAlgorithms:
        inline:
          type: CLASS_BASED
          props:
            strategy: STANDARD
            algorithmClassName: org.example.test.TestShardingAlgorithmFixture

.. _subsec:dist_pk:

Алгоритмы генерации распределённых (глобально уникальных) первичных ключей
"""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""

В традиционной разработке баз данных автоматическая генерация первичных ключей — это базовое требование.
СУБД Ред База Данных также поддерживает это. При создании таблицы можно задать столбец идентификации, 
связанный с внутренним генератором последовательностей.

После внедрения шардинга возникает сложная проблема — как генерировать глобально уникальные первичные ключи,
чтобы они не дублировались между шардами (разными узлами данных).

Если использовать обычные автоинкрементные ключи в каждой физической таблице (которая является частью одной логической таблицы),
то могут возникнуть дубликаты ключей, потому что разные таблицы не знают друг о друге и увеличивают свои значения независимо.

Можно избежать коллизий, если задать разные начальные значения и шаги инкремента для каждого шарда
(например, шард 0 → шаг 2, старт 1; шард 1 → шаг 2, старт 2), но это потребует ручной настройки и усложнит эксплуатацию.
Такое решение плохо масштабируется и неудобно в поддержке.

Существуют и сторонние решения этой проблемы, например:

- UUID, который использует специальные алгоритмы для генерации неповторяющихся ключей,
- использование внешних сервисов генерации ключей (например, ``Snowflake``).

Чтобы удовлетворить требования различных пользователей в различных сценариях, Apache ShardingSphere
не только предоставляет встроенные распределенные генераторы первичных ключей, такие как ``UUID`` и ``SNOWFLAKE``, но и
абстрагирует интерфейс распределенных генераторов первичных ключей, чтобы пользователи могли реализовывать собственные
настраиваемые генераторы первичных ключей.

.. rubric:: ``SNOWFLAKE``

Этот генератор первичных ключей используется по умолчанию.
Он генерирует 64-битные числовые ID, которые:

- уникальны во всех шардах;
- Упорядоченный по времени;
- встроенно кодируют метаданные (время, узел, последовательность).

.. code-block:: yaml

    keyGenerators:
      snowflake:
        type: SNOWFLAKE
        props: 
          worker-id: 123
          max-tolerate-time-difference-milliseconds: 1000


.. tabularcolumns:: |>{\ttfamily\arraybackslash}\X{4}{15}|>{\ttfamily\arraybackslash}\X{2}{15}|>{\arraybackslash}\X{9}{15}|
.. list-table:: Свойства алгоритма ``SNOWFLAKE``
   :class: longtable
   :name: table:snowflake
   :header-rows: 1

   * - Название свойства
     - Тип данных
     - Описание
   * - worker-id
     - long 
     - Уникальный идентификатор узла (0–1023). Позволяет отличать разные инстансы Proxy / приложения. Значение по умолчанию - 0
   * - max-tolerate-time-
       difference-milliseconds 
     - long
     - Максимальное допустимое расхождение времени на разных серверах в миллисекундах. Значение по умолчанию - 10
   * - max-vibration-offset
     - int
     - Предельное значение случайного смещения счётчика (vibration), который помогает избежать "неудачного" 
       распределения по шардам.

       **Примечание:** Если вы используете сгенерированные значения этого алгоритма (``SNOWFLAKE``) 
       в качестве шардинг-ключей, рекомендуется настроить этот параметр.

       Snowflake генерирует ID, которые растут по времени, но часть младших битов может быть предсказуемой. 
       Если шардирование идёт по формуле: ``id % 2^n`` (где ``2^n`` - это обычно общее количество таблиц или баз данных), 
       то без вибрации (vibration) результат часто чередуется только между 0 и 1 (особенно при малом количестве шардов).
       То есть при 2 шардах (``2^1 = 2``) распределение может быть неравномерным — все данные будут попадать почти в один шард.
       Чтобы улучшить равномерность распределения, вводится небольшой случайный сдвиг (vibration) к младшим битам, 
       чтобы значения Snowflake ID: распределялись чуть равномернее по шардам.
       Рекомендуемое значение ``max-vibration-offset = (2^n) - 1``. 
       Диапазон допустимых значений [0,4096).
       Значение по умолчанию - 1.

.. rubric:: ``UUID``

Алгоритм ``UUID`` — это один из самых простых и популярных способов генерации уникальных идентификаторов, в том числе в ShardingSphere.
``UUID`` — это 128-битный (16-байтовый) уникальный идентификатор, который записывается в строковой форме:

.. code-block:: properties

  550e8400-e29b-41d4-a716-446655440000

Если в ShardingSphere используется генератор UUID, то тип данных поля первичного ключа должен позволять 
хранить строковое значение фиксированной длины (36 символов) (``CHAR(36)`` или ``VARCHAR(36)``).

.. code-block:: yaml

  keyGenerators:
    uuid:
      type: UUID

Свойств нет.

Тег ``- !BROADCAST``
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ 

В правилах в модуле ``!BROADCAST`` задается список broadcast-таблиц.

Это таблицы, которые существует в каждом источнике данных (шарде), причём их структура и содержимое везде одинаковы.

.. code-block:: yaml

  rules:
  - !BROADCAST
    tables: # Broadcast таблицы
      - <имя_таблицы>
      - <имя_таблицы>


Тег ``- !SINGLE``
~~~~~~~~~~~~~~~~~~~~~~~~~

.. code-block:: yaml

  rules:
  - !SINGLE
    tables:
      - ds_0.t_single   # Load specified single table
      - ds_1.*          # Load all single tables in the specified data source
      - "*.*"           # Load all single tables
    defaultDataSource: ds_0 
    # The default data source is used when executing CREATE TABLE statement to create a single table. 
    The default value is null, indicating random unicast routing.

Тег ``- !READWRITE_SPLITTING``
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~


Тег ``- !ENCRYPT``
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~


Тег ``- !MASK``
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~


Тег ``- !SHADOW``
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~