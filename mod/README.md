# JAI Mod

Клиентский Fabric-мод для сервера **JustMC** (Creative+), Minecraft **1.21.11**.

## Что делает

Команда `/jai code <запрос>`:

1. Отправляет `<запрос>` в нейросеть через **OpenRouter** (модель настраивается).
2. Полученный JSON-модуль JustMC заливает на **paste.rs** (открытый API).
3. Паста автоматически удаляется через 5 минут (DELETE-запрос).
4. Отправляет за игрока команду `/module load-url <ссылка>`.
5. Когда открывается книга установки («ПРИНЯТЬ / ОТКАЗ») — мод сам «нажимает» **ПРИНЯТЬ** (выполняет click-event кнопки).

## Сборка

```bash
cd mod
gradle build
```
Или просто GitHub Actions: workflow `.github/workflows/build-mod.yml` собирает jar при пуше в `mod/**` и выкладывает артефакт `jaimod`.

## Настройка (config/jaimod.json)

| Поле | Описание |
|------|----------|
| `openRouterApiKey` | Ключ с https://openrouter.ai/keys — **впиши свой** |
| `model` | Модель OpenRouter, например `anthropic/claude-3.5-sonnet` |
| `systemPromptExtra` | Системный промпт (можно вставить выдержки из JAI.md) |
| `autoAccept` | Автонажатие «ПРИНЯТЬ» |
| `acceptDelayTicks` | Задержка перед нажатием в тиках |
| `deleteAfterSeconds` | Через сколько секунд удалять пасту (300 = 5 мин) |

## Важно

- Ключ API хранится локально — не коммить его.
- paste.rs — публичный сервис; для приватных миров лучше свой pastebin (`pasteUrl` в конфиге).
