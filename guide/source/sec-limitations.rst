Ограничения
=============

ShardingSphere полностью совместим со всеми распространёнными SQL-запросами, 
если они маршрутизируются к одному узлу данных (т.е. запрос выполняется в пределах одного шарда).

Однако если SQL-запрос затрагивает несколько шардов одновременно (multi-node routing), то поддержка 
таких запросов ограничена — они делятся на три категории по степени стабильности:

- *Стабильная поддержка* — полностью поддерживается;
- *Экспериментальная поддержка* — работает, но может быть нестабильно или неэффективно;
- *Не поддерживается* — не реализовано.

Стабильная поддержка
----------------------

Здесь описан уровень стабильной поддержки SQL-запросов в ShardingSphere при работе с несколькими узлами данных (шардами).

#. Полная поддержка DML, DDL, DCL, TCL и распространённых DAL.

#. Поддержка сложных запросов, таких как постраничная выборка (paging), удаление дубликатов, сортировка, группировка, агрегирование, объединение таблиц и т.д.

.. rubric:: Обычные SELECT-запросы

Осуществляется стабильная поддержка обычных ``SELECT``-запросов:

.. code-block:: sql

    SELECT select_expr [, select_expr ...] 
    FROM table_reference [, table_reference ...]
    [WHERE predicates]
    [GROUP BY {col_name | position} [ASC | DESC], ...]
    [ORDER BY {col_name | position} [ASC | DESC], ...]
    [LIMIT {[offset,] row_count | row_count OFFSET offset}]

    select_expr ::= * 
                    | [DISTINCT] <column name> [AS] [alias] 
                    | (MAX | MIN | SUM | AVG)(<column name> | alias) [AS] [alias] 
                    | COUNT(* | <column name> | alias) [AS] [alias]

    table_reference ::= tbl_name [[AS] alias] [index_hint_list]
                        | table_reference ([INNER] | {LEFT|RIGHT} [OUTER]) 
                          JOIN table_factor 
                          [JOIN ON conditional_expr | USING (column_list)]

.. rubric:: Подзапросы

Осуществляется стабильная поддержка следующих подзапросов при работе с несколькими узлами данных (шардами):

- Подзапросы с указанием шардингового ключа

  Ядро ShardingSphere стабильно поддерживает подзапросы, если как подзапрос, 
  так и внешний запрос используют шардинговый ключ и значения ключа для конкретного среза совпадают.

  .. code-block:: sql

    SELECT * FROM (SELECT * FROM t_order WHERE order_id = 1) o WHERE o.order_id = 1;

  Здесь ``order_id`` — шардинговый ключ, и значения совпадают, поэтому ядро корректно маршрутизирует запрос к нужному шарду.  

- Подзапросы для пагинации 

  .. code-block:: sql

    SELECT * FROM (SELECT row_.*, rownum rownum_ FROM (SELECT * FROM t_order) row_ WHERE rownum <= ?) WHERE rownum > ?;


.. rubric:: Пагинация запросов


(Поддерживается ли постраничный вывод в СУБД Ред База Данных нужно выяснить??)

.. code-block:: sql
    
    SELECT * FROM t_order o ORDER BY id LIMIT ? OFFSET ?


.. rubric:: Агрегатные функции


Поддерживаются агрегатные функции ``MAX, MIN, SUM, COUNT, AVG`` и другие.

.. rubric:: Ключ шардинга включен в операторное выражение

Для корректной маршрутизации запроса на конкретный шард значение ключа шардинга должно быть явно указано в SQL.
Если ключ шардинга используется в выражении или функции, ShardingSphere не может извлечь его значение напрямую.

В этом случае система не знает, какой шард выбрать, и выполняет full routing — отправляет запрос на все шарды.

В качестве примера, предположим, что ``create_time`` является ключом шардинга.

.. code-block:: sql

    SELECT * FROM t_order WHERE YEAR(create_time) = 2025;

Здесь ``YEAR(create_time)`` — это выражение, ShardingSphere не может понять, какое конкретное значение ``create_time`` 
выбрать для маршрутизации.

Поскольку значение ключа недоступно напрямую, ShardingSphere выполняет full routing, отправляя запрос на все шарды одновременно.

.. rubric:: Представления (view)


ShardingSphere-Proxy поддерживает операции создания, изменения и удаления представлений (``view``) в различных сценариях:

1. *На основе одной таблицы или нескольких одиночных (single) таблиц на одном узле хранения*

   Можно создавать, изменять и удалять представления, если они строятся на одной или 
   нескольких одиночных таблицах на одной базе данных.

2. *На основе любой broadcast-таблицы*

   Можно создавать, изменять и удалять представления, если они строятся на основе любой broadcast-таблицы.

3. *На основе любой sharding-таблицы*
   
   Представление должно быть сконфигурировано с такими же правилами шардинга, как и исходная таблица.
   Представление и таблица должны принадлежать одному "binding table" правилу.

4. *На основе broadcast-таблиц и sharding-таблиц одновременно*
   
   Представление должно быть сконфигурировано с такими же правилами шардинга, как и шардированная таблица.
   Представление и таблица должны принадлежать одному "binding table" правилу.

5. *На основе broadcast-таблиц и одиночных таблиц*
   
   Представления могут комбинировать broadcast- и single-таблицы.


Экспериментальная поддержка
--------------------------------

