# Minecraft Beta 1.7.3 — Нативний порт для Mac (Apple Silicon ARM64)

Цей проєкт є нативним портом легендарної версії **Minecraft Beta 1.7.3** під архітектуру **Apple Silicon (M1 / M2 / M3 / M4)** на базі **Java 17 LTS (ARM64)** та сучасного графічного стеку **LWJGL 3 (GLFW + OpenGL + OpenAL)**.

---

## 🚀 Основні переваги та оптимізації

1. **100% Нативний ARM64 запуск (без Rosetta 2)**:
   - Відсутність трансляції інструкцій, низьке енергоспоживання, ноутбук залишається холодним.
   - Використовує 64-бітний нативний OpenJDK 17 з високоефективним JIT-компілятором.
2. **Сучасний віконний рушій LWJGL 3 (GLFW)**:
   - Замінено застарілий LWJGL 2 та AWT Canvas на нативне вікно GLFW / Cocoa.
   - Головний цикл виконання повністю відповідає вимогам macOS (`-XstartOnFirstThread`).
3. **Підтримка ProMotion (120 FPS) та VSync**:
   - Частота оновлення кадрів підлаштовується під 120 Гц дисплеї MacBook Pro.
   - Плавний frametime без затримок і розривів екрана.
4. **Коректне захоплення миші (Raw Input)**:
   - Використовує `GLFW_CURSOR_DISABLED` для плавного керування оглядом без стрибків і зміщення курсора на трекпадах Apple і Magic Mouse.
5. **Повна автономність аудіо (OpenAL)**:
   - Всі 220 оригінальних звуків та музики C418 вбудовані локально. Гра не намагається зв'язуватися з непрацюючими серверами 2011 року.

---

## 🎮 Як запускати гру

### Варіант 1: Через термінал (Gradle)
Для швидкого запуску гри в режимі розробки:
```bash
./gradlew run
```

### Варіант 2: Запуск через IntelliJ IDEA
1. Відкрийте цю папку в **IntelliJ IDEA**.
2. Дочекайтеся індексації Gradle-проєкту.
3. Запустіть конфігурацію `Minecraft` (або клас [`net.minecraft.client.Minecraft`](file:///Users/marko/Проекти/minecraft%20beta%201.7.3%20for%20mac%20(apple%20silicon)/src/main/java/net/minecraft/client/Minecraft.java)).
4. Переконайтеся, що у параметрах запуску (VM options) додано:
   ```text
   -XstartOnFirstThread -Xmx2G -Dminecraft.dir=run
   ```

### Варіант 3: Запуск у VS Code / Antigravity
Просто натисніть **F5** (Run & Debug) — конфігурацію `.vscode/launch.json` уже налаштовано.

---

## 📦 Створення автономного додатку macOS (.app)

Ви можете зібрати повноцінний додаток для macOS з власною іконкою високої чіткості:
```bash
./gradlew bundleApp
```
Готовий додаток буде створено за шляхом:
```text
build/apps/Minecraft Beta 1.7.3.app
```
Ви можете відкрити його прямо з Finder або перетягнути у системну папку `/Applications`.

---

## 📂 Структура проєкту

* [`src/main/java/net/minecraft/`](file:///Users/marko/Проекти/minecraft%20beta%201.7.3%20for%20mac%20(apple%20silicon)/src/main/java/net/minecraft/): повний декомпільований та адаптований вихідний код Beta 1.7.3.
* [`src/main/java/org/lwjgl/`](file:///Users/marko/Проекти/minecraft%20beta%201.7.3%20for%20mac%20(apple%20silicon)/src/main/java/org/lwjgl/): сумісний шар вікна (Display, Keyboard, Mouse, GLU) на базі LWJGL 3 GLFW.
* [`src/main/resources/`](file:///Users/marko/Проекти/minecraft%20beta%201.7.3%20for%20mac%20(apple%20silicon)/src/main/resources/): текстури, шрифти, моделі, іконка `minecraft.icns` та звуки.
* [`run/`](file:///Users/marko/Проекти/minecraft%20beta%201.7.3%20for%20mac%20(apple%20silicon)/run/): ізольована робоча директорія гри для збережень світів, налаштувань (`options.txt`) та скріншотів.

