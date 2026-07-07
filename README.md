# Earth Online: Magic

`Earth Online: Magic` is an independent addon-style mod planned for the Earth Online ecosystem.

It is not part of the Earth Online core mod and should remain optional. The core mod focuses on real-world materials, industry, infrastructure, and common reality-based routes. This mod adds a fantasy/magic layer that can consume those materials when Earth Online is installed, while still providing vanilla fallbacks when it is not.

Initial target:

- Minecraft: 26.2
- Loader: NeoForge 26.2.0.7-beta
- Java: 25
- Current mod id: `earth_online_magic`
- Current version: `0.1.1`
- Current status: first in-development MVP

Design documents:

- [Initial Plan](docs/initial-plan.md)
- [Shared Mana and Magic](docs/shared-mana-and-magic.md)
- [Arcana Field and Meditation Plan](../earth_online_xuanhuan/docs/arcana-field-and-meditation-plan.md)

## First MVP

Implemented in `neoforge-26.2/`:

- Creative tab: `Earth Online: Magic` / `地球 Online：魔幻`
- Starter handbook: `field_arcane_notebook`, craftable from one dirt, any planks, or stone crafting materials
- Arcane initiation notes: `arcane_initiation_notes`, crafted from the handbook plus arcane dust; first use unlocks magic-route contribution to the shared mana value
- Basic materials: arcane dust, rune ink, ritual chalk, crystallized mana salt, aether glass, rune copper plate, aether crystal, dormant ritual core
- Basic blocks: alchemy table, rune carving table, ritual pedestal, aether crystal cluster
- Bilingual language files: `zh_cn` and `en_us`
- Vanilla fallback recipes; Earth Online core is optional, not required
- First image-generated texture pass:
  - 4 block textures at 64x64
  - 9 item icons at 32x32 with transparent backgrounds
  - Source generation prompts and raw previews are kept under ignored `tmp/imagegen/`
- First aether-field pass:
  - chunk-level `AetherChunkField`
  - aether crystal / ritual / rune / alchemy source terms
  - field disturbance and focus cooldown
  - notebook, initiation notes, adaptation notes and magic blocks show local field feedback

Texture note: the first pass replaces vanilla placeholders and is safe for testing.
The workstation blocks still use single-face textures on all cube faces; later polish should
split them into coherent `top` / `front` / `side` / `back` surfaces.

Mana note: this branch shares `earth_online_arcana.*` player data with `earth_online_xuanhuan`. Magic-route and qi-route bonuses add together, but each addon only writes its own contribution so both remain optional. Aether-field recovery is intentionally separate from xuanhuan qi: magic reads crystal, ritual, rune and alchemy structures, while xuanhuan reads veins, springs, spirit soil and arrays.

Build:

```powershell
cd neoforge-26.2
.\gradlew.bat build --no-daemon --offline
```
