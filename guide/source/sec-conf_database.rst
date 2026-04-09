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
  :heading-level: 4

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

    # Настройка алгоритмов генерации ключей
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
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

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

  - ``true`` - аудит можно отключить через SQL Hint;
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
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

``MOD``
""""""""""""""""""""""""""


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
 
``HASH_MOD``
""""""""""""""""""""""""""


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

``VOLUME_RANGE``
""""""""""""""""""""""""""

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

``BOUNDARY_RANGE``
""""""""""""""""""""""""""

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


``AUTO_INTERVAL``
""""""""""""""""""""""""""

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
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

``INLINE``
""""""""""""""""""""""""""

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


``INTERVAL``
""""""""""""""""""""""""""


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
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^


``COMPLEX_INLINE``
""""""""""""""""""""""""""

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
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

``HINT_INLINE``
""""""""""""""""""""""""""

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
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

``CLASS_BASED``
""""""""""""""""""""""""""

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
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

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

``SNOWFLAKE``
""""""""""""""""""""""""""

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

``UUID``
""""""""""""""""""""""""""

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


.. _subsec:row_expr:

Строковые выражения (Row Value Expressions)
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

Строковые выражения применяется для описания узлов данных (``actualDataNodes``), источников данных (``actualDataSources``) и 
для настройки алгоритмов шардинга (``algorithm-expression``).

.. compound::

  Строковые выражения состоят из двух частей, объединённых в одну строку:

  .. code-block::

    <Type_Name>Row_Expression

  - ``Type_Name`` - тип выражения, соответствующий реализации SPI интерфейса ``org.apache.shardingsphere.infra.expr.spi.InlineExpressionParser``.
    Указывается в угловых скобках ``< >`` в начале строки.
    Если тип выражения не указан, то по умолчанию используется реализация ``GROOVY`` — стандартный парсер ``InlineExpressionParser`` SPI.
  - ``Row_Expression`` - само выражение, описывающее диапазон или набор значений.

.. rubric:: Пример
  :heading-level: 5

.. compound::

  .. code-block:: yaml
    
    <GROOVY>t_order_${1..3}

  Здесь:

  - ``<GROOVY>`` — это тип реализации SPI для обработки выражения.

  - ``t_order_${1..3}`` — это само выражение, определяющее последовательность значений.

ShardingSphere поддерживает несколько вариантов реализации парсинга выражений:

- ``GROOVY``;
- ``LITERAL``;
- ``INTERVAL``;
- ``ESPRESSO``;
- *Пользовательские реализации*

Остановимся на каждом из них поподробнее.

``GROOVY`` - синтаксис
""""""""""""""""""""""""""

Применение Groovy-синтаксиса является основным и наиболее гибким способом описания выражений в конфигурации ShardingSphere. 
Данный подход позволяет использовать весь набор стандартных операций и конструкций языка Groovy при формировании значений.

В конфигурационных файлах выражения указываются с помощью следующих шаблонов: ``${ expression }`` или ``$->{ expression }``.

Такие выражения обрабатываются встроенной реализацией InlineExpressionParser типа ``GROOVY``, которая используется 
по умолчанию, если тип парсера явно не указан.

.. rubric:: Примеры выражений
  :heading-level: 5

1. *Диапазон значений*

   .. code-block:: groovy

      ${1..4} 

   создаёт последовательность 1, 2, 3, 4.

2. *Перечисление значений*

   .. code-block:: groovy

       ${['east', 'west', 'north']} 

   создаёт набор ``east, west, north``.

3. *Комбинированное выражение*

   Если в одном выражении строки присутствует несколько конструкций ``${ expression }`` или ``$->{ expression }``, 
   то итоговый результат будет представлять собой декартово произведение всех подвыражений.

   .. code-block:: groovy

     ${['online', 'offline']}_table${1..3}
   
   разворачивается в ``online_table1, online_table2, online_table3, offline_table1, offline_table2, offline_table3``.

``LITERAL`` - синтаксис
""""""""""""""""""""""""""

Тип ``LITERAL`` принимает строковый список без выполнения каких-либо преобразований.
Алгоритм ``LITERAL`` предназначен для сценариев, где использование Groovy-выражений 
невозможно или нежелательно (например, при сборке Native Image в GraalVM).
В этом случае значения задаются явно, без вычислений и интерпретации выражений.