Экспериментальная поддержка предоставляется через Federation execution engine — экспериментальный 
движок выполнения, который находится в разработке.
Хотя большинство функций уже доступно пользователям, движок требует значительной оптимизации и тестирования.

.. rubric:: Подзапросы

Движок Federation обеспечивает работу с подзапросами и внешними запросами, когда:

- не оба запроса указывают ключ шардинга, или
- значения ключа шардинга между подзапросом и внешним запросом не совпадают.

.. code-block:: sql

    SELECT * FROM (SELECT * FROM t_order) o;

    SELECT * FROM (SELECT * FROM t_order) o WHERE o.order_id = 1;

    SELECT * FROM (SELECT * FROM t_order WHERE order_id = 1) o;

    SELECT * FROM (SELECT * FROM t_order WHERE order_id = 1) o WHERE o.order_id = 2;

В таких случаях стандартный движок ядра ShardingSphere не может корректно маршрутизировать запросы. 
Federation execution engine позволяет выполнять их в экспериментальном режиме.

.. rubric:: Межбазные JOIN запросы

Если в запросе участвуют несколько таблиц, которые распределены по разным базам данных, 
стандартный движок ядра ShardingSphere не всегда может корректно выполнить такой запрос.

Экспериментальный Federation execution engine может обрабатывать такие ситуации.    

Например, ``t_order`` и ``t_order_item`` — это шардированные таблицы с несколькими узлами данных, без правил ``binding tables``.
``t_user`` и ``t_user_role`` — одиночные таблицы (single), которые распределены по разным базам данных.

В этом случае Federation engine позволяет выполнять обычные ассоциированные запросы, 
объединяющие эти таблицы, даже если они находятся в разных базах данных:

.. code-block:: sql

    SELECT * FROM t_order o INNER JOIN t_order_item i ON o.order_id = i.order_id WHERE o.order_id = 1;

    SELECT * FROM t_order o INNER JOIN t_user u ON o.user_id = u.user_id WHERE o.user_id = 1;
    
    SELECT * FROM t_order o LEFT JOIN t_user_role r ON o.user_id = r.user_id WHERE o.user_id = 1;

    SELECT * FROM t_order_item i LEFT JOIN t_user u ON i.user_id = u.user_id WHERE i.user_id = 1;

    SELECT * FROM t_order_item i RIGHT JOIN t_user_role r ON i.user_id = r.user_id WHERE i.user_id = 1;

    SELECT * FROM t_user u RIGHT JOIN t_user_role r ON u.user_id = r.user_id WHERE u.user_id = 1;


Не поддерживается
---------------------

.. rubric:: CASE WHEN


- Не поддерживаются запросы с ``CASE WHEN`` с подзапросом:

  .. code-block:: sql

    SELECT 
        CASE WHEN (SELECT COUNT(*) FROM orders WHERE user_id = u.id) > 0 
            THEN 'YES' 
            ELSE 'NO' 
        END AS has_orders
    FROM users u;

- Не поддерживаются запросы с ``CASE WHEN``, внутри которых используются логические имена.

  .. code-block:: sql

    SELECT 
        CASE WHEN t_order.status = 'NEW' THEN 'ACTIVE'
            ELSE 'INACTIVE'
        END AS order_status
    FROM t_order;

  Здесь ``t_order`` — логическое имя, и движок парсинга может его не распознать в ``CASE WHEN``. Решение — использовать алиасы:

  .. code-block:: sql

    SELECT 
        CASE WHEN o.status = 'NEW' THEN 'ACTIVE'
            ELSE 'INACTIVE'
        END AS order_status
    FROM t_order o;

.. rubric:: CTE

Не поддерживаются общие табличные выражения, то есть такого вида (?? надо перепроверить):

.. code-block:: sql

    WITH xxx AS (SELECT ...)
    SELECT ...
    FROM ... 

.. rubric:: Пагинационные запросы


Некоторые виды пагинационных запросов могут не поддерживаться. (?? надо проверить в тестах)

.. rubric:: Запросы с агрегатными функциями


Если запрос содержит несколько агрегатных функций, 
он не поддерживает одновременное использование агрегатных функций с ``DISTINCT`` и агрегатных функций без ``DISTINCT``. 
То есть, например таких:

.. code-block:: sql

    SELECT 
        COUNT(DISTINCT user_id),  
        COUNT(order_id)         
    FROM t_order;

Можно использовать или только ``DISTINCT``, или только без ``DISTINCT`` в одном запросе.

.. rubric:: Вставка с SELECT * 

Невозможно одновременное использование ``SELECT *`` и встроенного механизма 
генерации распределённого первичного ключа (``SNOWFLAKE`` или ``UUID``). Нужно указывать явно список полей. (?? надо проверить в тестах)

.. code-block:: sql

    {INSERT|UPDATE OR INSERT} INTO tbl_name (col1, col2, ...) (SELECT * FROM tbl_name WHERE col3 = ?)


.. rubric:: Запросы с функциями

Если в SQL-запросе столбец используется внутри функции (например, агрегатной — ``MAX, MIN, SUM`` и т. д.), 
необходимо указывать алиас таблицы, а не её имя.

Это требуется для корректной обработки выражений и маршрутизации запросов.

Неверный пример:

.. code-block:: sql

    SELECT MAX(tbl_name.col1) FROM tbl_name 

Правильный пример:

.. code-block:: sql

    SELECT MAX(t.col1) FROM tbl_name t