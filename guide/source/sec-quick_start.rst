.. _sec:quick_start:

.. raw:: latex

   \setcounter{secnumdepth}{5}

Быстрый старт
================


Данный раздел описывает минимальный набор шагов для запуска *Apache ShardingSphere-Proxy* 
в связке с СУБД Ред База Данных.


1. **Подготовка среды**

   Перед началом работы убедитесь, что:

   - установлена и запущена СУБД Ред База Данных;
   - установлена Java 8+ (JRE/JDK);
   - доступны права на чтение и запись в директории, где будет размещён ShardingSphere-Proxy.

2. **Загрузка и распаковка дистрибутива**

   Скачайте архив сборки: ``apache-shardingsphere-<версия>-shardingsphere-proxy-bin.tar.gz`` (откуда - ??)
   и распакуйте его в удобный каталог, например:

   .. code-block:: bash

     tar -xzf apache-shardingsphere-<версия>-shardingsphere-proxy-bin.tar.gz
     cd apache-shardingsphere-<версия>-shardingsphere-proxy-bin

3. **Создание источников данных**
   
   Создайте пустые базы данных — шарды, на которые будут распределяться данные таблиц при шардировании.
   Разместите их в любом удобном каталоге.

4. **Настройка конфигурационных файлов**

   В каталоге ``conf/`` расположены YAML-файлы конфигурации:

   - ``%SHARDINGSPHERE_PROXY_HOME%/conf/global.yaml`` — общие параметры прокси-сервера;
   - ``%SHARDINGSPHERE_PROXY_HOME%/conf/database-sharding.yaml`` — описание правил шардирования, логических баз данных и источников данных.
     В этом файле необходимо также указать пути к ранее созданным базам данных.

   Подробнее о параметрах конфигурации см. в разделе :ref:`sec:conf_global` и разделе :ref:`sec:conf_database` . 

5. **Импорт драйвера**

   ShardingSphere-Proxy требует наличия JDBC-драйвера для взаимодействия с Ред Базой Данных.

   Скачайте файл ``jaybird-5.0.6.java8.jar`` и поместите его в каталог: 
   ``%SHARDINGSPHERE_PROXY_HOME%/lib``

6. **Запуск сервера**

   Запустите ShardingSphere-Proxy в зависимости от операционной системы.

   - *Windows:*

     .. code-block:: bash

       cd bin
       cmd.exe
       start.bat

   - *Linux:*

     .. code-block:: bash

       sh bin/start.sh

   При успешном запуске появится сообщение:

   ``Starting the ShardingSphere-Proxy ...``

   По умолчанию сервер слушает порт **3307**.

   Информация о запуске и возможных ошибках записывается в файл журнала:
   ``%SHARDINGSPHERE_PROXY_HOME%/logs/stdout.log``

7. **Подключение приложения**

   После успешного запуска клиентское приложение подключается к ShardingSphere-Proxy, 
   а не напрямую к Ред Базе Данных

   Пример строки подключения JDBC:

   .. code-block:: properties

     jdbc:firebirdsql://localhost:3307/sharding_db

   После установления соединения ShardingSphere-Proxy будет обрабатывать SQL-запросы, 
   распределять их между шардами и возвращать результаты так, как если бы использовалась единая база данных.