.. rubric:: Примеры выражений
  :heading-level: 5

- 
  .. code-block::

      <LITERAL>t_order_1, t_order_2, t_order_3

  будет преобразовано в ``t_order_1, t_order_2, t_order_3``.

- 
  .. code-block::

    <LITERAL>t_order_${1..3}

  будет преобразовано в ``t_order_${1..3}``.

``INTERVAL`` - синтаксис
""""""""""""""""""""""""""

Тип ``INTERVAL`` предназначен для генерации последовательности значений на основе заданного временного диапазона и шаблонов именования.
Данный механизм особенно удобен в случаях, когда физические таблицы представляют собой временные или датированные таблицы.

Реализация ``INTERVAL`` использует синтаксис "ключ–значение" для описания набора временных диапазонов в виде одной строковой записи.

Строки, генерируемые с помощью ``INTERVAL``, применимы только для нескольких таблиц в рамках одной физической базы данных.
Если требуется работать с несколькими таблицами, распределёнными по разным физическим базам данных, необходимо реализовать 
собственную SPI-реализацию интерфейса
``org.apache.shardingsphere.infra.expr.spi.InlineExpressionParser``.

.. rubric:: Формат записи:
  :heading-level: 5

.. code-block::

  Key1=Value1;Key2=Value2;...

- символ ``;`` разделяет пары ключ–значение;
- символ ``=`` разделяет ключ и его значение;
- порядок следования пар *ключ–значение* не имеет значения;
- завершающий символ ``;`` в конце строки не требуется.

Реализация ``INTERVAL`` игнорирует информацию о временной зоне, указанную в параметрах ``DL`` и ``DU``.
Это означает, что преобразование часовых поясов не выполняется даже при их несовпадении.

.. rubric:: Поддерживаемые ключи
  :heading-level: 5

- ``P``

   Префикс элементов результирующего списка. Обычно используется как префикс имени физической таблицы.
- ``SP``

   Шаблон суффикса, определяющий формат временной метки. Используется для формирования суффикса имени физической таблицы.
   Формат должен соответствовать ``java.time.format.DateTimeFormatter``, например: ``yyyyMMdd, yyyyMM, yyyy``  и др.
- ``DIA``
   
   Величина временного интервала между элементами результирующего списка.
- ``DIU``

   Единица измерения временного интервала. Должна соответствовать значениям ``java.time.temporal.ChronoUnit#toString()``, например:
   DAYS, MONTHS, YEARS.
- ``DL``

   Нижняя граница временного диапазона. Формат должен соответствовать шаблону, заданному в SP.
- ``DU``

   Верхняя граница временного диапазона. Формат должен соответствовать шаблону, заданному в SP
- ``C``

   Календарная система. Значение должно соответствовать ``java.time.chrono.Chronology#getId()``, например:
   
   - ISO (значение по умолчанию)
   - Japanese 
   - Minguo 
   - ThaiBuddhist

   Доступность значения, заданного в параметре C, зависит от окружения, в котором работает JVM.
   
   - HotSpot JVM
     Значение java.util.Locale.getDefault() определяется во время выполнения (runtime).
   - GraalVM Native Image
     Значение java.util.Locale.getDefault() определяется на этапе сборки (build time), что отличается от поведения HotSpot JVM.

     В некоторых случаях (например, при использовании C=Japanese) может потребоваться явно задать локаль:
     ``Locale.setDefault(Locale.JAPAN);``. Это следует делать в стартовом классе приложения.

.. rubric:: Примеры выражений
  :heading-level: 5

1. Последовательность временных значений с интервалом по дням
   
   .. code-block::

     <INTERVAL>P=t_order_;SP=yyyy_MMdd;DIA=1;DIU=Days;DL=2023_1202;DU=2023_1204

   сконвертируется в ``t_order_2023_1202, t_order_2023_1203, t_order_2023_1204``.

2. Последовательность временных значений с интервалом по месяцам
   
   .. code-block::

      <INTERVAL>P=t_order_;SP=yyyy_MM;DIA=1;DIU=Months;DL=2023_10;DU=2023_12
   
   сконвертируется в ``t_order_2023_10, t_order_2023_11, t_order_2023_12``.

