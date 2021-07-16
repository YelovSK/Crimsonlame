Hra je 2D top down strielačka.
Ovládanie:
- pohyb: WSAD
- prebíjanie: R
- streľba: klik alebo držanie LMB
- reštart po výhre/prehre: SPACEBAR

Hra obsahuje 3 levely.
1. level: ≈20 nepriateľov, 1 sekunda spawn rate, typy: zombie
2. level: ≈30 nepriateľov, 0.8 sekundy spawn rate, typy: zombie, gun man
3. level: ≈70 nepriateľov, 0.6 sekundy spawn rate, typy: zombie, gun man, spider

3 typy nepriateľov.
Zombie: 50 HP, 0.4 dmg/s, najpomaľší
- dropuje prvú a druhú zbraň a double dmg perk
Gun man: 75 HP, 0.6 dmg/s, hit zbraňou -10 HP, rýchlejší
- dropuje druhú a tretiu zbraň, rýchle prebíjanie a double dmg perky
- strieľa každé ≈3s, rýchlosť projektilu 2.5x pomalšia ako hráčova 
Spider: 100 HP, 0.9 dmg/s, najrýchlejší
- dropuje tretiu zbraň a nesmrteľnosť perk

3 zbrane.
Zbraň 1: 25 dmg, zásobník 10, 5 RPM, prebíjanie 0.75s
Zbraň 2: 15 dmg, zásobník 25, 15 RPM, prebíjanie 1s
Zbraň 3: 50 dmg, zásobník 15, 12 RPM, prebíjanie 2s

3 perky.
Rýchle prebíjanie: na ≈4s je rýchlosť prebíjania 2x		// zmení kurzor na červený
Double damage: na ≈4s je damage zbraní 2x	// zmení obrázok projektilu
Nesmrteľnosť: na ≈3s hráč nedostáva dmg 	// zmení healthbar na zlatý

Každý typ nepriateľa má rozdieľnu šancu na dropnutie konkrétnych zbraní a perkov (upredňostňuje sa zbraň).
Dropy sa po náhodnom čase despawnú.
Strieľanie je nepresnejšie pri pohybe (náhodný uhol ak sa hráč hýbe).

// niekedy sa hra bugne a nejde prebíjať
// javafx.media som importol manuálne z .jar súboru

Vypracované úlohy a bonusová funkcionalita:
Postava: iba jedna postava
Nepriatelia: 3 typy nepriateľov
Zbrane: 3 typy zbraní
Perky: 3 rôzne perky
Levely: 3 levely
Vykreslenie mapy a spadnutých predmetov: áno
Správne vyhodnotenie konca hry: áno (dokončenie tretieho levelu)
Bonusová funkcionalita:
- zvuky
- nepriatelia smerujú za postavou
