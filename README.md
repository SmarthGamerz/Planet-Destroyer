# Planet Destroyer (Fabric 1.21.11)
Build: `gradle build` (Gradle 8.14+, JDK 21). Verify yarn/fabric-api versions in build.gradle first.

Implemented: items, 3 recipes, bow charge + arrow requirement, server-controlled target/cooldown/damage,
one-after-another planet sequence (zero entities), capped crater (config), spawn-protection check,
S2C sync payload, client state + capped shockwave particles, no commands.

NOT yet implemented (needs art + version-specific render code):
- Textures (add 16x16 PNGs in assets/planetdestroyer/textures/item/)
- Custom 3D bow/arrow/planet models and bow pull predicates
- Cosmic sky/portal/planet rendering (read CosmicState from the client render pipeline)
- sounds.json + .ogg files
- Tooltip lines, camera shake, arrow trail particles