3. Последовательность временных значений с интервалом по годам

   .. code-block::

      <INTERVAL>P=t_order_;SP=yyyy;DIA=1;DIU=Years;DL=2021;DU=2023

   сконвертируется в ``t_order_2021, t_order_2022, t_order_2023``.

4. Последовательность временных значений с интервалом по миллисекундам

   .. code-block::

     <INTERVAL>P=t_order_;SP=HH_mm_ss_SSS;DIA=1;DIU=Millis;DL=22_48_52_131;DU=22_48_52_133

   сконвертируется в ``t_order_22_48_52_131, t_order_22_48_52_132, t_order_22_48_52_133``.

5. Последовательность дата-временных значений с интервалом в днях

   .. code-block::

     <INTERVAL>P=t_order_;SP=yyyy_MM_dd_HH_mm_ss_SSS;DIA=1;DIU=Days;DL=2023_12_04_22_48_52_131;DU=2023_12_06_22_48_52_131

   сконвертируется в ``t_order_2023_12_04_22_48_52_131, t_order_2023_12_05_22_48_52_131, t_order_2023_12_06_22_48_52_131``.

6. Последовательность временных значений с интервалом по месяцам

   .. code-block::

     <INTERVAL>P=t_order_;SP=MM;DIA=1;DIU=Months;DL=10;DU=12

   сконвертируется в ``t_order_10, t_order_11, t_order_12``.

7. Пример использования INTERVAL с японской календарной системой

   .. code-block:: text

     <INTERVAL>P=t_order_;SP=GGGGyyyy_MM_dd;DIA=1;DIU=Days;DL=<японские иероглифы> 0001_12_05;DU=0001_12_06;C=Japanese 
   
 
   сконвертируется в ``t_order_<японские иероглифы> 0001_12_05, t_order_<японские иероглифы> 0001_12_06``

   .. code-block::

     <INTERVAL>P=t_order_;SP=GGGGyyy_MM_dd;DIA=1;DIU=Days;DL= <японские иероглифы> 001_12_05;DU= <японские иероглифы> 001_12_06;C=Japanese
   
   сконвертируется в ``t_order_<японские иероглифы> 001_12_05, t_order_<японские иероглифы> 001_12_06``

   .. code-block::

    <INTERVAL>P=t_order_;SP=GGGGy_MM_dd;DIA=1;DIU=Days;DL=<японские иероглифы> 1_12_05;DU=<японские иероглифы> 1_12_06;C=Japanese 
    
   сконвертируется в ``t_order_<японские иероглифы> 1_12_05, t_order_<японские иероглифы> 1_12_06``


``ESPRESSO`` - синтаксис
""""""""""""""""""""""""""


``ESPRESSO`` — экспериментальная реализация строковых выражений, предназначенная для использования Groovy-синтаксиса в GraalVM Native Image.
Модуль основан на реализации Espresso, входящей в состав фреймворка GraalVM Truffle.

Синтаксис выражений полностью совместим с реализацией GROOVY.

Реализация является опциональной, требует явного подключения зависимостей и использования OpenJDK 21+.

.. code-block::

  <dependencies>
    <dependency>
      <groupId>org.apache.shardingsphere</groupId>
      <artifactId>shardingsphere-infra-expr-espresso</artifactId>
      <version>${shardingsphere.version}</version>
    </dependency>
    <dependency>
      <groupId>org.graalvm.polyglot</groupId>
      <artifactId>polyglot</artifactId>
      <version>24.1.0</version>
    </dependency>
    <dependency>
      <groupId>org.graalvm.polyglot</groupId>
      <artifactId>java</artifactId>
      <version>24.1.0</version>
      <type>pom</type>
    </dependency>
  </dependencies>


Из-за ограничений Truffle, если модуль используется не в GraalVM Native Image, он работает только на Linux и только на архитектуре amd64 (``os.arch=amd64``).

Таблица совместимости версий Truffle и JDK приведена по указанной ссылке - https://medium.com/graalvm/40027a59c401.


.. rubric:: Примеры выражений
  :heading-level: 5

1. Диапазон значений 
   
   .. code-block::

     <ESPRESSO>t_order_${1..3}

   сконвертируется в ``t_order_1, t_order_2, t_order_3``.

2. Комбинированное выражение
   
   .. code-block::

      <ESPRESSO>${['online', 'offline']}_table${1..3}
   
   сконвертируется в ``online_table1, online_table2, online_table3, offline_table1, offline_table2, offline_table3``.


