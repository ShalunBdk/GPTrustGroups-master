# GPTrustGroups

Аддон к GriefPrevention: групповое доверие с исключениями и вложенностью.

## Возможности

- Личные группы игроков (`#name`), уникальные в пределах владельца
- Вложенность групп (`#builders` → `#public`)
- Исключения игроков и групп (перебивают добавление)
- Системная группа `#public` — все игроки
- Интеграция с `/trust`, `/containertrust`, `/accesstrust`, `/permissiontrust`, `/untrust`, `/trustlist`
- Автодополнение групп в командах доверия

## Команды

| Команда | Описание |
|---|---|
| `/trustgroups create <name>` | создать группу |
| `/trustgroups delete <name>` | удалить группу |
| `/trustgroups add <name> <player\|#group>` | добавить игрока / вложить группу |
| `/trustgroups remove <name> <player\|#group>` | убрать игрока / вложение |
| `/trustgroups exclude <name> <player\|#group>` | исключить |
| `/trustgroups unexclude <name> <player\|#group>` | снять исключение |
| `/trustgroups list <name>` | состав группы |
| `/trustgroups info <name>` | подробности |
| `/trustgroups my` | свои группы |

Доверие в приватах: `/trust #group`, `/untrust #group` (стоя внутри привата).

## Зависимости

- Paper/Spigot 1.21+
- [GriefPrevention](https://github.com/GriefPrevention/GriefPrevention)

## Установка

1. Положить `GPTrustGroups.jar` в `plugins/`
2. Запустить сервер
3. При необходимости настроить сообщения в `plugins/GPTrustGroups/messages.yml`

## Логика доступа

- Игрок в `members` группы → доступ
- Игрок в `excludedPlayers` → запрет (перебивает добавление)
- Исключение в подгруппе блокирует доступ через родителя, **если** игрок не добавлен в родителя напрямую
- `#public` доступна всем, кроме исключённых

## Структура данных

`plugins/GPTrustGroups/groups.yml` — приватные группы по владельцам, `public` — системная.
`plugins/GPTrustGroups/messages.yml` — тексты сообщений.