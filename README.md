Arwalls
============================
Приложение использует технологию SLAM, реализованную компанией Google в ARCore SDK.
SLAM отслеживает одни и те же точки пространства на разных кадрах. Зная информацию о движении камеры от сенсоров устройства и вычисляя разницу положений точек на соседних картах, алгоритм SLAM производит оценку положения камеры относительно точек пространства и оценку положения точек пространства относительно камеры. Таким образом, две оценки с каждым кадром видеояда уточняют друг друга. ARCore SDK строит мировую систему координат, а Depth API присваивает каждой выделенной точке оценку координат.
Приложение **Arwalls** использует полученные оценки координат и отображает в виде стен точки, находящиеся на высоте устройства. Таким образом, приложение позволяет нарисовать карту помещения изнутри, соблюдая масштаб. На карте отображается текущее местоположение устройства в виде зелёного маркера. 

## Системные требования
Для запуска приложения необходимо устройство под управлением ОС Android, на котором поддержвается ARCore и Depth Api.
[Поддерживаемые устройства](https://developers.google.com/ar/devices)

## Использование
Для построения карты необходимо захватить объём с помощью камеры и плавно сканировать стены на одной высоте с различных ракурсов, не допуская исчезновения с камеры точек глубины.

## Интерфейс и параметры
* Кнопка со значком геопозиции - центрирует карту на текущем местоположении устройства;
* Кнопка со значком мусорного бака очищает карту;
* Кнопка со значком шестерёнки открывает меню настроек:
    * *Показать маркер* - вкючить/выключить отображение маркера текущего местоположения;
    * *Точность* - установить минимальную для отображения достоверность точки (от 0 до 1);
    * *Длина вертикали обзора* - протяжённость по высоте в метрах области, точки которой будут отображаться на карте;
    * *Смещение высоты обзора* - смещение по высоте в метрах от первоначального положения устройства области, точки которой будут отображаться на карте;
    * *Максимальное число видимых точек* - максимальное число точек, которые будут отображаться на камере. Не влияет на качество карты, но может улучшить производительность.

## Информация о технологии

* https://developers.google.com/ar/develop/augmented-images/arcoreimg
* https://developers.google.com/ar/develop/java/depth/overview
* https://developers.googleblog.com/2020/06/a-new-wave-of-ar-realism-with-arcore-depth-api.html
