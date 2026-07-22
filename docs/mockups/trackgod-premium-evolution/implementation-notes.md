# TrackGod Premium Evolution · Umsetzungsempfehlung

## Kurzurteil

TrackGod hat bereits eine erkennbare, differenzierte Marke. Der größte Qualitätsgewinn kommt nicht von einem neuen Stil, sondern von einer strengeren Hierarchie: weniger gleichzeitig laute Flächen, Rot nur für Bedeutung, konsistente Typografie und ein noch schnellerer Satz-Logging-Flow.

## Was unbedingt bleiben sollte

- Schwarz/Rot, Null-Radius und Industrial-Brutalism
- TrackGod-Wortwelt wie Altar, Ritual und Transmissions
- Offline-/Privacy-Positionierung
- Swipe- und Bottom-Navigation der vier Hauptbereiche
- große Zahlen mit kleinen Einheiten
- direkte Plus/Minus-Erfassung während des Trainings

## Priorität 1 · Workout Session

Höchster täglicher Wert, geringstes Produkt-Risiko.

- Das große `END`-Element aus `WorkoutSessionScreen.kt:287` in die Toolbar verschieben und visuell klar sekundär behandeln.
- Übungsname, letzter Satz und Empfehlung als einen zusammenhängenden Kontextblock gestalten.
- Gewicht und Wiederholungen als stabile Einhand-Zone aufbauen; Touch-Ziele mindestens 48dp.
- `LOG SET` dauerhaft als stärkste Aktion behandeln und die bereits vorhandene Bestätigung ausbauen.
- `PLATE CALCULATOR` bei `WorkoutSessionScreen.kt:903` als mindestens 44dp hohe Textaktion oder Bottom-Sheet-Handle ausführen.
- Set-Typ nicht nur als einzelnes `W`, sondern als verständliches `WORK / WARM / DROP / FAIL`-Steuerelement zeigen.

## Priorität 2 · Altar

- Die nächste konkrete Aktion hervorheben: nächstes Ritual plus klarer Start-CTA statt des abstrakteren `START NEW` bei `AltarScreen.kt:233`.
- Tageswerte von vier großen Karten auf eine kompakte Dreierzeile reduzieren.
- Weekly Goal und Tagesmarker als eine Einheit behandeln.
- Die Spannung zwischen „kein Scroll auf Daily Screens“ und dem aktuellen `verticalScroll` bei `AltarScreen.kt:138` auflösen: Above-the-fold bleibt fest, Saved Rituals und History werden nachgelagert.
- Recent Workouts auf zwei Einträge begrenzen und „View all“ eindeutig zur History führen.

## Priorität 3 · History

- Die siebentägige Date-Picker-Zeile aus `HistoryScreen.kt:239` optional machen; standardmäßig `Week / Month / All` plus Suche.
- Datum, Name, Dauer/Sätze und Volumen konsequent auf denselben Achsen anordnen.
- 9sp-Sonderwerte wie `HistoryScreen.kt:337` und `HistoryScreen.kt:566` vermeiden; kleine Labels auf eine gut lesbare Mindeststufe anheben.
- Details erst beim Öffnen einer Session zeigen, nicht jede Karte mit gleich vielen Unterebenen belasten.

## Priorität 4 · Stats

- Pro Zeitraum genau eine Hero-Metrik und eine klare Diagrammreihenfolge.
- Vorhandene `TimeRangeChips` bei `StatsScreen.kt:723` behalten, aber als konsistente segmentierte Kontrolle vereinfachen.
- Volume Progression und Heatmap zuerst; spezialisierte Analysen darunter progressiv offenlegen.
- Diagrammflächen vergrößern und Rahmen/Container reduzieren.

## Design-System-Pass vor der Screen-Umsetzung

1. Sechs verbindliche Typografiestufen statt vieler `fontSize`-/`letterSpacing`-Overrides.
2. Zwei Flächenstufen für Standardinhalt, eine für aktive/elevated Zustände.
3. Rot ausschließlich für Primäraktion, aktiv, live und echte Warnung.
4. Drei Button-Varianten: Primary, Secondary, Ghost; jeweils 48dp Standardhöhe.
5. Interaktive Textaktionen und Icon-Buttons mindestens 44×44dp.
6. Ein Motion-System: Press 80ms, Standardtransition 160–200ms, Confirmation 400–600ms.

## Empfohlene Lieferreihenfolge

1. Typografie-, Touch- und Button-Tokens
2. Workout Session
3. Altar
4. History
5. Stats
6. Profile, Settings und Onboarding als abschließender Konsistenz-Pass

Diese Reihenfolge verbessert zuerst die täglich meistgenutzte Strecke und lässt Datenmodell, Navigation und Store-Positionierung unverändert.