Пользовательская реализация
"""""""""""""""""""""""""""""""""""

В дополнение к встроенным реализациям парсинга строковых выражеий, Apache ShardingSphere предоставляет
возможность создавать собственные классы реализации интерфейса
``org.apache.shardingsphere.infra.expr.spi.InlineExpressionParser``,
чтобы покрыть более сложные сценарии использования, включая, например, 
подключение к удалённому кластеру ``ElasticSearch`` и выполнение ES|QL-запросов с целью получения результата в виде ``java.util.List<String>``.

В качестве примера можно рассмотреть простую реализацию SPI-класса.

.. code-block:: java

  import org.apache.shardingsphere.infra.expr.spi.InlineExpressionParser;
  import java.util.Arrays;
  import java.util.List;
  import java.util.Properties;
  public final class CustomInlineExpressionParserFixture implements InlineExpressionParser {
      private String inlineExpression;
      @Override
      public void init(final Properties props) {
          inlineExpression = props.getProperty(INLINE_EXPRESSION_KEY);
      }
      @Override
      public List<String> splitAndEvaluate() {
          if ("spring".equals(inlineExpression)) {
              return Arrays.asList("t_order_2024_01", "t_order_2024_02");
          }
          return Arrays.asList("t_order_2024_03", "t_order_2024_04");
      }
      @Override
      public Object getType() {
          return "CUSTOM.FIXTURE";
      }
  }

Для подключения пользовательской реализации необходимо:

1. Создать класс, реализующий интерфейс ``org.apache.shardingsphere.infra.expr.spi.InlineExpressionParser``.

2. Добавить в ``classpath`` проекта файл: ``META-INF/services/org.apache.shardingsphere.infra.expr.spi.InlineExpressionParser``.

3. Указать в этом файле полное имя класса реализации, например: ``org.example.CustomInlineExpressionParserFixture``.

После этого ShardingSphere автоматически обнаружит и загрузит пользовательский парсер при старте.

Зарегистрированная реализация может использоваться в конфигурационных параметрах, поддерживающих
Row Value Expressions, например в ``actualDataNodes``.

.. rubric:: Примеры выражений
  :heading-level: 5

- 
  .. code-block:: yaml

    actualDataNodes: <CUSTOM.FIXTURE>spring

  Такая конфигурации возвращает список строк ``t_order_2024_01, t_order_2024_02``.

- 
  .. code-block:: yaml

    actualDataNodes: <CUSTOM.FIXTURE>summer

  Такая конфигурации возвращает список строк ``t_order_2024_03, t_order_2024_04``.


Тег ``- !BROADCAST``
~~~~~~~~~~~~~~~~~~~~~~~

В правилах под тегом ``!BROADCAST`` задается список broadcast-таблиц.

.. code-block:: yaml

  rules:
  - !BROADCAST
    tables:            # Broadcast таблицы
      - <имя_таблицы>  # логические таблицы
      - <имя_таблицы>

Broadcast-таблицы — это специальные таблицы, которые существует в каждом источнике данных (шарде), 
причём их структура и содержимое везде одинаковы.
ShardingSphere автоматически выполняет операции ``INSERT, UPDATE, DELETE`` на каждом 
узле и синхронизирует данные между всеми источниками. Запросы типа ``SELECT`` выполняются на любом 
удобном узле (выбор зависит от маршрутизации).

Broadcast-таблицы используются для:

- справочников (например, города, типы документов, статусы);
- небольших по объему данных;
- данных, которые редко изменяются и требуются на всех узлах;
- таблиц, которые участвуют в JOIN-операциях с шардинг-таблицами и должны быть доступны на всех шардах.

Broadcast-таблицы не шардируются — каждая таблица в списке полностью копируется на каждый ``dataSource``.

.. code-block:: yaml

  rules:
    - !BROADCAST
      tables:
        - t_region
        - t_currency
        - t_config


Тег ``- !SINGLE``
~~~~~~~~~~~~~~~~~~~~~~~~~

В блоке ``rules`` с использованием тега ``!SINGLE`` задаются одиночные аблицы. 

Single-таблицы — это таблицы, которые не шардируются и находятся только на одном физическом источнике данных.
В отличие от Broadcast-таблиц (которые копируются на все узлы), Single-таблица существует в единственном 
экземпляре, и все операции с ней направляются на один конкретный ``dataSource``.

Single-таблицы используются когда:

- данные не должны быть разделены на шарды;
- таблица небольшая и не требует масштабирования;
- требуется централизованное хранение (например, лог таблиц, служебные данные, метаданные);
- таблица используется редко, и нет смысла распределять её между узлами.

Одиночные таблицы можно настроить несколькими способами:

.. code-block:: yaml

  rules:
  - !SINGLE
    tables:
      - ds_0.t_single   # Загрузить конкретную таблицу t_single в источник данных ds_0
      - t_single        # Загрузить конкретную таблицу t_single в источник данных 
                        # из defaultDataSource
      - ds_1.*          # Загрузить все одиночные таблицы в источник данных ds_1
      - "*.*"           # Загрузить все одиночные таблицы в источник данных 
                        # из defaultDataSource
    defaultDataSource: ds_0  # Источник данных по умолчанию, используется при выполнении
                             # оператора CREATE TABLE для создания одиночной таблицы.
                             # Значение по умолчанию — null, что указывает на случайную 
                             # одноадресную маршрутизацию.


Тег ``- !READWRITE_SPLITTING``
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

.. note::

  Для использования ``!READWRITE_SPLITTING`` обязательно должна быть настроена репликация на уровне базы данных.

Тег ``!READWRITE_SPLITTING`` в конфигурации ShardingSphere используется для настройки 
разделения операций чтения и записи между различными источниками данных. Это позволяет 
направлять DML-запросы (``INSERT, UPDATE, DELETE``) на мастер  и ``SELECT``-запросы на 
реплики, улучшая производительность и масштабируемость.
ShardingSphere работает поверх уже существующей инфраструктуры репликации, и только маршрутизирует SQL-запросы.

.. code-block:: yaml

  rules:
  - !READWRITE_SPLITTING
    dataSourceGroups:
      <data_source_group_name>: # (+) Логическое имя группы источников данных 
                                # для readwrite-splitting.
        write_data_source_name: # Имя источника данных для операций записи (имя мастера)
                                # По умолчанию используется Groovy Row Value Expressions 
                                # SPI для разбора значений
        read_data_source_names: # Имена источников данных для операций чтения (реплики)
                                # Несколько имён указываются через запятую.
                                # По умолчанию используется Groovy Row Value Expressions 
                                # SPI для разбора значений.
        transactionalReadQueryStrategy:  
                          # Стратегия маршрутизации запросов на чтение внутри транзакции
                          # Возможные значения:
                          #   PRIMARY — направлять на основной источник данных,
                          #   FIXED — направлять на фиксированный источник данных,
                          #   DYNAMIC — направлять на любой доступный источник данных.
                          # Значение по умолчанию: DYNAMIC.
        loadBalancerName: # Алгоритм балансировки запросов чтения между репликами.

    # Настройка алгоритма балансировки
    loadBalancers:
      <load_balancer_name>:     # (+) Имя алгоритма балансировки
        type:                   # Тип алгоритма
        props:                  # Свойства алгоритма
          # ...


Алгоритмы балансировки
^^^^^^^^^^^^^^^^^^^^^^^^^

ShardingSphere содержит встроенные алгоритмы балансировки нагрузки, которые используются, 
например, при распределении запросов между репликами (в режиме readwrite-splitting). Среди них:

- ``ROUND_ROBIN`` (пуллинговый, по очереди);
- ``RANDOM`` (случайное распределение);
- ``WEIGHT`` (с учётом весов, чтобы более мощным узлам отдавать больше запросов).

У алгоритма ``WEIGHT`` есть свойство:

.. tabularcolumns:: |>{\ttfamily\arraybackslash}\X{4}{15}|>{\ttfamily\arraybackslash}\X{2}{15}|>{\arraybackslash}\X{9}{15}|
.. list-table:: Свойства алгоритма ``WEIGHT``
   :class: longtable
   :name: table:weight
   :header-rows: 1

   * - Название свойства
     - Тип данных
     - Описание
   * - <имя реплики>
     - double
     - Имя атрибута - это имя реплики, а значение - вес, соответствующий реплике. 
       Диапазон значений параметра веса: минимальное > 0, суммарное <= Double.MAX_VALUE.

.. note::

  Правила разделения на чтение/запись обычно используется с шардингом для балансировки нагрузки.
  Но в настоящее время для СУБД Ред База Данных эти правила не поддерживаются.



Тег ``- !ENCRYPT``
~~~~~~~~~~~~~~~~~~~

Тег ``!ENCRYPT`` в конфигурации ShardingSphere используется для настройки шифрования чувствительных данных.

.. code-block:: yaml

  rules:
  - !ENCRYPT
    tables:
      <table_name>: # Имя шифруемой таблицы
        columns:
          <column_name> (+): # Логическое имя шифруемого столбца таблицы
            cipher:
              name: # Имя шифруемого столбца (физическое)
              encryptorName: # Название алгоритма шифрования
            assistedQuery (?):
              name: # Имя столбца вспомогательных запросов
              encryptorName: # Имя алгоритма шифрования для assisted query
            likeQuery (?):
              name: # Имя столбца для выполнения запросов с LIKE
              encryptorName: # Имя алгоритма шифрования для LIKE-запросов

    encryptors:
      <encrypt_algorithm_name> (+): # Имя алгоритма шифрования
        type: # Тип алгоритма шифрования
        props: # Параметры (свойства) алгоритма шифрования
           # ...

Раздел ``encryptors`` описывает используемые алгоритмы шифрования.

Поддерживаемые типы алгоритмов:

- ``AES`` — симметричное шифрование (наиболее распространённый вариант);
- ``RSA`` — асимметричное шифрование;
- ``SM4`` — национальный криптоалгоритм;
- пользовательские алгоритмы через SPI.

.. note::

  В настоящее время для СУБД Ред База Данных правила шифрования данных не поддерживаются.


Тег ``- !MASK``
~~~~~~~~~~~~~~~~~

Тег ``!MASK`` в конфигурации ShardingSphere используется для настройки маскирования данных.
Маскирование применяется при чтении данных и позволяет скрывать значения
отдельных столбцов без изменения исходных данных в базе.

.. code-block:: yaml

  rules:
  - !MASK
    tables:
      <table_name>: # Имя маскируемой таблицы
        columns:
          <column_name>:   # Имя столбца, к которому применяется маскирование
            maskAlgorithm: #  Алгоритм маскирования

    maskAlgorithms:
      <mask_algorithm_name>: # Алгоритм маскирования
        type: # Тип алгоритма маскирования
        props: # Параметры алгоритма маскирования
        # ...

Встроенные алгоритмы маскирования включают, в частности:

- ``MD5`` — хэширование значения с использованием MD5;
- ``KEEP_FIRST_N_LAST_M`` — отображение первых N и последних M символов;
- ``KEEP_PREFIX_SUFFIX`` — сохранение префикса и суффикса строки;
- ``MASK_FROM_X_TO_Y`` — замена части строки символами маски;
- ``REPLACE`` — полная замена значения фиксированной строкой

.. note::

  В настоящее время для СУБД Ред База Данных правила шифрования данных не поддерживаются.


Тег ``- !SHADOW``
~~~~~~~~~~~~~~~~~~~

Тег ``!SHADOW`` в конфигурации ShardingSphere используется для организации теневого (shadow) трафика в ShardingSphere.
Оно позволяет дублировать часть SQL-запросов в отдельные источники данных — теневые базы —
без влияния на основной бизнес-трафик.

.. code-block:: yaml

  rules:
  - !SHADOW
    dataSources:
      shadowDataSource:
        productionDataSourceName: # Имя основного (production) источника данных
        shadowDataSourceName: # Имя теневого (shadow) источника данных
    tables:
      <table_name>:
        dataSourceNames: # Список имен источников теневых данных, связанных с теневыми таблицами
          - <shadow_data_source>
        shadowAlgorithmNames: # Список имен алгоритмов, связанных с теневой таблицей
          - <shadow_algorithm_name>
    defaultShadowAlgorithmName: # имя алгоритма по умолчанию 
    shadowAlgorithms:
      <shadow_algorithm_name> : # Имя shadow алгоритма
        type: # тип shadow алгоритма
        props: # параметра shadow алгоритма

.. note::

  В настоящее время для СУБД Ред База Данных правила шифрования данных не поддерживаются.