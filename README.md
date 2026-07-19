# Fantasy Earth / 魔幻地球

`Fantasy Earth` is an independently playable fantasy mod. Its public identity, workspace, artifacts and primary namespace are unified as `Fantasy Earth / 魔幻地球` and `fantasy_earth`. Legacy `earth_online_magic` registry ids and `earth_online_arcana.*` player data are retained only as migration inputs.

It has its own starter materials, world generation, progression, creatures, settlements and vanilla fallback routes. Earth on Minecraft, Earth Human and Xuanhuan Earth only add optional material, body-system and shared-mana integrations; none of them is required to play this mod.

Initial target:

- Minecraft: 26.2
- Loader: NeoForge 26.2.0.7-beta
- Java: 25
- Current mod id: `fantasy_earth`
- Current version: `0.7.0-alpha.10`
- Current status: in-development playable beta

`0.4.3` begins the large-mod phase with a configurable arcane-panel key (`M` by default).
Players can switch researched circuits and perform one attunement anywhere at 72% base
efficiency. The focus mat is now an optional full-efficiency, continuous-focus aid rather than
a progression gate. Position, cooldown, support state and rewards remain server-authoritative.

`0.6.0-alpha.1` adds the first complete creature slice. The Runic Watcher is an original
hostile arcane construct with its own entity type, attributes, combat AI, model, animation,
drops, spawn egg and mountain spawn rules. A reloadable settlement catalog now defines witch
hamlets, goblin exchanges, academy outposts, dwarven delvings and elven groves. These entries
are groundwork for structure pools, residents and trade; physical settlements are not generated
in this alpha.

`0.4.2` adds the shared mana spending contract, closes full-mana cooldown bypasses, accepts
Earth on Minecraft material tags directly in arcane facilities, and reports live optional
integration status in the handbook. Earth Human recovery now uses its public API instead of
writing private NBT or assuming fixed body-part capacities.

`0.7.0-alpha.3` separates guidance, research, training and active spells. The Field Arcane
Notebook remains a permanent handbook and is no longer consumed by initiation. Notes only
unlock circuits; the panel and focus mat advance independent level-1-to-10 experience tracks.
The configurable `B` key casts the selected circuit's mana-costed spell. Familiar, watcher and
crystal-spider render states now use real attack progress, sitting posture and distinct motion.

`0.7.0-alpha.4` makes progression rewards operational instead of descriptive. Endurance
reduces real food and saturation loss, body wards reduce entity-sourced combat damage and
fall impact, and breathing research lowers underwater air consumption by a persistent
multiplier. Xuanhuan Earth owns shared-body-effect consumption when both mods are installed,
preventing duplicate application. The handbook now shows live unlock/XP/next-step status,
while attunement, recovery and spell casts drive short-lived synchronized player poses.

`0.7.0-alpha.5` connects the existing systems into a persistent six-step standalone journey:
study initiation notes, complete valid attunement, personally collect a facility output, cast
an active spell, actually restore mana with a consumable, and bind a familiar. The handbook
shows live completion and the next concrete action, while old saves only recover milestones
that existing levels and experience can prove.

`0.7.0-alpha.6` adds a dedicated mod icon built around a brass arcane astrolabe, a violet
aether crystal and a block-shaped Earth. Its silhouette remains distinct from the xuanhuan
golden cultivation ring when both mods appear in the same NeoForge mod list.

`0.7.0-alpha.7` registers the required `minecraft:flying_speed` attribute for mana wisps.
Their `FlyingMoveControl` can now tick safely instead of crashing when a mana wisp is loaded.

`0.7.0-alpha.8` replaces the mod icon's opaque navy square with real PNG transparency while
preserving the brass astrolabe, block Earth, violet crystals and attached cyan arcane accents.

`0.7.0-alpha.9` makes settlement resident initialization incremental and player-aware. Each
anchor persists its spawn progress, only starts within 48 blocks of a player, and the whole
server permits at most two settlement entities to be added per tick. This prevents several
academy outposts or witch hamlets loading together from producing a synchronous entity spike.

`0.7.0-alpha.10` unifies the public brand, workspace, GitHub repository, Java package,
resource namespace, mod id and jar under `Fantasy Earth / 魔幻地球` and `fantasy_earth`.
NeoForge registry aliases migrate old content ids, while shared player data moves to
`earth_arcana.*` on first access.

Design documents:

- [Initial Plan](docs/initial-plan.md)
- [Shared Mana and Magic](docs/shared-mana-and-magic.md)
- [Arcana Field and Meditation Plan](../xuanhuan_earth/docs/arcana-field-and-meditation-plan.md)
- [Ecosystem Integration Contract](docs/ecosystem-integration-contract.md)
- [Large-Scale Development Plan](docs/large-scale-development-plan.md)

## First MVP

Implemented in `neoforge-26.2/`:

- Creative tab: `Fantasy Earth` / `魔幻地球`
- Starter handbook: `field_arcane_notebook`, craftable from one dirt, any planks, or stone crafting materials
- Arcane initiation notes: `arcane_initiation_notes`, crafted from a normal book, arcane dust and amethyst; first use unlocks magic-route contribution without consuming the handbook
- Basic materials: arcane dust, rune ink, ritual chalk, crystallized mana salt, aether glass, rune copper plate, aether crystal, dormant ritual core
- Basic blocks: alchemy table, rune carving table, ritual pedestal, aether crystal cluster
- Bilingual language files: `zh_cn` and `en_us`
- Vanilla fallback recipes; Earth on Minecraft is optional, not required
- First image-generated texture pass:
  - 4 block textures at 64x64
  - 9 item icons at 32x32 with transparent backgrounds
  - Source generation prompts and raw previews are kept under ignored `tmp/imagegen/`
- First aether-field pass:
  - chunk-level `AetherChunkField`
  - aether crystal / ritual / rune / alchemy source terms
  - field disturbance and focus cooldown
  - notebook, initiation notes, adaptation notes and magic blocks show local field feedback
- Portable attunement:
  - configurable arcane-panel key (`M` by default)
  - free one-cycle attunement anywhere at reduced efficiency
  - a real seated pose with occupancy and cleanup safety
  - a server-synced custom screen plus compact four-stage HUD
  - selectable aether, body-ward and breath-ward focuses gated by learned notes
  - distinct mana, fatigue, body-part, air and temporary ward outcomes

Texture note: facilities use per-face exterior textures, distinct active states and separate visual identities for alchemy, rune carving and rituals. This remains an in-development test build.

Mana note: this mod shares the versioned `earth_arcana.*` player-data contract with `xuanhuan_earth`. Magic-route and qi-route bonuses add together when both mods are present, but each mod writes only its own contribution and remains fully playable alone. Aether-field recovery is intentionally separate from xuanhuan qi: magic reads crystal, ritual, rune and alchemy structures, while xuanhuan reads veins, springs, spirit soil and arrays.

Build artifact: `fantasy-earth-neoforge-26.2-0.7.0-alpha.10.jar`.

Build:

```powershell
cd neoforge-26.2
.\gradlew.bat build --no-daemon --offline
```

## License

This project is licensed under `AGPL-3.0-only`. See [LICENSE](LICENSE) for the complete terms.
