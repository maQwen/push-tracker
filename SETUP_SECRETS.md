# GitHub Secrets для подписи APK

Для автоматической сборки и подписи релизных версий приложения в GitHub Actions необходимо настроить следующие секреты в репозитории.

## Настройка секретов

Перейдите в настройки репозитория на GitHub: **Settings → Secrets and variables → Actions** и добавьте следующие секреты:

### 1. KEYSTORE_BASE64

Base64-кодированный файл хранилища ключей (keystore).

**Как получить:**
```bash
base64 release.keystore | tr -d '\n' | pbcopy
```

Или на Linux:
```bash
base64 release.keystore | tr -d '\n' | xclip -selection clipboard
```

Затем вставьте значение в поле секрета `KEYSTORE_BASE64`.

### 2. KEYSTORE_PASSWORD

Пароль от хранилища ключей (keystore).

**Пример:**
```
MyKeystorePassword123!
```

### 3. KEY_ALIAS

Алиас ключа в хранилище.

**Пример:**
```
mykey
```

### 4. KEY_PASSWORD

Пароль от ключа подписи.

**Пример:**
```
MyKeyPassword456!
```

## Создание Keystore

Если у вас еще нет хранилища ключей, создайте его с помощью команды:

```bash
keytool -genkey -v \
  -keystore release.keystore \
  -alias mykey \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000
```

Вам будет предложено ввести:
- Пароль от хранилища (KEYSTORE_PASSWORD)
- Имя и фамилию
- Название организации
- Пароль от ключа (KEY_PASSWORD) - можно нажать Enter, чтобы использовать тот же пароль

**Важно:** Сохраните keystore файл в надежном месте! Без него вы не сможете обновлять приложение в Google Play Store.

## Локальная сборка с подписью

Для локальной сборки релизной версии с подписью установите переменные окружения:

### macOS/Linux:
```bash
export KEYSTORE_PATH=./release.keystore
export KEYSTORE_PASSWORD=your_keystore_password
export KEY_ALIAS=your_alias
export KEY_PASSWORD=your_key_password

./gradlew assembleRelease
```

### Windows (PowerShell):
```powershell
$env:KEYSTORE_PATH="./release.keystore"
$env:KEYSTORE_PASSWORD="your_keystore_password"
$env:KEY_ALIAS="your_alias"
$env:KEY_PASSWORD="your_key_password"

.\gradlew assembleRelease
```

### Windows (CMD):
```cmd
set KEYSTORE_PATH=./release.keystore
set KEYSTORE_PASSWORD=your_keystore_password
set KEY_ALIAS=your_alias
set KEY_PASSWORD=your_key_password

gradlew assembleRelease
```

## Автоматическая сборка в GitHub Actions

После настройки секретов, при каждом пуше в ветку `main`/`master` или создании тела версии, GitHub Actions автоматически:

1. Восстановит keystore из Base64
2. Соберет debug и release версии APK
3. Подпишет release версию используя ваши секреты
4. Загрузит APK как артефакты
5. При создании тега (например, `v1.0.0`) создаст релиз на GitHub с прикрепленными APK

## Безопасность

⚠️ **Важные рекомендации по безопасности:**

1. **Никогда не коммитьте keystore файл в репозиторий!**
2. Используйте сложные пароли для хранилища и ключа
3. Ограничьте доступ к секретам GitHub только доверенным лицам
4. Регулярно обновляйте пароли
5. Создавайте резервные копии keystore в надежном месте (менеджер паролей, зашифрованное хранилище)
6. Для тестирования используйте отдельный debug keystore

## Проверка подписи APK

После сборки вы можете проверить подпись APK:

```bash
apksigner verify --verbose app/build/outputs/apk/release/app-release.apk
```

Или используйте Android Studio: **Build → Analyze APK**.
